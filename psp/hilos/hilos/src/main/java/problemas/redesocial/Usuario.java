package problemas.redesocial;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Comportamiento de un usuario: bucle infinito de actividades aleatorias
 * con tiempos del enum TipoActividad. Cada usuario es un hilo virtual.
 * Tipos: 80% normales, 10% influencers (publican mas), 10% bots (velocidad x5).
 */
public class Usuario implements Runnable {

    public enum TipoUsuario { NORMAL, INFLUENCER, BOT }

    private static final List<String> CONTENIDOS = List.of(
            "Aprendiendo hilos virtuales! 🚀 #JavaConcurrency",
            "Gatos programando Java #ViralVideo",
            "Mi proyecto de 2º DAM ya compila ✅",
            "Alguien sabe como funciona un CyclicBarrier?",
            "El cafe de hoy sabe a deadlock resuelto ☕",
            "Demo en vivo de mi simulacion #JavaConcurrency",
            "Hilos, locks y semaforos... ¡que no cunda el panico!",
            "Thread.ofVirtual() me cambio la vida #JavaConcurrency");

    private final String nombre;
    private final TipoUsuario tipo;
    private final RedSocial red;
    private final Random rnd = new Random();
    private final Set<String> siguiendo = ConcurrentHashMap.newKeySet();

    public Usuario(String nombre, TipoUsuario tipo, RedSocial red) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.red = red;
    }

    public String getNombre() { return nombre; }
    public TipoUsuario getTipo() { return tipo; }

    @Override
    public void run() {
        red.getMetricas().usuarioActivo();
        try {
            while (!Thread.currentThread().isInterrupted()) {
                TipoActividad actividad = elegirActividad();
                dormir(actividad.tiempoMs * multiplicadorVelocidad());
                ejecutar(actividad);
            }
        } finally {
            red.getMetricas().usuarioInactivo();
        }
    }

    private int multiplicadorVelocidad() {
        return switch (tipo) {
            case INFLUENCER -> 1;
            case BOT -> 5;       // bots actuan rapidisimo -> dispara el anti-spam
            case NORMAL -> 1;
        };
    }

    private TipoActividad elegirActividad() {
        double d = rnd.nextDouble();
        return switch (tipo) {
            case INFLUENCER -> d < 0.70 ? TipoActividad.PUBLICAR_POST
                              : d < 0.85 ? TipoActividad.COMPARTIR_POST
                              : d < 0.95 ? TipoActividad.DAR_LIKE
                              : TipoActividad.SEGUIR_USUARIO;
            case BOT -> d < 0.60 ? TipoActividad.DAR_LIKE
                      : d < 0.80 ? TipoActividad.COMENTAR
                      : d < 0.90 ? TipoActividad.PUBLICAR_POST
                      : TipoActividad.SEGUIR_USUARIO;
            case NORMAL -> d < 0.25 ? TipoActividad.PUBLICAR_POST
                         : d < 0.55 ? TipoActividad.DAR_LIKE
                         : d < 0.75 ? TipoActividad.COMENTAR
                         : d < 0.90 ? TipoActividad.SEGUIR_USUARIO
                         : TipoActividad.COMPARTIR_POST;
        };
    }

    private void ejecutar(TipoActividad actividad) {
        // Anti-spam: si el rate limiter detecta exceso, sancionamos al usuario
        if (!red.getRateLimiter().registrarAccion(nombre)) {
            red.registrarEvento("🚨 SPAM detectado de @" + nombre + " -> sancionado");
            red.getNotificaciones().enviarNotificacion("moderador",
                    "@" + nombre + " ha sido sancionado por comportamiento bot");
            return;
        }
        if (red.getRateLimiter().estaSancionado(nombre)) {
            return; // los sancionados quedan silenciados
        }

        switch (actividad) {
            case PUBLICAR_POST -> publicar();
            case DAR_LIKE -> red.darLikeAleatorio(nombre);
            case COMENTAR -> red.comentarAleatorio(nombre,
                    "Comentario de @" + nombre + " 🔥");
            case SEGUIR_USUARIO -> seguir();
            case COMPARTIR_POST -> red.compartirAleatorio(nombre);
        }
    }

    private void publicar() {
        if (!red.getRateLimiter().intentarPublicar(nombre)) {
            red.registrarEvento("⏳ Rate limit activado para @" + nombre);
            return;
        }
        String contenido = CONTENIDOS.get(rnd.nextInt(CONTENIDOS.size()));
        red.publicarPost(nombre, contenido);
    }

    private void seguir() {
        String objetivo = red.usuarioAleatorio();
        if (objetivo != null && !objetivo.equals(nombre) && siguiendo.add(objetivo)) {
            red.seguir(nombre, objetivo);
        }
    }

    private void dormir(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
