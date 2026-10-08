package solucionesTareasAsyncronas;

// Practica 11 - handle(): gestionar exito Y error en un solo callback
// BiFunction(resultado, excepcion): si ex != null, hubo error.

import java.util.concurrent.CompletableFuture;

public class Practica11_Handle {

    static CompletableFuture<Integer> dividir(int a, int b) {
        return CompletableFuture
                .supplyAsync(() -> a / b) // ArithmeticException si b == 0
                .handle((resultado, ex) -> ex == null ? resultado : 0);
    }

    public static void main(String[] args) {
        System.out.println("10 / 2 = " + dividir(10, 2).join()); // 5
        System.out.println("10 / 0 = " + dividir(10, 0).join()); // 0 (división entre cero controlada)
    }
}
