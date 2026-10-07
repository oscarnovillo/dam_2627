package problemas.redesocial;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record Notificacion(String destinatario, String mensaje, LocalTime hora) {

    public static Notificacion de(String destinatario, String mensaje) {
        return new Notificacion(destinatario, mensaje, LocalTime.now());
    }

    @Override
    public String toString() {
        return "[" + hora.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                + "] -> " + destinatario + ": " + mensaje;
    }
}
