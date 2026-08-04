package Model;
       
public enum TipoIdentificacion {
    CC("Cédula de ciudadanía"), 
    TI("Tarjeta de identidad"), 
    PAS("Pasaporte");
    private final String descripcion;
    TipoIdentificacion(String descripcion) {
        this.descripcion = descripcion;
    }
    public String getDescripcion() {
        return descripcion;
    }
}
