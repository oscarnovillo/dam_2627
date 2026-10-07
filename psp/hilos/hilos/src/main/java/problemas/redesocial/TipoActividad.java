package problemas.redesocial;

public enum TipoActividad {
    PUBLICAR_POST(2000),   // 2 s para crear contenido
    DAR_LIKE(100),         // 100 ms
    COMENTAR(1500),        // 1.5 s
    SEGUIR_USUARIO(200),   // 200 ms
    COMPARTIR_POST(300);   // 300 ms

    final int tiempoMs;

    TipoActividad(int tiempoMs) {
        this.tiempoMs = tiempoMs;
    }
}
