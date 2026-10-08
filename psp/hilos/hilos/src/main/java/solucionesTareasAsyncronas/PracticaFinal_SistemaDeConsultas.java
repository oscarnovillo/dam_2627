package solucionesTareasAsyncronas;

// PRACTICA FINAL - Sistema de consultas a tres servicios
// CompletableFuture + supplyAsync + executor propio + allOf + errores + timeout
// Ampliacion: si estadisticas falla, continua con "Estadisticas: NO DISPONIBLES".

import java.util.concurrent.*;

public class PracticaFinal_SistemaDeConsultas {

    static void sleepEntre(int min, int max) {
        try {
            Thread.sleep((min + (int) (Math.random() * (max - min))) * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static CompletableFuture<String> servicioUsuario(ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("[usuario] en hilo " + Thread.currentThread().getName());
            sleepEntre(1, 3);
            return "Oscar";
        }, executor).orTimeout(4, TimeUnit.SECONDS);
    }

    static CompletableFuture<Integer> servicioPedidos(ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("[pedidos] en hilo " + Thread.currentThread().getName());
            sleepEntre(1, 3);
            return 8;
        }, executor).orTimeout(4, TimeUnit.SECONDS);
    }

    static CompletableFuture<String> servicioEstadisticas(ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("[estadisticas] en hilo " + Thread.currentThread().getName());
            sleepEntre(1, 3);
            if (Math.random() < 0.5) { // simula fallos aleatorios
                throw new RuntimeException("Servicio de estadísticas caído");
            }
            return "1250 visitas";
        }, executor)
        .orTimeout(4, TimeUnit.SECONDS)
        .exceptionally(ex -> "NO DISPONIBLES"); // Ampliacion: el programa continua
    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        long inicio = System.currentTimeMillis();

        CompletableFuture<String> usuario = servicioUsuario(executor);
        CompletableFuture<Integer> pedidos = servicioPedidos(executor);
        CompletableFuture<String> estadisticas = servicioEstadisticas(executor);

        CompletableFuture<Void> todas = CompletableFuture.allOf(usuario, pedidos, estadisticas);

        todas.join(); // las 3 consultas se hacen EN PARALELO

        System.out.println("\n===== RESULTADO =====");
        System.out.println("Usuario: " + usuario.join());
        System.out.println("Pedidos: " + pedidos.join());
        System.out.println("Estadísticas: " + estadisticas.join());
        System.out.println("=====================");
        System.out.println("Tiempo total: " + (System.currentTimeMillis() - inicio) + " ms");

        executor.shutdown();
    }
}
