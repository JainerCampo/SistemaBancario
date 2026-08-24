package Model;

import Model.Exceptions.DominioException;
import Model.Exceptions.SaldoInsuficienteException;

public class CuentaCorriente extends Cuenta {
    private final double limiteSobregiro;

    public CuentaCorriente(Cliente titular, double limiteSobregiro) throws DominioException {
        super(titular);
        if (!Double.isFinite(limiteSobregiro) || limiteSobregiro <= 0) {
            throw new DominioException("El límite de sobregiro debe ser mayor que cero.");
        }
        this.limiteSobregiro = limiteSobregiro;
    }

    public double getLimiteSobregiro() {
        return limiteSobregiro;
    }

    @Override
    protected void validarRetiro(double monto) throws DominioException {
        validarOperacionActiva();
        if (!Double.isFinite(monto) || monto <= 0) {
            throw new DominioException("El monto debe ser mayor que cero.");
        }
        if (monto > getSaldo() + limiteSobregiro) {
            throw new SaldoInsuficienteException(
                    "El monto supera el saldo disponible y el límite de sobregiro.");
        }
    }
}