package solucionesTareasAsyncronas;

// Practica 5 - CompletableFuture basico: supplyAsync + thenApply + thenAccept

import java.util.concurrent.CompletableFuture;

public class Practica05_CompletableFutureBasico {

    public static void main(String[] args) {
        CompletableFuture
                .supplyAsync(() -> 10 + 20)      // calcula 30
                .thenApply(n -> n * 2)           // transforma: 60
                .thenAccept(resultado ->
                        System.out.println("Resultado: " + resultado))
                .join();                         // esperamos a que termine la cadena
    }
}
