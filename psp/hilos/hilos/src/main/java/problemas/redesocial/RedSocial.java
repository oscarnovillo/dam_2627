package problemas.redesocial;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PROBLEMA INTEGRADOR FINAL - La Red Social.
 *
 * Simulacion completa que integra TODAS las tecnicas del curso:
 * - 1000 usuarios como hilos virtuales (JEP 444)
 * - Posts con AtomicLong (likes/shares) y ConcurrentLinkedQueue (comentarios)
 * - Feed en ConcurrentHashMap + Cache LRU de posts populares
 * - Sistema de notificaciones asincrono con BlockingQueue
 * - Base de datos simulada: pool de 20 conexiones (Semaphore) + CompletableFuture
 * - Metricas en tiempo real con AtomicInteger/AtomicLong + ScheduledExecutorService
 * - Rate limiting (10 posts/min) y deteccion de spam (>10 acciones/s)
 * - Viralizacion: >100 likes/min -> trending + notificaciones + cache
 * - Dashboard en vivo y eventos recientes
 *
 * Ejecutar:  java redesocial.RedSocial [duracionSegundos]   (por defecto 60 s)
 */
public class RedSocial {

    private static final int TOTAL_USUARIOS = 1000;
    private static final int DURACION_DEFECTO_S = 60;

    // --- Componentes del sistema ---
    private final MetricasRedSocial metricas = new MetricasRedSocial();
    private final BaseDatosSimulada bd = new BaseDatosSimulada();
    private final SistemaNotificaciones notificaciones = new SistemaNotificaciones();
    private final RateLimiter rateLimiter = new RateLimiter();
    private final CacheLRU cache = new CacheLRU();

    // --- Estado compartido ---
    private final ConcurrentHashMap<String, Post> posts = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<Post> postsRecientes = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<String>> followers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> mencionesHashtag = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<String> eventos = new ConcurrentLinkedQueue<>();
    private final CopyOnWriteArrayList<Usuario> usuarios = new CopyOnWriteArrayList<>();
    private final AtomicLong peticionesCache = new AtomicLong();
    private final AtomicLong aciertosCache = new AtomicLong();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private final Instant inicio = Instant.now();
    private volatile boolean enMarcha = true;

    public static void main(String[] args) throws InterruptedException {
        int duracion = args.length > 0 ? Integer.parseInt(args[0]) : DURACION_DEFECTO_S;
        new RedSocial().arrancar(duracion);
    }

    void arrancar(int duracionSegundos) throws InterruptedException {
        System.out.println("=== RED SOCIAL - SIMULACION CONCURRENTE ===");
        System.out.println("Arrancando " + TOTAL_USUARIOS + " usuarios durante "
                + duracionSegundos + " s...\n");

        metricas.iniciarTasas(scheduler);

        // --- Crear usuarios (80% normales, 10% influencers, 10% bots) ---
        Random rnd = new Random();
        for (int i = 0; i < TOTAL_USUARIOS; i++) {
            double d = rnd.nextDouble();
            Usuario.TipoUsuario tipo = d < 0.80 ? Usuario.TipoUsuario.NORMAL
                    : d < 0.90 ? Usuario.TipoUsuario.INFLUENCER
                    : Usuario.TipoUsuario.BOT;
            Usuario u = new Usuario("Usuario_" + i, tipo, this);
            usuarios.add(u);
            // Los hilos virtuales: 1000 usuarios con coste de pila despreciable
            Thread.ofVirtual().name(u.getNombre()).start(u);
        }

        // --- Tareas periodicas ---
        scheduler.scheduleAtFixedRate(this::pintarDashboard, 5, 5, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(rateLimiter::resetMinuto, 1, 1, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(rateLimiter::resetSegundo, 1, 1, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::resetVentanasViral, 1, 1, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(this::detectarViralizacion, 10, 10, TimeUnit.SECONDS);

        // --- Dejar correr la simulacion ---
        Thread.sleep(duracionSegundos * 1000L);
        enMarcha = false;

        // --- Apagado ordenado ---
        scheduler.shutdownNow();
        notificaciones.cerrar();
        pintarResumenFinal();
        System.exit(0);
    }

    // ================== API usada por los usuarios ==================

    public void publicarPost(String autor, String contenido) {
        Post post = new Post(autor, contenido);
        posts.put(post.getId(), post);
        postsRecientes.add(post);
        if (postsRecientes.size() > 200) {
            postsRecientes.remove(0); // ventana de posts "recientes"
        }
        metricas.postCreado();

        // Guardar en BD de forma asincrona (no bloquea al usuario)
        bd.guardarPost(post);

        // Contar hashtags para trending
        for (String palabra : contenido.split("\\s+")) {
            if (palabra.startsWith("#")) {
                mencionesHashtag.computeIfAbsent(palabra, k -> new AtomicLong())
                                .incrementAndGet();
            }
        }

        // Notificar a los followers del autor (asincrono)
        CopyOnWriteArrayList<String> seguidores = followers.get(autor);
        if (seguidores != null) {
            seguidores.forEach(f -> notificaciones.enviarNotificacion(f,
                    "@" + autor + " publico: " + recortar(contenido, 40)));
        }

        registrarEvento("📝 @" + autor + " publico: " + recortar(contenido, 50));
    }

    public void darLikeAleatorio(String quien) {
        Post post = postAleatorio();
        if (post == null) return;

        // Cache: los posts populares se sirven de memoria
        peticionesCache.incrementAndGet();
        if (cache.get(post.getId()) != null) {
            aciertosCache.incrementAndGet();
        }

        long likes = post.darLike();
        metricas.like();
        notificaciones.enviarNotificacion(post.getAutor(),
                "@" + quien + " le dio like a tu post");

        // Umbral de popularidad: entrar en cache
        if (likes == 100) {
            cache.put(post);
        }
    }

    public void comentarAleatorio(String quien, String texto) {
        Post post = postAleatorio();
        if (post == null) return;
        post.addComentario(quien, texto);
        metricas.comentario();
        notificaciones.enviarNotificacion(post.getAutor(),
                "@" + quien + " comento tu post");
    }

    public void compartirAleatorio(String quien) {
        Post post = postAleatorio();
        if (post == null) return;
        post.compartir();
        metricas.share();
        mencionesHashtag.computeIfAbsent("#ViralVideo", k -> new AtomicLong())
                        .incrementAndGet();
    }

    public void seguir(String quien, String aQuien) {
        followers.computeIfAbsent(aQuien, k -> new CopyOnWriteArrayList<>()).add(quien);
        metricas.seguir();
        notificaciones.enviarNotificacion(aQuien, "@" + quien + " te empezo a seguir");
    }

    // ================== Viralizacion y trending ==================

    private void detectarViralizacion() {
        if (!enMarcha) return;
        for (Post post : postsRecientes) {
            if (!post.esViral() && post.getLikesUltimoMinuto() >= 100) {
                if (post.marcarViral()) {
                    manejarViralizado(post);
                }
            }
        }
    }

    /** Caso de uso complejo: un post se viraliza (>100 likes en 1 minuto). */
    private void manejarViralizado(Post post) {
        cache.put(post);                                    // 4. cache especial
        mencionesHashtag.computeIfAbsent("#ViralVideo", k -> new AtomicLong())
                        .addAndGet(100);                    // 2. trending topics
        registrarEvento("🔥 Post viral (" + post.getLikes() + " likes): "
                + recortar(post.getContenido(), 40));

        CopyOnWriteArrayList<String> seguidores = followers.get(post.getAutor());
        if (seguidores != null) {
            seguidores.forEach(f -> notificaciones.enviarNotificacion(f, // 1. notificar followers
                    "🔥 Post viral de @" + post.getAutor() + ": "
                    + recortar(post.getContenido(), 30)));
        }
        // 3. aumentar prioridad: lo anadimos al principio de recientes
        postsRecientes.remove(post);
        postsRecientes.add(0, post);
    }

    private void resetVentanasViral() {
        postsRecientes.forEach(Post::resetVentanaLikes);
    }

    // ================== Utilidades ==================

    private Post postAleatorio() {
        if (postsRecientes.isEmpty()) return null;
        return postsRecientes.get(new Random().nextInt(postsRecientes.size()));
    }

    public String usuarioAleatorio() {
        if (usuarios.isEmpty()) return null;
        return usuarios.get(new Random().nextInt(usuarios.size())).getNombre();
    }

    public void registrarEvento(String evento) {
        eventos.offer(evento);
        while (eventos.size() > 6) {
            eventos.poll(); // mantener solo los 6 ultimos
        }
    }

    static String recortar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max) + "...";
    }

    // ================== Dashboard y resumen ==================

    private void pintarDashboard() {
        if (!enMarcha) return;
        Duration uptime = Duration.between(inicio, Instant.now());
        long hits = aciertosCache.get();
        long pet = peticionesCache.get();

        System.out.println("=== RED SOCIAL - DASHBOARD EN VIVO ===");
        System.out.printf("🕐 Uptime: %dh %dm %ds%n",
                uptime.toHours(), uptime.toMinutesPart(), uptime.toSecondsPart());
        System.out.println("┌─────────────────────────────────────┐");
        System.out.printf("│ 👥 Usuarios activos:   %6d/1000   │%n", metricas.getUsuariosActivos());
        System.out.printf("│ 📝 Posts/minuto:        %6.0f      │%n", metricas.getPostsPorMinuto());
        System.out.printf("│ ❤️  Likes/segundo:       %6.1f      │%n", metricas.getLikesPorSegundo());
        System.out.printf("│ 💬 Comentarios/minuto:  %6.0f      │%n", metricas.getComentariosPorMinuto());
        System.out.printf("│ 🔄 Shares/minuto:       %6.0f      │%n", metricas.getSharesPorMinuto());
        System.out.println("└─────────────────────────────────────┘");
        System.out.println("💾 SISTEMA:");
        System.out.println("┌─────────────────────────────────────┐");
        System.out.printf("│ 🗄️  Pool BD:           %6d/20    │%n", bd.conexionesEnUso());
        System.out.printf("│ 🚀 Cache hits:         %6.1f%%     │%n", cache.tasaAcierto(pet, hits));
        System.out.printf("│ 📱 Notif. en cola:     %6d      │%n", notificaciones.pendientes());
        System.out.printf("│ ⚡ Hilos virtuales:    %6d      │%n", TOTAL_USUARIOS);
        System.out.printf("│ 🚫 Rate limits:        %6d      │%n", metricas.getRateLimitActivados());
        System.out.printf("│ 🚨 Spams detectados:   %6d      │%n", metricas.getSpamDetectados());
        System.out.println("└─────────────────────────────────────┘");
        System.out.println("🔥 TRENDING:");
        mencionesHashtag.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .limit(3)
                .forEach(e -> System.out.printf("   %s (%d menciones)%n",
                        e.getKey(), e.getValue().get()));
        System.out.println("📈 ULTIMOS EVENTOS:");
        eventos.forEach(e -> System.out.println("   " + e));
        System.out.println();
    }

    private void pintarResumenFinal() {
        Duration uptime = Duration.between(inicio, Instant.now());
        long bots = usuarios.stream().filter(u -> u.getTipo() == Usuario.TipoUsuario.BOT).count();
        long influencers = usuarios.stream()
                .filter(u -> u.getTipo() == Usuario.TipoUsuario.INFLUENCER).count();

        System.out.println("\n===== RESUMEN FINAL DE LA SIMULACION =====");
        System.out.printf("Duracion: %dm %ds%n", uptime.toMinutesPart(), uptime.toSecondsPart());
        System.out.println("Posts creados:      " + metricas.getPostsCreados());
        System.out.println("Likes totales:      " + metricas.getLikesTotales());
        System.out.println("Comentarios:        " + metricas.getComentariosTotales());
        System.out.println("Shares:             " + metricas.getSharesTotales());
        System.out.println("Usuarios:           " + usuarios.size()
                + " (normales: " + (TOTAL_USUARIOS - bots - influencers)
                + ", influencers: " + influencers + ", bots: " + bots + ")");
        System.out.println("Posts en BD:        " + bd.totalPosts());
        System.out.println("Posts en cache LRU: " + cache.size() + "/50");
        System.out.println("Rate limits:        " + metricas.getRateLimitActivados());
        System.out.println("Spams sancionados:  " + metricas.getSpamDetectados());
        System.out.println("Notificaciones enviadas (en bandejas): "
                + usuarios.stream().mapToInt(u ->
                        notificaciones.obtenerNotificaciones(u.getNombre()).size()).sum());
        System.out.println("===========================================");
        System.out.println("Con hilos de plataforma habria sido inviable:");
        System.out.println("1000 hilos x ~1 MB de pila = ~1 GB solo en pilas.");
        System.out.println("Con hilos virtuales: unos pocos KB de monton por hilo.");
    }

    // ================== Getters para los componentes ==================

    public MetricasRedSocial getMetricas() { return metricas; }
    public SistemaNotificaciones getNotificaciones() { return notificaciones; }
    public RateLimiter getRateLimiter() { return rateLimiter; }
}
