# Tareas asíncronas en Java

## Material de clase — DAM

> **Prerequisitos:** se supone que ya conocemos `Thread`, `Runnable`, creación de hilos, sincronización, `synchronized`, `CountDownLatch` y los problemas básicos de concurrencia.

---

# 1. ¿Qué significa ejecutar una tarea de forma asíncrona?

Hasta ahora podemos crear un hilo y hacer que ejecute una tarea:

```java
Thread hilo = new Thread(() -> {
    System.out.println("Ejecutando tarea");
});

hilo.start();
```

La dificultad aparece cuando queremos saber:

- ¿Cuándo ha terminado?
- ¿Qué resultado ha producido?
- ¿Ha ocurrido una excepción?
- ¿Podemos cancelar la tarea?
- ¿Podemos ejecutar varias tareas y después combinar sus resultados?
- ¿Podemos hacer una segunda tarea cuando termine la primera?

Para solucionar estos problemas Java proporciona diferentes abstracciones.

La evolución que estudiaremos será:

```text
ExecutorService
      ↓
    Future
      ↓
CompletableFuture
      ↓
 Spring @Async
```

---

# 2. `Callable` y `ExecutorService`

Antes de estudiar `Future` necesitamos conocer `Callable`.

`Runnable` ejecuta una tarea pero no devuelve un resultado:

```java
Runnable tarea = () -> {
    System.out.println("Calculando...");
};
```

`Callable<T>` permite devolver un resultado:

```java
Callable<Integer> tarea = () -> {
    return 10 + 20;
};
```

Además, un `Callable` puede lanzar excepciones comprobadas.

## ExecutorService

En lugar de crear directamente un `Thread`, podemos utilizar un `ExecutorService`.

```java
ExecutorService executor = Executors.newFixedThreadPool(3);

executor.submit(() -> {
    System.out.println("Tarea ejecutándose");
});

executor.shutdown();
```

El `ExecutorService` administra un conjunto de hilos que pueden reutilizarse.

Esto evita tener que crear manualmente un hilo para cada tarea.

---

# 3. `Future`

## 3.1. ¿Qué es un Future?

Un `Future<T>` representa **el resultado futuro de una tarea**.

Podemos pensar en él como un recibo:

```text
submit()
   │
   ▼
Future<Integer>
   │
   │ el resultado todavía no está disponible
   ▼
tarea ejecutándose
   │
   ▼
resultado = 42
```

Ejemplo:

```java
ExecutorService executor = Executors.newFixedThreadPool(2);

Future<Integer> future = executor.submit(() -> {
    Thread.sleep(2000);
    return 42;
});

System.out.println("La tarea se ha lanzado");

Integer resultado = future.get();

System.out.println("Resultado: " + resultado);

executor.shutdown();
```

Mientras la tarea está ejecutándose, `future` representa el resultado que obtendremos posteriormente.

---

# 4. El problema de `get()`

Esta línea:

```java
Integer resultado = future.get();
```

es importante.

`get()` **bloquea el hilo que lo llama** hasta que el resultado está disponible.

Por ejemplo:

```java
Future<Integer> future = executor.submit(() -> {
    Thread.sleep(5000);
    return 100;
});

System.out.println("Antes del get");

Integer resultado = future.get();

System.out.println("Después del get");
```

El hilo que ejecuta `main` se quedará esperando aproximadamente 5 segundos.

Por tanto:

> Una tarea puede ser asíncrona aunque posteriormente esperemos su resultado de forma bloqueante.

Esto es una idea fundamental.

---

# 5. Métodos principales de `Future`

## `get()`

Espera hasta obtener el resultado.

```java
Integer resultado = future.get();
```

Puede lanzar:

```java
InterruptedException
ExecutionException
```

---

## `get(timeout)`

Podemos establecer un tiempo máximo de espera:

```java
Integer resultado = future.get(2, TimeUnit.SECONDS);
```

Puede producir:

```java
TimeoutException
```

---

## `isDone()`

Permite comprobar si la tarea ha terminado.

```java
if (future.isDone()) {
    System.out.println("Terminada");
}
```

---

## `cancel()`

Intenta cancelar la tarea:

```java
future.cancel(true);
```

El parámetro indica si se permite interrumpir el hilo que está ejecutando la tarea.

---

## `isCancelled()`

```java
if (future.isCancelled()) {
    System.out.println("La tarea fue cancelada");
}
```

---

# 6. `Future` y `Callable`

Una combinación habitual es:

```java
ExecutorService executor = Executors.newFixedThreadPool(3);

Callable<Integer> callable = () -> {
    Thread.sleep(1000);
    return 50;
};

Future<Integer> future = executor.submit(callable);

System.out.println("Tarea enviada");

Integer resultado = future.get();

System.out.println("Resultado: " + resultado);

executor.shutdown();
```

Aquí tenemos:

```text
Callable
   ↓
ExecutorService.submit()
   ↓
Future<Integer>
   ↓
get()
   ↓
Integer
```

---

# 7. Varias tareas con `Future`

Podemos lanzar varias tareas:

```java
ExecutorService executor = Executors.newFixedThreadPool(3);

Future<Integer> f1 = executor.submit(() -> {
    Thread.sleep(2000);
    return 10;
});

Future<Integer> f2 = executor.submit(() -> {
    Thread.sleep(1000);
    return 20;
});

Future<Integer> f3 = executor.submit(() -> {
    Thread.sleep(3000);
    return 30;
});
```

Después podemos obtener los resultados:

```java
int resultado1 = f1.get();
int resultado2 = f2.get();
int resultado3 = f3.get();

System.out.println(resultado1 + resultado2 + resultado3);
```

Las tres tareas se pueden ejecutar en paralelo.

Pero aparece un problema:

```java
f1.get();
f2.get();
f3.get();
```

El orden en el que llamamos a `get()` puede hacer que esperemos innecesariamente.

Por ejemplo, aunque `f2` termine al segundo segundo, si hacemos primero:

```java
f1.get();
```

esperaremos a `f1` aunque `f2` ya haya terminado.

---

# 8. `Future`: ventajas y limitaciones

## Ventajas

`Future` permite:

- representar resultados futuros;
- esperar resultados;
- consultar si una tarea terminó;
- cancelar tareas;
- establecer tiempos máximos de espera.

## Limitaciones

El código se complica cuando queremos expresar:

```text
A termina
   ↓
B utiliza el resultado de A
   ↓
C utiliza el resultado de B
```

Con `Future` solemos acabar haciendo:

```java
ResultadoA a = futureA.get();

Future<ResultadoB> futureB =
        executor.submit(() -> procesar(a));

ResultadoB b = futureB.get();

Future<ResultadoC> futureC =
        executor.submit(() -> procesar(b));

ResultadoC c = futureC.get();
```

Esto funciona, pero estamos utilizando `get()` continuamente.

Aquí aparece `CompletableFuture`.

---

# 9. `CompletableFuture`

`CompletableFuture` permite representar un resultado futuro y, además, **componer acciones que se ejecutarán cuando ese resultado esté disponible**.

Por ejemplo:

```java
CompletableFuture
        .supplyAsync(() -> obtenerUsuario())
        .thenApply(usuario -> obtenerNombre(usuario))
        .thenAccept(nombre -> System.out.println(nombre));
```

Podemos leerlo como:

```text
ejecutar obtenerUsuario()
          ↓
cuando termine:
obtenerNombre(usuario)
          ↓
cuando termine:
mostrar nombre
```

Sin hacer un `get()` entre cada paso.

---

# 10. `runAsync()`

Se utiliza cuando la tarea **no devuelve resultado**.

```java
CompletableFuture<Void> future =
        CompletableFuture.runAsync(() -> {
            System.out.println("Ejecutando tarea");
        });
```

Conceptualmente:

```text
runAsync()
   ↓
tarea
   ↓
Void
```

Ejemplo:

```java
CompletableFuture<Void> future =
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println("Tarea terminada");
        });

System.out.println("La tarea se ha lanzado");

future.join();

System.out.println("Fin");
```

---

# 11. `supplyAsync()`

Se utiliza cuando queremos devolver un resultado.

```java
CompletableFuture<Integer> future =
        CompletableFuture.supplyAsync(() -> {
            return 10 + 20;
        });
```

Podemos obtener posteriormente el resultado:

```java
Integer resultado = future.join();
```

---

# 12. `join()` frente a `get()`

Los dos pueden esperar el resultado.

```java
future.get();
```

puede lanzar excepciones comprobadas:

```java
InterruptedException
ExecutionException
```

Mientras que:

```java
future.join();
```

utiliza excepciones no comprobadas.

Para simplificar ejemplos con `CompletableFuture`, normalmente utilizaremos:

```java
join()
```

pero hay que recordar:

> `join()` también bloquea si el resultado todavía no está disponible.

---

# 13. `thenApply()`

`thenApply()` transforma el resultado.

Ejemplo:

```java
CompletableFuture<Integer> future =
        CompletableFuture.supplyAsync(() -> 10);

CompletableFuture<Integer> resultado =
        future.thenApply(numero -> numero * 2);
```

Tenemos:

```text
10
 ↓
20
```

También podemos encadenarlo:

```java
CompletableFuture<Integer> resultado =
        CompletableFuture
                .supplyAsync(() -> 10)
                .thenApply(n -> n * 2)
                .thenApply(n -> n + 5);
```

Resultado:

```text
10 → 20 → 25
```

---

# 14. `thenAccept()`

`thenAccept()` utiliza el resultado pero no genera otro resultado.

```java
CompletableFuture
        .supplyAsync(() -> 10)
        .thenAccept(resultado ->
                System.out.println("Resultado: " + resultado));
```

La diferencia:

```text
thenApply
    resultado → nuevo resultado

thenAccept
    resultado → acción
```

---

# 15. `thenRun()`

`thenRun()` ejecuta una acción cuando termina la anterior, pero no recibe su resultado.

```java
CompletableFuture
        .supplyAsync(() -> 10)
        .thenRun(() ->
                System.out.println("La tarea terminó"));
```

No necesitamos el valor `10`.

---

# 16. Resumen de las operaciones básicas

| Método | Recibe resultado | Devuelve resultado |
|---|---:|---:|
| `thenApply` | Sí | Sí |
| `thenAccept` | Sí | No |
| `thenRun` | No | No |

Una regla sencilla:

```text
Quiero transformar → thenApply

Quiero consumir → thenAccept

Solo quiero hacer algo después → thenRun
```

---

# 17. Encadenamiento

Supongamos:

```java
String obtenerUsuario()
String obtenerEmail(String usuario)
void enviarEmail(String email)
```

Podemos escribir:

```java
CompletableFuture
        .supplyAsync(() -> obtenerUsuario())
        .thenApply(usuario -> obtenerEmail(usuario))
        .thenAccept(email -> enviarEmail(email));
```

La cadena es:

```text
obtenerUsuario()
       ↓
obtenerEmail()
       ↓
enviarEmail()
```

Esto es mucho más expresivo que controlar manualmente cada `Future`.

---

# 18. `thenCompose()`

Este método es especialmente importante.

Supongamos que tenemos:

```java
CompletableFuture<Usuario> obtenerUsuarioAsync()
```

y:

```java
CompletableFuture<List<Pedido>> obtenerPedidosAsync(Usuario usuario)
```

Queremos:

```text
Usuario
   ↓
Pedidos
```

Podemos hacer:

```java
CompletableFuture<List<Pedido>> pedidos =
        obtenerUsuarioAsync()
                .thenCompose(usuario ->
                        obtenerPedidosAsync(usuario));
```

¿Por qué no `thenApply()`?

Porque `obtenerPedidosAsync()` ya devuelve un `CompletableFuture`.

Con `thenApply()` tendríamos:

```text
CompletableFuture<CompletableFuture<List<Pedido>>>
```

Con `thenCompose()` obtenemos:

```text
CompletableFuture<List<Pedido>>
```

Por tanto:

> `thenCompose()` sirve para encadenar operaciones asíncronas.

---

# 19. `thenApply` frente a `thenCompose`

### `thenApply`

Si tenemos:

```java
CompletableFuture<Usuario>
```

y una función:

```java
Usuario → String
```

utilizamos:

```java
thenApply()
```

Resultado:

```text
CompletableFuture<String>
```

### `thenCompose`

Si tenemos:

```java
CompletableFuture<Usuario>
```

y una función:

```java
Usuario → CompletableFuture<String>
```

utilizamos:

```java
thenCompose()
```

Resultado:

```text
CompletableFuture<String>
```

---

# 20. `thenCombine()`

Se utiliza cuando tenemos **dos tareas independientes** y queremos combinar sus resultados.

Por ejemplo:

```text
      obtenerUsuario()
             │
             ▼
        Usuario
             │
             │
             ├─────────────┐
             │             │
             ▼             ▼
 obtenerPedidos()     obtenerSaldo()
             │             │
             ▼             ▼
          Pedidos       Saldo
             │             │
             └──────┬──────┘
                    ▼
               Resultado
```

Ejemplo:

```java
CompletableFuture<List<Pedido>> pedidos =
        CompletableFuture.supplyAsync(() -> obtenerPedidos());

CompletableFuture<Double> saldo =
        CompletableFuture.supplyAsync(() -> obtenerSaldo());

CompletableFuture<String> resultado =
        pedidos.thenCombine(
                saldo,
                (listaPedidos, saldoActual) ->
                        "Pedidos: " + listaPedidos.size()
                        + ", saldo: " + saldoActual
        );
```

Las dos tareas son independientes y pueden ejecutarse en paralelo.

---

# 21. `allOf()`

Cuando tenemos muchas tareas independientes podemos utilizar:

```java
CompletableFuture.allOf(...)
```

Ejemplo:

```java
CompletableFuture<Void> f1 =
        CompletableFuture.runAsync(() -> tarea(1));

CompletableFuture<Void> f2 =
        CompletableFuture.runAsync(() -> tarea(2));

CompletableFuture<Void> f3 =
        CompletableFuture.runAsync(() -> tarea(3));

CompletableFuture<Void> todas =
        CompletableFuture.allOf(f1, f2, f3);

todas.join();

System.out.println("Todas terminadas");
```

`allOf()` termina cuando han terminado todos los `CompletableFuture`.

Importante:

```java
allOf()
```

devuelve:

```java
CompletableFuture<Void>
```

No devuelve automáticamente una lista con los resultados.

---

# 22. `allOf()` con resultados

Podemos conservar los `CompletableFuture`:

```java
CompletableFuture<Integer> f1 =
        CompletableFuture.supplyAsync(() -> 10);

CompletableFuture<Integer> f2 =
        CompletableFuture.supplyAsync(() -> 20);

CompletableFuture<Integer> f3 =
        CompletableFuture.supplyAsync(() -> 30);

CompletableFuture.allOf(f1, f2, f3).join();

List<Integer> resultados = List.of(
        f1.join(),
        f2.join(),
        f3.join()
);
```

La idea es:

```text
f1 ─┐
f2 ─┼──→ allOf() → todos terminados
f3 ─┘

f1.join() → resultado 1
f2.join() → resultado 2
f3.join() → resultado 3
```

---

# 23. `anyOf()`

`anyOf()` termina cuando termina cualquiera de las tareas.

```java
CompletableFuture<Object> primero =
        CompletableFuture.anyOf(
                tarea1,
                tarea2,
                tarea3
        );
```

Puede ser útil cuando tenemos varias fuentes y nos interesa obtener la primera respuesta.

---

# 24. Manejo de errores

Las tareas asíncronas también pueden fallar.

```java
CompletableFuture<Integer> future =
        CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Error");
        });
```

El error queda asociado al `CompletableFuture`.

Podemos recuperarlo con `exceptionally()`:

```java
CompletableFuture<Integer> future =
        CompletableFuture
                .supplyAsync(() -> {
                    throw new RuntimeException("Error");
                })
                .exceptionally(error -> {
                    System.out.println(
                            "Ha ocurrido un error: "
                            + error.getMessage()
                    );
                    return 0;
                });
```

---

# 25. `handle()`

`handle()` permite recibir tanto el resultado como la excepción.

```java
CompletableFuture<Integer> future =
        CompletableFuture
                .supplyAsync(() -> 10)
                .handle((resultado, error) -> {

                    if (error != null) {
                        System.out.println("Error");
                        return 0;
                    }

                    return resultado * 2;
                });
```

Podemos pensar:

```text
resultado ─┐
           ├──→ handle() → resultado final
error ─────┘
```

---

# 26. `whenComplete()`

`whenComplete()` sirve para ejecutar una acción cuando termina la tarea, tanto si termina correctamente como si falla.

```java
CompletableFuture<Integer> future =
        CompletableFuture
                .supplyAsync(() -> 10)
                .whenComplete((resultado, error) -> {

                    if (error != null) {
                        System.out.println("Error");
                    } else {
                        System.out.println(
                                "Resultado: " + resultado
                        );
                    }
                });
```

A diferencia de `handle()`, normalmente no se utiliza para transformar el resultado.

---

# 27. Resumen de errores

| Método | Finalidad |
|---|---|
| `exceptionally()` | Recuperarse de un error |
| `handle()` | Procesar resultado o error |
| `whenComplete()` | Ejecutar una acción al terminar |

---

# 28. ¿Dónde se ejecutan las tareas?

Si escribimos:

```java
CompletableFuture.supplyAsync(() -> tarea());
```

Java utilizará un executor común por defecto.

Para aplicaciones reales es importante entender que no siempre queremos dejar que todas las tareas utilicen el executor común.

Podemos proporcionar nuestro propio `Executor`.

```java
ExecutorService executor =
        Executors.newFixedThreadPool(4);

CompletableFuture<Integer> future =
        CompletableFuture.supplyAsync(
                () -> tarea(),
                executor
        );
```

Esto nos permite controlar mejor los recursos.

Al finalizar:

```java
executor.shutdown();
```

---

# 29. ¿Por qué utilizar un executor propio?

Imaginemos que tenemos:

- tareas de CPU;
- tareas de acceso a disco;
- peticiones HTTP;
- tareas que tardan mucho.

No necesariamente queremos que todas compartan el mismo pool.

Podemos crear:

```java
ExecutorService executorIO =
        Executors.newFixedThreadPool(10);
```

y:

```java
ExecutorService executorCPU =
        Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
        );
```

La elección concreta depende del tipo de aplicación.

Para DAM lo importante es comprender:

> `CompletableFuture` no crea mágicamente recursos infinitos. Las tareas necesitan hilos y esos hilos pertenecen a un executor.

---

# 30. Asincronía no significa automáticamente paralelismo

Esto es fundamental.

### Asincronía

Significa que no necesitamos esperar inmediatamente al resultado.

### Concurrencia

Varias tareas pueden estar progresando durante el mismo periodo de tiempo.

### Paralelismo

Varias tareas se ejecutan realmente al mismo tiempo, por ejemplo en diferentes núcleos.

No debemos pensar:

```text
asíncrono = más rápido
```

Una operación de E/S puede beneficiarse mucho de la asincronía.

Pero lanzar 1000 tareas de CPU no implica que tardemos menos.

---

# 31. Evitar convertir código asíncrono en síncrono

Este código:

```java
CompletableFuture<Integer> future =
        CompletableFuture.supplyAsync(() -> calcular());

Integer resultado = future.join();
```

contiene una tarea asíncrona, pero el `join()` hace que esperemos.

El problema es mucho mayor cuando hacemos:

```java
Integer a = tareaA().join();
Integer b = tareaB().join();
Integer c = tareaC().join();
```

Podemos estar serializando innecesariamente las esperas.

Si las tareas son independientes, podemos hacer:

```java
CompletableFuture<Integer> a = tareaA();
CompletableFuture<Integer> b = tareaB();
CompletableFuture<Integer> c = tareaC();

CompletableFuture.allOf(a, b, c).join();
```

---

# 32. Ejemplo completo

Supongamos una aplicación que consulta tres servicios:

```text
Usuario
Pedidos
Saldo
```

Las consultas son independientes:

```java
CompletableFuture<Usuario> usuario =
        CompletableFuture.supplyAsync(
                () -> obtenerUsuario()
        );

CompletableFuture<List<Pedido>> pedidos =
        CompletableFuture.supplyAsync(
                () -> obtenerPedidos()
        );

CompletableFuture<Double> saldo =
        CompletableFuture.supplyAsync(
                () -> obtenerSaldo()
        );
```

Esperamos a las tres:

```java
CompletableFuture.allOf(
        usuario,
        pedidos,
        saldo
).join();
```

Y después:

```java
System.out.println(usuario.join());
System.out.println(pedidos.join());
System.out.println(saldo.join());
```

Conceptualmente:

```text
           ┌─ Usuario
           │
Inicio ────┼─ Pedidos ────→ allOf()
           │
           └─ Saldo
```

---

# 33. Timeouts

Podemos establecer un tiempo máximo:

```java
CompletableFuture<Integer> future =
        CompletableFuture
                .supplyAsync(() -> tareaLenta())
                .orTimeout(3, TimeUnit.SECONDS);
```

Si supera el tiempo:

```text
tarea
  │
  ├── termina antes de 3 s → resultado
  │
  └── supera 3 s → TimeoutException
```

También existe:

```java
completeOnTimeout(
        valorPorDefecto,
        tiempo,
        unidad
);
```

Ejemplo:

```java
CompletableFuture<Integer> future =
        CompletableFuture
                .supplyAsync(() -> tareaLenta())
                .completeOnTimeout(
                        0,
                        3,
                        TimeUnit.SECONDS
                );
```

---

# 34. Cancelación

Podemos intentar cancelar:

```java
future.cancel(true);
```

Es importante entender que cancelar un `Future` no significa necesariamente que podamos detener instantáneamente cualquier código que esté ejecutándose.

La tarea debe responder correctamente a la interrupción cuando corresponda.

---

# 35. Problemas de concurrencia y estado compartido

Las herramientas asíncronas no eliminan los problemas de concurrencia.

Por ejemplo:

```java
int contador = 0;
```

y varias tareas:

```java
contador++;
```

pueden provocar una condición de carrera.

Podemos utilizar, según el caso:

```java
AtomicInteger contador = new AtomicInteger();
```

y:

```java
contador.incrementAndGet();
```

La recomendación general es:

> Cuanto menos estado mutable compartido necesitemos, más sencillo será razonar sobre el programa concurrente.

---

# 36. Spring Boot: `@Async`

Una vez comprendido Java puro podemos ver cómo Spring simplifica parte del trabajo.

Podemos habilitar la ejecución asíncrona:

```java
@EnableAsync
@SpringBootApplication
public class Application {
}
```

Y marcar un método:

```java
@Async
public void procesar() {
    System.out.println("Procesando...");
}
```

Cuando otro componente llama:

```java
servicio.procesar();
```

la ejecución puede realizarse en otro hilo gestionado por Spring.

---

# 37. `@Async` con resultado

Es mejor utilizar `CompletableFuture` cuando queremos representar el resultado:

```java
@Async
public CompletableFuture<String> procesar() {

    String resultado = realizarProceso();

    return CompletableFuture.completedFuture(resultado);
}
```

Después:

```java
CompletableFuture<String> future =
        servicio.procesar();
```

Podemos continuar:

```java
future.thenAccept(resultado ->
        System.out.println(resultado)
);
```

---

# 38. Configurar el executor de Spring

Podemos definir nuestro propio executor:

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public Executor taskExecutor() {

        ThreadPoolTaskExecutor executor =
                new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("async-");

        executor.initialize();

        return executor;
    }
}
```

Y utilizarlo:

```java
@Async("taskExecutor")
public CompletableFuture<String> procesar() {
    ...
}
```

---

# 39. Un error típico con `@Async`

Esto puede sorprender:

```java
@Service
public class MiServicio {

    public void metodo1() {
        metodo2();
    }

    @Async
    public void metodo2() {
        ...
    }
}
```

No debemos asumir que `metodo2()` será asíncrono.

Spring aplica `@Async` mediante un proxy.

Una llamada desde el propio objeto:

```text
this.metodo2()
```

no pasa por el proxy de Spring.

Por eso, normalmente:

```text
Controller
    ↓
Servicio A
    ↓
Servicio B (@Async)
```

funciona correctamente, mientras que una llamada interna puede no hacerlo.

---

# 40. Reglas prácticas

Al trabajar con tareas asíncronas:

1. No crear hilos sin necesidad.
2. Utilizar `ExecutorService` para gestionar pools.
3. Utilizar `Future` cuando necesitemos representar un resultado futuro sencillo.
4. Utilizar `CompletableFuture` cuando necesitemos composición.
5. Evitar `get()`/`join()` prematuros.
6. No compartir estado mutable si podemos evitarlo.
7. Controlar errores.
8. Establecer timeouts cuando una operación pueda bloquearse indefinidamente.
9. Elegir adecuadamente el executor.
10. Recordar que asincronía no significa automáticamente mayor velocidad.

---

# 41. PRÁCTICAS

## Práctica 1 — Primer `Future`

Crea un programa que:

1. Cree un `ExecutorService` con dos hilos.
2. Envíe una tarea mediante `submit()`.
3. La tarea debe tardar 2 segundos.
4. Debe devolver el número `42`.
5. El programa principal debe mostrar:
   - `"Tarea enviada"`
   - `"Esperando resultado..."`
   - el resultado.

### Objetivo

Comprender:

- `Callable`
- `ExecutorService`
- `Future`
- `get()`

---

## Práctica 2 — Comprobar si ha terminado

Crea una tarea que tarde 5 segundos.

Mientras no haya terminado, el programa deberá mostrar:

```text
Esperando...
```

Utiliza:

```java
future.isDone()
```

No utilices `get()` inmediatamente.

### Ampliación

Muestra también el nombre del hilo que ejecuta la tarea.

---

## Solución 2

```java
ExecutorService executor =
        Executors.newFixedThreadPool(2);

Future<Integer> future = executor.submit(() -> {

    Thread.sleep(5000);

    return 100;
});

while (!future.isDone()) {
    System.out.println("Esperando...");

    Thread.sleep(500);
}

System.out.println("Resultado: " + future.get());

executor.shutdown();
```

---

# Práctica 3 — Varias tareas

Crea tres tareas:

```text
Tarea 1 → tarda 2 segundos → devuelve 10
Tarea 2 → tarda 1 segundo  → devuelve 20
Tarea 3 → tarda 3 segundos → devuelve 30
```

Utiliza un pool de tres hilos.

Al terminar muestra:

```text
Resultado total: 60
```

Mide el tiempo total.

### Pregunta

¿Por qué el programa tarda aproximadamente 3 segundos y no 6?

---

# Práctica 4 — El problema de `get()`

Modifica la práctica anterior para obtener los resultados haciendo:

```java
f1.get();
f2.get();
f3.get();
```

Después cambia el orden:

```java
f2.get();
f1.get();
f3.get();
```

Comprueba qué ocurre.

### Objetivo

Comprender que las tareas pueden ejecutarse en paralelo aunque esperemos sus resultados en un orden determinado.

---

# Práctica 5 — `CompletableFuture`

Crea:

```java
CompletableFuture<Integer>
```

que calcule:

```text
10 + 20
```

Después utiliza:

```java
thenApply()
```

para multiplicarlo por `2`.

Finalmente utiliza:

```java
thenAccept()
```

para mostrar:

```text
Resultado: 60
```

---

# Práctica 6 — Cadena de operaciones

Crea una cadena:

```text
10
 ↓
multiplicar por 2
 ↓
sumar 5
 ↓
convertir a String
 ↓
mostrar
```

Utiliza:

- `supplyAsync`
- `thenApply`
- `thenAccept`

El resultado debe ser:

```text
Resultado: 25
```

---

## Solución 6

```java
CompletableFuture
        .supplyAsync(() -> 10)
        .thenApply(n -> n * 2)
        .thenApply(n -> n + 5)
        .thenApply(String::valueOf)
        .thenAccept(resultado ->
                System.out.println(
                        "Resultado: " + resultado
                )
        )
        .join();
```

---

# Práctica 7 — `thenCompose`

Implementa:

```java
CompletableFuture<String> obtenerUsuario()
```

que tarde un segundo y devuelva:

```text
Oscar
```

Después implementa:

```java
CompletableFuture<String> obtenerEmail(String usuario)
```

que tarde otro segundo y devuelva:

```text
usuario@example.com
```

Utiliza `thenCompose()` para encadenar ambas operaciones.

No utilices `get()` entre las dos tareas.

---

# Práctica 8 — Dos tareas independientes

Crea dos tareas:

```text
consultarTemperatura() → tarda 2 segundos
consultarHumedad()     → tarda 3 segundos
```

Ambas deben ejecutarse en paralelo.

Cuando las dos terminen muestra:

```text
Temperatura: 25 ºC
Humedad: 60 %
```

Utiliza `thenCombine()`.

### Pregunta

¿Por qué no debemos hacer:

```java
temperatura.join();
humedad.join();
```

antes de empezar a combinar?

---

# Práctica 9 — `allOf`

Crea cinco tareas que representen descargas:

```text
Archivo 1 → 2 segundos
Archivo 2 → 1 segundo
Archivo 3 → 3 segundos
Archivo 4 → 2 segundos
Archivo 5 → 1 segundo
```

Todas deben empezar aproximadamente al mismo tiempo.

Cuando todas hayan terminado:

```text
Todas las descargas han terminado
```

Utiliza:

```java
CompletableFuture.allOf(...)
```

---

# Práctica 10 — Errores

Crea una tarea que lance:

```java
RuntimeException("Error al consultar el servidor")
```

Utiliza:

```java
exceptionally()
```

para devolver:

```text
"DATOS POR DEFECTO"
```

---

# Práctica 11 — `handle`

Crea una función:

```java
CompletableFuture<Integer> dividir(int a, int b)
```

que realice:

```text
a / b
```

Utiliza `handle()` para que:

- si todo va bien → devuelva el resultado;
- si `b == 0` → devuelva `0`.

Prueba:

```java
dividir(10, 2)
dividir(10, 0)
```

---

# Práctica 12 — Timeout

Crea una tarea que tarde 10 segundos.

Utiliza:

```java
orTimeout()
```

para establecer un máximo de 3 segundos.

Controla correctamente el error.

Después modifica el programa utilizando:

```java
completeOnTimeout()
```

para devolver:

```text
"Resultado no disponible"
```

---

# Práctica 13 — Executor personalizado

Crea un executor:

```java
Executors.newFixedThreadPool(3)
```

Utilízalo explícitamente con:

```java
CompletableFuture.supplyAsync(...)
```

Haz cinco tareas y muestra:

```text
Tarea X - hilo: NOMBRE
```

Observa que no necesariamente se crea un hilo nuevo para cada tarea.

---

# Práctica 14 — Procesamiento paralelo

Tienes una lista:

```java
List<Integer> numeros =
        List.of(1, 2, 3, 4, 5);
```

Crea una tarea asíncrona para calcular el cuadrado de cada número.

Después espera a que todas terminen y muestra:

```text
1 → 1
2 → 4
3 → 9
4 → 16
5 → 25
```

### Ampliación

Haz que cada cálculo tarde 1 segundo.

Compara:

```text
ejecución secuencial
```

con:

```text
ejecución concurrente
```

---

# Práctica 15 — Contador concurrente

Crea 10 tareas.

Cada tarea debe incrementar 10.000 veces un contador compartido.

Primero utiliza:

```java
int contador;
```

y observa el resultado.

Después utiliza:

```java
AtomicInteger
```

y compara.

### Objetivo

Recordar que `CompletableFuture` no soluciona automáticamente los problemas de concurrencia.

---

# PRÁCTICA FINAL — Sistema de consultas

Desarrolla un programa que simule una aplicación que necesita obtener información de tres servicios:

```text
Servicio de usuario
Servicio de pedidos
Servicio de estadísticas
```

Cada servicio debe:

- ejecutarse de forma asíncrona;
- tardar entre 1 y 3 segundos;
- devolver un resultado;
- mostrar el nombre del hilo utilizado.

Las tres consultas deben ejecutarse en paralelo.

Cuando todas terminen:

```text
===== RESULTADO =====
Usuario: Oscar
Pedidos: 8
Estadísticas: 1250 visitas
=====================
```

## Requisitos

Debes utilizar:

- `CompletableFuture`
- `supplyAsync`
- un `Executor` propio
- `allOf`
- control de errores
- timeout

### Ampliación

Si el servicio de estadísticas falla, el programa debe continuar utilizando:

```text
Estadísticas: NO DISPONIBLES
```

---

# 42. PRÁCTICAS DE SPRING BOOT

## Práctica 16 — Primer `@Async`

Crea una aplicación Spring Boot con:

```java
@EnableAsync
```

Crea un servicio:

```java
@Async
public void procesar() {
    // tarda 5 segundos
}
```

Desde un controlador llama al método.

El controlador debe devolver inmediatamente una respuesta como:

```text
Proceso iniciado
```

Mientras tanto, el servicio continúa trabajando.

---

# Práctica 17 — `@Async` con `CompletableFuture`

Crea:

```java
@Async
public CompletableFuture<String> obtenerDatos()
```

que tarde 3 segundos y devuelva:

```text
Datos obtenidos
```

Desde otro componente consume el `CompletableFuture`.

### Ampliación

Encadena:

```text
obtenerDatos()
      ↓
procesarDatos()
      ↓
mostrarResultado()
```

utilizando métodos de `CompletableFuture`.

---

# Práctica 18 — Executor de Spring

Configura un:

```java
ThreadPoolTaskExecutor
```

con:

```text
corePoolSize = 3
maxPoolSize = 5
queueCapacity = 20
```

Utiliza ese executor desde:

```java
@Async("taskExecutor")
```

Haz 10 tareas y muestra el nombre del hilo.

Observa cómo se reutilizan los hilos.

---

# Práctica 19 — Detectar el error de `@Async`

Crea:

```java
@Service
public class Servicio {

    public void iniciar() {
        procesar();
    }

    @Async
    public void procesar() {
        ...
    }
}
```

Comprueba si `procesar()` realmente se ejecuta de forma asíncrona.

Después modifica el diseño para que la llamada pase por otro bean Spring.

### Objetivo

Comprender el funcionamiento de los proxies de Spring.

---

# 43. Reto final

Diseña una aplicación que simule un proceso de compra:

```text
               ┌─ comprobar stock
               │
Compra ─────────┼─ calcular precio
               │
               └─ consultar envío
                       │
                       ▼
                 confirmar compra
```

Las tres primeras operaciones son independientes.

Si todas tienen éxito:

```text
Compra confirmada
```

Si alguna falla:

```text
Compra no disponible
```

Debe incluir:

- `CompletableFuture`
- `thenCombine()` o `allOf()`
- manejo de errores
- timeout
- executor personalizado
- al menos una operación `@Async` en Spring Boot

---

# 44. Conceptos que debes saber explicar

Al terminar estas prácticas debes ser capaz de explicar con tus palabras:

### 1. ¿Qué es un `Future`?

Un objeto que representa el resultado futuro de una tarea.

### 2. ¿Qué problema tiene `Future`?

La composición de tareas resulta poco cómoda y normalmente acabamos utilizando `get()`.

### 3. ¿Qué aporta `CompletableFuture`?

Permite construir cadenas y combinaciones de operaciones asíncronas.

### 4. ¿Diferencia entre `runAsync` y `supplyAsync`?

```text
runAsync    → no devuelve resultado
supplyAsync → devuelve resultado
```

### 5. ¿Diferencia entre `thenApply`, `thenAccept` y `thenRun`?

```text
thenApply  → transforma
thenAccept → consume
thenRun    → ejecuta una acción
```

### 6. ¿Cuándo utilizar `thenCompose`?

Cuando la siguiente operación también devuelve un `CompletableFuture`.

### 7. ¿Cuándo utilizar `thenCombine`?

Cuando tenemos dos operaciones independientes y queremos combinar sus resultados.

### 8. ¿Para qué sirve `allOf`?

Para esperar a que terminen varias tareas.

### 9. ¿`join()` es asíncrono?

No. `join()` puede bloquear el hilo que lo llama.

### 10. ¿Asíncrono significa más rápido?

No.

### 11. ¿Qué es un executor?

Un componente que administra la ejecución de tareas y los hilos utilizados para ejecutarlas.

### 12. ¿Qué aporta `@Async`?

Permite delegar la ejecución asíncrona a la infraestructura de Spring.

---

# 45. Chuleta rápida

```java
// Sin resultado
CompletableFuture.runAsync(() -> tarea());

// Con resultado
CompletableFuture.supplyAsync(() -> calcular());

// Transformar
future.thenApply(resultado -> transformar(resultado));

// Consumir
future.thenAccept(resultado -> mostrar(resultado));

// Acción posterior
future.thenRun(() -> finalizar());

// Encadenar tareas asíncronas
future.thenCompose(resultado ->
        otraTareaAsync(resultado));

// Combinar dos resultados
future1.thenCombine(
        future2,
        (a, b) -> combinar(a, b)
);

// Esperar varias tareas
CompletableFuture.allOf(f1, f2, f3);

// Primera tarea que termina
CompletableFuture.anyOf(f1, f2, f3);

// Recuperar error
future.exceptionally(error -> valorPorDefecto());

// Resultado o error
future.handle((resultado, error) -> ...);

// Acción al terminar
future.whenComplete((resultado, error) -> ...);

// Timeout
future.orTimeout(3, TimeUnit.SECONDS);

// Valor por defecto en timeout
future.completeOnTimeout(0, 3, TimeUnit.SECONDS);

// Resultado bloqueante
future.join();
```

---

# 46. Idea final

La programación asíncrona no consiste simplemente en crear más hilos.

El objetivo es poder expresar correctamente:

```text
¿Qué tareas pueden ejecutarse independientemente?
¿Qué tareas dependen de otras?
¿Qué resultados necesitamos?
¿Qué hacemos cuando una tarea falla?
¿Cuánto tiempo estamos dispuestos a esperar?
¿Qué recursos utilizamos para ejecutar las tareas?
```

La evolución que hemos estudiado es:

```text
Thread
  ↓
ExecutorService
  ↓
Future
  ↓
CompletableFuture
  ↓
Composición de tareas
  ↓
Errores / timeout / cancelación
  ↓
Executors personalizados
  ↓
Spring @Async
```

La herramienta no es lo más importante.

Lo importante es **saber identificar la relación entre las tareas y elegir una estrategia adecuada para ejecutarlas**.
