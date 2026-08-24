package Model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import Model.Exceptions.CuentaInactivaException;
import Model.Exceptions.DominioException;
import Model.Exceptions.SaldoInsuficienteException;

public abstract class Cuenta {
    private static final AtomicLong siguienteNumeroCuenta = new AtomicLong(1000000000L);
    private final String numeroCuenta;
    private final Cliente titular;
    private double saldo;
    private EstadoCuenta estado;
    private final LocalDate fechaApertura;
    private final ArrayList<Movimiento> movimientos;

    public Cuenta(Cliente titular) throws DominioException {
        if (titular == null)
            throw new DominioException("La cuenta debe tener un titular.");
        this.titular = titular;
        this.estado = EstadoCuenta.ACTIVO;
        this.fechaApertura = LocalDate.now();
        this.movimientos = new ArrayList<>();
        this.numeroCuenta = String.valueOf(siguienteNumeroCuenta.getAndIncrement());
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public Cliente getTitular() {
        return titular;
    }

    public synchronized EstadoCuenta getEstado() {
        return estado;
    }

    public synchronized double getSaldo() {
        return saldo;
    }

    public LocalDate getFechaApertura() {
        return fechaApertura;
    }

    public synchronized ArrayList<Movimiento> getMovimientos() {
        return new ArrayList<>(movimientos);
    }

    protected void aumentarSaldo(double cantidad) {
        saldo += cantidad;
    }

    protected void disminuirSaldo(double cantidad) {
        saldo -= cantidad;
    }

    protected void validarOperacionActiva() throws CuentaInactivaException {
        if (estado != EstadoCuenta.ACTIVO)
            throw new CuentaInactivaException("La cuenta no está activa.");
    }

    public synchronized void consignar(double monto, CanalMovimiento canal, String descripcion)
            throws DominioException {
        validarOperacionActiva();
        validarMonto(monto);
        Movimiento movimiento = Movimiento.crearConsignacion(canal, monto, descripcion);
        aumentarSaldo(monto);
        agregarMovimiento(movimiento);
    }

    public synchronized void retirar(double monto, CanalMovimiento canal, String descripcion)
            throws DominioException {
        validarRetiro(monto);
        Movimiento movimiento = Movimiento.crearRetiro(canal, monto, descripcion);
        disminuirSaldo(monto);
        agregarMovimiento(movimiento);
    }

    protected synchronized void agregarMovimiento(Movimiento movimiento) throws DominioException {
        if (movimiento == null)
            throw new DominioException("El movimiento no puede ser nulo.");
        movimientos.add(movimiento);
    }

    protected void validarRetiro(double monto) throws DominioException {
        validarOperacionActiva();
        validarMonto(monto);
        if (monto > saldo)
            throw new SaldoInsuficienteException("Saldo insuficiente para realizar el retiro.");
    }

    private void validarMonto(double monto) throws DominioException {
        if (!Double.isFinite(monto) || monto <= 0)
            throw new DominioException("El monto debe ser mayor que cero.");
    }

    public void transferir(Cuenta cuentaDestino, double monto, CanalMovimiento canal,
            String descripcion) throws DominioException {
        if (cuentaDestino == null)
            throw new DominioException("La cuenta destino no puede ser nula.");
        if (cuentaDestino == this)
            throw new DominioException("La cuenta origen y destino no pueden ser la misma.");
        Cuenta primera = numeroCuenta.compareTo(cuentaDestino.numeroCuenta) < 0 ? this : cuentaDestino;
        Cuenta segunda = primera == this ? cuentaDestino : this;
        synchronized (primera) {
            synchronized (segunda) {
                validarRetiro(monto);
                cuentaDestino.validarOperacionActiva();
                if (canal == null)
                    throw new DominioException("El canal del movimiento es obligatorio.");
                if (descripcion == null || descripcion.isBlank())
                    throw new DominioException("La descripción es obligatoria.");
                disminuirSaldo(monto);
                cuentaDestino.aumentarSaldo(monto);
                agregarMovimiento(Movimiento.crearTransferenciaEnviada(canal, monto, descripcion, numeroCuenta,
                        cuentaDestino.numeroCuenta));
                cuentaDestino.agregarMovimiento(Movimiento.crearTransferenciaRecibida(canal, monto, descripcion,
                        numeroCuenta, cuentaDestino.numeroCuenta));
            }
        }
    }

    protected synchronized void cambiarEstado(EstadoCuenta nuevoEstado) throws DominioException {
        if (nuevoEstado == null)
            throw new DominioException("El nuevo estado de la cuenta no puede ser nulo.");
        estado = nuevoEstado;
    }
}