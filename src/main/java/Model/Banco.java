package Model;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import Database.ConexionBD;
import Database.DAO.ClienteDAO;
import Database.DAO.CuentaDAO;
import Database.DAO.MovimientoDAO;
import Model.Exceptions.ClienteDuplicadoException;
import Model.Exceptions.DominioException;

public class Banco {
    private final Connection conexion;
    private final ClienteDAO clienteDAO;
    private final CuentaDAO cuentaDAO;
    private final MovimientoDAO movimientoDAO;

    public Banco(Connection conexion) {
        this.conexion = conexion;
        this.clienteDAO = new ClienteDAO(conexion);
        this.cuentaDAO = new CuentaDAO(conexion);
        this.movimientoDAO = new MovimientoDAO(conexion);
    }

    public Banco() throws DominioException {
        this(ConexionBD.getInstance());
    }

    public synchronized Cliente buscarCliente(TipoIdentificacion tipo, String documento) throws DominioException {
        if (tipo == null)
            throw new DominioException("El tipo de identificación no puede ser nulo.");
        if (documento == null || documento.isBlank())
            throw new DominioException("El documento no puede ser nulo o vacío.");
        Cliente cliente = clienteDAO.buscarPorDocumento(tipo, documento);
        if (cliente != null)
            cliente.asignarCuentaDAO(cuentaDAO);
        return cliente;
    }

    public synchronized void agregarCliente(Cliente cliente) throws DominioException {
        if (cliente == null)
            throw new DominioException("El cliente no puede ser nulo.");
        if (clienteDAO.existe(cliente.getDocumento()))
            throw new ClienteDuplicadoException("El cliente ya existe.");
        clienteDAO.insertar(cliente);
        cliente.asignarCuentaDAO(cuentaDAO);
    }

    public synchronized CuentaAhorros crearCuentaAhorros(TipoIdentificacion tipo, String documento, double tasaInteres)
            throws DominioException {
        Cliente titular = exigirCliente(tipo, documento);
        CuentaAhorros cuenta = new CuentaAhorros(titular, tasaInteres);
        cuentaDAO.insertar(cuenta, titular.getId());
        cuenta.asignarCuentaDAO(cuentaDAO);
        return cuenta;
    }

    public synchronized CuentaCorriente crearCuentaCorriente(TipoIdentificacion tipo, String documento,
            double limiteSobregiro) throws DominioException {
        Cliente titular = exigirCliente(tipo, documento);
        CuentaCorriente cuenta = new CuentaCorriente(titular, limiteSobregiro);
        cuentaDAO.insertar(cuenta, titular.getId());
        cuenta.asignarCuentaDAO(cuentaDAO);
        return cuenta;
    }

    private Cliente exigirCliente(TipoIdentificacion tipo, String documento) throws DominioException {
        Cliente cliente = buscarCliente(tipo, documento);
        if (cliente == null)
            throw new DominioException("El cliente no existe.");
        return cliente;
    }

    public synchronized Cuenta buscarCuentaBanco(String numeroCuenta) throws DominioException {
        if (numeroCuenta == null || numeroCuenta.isBlank())
            throw new DominioException("El número de cuenta no puede ser nulo o vacío.");
        return cuentaDAO.buscarPorNumero(numeroCuenta.trim());
    }

    public void transferir(String origen, String destino, double monto, CanalMovimiento canal, String descripcion)
            throws DominioException {
        transferir(origen, destino, monto, canal, descripcion, descripcion);
    }

    public void transferir(String origen, String destino, double monto, CanalMovimiento canal, String descripcion,
            String motivo) throws DominioException {
        try {
            conexion.setAutoCommit(false);
            Cuenta cuentaOrigen = cuentaDAO.buscarPorNumeroBloqueado(origen);
            Cuenta cuentaDestino = cuentaDAO.buscarPorNumeroBloqueado(destino);
            if (cuentaOrigen == null)
                throw new DominioException("La cuenta origen no existe.");
            if (cuentaDestino == null)
                throw new DominioException("La cuenta destino no existe.");
            if (cuentaOrigen == cuentaDestino)
                throw new DominioException("La cuenta origen y destino no pueden ser la misma.");
            cuentaOrigen.validarRetiro(monto);
            cuentaDestino.validarOperacionActiva();
            if (canal == null)
                throw new DominioException("El canal del movimiento es obligatorio.");
            if (descripcion == null || descripcion.isBlank())
                throw new DominioException("La descripción es obligatoria.");
            if (motivo == null || motivo.trim().length() < 10)
                throw new DominioException("El motivo es obligatorio y debe tener al menos 10 caracteres.");
            cuentaDAO.actualizarSaldo(origen, cuentaOrigen.getSaldo() - monto);
            cuentaDAO.actualizarSaldo(destino, cuentaDestino.getSaldo() + monto);
            String detalle = descripcion.trim() + " | Motivo: " + motivo.trim();
            movimientoDAO.insertar(Movimiento.crearTransferenciaEnviada(canal, monto, detalle, origen, destino),
                    origen);
            movimientoDAO.insertar(Movimiento.crearTransferenciaRecibida(canal, monto, detalle, origen, destino),
                    destino);
            conexion.commit();
        } catch (Exception e) {
            try {
                conexion.rollback();
            } catch (Exception rollback) {
                e.addSuppressed(rollback);
            }
            if (e instanceof DominioException dominio)
                throw dominio;
            throw new DominioException("Error en transferencia: " + e.getMessage(), e);
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (Exception ignored) {
            }
        }
    }

    public void cambiarEstadoCuenta(String numeroCuenta, EstadoCuenta nuevoEstado) throws DominioException {
        cambiarEstadoCuenta(numeroCuenta, nuevoEstado, "Cambio de estado existente");
    }

    public void cambiarEstadoCuenta(String numeroCuenta, EstadoCuenta nuevoEstado, String motivo)
            throws DominioException {
        Cuenta cuenta = buscarCuentaBanco(numeroCuenta);
        if (cuenta == null)
            throw new DominioException("La cuenta no existe.");
        cuenta.cambiarEstado(nuevoEstado, motivo);
        cuentaDAO.cambiarEstado(numeroCuenta, nuevoEstado);
    }

    public List<Cliente> listarClientes() {
        try {
            List<Cliente> clientes = clienteDAO.listarTodos();
            clientes.forEach(c -> c.asignarCuentaDAO(cuentaDAO));
            return Collections.unmodifiableList(clientes);
        } catch (DominioException e) {
            return List.of();
        }
    }

    public List<Cuenta> listarCuentas() {
        try {
            return Collections.unmodifiableList(cuentaDAO.listarTodas());
        } catch (DominioException e) {
            return List.of();
        }
    }

    public List<Cuenta> obtenerCuentasPorCliente(String documento) throws DominioException {
        Cliente cliente = exigirClientePorDocumento(documento);
        return Collections.unmodifiableList(cuentaDAO.buscarPorCliente(cliente.getId()));
    }

    public List<Movimiento> obtenerMovimientosPorCuenta(String numeroCuenta) throws DominioException {
        if (buscarCuentaBanco(numeroCuenta) == null)
            throw new DominioException("La cuenta no existe.");
        return Collections.unmodifiableList(movimientoDAO.listarPorCuenta(numeroCuenta));
    }

    private Cliente exigirClientePorDocumento(String documento) throws DominioException {
        if (documento == null || documento.isBlank())
            throw new DominioException("El documento no puede ser nulo o vacío.");
        for (TipoIdentificacion tipo : TipoIdentificacion.values()) {
            Cliente c = buscarCliente(tipo, documento);
            if (c != null)
                return c;
        }
        throw new DominioException("El cliente no existe.");
    }
}
