package examen;

import java.util.concurrent.CompletableFuture;

/*
Corregir una cadena de `CompletableFuture` (1.5 Pto)

El objetivo del siguiente programa es calcular `((10 * 2) + 5)`,
convertir el resultado a texto y mostrar `Resultado: 25`.

El código contiene errores:

corrigelos y exploca en un comentario que estaba mal y como lo has arreglado.

 */
public class Ejercicio1 {

    static void main() {
        CompletableFuture<Void> tarea =
                CompletableFuture.supplyAsync(() -> 10)
                        .thenApply(n -> n * 2)
                        .thenApply(n -> n + 5
                                // ERROR 1: n + 5 calcula la suma, pero no
                                // devuelve el resultado.
                                // SOLUCIÓN: añadimos return para que la
                                // siguiente etapa reciba el valor 25.
                                // así o con return implicito
                        )
                        .thenApply(String::valueOf)
                        .thenAccept(resultado ->
                                System.out.println("Resultado: " + resultado)
                        );
        // ERROR 2: la variable tarea no puede ser
        // CompletableFuture<Integer>, porque la última etapa
        // thenAccept no devuelve ningún resultado (es void).
        // SOLUCIÓN: declaramos CompletableFuture<Void>.

        tarea.join();

    }
}
