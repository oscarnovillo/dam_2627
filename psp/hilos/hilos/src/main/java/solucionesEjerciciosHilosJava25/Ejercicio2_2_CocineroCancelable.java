package solucionesEjerciciosHilosJava25;
// Ejercicio 2.2 - Cocinero con temporizador cancelable (interrupt + estados)
//
// POR QUE interrupt() NO DETIENE EL HILO POR SI SOLO:
// interrupt() solo establece la "bandera de interrupcion" del hilo (y la propaga
// despertando sleeps/waits con InterruptedException). El hilo sigue ejecutandose
// a menos que:
//   a) capture InterruptedException y decida terminar (como aqui), o
//   b) compruebe isInterrupted() / Thread.interrupted() periodicamente y decida terminar.
// Es un mecanismo de PETICION educada de cancelacion, no de parada forzosa.

public class Ejercicio2_2_CocineroCancelable {

    public static void main(String[] args) throws InterruptedException {
        Thread cocinero = new Thread(() -> {
            for (int segundo = 1; segundo <= 10; segundo++) {
                try {
                    Thread.sleep(1000);
                    System.out.println("[Cocinero] cocinando... " + segundo + " s");
                } catch (InterruptedException e) {
                    // La bandera se limpia al lanzarse la excepcion: restauramos y salimos limpio
                    System.out.println("[Cocinero] Coccion CANCELADA en el segundo " + segundo);
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            System.out.println("[Cocinero] plato LISTO.");
        }, "cocinero");

        System.out.println("Estado inicial:        " + cocinero.getState()); // NEW
        cocinero.start();
        System.out.println("Tras start():          " + cocinero.getState()); // RUNNABLE (o TIMED_WAITING ya)

        Thread.sleep(3000);
        System.out.println("Durante el sleep():    " + cocinero.getState()); // TIMED_WAITING

        cocinero.interrupt(); // pedimos la cancelacion
        cocinero.join();

        System.out.println("Al terminar:           " + cocinero.getState()); // TERMINATED
    }
}
