package solucionesTareasAsyncronas;

// Practica 12 - Timeout con orTimeout() y completeOnTimeout()
//
// orTimeout(3, SEGUNDOS): si la tarea tarda mas, se completa con
// TimeoutException (la lanzamos como RuntimeException en el join).
// completeOnTimeout(valor, 3, SEGUNDOS): en vez de fallar, devuelve el valor por defecto.

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Practica12_Timeout {

    public static void main(String[] args) {
        // --- Version 1: orTimeout -> controlamos el error ---
        try {
            String r1 = CompletableFuture
                    .supplyAsync(() -> {
                        sleep(10000); // tarda 10 s...
                        return "terminado";
                    })
                    .orTimeout(3, TimeUnit.SECONDS) // ...pero solo esperamos 3 s
                    .join();
            System.out.println("V1: " + r1);
        } catch (RuntimeException e) { // TimeoutException envuelto en CompletionException/RuntimeException
            System.out.println("V1: tiempo agotado (TimeoutException): " + e.getCause());
        }

        // --- Version 2: completeOnTimeout -> valor por defecto ---
        String r2 = CompletableFuture
                .supplyAsync(() -> {
                    sleep(10000);
                    return "terminado";
                })
                .completeOnTimeout("Resultado no disponible", 3, TimeUnit.SECONDS)
                .join();
        System.out.println("V2: " + r2);
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
