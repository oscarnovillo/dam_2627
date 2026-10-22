package config;

/**
 * Clase principal de constantes que actúa como fachada para acceder a todas las constantes
 * de la aplicación web organizadas por categorías
 */
public final class Constants {

    // Constructor privado para prevenir instanciación
    private Constants() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad");
    }

    // URLs y rutas
    public static final String URL_HOLA = UrlConstants.URL_HOLA;
    public static final String URL_ADIVINA = UrlConstants.URL_ADIVINA;
    public static final String TEMPLATE_HOLA = UrlConstants.TEMPLATE_HOLA;
    public static final String TEMPLATE_ADIVINA = UrlConstants.TEMPLATE_ADIVINA;

    // Configuración de Thymeleaf
    public static final String TEMPLATE_ENGINE_ATTR = ThymeleafConstants.TEMPLATE_ENGINE_ATTR;

    // Constantes del juego
    public static final int MAX_INTENTOS = GameConstants.MAX_INTENTOS;
    public static final int MAX_NUMERO = GameConstants.MAX_NUMERO;
    public static final String NUMERO_SECRETO = GameConstants.NUMERO_SECRETO;
    public static final String INTENTOS_RESTANTES = GameConstants.INTENTOS_RESTANTES;
    public static final String GAME_OVER = GameConstants.GAME_OVER;
    public static final String PARAM_NUMERO = GameConstants.PARAM_NUMERO;
    public static final String PARAM_REINICIAR = GameConstants.PARAM_REINICIAR;
    public static final String VAR_INTENTOS = GameConstants.VAR_INTENTOS;

    // Mensajes
    public static final String MSG_NUMERO_MAYOR = MessageConstants.MSG_NUMERO_MAYOR;
    public static final String MSG_NUMERO_MENOR = MessageConstants.MSG_NUMERO_MENOR;
    public static final String MSG_NUMERO_INVALIDO = MessageConstants.MSG_NUMERO_INVALIDO;
    public static final String MSG_GANASTE = MessageConstants.MSG_GANASTE;
    public static final String MSG_GAME_OVER = MessageConstants.MSG_GAME_OVER;
    public static final String VAR_MENSAJE = MessageConstants.VAR_MENSAJE;
    public static final String VAR_ERROR = MessageConstants.VAR_ERROR;
}
