package solucionesEjerciciosHilosJava25;

// Ejercicio 7.1 - Suma paralela de un array grande
// ExecutorService + Callable<Long> + Future<Long>

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Ejercicio7_1_SumaParalela {

    public static void main(String[] args) throws Exception {
        int[] numeros = new int[10_000_000];
        Arrays.fill(numeros, 1);
        long esperada = numeros.length;

        try (ExecutorService pool = Executors.newFixedThreadPool(4)) {
            int trozos = 4;
            int tamanyo = numeros.length / trozos;
            List<Callable<Long>> tareas = new ArrayList<>();

            for (int i = 0; i < trozos; i++) {
                final int inicio = i * tamanyo;
                final int fin = (i == trozos - 1) ? numeros.length : inicio + tamanyo;
                tareas.add(() -> {
                    long suma = 0;
                    for (int j = inicio; j < fin; j++) {
                        suma += numeros[j];
                    }
                    return suma;
                });
            }

            List<Future<Long>> resultados = pool.invokeAll(tareas);

            long total = 0;
            for (Future<Long> f : resultados) {
                total += f.get(); // bloquea hasta obtener cada resultado
            }

            System.out.println("Suma total: " + total + " (esperada: " + esperada + ")");
            System.out.println(total == esperada ? "OK" : "ERROR");
        } // try-with-resources cierra el pool (AutoCloseable desde Java 19)
    }
}
