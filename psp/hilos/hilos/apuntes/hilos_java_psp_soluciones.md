# PSP — Programación Multihilo en Java
## Soluciones de referencia de los ejercicios

> Estas son soluciones de referencia con fines docentes. Hay varias formas correctas de resolver cada ejercicio; lo importante es que el alumnado justifique sus decisiones de diseño.

---

## Bloque 1 — Fundamentos: `Thread` y `Runnable`

### Solución 1.1 — Primer hilo
```java
public class ContadorThread extends Thread {
    @Override
    public void run() {
        for (int i = 1; i <= 10; i++) {
            System.out.println(getName() + " -> " + i);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        ContadorThread t1 = new ContadorThread();
        ContadorThread t2 = new ContadorThread();
        t1.start();
        t2.start();
    }
}
```
**Nota didáctica:** al lanzar dos instancias, la salida se entremezcla porque ambos hilos se ejecutan de forma concurrente y el planificador decide el orden.

### Solución 1.2 — Con `Runnable`
```java
public class ContadorRunnable implements Runnable {
    @Override
    public void run() {
        for (int i = 1; i <= 10; i++) {
            System.out.println(Thread.currentThread().getName() + " -> " + i);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        Thread t1 = new Thread(new ContadorRunnable());
        Thread t2 = new Thread(new ContadorRunnable());
        t1.start();
        t2.start();
    }
}
```
**Ventaja de `Runnable`:** la clase no "gasta" su única herencia en `Thread`, por lo que puede extender otra clase si lo necesita; además separa la tarea (qué se hace) del mecanismo de ejecución (cómo se ejecuta).

### Solución 1.3 — El error clásico
```java
public class ErrorRunVsStart {
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            System.out.println("Ejecutando en: " + Thread.currentThread().getName());
        });
        t.run(); // <-- error: se ejecuta en el hilo main, no crea hilo nuevo
    }
}
```
**Explicación esperada del alumno:** `run()` es un método normal; llamarlo directamente simplemente ejecuta ese código en el hilo que hace la llamada (`main`), sin crear ningún hilo nuevo. Solo `start()` pide a la JVM que reserve una nueva pila de ejecución y planifique el hilo.

### Solución 1.4 — Hilos daemon
```java
import java.time.LocalTime;

public class RelojDaemon {
    public static void main(String[] args) throws InterruptedException {
        Thread reloj = new Thread(() -> {
            while (true) {
                System.out.println("Hora actual: " + LocalTime.now());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        reloj.setDaemon(true);
        reloj.start();

        Thread.sleep(3500);
        System.out.println("Fin del main, el daemon morirá con él.");
    }
}
```

---

## Bloque 2 — Estados y ciclo de vida

### Solución 2.1 — Observador de estados
```java
public class ObservadorEstados {
    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {}
        });

        System.out.println("Estado antes de start: " + t.getState()); // NEW
        t.start();

        while (t.isAlive()) {
            System.out.println("Estado: " + t.getState());
            Thread.sleep(500);
        }
        System.out.println("Estado final: " + t.getState()); // TERMINATED
    }
}
```

### Solución 2.2 — `join()` en acción
```java
import java.util.Random;

public class DescargasJoin {
    public static void main(String[] args) throws InterruptedException {
        Random random = new Random();
        Thread[] descargas = new Thread[3];

        for (int i = 0; i < 3; i++) {
            int id = i + 1;
            descargas[i] = new Thread(() -> {
                int duracion = 1000 + random.nextInt(2000);
                System.out.println("Descarga " + id + " iniciada (" + duracion + " ms)");
                try {
                    Thread.sleep(duracion);
                } catch (InterruptedException ignored) {}
                System.out.println("Descarga " + id + " finalizada");
            });
            descargas[i].start();
        }

        for (Thread t : descargas) {
            t.join();
        }
        System.out.println("Todas las descargas han finalizado");
    }
}
```

### Solución 2.3 — Interrupción cooperativa
```java
public class InterrupcionCooperativa {
    public static void main(String[] args) throws InterruptedException {
        Thread trabajador = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                // simula trabajo
            }
            System.out.println("Interrumpido de forma educada, cerrando recursos...");
        });

        trabajador.start();
        Thread.sleep(2000);
        trabajador.interrupt();
    }
}
```

---

## Bloque 3 — Condiciones de carrera y `synchronized`

### Solución 3.1 — El contador roto
```java
public class ContadorRoto {
    private int contador = 0;

    public void incrementar() {
        contador++; // NO es atómico: leer + sumar + escribir
    }

    public int getContador() {
        return contador;
    }

    public static void main(String[] args) throws InterruptedException {
        ContadorRoto c = new ContadorRoto();
        Runnable tarea = () -> {
            for (int i = 0; i < 10_000; i++) c.incrementar();
        };

        Thread[] hilos = new Thread[10];
        for (int i = 0; i < 10; i++) hilos[i] = new Thread(tarea);
        for (Thread t : hilos) t.start();
        for (Thread t : hilos) t.join();

        System.out.println("Resultado (esperado 100000): " + c.getContador());
    }
}
```
**Explicación:** `contador++` implica tres pasos (leer, incrementar, escribir) que pueden entrelazarse entre hilos, perdiendo incrementos.

### Solución 3.2 — El contador arreglado (`synchronized` en el método)
```java
public class ContadorSincronizado {
    private int contador = 0;

    public synchronized void incrementar() {
        contador++;
    }

    public synchronized int getContador() {
        return contador;
    }

    // main igual que en 3.1, cambiando la clase usada
}
```

### Solución 3.3 — Bloques sincronizados
```java
public class ContadorBloque {
    private int contador = 0;
    private final Object lock = new Object();

    public void incrementar() {
        synchronized (lock) {
            contador++;
        }
    }

    public int getContador() {
        synchronized (lock) {
            return contador;
        }
    }
}
```
**Ventaja:** permite sincronizar solo la parte crítica del método (útil si hay más código no sensible a condiciones de carrera), y usar un lock distinto a `this` evita interferencias con otros bloqueos externos sobre el propio objeto.

### Solución 3.4 — Deadlock provocado y solución
**Versión con deadlock:**
```java
public class DeadlockDemo {
    private static final Object recursoA = new Object();
    private static final Object recursoB = new Object();

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            synchronized (recursoA) {
                System.out.println("Hilo 1 bloquea A");
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                synchronized (recursoB) {
                    System.out.println("Hilo 1 bloquea B");
                }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (recursoB) {
                System.out.println("Hilo 2 bloquea B");
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                synchronized (recursoA) {
                    System.out.println("Hilo 2 bloquea A");
                }
            }
        });

        t1.start();
        t2.start();
    }
}
```

**Versión corregida (orden global de adquisición A → B en ambos hilos):**
```java
public class DeadlockSolucionado {
    private static final Object recursoA = new Object();
    private static final Object recursoB = new Object();

    public static void main(String[] args) {
        Runnable tarea1 = () -> {
            synchronized (recursoA) {
                System.out.println(Thread.currentThread().getName() + " bloquea A");
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                synchronized (recursoB) {
                    System.out.println(Thread.currentThread().getName() + " bloquea B");
                }
            }
        };

        // Ambos hilos adquieren SIEMPRE en el mismo orden: A antes que B
        Thread t1 = new Thread(tarea1, "Hilo-1");
        Thread t2 = new Thread(tarea1, "Hilo-2");
        t1.start();
        t2.start();
    }
}
```

---

## Bloque 4 — `wait()` / `notify()`

### Solución 4.1 — Productor-consumidor manual (capacidad 1)
```java
public class BufferUnitario {
    private Integer valor = null;

    public synchronized void producir(int v) throws InterruptedException {
        while (valor != null) {
            wait();
        }
        valor = v;
        System.out.println("Producido: " + v);
        notifyAll();
    }

    public synchronized int consumir() throws InterruptedException {
        while (valor == null) {
            wait();
        }
        int v = valor;
        valor = null;
        System.out.println("Consumido: " + v);
        notifyAll();
        return v;
    }

    public static void main(String[] args) {
        BufferUnitario buffer = new BufferUnitario();

        Thread productor = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                try {
                    buffer.producir(i);
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {}
            }
        });

        Thread consumidor = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                try {
                    buffer.consumir();
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {}
            }
        });

        productor.start();
        consumidor.start();
    }
}
```

### Solución 4.2 — Buffer con capacidad N
```java
import java.util.LinkedList;
import java.util.Queue;

public class BufferN {
    private final Queue<Integer> cola = new LinkedList<>();
    private final int capacidad;

    public BufferN(int capacidad) {
        this.capacidad = capacidad;
    }

    public synchronized void producir(int v) throws InterruptedException {
        while (cola.size() == capacidad) {
            wait();
        }
        cola.add(v);
        System.out.println(Thread.currentThread().getName() + " produce " + v);
        notifyAll();
    }

    public synchronized int consumir() throws InterruptedException {
        while (cola.isEmpty()) {
            wait();
        }
        int v = cola.poll();
        System.out.println(Thread.currentThread().getName() + " consume " + v);
        notifyAll();
        return v;
    }

    public static void main(String[] args) {
        BufferN buffer = new BufferN(5);

        Runnable tareaProductor = () -> {
            for (int i = 0; i < 10; i++) {
                try {
                    buffer.producir(i);
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }
        };

        Runnable tareaConsumidor = () -> {
            for (int i = 0; i < 5; i++) {
                try {
                    buffer.consumir();
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {}
            }
        };

        new Thread(tareaProductor, "Productor-1").start();
        new Thread(tareaProductor, "Productor-2").start();
        new Thread(tareaConsumidor, "Consumidor-1").start();
        new Thread(tareaConsumidor, "Consumidor-2").start();
    }
}
```

### Solución 4.3 — Análisis crítico (`if` en lugar de `while`)
```java
// Cambio propuesto en producir()/consumir():
public synchronized void producir(int v) throws InterruptedException {
    if (valor != null) {   // <-- BUG: debería ser while
        wait();
    }
    valor = v;
    notifyAll();
}
```
**Explicación esperada:** con `notifyAll()` pueden despertarse varios hilos en espera simultáneamente. Si solo se comprueba la condición una vez (`if`) antes de esperar, un hilo despertado puede continuar sin volver a comprobar si la condición sigue siendo válida (por ejemplo, otro hilo "más rápido" ya volvió a llenar el buffer). Esto provoca sobrescritura de datos o consumos duplicados. El `while` obliga a re-evaluar la condición cada vez que el hilo se despierta, protegiendo frente a **despertares espurios** y condiciones de carrera entre varios hilos en espera.

---

## Bloque 5 — Locks, semáforos y atómicos

### Solución 5.1 — `ReentrantLock`
```java
import java.util.concurrent.locks.ReentrantLock;

public class ContadorLock {
    private int contador = 0;
    private final ReentrantLock lock = new ReentrantLock();

    public void incrementar() {
        lock.lock();
        try {
            contador++;
        } finally {
            lock.unlock();
        }
    }

    public int getContador() {
        lock.lock();
        try {
            return contador;
        } finally {
            lock.unlock();
        }
    }
}
```

### Solución 5.2 — `AtomicInteger`
```java
import java.util.concurrent.atomic.AtomicInteger;

public class ContadorAtomico {
    private final AtomicInteger contador = new AtomicInteger(0);

    public void incrementar() {
        contador.incrementAndGet();
    }

    public int getContador() {
        return contador.get();
    }
}
```
**Comparativa:** la versión atómica es la más simple y no requiere gestionar bloqueos explícitos; internamente usa instrucciones CAS (*compare-and-swap*) de la CPU. Es ideal para contadores simples, pero no sustituye a `synchronized`/`Lock` cuando hay que proteger varias variables o invariantes conjuntas.

### Solución 5.3 — El aparcamiento
```java
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Aparcamiento {
    public static void main(String[] args) {
        Semaphore plazas = new Semaphore(3, true);
        Random random = new Random();

        for (int i = 1; i <= 10; i++) {
            int coche = i;
            new Thread(() -> {
                try {
                    System.out.println("Coche " + coche + " esperando plaza...");
                    plazas.acquire();
                    System.out.println("Coche " + coche + " ha aparcado. Plazas libres: "
                            + plazas.availablePermits());

                    Thread.sleep(500 + random.nextInt(1500));

                    System.out.println("Coche " + coche + " se va.");
                    plazas.release();
                    System.out.println("Plazas libres tras salir coche " + coche + ": "
                            + plazas.availablePermits());
                } catch (InterruptedException ignored) {}
            }).start();

            try { Thread.sleep(random.nextInt(300)); } catch (InterruptedException ignored) {}
        }
    }
}
```

### Solución 5.4 — `tryLock` con tiempo de espera
```java
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class DeadlockConTryLock {
    private static final ReentrantLock lockA = new ReentrantLock();
    private static final ReentrantLock lockB = new ReentrantLock();

    static void tarea(ReentrantLock primero, ReentrantLock segundo, String nombre) {
        while (true) {
            try {
                if (primero.tryLock(200, TimeUnit.MILLISECONDS)) {
                    try {
                        Thread.sleep(50);
                        if (segundo.tryLock(200, TimeUnit.MILLISECONDS)) {
                            try {
                                System.out.println(nombre + " consiguió ambos locks");
                                return;
                            } finally {
                                segundo.unlock();
                            }
                        } else {
                            System.out.println(nombre + " no consiguió el segundo lock, reintentando...");
                        }
                    } finally {
                        primero.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public static void main(String[] args) {
        new Thread(() -> tarea(lockA, lockB, "Hilo-1")).start();
        new Thread(() -> tarea(lockB, lockA, "Hilo-2")).start();
    }
}
```

---

## Bloque 6 — Colecciones concurrentes

### Solución 6.1 — Productor-consumidor con `BlockingQueue`
```java
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProductorConsumidorBlockingQueue {
    public static void main(String[] args) {
        BlockingQueue<Integer> cola = new ArrayBlockingQueue<>(5);

        Runnable productor = () -> {
            for (int i = 0; i < 10; i++) {
                try {
                    cola.put(i); // se bloquea automáticamente si está llena
                    System.out.println(Thread.currentThread().getName() + " produce " + i);
                } catch (InterruptedException ignored) {}
            }
        };

        Runnable consumidor = () -> {
            for (int i = 0; i < 5; i++) {
                try {
                    int v = cola.take(); // se bloquea automáticamente si está vacía
                    System.out.println(Thread.currentThread().getName() + " consume " + v);
                } catch (InterruptedException ignored) {}
            }
        };

        new Thread(productor, "Productor-1").start();
        new Thread(productor, "Productor-2").start();
        new Thread(consumidor, "Consumidor-1").start();
        new Thread(consumidor, "Consumidor-2").start();
    }
}
```
**Comparativa:** misma funcionalidad que el ejercicio 4.2 pero sin gestionar manualmente `wait()`/`notify()`/condición de buffer lleno-vacío: `BlockingQueue` ya lo resuelve internamente.

### Solución 6.2 — Caché compartida
```java
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class CacheCompartida {
    public static void main(String[] args) throws InterruptedException {
        Map<String, Integer> cache = new ConcurrentHashMap<>();

        Runnable tarea = () -> {
            for (int i = 0; i < 1000; i++) {
                String clave = "clave" + (i % 10);
                cache.merge(clave, 1, Integer::sum);
            }
        };

        Thread[] hilos = new Thread[5];
        for (int i = 0; i < 5; i++) hilos[i] = new Thread(tarea);
        for (Thread t : hilos) t.start();
        for (Thread t : hilos) t.join();

        cache.forEach((k, v) -> System.out.println(k + " = " + v));
    }
}
```
**Nota:** si se sustituye `ConcurrentHashMap` por `HashMap`, el programa puede lanzar `ConcurrentModificationException` o corromper su estructura interna al modificarse desde varios hilos a la vez.

---

## Bloque 7 — `ExecutorService`

### Solución 7.1 — Pool fijo de hilos
```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Random;

public class PoolPedidos {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        Random random = new Random();

        for (int i = 1; i <= 20; i++) {
            int pedido = i;
            executor.submit(() -> {
                System.out.println("Procesando pedido " + pedido
                        + " en " + Thread.currentThread().getName());
                try {
                    Thread.sleep(200 + random.nextInt(500));
                } catch (InterruptedException ignored) {}
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        System.out.println("Todos los pedidos procesados.");
    }
}
```

### Solución 7.2 — `Callable` y `Future`
```java
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FactorialesFuture {
    static BigInteger factorial(int n) {
        BigInteger resultado = BigInteger.ONE;
        for (int i = 2; i <= n; i++) resultado = resultado.multiply(BigInteger.valueOf(i));
        return resultado;
    }

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<BigInteger>> resultados = new ArrayList<>();

        int[] numeros = {5, 10, 15, 20, 25};
        for (int n : numeros) {
            Future<BigInteger> future = executor.submit(() -> factorial(n));
            resultados.add(future);
        }

        for (int i = 0; i < resultados.size(); i++) {
            System.out.println(numeros[i] + "! = " + resultados.get(i).get());
        }

        executor.shutdown();
    }
}
```

### Solución 7.3 — Comparativa de pools
```java
import java.util.concurrent.*;

public class ComparativaPools {
    static void ejecutarPrueba(ExecutorService executor, String nombre) throws InterruptedException {
        long inicio = System.nanoTime();

        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        long fin = System.nanoTime();
        System.out.println(nombre + " -> " + (fin - inicio) / 1_000_000 + " ms");
    }

    public static void main(String[] args) throws InterruptedException {
        ejecutarPrueba(Executors.newFixedThreadPool(4), "FixedThreadPool(4)");
        ejecutarPrueba(Executors.newCachedThreadPool(), "CachedThreadPool");
    }
}
```
**Cuándo usar cada uno:** `newFixedThreadPool` es preferible cuando se quiere limitar el uso de recursos (por ejemplo, CPU o memoria) con una carga sostenida; `newCachedThreadPool` es útil para ráfagas de tareas cortas y numerosas, ya que crea y reutiliza hilos dinámicamente, pero puede crear demasiados hilos si la carga es muy alta y sostenida.

---

## Bloque 8 — Sincronizadores de alto nivel

### Solución 8.1 — Arranque simultáneo de una carrera
```java
import java.util.Random;
import java.util.concurrent.CyclicBarrier;

public class CarreraBarrier {
    public static void main(String[] args) {
        int numCorredores = 5;
        Random random = new Random();

        CyclicBarrier barrera = new CyclicBarrier(numCorredores,
                () -> System.out.println("¡Todos listos! Empieza la carrera."));

        for (int i = 1; i <= numCorredores; i++) {
            int corredor = i;
            new Thread(() -> {
                System.out.println("Corredor " + corredor + " en la línea de salida");
                try {
                    barrera.await(); // espera a que todos lleguen aquí
                    int tiempo = 1000 + random.nextInt(2000);
                    Thread.sleep(tiempo);
                    System.out.println("Corredor " + corredor + " cruza la meta en " + tiempo + " ms");
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
}
```

### Solución 8.2 — `CountDownLatch`
```java
import java.util.concurrent.CountDownLatch;

public class CargaDatosLatch {
    public static void main(String[] args) throws InterruptedException {
        int numTrabajadores = 5;
        CountDownLatch latch = new CountDownLatch(numTrabajadores);

        for (int i = 1; i <= numTrabajadores; i++) {
            int id = i;
            new Thread(() -> {
                System.out.println("Trabajador " + id + " cargando datos...");
                try {
                    Thread.sleep(500 + (long) (Math.random() * 1500));
                } catch (InterruptedException ignored) {}
                System.out.println("Trabajador " + id + " ha terminado");
                latch.countDown();
            }).start();
        }

        latch.await(); // el main espera a que el contador llegue a 0
        System.out.println("Sistema listo");
    }
}
```

---

## Proyecto final integrador — Solución de referencia

```java
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;

public class PizzeriaSistema {

    record Pedido(int id, String cliente) {}

    public static void main(String[] args) throws InterruptedException {
        final int MAX_PEDIDOS = 20;
        final int NUM_HORNOS = 2;
        final int NUM_COCINEROS = 4;

        BlockingQueue<Pedido> cola = new LinkedBlockingQueue<>();
        Semaphore hornos = new Semaphore(NUM_HORNOS, true);
        AtomicInteger pedidosCompletados = new AtomicInteger(0);
        Random random = new Random();

        // Hilos Cliente: generan pedidos
        ExecutorService clientes = Executors.newFixedThreadPool(3);
        for (int i = 1; i <= MAX_PEDIDOS; i++) {
            int idPedido = i;
            clientes.submit(() -> {
                try {
                    Thread.sleep(random.nextInt(300));
                    Pedido pedido = new Pedido(idPedido, "Cliente-" + idPedido);
                    cola.put(pedido);
                    System.out.println("Nuevo pedido recibido: " + pedido);
                } catch (InterruptedException ignored) {}
            });
        }

        // Pool de Cocineros: consumen pedidos de la cola
        ExecutorService cocineros = Executors.newFixedThreadPool(NUM_COCINEROS);
        for (int i = 0; i < NUM_COCINEROS; i++) {
            cocineros.submit(() -> {
                while (pedidosCompletados.get() < MAX_PEDIDOS) {
                    try {
                        Pedido pedido = cola.poll(500, TimeUnit.MILLISECONDS);
                        if (pedido == null) continue;

                        hornos.acquire(); // esperar horno libre
                        try {
                            System.out.println(Thread.currentThread().getName()
                                    + " cocinando pedido " + pedido.id());
                            Thread.sleep(300 + random.nextInt(700));
                        } finally {
                            hornos.release();
                        }

                        int completados = pedidosCompletados.incrementAndGet();
                        System.out.println("Pedido " + pedido.id() + " completado ("
                                + completados + "/" + MAX_PEDIDOS + ")");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }

        clientes.shutdown();
        clientes.awaitTermination(1, TimeUnit.MINUTES);

        // Esperar a que se completen todos los pedidos
        while (pedidosCompletados.get() < MAX_PEDIDOS) {
            Thread.sleep(200);
        }

        cocineros.shutdown();
        cocineros.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println("\n--- RESUMEN FINAL ---");
        System.out.println("Pedidos completados: " + pedidosCompletados.get() + "/" + MAX_PEDIDOS);
    }
}
```

**Decisiones de diseño a comentar con el alumnado:**
- `BlockingQueue` evita tener que sincronizar manualmente el acceso a la cola de pedidos.
- `Semaphore(2)` modela el número limitado de hornos, evitando que más de 2 pedidos se cocinen simultáneamente.
- `AtomicInteger` permite contar pedidos completados desde varios hilos cocineros sin necesidad de `synchronized`.
- El cierre ordenado se hace comprobando una condición de parada (`pedidosCompletados.get() < MAX_PEDIDOS`) en lugar de usar `shutdownNow()` de forma abrupta, evitando dejar pedidos a medio cocinar.
- No hay riesgo de deadlock porque solo se usa un recurso de bloqueo compartido (el semáforo de hornos) y no hay adquisición anidada de varios locks.
