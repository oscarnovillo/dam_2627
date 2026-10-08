package solucionesEjerciciosHilosJava25;

// FINAL - Simulador de restaurante concurrente (solucion completa)
// Reune todas las tecnicas: hilos con nombre, sleep/join/interrupt,
// synchronized + ReentrantLock, BlockingQueue, ConcurrentHashMap,
// AtomicInteger, ExecutorService/Callable/Future, CountDownLatch y CyclicBarrier.

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RestauranteConcurrente {

    record Pedido(int id, String plato, long tiempoPreparacionMs) {}

    // Recurso compartido protegido con lock explicito (alternativa a synchronized)
    private static final List<String> registroPedidos = Collections.synchronizedList(new ArrayList<>());
    private static final ReentrantLockAudit auditLock = new ReentrantLockAudit();

    // Pequena envoltura para dejar patente el uso de ReentrantLock ademas de synchronized
    static class ReentrantLockAudit {
        private final java.util.concurrent.locks.ReentrantLock lock = new java.util.concurrent.locks.ReentrantLock();
        void registrar(String texto) {
            lock.lock();
            try {
                registroPedidos.add(texto);
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final int NUM_PEDIDOS = 15;
        final int NUM_COCINEROS = 3;

        BlockingQueue<Pedido> cola = new ArrayBlockingQueue<>(10);
        ConcurrentHashMap<String, Integer> platosPreparados = new ConcurrentHashMap<>();
        AtomicInteger totalCompletados = new AtomicInteger(0);
        CountDownLatch latchPedidosCompletados = new CountDownLatch(NUM_PEDIDOS);

        // Barrera de "preparacion de turno": los 3 cocineros deben estar listos antes de abrir
        CyclicBarrier barreraApertura = new CyclicBarrier(NUM_COCINEROS,
                () -> System.out.println(">> Todos los cocineros listos. !Restaurante abierto!"));

        String[] tiposPlato = {"Paella", "Tortilla", "Gazpacho", "Croquetas"};
        long inicioGlobal = System.currentTimeMillis();

        // --- Hilo camarero: genera los pedidos ---
        Thread camarero = Thread.ofPlatform().name("camarero").start(() -> {
            Random rnd = new Random();
            try {
                for (int i = 1; i <= NUM_PEDIDOS; i++) {
                    String plato = tiposPlato[rnd.nextInt(tiposPlato.length)];
                    long tiempo = 300 + rnd.nextInt(700);
                    Pedido pedido = new Pedido(i, plato, tiempo);
                    cola.put(pedido); // bloquea si la cola esta llena
                    System.out.println("[Camarero] Pedido #" + i + " (" + plato + ") enviado a cocina.");
                    Thread.sleep(150);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // --- Hilos cocineros ---
        List<Thread> cocineros = new ArrayList<>();
        for (int c = 1; c <= NUM_COCINEROS; c++) {
            final int idCocinero = c;
            Thread cocinero = Thread.ofPlatform().name("cocinero-" + c).start(() -> {
                try {
                    // Fase previa: "preparar el turno" antes de poder cocinar
                    Thread.sleep(200 + new Random().nextInt(300));
                    System.out.println("[Cocinero " + idCocinero + "] turno preparado, esperando al resto...");
                    barreraApertura.await();

                    while (totalCompletados.get() < NUM_PEDIDOS) {
                        Pedido pedido = cola.poll(500, TimeUnit.MILLISECONDS);
                        if (pedido == null) continue; // no habia pedidos, reintenta

                        Thread.sleep(pedido.tiempoPreparacionMs());

                        platosPreparados.merge(pedido.plato(), 1, Integer::sum);
                        int completadosAhora = totalCompletados.incrementAndGet();

                        auditLock.registrar("Pedido #" + pedido.id() + " (" + pedido.plato()
                                + ") preparado por cocinero-" + idCocinero
                                + " en " + pedido.tiempoPreparacionMs() + " ms");

                        System.out.println("[Cocinero " + idCocinero + "] completo pedido #" + pedido.id()
                                + " -> total completados: " + completadosAhora);

                        latchPedidosCompletados.countDown();
                    }
                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                }
            });
            cocineros.add(cocinero);
        }

        // Esperamos a que se completen todos los pedidos
        latchPedidosCompletados.await();
        camarero.join();
        for (Thread t : cocineros) t.join();

        long duracionTotal = System.currentTimeMillis() - inicioGlobal;

        // --- Generacion de informes en paralelo con ExecutorService/Callable/Future ---
        try (ExecutorService executor = Executors.newFixedThreadPool(3)) {
            Callable<String> informePlatos = () ->
                    "Resumen por plato: " + platosPreparados;

            Callable<String> informeTiempo = () ->
                    "Tiempo total del servicio: " + duracionTotal + " ms";

            Callable<String> informeAuditoria = () -> {
                synchronized (registroPedidos) {
                    return "Registro de auditoria (" + registroPedidos.size() + " entradas):\n  "
                            + String.join("\n  ", registroPedidos);
                }
            };

            List<Future<String>> resultados = executor.invokeAll(
                    List.of(informePlatos, informeTiempo, informeAuditoria));

            System.out.println("\n===== INFORMES FINALES =====");
            for (Future<String> f : resultados) {
                System.out.println(f.get());
            }
        }
    }
}
