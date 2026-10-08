package solucionesEjerciciosHilosJava25;

// Ejercicio 6.1 - Productor-consumidor con ArrayBlockingQueue
//
// COMPARACION CON wait/notify:
// Con wait/notify manual necesitas un bucle while para comprobar la condicion,
// notifyAll(), try/finally con synchronized, etc. (~40-60 lineas y facil de
// equivocarse: perdida de seniales, despertar espurio...).
// Con BlockingQueue: put() bloquea si esta llena y take() bloquea si esta vacia.
// Toda la coordinacion queda en DOS llamadas de metodo.

import java.util.concurrent.ArrayBlockingQueue;

public class Ejercicio6_1_ProductorConsumidorBQ {

    private static final int CAPACIDAD = 5;
    private static final int TOTAL = 20;
    private static final ArrayBlockingQueue<Integer> cola = new ArrayBlockingQueue<>(CAPACIDAD);

    public static void main(String[] args) throws InterruptedException {
        Thread productor = Thread.ofPlatform().name("productor").start(() -> {
            try {
                for (int i = 1; i <= TOTAL; i++) {
                    cola.put(i); // bloquea si la cola esta llena
                    System.out.println("[Productor] produjo " + i + " (cola: " + cola.size() + "/" + CAPACIDAD + ")");
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumidor = Thread.ofPlatform().name("consumidor").start(() -> {
            try {
                for (int i = 1; i <= TOTAL; i++) {
                    Integer valor = cola.take(); // bloquea si la cola esta vacia
                    System.out.println("[Consumidor] consumio " + valor + " (cola: " + cola.size() + "/" + CAPACIDAD + ")");
                    Thread.sleep(300);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        productor.join();
        consumidor.join();
        System.out.println("Fin. Con BlockingQueue el codigo es mucho mas simple y seguro que con wait/notify.");
    }
}
