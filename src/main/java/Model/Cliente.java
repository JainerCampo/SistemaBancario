package Model;

import java.time.LocalDate;
import java.util.List;
import Database.DAO.CuentaDAO;
import Model.Exceptions.DominioException;

public class Cliente extends Persona {
    private CuentaDAO cuentaDAO;

    public Cliente(TipoIdentificacion tipoIdentificacion, String documento, String nombres,
            String apellidos, LocalDate fechaNacimiento, Direccion direccion,
            String telefono, String correo) throws DominioException {
        super(tipoIdentificacion, documento, nombres, apellidos, fechaNacimiento, direccion, telefono, correo);
    }

    public void asignarCuentaDAO(CuentaDAO cuentaDAO) {
        this.cuentaDAO = cuentaDAO;
    }

    public List<Cuenta> getCuentas() {
        if (cuentaDAO == null || getId() <= 0)
            return List.of();
        try {
            return cuentaDAO.buscarPorCliente(getId());
        } catch (DominioException e) {
            return List.of();
        }
    }
}