# Soluciones - Ejercicios de Concurrencia en Java 25

Este paquete contiene las solucionesEjerciciosHilosJava25 de todos los ejercicios del fichero
`ejercicios-hilos-java25.md`.

## Requisitos

- JDK 25 instalado (`java --version` debe mostrar 25.x)

## Compilar y ejecutar

Desde esta carpeta:

    # compilar todo
    javac --release 25 *.java

    # o usar el script incluido (Linux/macOS)
    ./compilar-y-ejecutar.sh <NombreClaseSinPuntoJava>

    # ejemplo
    ./compilar-y-ejecutar.sh RestauranteConcurrente

## Relacion ejercicio -> fichero

| Ejercicio | Fichero |
|---|---|
| 1.1 Descarga simultanea (Thread vs Runnable) | Ejercicio1_1_DescargaSimultanea.java |
| 1.2 Hilos virtuales (10.000 descargas) | Ejercicio1_2_DescargasVirtuales.java |
| 2.1 Cuenta regresiva (join) | Ejercicio2_1_CuentaRegresiva.java |
| 2.2 Cocinero cancelable (interrupt + estados) | Ejercicio2_2_CocineroCancelable.java |
| 3.1 Cuenta bancaria con condicion de carrera | Ejercicio3_1_CuentaBancariaRace.java |
| 3.2 Deadlock entre dos cuentas (+ correccion) | Ejercicio3_2_DeadlockCuentas.java |
| 5.1 Parking con plazas limitadas (Semaphore) | Ejercicio5_1_ParkingSemaphore.java |
| 5.2 Contador de visitas (AtomicInteger) | Ejercicio5_2_ContadorVisitas.java |
| 5.3 Recurso compartido con tryLock | Ejercicio5_3_RecursoTryLock.java |
| 6.1 Productor-consumidor (ArrayBlockingQueue) | Ejercicio6_1_ProductorConsumidorBQ.java |
| 6.2 Contador de palabras concurrente | Ejercicio6_2_ContadorPalabrasConcurrente.java |
| 7.1 Suma paralela de un array grande | Ejercicio7_1_SumaParalela.java |
| 7.2 Consultas simuladas a APIs externas | Ejercicio7_2_LlamadasAPI.java |
| 8.1 Salida de carrera (CountDownLatch) | Ejercicio8_1_SalidaCarrera.java |
| 8.2 Trabajo en fases (CyclicBarrier) | Ejercicio8_2_TrabajoEnFases.java |
| FINAL Restaurante concurrente (integracion) | RestauranteConcurrente.java |

## Notas

- Ejecuta el 3.1 varias veces: la fase sin sincronizar da resultados distintos.
- En el 3.2 la Fase 1 provoca deliberadamente un deadlock; el programa
  usa System.exit(0) para terminar tras demostrarlo (detectalo en caliente
  con `jconsole` o `jcmd <pid> Thread.print`).
- El 7.2 usa `newVirtualThreadPerTaskExecutor()` (pista Java 25).
- Las solucionesEjerciciosHilosJava25 estan comentadas: cada fichero explica la tecnica usada.
