package problemas;// PROBLEMA 5 - El Sistema de Descargas (Nivel avanzado)
// Servidor con maximo 5 descargas simultaneas (ThreadPool de 5), cola de espera
// con prioridad (PriorityBlockingQueue: premium primero), reintentos (max 3),
// barras de progreso en consola, clientes en hilos virtuales y CompletableFuture.

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Problema5_SistemaDescargas {

    enum TipoArchivo {
        DOCUMENTO(10, 1000),   // 10 MB   -> 1 s
        IMAGEN(50, 5000),      // 50 MB   -> 5 s
        VIDEO(500, 50000),     // 500 MB  -> 50 s
        JUEGO(2000, 200000);   // 2 GB    -> 200 s
        final int sizeMB;
        final int tiempoMs;
        TipoArchivo(int sizeMB, int tiempoMs) {
            this.sizeMB = sizeMB;
            this.tiempoMs = tiempoMs;
        }
        static TipoArchivo aleatorio(Random r) {
            double d = r.nextDouble();
            if (d < 0.50) return DOCUMENTO;
            if (d < 0.80) return IMAGEN;
            if (d < 0.95) return VIDEO;
            return JUEGO;
        }
        String nombreAleatorio(Random r) {
            return switch (this) {
                case DOCUMENTO -> "doc_" + r.nextInt(100) + ".pdf";
                case IMAGEN -> "imagen_" + r.nextInt(100) + ".jpg";
                case VIDEO -> "video_" + r.nextInt(100) + ".mp4";
                case JUEGO -> "juego_" + r.nextInt(100) + ".zip";
            };
        }
    }

    // --- Solicitud con prioridad (premium sale antes de la cola) ---
    static class Solicitud implements Comparable<Solicitud> {
        final String usuario;
        final String archivo;
        final TipoArchivo tipo;
        final boolean premium;
        final CompletableFuture<Resultado> futuro = new CompletableFuture<>();

        Solicitud(String usuario, String archivo, TipoArchivo tipo, boolean premium) {
            this.usuario = usuario;
            this.archivo = archivo;
            this.tipo = tipo;
            this.premium = premium;
        }

        public int compareTo(Solicitud otra) {
            return Boolean.compare(otra.premium, this.premium); // premium primero
        }
    }

    record Resultado(boolean exito, String detalle, int intentos) {}

    // --- Estado del servidor ---
    static final int MAX_CONEXIONES = 5;
    static final int MAX_INTENTOS = 3;
    static final ThreadPoolExecutor servidor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_CONEXIONES);
    static final PriorityBlockingQueue<Solicitud> cola = new PriorityBlockingQueue<>();
    static final ConcurrentHashMap<String, Integer> progreso = new ConcurrentHashMap<>(); // % por usuario

    static final AtomicInteger completadas = new AtomicInteger();
    static final AtomicInteger fallidas = new AtomicInteger();
    static final AtomicInteger reintentos = new AtomicInteger();
    static final AtomicLong datosMB = new AtomicLong();
    static final AtomicLong tiempoTotalMs = new AtomicLong();
    static final int USUARIOS = 50;

    static String ahora() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== GESTOR DE DESCARGAS ===");
        System.out.println("Servidor: " + MAX_CONEXIONES + "/" + MAX_CONEXIONES + " conexiones activas\n");

        // Monitor de progreso (barras) cada 2 s
        ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();
        monitor.scheduleAtFixedRate(Problema5_SistemaDescargas::pintarProgreso, 2, 2, TimeUnit.SECONDS);

        CountDownLatch fin = new CountDownLatch(USUARIOS);

        // --- 50 clientes en hilos virtuales ---
        for (int i = 1; i <= USUARIOS; i++) {
            final int id = i;
            final boolean premium = i % 5 == 0; // 1 de cada 5 es premium
            Thread.ofVirtual().name("usuario-" + id).start(() -> {
                try {
                    Thread.sleep(new Random().nextInt(3000)); // llegan escalonados
                    Random r = new Random();
                    TipoArchivo tipo = TipoArchivo.aleatorio(r);
                    String nombre = tipo.nombreAleatorio(r);

                    int intentos = 0;
                    Resultado res;
                    do {
                        intentos++;
                        res = solicitarDescarga("usuario-" + id, nombre, tipo, premium);
                        if (!res.exito() && intentos < MAX_INTENTOS) {
                            reintentos.incrementAndGet();
                            System.out.println("[" + ahora() + "] usuario-" + id
                                    + " reintentando (" + intentos + "/" + MAX_INTENTOS + "): " + nombre);
                        }
                    } while (!res.exito() && intentos < MAX_INTENTOS);

                    if (res.exito()) {
                        completadas.incrementAndGet();
                        datosMB.addAndGet(tipo.sizeMB);
                        tiempoTotalMs.addAndGet(tipo.tiempoMs);
                        System.out.println("[" + ahora() + "] usuario-" + id + " completo: " + nombre);
                    } else {
                        fallidas.incrementAndGet();
                        System.out.println("[" + ahora() + "] usuario-" + id + " FALLO tras "
                                + MAX_INTENTOS + " intentos: " + nombre);
                    }
                } finally {
                    fin.countDown();
                }
            });
        }

        fin.await();
        monitor.shutdownNow();

        System.out.println("\n--- ESTADISTICAS DEL SERVIDOR ---");
        System.out.println("Solicitudes: " + USUARIOS);
        System.out.println("Descargas exitosas: " + completadas.get()
                + " (" + (completadas.get() * 100.0 / USUARIOS) + "%)");
        System.out.println("Descargas fallidas: " + fallidas.get() + " (reintentos agotados)");
        System.out.println("Reintentos realizados: " + reintentos.get());
        System.out.println("Datos transferidos: " + (datosMB.get() / 1000.0) + " GB");
        System.out.println("Tiempo promedio por descarga: "
                + (completadas.get() == 0 ? 0 : tiempoTotalMs.get() / 1000.0 / completadas.get())
                + "s");
        servidor.shutdown();
        System.exit(0);
    }

    // Encola la solicitud y espera el resultado (el servidor la procesa cuando hay conexion libre)
    static Resultado solicitarDescarga(String usuario, String archivo,
                                       TipoArchivo tipo, boolean premium) {
        Solicitud sol = new Solicitud(usuario, archivo, tipo, premium);
        cola.offer(sol);
        despertarServidor();
        try {
            return sol.futuro.get(5, TimeUnit.MINUTES);
        } catch (Exception e) {
            return new Resultado(false, "timeout esperando servidor", 0);
        }
    }

    // Lanza un trabajador del pool por solicitud encolada (los hilos del pool
    // se reutilizan: maximo 5 descargas simultaneas reales)
    static synchronized void despertarServidor() {
        Solicitud sol = cola.poll();
        if (sol == null) return;
        servidor.submit(() -> procesar(sol));
    }

    static void procesar(Solicitud sol) {
        progreso.put(sol.usuario, 0);
        long t0 = System.currentTimeMillis();
        try {
            // Simulacion de la descarga: 10 ticks de progreso; 10% de fallo aleatorio
            int ticks = 10;
            for (int i = 1; i <= ticks; i++) {
                Thread.sleep(sol.tipo.tiempoMs / ticks);
                if (new Random().nextDouble() < 0.01) { // fallo de red ocasional
                    progreso.remove(sol.usuario);
                    sol.futuro.complete(new Resultado(false, "error de red", 0));
                    return;
                }
                progreso.put(sol.usuario, i * 100 / ticks);
            }
            progreso.remove(sol.usuario);
            sol.futuro.complete(new Resultado(true,
                    "OK en " + (System.currentTimeMillis() - t0) + " ms", 0));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            sol.futuro.complete(new Resultado(false, "interrumpido", 0));
        }
    }

    static void pintarProgreso() {
        StringBuilder sb = new StringBuilder("\n🔄 Descargas activas (" + progreso.size()
                + "/" + MAX_CONEXIONES + " conexiones):\n");
        sb.append("┌──────────────────────────────────────────┐\n");
        progreso.forEach((user, pct) -> sb.append(String.format("│ %-11s %s %3d%%%n",
                user, barra(pct), pct)));
        sb.append("└──────────────────────────────────────────┘\n");
        sb.append("⏳ Cola de espera: ").append(cola.size())
                .append(" | ❌ Fallos: ").append(fallidas.get())
                .append(" | ✅ Completadas: ").append(completadas.get());
        System.out.println(sb);
    }

    static String barra(int pct) {
        int celdas = 10;
        int llenas = pct / 10;
        return "█".repeat(llenas) + "░".repeat(celdas - llenas);
    }
}
