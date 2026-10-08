# Soluciones - Problemas de Hilos y Sincronización (2º DAM)

Soluciones de los 6 problemas del fichero `problemas.md`, en orden de
dificultad progresiva (básico → proyecto final).

## Requisitos

- **JDK 21 o superior** (se usan hilos virtuales: `Thread.ofVirtual()` y
  `Thread.ofPlatform()`, JEP 444). Con JDK 25 funciona igual.

## Estructura

    problemas/
    ├── Problema1_ContadorVisitas.java          (Básico)
    ├── Problema2_BancoVirtual.java             (Intermedio)
    ├── Problema3_RestauranteConcurrente.java   (Intermedio-Avanzado)
    ├── Problema4_ParkingInteligente.java       (Avanzado)
    ├── Problema5_SistemaDescargas.java         (Avanzado)
    └── redesocial/                             (Proyecto final, paquete propio)
        ├── RedSocial.java          <- main: arranca todo + dashboard
        ├── Usuario.java            <- comportamiento (normal/influencer/bot)
        ├── Post.java               <- likes AtomicLong, comentarios concurrentes
        ├── TipoActividad.java
        ├── BaseDatosSimulada.java  <- pool 20 conexiones + CompletableFuture
        ├── SistemaNotificaciones.java <- cola asincrona BlockingQueue
        ├── MetricasRedSocial.java  <- contadores atomicos + tasas
        ├── RateLimiter.java        <- 10 posts/min + anti-spam
        └── CacheLRU.java           <- 50 posts populares

## Compilar y ejecutar

    # compilar todo (incluido el paquete redesocial)
    javac --release 25 *.java redesocial/*.java

    # o usar el script (compila todo y ejecuta la clase indicada)
    ./compilar-y-ejecutar.sh Problema1_ContadorVisitas
    ./compilar-y-ejecutar.sh redesocial.RedSocial

La Red Social admite duración como argumento (por defecto 60 s):

    ./compilar-y-ejecutar.sh redesocial.RedSocial 30

## Notas por problema

| Problema | Puntos clave que demuestra |
|---|---|
| 1. Contador | 3 implementaciones (carrera / synchronized / AtomicInteger) con tiempo |
| 2. Banco | ReentrantLock vs synchronized (mismo saldo con semilla fija) y volatile fallando con check-then-act |
| 3. Restaurante | Productor-consumidor con 2 colas (clientes→camareros→mesa), CountDownLatch de cierre |
| 4. Parking | 2 semáforos (normales/VIP), cola acotada, barreras serializadas, dashboard programado |
| 5. Descargas | Pool de 5, PriorityBlockingQueue (premium), reintentos, barras de progreso, CompletableFuture por solicitud |
| Final. Red Social | Integra todo: hilos virtuales, BlockingQueue, ConcurrentHashMap, Semaphore, Atomic*, ScheduledExecutor, rate limiting, LRU |

## Avisos

- El problema 3 tarda ~2 min (100 clientes llegando cada 500 ms + cocina).
- El problema 4, fiel al enunciado (barrera de 2 s/entrada), tarda varios
  minutos con 200 coches. Para pruebas rápidas reduce `BARRERA_ENTRADA_MS`.
- El problema 5: las descargas JUEGO (200 s) ralentizan la demo; el
  generador las selecciona solo el 5% de las veces.
- Los problemas 3, 4 y 5 y la Red Social terminan con `System.exit(0)`
  para cerrar los hilos con bucles de servicio (cocineros, notificaciones...).
