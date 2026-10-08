package solucionesEjerciciosHilosJava25;
// Ejercicio 5.1 - Parking con plazas limitadas (Semaphore)
//
// acquire() toma un permiso (bloquea si no queda ninguno);
// release() lo devuelve. Con 3 permisos, nunca hay mas de 3 coches dentro.

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Ejercicio5_1_ParkingSemaphore {

    public static void main(String[] args) throws InterruptedException {
        final int PLAZAS = 3;
        Semaphore parking = new Semaphore(PLAZAS);
        List<Thread> coches = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            final int id = i;
            Thread coche = Thread.ofPlatform().name("coche-" + id).start(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " llega al parking "
                            + "(plazas libres: " + parking.availablePermits() + ")");
                    parking.acquire(); // espera si no hay plazas
                    System.out.println("  -> " + Thread.currentThread().getName() + " ENTRA "
                            + "(plazas libres: " + parking.availablePermits() + ")");
                    Thread.sleep(1000 + new Random().nextInt(3000)); // esta aparcado
                    parking.release();
                    System.out.println("  <- " + Thread.currentThread().getName() + " SALE "
                            + "(plazas libres: " + parking.availablePermits() + ")");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            coches.add(coche);
        }

        for (Thread c : coches) c.join();
        System.out.println("Todos los coches han salido. Plazas finales: " + parking.availablePermits());
    }
}
