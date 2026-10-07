package solucionesEjerciciosHilosJava25;
// Ejercicio 2.1 - Cuenta regresiva de cohete con join()

public class Ejercicio2_1_CuentaRegresiva {

    public static void main(String[] args) throws InterruptedException {
        Thread cohete = new Thread(() -> {
            for (int i = 10; i >= 0; i--) {
                System.out.println(i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "cohete");

        cohete.start();
        cohete.join(); // el hilo principal espera hasta que "cohete" termine

        System.out.println("!Despegue!");
    }
}
