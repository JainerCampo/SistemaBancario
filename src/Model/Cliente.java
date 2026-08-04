package Model;

import java.util.ArrayList;

public abstract class Cliente {
    private String cedula;
    private String telefono;
    private String direccion;
    private ArrayList<Cuenta> cuentas;
    
    public Cliente (String cedula, String telefono, String direccion) {
        this.cedula = cedula;
        this.telefono = telefono;
        this.direccion = direccion; 
    }
}
