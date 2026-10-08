package solucionesEjerciciosHilosJava25;

// Ejercicio 8.2 - Trabajo en fases (CyclicBarrier)
//
// CyclicBarrier se REUTILIZA en cada ciclo: tras la fase 1, la barrera
// se reinicia sola para la fase 2, etc. La accion de barrera se ejecuta
// UNA vez por ciclo, cuando llega el ultimo hilo, ANTES de liberar a todos.

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;

public class Ejercicio8_2_TrabajoEnFases {

    public static void main(String[] args) throws InterruptedException {
        final int MIEMBROS = 4;
        final int FASES = 3;

        CyclicBarrier barrera = new CyclicBarrier(MIEMBROS,
                () -> System.out.println(">> Fase completada, todos avanzan <<"));

        List<Thread> equipo = new ArrayList<>();
        for (int i = 1; i <= MIEMBROS; i++) {
            final int id = i;
            Thread miembro = Thread.ofPlatform().name("miembro-" + id).start(() -> {
                try {
                    for (int fase = 1; fase <= FASES; fase++) {
                        Thread.sleep(500 + new Random().nextInt(1000)); // trabaja
                        System.out.println(Thread.currentThread().getName() + " termino la fase " + fase);
                        barrera.await(); // espera al resto antes de la siguiente fase
                    }
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            });
            equipo.add(miembro);
        }

        for (Thread m : equipo) m.join();
        System.out.println("Proyecto completado: las 3 fases terminadas por los 4 miembros.");
    }
}
