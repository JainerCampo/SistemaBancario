package Model;

import java.time.LocalDate;
import java.util.List;
import Database.DAO.MovimientoDAO;
import Database.DAO.CuentaDAO;

import Model.Exceptions.CuentaInactivaException;
import Model.Exceptions.DominioException;
import Model.Exceptions.SaldoInsuficienteException;

public abstract class Cuenta {
    private String numeroCuenta;
    private final Cliente titular;
    private double saldo;
    private EstadoCuenta estado;
    private LocalDate fechaApertura;
    private MovimientoDAO movimientoDAO;
    private CuentaDAO cuentaDAO;

    public Cuenta(Cliente titular) throws DominioException {
        if (titular == null)
            throw new DominioException("La cuenta debe tener un titular.");
        this.titular = titular;
        this.estado = EstadoCuenta.ACTIVO;
        this.fechaApertura = LocalDate.now();
        this.numeroCuenta = null;
    }

    protected Cuenta(Cliente titular, String numeroCuenta, double saldo, EstadoCuenta estado,
            LocalDate fechaApertura) throws DominioException {
        this(titular);
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldo;
        this.estado = estado;
        this.fechaApertura = fechaApertura;
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

    public synchronized List<Movimiento> getMovimientos() {
        if (movimientoDAO == null || numeroCuenta == null)
            return List.of();
        try {
            return movimientoDAO.listarPorCuenta(numeroCuenta);
        } catch (DominioException e) {
            return List.of();
        }
    }

    public void asignarNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public void asignarMovimientoDAO(MovimientoDAO movimientoDAO) {
        this.movimientoDAO = movimientoDAO;
    }

    public void asignarCuentaDAO(CuentaDAO cuentaDAO) {
        this.cuentaDAO = cuentaDAO;
    }

    public void cargarSaldo(double saldo) {
        this.saldo = saldo;
    }

    public void cargarEstado(EstadoCuenta estado) {
        this.estado = estado;
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
        consignar(monto, canal, descripcion, descripcion);
    }

    public synchronized void consignar(double monto, CanalMovimiento canal, String descripcion, String motivo)
            throws DominioException {
        validarOperacionActiva();
        validarMonto(monto);
        validarMotivo(motivo);
        Movimiento movimiento = Movimiento.crearConsignacion(canal, monto, descripcion + " | Motivo: " + motivo);
        aumentarSaldo(monto);
        persistirMovimiento(movimiento);
    }

    public synchronized void retirar(double monto, CanalMovimiento canal, String descripcion)
            throws DominioException {
        retirar(monto, canal, descripcion, descripcion);
    }

    public synchronized void retirar(double monto, CanalMovimiento canal, String descripcion, String motivo)
            throws DominioException {
        validarRetiro(monto);
        validarMotivo(motivo);
        Movimiento movimiento = Movimiento.crearRetiro(canal, monto, descripcion + " | Motivo: " + motivo);
        disminuirSaldo(monto);
        persistirMovimiento(movimiento);
    }

    protected void persistirMovimiento(Movimiento movimiento) throws DominioException {
        if (movimiento == null)
            throw new DominioException("El movimiento no puede ser nulo.");
        if (cuentaDAO != null)
            cuentaDAO.actualizarSaldo(numeroCuenta, saldo);
        if (movimientoDAO != null)
            movimientoDAO.insertar(movimiento, numeroCuenta);
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
        transferir(cuentaDestino, monto, canal, descripcion, descripcion);
    }

    public void transferir(Cuenta cuentaDestino, double monto, CanalMovimiento canal,
            String descripcion, String motivo) throws DominioException {
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
                validarMotivo(motivo);
                String detalle = descripcion.trim() + " | Motivo: " + motivo.trim();
                disminuirSaldo(monto);
                cuentaDestino.aumentarSaldo(monto);
                persistirMovimiento(Movimiento.crearTransferenciaEnviada(canal, monto, detalle, numeroCuenta,
                        cuentaDestino.numeroCuenta));
                cuentaDestino.persistirMovimiento(Movimiento.crearTransferenciaRecibida(canal, monto, detalle,
                        numeroCuenta, cuentaDestino.numeroCuenta));
            }
        }
    }

    protected synchronized void cambiarEstado(EstadoCuenta nuevoEstado) throws DominioException {
        cambiarEstado(nuevoEstado, "Cambio de estado existente");
    }

    protected synchronized void cambiarEstado(EstadoCuenta nuevoEstado, String motivo) throws DominioException {
        if (nuevoEstado == null)
            throw new DominioException("El nuevo estado de la cuenta no puede ser nulo.");
        validarMotivo(motivo);
        estado = nuevoEstado;
    }

    private void validarMotivo(String motivo) throws DominioException {
        if (motivo == null || motivo.trim().length() < 10)
            throw new DominioException("El motivo es obligatorio y debe tener al menos 10 caracteres.");
    }
}