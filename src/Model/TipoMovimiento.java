package Model;

public enum TipoMovimiento {
    CONSIGNACION("Consignación"),
    RETIRO("Retiro"),
    TRANSFERENCIA_ENVIADA("Transferencia enviada"),
    TRANSFERENCIA_RECIBIDA("Transferencia recibida");
    
    private final String descripcion;

    TipoMovimiento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
