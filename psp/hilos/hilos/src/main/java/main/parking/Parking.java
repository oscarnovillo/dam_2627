package main.parking;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Parking {

    public static final int PLAZAS = 10;
    public static final int NUM_COCHES= 1000;

    private final CountDownLatch latch = new CountDownLatch(NUM_COCHES*2);
    private AtomicInteger contadorCochesAparcados = new AtomicInteger();
    private AtomicInteger contadorCochesSeVan = new AtomicInteger(0);
    private final Random rand = new Random();


    public final Semaphore parking = new Semaphore(PLAZAS);

    public final Semaphore parkingExecutor = new Semaphore(PLAZAS);
    public final ExecutorService executor = Executors.newFixedThreadPool(10);
    public final List<Thread> coches = new ArrayList<>();

    public void runCoche(Semaphore semaphore)
    {
        //aparcar, esperar un poco y salir
        if (semaphore.tryAcquire())
        {

            try {
                Thread.sleep(rand.nextInt(500)+500);
                int i = 0;
                int j=0;
                // lo que tengo que hacer al final.
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            semaphore.release();
            contadorCochesAparcados.incrementAndGet();
        }
        else{
            System.out.println("no hay plazas");
            contadorCochesSeVan.incrementAndGet();
        }
        latch.countDown();
    }

    public void crearCoches()
    {
        for (int i = 0; i < NUM_COCHES; i++)
        {
            coches.add(new Thread(() -> this.runCoche(parking)));
            executor.submit(() -> this.runCoche(parkingExecutor));
        }
        coches.forEach(Thread::start);
    }


    public void esperarFinal()  {

        //con HILOS
        coches.forEach(coche -> {
            try {
                coche.join();
            } catch (InterruptedException e) {
                // Manejar la excepción aquí
                Thread.currentThread().interrupt();

            }
        });

        //con Executor
        executor.shutdown();
//        try {
//            executor.awaitTermination(1, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void debug() {

        System.out.println("Parking");
        System.out.println("ContadorAparcados: " + contadorCochesAparcados.get());
        System.out.println("ContadorVan: " + contadorCochesSeVan.get());
    }
}
