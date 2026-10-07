package solucionesEjerciciosHilosJava25;

// Ejercicio 7.2 - Consultas simuladas a servicios externos
// Callable + invokeAll + hilos virtuales (newVirtualThreadPerTaskExecutor)

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Ejercicio7_2_LlamadasAPI {

    static Callable<String> llamadaApi(String nombre) {
        return () -> {
            int retardo = 1000 + new Random().nextInt(2000); // 1-3 s
            Thread.sleep(retardo);
            return "Respuesta de " + nombre + " en " + retardo + " ms";
        };
    }

    public static void main(String[] args) throws Exception {
        List<Callable<String>> apis = new ArrayList<>(List.of(
                llamadaApi("api-usuarios"),
                llamadaApi("api-pedidos"),
                llamadaApi("api-pagos"),
                llamadaApi("api-inventario"),
                llamadaApi("api-envios")));

        // try-with-resources: ExecutorService es AutoCloseable desde Java 19.
        // Pista Java 25: un hilo virtual por tarea, sin limite de pool.
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            long inicio = System.currentTimeMillis();

            List<Future<String>> resultados = pool.invokeAll(apis); // lanza todos a la vez

            for (Future<String> f : resultados) {
                System.out.println(f.get()); // en orden de la lista
            }

            System.out.println("Todas las APIs respondieron en "
                    + (System.currentTimeMillis() - inicio) + " ms "
                    + "(las llamadas se hicieron EN PARALELO, no sumadas).");
        }
    }
}
