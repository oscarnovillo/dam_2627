package examen;
/*
 PROBLEMA 3 - El Restaurante Concurrente de clase dado como solucion
 Patron Productor-Consumidor con 5 camareros (productores), 3 cocineros
 (consumidores) y 100 clientes (hilos virtuales).
 con los siguientes cambios
 1Pto El cierre del resturante deberá de ser más controlado, y no salir hasta parar a los cocineros y camareros.
 0,5 Pto  Tb pon precio a los platos y acumula cuanto ha ganado el restaurente en el servicio.
 2Pto Flujo de un cliente (hilo virtual):
   llega -> espera mesa libre durante un tiempo(elige el tiempo para que no se vayan todos) -> se sienta si la consigue y si no se va-> pide -> espera el plato
         -> se lo come -> libera la mesa -> se va
 2Pto Los camareros tienen PRIORIDAD para servir platos: antes de tomar un pedido nuevo
     o de meter uno en cocina, siempre miran si hay platos listos para entregarlos a los clientes.
 2Pto Cambia los clientes por un grupo de clientes.  Flujo de un grupo:
       la primera parte hasta que se sientan es igual que un cliente solo -> el camarero toma la comanda (un plato por persona)
             -> espera a que le sirvan TODOS los platos -> comen -> liberan la mesa -> se van

*/
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Ejercicio3 {

    enum TipoPlato {
        ENSALADA(2000), PASTA(3000), PIZZA(4000), CARNE(5000);
        final int tiempoMs;
        TipoPlato(int tiempoMs) { this.tiempoMs = tiempoMs; }
        static examen.Ejercicio3.TipoPlato aleatorio(Random r) {
            return values()[r.nextInt(values().length)];
        }
    }

    record Pedido(int cliente, examen.Ejercicio3.TipoPlato plato, long horaCreacionMs) {}

    // --- Constantes del enunciado ---
    static final int COCINEROS = 3;
    static final int CAMAREROS = 5;
    static final int CLIENTES = 100;
    static final int CAPACIDAD_MESA = 10;
    static final long LLEGADA_CLIENTE_MS = 500;
    static final long TIEMPO_CAMARERO_MS = 1000;

    // --- Recursos compartidos ---
    static final BlockingQueue<examen.Ejercicio3.Pedido> mesa = new ArrayBlockingQueue<>(CAPACIDAD_MESA);
    static final CountDownLatch clientesAtendidos = new CountDownLatch(CLIENTES);
    // Cola intermedia clientes -> camareros (ilimitada: los clientes nunca se quedan sin atender)
    static final BlockingQueue<Pedido> colaClientes = new ArrayBlockingQueue<>(CLIENTES);

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

        // --- Cocineros (consumidores) ---
        for (int i = 1; i <= COCINEROS; i++) {
            final int id = i;
            Thread.ofPlatform().name("cocinero-" + id).start(() -> {
                try {
                    while (true) {
                        long t0 = System.currentTimeMillis();
                        Pedido p = mesa.take(); // espera si no hay pedidos
                        cocinerosEsperandoMs.addAndGet(System.currentTimeMillis() - t0);

                        Thread.sleep(p.plato().tiempoMs); // cocina el plato
                        esperaTotalMs.addAndGet(System.currentTimeMillis() - p.horaCreacionMs());
                        platosServidos.incrementAndGet();
                        clientesAtendidos.countDown();

                        System.out.println("[" + ahora() + "] Cocinero-" + id + " termina "
                                + p.plato() + " para Cliente-" + p.cliente());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // cierre del restaurante
                }
            });
        }

        // --- Camareros (productores) ---
        for (int i = 1; i <= CAMAREROS; i++) {
            final int id = i;
            Thread.ofPlatform().name("camarero-" + id).start(() -> {
                try {
                    while (true) {
                        Pedido p = colaClientes.take(); // espera a que llegue un cliente
                        Thread.sleep(TIEMPO_CAMARERO_MS); // toma el pedido
                        System.out.println("[" + ahora() + "] Camarero-" + id
                                + " toma pedido " + p.plato());
                        if (mesa.remainingCapacity() == 0) {
                            mesaLlenaCount.incrementAndGet();
                        }
                        mesa.put(p); // espera si la mesa esta llena
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // --- Clientes (hilos virtuales, llegan cada 500 ms) ---
        Thread generadorClientes = Thread.ofPlatform().name("puerta").start(() -> {
            for (int i = 1; i <= CLIENTES; i++) {
                final int id = i;
                TipoPlato plato = TipoPlato.aleatorio(new Random());
                System.out.println("[" + ahora() + "] Cliente-" + String.format("%03d", id)
                        + " pide " + plato);
                try {
                    colaClientes.put(new Pedido(id, plato, System.currentTimeMillis()));
                    Thread.sleep(LLEGADA_CLIENTE_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        clientesAtendidos.await(); // el restaurante no cierra hasta atender a los 100
        generadorClientes.join();

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

}
