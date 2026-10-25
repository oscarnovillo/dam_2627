package config;

/**
 * Constantes relacionadas con la configuración de Thymeleaf
 */
public final class ThymeleafConstants {

    // Constructor privado para prevenir instanciación
    private ThymeleafConstants() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad");
    }

    // Configuración de Thymeleaf
    public static final String TEMPLATE_ENGINE_ATTR = "com.daw.TemplateEngine";
    public static final String TEMPLATE_MODE = "HTML";
    public static final String CHARACTER_ENCODING = "UTF-8";
    public static final String CONTENT_TYPE = "text/html;charset=UTF-8";
}
