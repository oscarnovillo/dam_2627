package solucionesTareasAsyncronas;

// Practica 2 - Comprobar si ha terminado (isDone)
// Ampliacion: mostramos tambien el nombre del hilo que ejecuta la tarea.

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Practica02_IsDone {

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<Integer> future = executor.submit(() -> {
            System.out.println("Ejecutando la tarea en el hilo: "
                    + Thread.currentThread().getName());
            Thread.sleep(5000);
            return 100;
        });

        // No usamos get() de inmediato: comprobamos isDone() en un bucle
        while (!future.isDone()) {
            System.out.println("Esperando...");
            Thread.sleep(500);
        }

        System.out.println("Resultado: " + future.get());

        executor.shutdown();
    }
}
