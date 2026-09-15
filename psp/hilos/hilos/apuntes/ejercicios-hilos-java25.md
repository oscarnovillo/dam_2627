# Ejercicios de Concurrencia en Java 25

> Requisitos: JDK 25. Se recomienda compilar con `javac --release 25` y ejecutar con `java`.
> Cada ejercicio incluye: contexto, enunciado, requisitos técnicos y pistas. El ejercicio **Final** incluye solución completa comentada.

---

## 1. `Thread`, `Runnable` — Creación de hilos

### Ejercicio 1.1 — "Descarga simultánea de archivos"
Simula la descarga de 4 archivos en paralelo. Cada archivo debe descargarse en su propio hilo, imprimiendo su progreso cada 200 ms hasta llegar al 100%.

**Requisitos:**
- Implementa una versión usando una clase que **extienda `Thread`**.
- Implementa otra versión usando una clase que **implemente `Runnable`**, lanzada con `new Thread(runnable).start()`.
- Cada hilo debe imprimir su nombre (`Thread.currentThread().getName()`) y el progreso.
- Compara ambos enfoques en un comentario: ¿cuándo conviene cada uno?

**Pista:** usa `Thread.ofPlatform().name("descarga-" + i).start(runnable)` (API de hilos con nombre de Java 21+, disponible en 25) como alternativa moderna a `new Thread(...)`.

### Ejercicio 1.2 — "Hilos virtuales"
Repite el ejercicio 1.1 pero lanzando 10.000 "descargas" simultáneas usando **hilos virtuales** (`Thread.ofVirtual()`), y mide el tiempo total frente a usar hilos de plataforma.

---

## 2. `join()`, `sleep()`, `interrupt()` — Ciclo de vida

### Ejercicio 2.1 — "Cuenta regresiva de cohete"
Un hilo hace una cuenta regresiva de 10 a 0, durmiendo 1 segundo entre número y número (`Thread.sleep`). El hilo principal debe esperar a que termine con `join()` antes de imprimir "¡Despegue!".

### Ejercicio 2.2 — "Cocinero con temporizador cancelable"
Un hilo "cocina" un plato que tarda 10 segundos (`sleep` en bucle de 1 en 1 segundo). El hilo principal, tras 3 segundos, debe **interrumpir** la cocción con `interrupt()`. El hilo cocinero debe capturar `InterruptedException`, imprimir "Cocción cancelada" y terminar limpiamente.

**Requisitos:**
- Muestra el estado del hilo (`getState()`) en distintos momentos: `NEW`, `RUNNABLE`, `TIMED_WAITING`, `TERMINATED`.
- Explica por qué `interrupt()` no detiene el hilo por sí solo si no se comprueba `isInterrupted()` o no se captura `InterruptedException`.

---

## 3. `synchronized` — Exclusión mutua, deadlock

### Ejercicio 3.1 — "Cuenta bancaria con condición de carrera"
Crea una clase `CuentaBancaria` con un `saldo` (int) y un método `depositar(int cantidad)` que haga `saldo = saldo + cantidad` (sin sincronizar). Lanza 100 hilos que depositen 1 euro cada uno, de forma concurrente, y comprueba que el saldo final **no** es 100 (condición de carrera).

**Requisitos:**
- Primero demuestra el fallo (ejecuta varias veces y observa resultados distintos).
- Corrige el problema declarando `depositar` como `synchronized`.
- Repite con un bloque `synchronized(this)` en vez de sincronizar el método completo.

### Ejercicio 3.2 — "Deadlock entre dos cuentas"
Implementa un método `transferir(CuentaBancaria destino, int cantidad)` que haga `synchronized(this)` y dentro `synchronized(destino)`. Crea dos hilos: uno transfiere de A a B, el otro de B a A, simultáneamente. Provoca el **deadlock** y luego corrígelo ordenando los locks (por ejemplo, por un ID único de cuenta).

**Pista:** usa `jconsole` o `jcmd <pid> Thread.print` para detectar el deadlock.

---

## 4. `wait()`/`notify()` — Comunicación entre hilos

### Ejercicio 4.1 — "Buffer de un solo hueco (productor-consumidor)"
Implementa una clase `Buffer` con capacidad 1 y métodos `poner(int valor)` y `int tomar()`, ambos `synchronized`, usando `wait()` y `notifyAll()` para evitar que el productor sobrescriba un valor no consumido y que el consumidor lea un buffer vacío.

**Requisitos:**
- Un hilo productor genera números del 1 al 20.
- Un hilo consumidor los consume e imprime.
- Usa un bucle `while` (no `if`) al comprobar la condición antes de `wait()`.

---

## 5. `ReentrantLock`, `Semaphore`, `AtomicInteger` — Alternativas a `synchronized`

### Ejercicio 5.1 — "Parking con plazas limitadas"
Un parking tiene 3 plazas. Modela cada plaza con un `Semaphore(3)`. Simula 10 coches (hilos) que intentan entrar (`acquire()`), permanecen un tiempo aleatorio y salen (`release()`). Imprime cuándo un coche espera porque no hay plazas.

### Ejercicio 5.2 — "Contador de visitas sin bloqueo"
Implementa un contador de visitas a una web usando `AtomicInteger` en lugar de `synchronized`. Lanza 1000 hilos incrementando el contador y verifica que el resultado final es siempre 1000.

### Ejercicio 5.3 — "Recurso compartido con `tryLock`"
Usa `ReentrantLock` con `tryLock(500, TimeUnit.MILLISECONDS)` para que un hilo que no consigue el lock en medio segundo, en vez de bloquearse indefinidamente, imprima "Ocupado, lo intento más tarde" y reintente.

---

## 6. `BlockingQueue`, `ConcurrentHashMap` — Colecciones seguras

### Ejercicio 6.1 — "Productor-consumidor con `ArrayBlockingQueue`"
Reimplementa el ejercicio 4.1 pero usando `ArrayBlockingQueue<Integer>` de capacidad 5 en vez de `wait/notify` manual. Compara la simplicidad del código.

### Ejercicio 6.2 — "Contador de palabras concurrente"
Varios hilos leen fragmentos distintos de un texto grande y actualizan un `ConcurrentHashMap<String, Integer>` con la frecuencia de cada palabra, usando `merge(palabra, 1, Integer::sum)`.

**Requisito extra:** compara el resultado y el rendimiento frente a usar un `HashMap` normal con `synchronized`.

---

## 7. `ExecutorService`, `Callable`, `Future` — Pools de hilos

### Ejercicio 7.1 — "Suma paralela de un array grande"
Divide un array de 10 millones de enteros en 4 trozos. Usa un `ExecutorService` (`Executors.newFixedThreadPool(4)`) con tareas `Callable<Long>` que sumen cada trozo, recogiendo los resultados con `Future<Long>` y sumándolos al final.

### Ejercicio 7.2 — "Consultas simuladas a servicios externos"
Simula 5 "llamadas a APIs" (métodos que duermen entre 1 y 3 segundos y devuelven un `String`) usando `Callable<String>`. Lánzalas con `invokeAll()` y procesa los resultados con `future.get()`. Usa un `try-with-resources` sobre el `ExecutorService` (característica de Java 19+, `AutoCloseable`).

**Pista Java 25:** puedes usar `Executors.newVirtualThreadPerTaskExecutor()` para lanzar cada tarea en su propio hilo virtual sin límite de pool.

---

## 8. `CountDownLatch`, `CyclicBarrier` — Sincronización de fases

### Ejercicio 8.1 — "Salida de carrera"
5 corredores (hilos) se preparan (duermen un tiempo aleatorio simulando "calentar"). Ninguno puede empezar a correr hasta que todos estén listos. Usa un `CountDownLatch(5)`: cada corredor hace `countDown()` al estar listo, y todos esperan un segundo `CountDownLatch(1)` que el hilo "juez" libera con `countDown()` para dar la salida.

### Ejercicio 8.2 — "Trabajo en fases con `CyclicBarrier`"
4 hilos representan a un equipo que debe completar 3 fases de un proyecto. Ningún hilo puede pasar a la fase siguiente hasta que **todos** terminen la fase actual. Usa un `CyclicBarrier(4, accionAlLlegarTodos)` donde la acción imprime "Fase completada, todos avanzan".

---

## Final — Integración completa (con solución)

### Enunciado
Implementa un **simulador de restaurante concurrente**:

- Hay **3 cocineros** (hilos) que preparan pedidos.
- Los pedidos llegan a una `BlockingQueue<Pedido>` desde un hilo "camarero" que genera 15 pedidos.
- Cada cocinero toma un pedido de la cola, lo prepara (simulado con `sleep` aleatorio) y actualiza un `ConcurrentHashMap<String, Integer>` con el conteo de platos preparados por tipo.
- Un contador `AtomicInteger` lleva el total de pedidos completados.
- El restaurante **no cierra** (no se detienen los cocineros) hasta que se han completado los 15 pedidos: usa un `CountDownLatch(15)`.
- Antes de abrir el restaurante, los 3 cocineros deben terminar su "preparación de turno" (fase previa) sincronizada con un `CyclicBarrier(3)`.
- El acceso a un `List<String> registroPedidos` compartido para auditoría debe protegerse con `synchronized` (o `ReentrantLock`), demostrando que sabes combinarlo con las colecciones concurrentes.
- Al final, usa un `ExecutorService` (con `invokeAll` o `submit` + `Future`) para generar 3 informes en paralelo (resumen por tipo de plato, tiempo total, pedido más lento), devueltos como `Callable<String>`.

### Solución completa

```java
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RestauranteConcurrente {

    record Pedido(int id, String plato, long tiempoPreparacionMs) {}

    // Recurso compartido protegido con lock explícito (alternativa a synchronized)
    private static final List<String> registroPedidos = Collections.synchronizedList(new ArrayList<>());
    private static final ReentrantLockAudit auditLock = new ReentrantLockAudit();

    // Pequeña envoltura para dejar patente el uso de ReentrantLock además de synchronized
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

        // Barrera de "preparación de turno": los 3 cocineros deben estar listos antes de abrir
        CyclicBarrier barreraApertura = new CyclicBarrier(NUM_COCINEROS,
                () -> System.out.println(">> Todos los cocineros listos. ¡Restaurante abierto!"));

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
                    cola.put(pedido); // bloquea si la cola está llena
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
                        if (pedido == null) continue; // no había pedidos disponibles, reintenta

                        Thread.sleep(pedido.tiempoPreparacionMs());

                        platosPreparados.merge(pedido.plato(), 1, Integer::sum);
                        int completadosAhora = totalCompletados.incrementAndGet();

                        auditLock.registrar("Pedido #" + pedido.id() + " (" + pedido.plato()
                                + ") preparado por cocinero-" + idCocinero
                                + " en " + pedido.tiempoPreparacionMs() + " ms");

                        System.out.println("[Cocinero " + idCocinero + "] completó pedido #" + pedido.id()
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

        // --- Generación de informes en paralelo con ExecutorService/Callable/Future ---
        try (ExecutorService executor = Executors.newFixedThreadPool(3)) {
            Callable<String> informePlatos = () ->
                    "Resumen por plato: " + platosPreparados;

            Callable<String> informeTiempo = () ->
                    "Tiempo total del servicio: " + duracionTotal + " ms";

            Callable<String> informeAuditoria = () -> {
                synchronized (registroPedidos) {
                    return "Registro de auditoría (" + registroPedidos.size() + " entradas):\n  "
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
```

### Puntos clave que demuestra la solución

| Técnica | Dónde se usa |
|---|---|
| `Thread.ofPlatform().start(...)` | Creación de hilos con nombre (tema 1) |
| `sleep`, `join`, manejo de `InterruptedException` | Ciclo de vida (tema 2) |
| `synchronized` (via `Collections.synchronizedList` y bloque `synchronized`) | Exclusión mutua (tema 3) |
| *(sustituido conceptualmente por `BlockingQueue`)* | Comunicación productor-consumidor (tema 4) |
| `ReentrantLock` | Alternativa a `synchronized` (tema 5) |
| `ArrayBlockingQueue`, `ConcurrentHashMap` | Colecciones seguras (tema 6) |
| `ExecutorService`, `Callable`, `Future`, `invokeAll` | Pools de hilos (tema 7) |
| `CountDownLatch`, `CyclicBarrier` | Sincronización de fases (tema 8) |

### Posibles extensiones (para practicar más)
- Sustituir los hilos de plataforma por **hilos virtuales** (`Thread.ofVirtual()`) y comparar el uso de memoria con 1000 cocineros simulados.
- Introducir un cocinero que se "atasca" y usar `tryLock`/timeout para detectar bloqueos.
- Añadir un `Semaphore` que limite cuántos pedidos pueden estar "en preparación" simultáneamente, independientemente del número de cocineros.

