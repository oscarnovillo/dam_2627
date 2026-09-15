# Programación de Servicios y Procesos (PSP)
## Unidad: Programación Multihilo en Java — Guía de Clase y Ejercicios

---

## Objetivos de la unidad

Al finalizar esta unidad, el alumnado será capaz de:

- Crear y gestionar hilos en Java mediante `Thread` y `Runnable`.
- Identificar y resolver condiciones de carrera mediante sincronización.
- Implementar comunicación entre hilos con `wait()`/`notify()`.
- Utilizar las herramientas de alto nivel de `java.util.concurrent`.
- Diseñar soluciones concurrentes correctas, evitando deadlocks e interbloqueos.

---

## Bloque 1 — Fundamentos: `Thread` y `Runnable`

### Conceptos
- Proceso vs hilo.
- Dos formas de crear un hilo: heredar de `Thread` vs implementar `Runnable`.
- `start()` vs `run()`.
- Hilo principal (`main`) y su relación con los hilos creados.

### Ejercicio 1.1 — Primer hilo
Crea una clase `ContadorThread` que extienda `Thread` y que imprima los números del 1 al 10 con una pausa de 500 ms entre cada uno. Lánzalo desde el `main` y observa qué ocurre si lanzas dos instancias a la vez.

### Ejercicio 1.2 — El mismo ejercicio con `Runnable`
Reimplementa el ejercicio 1.1 pero implementando la interfaz `Runnable` en lugar de heredar de `Thread`. Compara ambas soluciones: ¿qué ventajas tiene usar `Runnable`?

### Ejercicio 1.3 — El error clásico
Escribe un programa que cree un hilo y llame a `run()` en lugar de `start()`. Observa la salida y explica por escrito, con tus propias palabras, por qué no se ha creado un hilo nuevo.

### Ejercicio 1.4 — Hilos daemon
Crea un hilo daemon que imprima la hora actual cada segundo indefinidamente. Comprueba que el programa termina en cuanto acaba el `main`, aunque el hilo daemon no haya sido detenido explícitamente.

---

## Bloque 2 — Estados y ciclo de vida de un hilo

### Conceptos
- Estados: `NEW`, `RUNNABLE`, `BLOCKED`, `WAITING`, `TIMED_WAITING`, `TERMINATED`.
- `join()`, `sleep()`, `interrupt()`.
- `Thread.getState()`.

### Ejercicio 2.1 — Observador de estados
Crea un hilo que duerma 3 segundos y, desde el `main`, imprime su estado (`getState()`) cada 500 ms mientras el hilo está vivo. Debes ver como mínimo los estados `RUNNABLE`/`TIMED_WAITING` y `TERMINATED`.

### Ejercicio 2.2 — `join()` en acción
Lanza tres hilos que simulan descargas de archivos (con `Thread.sleep` de duración aleatoria). El hilo principal debe esperar a que **todos** terminen (usando `join()`) antes de imprimir "Todas las descargas han finalizado".

### Ejercicio 2.3 — Interrupción cooperativa
Crea un hilo que ejecute un bucle infinito comprobando `Thread.currentThread().isInterrupted()`. Desde el `main`, espera 2 segundos y llama a `interrupt()`. El hilo debe terminar de forma "educada" mostrando un mensaje antes de morir.

---

## Bloque 3 — Condiciones de carrera y `synchronized`

### Conceptos
- Race condition (condición de carrera).
- Sección crítica.
- `synchronized` en métodos y en bloques.
- Monitor intrínseco de un objeto.

### Ejercicio 3.1 — El contador roto
Crea una clase `Contador` con un método `incrementar()` que haga `contador++`. Lanza 10 hilos que llamen 10.000 veces cada uno a `incrementar()`. Comprueba que el resultado final **no** es 100.000. Explica por qué.

### Ejercicio 3.2 — El contador arreglado
Soluciona el ejercicio 3.1 añadiendo `synchronized` al método `incrementar()`. Verifica que ahora el resultado siempre es 100.000.

### Ejercicio 3.3 — Bloques sincronizados
Reescribe el ejercicio 3.2 usando un bloque `synchronized(this)` en lugar de sincronizar el método completo. ¿Cambia el resultado? ¿Qué ventaja tiene sincronizar solo una parte del código?

### Ejercicio 3.4 — Deadlock provocado
Crea dos objetos `recursoA` y `recursoB`. Lanza un hilo que bloquee `recursoA` y luego intente bloquear `recursoB`, y otro hilo que haga lo contrario (bloquear `recursoB` y luego `recursoA`). Ejecuta el programa varias veces hasta que se produzca un deadlock. Después, corrige el código estableciendo un **orden global** de adquisición de bloqueos.

---

## Bloque 4 — Comunicación entre hilos: `wait()` / `notify()`

### Conceptos
- Por qué `wait()`/`notify()` deben ir dentro de `synchronized`.
- Por qué se comprueba la condición con `while` y no con `if`.
- Patrón productor-consumidor.

### Ejercicio 4.1 — Productor-consumidor manual
Implementa un buffer de capacidad 1 (una casilla) compartido entre un hilo Productor y un hilo Consumidor:
- El Productor debe esperar (`wait()`) si el buffer está lleno.
- El Consumidor debe esperar (`wait()`) si el buffer está vacío.
- Usa `notifyAll()` para despertar al hilo correspondiente.

### Ejercicio 4.2 — Buffer con capacidad N
Amplía el ejercicio 4.1 para que el buffer sea una cola de tamaño configurable (por ejemplo, 5 elementos), con varios productores y varios consumidores simultáneos.

### Ejercicio 4.3 — Análisis crítico
Sustituye en tu solución del ejercicio 4.1 el `while` de comprobación de condición por un `if`. Ejecuta el programa con varios hilos productores/consumidores y explica qué falla (o por qué podría fallar de forma intermitente).

---

## Bloque 5 — `java.util.concurrent`: locks, semáforos y atómicos

### Conceptos
- `ReentrantLock` vs `synchronized` (ventajas: `tryLock`, `fairness`, `lockInterruptibly`).
- `Semaphore` (permisos, `acquire()`/`release()`).
- Clases atómicas: `AtomicInteger`, `AtomicLong`.

### Ejercicio 5.1 — `ReentrantLock`
Reimplementa el "contador roto" del ejercicio 3.1 usando `ReentrantLock` en lugar de `synchronized`. Recuerda liberar siempre el lock en un bloque `finally`.

### Ejercicio 5.2 — Contador con `AtomicInteger`
Reimplementa de nuevo el mismo contador, esta vez usando `AtomicInteger` y su método `incrementAndGet()`. Compara el código con las dos versiones anteriores: ¿cuál es más simple?

### Ejercicio 5.3 — El aparcamiento
Simula un aparcamiento con **3 plazas libres** (`Semaphore(3)`) al que llegan 10 coches (hilos) en momentos aleatorios. Cada coche debe:
1. Pedir una plaza (`acquire()`).
2. "Aparcar" (dormir un tiempo aleatorio).
3. Liberar la plaza (`release()`).

Imprime en cada momento cuántas plazas libres quedan.

### Ejercicio 5.4 — `tryLock` con tiempo de espera
Modifica el ejercicio del deadlock (3.4) para que, en lugar de `synchronized`, use `ReentrantLock` con `tryLock(tiempo, unidad)`. Si un hilo no consigue el segundo lock en el tiempo indicado, debe liberar el primero y reintentar más tarde.

---

## Bloque 6 — Colecciones concurrentes: `BlockingQueue`

### Conceptos
- `BlockingQueue` (`put()`/`take()` bloqueantes).
- `ArrayBlockingQueue` vs `LinkedBlockingQueue`.
- `ConcurrentHashMap`.

### Ejercicio 6.1 — Productor-consumidor con `BlockingQueue`
Reescribe el ejercicio 4.2 (productor-consumidor con N productores/consumidores) usando `ArrayBlockingQueue` en lugar de `wait()`/`notify()` manuales. Compara la cantidad de código necesario frente a la solución manual.

### Ejercicio 6.2 — Caché compartida
Implementa una caché compartida (`ConcurrentHashMap<String, Integer>`) a la que 5 hilos acceden simultáneamente para leer y escribir valores. Comprueba que no se producen excepciones de concurrencia (a diferencia de usar un `HashMap` normal, que puedes probar también para ver el error).

---

## Bloque 7 — Pools de hilos: `ExecutorService`

### Conceptos
- `Executors.newFixedThreadPool()`, `newCachedThreadPool()`, `newSingleThreadExecutor()`.
- `Runnable` vs `Callable<T>`.
- `Future<T>` y `get()`.
- Cierre correcto: `shutdown()`, `shutdownNow()`, `awaitTermination()`.

### Ejercicio 7.1 — Pool fijo de hilos
Crea un `ExecutorService` con un pool de 4 hilos y envíale 20 tareas `Runnable` que simulan procesar un pedido (imprimir mensaje + dormir un tiempo aleatorio). Cierra el pool correctamente al finalizar.

### Ejercicio 7.2 — Resultados con `Callable` y `Future`
Crea 5 tareas `Callable<Integer>` que calculen el factorial de un número distinto cada una. Envíalas a un `ExecutorService`, recoge los `Future<Integer>` y muestra los resultados a medida que estén disponibles.

### Ejercicio 7.3 — Comparativa de pools
Ejecuta la misma tarea (por ejemplo, 100 tareas cortas) con `newFixedThreadPool(4)` y con `newCachedThreadPool()`. Mide el tiempo total con `System.nanoTime()` y compara. ¿Cuándo usarías cada uno?

---

## Bloque 8 — Sincronizadores de alto nivel

### Conceptos
- `CountDownLatch`: esperar a que N hilos completen una fase.
- `CyclicBarrier`: sincronizar varios hilos en un punto común, de forma repetible.

### Ejercicio 8.1 — Arranque simultáneo de una carrera
Simula una carrera de 5 corredores (hilos). Cada corredor debe esperar en la línea de salida hasta que **todos** estén listos (`CyclicBarrier`), y solo entonces empezar a "correr" (bucle con `sleep` aleatorio) hasta cruzar la meta.

### Ejercicio 8.2 — Esperar a que terminen varias tareas
Usa un `CountDownLatch` inicializado a 5 para que el hilo principal espere a que 5 hilos trabajadores terminen de "cargar datos" antes de imprimir "Sistema listo".

---

## Proyecto final integrador (evaluable)

**Enunciado:** Sistema de pedidos de una pizzería.

- Varios hilos **Cliente** generan pedidos de forma continua y los depositan en una `BlockingQueue` compartida.
- Un pool de hilos **Cocinero** (`ExecutorService`) toma pedidos de la cola y los prepara (tiempo aleatorio de "cocinado").
- Solo hay un número limitado de **hornos** disponibles simultáneamente (`Semaphore`).
- Un contador global de pedidos completados debe ser seguro frente a condiciones de carrera (`AtomicInteger`).
- El sistema debe poder cerrarse de forma ordenada: cuando se han generado un número máximo de pedidos y todos han sido cocinados, se debe imprimir un resumen final y cerrar el `ExecutorService` correctamente.

**Requisitos que debe demostrar el alumno:**
1. Uso correcto de `Runnable`/`Thread` o `ExecutorService`.
2. Sincronización correcta de recursos compartidos (sin condiciones de carrera).
3. Uso de al menos una estructura de `java.util.concurrent` (`BlockingQueue`, `Semaphore`, `AtomicInteger`).
4. Cierre ordenado de todos los hilos (sin hilos "zombie" ni el programa colgado).
5. Documentación breve explicando las decisiones de diseño tomadas frente a condiciones de carrera y posibles deadlocks.

---

## Tabla resumen de estructuras usadas en cada bloque

| Bloque | Estructura principal | Concepto que refuerza |
|---|---|---|
| 1 | `Thread`, `Runnable` | Creación de hilos |
| 2 | `join()`, `sleep()`, `interrupt()` | Ciclo de vida |
| 3 | `synchronized` | Exclusión mutua, deadlock |
| 4 | `wait()`/`notify()` | Comunicación entre hilos |
| 5 | `ReentrantLock`, `Semaphore`, `AtomicInteger` | Alternativas a `synchronized` |
| 6 | `BlockingQueue`, `ConcurrentHashMap` | Colecciones seguras |
| 7 | `ExecutorService`, `Callable`, `Future` | Pools de hilos |
| 8 | `CountDownLatch`, `CyclicBarrier` | Sincronización de fases |
| Final | Todas las anteriores | Integración completa |

---

## Sugerencia de temporalización (orientativa)

| Sesiones | Contenido |
|---|---|
| 1-2 | Bloque 1 y 2 |
| 3-4 | Bloque 3 |
| 5-6 | Bloque 4 |
| 7-8 | Bloque 5 |
| 9 | Bloque 6 |
| 10-11 | Bloque 7 |
| 12 | Bloque 8 |
| 13-15 | Proyecto final integrador |
