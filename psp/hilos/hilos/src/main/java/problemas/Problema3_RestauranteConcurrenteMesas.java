package problemas;
// PROBLEMA 3 - El Restaurante Concurrente (version con mesas)
//
// Flujo de un cliente (hilo virtual):
//   llega -> espera mesa libre -> se sienta -> pide -> espera el plato
//         -> se lo come -> libera la mesa -> se va
//
// Recursos compartidos (todas son BlockingQueue):
//   mesasLibres  : pool de Mesa libres (justa/FIFO => ya es la "lista de espera").
//                  El cliente hace take() para sentarse y put() al irse.
//   colaPedidos  : clientes sentados que quieren pedir (cliente -> camarero).
//   cocina       : mesa de pedidos de la cocina, capacidad 10 (camarero -> cocinero).
//   platosListos : platos terminados (cocinero -> camarero).
//
// Cada Pedido lleva un CountDownLatch: el camarero que sirve el plato hace
// countDown() y despierta justo al cliente dueño del pedido (sin lista de semaforos).
//
// Los camareros tienen PRIORIDAD para servir platos: antes de tomar un pedido nuevo
// o de meter uno en cocina, siempre miran si hay platos listos. Ademas nunca se
// bloquean indefinidamente en cocina.put() (usan offer con timeout), lo que evita
// el interbloqueo: cocina llena -> camareros bloqueados -> nadie sirve -> mesas
// sin liberar -> clientes sin irse.

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Problema3_RestauranteConcurrenteMesas {

    enum TipoPlato {
        ENSALADA(2000), PASTA(3000), PIZZA(4000), CARNE(5000);
        final int tiempoMs;

        TipoPlato(int tiempoMs) {
            this.tiempoMs = tiempoMs;
        }

        static TipoPlato aleatorio(Random r) {
            return values()[r.nextInt(values().length)];
        }
    }

    record Mesa(int id) {
    }

    // El latch "listo" se libera cuando el camarero deja el plato en la mesa.
    record Pedido(int cliente, TipoPlato plato, Mesa mesa, long horaPedidoMs, CountDownLatch listo) {
    }

    // --- Constantes ---
    static final int COCINEROS = 3;
    static final int CAMAREROS = 5;
    static final int CLIENTES = 100;
    static final int MESAS = 15;               // > CAPACIDAD_COCINA para que la cocina pueda llenarse
    static final int CAPACIDAD_COCINA = 10;
    static final long LLEGADA_CLIENTE_MS = 500;
    static final long TOMAR_PEDIDO_MS = 1000;
    static final long SERVIR_PLATO_MS = 500;
    static final long COMER_MIN_MS = 2000;
    static final long COMER_MAX_MS = 5000;
    static final long ESPERA_POLL_MS = 100;

    // --- Recursos compartidos ---
    // fair = true => los clientes obtienen mesa por orden de llegada
    static final BlockingQueue<Mesa> mesasLibres = new ArrayBlockingQueue<>(MESAS, true);
    // Como solo hay MESAS clientes sentados, nunca hay mas de MESAS pedidos vivos:
    // estas dos colas jamas se llenan y sus productores nunca se bloquean.
    static final BlockingQueue<Pedido> colaPedidos = new ArrayBlockingQueue<>(MESAS);
    static final BlockingQueue<Pedido> platosListos = new ArrayBlockingQueue<>(MESAS);
    // Unico punto de bloqueo "real": la mesa de pedidos de la cocina
    static final BlockingQueue<Pedido> cocina = new ArrayBlockingQueue<>(CAPACIDAD_COCINA);
    static final CountDownLatch clientesAtendidos = new CountDownLatch(CLIENTES);

    // --- Estadisticas ---
    static final AtomicInteger platosServidos = new AtomicInteger();
    static final AtomicLong esperaMesaTotalMs = new AtomicLong();
    static final AtomicLong esperaPlatoTotalMs = new AtomicLong();   // desde que pide hasta que le sirven
    static final AtomicInteger cocinaLlenaCount = new AtomicInteger();
    static final AtomicLong cocinerosEsperandoMs = new AtomicLong();
    static final AtomicInteger clientesEnEspera = new AtomicInteger();
    static final AtomicInteger maxClientesEnEspera = new AtomicInteger();

    static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    static String ahora() {
        return LocalTime.now().format(FORMATO_HORA);
    }

    static void log(String mensaje) {
        System.out.println("[" + ahora() + "] " + mensaje);
    }

    static String cli(int id) {
        return "Cliente-" + String.format("%03d", id);
    }

    // ------------------------------------------------------------------ COCINERO
    static void cocinero(int id) {
        try {
            while (true) {
                long t0 = System.currentTimeMillis();
                Pedido p = cocina.take(); // espera si no hay pedidos
                cocinerosEsperandoMs.addAndGet(System.currentTimeMillis() - t0);

                Thread.sleep(p.plato().tiempoMs); // cocina el plato
                log("Cocinero-" + id + " termina " + p.plato() + " para " + cli(p.cliente()));
                platosListos.put(p); // nunca bloquea (capacidad MESAS)
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // cierre del restaurante
        }
    }

    // ------------------------------------------------------------------ CAMARERO
    static void camarero(int id) {
        Pedido pendiente = null; // pedido ya tomado que aun no ha cabido en la cocina
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // 1) PRIORIDAD: servir platos listos
                Pedido listo = platosListos.poll();
                if (listo != null) {
                    Thread.sleep(SERVIR_PLATO_MS); // lleva el plato a la mesa
                    log("Camarero-" + id + " sirve " + listo.plato() + " a " + cli(listo.cliente())
                            + " en mesa " + listo.mesa().id());
                    listo.listo().countDown(); // despierta a ESE cliente
                    continue;
                }

                // 2) Si tiene un pedido esperando hueco en cocina, reintenta sin bloquearse para siempre
                if (pendiente != null) {
                    if (cocina.offer(pendiente, ESPERA_POLL_MS, TimeUnit.MILLISECONDS)) {
                        pendiente = null;
                    }
                    continue;
                }

                // 3) Si no, atiende a un cliente sentado (con timeout para volver a mirar los platos)
                Pedido nuevo = colaPedidos.poll(ESPERA_POLL_MS, TimeUnit.MILLISECONDS);
                if (nuevo != null) {
                    Thread.sleep(TOMAR_PEDIDO_MS); // toma nota
                    log("Camarero-" + id + " toma pedido " + nuevo.plato() + " de " + cli(nuevo.cliente()));
                    if (!cocina.offer(nuevo)) { // cocina llena
                        cocinaLlenaCount.incrementAndGet();
                        pendiente = nuevo;
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ------------------------------------------------------------------ CLIENTE
    static void cliente(int id) {
        Mesa mesa = null;
        try {
            long llegada = System.currentTimeMillis();
            log(cli(id) + " llega al restaurante");

            // Espera una mesa (cola FIFO de mesas libres)
            int enEspera = clientesEnEspera.incrementAndGet();
            maxClientesEnEspera.accumulateAndGet(enEspera, Math::max);
            mesa = mesasLibres.take();
            clientesEnEspera.decrementAndGet();
            esperaMesaTotalMs.addAndGet(System.currentTimeMillis() - llegada);
            log(cli(id) + " se sienta en mesa " + mesa.id());

            // Pide y espera su plato
            TipoPlato plato = TipoPlato.aleatorio(ThreadLocalRandom.current());
            Pedido pedido = new Pedido(id, plato, mesa, System.currentTimeMillis(), new CountDownLatch(1));
            log(cli(id) + " pide " + plato);
            colaPedidos.put(pedido);
            pedido.listo().await(); // lo despierta el camarero que le sirve
            esperaPlatoTotalMs.addAndGet(System.currentTimeMillis() - pedido.horaPedidoMs());
            platosServidos.incrementAndGet();

            // Come
            long comer = ThreadLocalRandom.current().nextLong(COMER_MIN_MS, COMER_MAX_MS + 1);
            log(cli(id) + " empieza a comer " + plato);
            Thread.sleep(comer);

            // Se va y libera la mesa
            mesasLibres.put(mesa);
            mesa = null;
            log(cli(id) + " se va y deja libre la mesa");
            clientesAtendidos.countDown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (mesa != null) { // por si algo falla a medias, no perdemos la mesa
                mesasLibres.offer(mesa);
            }
        }
    }

    // ------------------------------------------------------------------ MAIN
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== RESTAURANTE CONCURRENTE ===");
        System.out.println("Iniciando servicio con " + COCINEROS + " cocineros, " + CAMAREROS
                + " camareros y " + MESAS + " mesas...\n");

        // Todas las mesas empiezan libres
        for (int i = 1; i <= MESAS; i++) {
            mesasLibres.add(new Mesa(i));
        }

        List<Thread> personal = new ArrayList<>();

        // --- Cocineros ---
        for (int i = 1; i <= COCINEROS; i++) {
            final int id = i;
            personal.add(Thread.ofPlatform().name("cocinero-" + id).start(() -> cocinero(id)));
        }

        // --- Camareros ---
        for (int i = 1; i <= CAMAREROS; i++) {
            final int id = i;
            personal.add(Thread.ofPlatform().name("camarero-" + id).start(() -> camarero(id)));
        }

        // --- Clientes (hilos virtuales, llega uno cada 500 ms) ---
        for (int i = 1; i <= CLIENTES; i++) {
            final int id = i;
            Thread.ofVirtual().name("cliente-" + id).start(() -> cliente(id));
            Thread.sleep(LLEGADA_CLIENTE_MS);
        }

        clientesAtendidos.await(); // el restaurante no cierra hasta que se van los 100

        // Cierre ordenado: interrumpimos al personal y esperamos a que acabe
        for (Thread t : personal) {
            t.interrupt();
        }
        for (Thread t : personal) {
            t.join();
        }

        System.out.println("\n--- ESTADISTICAS FINALES ---");
        System.out.println("Clientes atendidos: " + CLIENTES + "/" + CLIENTES + " ✅");
        System.out.println("Platos servidos: " + platosServidos.get());
        System.out.printf("Tiempo medio esperando mesa: %.2fs%n",
                esperaMesaTotalMs.get() / 1000.0 / CLIENTES);
        System.out.printf("Tiempo medio desde que pide hasta que le sirven: %.2fs%n",
                esperaPlatoTotalMs.get() / 1000.0 / CLIENTES);
        System.out.println("Maximo de clientes esperando mesa a la vez: " + maxClientesEnEspera.get());
        System.out.println("Cocina llena (veces que un camarero no pudo dejar el pedido): "
                + cocinaLlenaCount.get());
        System.out.println("Cocineros esperando (tiempo acumulado): "
                + (cocinerosEsperandoMs.get() / 1000) + "s total");
    }
}
