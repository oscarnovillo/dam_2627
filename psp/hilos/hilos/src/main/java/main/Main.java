package main;

import jdk.swing.interop.SwingInterOpUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Main {


    private static final int NUM_HILOS = 100;
    public static int contador = 0;
    public static ReentrantLock lock = new ReentrantLock();
    public static Semaphore semaphore = new Semaphore(1);

    private Main() {
        // Constructor privado para clase utilitaria
    }

    public static void sumar() {
        synchronized (lock) {
            contador++;
        }
    }

    public static void sumar1() {
        synchronized (lock) {
            contador++;
        }
    }

    public static void sumarConLock() {
        Boolean noHecho = true;
        while (noHecho) {
            if (lock.tryLock()) {
                contador++;
                noHecho = false;
                lock.unlock();
            } else {
                System.out.println("suyma perdida");
                System.out.println("estoy haciendo otra cosa");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }


    public static void sumarConSemaphore() {
        Boolean noHecho = true;
        while (noHecho) {
            if (semaphore.tryAcquire()) {
                contador++;
                noHecho = false;
                semaphore.release();
            } else {
                System.out.println("suyma perdida");
                System.out.println("estoy haciendo otra cosa");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    static void main(String[] args) throws InterruptedException {
        System.out.println("=== SUMA CON HILOS TRADICIONALES (Thread.start()) ===");

        // Generar los mismos números que usarán todas las implementaciones
        List<Thread> hilos = new ArrayList<>();
        final AtomicInteger contador1 = new AtomicInteger(0);

        int inicioIndice = 0;
        ExecutorService executor = Executors.newFixedThreadPool(4);
        // Crear y lanzar los hilos
        for (int i = 0; i < NUM_HILOS; i++) {

//            Thread hilo = new Thread(() -> {
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//
//                }
//                Main.sumarConSemaphore();
//
//            });
            executor.execute(() -> {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {

                        }
                        Main.sumarConSemaphore();
            });

            //hilos.add(hilo);

        }

        //hilos.forEach(Thread::start);

//        hilos.forEach(thread -> {
//            try {
//                thread.join();
//            } catch (InterruptedException e) {
//            }
//        });
        executor.shutdown();
        Thread.sleep(4000);
        System.out.println(contador);
        System.out.println(contador1.get());
        //executor.shutdown();
    }
}
