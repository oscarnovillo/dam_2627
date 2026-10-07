package solucionesTareasAsyncronas;

// Practica 3 - Varias tareas en paralelo y medicion del tiempo
//
// PREGUNTA: ¿por que tarda ~3 s y no 6 s (2+1+3)?
// RESPUESTA: las tres tareas se ejecutan EN PARALELO en un pool de 3 hilos.
// El tiempo total lo marca la tarea mas lenta (3 s), no la suma de todas.

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Practica03_VariasTareas {

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        long inicio = System.currentTimeMillis();

        Future<Integer> t1 = executor.submit(() -> {
            Thread.sleep(2000);
            return 10;
        });
        Future<Integer> t2 = executor.submit(() -> {
            Thread.sleep(1000);
            return 20;
        });
        Future<Integer> t3 = executor.submit(() -> {
            Thread.sleep(3000);
            return 30;
        });

        int total = t1.get() + t2.get() + t3.get();

        long fin = System.currentTimeMillis();

        System.out.println("Resultado total: " + total);
        System.out.println("Tiempo total: " + (fin - inicio) + " ms (aprox. 3 s)");
        System.out.println("No son 6 s porque las tareas corren en PARALELO.");

        executor.shutdown();
    }
}
