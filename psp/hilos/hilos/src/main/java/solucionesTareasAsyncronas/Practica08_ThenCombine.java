package solucionesTareasAsyncronas;

// Practica 8 - Dos tareas independientes con thenCombine()
//
// PREGUNTA: ¿por que no hacer temperatura.join(); humedad.join(); antes de combinar?
// RESPUESTA: join() BLOQUEA el hilo llamante. Si hacemos ambos join() seguidos,
// el hilo principal espera primero 2 s (temperatura) y DESPUES 3 s (humedad),
// total ~5 s: las tareas se lanzan en paralelo, pero las ESPERAMOS en serie.
// thenCombine() registra un callback que se ejecuta cuando AMBAS terminan,
// sin bloquear el hilo principal: el total es ~3 s (la mas lenta).

import java.util.concurrent.CompletableFuture;

public class Practica08_ThenCombine {

    static CompletableFuture<Integer> consultarTemperatura() {
        return CompletableFuture.supplyAsync(() -> {
            sleep(2000);
            return 25;
        });
    }

    static CompletableFuture<Integer> consultarHumedad() {
        return CompletableFuture.supplyAsync(() -> {
            sleep(3000);
            return 60;
        });
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        long inicio = System.currentTimeMillis();

        String resultado = consultarTemperatura()
                .thenCombine(consultarHumedad(), (temperatura, humedad) ->
                        "Temperatura: " + temperatura + " ºC\nHumedad: " + humedad + " %")
                .join();

        System.out.println(resultado);
        System.out.println("Tiempo total: " + (System.currentTimeMillis() - inicio)
                + " ms (~3 s: la tarea mas lenta, no la suma)");

        // Version ANTI-PATRON (no hacer): join() + join() en serie -> ~5 s
        // CompletableFuture<Integer> t = consultarTemperatura();
        // CompletableFuture<Integer> h = consultarHumedad();
        // System.out.println("Temperatura: " + t.join() + " ºC"); // espera 2 s
        // System.out.println("Humedad: " + h.join() + " %");       // espera 3 s mas
    }
}
