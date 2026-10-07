# Soluciones - Tareas Asíncronas en Java (Future / CompletableFuture)

Soluciones de todas las prácticas del fichero `ejerciciosTareasAsyncronas.md`:
Future básico, isDone, get(), CompletableFuture, thenApply/thenAccept,
thenCompose, thenCombine, allOf, exceptionally, handle, orTimeout,
completeOnTimeout, executores personalizados, procesamiento paralelo,
contadores atómicos, la práctica final y el reto final.

## Requisitos

- JDK 17 o superior (todo el código es compatible; en JDK 25 funciona igual)

## Compilar y ejecutar

Desde esta carpeta:

    # compilar todo
    javac *.java

    # o usar el script incluido (Linux/macOS)
    ./compilar-y-ejecutar.sh <NombreClaseSinPuntoJava>

    # ejemplo
    ./compilar-y-ejecutar.sh RetoFinal_ProcesoDeCompra

## Relación práctica -> fichero

| Práctica | Fichero |
|---|---|
| 1. Primer Future | Practica01_PrimerFuture.java |
| 2. Comprobar si ha terminado (isDone) | Practica02_IsDone.java |
| 3. Varias tareas | Practica03_VariasTareas.java |
| 4. El problema de get() | Practica04_ProblemaDeGet.java |
| 5. CompletableFuture básico | Practica05_CompletableFutureBasico.java |
| 6. Cadena de operaciones | Practica06_CadenaDeOperaciones.java |
| 7. thenCompose | Practica07_ThenCompose.java |
| 8. Dos tareas independientes (thenCombine) | Practica08_ThenCombine.java |
| 9. allOf | Practica09_AllOf.java |
| 10. Errores (exceptionally) | Practica10_Exceptionally.java |
| 11. handle | Practica11_Handle.java |
| 12. Timeout (orTimeout / completeOnTimeout) | Practica12_Timeout.java |
| 13. Executor personalizado | Practica13_ExecutorPersonalizado.java |
| 14. Procesamiento paralelo | Practica14_ProcesamientoParalelo.java |
| 15. Contador concurrente | Practica15_ContadorConcurrente.java |
| FINAL. Sistema de consultas | PracticaFinal_SistemaDeConsultas.java |
| Reto final. Proceso de compra | RetoFinal_ProcesoDeCompra.java |

## Notas

- Los ficheros están comentados: explican las preguntas planteadas
  (por qué tarda ~3 s y no 6, por qué no hacer join()+join(), etc.).
- Practica15 demuestra que CompletableFuture NO evita condiciones de carrera:
  `int` da un resultado incorrecto; `AtomicInteger` es exacto.
- PracticaFinal incluye la ampliación: si el servicio de estadísticas falla,
  el programa continúa con "Estadísticas: NO DISPONIBLES".
