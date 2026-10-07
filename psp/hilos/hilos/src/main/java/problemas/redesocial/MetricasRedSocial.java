package problemas.redesocial;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** Metricas globales con contadores atomicos y tasas por segundo/minuto. */
public class MetricasRedSocial {

    private final AtomicLong postsCreados = new AtomicLong();
    private final AtomicLong likesTotales = new AtomicLong();
    private final AtomicLong comentariosTotales = new AtomicLong();
    private final AtomicLong sharesTotales = new AtomicLong();
    private final AtomicLong seguidosTotales = new AtomicLong();
    private final AtomicInteger usuariosActivos = new AtomicInteger();
    private final AtomicLong rateLimitActivados = new AtomicLong();
    private final AtomicLong spamDetectados = new AtomicLong();

    // ventanas para calcular tasas
    private final AtomicLong likesVentana = new AtomicLong();
    private volatile double likesPorSegundo;
    private final AtomicLong postsVentana = new AtomicLong();
    private volatile double postsPorMinuto;
    private final AtomicLong comentariosVentana = new AtomicLong();
    private volatile double comentariosPorMinuto;
    private final AtomicLong sharesVentana = new AtomicLong();
    private volatile double sharesPorMinuto;

    public void iniciarTasas(ScheduledExecutorService scheduler) {
        scheduler.scheduleAtFixedRate(() -> {
            likesPorSegundo = likesVentana.getAndSet(0) / 5.0;       // snapshot cada 5 s
            postsPorMinuto = postsVentana.getAndSet(0) * (60.0 / 5);
            comentariosPorMinuto = comentariosVentana.getAndSet(0) * (60.0 / 5);
            sharesPorMinuto = sharesVentana.getAndSet(0) * (60.0 / 5);
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void postCreado()    { postsCreados.incrementAndGet(); postsVentana.incrementAndGet(); }
    public void like()          { likesTotales.incrementAndGet(); likesVentana.incrementAndGet(); }
    public void comentario()    { comentariosTotales.incrementAndGet(); comentariosVentana.incrementAndGet(); }
    public void share()         { sharesTotales.incrementAndGet(); sharesVentana.incrementAndGet(); }
    public void seguir()        { seguidosTotales.incrementAndGet(); }
    public void usuarioActivo() { usuariosActivos.incrementAndGet(); }
    public void usuarioInactivo() { usuariosActivos.decrementAndGet(); }
    public void rateLimit()     { rateLimitActivados.incrementAndGet(); }
    public void spam()          { spamDetectados.incrementAndGet(); }

    public long getPostsCreados()        { return postsCreados.get(); }
    public long getLikesTotales()        { return likesTotales.get(); }
    public long getComentariosTotales()  { return comentariosTotales.get(); }
    public long getSharesTotales()       { return sharesTotales.get(); }
    public int getUsuariosActivos()      { return usuariosActivos.get(); }
    public long getRateLimitActivados()  { return rateLimitActivados.get(); }
    public long getSpamDetectados()      { return spamDetectados.get(); }
    public double getLikesPorSegundo()   { return likesPorSegundo; }
    public double getPostsPorMinuto()    { return postsPorMinuto; }
    public double getComentariosPorMinuto() { return comentariosPorMinuto; }
    public double getSharesPorMinuto()   { return sharesPorMinuto; }
}
