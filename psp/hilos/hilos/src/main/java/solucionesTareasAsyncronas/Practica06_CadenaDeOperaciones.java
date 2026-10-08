package solucionesTareasAsyncronas;

// Practica 6 - Cadena de operaciones
// 10 -> *2 -> +5 -> toString -> mostrar  =>  "Resultado: 25"

import java.util.concurrent.CompletableFuture;

public class Practica06_CadenaDeOperaciones {

    public static void main(String[] args) {
        CompletableFuture
                .supplyAsync(() -> 10)
                .thenApply(n -> n * 2)
                .thenApply(n -> n + 5)
                .thenApply(String::valueOf)
                .thenAccept(resultado ->
                        System.out.println("Resultado: " + resultado))
                .join();
    }
}
