package Model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

import Model.Exceptions.DominioException;

public final class Movimiento {
    private static final AtomicInteger siguienteId = new AtomicInteger(1);
    private final int id;
    private final TipoMovimiento tipoMovimiento;
    private final CanalMovimiento canalMovimiento;
    private final double monto;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final String descripcion;
    private final String cuentaOrigen;
    private final String cuentaDestino;

    private Movimiento(TipoMovimiento tipoMovimiento, CanalMovimiento canalMovimiento, double monto,
            String descripcion, String cuentaOrigen, String cuentaDestino) throws DominioException {
        if (tipoMovimiento == null) throw new DominioException("El tipo de movimiento es obligatorio.");
        if (canalMovimiento == null) throw new DominioException("El canal del movimiento es obligatorio.");
        if (!Double.isFinite(monto) || monto <= 0) throw new DominioException("El monto debe ser mayor que cero.");
        if (descripcion == null || descripcion.isBlank()) throw new DominioException("La descripción es obligatoria.");
        this.id = siguienteId.getAndIncrement();
        this.tipoMovimiento = tipoMovimiento;
        this.canalMovimiento = canalMovimiento;
        this.monto = monto;
        this.descripcion = descripcion.trim();
        this.fecha = LocalDate.now();
        this.hora = LocalTime.now();
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
    }

    public int getId() { return id; }
    public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
    public CanalMovimiento getCanalMovimiento() { return canalMovimiento; }
    public double getMonto() { return monto; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public String getDescripcion() { return descripcion; }
    public String getCuentaOrigen() { return cuentaOrigen; }
    public String getCuentaDestino() { return cuentaDestino; }

    public static Movimiento crearConsignacion(CanalMovimiento canal, double monto, String descripcion)
            throws DominioException {
        return new Movimiento(TipoMovimiento.CONSIGNACION, canal, monto, descripcion, null, null);
    }

    public static Movimiento crearRetiro(CanalMovimiento canal, double monto, String descripcion)
            throws DominioException {
        return new Movimiento(TipoMovimiento.RETIRO, canal, monto, descripcion, null, null);
    }

    public static Movimiento crearInteresesGenerados(double monto, String descripcion)
            throws DominioException {
        return new Movimiento(TipoMovimiento.INTERESES_GENERADOS, CanalMovimiento.SISTEMA,
                monto, descripcion, null, null);
    }

    public static Movimiento crearTransferenciaEnviada(CanalMovimiento canal, double monto,
            String descripcion, String cuentaOrigen, String cuentaDestino) throws DominioException {
        return new Movimiento(TipoMovimiento.TRANSFERENCIA_ENVIADA, canal, monto, descripcion,
                cuentaOrigen, cuentaDestino);
    }

    public static Movimiento crearTransferenciaRecibida(CanalMovimiento canal, double monto,
            String descripcion, String cuentaOrigen, String cuentaDestino) throws DominioException {
        return new Movimiento(TipoMovimiento.TRANSFERENCIA_RECIBIDA, canal, monto, descripcion,
                cuentaOrigen, cuentaDestino);
    }

    public static Movimiento crearTransferenciaEnviada(CanalMovimiento canal, double monto,
            String descripcion) throws DominioException {
        return crearTransferenciaEnviada(canal, monto, descripcion, null, null);
    }

    public static Movimiento crearTransferenciaRecibida(CanalMovimiento canal, double monto,
            String descripcion) throws DominioException {
        return crearTransferenciaRecibida(canal, monto, descripcion, null, null);
    }

    @Override
    public String toString() {
        return "Tipo: " + tipoMovimiento.getDescripcion() + "\n"
                + "Canal: " + canalMovimiento.getDescripcion() + "\n"
                + "Monto: $" + monto + "\n"
                + "Fecha: " + fecha + "\n"
                + "Hora: " + hora + "\n"
                + "Descripción: " + descripcion;
    }
}