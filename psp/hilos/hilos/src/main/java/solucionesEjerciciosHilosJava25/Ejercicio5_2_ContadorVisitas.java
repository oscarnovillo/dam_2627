package solucionesEjerciciosHilosJava25;
// Ejercicio 5.2 - Contador de visitas con AtomicInteger (sin bloqueo)
//
// incrementAndGet() es atomica gracias a operaciones CAS (Compare-And-Swap)
// a nivel hardware: nunca pierde actualizaciones Y no usa cerrojos,
// asi que no hay bloqueos ni cambios de contexto.

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio5_2_ContadorVisitas {

    private static final AtomicInteger visitas = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        try (ExecutorService pool = Executors.newFixedThreadPool(100)) {
            List<Future<?>> futuros = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                futuros.add(pool.submit(() -> visitas.incrementAndGet()));
            }
            for (Future<?> f : futuros) {
                f.get(); // esperar a los 1000 incrementos
            }
        }

        System.out.println("Visitas totales: " + visitas.get() + " (esperado: 1000)");
        System.out.println(visitas.get() == 1000
                ? "OK: AtomicInteger garantiza el resultado exacto."
                : "ERROR: resultado incorrecto");
    }
}
