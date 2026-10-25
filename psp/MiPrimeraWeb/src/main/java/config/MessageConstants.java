package config;

/**
 * Constantes de mensajes de la aplicación
 */
public final class MessageConstants {

    // Constructor privado para prevenir instanciación
    private MessageConstants() {

    }

    // Mensajes del juego
    public static final String MSG_NUMERO_MAYOR = "El número es mayor";
    public static final String MSG_NUMERO_MENOR = "El número es menor";
    public static final String MSG_NUMERO_INVALIDO = "Por favor, introduce un número válido";
    public static final String MSG_GANASTE = "¡Has ganado! El número era ";
    public static final String MSG_GAME_OVER = "¡Game Over! El número era ";

    // Mensajes generales
    public static final String MSG_HOLA_MUNDO = "Hola Mundo";
    public static final String MSG_THYMELEAF_ERROR = "Thymeleaf no está inicializado correctamente";

    // Variables de contexto para mensajes
    public static final String VAR_MENSAJE = "mensaje";
    public static final String VAR_ERROR = "error";
}
