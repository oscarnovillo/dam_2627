package solucionesEjerciciosHilosJava25;

// Ejercicio 1.1 - Descarga simultanea de archivos (extends Thread vs implements Runnable)
//
// COMPARACION DE ENFOQUES:
// - extends Thread: la logica esta acoplada a la clase Thread; adecuado cuando la tarea
//   ES un hilo y no necesitas heredar de otra clase (Java no tiene herencia multiple).
// - implements Runnable: separa la TAREA del HILO; la misma clase Runnable puede
//   ejecutarse en Thread, ExecutorService, pools, hilos virtuales, etc.
//   Es el enfoque recomendado en produccion por su flexibilidad.

public class Ejercicio1_1_DescargaSimultanea {

    // ----- Version 1: extends Thread -----
    static class DescargaThread extends Thread {
        private final String archivo;

        DescargaThread(String archivo) {
            super("descarga-" + archivo);
            this.archivo = archivo;
        }

        @Override
        public void run() {
            for (int progreso = 0; progreso <= 100; progreso += 10) {
                System.out.println(getName() + " -> " + archivo + ": " + progreso + "%");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            System.out.println(getName() + " -> " + archivo + ": descarga COMPLETA.");
        }
    }

    // ----- Version 2: implements Runnable -----
    static class DescargaRunnable implements Runnable {
        private final String archivo;

        DescargaRunnable(String archivo) {
            this.archivo = archivo;
        }

        @Override
        public void run() {
            for (int progreso = 0; progreso <= 100; progreso += 10) {
                System.out.println(Thread.currentThread().getName() + " -> " + archivo + ": " + progreso + "%");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            System.out.println(Thread.currentThread().getName() + " -> " + archivo + ": descarga COMPLETA.");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Version 1: extends Thread ===");
        for (int i = 1; i <= 4; i++) {
            new DescargaThread("archivo" + i + ".zip").start();
        }
        Thread.sleep(2500); // esperar a que terminen para que no se mezcle la salida

        System.out.println("\n=== Version 2: implements Runnable (API moderna Thread.ofPlatform) ===");
        for (int i = 1; i <= 4; i++) {
            Thread.ofPlatform()
                    .name("descarga-" + i)
                    .start(new DescargaRunnable("archivo" + i + ".zip"));
        }
    }
}
