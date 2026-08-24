package Model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import Model.Exceptions.DominioException;

public class CuentaAhorros extends Cuenta {
    private final double tasaInteres;
    private LocalDate fechaUltimoInteres;

    public CuentaAhorros(Cliente titular, double tasaInteres) throws DominioException {
        super(titular);
        if (!Double.isFinite(tasaInteres) || tasaInteres < 0)
            throw new DominioException("La tasa de interés no puede ser negativa.");
        this.tasaInteres = tasaInteres;
        this.fechaUltimoInteres = getFechaApertura();
    }

    public double getTasaInteres() {
        return tasaInteres;
    }

    public synchronized LocalDate getFechaUltimoInteres() {
        return fechaUltimoInteres;
    }

    public synchronized void aplicarIntereses() throws DominioException {
        validarOperacionActiva();
        aplicarInteres(tasaInteres / 100);
        fechaUltimoInteres = LocalDate.now();
    }

    public synchronized void aplicarInteresesDiarios() throws DominioException {
        validarOperacionActiva();
        long dias = ChronoUnit.DAYS.between(fechaUltimoInteres, LocalDate.now());
        if (dias <= 0)
            return;
        double interes = getSaldo() * (tasaInteres / 100) * dias / 365.0;
        if (interes > 0) {
            aumentarSaldo(interes);
            agregarMovimiento(Movimiento.crearInteresesGenerados(interes, "Intereses generados por " + dias + " días"));
        }
        fechaUltimoInteres = LocalDate.now();
    }

    private void aplicarInteres(double tasa) throws DominioException {
        if (tasa <= 0)
            throw new DominioException("No se pueden aplicar intereses con una tasa de cero.");
        if (getSaldo() <= 0)
            throw new DominioException("No se pueden aplicar intereses con saldo igual a cero.");
        double interes = getSaldo() * tasa;
        aumentarSaldo(interes);
        agregarMovimiento(Movimiento.crearInteresesGenerados(interes, "Intereses generados"));
    }
}