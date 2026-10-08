package solucionesTareasAsyncronas;

// Practica 14 - Procesamiento paralelo de una lista
// Ampliacion: con 1 s de retardo por calculo, comparamos secuencial vs concurrente.

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Practica14_ProcesamientoParalelo {

    static final List<Integer> NUMEROS = List.of(1, 2, 3, 4, 5);

    public static void main(String[] args) {
        // --- Version basica: calcular y mostrar los cuadrados ---
        List<CompletableFuture<String>> tareas = NUMEROS.stream()
                .map(n -> CompletableFuture.supplyAsync(() -> n + " → " + (n * n)))
                .toList();

        CompletableFuture.allOf(tareas.toArray(new CompletableFuture[0])).join();

        tareas.stream()
                .map(CompletableFuture::join)
                .forEach(System.out::println);

        // --- Ampliacion: cada calculo tarda 1 s ---
        System.out.println("\n--- Comparativa con calculo de 1 s por elemento ---");

        long inicio = System.currentTimeMillis();
        NUMEROS.forEach(n -> System.out.println("  sec: " + n + " → " + cuadradoLento(n)));
        System.out.println("Secuencial: " + (System.currentTimeMillis() - inicio) + " ms (~5 s)");

        inicio = System.currentTimeMillis();
        List<CompletableFuture<Integer>> paralelas = NUMEROS.stream()
                .map(n -> CompletableFuture.supplyAsync(() -> cuadradoLento(n)))
                .toList();
        CompletableFuture.allOf(paralelas.toArray(new CompletableFuture[0])).join();
        paralelas.stream().map(CompletableFuture::join)
                .forEach(c -> System.out.println("  par: → " + c));
        System.out.println("Concurrente: " + (System.currentTimeMillis() - inicio) + " ms (~1 s)");
    }

    static int cuadradoLento(int n) {
        try {
            Thread.sleep(1000); // simula un calculo costoso
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return n * n;
    }
}
