package main.completable;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.*;

public class Main {


    static void main() {
        CompletableFuture<String> obtenNombre =
        CompletableFuture
                .supplyAsync(() -> obtenerUsuario())
                .thenCompose(usuario -> CompletableFuture.supplyAsync(() -> cargarDireccion(usuario)))
                .thenApply(usuario -> obtenerNombre(usuario));

        Executor executor = Executors.newVirtualThreadPerTaskExecutor();
        String nombre = obtenNombre.join();
        System.out.println(nombre);
        CompletableFuture
                .supplyAsync(() -> 10,executor)
                .thenApply(n ->  {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return n * 2;})
                .thenApply(n -> n + 5)
                .thenApply(String::valueOf)
                .thenAccept(resultado ->
                        System.out.println(
                                "Resultado: " + resultado
                        )
                );

        BlockingQueue<Usuario> b = new ArrayBlockingQueue<>(10);
        



    }

    private static Usuario cargarDireccion(Usuario usuario) {
        return new Usuario(usuario.nombre(),"direccion","");
    }

    private static String obtenerNombre(Usuario usuario) {
        return usuario.nombre();
    }

    private static Usuario obtenerUsuario() {
        return new Usuario("admin");
    }
}
