package solucionesTareasAsyncronas;
// Practica 1 - Primer Future
// Conceptos: Callable + ExecutorService + Future + get()

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Practica01_PrimerFuture {

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Integer> tarea = () -> {
            Thread.sleep(2000); // la tarea tarda 2 segundos
            return 42;
        };

        Future<Integer> future = executor.submit(tarea);

        System.out.println("Tarea enviada");
        System.out.println("Esperando resultado...");

        Integer resultado = future.get(); // bloquea hasta que la tarea termine

        System.out.println("Resultado: " + resultado);

        executor.shutdown();
    }
}
