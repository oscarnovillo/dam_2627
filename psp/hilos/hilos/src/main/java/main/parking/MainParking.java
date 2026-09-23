package main.parking;

public class MainParking {

    static void main() {
        Parking parking = new Parking();


        parking.crearCoches();

        parking.esperarFinal();
        parking.debug();
    }
}
