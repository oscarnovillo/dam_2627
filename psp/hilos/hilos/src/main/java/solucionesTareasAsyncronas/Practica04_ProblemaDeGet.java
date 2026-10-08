package solucionesTareasAsyncronas;

// Practica 4 - El problema de get()
//
// CONCLUSION: el orden de las llamadas a get() solo afecta al ORDEN EN QUE
// EL HILO PRINCIPAL ESPERA. Las tareas ya estan corriendo en paralelo desde
// el submit(); pedir f2.get() antes que f1.get() no hace que la tarea 2
// empiece antes. get() solo espera si la tarea aun no ha terminado.
// Cambiar el orden NO cambia los resultados: siempre son 60 y ~3 s.

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Practica04_ProblemaDeGet {

    static Future<Integer> tarea(ExecutorService executor, int segundos, int valor) {
        return executor.submit(() -> {
            Thread.sleep(segundos * 1000);
            return valor;
        });
    }

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        Future<Integer> f1 = tarea(executor, 2, 10); // 2 s
        Future<Integer> f2 = tarea(executor, 1, 20); // 1 s
        Future<Integer> f3 = tarea(executor, 3, 30); // 3 s

        long inicio = System.currentTimeMillis();

        // Orden original: f1, f2, f3
        int total1 = f1.get() + f2.get() + f3.get();
        System.out.println("Orden f1-f2-f3 -> total: " + total1
                + " en " + (System.currentTimeMillis() - inicio) + " ms");

        Future<Integer> g1 = tarea(executor, 2, 10);
        Future<Integer> g2 = tarea(executor, 1, 20);
        Future<Integer> g3 = tarea(executor, 3, 30);

        inicio = System.currentTimeMillis();

        // Orden cambiado: f2 primero. El total y el tiempo NO cambian.
        int total2 = g2.get() + g1.get() + g3.get();
        System.out.println("Orden f2-f1-f3 -> total: " + total2
                + " en " + (System.currentTimeMillis() - inicio) + " ms");

        executor.shutdown();
    }
}
