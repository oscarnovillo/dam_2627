package main;

import java.util.concurrent.CompletableFuture;

public class prueba {
    static void main() {
        CompletableFuture<Void> tarea =
                CompletableFuture.supplyAsync(() -> 10)
                        .thenApply(n -> n * 2)
                        .thenApply(
                        n -> n + 5 )
                        .thenApply(String::valueOf)

                        .thenAccept(resultado ->
                                System.out.println("Resultado: " + resultado)
                        );

        tarea.join();
    }
}
