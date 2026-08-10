package Model;

import java.time.LocalDate;
import java.util.ArrayList;

public class Cliente extends Persona {

    private ArrayList<Cuenta> cuentas;

    // constructor
    public Cliente(TipoIdentificacion tipoidentificacion, String documento,
            String nombres, String apellidos, LocalDate fechaNacimiento,
            Direccion direccion, String telefono, String correo) {
        super(tipoidentificacion, documento, nombres, apellidos, fechaNacimiento,
                direccion, telefono, correo);
        this.cuentas = new ArrayList<>();
    }

    // Método para agregar una cuenta al cliente
    public void agregarCuenta(Cuenta cuenta) {
        if (cuenta == null) {
            return;
        }
        cuentas.add(cuenta);
    }

    public Cuenta buscarCuenta(String numeroCuenta) {
        for (Cuenta cuenta : cuentas) {
            if (cuenta.getNumeroCuenta().equals(numeroCuenta)) {
                return cuenta;
            }
        }
        return null; // Retorna null si no se encuentra la cuenta
    }

    //metodo para mostrar todas las cuentas del cliente
    public int cantidadCuentas() {
        return cuentas.size();
    }

    /*
     * cliente.agregarCuenta(cuenta);
     * 
     * cliente.obtenerCuenta(numeroCuenta);
     * 
     * cliente.mostrarCuentas();
     * 
     * cliente.cantidadCuentas();
     * 
     * buscarCuenta(...)
     * 
     * tieneCuenta(...)
     * 
     * cantidadDeCuentas()
     * 
     * mostrarCuentas()
     * 
     * obtenerCuentaPorNumero(...)
     */
}
