package problemas;// PROBLEMA 2 - El Banco Virtual (Nivel intermedio)
// 50 clientes x 10 operaciones sobre una cuenta de 10.000 €.
// Tres implementaciones: ReentrantLock, synchronized y volatile (insuficiente).
//
// TRUCO DIDACTICO: las operaciones se GENERAN UNA SOLA VEZ (semilla fija 42)
// y se REPITEN sobre cada implementacion. Asi lock y synchronized deben dar
// EXACTAMENTE el mismo saldo final, y la diferencia con volatile queda clara.

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;

public class Problema2_BancoVirtual {

    static final double SALDO_INICIAL = 10_000;
    static final int CLIENTES = 50;
    static final int OPS_POR_CLIENTE = 10;
    static final int TOTAL_OPS = CLIENTES * OPS_POR_CLIENTE;

    // ---------- Contrato comun ----------
    interface Cuenta {
        boolean retirar(double cantidad); // false = fondos insuficientes
        void ingresar(double cantidad);
        double consultarSaldo();
        List<String> obtenerHistorial();
    }

    // ---------- 1) ReentrantLock ----------
    static class CuentaConLock implements Cuenta {
        private double saldo = SALDO_INICIAL;
        private final ReentrantLock lock = new ReentrantLock();
        private final List<String> historial = new ArrayList<>();

        private void registrar(String texto) {
            historial.add("[" + ahora() + "] " + texto);
        }

        public boolean retirar(double cantidad) {
            lock.lock();
            try {
                if (saldo < cantidad) {
                    registrar("FALLO retiro de " + fmt(cantidad) + "€ - fondos insuficientes (saldo "
                            + fmt(saldo) + "€)");
                    return false;
                }
                saldo -= cantidad;
                registrar("RETIRO  -" + fmt(cantidad) + "€ | saldo: " + fmt(saldo) + "€");
                return true;
            } finally {
                lock.unlock();
            }
        }

        public void ingresar(double cantidad) {
            lock.lock();
            try {
                saldo += cantidad;
                registrar("INGRESO +" + fmt(cantidad) + "€ | saldo: " + fmt(saldo) + "€");
            } finally {
                lock.unlock();
            }
        }

        public double consultarSaldo() { return saldo; }
        public List<String> obtenerHistorial() { return historial; }
    }

    // ---------- 2) synchronized ----------
    static class CuentaSynchronized implements Cuenta {
        private double saldo = SALDO_INICIAL;
        private final List<String> historial = new ArrayList<>();

        private void registrar(String texto) {
            historial.add("[" + ahora() + "] " + texto);
        }

        public synchronized boolean retirar(double cantidad) {
            if (saldo < cantidad) {
                registrar("FALLO retiro de " + fmt(cantidad) + "€ - fondos insuficientes (saldo "
                        + fmt(saldo) + "€)");
                return false;
            }
            saldo -= cantidad;
            registrar("RETIRO  -" + fmt(cantidad) + "€ | saldo: " + fmt(saldo) + "€");
            return true;
        }

        public synchronized void ingresar(double cantidad) {
            saldo += cantidad;
            registrar("INGRESO +" + fmt(cantidad) + "€ | saldo: " + fmt(saldo) + "€");
        }

        public synchronized double consultarSaldo() { return saldo; }
        public synchronized List<String> obtenerHistorial() { return historial; }
    }

    // ---------- 3) volatile: visibilidad SI, atomicidad NO ----------
    static class CuentaVolatile implements Cuenta {
        private volatile double saldo = SALDO_INICIAL; // volatile NO protege check-then-act
        private final List<String> historial = new ArrayList<>();

        public boolean retirar(double cantidad) {
            if (saldo < cantidad) return false;   // CHECK...
            dormir(2);                            // ...otro hilo puede intercalarse aqui
            saldo -= cantidad;                    // ...y ACT sobre un saldo ya cambiado
            return true;
        }

        public void ingresar(double cantidad) {
            double tmp = saldo;                   // read-modify-write NO atomico:
            dormir(2);
            saldo = tmp + cantidad;               // se pierden actualizaciones
        }

        public double consultarSaldo() { return saldo; }
        public List<String> obtenerHistorial() { return historial; }
    }

    // ---------- Escenario fijo: [tipo, cantidad] tipo 0=retiro(60%) 1=ingreso(40%) ----------
    record Resultado(double saldo, int exitosas, int fallidas, long tiempoMs) {}

    static int[][][] generarOperaciones() {
        Random rnd = new Random(42); // semilla fija: misma secuencia en cada ejecucion
        int[][][] ops = new int[CLIENTES][OPS_POR_CLIENTE][2];
        for (int c = 0; c < CLIENTES; c++) {
            for (int o = 0; o < OPS_POR_CLIENTE; o++) {
                boolean esRetiro = rnd.nextDouble() < 0.6;
                ops[c][o][0] = esRetiro ? 0 : 1;
                ops[c][o][1] = esRetiro ? 1 + rnd.nextInt(100)   // retiro 1-100 €
                                        : 1 + rnd.nextInt(50);   // ingreso 1-50 €
            }
        }
        return ops;
    }

    static Resultado ejecutar(Cuenta cuenta, int[][][] ops) throws InterruptedException {
        AtomicInteger exitosas = new AtomicInteger();
        AtomicInteger fallidas = new AtomicInteger();
        List<Thread> clientes = new ArrayList<>();
        Random rnd = new Random(7);

        long inicio = System.currentTimeMillis();
        for (int c = 0; c < CLIENTES; c++) {
            final int id = c + 1;
            final int[][] misOps = ops[c];
            clientes.add(Thread.ofPlatform().name("cliente-" + id).start(() -> {
                for (int[] op : misOps) {
                    dormir(100 + rnd.nextInt(201)); // latencia bancaria 100-300 ms
                    double cantidad = op[1];
                    if (op[0] == 0) {                        // retiro
                        if (cuenta.retirar(cantidad)) exitosas.incrementAndGet();
                        else fallidas.incrementAndGet();
                    } else {                                 // ingreso
                        cuenta.ingresar(cantidad);
                        exitosas.incrementAndGet();
                    }
                }
            }));
        }
        for (Thread t : clientes) t.join();
        return new Resultado(cuenta.consultarSaldo(), exitosas.get(), fallidas.get(),
                System.currentTimeMillis() - inicio);
    }

    static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    static String ahora() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    static String fmt(double n) {
        return String.format("%.2f", n);
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== BANCO VIRTUAL ===");
        System.out.println("Saldo inicial: " + fmt(SALDO_INICIAL) + "€");
        System.out.println(CLIENTES + " clientes realizando " + TOTAL_OPS + " operaciones totales...\n");

        int[][][] ops = generarOperaciones();

        Cuenta conLock = new CuentaConLock();
        Resultado r1 = ejecutar(conLock, ops);
        System.out.println("--- CON REENTRANTLOCK ---");
        System.out.println("Saldo final: " + fmt(r1.saldo()) + "€");
        System.out.println("Operaciones exitosas: " + r1.exitosas() + "/" + TOTAL_OPS);
        System.out.println("Operaciones fallidas: " + r1.fallidas() + " (fondos insuficientes)");
        System.out.println("Tiempo total: " + fmt(r1.tiempoMs() / 1000.0) + "s ✅\n");

        Cuenta conSync = new CuentaSynchronized();
        Resultado r2 = ejecutar(conSync, ops);
        System.out.println("--- CON SYNCHRONIZED ---");
        System.out.println("Saldo final: " + fmt(r2.saldo()) + "€");
        System.out.println("Operaciones exitosas: " + r2.exitosas() + "/" + TOTAL_OPS);
        System.out.println("Tiempo total: " + fmt(r2.tiempoMs() / 1000.0) + "s ✅");
        System.out.println("Mismo saldo que con lock: " + (r1.saldo() == r2.saldo()) + "\n");

        Cuenta conVolatile = new CuentaVolatile();
        Resultado r3 = ejecutar(conVolatile, ops);
        System.out.println("--- CON VOLATILE ---");
        System.out.println("Saldo final: " + fmt(r3.saldo()) + "€ ❌ INCORRECTO");
        System.out.println("Operaciones registradas: " + (r3.exitosas() + r3.fallidas()) + " de "
                + TOTAL_OPS + " (el resto se pierde por carreras)");
        System.out.println("Leccion: volatile da VISIBILIDAD pero no ATOMICIDAD:");
        System.out.println("- retirar() hace check-then-act (saldo puede quedar negativo)");
        System.out.println("- ingresar() hace read-modify-write (se pierden ingresos)\n");

        System.out.println("--- MUESTRA DEL HISTORIAL (ReentrantLock, primeras 5 entradas) ---");
        conLock.obtenerHistorial().stream().limit(5).forEach(System.out::println);
    }
}
