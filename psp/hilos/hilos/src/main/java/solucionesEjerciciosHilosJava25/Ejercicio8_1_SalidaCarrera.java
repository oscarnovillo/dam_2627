package solucionesEjerciciosHilosJava25;

// Ejercicio 8.1 - Salida de carrera (CountDownLatch)
//
// Primer latch (5): los corredores avisan de que estan listos.
// El juez espera a ese latch y libera el segundo latch (1) = senyal de salida.

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Ejercicio8_1_SalidaCarrera {

    public static void main(String[] args) throws InterruptedException {
        final int NUM_CORREDORES = 5;
        CountDownLatch todosListos = new CountDownLatch(NUM_CORREDORES);
        CountDownLatch senyalSalida = new CountDownLatch(1);
        List<Thread> corredores = new ArrayList<>();

        for (int i = 1; i <= NUM_CORREDORES; i++) {
            final int dorsal = i;
            Thread corredor = Thread.ofPlatform().name("corredor-" + dorsal).start(() -> {
                try {
                    Thread.sleep(new Random().nextInt(3000)); // calentamiento
                    System.out.println(Thread.currentThread().getName() + " LISTO (faltan "
                            + (todosListos.getCount() - 1) + ")");
                    todosListos.countDown();   // avisa que esta listo
                    senyalSalida.await();      // espera la senyal del juez
                    System.out.println(Thread.currentThread().getName() + " >>> ARRANCA!");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            corredores.add(corredor);
        }

        todosListos.await(); // el juez espera a los 5
        System.out.println("JUEZ: todos listos -> !DAD LA SALIDA!");
        senyalSalida.countDown(); // libera a los corredores

        for (Thread c : corredores) c.join();
    }
}
