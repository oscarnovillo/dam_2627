package main.completable;

public record Usuario(String nombre,String direccion,String email) {
    public Usuario(String admin) {
        this(admin,"","");
    }
}
