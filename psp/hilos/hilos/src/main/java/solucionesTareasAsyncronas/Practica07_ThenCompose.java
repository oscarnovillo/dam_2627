package solucionesTareasAsyncronas;

// Practica 7 - thenCompose: encadenar dos futures donde el segundo DEPENDE del primero
// (no usamos get() entre medias: la composicion es asincrona de principio a fin)

import java.util.concurrent.CompletableFuture;

public class Practica07_ThenCompose {

    static CompletableFuture<String> obtenerUsuario() {
        return CompletableFuture.supplyAsync(() -> {
            dormir(1000);
            return "Oscar";
        });
    }

    static CompletableFuture<String> obtenerEmail(String usuario) {
        return CompletableFuture.supplyAsync(() -> {
            dormir(1000);
            return usuario + "@example.com";
        });
    }

    static void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        CompletableFuture<String> futuroEmail =
                obtenerUsuario()
                        .thenCompose(usuario -> {
                            System.out.println("Usuario obtenido: " + usuario);
                            return obtenerEmail(usuario); // aplana Future<Future<String>>
                        });

        System.out.println("Email: " + futuroEmail.join());
    }
}
