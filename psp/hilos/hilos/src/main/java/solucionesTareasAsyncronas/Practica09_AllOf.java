package solucionesTareasAsyncronas;

// Practica 9 - allOf(): esperar a que terminen N tareas

import java.util.concurrent.CompletableFuture;

public class Practica09_AllOf {

    static CompletableFuture<Void> descargar(String nombre, int segundos) {
        return CompletableFuture.runAsync(() -> {
            try {
                System.out.println(nombre + " empezando...");
                Thread.sleep(segundos * 1000);
                System.out.println(nombre + " terminada (" + segundos + " s)");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public static void main(String[] args) {
        long inicio = System.currentTimeMillis();

        CompletableFuture<Void> todas = CompletableFuture.allOf(
                descargar("Archivo 1", 2),
                descargar("Archivo 2", 1),
                descargar("Archivo 3", 3),
                descargar("Archivo 4", 2),
                descargar("Archivo 5", 1));

        todas.join(); // espera a que terminen TODAS

        System.out.println("Todas las descargas han terminado en "
                + (System.currentTimeMillis() - inicio) + " ms");
    }
}
