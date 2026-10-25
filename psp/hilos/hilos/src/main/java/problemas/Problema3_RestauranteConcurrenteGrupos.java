package problemas;
// PROBLEMA 3 - El Restaurante Concurrente (version con grupos de comensales)
//
// Cada "cliente" es ahora un GRUPO de 1 a PLAZAS_MESA personas (hilo virtual).
// Cada comensal pide su propio plato.
//
// Flujo de un grupo:
//   llega -> espera mesa libre -> se sienta -> el camarero toma la comanda (un plato por persona)
//         -> espera a que le sirvan TODOS los platos -> comen -> liberan la mesa -> se van
//
// Recursos compartidos (todas son BlockingQueue):
//   mesasLibres   : pool de Mesa libres (justa/FIFO => es la lista de espera de la puerta).
//   colaComandas  : grupos sentados esperando que un camarero les tome nota.
//   cocina        : mesa de pedidos de la cocina, capacidad 10 (UN plato = UN elemento).
//   platosListos  : platos terminados esperando que un camarero los lleve a la mesa.
//
// Sincronizacion grupo <-> camareros: el Grupo lleva un CountDownLatch(n comensales).
// Cada plato servido hace countDown(); cuando llega a 0 se despierta al grupo.
//
// Los camareros tienen PRIORIDAD para servir platos listos, y nunca se bloquean
// indefinidamente al dejar platos en cocina (offer con timeout). Cada camarero guarda
// en una lista local los platos de su comanda que aun no han cabido en la cocina.

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Problema3_RestauranteConcurrenteGrupos {

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

    // El latch cuenta los platos que faltan por servir al grupo.
    record Grupo(int id, int comensales, Mesa mesa, CountDownLatch platosPendientes) {
    }

    // Un plato de un comensal concreto del grupo.
    record Pedido(Grupo grupo, int comensal, TipoPlato plato) {
    }

    // Todos los platos de un grupo, tal como los toma el camarero.
    record Comanda(Grupo grupo, List<Pedido> pedidos) {
    }

    // --- Constantes ---
    static final int COCINEROS = 3;
    static final int CAMAREROS = 5;
    static final int GRUPOS = 40;
    static final int MESAS = 8;
    static final int PLAZAS_MESA = 4;          // MESAS * PLAZAS_MESA = 32 > CAPACIDAD_COCINA: la cocina puede llenarse
    static final int CAPACIDAD_COCINA = 10;
    static final long LLEGADA_GRUPO_MS = 1000;
    static final long TOMAR_PEDIDO_POR_PERSONA_MS = 500;
    static final long SERVIR_PLATO_MS = 500;
    static final long COMER_MIN_MS = 2000;
    static final long COMER_MAX_MS = 5000;
    static final long ESPERA_POLL_MS = 100;

    // --- Recursos compartidos ---
    // fair = true => los grupos obtienen mesa por orden de llegada
    static final BlockingQueue<Mesa> mesasLibres = new ArrayBlockingQueue<>(MESAS, true);
    // Como mucho hay MESAS grupos sentados y MESAS*PLAZAS_MESA platos vivos:
    // estas colas jamas se llenan y sus productores nunca se bloquean.
    static final BlockingQueue<Comanda> colaComandas = new ArrayBlockingQueue<>(MESAS);
    static final BlockingQueue<Pedido> platosListos = new ArrayBlockingQueue<>(MESAS * PLAZAS_MESA);
    // Unico punto de bloqueo "real": la mesa de pedidos de la cocina
    static final BlockingQueue<Pedido> cocina = new ArrayBlockingQueue<>(CAPACIDAD_COCINA);
    static final CountDownLatch gruposAtendidos = new CountDownLatch(GRUPOS);

    // --- Estadisticas ---
    static final AtomicInteger comensalesAtendidos = new AtomicInteger();
    static final AtomicInteger platosServidos = new AtomicInteger();
    static final AtomicLong esperaMesaTotalMs = new AtomicLong();
    static final AtomicLong esperaPlatosTotalMs = new AtomicLong();   // desde la comanda hasta tener TODOS los platos
    static final AtomicInteger cocinaLlenaCount = new AtomicInteger();
    static final AtomicLong cocinerosEsperandoMs = new AtomicLong();
    static final AtomicInteger gruposEnEspera = new AtomicInteger();
    static final AtomicInteger maxGruposEnEspera = new AtomicInteger();

    static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    static String ahora() {
        return LocalTime.now().format(FORMATO_HORA);
    }

    static void log(String mensaje) {
        System.out.println("[" + ahora() + "] " + mensaje);
    }

    static String etiqueta(int id, int comensales) {
        return "Grupo-" + String.format("%03d", id) + " (" + comensales + " pers.)";
    }

    static String etiqueta(Grupo g) {
        return etiqueta(g.id(), g.comensales());
    }

    // ------------------------------------------------------------------ COCINERO
    static void cocinero(int id) {
        try {
            while (true) {
                long t0 = System.currentTimeMillis();
                Pedido p = cocina.take(); // espera si no hay platos que cocinar
                cocinerosEsperandoMs.addAndGet(System.currentTimeMillis() - t0);

                Thread.sleep(p.plato().tiempoMs); // cocina el plato
                log("Cocinero-" + id + " termina " + p.plato() + " para " + etiqueta(p.grupo())
                        + " [comensal " + p.comensal() + "]");
                platosListos.put(p); // nunca bloquea (capacidad MESAS*PLAZAS_MESA)
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // cierre del restaurante
        }
    }

    // ------------------------------------------------------------------ CAMARERO
    static void camarero(int id) {
        // Platos de comandas ya tomadas que aun no han cabido en la cocina
        Deque<Pedido> pendientes = new ArrayDeque<>();
        boolean esperandoHueco = false; // para contar "cocina llena" una sola vez por plato
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // 1) PRIORIDAD: servir platos listos
                Pedido listo = platosListos.poll();
                if (listo != null) {
                    Thread.sleep(SERVIR_PLATO_MS); // lleva el plato a la mesa
                    log("Camarero-" + id + " sirve " + listo.plato() + " a " + etiqueta(listo.grupo())
                            + " [comensal " + listo.comensal() + "] en mesa " + listo.grupo().mesa().id());
                    platosServidos.incrementAndGet();
                    listo.grupo().platosPendientes().countDown(); // un plato menos; con 0 despierta al grupo
                    continue;
                }

                // 2) Si tiene platos esperando hueco en cocina, intenta dejar el primero sin bloquearse para siempre
                Pedido siguiente = pendientes.peekFirst();
                if (siguiente != null) {
                    boolean colocado = cocina.offer(siguiente);
                    if (!colocado) {
                        if (!esperandoHueco) {
                            cocinaLlenaCount.incrementAndGet();
                            esperandoHueco = true;
                        }
                        colocado = cocina.offer(siguiente, ESPERA_POLL_MS, TimeUnit.MILLISECONDS);
                    }
                    if (colocado) {
                        pendientes.pollFirst();
                        esperandoHueco = false;
                    }
                    continue;
                }

                // 3) Si no, atiende a un grupo sentado (con timeout para volver a mirar los platos)
                Comanda comanda = colaComandas.poll(ESPERA_POLL_MS, TimeUnit.MILLISECONDS);
                if (comanda != null) {
                    Thread.sleep(TOMAR_PEDIDO_POR_PERSONA_MS * comanda.grupo().comensales()); // toma nota a todos
                    log("Camarero-" + id + " toma comanda de " + etiqueta(comanda.grupo()) + ": "
                            + comanda.pedidos().stream().map(p -> p.plato().name()).toList());
                    pendientes.addAll(comanda.pedidos());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ------------------------------------------------------------------ GRUPO DE CLIENTES
    static void grupo(int id) {
        Random rnd = ThreadLocalRandom.current();
        int comensales = rnd.nextInt(1, PLAZAS_MESA + 1);
        String nombre = etiqueta(id, comensales);
        Mesa mesa = null;
        try {
            long llegada = System.currentTimeMillis();
            log(nombre + " llega al restaurante");

            // Espera una mesa (cola FIFO de mesas libres)
            int enEspera = gruposEnEspera.incrementAndGet();
            maxGruposEnEspera.accumulateAndGet(enEspera, Math::max);
            mesa = mesasLibres.take();
            gruposEnEspera.decrementAndGet();
            esperaMesaTotalMs.addAndGet(System.currentTimeMillis() - llegada);
            log(nombre + " se sienta en mesa " + mesa.id());

            // Cada comensal elige su plato y el grupo pide todo junto
            Grupo grupo = new Grupo(id, comensales, mesa, new CountDownLatch(comensales));
            List<Pedido> pedidos = new ArrayList<>();
            for (int c = 1; c <= comensales; c++) {
                pedidos.add(new Pedido(grupo, c, TipoPlato.aleatorio(rnd)));
            }
            long horaComanda = System.currentTimeMillis();
            colaComandas.put(new Comanda(grupo, pedidos));

            grupo.platosPendientes().await(); // lo despierta el camarero que sirve el ultimo plato
            esperaPlatosTotalMs.addAndGet(System.currentTimeMillis() - horaComanda);
            comensalesAtendidos.addAndGet(comensales);

            // Comen (todos a la vez, empiezan cuando ya tienen todos los platos)
            long comer = rnd.nextLong(COMER_MIN_MS, COMER_MAX_MS + 1);
            log(nombre + " empieza a comer");
            Thread.sleep(comer);

            // Se van y liberan la mesa
            mesasLibres.put(mesa);
            mesa = null;
            log(nombre + " se va y deja libre la mesa");
            gruposAtendidos.countDown();
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
        System.out.println("=== RESTAURANTE CONCURRENTE (GRUPOS) ===");
        System.out.println("Iniciando servicio con " + COCINEROS + " cocineros, " + CAMAREROS
                + " camareros y " + MESAS + " mesas de " + PLAZAS_MESA + " plazas...\n");

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

        // --- Grupos de clientes (hilos virtuales, llega uno cada LLEGADA_GRUPO_MS) ---
        for (int i = 1; i <= GRUPOS; i++) {
            final int id = i;
            Thread.ofVirtual().name("grupo-" + id).start(() -> grupo(id));
            Thread.sleep(LLEGADA_GRUPO_MS);
        }

        gruposAtendidos.await(); // el restaurante no cierra hasta que se van todos los grupos

        // Cierre ordenado: interrumpimos al personal y esperamos a que acabe
        for (Thread t : personal) {
            t.interrupt();
        }
        for (Thread t : personal) {
            t.join();
        }

        System.out.println("\n--- ESTADISTICAS FINALES ---");
        System.out.println("Grupos atendidos: " + GRUPOS + "/" + GRUPOS + " ✅");
        System.out.println("Comensales atendidos: " + comensalesAtendidos.get());
        System.out.println("Platos servidos: " + platosServidos.get());
        System.out.printf("Tiempo medio de un grupo esperando mesa: %.2fs%n",
                esperaMesaTotalMs.get() / 1000.0 / GRUPOS);
        System.out.printf("Tiempo medio desde la comanda hasta tener todos los platos: %.2fs%n",
                esperaPlatosTotalMs.get() / 1000.0 / GRUPOS);
        System.out.println("Maximo de grupos esperando mesa a la vez: " + maxGruposEnEspera.get());
        System.out.println("Cocina llena (veces que un camarero no pudo dejar un plato): "
                + cocinaLlenaCount.get());
        System.out.println("Cocineros esperando (tiempo acumulado): "
                + (cocinerosEsperandoMs.get() / 1000) + "s total");
    }
}
