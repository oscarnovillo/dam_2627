package problemas;// PROBLEMA 1 - El Contador Compartido (Nivel basico)
// Mismo escenario con 3 implementaciones: sin sincronizar, synchronized y AtomicInteger.
// Las 3 usan hilos virtuales: la diferencia de tiempo que se observa es la
// contencion (o su ausencia) sobre el contador, no el coste de crear hilos.

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Problema1_ContadorVisitas {

    static final int VISITANTES = 1000;

    // --- 1) SIN sincronizacion: condicion de carrera (visitas++ lee-suma-escribe) ---
    static class ContadorInseguro implements Contador {
        private int visitas;
        public void incrementarVisita() { visitas++; }
        public int getVisitas() { return visitas; }
    }

    // --- 2) CON synchronized: un hilo por vez entra al metodo ---
    static class ContadorSynchronized implements Contador {
        private int visitas;
        public synchronized void incrementarVisita() { visitas++; }
        public synchronized int getVisitas() { return visitas; }
    }

    // --- 3) CON AtomicInteger: CAS hardware, sin cerrojos ---
    static class ContadorAtomico implements Contador {
        private final AtomicInteger visitas = new AtomicInteger();
        public void incrementarVisita() { visitas.incrementAndGet(); }
        public int getVisitas() { return visitas.get(); }
    }

    interface Contador {
        void incrementarVisita();
        int getVisitas();
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CONTADOR DE VISITAS WEB ===");
        System.out.println("Esperando " + VISITANTES + " visitantes...\n");

        probar("SIN SINCRONIZACION", new ContadorInseguro());
        probar("CON SYNCHRONIZED", new ContadorSynchronized());
        probar("CON ATOMICINTEGER", new ContadorAtomico());
    }

    static void probar(String titulo, Contador contador) throws InterruptedException {
        Random rnd = new Random();
        List<Thread> hilos = new ArrayList<>(VISITANTES);

        long inicio = System.currentTimeMillis();
        for (int i = 0; i < VISITANTES; i++) {
            Thread h = Thread.ofVirtual().start(() -> {
                try {
                    Thread.sleep(50 + rnd.nextInt(101)); // espera aleatoria 50-150 ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                contador.incrementarVisita();
            });
            hilos.add(h);
        }
        for (Thread h : hilos) h.join();
        long tiempo = System.currentTimeMillis() - inicio;

        int esperadas = VISITANTES;
        int contadas = contador.getVisitas();
        System.out.println("--- " + titulo + " ---");
        System.out.println("Visitas esperadas: " + esperadas);
        System.out.println("Visitas contadas:  " + contadas
                + (contadas == esperadas ? "  ✅ CORRECTO" : "  ❌ INCORRECTO (condicion de carrera)"));
        System.out.println("Tiempo: " + tiempo + "ms\n");
    }
}
