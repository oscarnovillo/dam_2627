package problemas;
// PROBLEMA 3 - El Restaurante Concurrente (Nivel intermedio-avanzado)
// Patron Productor-Consumidor con 5 camareros (productores), 3 cocineros
// (consumidores) y 100 clientes (hilos virtuales).
// Mesa de pedidos = ArrayBlockingQueue (capacidad 10) -> put/take bloqueantes.

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Problema3_RestauranteConcurrente {

    public static final int NUM_MESAS = 10;

    enum TipoPlato {
        ENSALADA(2000), PASTA(3000), PIZZA(4000), CARNE(5000);
        final int tiempoMs;
        TipoPlato(int tiempoMs) { this.tiempoMs = tiempoMs; }
        static TipoPlato aleatorio(Random r) {
            return values()[r.nextInt(values().length)];
        }
    }

    record Pedido(int cliente, TipoPlato plato, long horaCreacionMs, CountDownLatch pedidoCompletado) {}

    // --- Constantes del enunciado ---
    static final int COCINEROS = 3;
    static final int CAMAREROS = 5;
    static final int CLIENTES = 100;
    static final int CAPACIDAD_MESA = 10;
    static final long LLEGADA_CLIENTE_MS = 500;
    static final long TIEMPO_CAMARERO_MS = 1000;

    // --- Recursos compartidos ---
    static final BlockingQueue<Pedido> cocina = new ArrayBlockingQueue<>(CAPACIDAD_MESA);
    static final CountDownLatch clientesAtendidos = new CountDownLatch(CLIENTES);

    // --- Estadisticas ---
    static final AtomicInteger platosServidos = new AtomicInteger();
    static final AtomicLong esperaTotalMs = new AtomicLong();
    static final AtomicInteger mesaLlenaCount = new AtomicInteger();
    static final AtomicLong cocinerosEsperandoMs = new AtomicLong();

    static String ahora() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== RESTAURANTE CONCURRENTE ===");
        System.out.println("Iniciando servicio con " + COCINEROS + " cocineros y "
                + CAMAREROS + " camareros...\n");

        AtomicBoolean finServicio = new AtomicBoolean(false);
        List<Thread> clientes = new ArrayList<>();
        List<Thread> cocineros = new ArrayList<>();
        List<Thread> camareros = new ArrayList<>();
        final Semaphore mesas =  new Semaphore(NUM_MESAS);



        // --- Cocineros (consumidores) ---
        for (int i = 1; i <= COCINEROS; i++) {
            final int id = i;
            cocineros.add(Thread.ofPlatform().name("cocinero-" + id).start(() -> {
                try {
                    while (!finServicio.get()) {
                        long t0 = System.currentTimeMillis();
                        Pedido p = cocina.take(); // espera si no hay pedidos
                        cocinerosEsperandoMs.addAndGet(System.currentTimeMillis() - t0);

                        Thread.sleep(p.plato().tiempoMs); // cocina el plato
                        esperaTotalMs.addAndGet(System.currentTimeMillis() - p.horaCreacionMs());
                        platosServidos.incrementAndGet();
                        p.pedidoCompletado.countDown();
                        clientesAtendidos.countDown();

                        System.out.println("[" + ahora() + "] Cocinero-" + id + " termina "
                                + p.plato() + " para Cliente-" + p.cliente());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // cierre del restaurante
                }
            }));
        }

        // --- Camareros (productores) ---
        for (int i = 1; i <= CAMAREROS; i++) {
            final int id = i;
            camareros.add(Thread.ofPlatform().name("camarero-" + id).start(() -> {
                try {
                    while (!finServicio.get()) {
                        Pedido p = colaClientes.take(); // espera a que llegue un cliente
                        Thread.sleep(TIEMPO_CAMARERO_MS); // toma el pedido
                        System.out.println("[" + ahora() + "] Camarero-" + id
                                + " toma pedido " + p.plato());
                        if (cocina.remainingCapacity() == 0) {
                            mesaLlenaCount.incrementAndGet();
                        }
                        cocina.put(p); // espera si la mesa esta llena
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }


        // --- Clientes (hilos virtuales, llegan cada 500 ms) ---
        for (int i = 1; i <= CLIENTES; i++) {

            final int id = i;

            clientes.add(Thread.ofPlatform().name("puerta").start(() -> {

                boolean meQuedo = false;
                try {
                    meQuedo = mesas.tryAcquire(10, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (meQuedo) {
                    TipoPlato plato = TipoPlato.aleatorio(new Random());
                    System.out.println("[" + ahora() + "] Cliente-" + String.format("%03d", id)
                            + " pide " + plato);
                    try {
                        CountDownLatch pedidoCompletado = new CountDownLatch(1);
                        colaClientes.put(new Pedido(id, plato, System.currentTimeMillis(), pedidoCompletado));

                        pedidoCompletado.await();
                        mesas.release();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                else {
                    System.out.println("[" + ahora() + "] MEPIRO  Cliente-" + String.format("%03d", id));
                }

            }));
        }

        clientesAtendidos.await(); // el restaurante no cierra hasta atender a los 100
        clientes.forEach((hilo) -> {
            try {
                hilo.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        finServicio.set(true);

        cocineros.forEach((hilo) -> {
            try {
                hilo.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        camareros.forEach((hilo) -> {
            try {
                hilo.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });


        System.out.println("\n--- ESTADISTICAS FINALES ---");
        System.out.println("Clientes atendidos: " + CLIENTES + "/" + CLIENTES + " ✅");
        System.out.println("Platos servidos: " + platosServidos.get());
        System.out.println("Tiempo promedio de espera: "
                + (esperaTotalMs.get() / 1000.0 / CLIENTES) + "s");
        System.out.println("Mesa llena (veces): " + mesaLlenaCount.get());
        System.out.println("Cocineros esperando (tiempo acumulado): "
                + (cocinerosEsperandoMs.get() / 1000) + "s total");
        System.out.println("Eficiencia: el sistema soporta " + CAMAREROS + " pedidos/s en entrada"
                + " y " + COCINEROS + " platos en paralelo en cocina.");
        System.exit(0); // cerrar los hilos camareros/cocineros (bucle infinito por disenyo)
    }

    // Cola intermedia clientes -> camareros (ilimitada: los clientes nunca se quedan sin atender)
    static final BlockingQueue<Pedido> colaClientes = new ArrayBlockingQueue<>(CLIENTES);
}
