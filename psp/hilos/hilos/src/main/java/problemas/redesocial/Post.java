package problemas.redesocial;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class Post {

    public record Comentario(String autor, String texto, LocalDateTime fecha) {}

    private final String id = UUID.randomUUID().toString().substring(0, 8);
    private final String autor;
    private final String contenido;
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final AtomicLong likes = new AtomicLong();
    private final ConcurrentLinkedQueue<Comentario> comentarios = new ConcurrentLinkedQueue<>();
    private final AtomicLong shares = new AtomicLong();
    private final AtomicLong likesPorMinuto = new AtomicLong(); // para detectar viralizacion
    private volatile boolean marcadoViral = false;

    public Post(String autor, String contenido) {
        this.autor = autor;
        this.contenido = contenido;
    }

    public String getId() { return id; }
    public String getAutor() { return autor; }
    public String getContenido() { return contenido; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public long getLikes() { return likes.get(); }
    public long getShares() { return shares.get(); }
    public ConcurrentLinkedQueue<Comentario> getComentarios() { return comentarios; }

    public long darLike() {
        likesPorMinuto.incrementAndGet();
        return likes.incrementAndGet();
    }

    public void addComentario(String autor, String texto) {
        comentarios.add(new Comentario(autor, texto, LocalDateTime.now()));
    }

    public long compartir() {
        return shares.incrementAndGet();
    }

    /** Marca la ventana de 1 minuto: llamar desde el actualizador periodico. */
    public void resetVentanaLikes() {
        likesPorMinuto.set(0);
    }

    public long getLikesUltimoMinuto() {
        return likesPorMinuto.get();
    }

    public boolean marcarViral() {
        return !marcadoViral && (marcadoViral = true);
    }

    public boolean esViral() { return marcadoViral; }

    @Override
    public String toString() {
        return "@" + autor + ": " + contenido + " (❤" + likes.get()
                + " 💬" + comentarios.size() + " 🔄" + shares.get() + ")";
    }
}
