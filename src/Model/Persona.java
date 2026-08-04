package Model;

import java.time.LocalDate;

public abstract class Persona {

    private static int contador = 1;

    private int id;
    private TipoIdentificacion tipoIdentificacion;
    private String documento;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private Direccion direccion;
    private String telefono;
    private String correo;

    // Constructor
    public Persona(TipoIdentificacion tipoIdentificacion, String documento, String nombres,
            String apellidos, LocalDate fechaNacimiento, Direccion direccion,
            String telefono, String correo) {
        this.id = contador++;
        this.tipoIdentificacion = tipoIdentificacion;
        this.documento = documento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion = direccion;
        this.telefono = telefono;
        this.correo = correo;
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

    // metodo nombre completo
    public String getNombreCompleto() {
        return this.nombres + " " + this.apellidos;
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " - " + 
        this.tipoIdentificacion + ": " + 
        this.documento +" - " + 
        this.direccion + " - " + 
        this.telefono + " - "+
        this.correo;
    }
}