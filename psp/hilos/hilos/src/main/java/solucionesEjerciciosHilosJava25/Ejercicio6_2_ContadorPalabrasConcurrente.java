package solucionesEjerciciosHilosJava25;
// Ejercicio 6.2 - Contador de palabras concurrente:
// ConcurrentHashMap + merge vs HashMap + synchronized
//
// ConcurrentHashMap permite acceso concurrente por segmentos internos:
// los hilos rara vez se bloquean entre si y no hace falta sincronizar
// las lecturas. HashMap + synchronized(candado global) serializa TODO el
// acceso al mapa: correcto, pero con mucha mas contencion.

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Ejercicio6_2_ContadorPalabrasConcurrente {

    private static final String FRASE = "el rapido zorro marron salta sobre el perro perezoso "
            + "el zorro es rapido y el perro es perezoso mientras el zorro salta";

    public static void main(String[] args) throws Exception {
        String texto = String.join(" ", Collections.nCopies(500, FRASE));
        int trozos = 4;
        int tamanyo = texto.length() / trozos;

        // ---- Version 1: ConcurrentHashMap ----
        long t0 = System.nanoTime();
        ConcurrentHashMap<String, Integer> concurrente = new ConcurrentHashMap<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(trozos)) {
            List<Future<?>> futuros = new ArrayList<>();
            for (int i = 0; i < trozos; i++) {
                final String fragmento = texto.substring(i * tamanyo,
                        i == trozos - 1 ? texto.length() : (i + 1) * tamanyo);
                futuros.add(pool.submit(() -> {
                    for (String palabra : fragmento.split("\\s+")) {
                        if (!palabra.isEmpty()) {
                            concurrente.merge(palabra, 1, Integer::sum); // atomico
                        }
                    }
                }));
            }
            for (Future<?> f : futuros) f.get();
        }
        long tiempoConcurrente = System.nanoTime() - t0;

        // ---- Version 2: HashMap + synchronized ----
        t0 = System.nanoTime();
        Map<String, Integer> sincronizado = new HashMap<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(trozos)) {
            List<Future<?>> futuros = new ArrayList<>();
            for (int i = 0; i < trozos; i++) {
                final String fragmento = texto.substring(i * tamanyo,
                        i == trozos - 1 ? texto.length() : (i + 1) * tamanyo);
                futuros.add(pool.submit(() -> {
                    for (String palabra : fragmento.split("\\s+")) {
                        if (!palabra.isEmpty()) {
                            synchronized (sincronizado) {
                                sincronizado.merge(palabra.toLowerCase(), 1, Integer::sum);
                            }
                        }
                    }
                }));
            }
            for (Future<?> f : futuros) f.get();
        }
        long tiempoSync = System.nanoTime() - t0;

        System.out.println("Resultado ConcurrentHashMap: " + concurrente);
        System.out.println("Resultado HashMap+sync:      " + sincronizado);
        System.out.println("Ambos mapas coinciden: " + concurrente.equals(sincronizado));
        System.out.printf("Tiempo ConcurrentHashMap: %d ms | HashMap+sync: %d ms%n",
                tiempoConcurrente / 1_000_000, tiempoSync / 1_000_000);
        System.out.println("(Con mas hilos/datos, la diferencia crece: el candado global serializa todo.)");
    }
}
