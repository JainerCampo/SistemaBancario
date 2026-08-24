package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import Model.Exceptions.ClienteDuplicadoException;
import Model.Exceptions.CuentaDuplicadaException;
import Model.Exceptions.DominioException;

public class Banco { 
    private final ArrayList<Cliente> clientes = new ArrayList<>();
    private final ArrayList<Cuenta> cuentas = new ArrayList<>();

    public synchronized Cliente buscarCliente(TipoIdentificacion tipoIdentificacion, String documento)
            throws DominioException {
        if (tipoIdentificacion == null)
            throw new DominioException("El tipo de identificación no puede ser nulo.");
        if (documento == null || documento.isBlank())
            throw new DominioException("El documento no puede ser nulo o vacío.");
        for (Cliente cliente : clientes)
            if (cliente.getTipoIdentificacion() == tipoIdentificacion
                    && cliente.getDocumento().equals(documento.trim()))
                return cliente;
        return null;
    }

    public synchronized void agregarCliente(Cliente cliente) throws DominioException {
        if (cliente == null)
            throw new DominioException("El cliente no puede ser nulo.");
        if (buscarCliente(cliente.getTipoIdentificacion(), cliente.getDocumento()) != null)
            throw new ClienteDuplicadoException("El cliente ya existe.");
        clientes.add(cliente);
    }

    public synchronized CuentaAhorros crearCuentaAhorros(TipoIdentificacion tipo, String documento, double tasaInteres)
            throws DominioException {
        Cliente titular = exigirCliente(tipo, documento);
        CuentaAhorros cuenta = new CuentaAhorros(titular, tasaInteres);
        registrarCuenta(titular, cuenta);
        return cuenta;
    }

    public synchronized CuentaCorriente crearCuentaCorriente(TipoIdentificacion tipo, String documento,
            double limiteSobregiro) throws DominioException {
        Cliente titular = exigirCliente(tipo, documento);
        CuentaCorriente cuenta = new CuentaCorriente(titular, limiteSobregiro);
        registrarCuenta(titular, cuenta);
        return cuenta;
    }

    private Cliente exigirCliente(TipoIdentificacion tipo, String documento) throws DominioException {
        Cliente cliente = buscarCliente(tipo, documento);
        if (cliente == null)
            throw new DominioException("El cliente no existe.");
        return cliente;
    }

    private synchronized void registrarCuenta(Cliente titular, Cuenta cuenta) throws DominioException {
        if (buscarCuentaBanco(cuenta.getNumeroCuenta()) != null)
            throw new CuentaDuplicadaException("La cuenta ya existe.");
        titular.agregarCuentaCliente(cuenta);
        cuentas.add(cuenta);
    }

    public synchronized Cuenta buscarCuentaBanco(String numeroCuenta) throws DominioException {
        if (numeroCuenta == null || numeroCuenta.isBlank())
            throw new DominioException("El número de cuenta no puede ser nulo o vacío.");
        for (Cuenta cuenta : cuentas)
            if (cuenta.getNumeroCuenta().equals(numeroCuenta.trim()))
                return cuenta;
        return null;
    }

    public void transferir(String origen, String destino, double monto, CanalMovimiento canal, String descripcion)
            throws DominioException {
        Cuenta cuentaOrigen = buscarCuentaBanco(origen);
        if (cuentaOrigen == null)
            throw new DominioException("La cuenta origen no existe.");
        Cuenta cuentaDestino = buscarCuentaBanco(destino);
        if (cuentaDestino == null)
            throw new DominioException("La cuenta destino no existe.");
        cuentaOrigen.transferir(cuentaDestino, monto, canal, descripcion);
    }

    public void cambiarEstadoCuenta(String numeroCuenta, EstadoCuenta nuevoEstado) throws DominioException {
        Cuenta cuenta = buscarCuentaBanco(numeroCuenta);
        if (cuenta == null)
            throw new DominioException("La cuenta no existe.");
        cuenta.cambiarEstado(nuevoEstado);
    }

    public synchronized List<Cliente> listarClientes() {
        return Collections.unmodifiableList(new ArrayList<>(clientes));
    }

    public synchronized List<Cuenta> listarCuentas() {
        return Collections.unmodifiableList(new ArrayList<>(cuentas));
    }

    public synchronized List<Cuenta> obtenerCuentasPorCliente(String documento) throws DominioException {
        if (documento == null || documento.isBlank())
            throw new DominioException("El documento no puede ser nulo o vacío.");
        ArrayList<Cuenta> resultado = new ArrayList<>();
        for (Cuenta cuenta : cuentas)
            if (cuenta.getTitular().getDocumento().equals(documento.trim()))
                resultado.add(cuenta);
        return Collections.unmodifiableList(resultado);
    }

    public List<Movimiento> obtenerMovimientosPorCuenta(String numeroCuenta) throws DominioException {
        Cuenta cuenta = buscarCuentaBanco(numeroCuenta);
        if (cuenta == null)
            throw new DominioException("La cuenta no existe.");
        return Collections.unmodifiableList(cuenta.getMovimientos());
    }
}