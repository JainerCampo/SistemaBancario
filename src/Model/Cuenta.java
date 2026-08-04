package Model;

import java.util.ArrayList;

public abstract class Cuenta {
    private String numeroCuenta;
    private double saldo;
    private enum estado{activo, inactivo, bloquedo, suspedido, cancelado }
    private Cliente cliente;
    private ArrayList<Movimiento> movimientos;

    /*
    consignar()
    retirar()
    agregarMovimiento()
    mostrarMovimientos()
    consultarSaldo() 
    */
}
