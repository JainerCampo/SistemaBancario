package Model;

import java.time.LocalDate;
import java.util.ArrayList;
import Model.Exceptions.CuentaDuplicadaException;
import Model.Exceptions.DominioException;

public class Cliente extends Persona {
    private final ArrayList<Cuenta> cuentas = new ArrayList<>();

    public Cliente(TipoIdentificacion tipoIdentificacion, String documento, String nombres,
            String apellidos, LocalDate fechaNacimiento, Direccion direccion,
            String telefono, String correo) throws DominioException {
        super(tipoIdentificacion, documento, nombres, apellidos, fechaNacimiento, direccion, telefono, correo);
    }

    protected void agregarCuentaCliente(Cuenta cuenta) throws DominioException {
        if (cuenta == null)
            throw new DominioException("La cuenta no puede ser nula.");
        if (cuenta.getTitular() != this)
            throw new DominioException("La cuenta no pertenece a este cliente.");
        if (buscarCuentaCliente(cuenta.getNumeroCuenta()) != null)
            throw new CuentaDuplicadaException("La cuenta ya está registrada para este cliente.");
        cuentas.add(cuenta);
    }

    public Cuenta buscarCuentaCliente(String numeroCuenta) throws DominioException {
        if (numeroCuenta == null || numeroCuenta.isBlank())
            throw new DominioException("El número de cuenta no puede ser nulo o vacío.");
        for (Cuenta cuenta : cuentas)
            if (cuenta.getNumeroCuenta().equals(numeroCuenta.trim()))
                return cuenta;
        return null;
    }

    public int cantidadCuentas() {
        return cuentas.size();
    }

    public ArrayList<Cuenta> getCuentas() {
        return new ArrayList<>(cuentas);
    }
}