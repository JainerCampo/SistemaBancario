package Model;

import java.time.LocalDate;
import java.util.ArrayList;

public abstract class Cuenta {

    private static long siguienteNumeroCuenta = 1000000000;

    private String numeroCuenta;
    private Cliente titular;
    private double saldo;
    private EstadoCuenta estado;
    private LocalDate fechaApertura;
    // private LocalDate fechaCierre;
    private ArrayList<Movimiento> movimientos;

    // Constructor
    public Cuenta(Cliente titular) {
        // verificar que el titular no sea nulo
        if (titular == null) {
            throw new IllegalArgumentException(
                    "La cuenta debe tener un titular.");
        }
        this.titular = titular;
        this.saldo = 0; // Inicializa el saldo en 0 al crear la cuenta
        this.estado = EstadoCuenta.ACTIVO;
        this.fechaApertura = LocalDate.now();
        this.movimientos = new ArrayList<>();
        this.numeroCuenta = String.valueOf(siguienteNumeroCuenta++);
    }

    // Getters
    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public Cliente getTitular() {
        return titular;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    public double getSaldo() {
        return saldo;
    }

    public LocalDate getFechaApertura() {
        return fechaApertura;
    }

    public ArrayList<Movimiento> getMovimientos() {
        return new ArrayList<>(movimientos);
    }

    // metodo para aumentar el saldo de la cuenta
    protected void aumentarSaldo(double cantidad) {
        saldo += cantidad;
    }

    // metodo para disminuir el saldo de la cuenta
    protected void disminuirSaldo(double cantidad) {
        saldo -= cantidad;
    }

    // metodo para consignar dinero en la cuenta
    public void consignar(double monto,
            CanalMovimiento canal,
            String descripcion) {

        // verificar si la cuenta está activa antes de permitir la consignación
        if (this.estado != EstadoCuenta.ACTIVO) {
            throw new IllegalStateException("La cuenta no está activa.");
        }

        // Verificar si el monto es válido
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        }

        // Crear el movimiento de consignación
        Movimiento movimiento = Movimiento.crearConsignacion(
                canal,
                monto,
                descripcion);

        // Actualizar el saldo de la cuenta
        aumentarSaldo(monto);

        // Agregar el movimiento a la lista de movimientos de la cuenta
        agregarMovimiento(movimiento);
    }

    // metodo para retirar dinero de la cuenta
    public void retirar(double monto,
            CanalMovimiento canal,
            String descripcion) {

        // verificar si la cuenta está activa antes de permitir el retiro
        if (this.estado != EstadoCuenta.ACTIVO) {
            throw new IllegalStateException("La cuenta no está activa.");
        }

        // Verificar si el monto es válido
        if (monto <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor que cero.");
        }

        // Verificar si hay suficiente saldo para realizar el retiro
        if (monto > saldo) {
            throw new IllegalStateException(
                    "Saldo insuficiente para realizar el retiro.");
        }

        // Crear el movimiento de retiro
        Movimiento movimiento = Movimiento.crearRetiro(
                canal,
                monto,
                descripcion);

        // Actualizar el saldo de la cuenta
        disminuirSaldo(monto);

        // Agregar el movimiento a la lista de movimientos de la cuenta
        agregarMovimiento(movimiento);
    }

    // metodo para agregar un movimiento a la cuenta
    protected void agregarMovimiento(Movimiento movimiento) {
        movimientos.add(movimiento);
    }
}
