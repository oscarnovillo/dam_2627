package solucionesEjerciciosHilosJava25;
// Ejercicio 1.2 - 10.000 descargas simultaneas: hilos de plataforma vs hilos virtuales
//
// Los hilos virtuales (Java 21+, disponibles en 25) son gestionados por la JVM
// sobre la base de "desmontar" la pila cuando se bloquean (ej. en sleep), lo que
// permite millones de hilos con una fraccion de la memoria de los hilos de plataforma.

import java.util.ArrayList;
import java.util.List;

public class Ejercicio1_2_DescargasVirtuales {

    public static void main(String[] args) throws InterruptedException {
        final int N = 10_000;

        long t0 = System.currentTimeMillis();
        lanzarDescargas(N, false);
        long msPlataforma = System.currentTimeMillis() - t0;

        t0 = System.currentTimeMillis();
        lanzarDescargas(N, true);
        long msVirtuales = System.currentTimeMillis() - t0;

        System.out.printf("%,d descargas | plataforma: %d ms | virtuales: %d ms%n",
                N, msPlataforma, msVirtuales);
        System.out.println("Los hilos virtuales ademas consumen muchisima menos memoria:");
        System.out.println("~1 MB de pila por hilo de plataforma vs ~pocos KB de monton por hilo virtual.");
    }

    static void lanzarDescargas(int n, boolean virtuales) throws InterruptedException {
        List<Thread> hilos = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            final int id = i;
            Runnable tarea = () -> {
                try {
                    Thread.sleep(50); // simula el tiempo de descarga
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            };
            Thread h = virtuales
                    ? Thread.ofVirtual().name("v-descarga-" + id).start(tarea)
                    : Thread.ofPlatform().daemon().name("p-descarga-" + id).start(tarea);
            hilos.add(h);
        }
        for (Thread h : hilos) {
            h.join();
        }
    }
}
