package Model;

public enum CanalMovimiento {
    CAJERO("Cajero"),
    TRANSFERENCIA("Transferencia"),
    VENTANILLA("Ventanilla"), // hace referencia a la ventanilla del banco
    CORRESPONSAL_BANCARIO("Corresponsal Bancario"),
    SISTEMA("Sistema"); // hace referencia a una opreacion propia del sistema
    private final String descripcion;

    CanalMovimiento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
