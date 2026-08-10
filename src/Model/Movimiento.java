package Model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Movimiento {
    private static int siguienteId = 1;
    private final int id;
    private final TipoMovimiento tipoMovimiento;
    private final CanalMovimiento canalMovimiento;
    private final double monto;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final String descripcion;

    // constructor privado para crear un movimiento
    private Movimiento(TipoMovimiento tipoMovimiento,
            CanalMovimiento canalMovimiento,
            double monto, String descripcion) {
        this.tipoMovimiento = tipoMovimiento;
        // validar que el canal de movimiento no sea nulo
        if (canalMovimiento == null) {
            throw new IllegalArgumentException("El canal del movimiento es obligatorio.");
        }
        this.canalMovimiento = canalMovimiento;
        // validar que el monto sea mayor que cero
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        }
        this.monto = monto;
        // validar que la descripción no sea nula o vacía
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }
        this.descripcion = descripcion;
        this.fecha = LocalDate.now();
        this.hora = LocalTime.now();
        this.id = siguienteId++; // Incrementa el contador para asignar un ID único a cada movimiento
    }

    // Getters
    public int getId() {
        return id;
    }

    public TipoMovimiento getTipoMovimiento() {
        return tipoMovimiento;
    }

    public CanalMovimiento getCanalMovimiento() {
        return canalMovimiento;
    }

    public double getMonto() {
        return monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public String getDescripcion() {
        return descripcion;
    }

    // Método toString para mostrar la información del movimiento
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("====================================\n")
                .append("Tipo: ").append(tipoMovimiento.getDescripcion()).append("\n")
                .append("Canal: ").append(canalMovimiento.getDescripcion()).append("\n")
                .append("Monto: $").append(monto).append("\n")
                .append("Fecha: ").append(fecha).append("\n")
                .append("Hora: ").append(hora).append("\n")
                .append("Descripción: ").append(descripcion).append("\n")
                .append("====================================");

        return sb.toString();
    }

    // Métodos estáticos para crear movimientos específicos

    // Método para crear un movimiento de consignación
    public static Movimiento crearConsignacion(
            CanalMovimiento canal,
            double monto,
            String descripcion) {
        return new Movimiento(
                TipoMovimiento.CONSIGNACION, canal, monto,
                descripcion);
    }

    // Método para crear un movimiento de retiro
    public static Movimiento crearRetiro(
            CanalMovimiento canal,
            double monto,
            String descripcion) {
        return new Movimiento(TipoMovimiento.RETIRO, canal, monto,
                descripcion);
    }

    // Métodos estáticos para crear movimientos de transferencia

    // Método para crear un movimiento de transferencia enviada
    public static Movimiento crearTransferenciaEnviada(
            CanalMovimiento canal,
            double monto,
            String descripcion) {
        return new Movimiento(
                TipoMovimiento.TRANSFERENCIA_ENVIADA, canal, monto,
                descripcion);
    }

    // Método para crear un movimiento de transferencia recibida
    public static Movimiento crearTransferenciaRecibida(
            CanalMovimiento canal,
            double monto,
            String descripcion) {
        return new Movimiento(
                TipoMovimiento.TRANSFERENCIA_RECIBIDA, canal, monto,
                descripcion);
    }
}