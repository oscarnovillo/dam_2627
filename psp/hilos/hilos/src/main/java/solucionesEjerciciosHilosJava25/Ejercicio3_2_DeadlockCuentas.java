package solucionesEjerciciosHilosJava25;
// Ejercicio 3.2 - Deadlock entre dos cuentas y su correccion
//
// DEADLOCK: hilo-1 bloquea A y espera B; hilo-2 bloquea B y espera A.
// Ninguno puede avanzar. Detectalo en caliente con:
//   jconsole  o  jcmd <pid> Thread.print   (veras "Found 1 deadlock" / BLOCKED)
//
// CORRECCION: establecer un ORDEN GLOBAL en la adquisicion de los locks
// (p. ej. por el id de cuenta). Si todos los hilos toman los locks en el
// mismo orden, el deadlock circular es imposible.

public class Ejercicio3_2_DeadlockCuentas {

    static class CuentaBancaria {
        private final int id;
        private int saldo;

        CuentaBancaria(int id, int saldo) {
            this.id = id;
            this.saldo = saldo;
        }

        // Version con deadlock: this -> destino (orden distinto segun el llamante)
        void transferirInsegura(CuentaBancaria destino, int cantidad) {
            synchronized (this) {
                synchronized (destino) {
                    saldo -= cantidad;
                    destino.saldo += cantidad;
                    System.out.println(Thread.currentThread().getName() + " transfirio "
                            + cantidad + " de cuenta-" + id + " a cuenta-" + destino.id);
                }
            }
        }

        // Version corregida: locks SIEMPRE en el mismo orden (por id de cuenta)
        void transferirSegura(CuentaBancaria destino, int cantidad) {
            CuentaBancaria primera = this.id < destino.id ? this : destino;
            CuentaBancaria segunda = this.id < destino.id ? destino : this;
            synchronized (primera) {
                synchronized (segunda) {
                    saldo -= cantidad;
                    destino.saldo += cantidad;
                    System.out.println(Thread.currentThread().getName() + " transfirio "
                            + cantidad + " de cuenta-" + id + " a cuenta-" + destino.id);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Fase 1: provocando el DEADLOCK ===");
        CuentaBancaria a = new CuentaBancaria(1, 100);
        CuentaBancaria b = new CuentaBancaria(2, 100);

        Thread h1 = Thread.ofPlatform().name("hilo-A-a-B").start(() -> a.transferirInsegura(b, 50));
        Thread h2 = Thread.ofPlatform().name("hilo-B-a-A").start(() -> b.transferirInsegura(a, 50));

        h1.join(3000); // esperamos con timeout
        h2.join(3000);
        if (h1.isAlive() || h2.isAlive()) {
            System.out.println(">>> DEADLOCK DETECTADO: los hilos siguen BLOQUEADOS despues de 3 s.");
            System.out.println("    (En la vida real: jconsole -> pestana Threads, o jcmd <pid> Thread.print)");
        } else {
            System.out.println("Esta vez hubo suerte y no hubo deadlock (no deterministico).");
        }

        System.out.println("\n=== Fase 2: transferencias con orden de locks (sin deadlock) ===");
        CuentaBancaria x = new CuentaBancaria(1, 100);
        CuentaBancaria y = new CuentaBancaria(2, 100);
        Thread h3 = Thread.ofPlatform().name("hilo-X-a-Y").start(() -> x.transferirSegura(y, 50));
        Thread h4 = Thread.ofPlatform().name("hilo-Y-a-X").start(() -> y.transferirSegura(x, 50));
        h3.join();
        h4.join();
        System.out.println("Ambos hilos terminaron correctamente.");

        System.exit(0); // terminar los hilos bloqueados de la fase 1
    }
}
