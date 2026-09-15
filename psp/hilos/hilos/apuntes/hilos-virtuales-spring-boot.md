# Hilos Virtuales y Concurrencia Moderna en Spring Boot (Java 25)

> Requisitos: JDK 25, Spring Boot 3.2+ (idealmente 3.3/3.4). Algún ejercicio usa `StructuredTaskScope`, que en Java 25 sigue siendo **preview** (JEP 505, 5ª preview) — hay que compilar y ejecutar con `--enable-preview`.

---

## 1. ¿Por qué importan los hilos virtuales?

Antes de Java 21, un servidor Spring Boot tradicional (Tomcat) asignaba **un hilo de plataforma (OS thread) por petición**. Cada hilo de plataforma cuesta memoria (~1 MB de stack) y es un recurso del sistema operativo, así que el pool está limitado (por defecto ~200 hilos en Tomcat). Si una petición hace una llamada de red o a base de datos que tarda 500 ms, ese hilo del pool queda **bloqueado y desperdiciado** durante ese tiempo.

Los **hilos virtuales** (`java.lang.Thread` en modo virtual, Project Loom) son gestionados por la JVM, no por el SO. Son baratísimos (puedes crear millones), y cuando un hilo virtual se bloquea en una operación I/O (una llamada JDBC, HTTP, etc.), la JVM libera el hilo de plataforma subyacente ("carrier thread") para que otro hilo virtual lo use. El resultado: puedes atender miles de peticiones concurrentes con un puñado de hilos de plataforma reales.

**Puntos clave:**
- Los hilos virtuales **no son más rápidos por hilo**; lo que mejoran es la **capacidad de concurrencia** en cargas I/O-bound (llamadas a BD, APIs externas, colas).
- Para tareas **CPU-bound** (cálculos puros) no aportan nada — ahí siguen mandando los hilos de plataforma y el paralelismo real de cores.
- Con hilos virtuales generalmente **ya no necesitas programar reactivo** (WebFlux/Reactor) solo para escalar I/O: puedes seguir escribiendo código síncrono de toda la vida y obtener el mismo beneficio de escalabilidad.

---

## 2. Activar hilos virtuales en Spring Boot

Desde Spring Boot 3.2, es literalmente una línea:

```properties
# application.properties
spring.threads.virtual.enabled=true
```

Con esto:
- Tomcat (o Jetty/Undertow) atiende cada petición HTTP en un hilo virtual.
- `@Async` usa un `Executor` de hilos virtuales si no defines otro explícitamente.
- Los `@Scheduled` también pueden ejecutarse sobre hilos virtuales.

Si prefieres controlarlo manualmente (por ejemplo, para un `TaskExecutor` de `@Async` específico):

```java
@Configuration
public class AsyncConfig {

    @Bean
    public AsyncTaskExecutor applicationTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}
```

---

## 3. Trampas de producción (lo que casi nadie cuenta al principio)

### 3.1 "Pinning" (anclaje del hilo virtual)

Si un hilo virtual ejecuta un bloque `synchronized` y dentro se bloquea en I/O, **no se libera el hilo de plataforma** (queda "pinned" o anclado) — se pierde la ventaja de los hilos virtuales, y si tienes muchos hilos virtuales pinneados a la vez, puedes agotar el pool de carrier threads y bloquear toda la aplicación.

**Solución:** sustituir `synchronized` por `java.util.concurrent.locks.ReentrantLock` en las rutas donde haya I/O bloqueante dentro de una sección crítica. Desde Java 24, el pinning por `synchronized` se redujo bastante (JEP 491 elimina el pinning en muchos casos), pero sigue siendo buena práctica evitarlo si usas librerías o `ThreadLocal` en bloques nativos.

### 3.2 `ThreadLocal` y pools de conexión

Muchos hilos virtuales pueden compartir pocos hilos de plataforma, así que patrones que asumen "un hilo = una conexión reservada todo el rato" (como ciertos usos ingenuos de `ThreadLocal` para cachear recursos pesados) dejan de tener sentido: con millones de hilos virtuales, tendrías millones de copias. Usa `ScopedValue` (JEP 481, ya finalizado en Java 25) como alternativa moderna e inmutable a `ThreadLocal` para pasar contexto (p. ej. el usuario autenticado) a través de llamadas concurrentes.

### 3.3 El tamaño de pool de Tomcat deja de importar (para bien y para mal)

Con `spring.threads.virtual.enabled=true`, la propiedad `server.tomcat.threads.max` deja de limitar la concurrencia real (cada petición tiene su propio hilo virtual barato). El nuevo cuello de botella suele estar en **otro sitio**: el pool de conexiones a la base de datos (HikariCP), un `Semaphore` que hayas puesto tú, o el propio servicio externo al que llamas.

---

## 4. Ejercicios

### Ejercicio 4.1 — Activar hilos virtuales y medir la diferencia

**Enunciado:** Crea un controlador REST con un endpoint `/lento` que simule una llamada a base de datos con `Thread.sleep(300)`. Lánzale 500 peticiones concurrentes (con `curl`, `ab`, `wrk` o un test con hilos) primero con hilos de plataforma normales y luego con `spring.threads.virtual.enabled=true`. Compara el tiempo total y el uso de hilos.

**Requisitos:**
- Configura `server.tomcat.threads.max=20` para forzar que se note la diferencia con pocos hilos de plataforma.
- Imprime `Thread.currentThread()` en el endpoint para comprobar que aparece `VirtualThread[...]` cuando está activado.

---

### Ejercicio 4.2 — Detectar y arreglar el "pinning"

**Enunciado:** Implementa un servicio con un método `synchronized` que, dentro del bloque sincronizado, hace una llamada bloqueante (por ejemplo `Thread.sleep(200)` simulando una consulta a BD). Ejecuta 50 peticiones concurrentes sobre hilos virtuales y observa cómo se degrada el rendimiento (o usa `jcmd <pid> Thread.dump_to_file` para ver hilos virtuales "pinned"). Después, sustituye `synchronized` por `ReentrantLock` y repite la medición.

---

### Ejercicio 4.3 — `@Async` con hilos virtuales

**Enunciado:** Crea un método de servicio anotado con `@Async` que llame a dos "servicios externos" simulados (cada uno con `Thread.sleep` de duración aleatoria) y combine sus resultados. Configura el executor de `@Async` para usar hilos virtuales.

---

### Ejercicio 4.4 — `StructuredTaskScope` para llamadas paralelas con cancelación conjunta

**Enunciado:** Implementa un método que consulte en paralelo el "servicio de usuario" y el "servicio de pedidos" para construir un `PerfilUsuario`. Si cualquiera de las dos llamadas falla, la otra debe cancelarse automáticamente (no esperar innecesariamente). Usa `StructuredTaskScope` con la política de "todo o nada" (fallar si cualquier subtarea falla).

**Nota:** requiere `--enable-preview` en compilación y ejecución (JDK 25, JEP 505).

---

### Ejercicio 4.5 — De `@Async` + `CompletableFuture` a `StructuredTaskScope`

**Enunciado:** Reimplementa el ejercicio 4.3 (que combinaba dos llamadas) pero ahora con `StructuredTaskScope` en vez de `@Async`/`CompletableFuture`, y compara la legibilidad del manejo de errores en ambos enfoques.

---

## Final — Microservicio de "ficha de cliente" (integración completa, con solución)

### Enunciado

Construye un endpoint `GET /clientes/{id}/ficha` en Spring Boot que:

1. Llame **en paralelo** a tres "servicios" simulados: datos personales, historial de pedidos y puntuación de riesgo (cada uno con latencia aleatoria de 100–400 ms).
2. Use **hilos virtuales** de extremo a extremo (controlador + llamadas internas).
3. Si **cualquiera** de las tres llamadas falla, debe devolver un error 502 y **cancelar** las llamadas restantes que sigan en curso (no dejarlas "colgadas").
4. Tenga un **timeout global** de 1 segundo para toda la operación conjunta.
5. Use `ReentrantLock` (no `synchronized`) para proteger una caché en memoria de resultados recientes (`ConcurrentHashMap` + lock para operaciones compuestas de "comprobar y luego insertar").

### Solución completa

```java
// build.gradle / pom.xml: Spring Boot 3.3+, Java 25
// Compilar y ejecutar con --enable-preview por StructuredTaskScope (JEP 505)

package com.ejemplo.ficha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootApplication
public class FichaClienteApplication {
    public static void main(String[] args) {
        SpringApplication.run(FichaClienteApplication.class, args);
    }
}

// ---------- DTOs ----------

record DatosPersonales(String id, String nombre) {}
record HistorialPedidos(String id, int totalPedidos) {}
record PuntuacionRiesgo(String id, double puntuacion) {}

record FichaCliente(
        DatosPersonales datosPersonales,
        HistorialPedidos historialPedidos,
        PuntuacionRiesgo puntuacionRiesgo
) {}

// ---------- "Servicios externos" simulados ----------

class ServiciosExternosSimulados {

    private static final Random random = new Random();

    static DatosPersonales obtenerDatosPersonales(String id) throws InterruptedException {
        Thread.sleep(100 + random.nextInt(300));
        if (random.nextInt(20) == 0) throw new RuntimeException("Fallo simulado en datos personales");
        return new DatosPersonales(id, "Cliente-" + id);
    }

    static HistorialPedidos obtenerHistorialPedidos(String id) throws InterruptedException {
        Thread.sleep(100 + random.nextInt(300));
        return new HistorialPedidos(id, random.nextInt(50));
    }

    static PuntuacionRiesgo obtenerPuntuacionRiesgo(String id) throws InterruptedException {
        Thread.sleep(100 + random.nextInt(300));
        return new PuntuacionRiesgo(id, random.nextDouble());
    }
}

// ---------- Caché protegida con ReentrantLock ----------

class CachePerfilCliente {

    private final ConcurrentHashMap<String, FichaCliente> cache = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    /** Operación compuesta (comprobar-y-si-no-existe-calcular) protegida explícitamente. */
    FichaCliente obtenerOCalcular(String id, Callable<FichaCliente> calculador) throws Exception {
        FichaCliente existente = cache.get(id);
        if (existente != null) {
            return existente;
        }
        lock.lock();
        try {
            // doble comprobación por si otro hilo la calculó mientras esperábamos el lock
            existente = cache.get(id);
            if (existente != null) {
                return existente;
            }
            FichaCliente calculada = calculador.call();
            cache.put(id, calculada);
            return calculada;
        } finally {
            lock.unlock();
        }
    }
}

// ---------- Servicio principal con StructuredTaskScope ----------

@org.springframework.stereotype.Service
class FichaClienteService {

    private final CachePerfilCliente cache = new CachePerfilCliente();

    FichaCliente construirFicha(String id) throws Exception {
        return cache.obtenerOCalcular(id, () -> construirFichaSinCache(id));
    }

    private FichaCliente construirFichaSinCache(String id) throws Exception {
        // StructuredTaskScope.open() (JEP 505, JDK 25): política por defecto = "todo o nada",
        // si una subtarea falla, se cancelan las demás automáticamente.
        try (var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.<Object>allSuccessfulOrThrow())) {

            StructuredTaskScope.Subtask<DatosPersonales> datos =
                    scope.fork(() -> ServiciosExternosSimulados.obtenerDatosPersonales(id));

            StructuredTaskScope.Subtask<HistorialPedidos> historial =
                    scope.fork(() -> ServiciosExternosSimulados.obtenerHistorialPedidos(id));

            StructuredTaskScope.Subtask<PuntuacionRiesgo> riesgo =
                    scope.fork(() -> ServiciosExternosSimulados.obtenerPuntuacionRiesgo(id));

            // join(Duration) aplica el timeout global a las tres subtareas conjuntamente
            scope.joinUntil(java.time.Instant.now().plus(Duration.ofSeconds(1)));

            return new FichaCliente(datos.get(), historial.get(), riesgo.get());

        } catch (TimeoutException e) {
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,
                    "La ficha del cliente " + id + " tardó demasiado en construirse");
        } catch (StructuredTaskScope.FailedException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Fallo al obtener datos del cliente " + id, e.getCause());
        }
    }
}

// ---------- Controlador (se beneficia de hilos virtuales gracias a spring.threads.virtual.enabled=true) ----------

@RestController
@RequestMapping("/clientes")
class FichaClienteController {

    private final FichaClienteService service;

    FichaClienteController(FichaClienteService service) {
        this.service = service;
    }

    @GetMapping("/{id}/ficha")
    FichaCliente ficha(@PathVariable String id) throws Exception {
        return service.construirFicha(id);
    }
}
```

```properties
# application.properties
spring.threads.virtual.enabled=true
server.tomcat.threads.max=50
```

### Cómo compilar y ejecutar (por el preview de `StructuredTaskScope`)

```bash
# Con Maven, en el pom.xml del compiler plugin:
# <compilerArgs><arg>--enable-preview</arg></compilerArgs>
# Y en spring-boot-maven-plugin, jvmArguments incluyendo --enable-preview

mvn clean package
java --enable-preview -jar target/ficha-cliente-0.0.1-SNAPSHOT.jar
```

### Qué demuestra esta solución

| Técnica | Dónde se usa |
|---|---|
| Hilos virtuales de extremo a extremo | `spring.threads.virtual.enabled=true` procesando cada petición HTTP |
| `StructuredTaskScope` + `Joiner.allSuccessfulOrThrow()` | Llamadas paralelas con cancelación conjunta ante fallo |
| `joinUntil(Instant)` | Timeout global sobre el conjunto de subtareas |
| `ReentrantLock` en vez de `synchronized` | Evita "pinning" en una operación compuesta con I/O potencial |
| `ConcurrentHashMap` | Caché segura para lecturas concurrentes rápidas |
| `ResponseStatusException` | Traducción de fallos de concurrencia a códigos HTTP significativos (502/504) |

### Posibles extensiones

- Sustituir el `Joiner.allSuccessfulOrThrow()` por una política personalizada que tolere que el servicio de "puntuación de riesgo" falle y devuelva la ficha igualmente con ese campo a `null`.
- Añadir un `Semaphore` que limite cuántas fichas se pueden calcular simultáneamente (protección de un recurso downstream limitado), independientemente de cuántos hilos virtuales lance Spring.
- Escribir un test de carga (Gatling o JMeter) comparando latencia p99 con hilos de plataforma vs. hilos virtuales para este mismo endpoint.
