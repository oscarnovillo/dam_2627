package examen;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
## Ejercicio 2. Entender un programa asíncrono defectuoso (1 puntos)

Una aplicación consulta dos servicios independientes: temperatura y humedad.
Se desea que ambas consultas comiencen sin esperar a que termine la otra y que,
cuando las dos hayan acabado, se muestre el resultado.

 */
public class Ejercicio2 {

    static void main() {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        CompletableFuture<String> temperatura =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "25 ºC";
                }, executor);

       // System.out.println("Temperatura: " + temperatura.join());

        CompletableFuture<String> humedad =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "60 %";
                }, executor);

       // System.out.println("Humedad: " + humedad.join());
      // ERROR: no debemos hacer join() a temperatura antes de
        // crear humedad, porque estaríamos esperando innecesariamente.
        // SOLUCIÓN: creamos las dos tareas primero y usamos
        // allOf() para esperar a que ambas terminen.
        CompletableFuture.allOf(temperatura, humedad).join();

        System.out.println("Temperatura: " + temperatura.join());
        System.out.println("Humedad: " + humedad.join());
        executor.shutdown();
    }
}
