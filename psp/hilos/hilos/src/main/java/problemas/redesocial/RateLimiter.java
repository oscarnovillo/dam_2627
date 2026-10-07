package problemas.redesocial;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting: maximo 10 publicaciones por minuto y usuario.
 * Contador por usuario que un reloj periodico resetea cada minuto.
 * Anti-spam: mas de 10 acciones por SEGUNdo -> usuario sancionado.
 */
public class RateLimiter {

    private static final int MAX_POSTS_MINUTO = 10;
    private static final int MAX_ACCIONES_SEGUNDO = 10;

    private final Map<String, AtomicInteger> postsPorMinuto = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> accionesPorSegundo = new ConcurrentHashMap<>();
    private final Map<String, Boolean> sancionados = new ConcurrentHashMap<>();

    /** Llamar ANTES de publicar. true = permitido, false = supera el limite. */
    public boolean intentarPublicar(String usuario) {
        boolean ok = postsPorMinuto.computeIfAbsent(usuario, k -> new AtomicInteger())
                                   .incrementAndGet() <= MAX_POSTS_MINUTO;
        if (!ok) {
            MetricasHolder.METRICAS.rateLimit();
        }
        return ok;
    }

    /** Registra cualquier accion para la deteccion de spam. true = normal, false = spam. */
    public boolean registrarAccion(String usuario) {
        int acciones = accionesPorSegundo.computeIfAbsent(usuario, k -> new AtomicInteger())
                                         .incrementAndGet();
        if (acciones > MAX_ACCIONES_SEGUNDO) {
            sancionados.put(usuario, Boolean.TRUE);
            MetricasHolder.METRICAS.spam();
            return false;
        }
        return true;
    }

    public boolean estaSancionado(String usuario) {
        return sancionados.containsKey(usuario);
    }

    public void resetMinuto() {
        postsPorMinuto.values().forEach(c -> c.set(0));
    }

    public void resetSegundo() {
        accionesPorSegundo.values().forEach(c -> c.set(0));
    }

    /** Acceso global a las metricas compartidas (evita pasar referencias por todas partes). */
    static class MetricasHolder {
        static final MetricasRedSocial METRICAS = new MetricasRedSocial();
    }
}
