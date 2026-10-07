package problemas.redesocial;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Notificaciones asincronas: los productores solo encolan (put no bloquea
 * practicamente nunca) y un consumidor dedicado las procesa en segundo plano.
 */
public class SistemaNotificaciones {

    private final BlockingQueue<Notificacion> colaNotificaciones = new LinkedBlockingQueue<>();
    private final Map<String, List<Notificacion>> bandejaPorUsuario = new ConcurrentHashMap<>();
    private final Thread procesador;

    public SistemaNotificaciones() {
        this.procesador = Thread.ofPlatform().name("notificaciones").start(() -> {
            try {
                while (true) {
                    Notificacion n = colaNotificaciones.take();
                    bandejaPorUsuario
                            .computeIfAbsent(n.destinatario(), k -> new CopyOnWriteArrayList<>())
                            .add(n);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void enviarNotificacion(String usuario, String mensaje) {
        colaNotificaciones.offer(Notificacion.de(usuario, mensaje));
    }

    public List<Notificacion> obtenerNotificaciones(String usuario) {
        return bandejaPorUsuario.getOrDefault(usuario, List.of());
    }

    public int pendientes() {
        return colaNotificaciones.size();
    }

    public void cerrar() {
        procesador.interrupt();
    }
}
