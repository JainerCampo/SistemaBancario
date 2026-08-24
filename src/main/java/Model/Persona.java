package Model;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import Model.Exceptions.DominioException;

public abstract class Persona {
    private static final AtomicInteger contador = new AtomicInteger(1);
    private final int id;
    private final TipoIdentificacion tipoIdentificacion;
    private final String documento;
    private final String nombres;
    private final String apellidos;
    private final LocalDate fechaNacimiento;
    private final Direccion direccion;
    private String telefono;
    private String correo;

    public Persona(TipoIdentificacion tipoIdentificacion, String documento, String nombres,
            String apellidos, LocalDate fechaNacimiento, Direccion direccion,
            String telefono, String correo) throws DominioException {
        if (tipoIdentificacion == null)
            throw new DominioException("El tipo de identificación es obligatorio.");
        if (documento == null || documento.isBlank() || !documento.trim().matches("\\d+"))
            throw new DominioException("El documento no es válido.");
        validarNombre(nombres, "Los nombres");
        validarNombre(apellidos, "Los apellidos");
        if (fechaNacimiento == null || fechaNacimiento.isAfter(LocalDate.now()))
            throw new DominioException("La fecha de nacimiento no es válida.");
        if (direccion == null)
            throw new DominioException("La dirección es obligatoria.");
        validarTelefono(telefono);
        validarCorreo(correo);
        this.id = contador.getAndIncrement();
        this.tipoIdentificacion = tipoIdentificacion;
        this.documento = documento.trim();
        this.nombres = nombres.trim();
        this.apellidos = apellidos.trim();
        this.fechaNacimiento = fechaNacimiento;
        this.direccion = direccion;
        this.telefono = telefono.trim();
        this.correo = correo.trim();
    }

    private void validarNombre(String valor, String campo) throws DominioException {
        if (valor == null || valor.isBlank() || !valor.trim().matches("[\\p{L}][\\p{L} '\\-]*")) {
            throw new DominioException(campo + " solo pueden contener letras, espacios, apóstrofes y guiones.");
        }
    }

    public int getId() {
        return id;
    }

    public TipoIdentificacion getTipoIdentificacion() {
        return tipoIdentificacion;
    }

    public String getDocumento() {
        return documento;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    // Cambia el teléfono conservando la validación del modelo.
    public void cambiarTelefono(String nuevoTelefono) throws DominioException {
        validarTelefono(nuevoTelefono);
        this.telefono = nuevoTelefono.trim();
    }

    // Cambia el correo electrónico conservando la validación del modelo.
    public void cambiarCorreo(String nuevoCorreo) throws DominioException {
        validarCorreo(nuevoCorreo);
        this.correo = nuevoCorreo.trim();
    }

    private void validarTelefono(String valor) throws DominioException {
        if (valor == null || valor.isBlank() || !valor.trim().matches("\\d+"))
            throw new DominioException("El teléfono no es válido.");
    }

    private void validarCorreo(String valor) throws DominioException {
        if (valor == null || valor.isBlank()
                || !valor.trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            throw new DominioException("El correo electrónico no es válido.");
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " - " + tipoIdentificacion + ": " + documento + " - " + direccion + " - "
                + telefono + " - " + correo;
    }
}