package solucionesTareasAsyncronas;

// Practica 13 - Usar un executor propio con supplyAsync()
// Con un pool de 3 hilos, varias tareas REUTILIZAN los mismos hilos:
// el numero de hilo (nombre) se repite. No se crea un hilo nuevo por tarea.

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Practica13_ExecutorPersonalizado {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        List<CompletableFuture<String>> tareas = java.util.stream.IntStream
                .rangeClosed(1, 5)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                    System.out.println("Tarea " + i + " - hilo: "
                            + Thread.currentThread().getName());
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return "ok-" + i;
                }, executor))
                .toList();

        CompletableFuture.allOf(tareas.toArray(new CompletableFuture[0])).join();

        System.out.println("Resultados: " + tareas.stream().map(CompletableFuture::join).toList());
        System.out.println("Observa que pool-1-thread-1/2/3 se REUTILIZAN entre tareas.");

        executor.shutdown();
    }
}
