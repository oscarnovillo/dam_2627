package solucionesEjerciciosHilosJava25;
// Ejercicio 5.3 - Recurso compartido con tryLock (ReentrantLock con timeout)
//
// tryLock(500, MS) espera el lock como maximo medio segundo:
// si no lo consigue, devuelve false en vez de bloquearse para siempre.
// Util para evitar deadlocks y para degradar el servicio con elegancia.

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Ejercicio5_3_RecursoTryLock {

    private static final ReentrantLock lock = new ReentrantLock();

    static void usarRecurso() {
        boolean conseguido = false;
        try {
            conseguido = lock.tryLock(500, TimeUnit.MILLISECONDS);
            if (conseguido) {
                try {
                    System.out.println(Thread.currentThread().getName() + " usa el recurso (2 s)");
                    Thread.sleep(2000);
                } finally {
                    lock.unlock();
                }
            } else {
                System.out.println(Thread.currentThread().getName() + ": Ocupado, lo intento mas tarde");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Cada hilo intenta usar el recurso dos veces: la segunda suele fallar (timeout)
        Thread t1 = Thread.ofPlatform().name("hilo-1").start(() -> { usarRecurso(); usarRecurso(); });
        Thread t2 = Thread.ofPlatform().name("hilo-2").start(() -> { usarRecurso(); usarRecurso(); });
        t1.join();
        t2.join();
    }
}
