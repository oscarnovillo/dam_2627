package problemas.redesocial;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base de datos simulada: latencia de 100 ms por operacion y pool de 20
 * conexiones (Semaphore). Las operaciones devuelven CompletableFuture:
 * el hilo llamante NO se bloquea mientras "consulta" la BD.
 */
public class BaseDatosSimulada {

    private static final int MAX_CONEXIONES = 20;
    private static final long LATENCIA_MS = 100;

    private final Semaphore poolConexiones = new Semaphore(MAX_CONEXIONES);
    private final Map<String, Post> posts = new ConcurrentHashMap<>();
    private final AtomicInteger conexionesEnUso = new AtomicInteger();

    public CompletableFuture<Post> guardarPost(Post post) {
        return ejecutar(() -> {
            posts.put(post.getId(), post);
            return post;
        });
    }

    public CompletableFuture<Post> obtenerPost(String id) {
        return ejecutar(() -> posts.get(id));
    }

    public CompletableFuture<List<Post>> obtenerFeed(String usuario) {
        return ejecutar(() -> posts.values().stream()
                .filter(p -> !p.getAutor().equals(usuario))
                .limit(20)
                .toList());
    }

    public int conexionesEnUso() {
        return conexionesEnUso.get();
    }

    public int totalPosts() {
        return posts.size();
    }

    private <T> CompletableFuture<T> ejecutar(ConsultaBD<T> consulta) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                poolConexiones.acquire();
                conexionesEnUso.incrementAndGet();
                Thread.sleep(LATENCIA_MS); // latencia de red
                return consulta.ejecutar();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("BD interrumpida", e);
            } finally {
                conexionesEnUso.decrementAndGet();
                poolConexiones.release();
            }
        });
    }

    @FunctionalInterface
    private interface ConsultaBD<T> { T ejecutar(); }
}
