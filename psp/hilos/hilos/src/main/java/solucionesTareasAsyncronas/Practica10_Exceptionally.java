package solucionesTareasAsyncronas;

// Practica 10 - exceptionally(): valor alternativo cuando falla la tarea

import java.util.concurrent.CompletableFuture;

public class Practica10_Exceptionally {

    public static void main(String[] args) {
        String resultado = CompletableFuture
                .<String>supplyAsync(() -> {
                    sleep(1000);
                    throw new RuntimeException("Error al consultar el servidor");
                })
                .exceptionally(ex -> {
                    System.out.println("Excepcion capturada: " + ex.getMessage());
                    return "DATOS POR DEFECTO";
                })
                .join();

        System.out.println("Resultado: " + resultado);
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
