package solucionesTareasAsyncronas;

// RETO FINAL - Proceso de compra
// Tres operaciones independientes en paralelo (stock, precio, envio).
// Si TODAS tienen exito -> "Compra confirmada"; si alguna falla -> "Compra no disponible".
// Incluye: CompletableFuture + thenCombine/allOf + errores + timeout + executor propio.

import java.util.concurrent.*;

public class RetoFinal_ProcesoDeCompra {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        CompletableFuture<Boolean> comprobarStock = CompletableFuture.supplyAsync(() -> {
            System.out.println("[stock] hilo: " + Thread.currentThread().getName());
            sleep(1500);
            return true; // hay stock
        }, executor).orTimeout(3, TimeUnit.SECONDS);

        CompletableFuture<Double> calcularPrecio = CompletableFuture.supplyAsync(() -> {
            System.out.println("[precio] hilo: " + Thread.currentThread().getName());
            sleep(2000);
            return 49.99;
        }, executor).orTimeout(3, TimeUnit.SECONDS);

        CompletableFuture<String> consultarEnvio = CompletableFuture.supplyAsync(() -> {
            System.out.println("[envío] hilo: " + Thread.currentThread().getName());
            sleep(2500);
            if (Math.random() < 0.3) {
                throw new RuntimeException("No hay cobertura de envío");
            }
            return "Entrega en 24-48 h";
        }, executor).orTimeout(3, TimeUnit.SECONDS);

        long inicio = System.currentTimeMillis();

        // allOf: espera a las tres en paralelo; handle: exito o fallo en un solo punto
        String resultado = CompletableFuture
                .allOf(comprobarStock, calcularPrecio, consultarEnvio)
                .handle((vacio, error) -> {
                    if (error != null) {
                        return "Compra no disponible ("
                                + error.getCause().getMessage() + ")";
                    }
                    System.out.println("Stock: OK | Precio: " + calcularPrecio.join()
                            + " € | Envío: " + consultarEnvio.join());
                    return "Compra confirmada";
                })
                .join();

        System.out.println("\n" + resultado);
        System.out.println("Tiempo total: " + (System.currentTimeMillis() - inicio)
                + " ms (~2,5 s: la operacion mas lenta, no la suma)");

        executor.shutdown();
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
