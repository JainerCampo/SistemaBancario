package Model;

import Model.Exceptions.DominioException;

public class Direccion {
    private String direccion;

    public Direccion(String direccion) throws DominioException {
        validarDireccion(direccion);
        this.direccion = direccion;
    }

    public String getDireccion() {
        return direccion;
    }

    @Override
    public String toString() {
        return "Dirección: " + direccion;
    }

    public void cambiarDireccion(String nuevaDireccion) throws DominioException {
        if (nuevaDireccion == null || nuevaDireccion.isBlank()) {
            throw new DominioException("La nueva dirección es obligatoria.");
        }
        this.direccion = nuevaDireccion;
    }

    private void validarDireccion(String valor) throws DominioException {
        if (valor == null || valor.isBlank()) {
            throw new DominioException("La dirección es obligatoria.");
        }
    }
}
