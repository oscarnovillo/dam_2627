package solucionesTareasAsyncronas;

// Practica 15 - Contador concurrente: int vs AtomicInteger
//
// CONCLUSION: CompletableFuture NO soluciona los problemas de concurrencia.
// El incremento "contador++" no es atomico (leer + sumar + escribir): con 10
// tareas x 10.000 incrementos en paralelo se pierden actualizaciones y casi
// nunca da 100.000. Con AtomicInteger el resultado es EXACTO siempre.

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Practica15_ContadorConcurrente {

    static int contadorNormal = 0;          // NO seguro
    static final AtomicInteger contadorAtomico = new AtomicInteger(0); // seguro

    public static void main(String[] args) {
        // --- Con int normal: resultado incorrecto (casi nunca 100.000) ---
        CompletableFuture<?>[] tareasNormal = IntStream.range(0, 10)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    for (int j = 0; j < 10_000; j++) {
                        contadorNormal++; // condicion de carrera
                    }
                }))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(tareasNormal).join();
        System.out.println("int normal:        " + contadorNormal + " (esperado: 100.000) <- INCORRECTO");

        // --- Con AtomicInteger: resultado exacto ---
        CompletableFuture<?>[] tareasAtomicas = IntStream.range(0, 10)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    for (int j = 0; j < 10_000; j++) {
                        contadorAtomico.incrementAndGet(); // atomico
                    }
                }))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(tareasAtomicas).join();
        System.out.println("AtomicInteger:     " + contadorAtomico.get() + " <- CORRECTO");
    }
}
