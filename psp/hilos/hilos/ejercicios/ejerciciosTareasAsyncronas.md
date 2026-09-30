
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
