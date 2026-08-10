package Model;

public class CuentaCorriente extends Cuenta {

    private double limiteSobregiro;

    public CuentaCorriente(Cliente titular, double limiteSobregiro) {

        super(titular);

        if (limiteSobregiro <= 0) {
            throw new IllegalArgumentException(
                    "El límite de sobregiro debe ser mayor que cero.");
        }

        this.limiteSobregiro = limiteSobregiro;
    }

    public double getLimiteSobregiro() {
        return limiteSobregiro;
    }

    @Override
    public void retirar(double monto,
            CanalMovimiento canal,
            String descripcion) {

        // verificar si la cuenta está activa antes de permitir el retiro
        if (getEstado() != EstadoCuenta.ACTIVO) {
            throw new IllegalStateException("La cuenta no está activa.");
        }

        // Verificar si el monto es válido
        if (monto <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor que cero.");
        }

        // Verificar si hay suficiente saldo para realizar el retiro
        if (monto > getSaldo() + limiteSobregiro) {
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
}