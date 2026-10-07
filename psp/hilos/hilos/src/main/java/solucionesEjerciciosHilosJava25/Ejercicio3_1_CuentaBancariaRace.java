package solucionesEjerciciosHilosJava25;
// Ejercicio 3.1 - Condicion de carrera en una cuenta bancaria
//
// La operacion "saldo = saldo + cantidad" NO es atomica: se lee, se suma y se escribe.
// Si dos hilos intercalan esos pasos, un incremento se pierde.
// Ejecuta el programa varias veces: la version sin sincronizar da resultados distintos.

import java.util.ArrayList;
import java.util.List;

public class Ejercicio3_1_CuentaBancariaRace {

    static class CuentaBancaria {
        private int saldo;

        void depositarSinSync(int cantidad) {          // SIN sincronizar: falla
            saldo = saldo + cantidad;
        }

        synchronized void depositarSync(int cantidad) { // metodo synchronized
            saldo = saldo + cantidad;
        }

        void depositarBloque(int cantidad) {            // bloque synchronized
            synchronized (this) {
                saldo = saldo + cantidad;
            }
        }

        int getSaldo() { return saldo; }
        void reset() { saldo = 0; }
    }

    interface Deposito { void aplicar(CuentaBancaria c); }

    static void lanzar100Depositos(CuentaBancaria cuenta, Deposito op) throws InterruptedException {
        List<Thread> hilos = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            hilos.add(Thread.ofPlatform().start(() -> op.aplicar(cuenta)));
        }
        for (Thread h : hilos) h.join();
    }

    public static void main(String[] args) throws InterruptedException {
        CuentaBancaria cuenta = new CuentaBancaria();

        System.out.println("=== SIN sincronizar (resultados distintos en cada ejecucion) ===");
        for (int intento = 1; intento <= 3; intento++) {
            cuenta.reset();
            lanzar100Depositos(cuenta, c -> c.depositarSinSync(1));
            System.out.println("Intento " + intento + " -> saldo: " + cuenta.getSaldo() + "  (esperado: 100)");
        }

        System.out.println("\n=== CON metodo synchronized ===");
        cuenta.reset();
        lanzar100Depositos(cuenta, c -> c.depositarSync(1));
        System.out.println("Saldo final: " + cuenta.getSaldo());

        System.out.println("\n=== CON bloque synchronized(this) ===");
        cuenta.reset();
        lanzar100Depositos(cuenta, c -> c.depositarBloque(1));
        System.out.println("Saldo final: " + cuenta.getSaldo());
    }
}
