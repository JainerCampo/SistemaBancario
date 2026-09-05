package Database.DAO;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Model.*;
import Model.Exceptions.DominioException;

public class CuentaDAO {
    private final Connection conexion;
    private final ClienteDAO clienteDAO;
    private final MovimientoDAO movimientoDAO;

    public CuentaDAO(Connection conexion) {
        this.conexion = conexion;
        this.clienteDAO = new ClienteDAO(conexion);
        this.movimientoDAO = new MovimientoDAO(conexion);
    }

    public MovimientoDAO getMovimientoDAO() {
        return movimientoDAO;
    }

    public void insertar(Cuenta cuenta, int clienteId) throws DominioException {
        String sql = "INSERT INTO cuentas (cliente_id, tipo_cuenta, saldo, estado, fecha_apertura, tasa_interes, limite_sobregiro) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, clienteId);
            ps.setString(2, cuenta instanceof CuentaAhorros ? "AHORROS" : "CORRIENTE");
            ps.setBigDecimal(3, BigDecimal.valueOf(cuenta.getSaldo()));
            ps.setString(4, cuenta.getEstado().name());
            ps.setDate(5, Date.valueOf(cuenta.getFechaApertura()));
            if (cuenta instanceof CuentaAhorros a) {
                ps.setBigDecimal(6, BigDecimal.valueOf(a.getTasaInteres()));
                ps.setNull(7, Types.DECIMAL);
            } else {
                ps.setNull(6, Types.DECIMAL);
                ps.setBigDecimal(7, BigDecimal.valueOf(((CuentaCorriente) cuenta).getLimiteSobregiro()));
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next())
                    throw new DominioException("MySQL no devolvió el número de cuenta generado.");
                cuenta.asignarNumeroCuenta(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new DominioException("No fue posible insertar la cuenta.", e);
        }
        cuenta.asignarMovimientoDAO(movimientoDAO);
    }

    public Cuenta buscarPorNumero(String numeroCuenta) throws DominioException {
        return buscar(numeroCuenta, false);
    }

    public Cuenta buscarPorNumeroBloqueado(String numeroCuenta) throws DominioException {
        return buscar(numeroCuenta, true);
    }

    private Cuenta buscar(String numeroCuenta, boolean bloquear) throws DominioException {
        String sql = "SELECT numero_cuenta, cliente_id, tipo_cuenta, saldo, estado, fecha_apertura, tasa_interes, limite_sobregiro FROM cuentas WHERE numero_cuenta = ?"
                + (bloquear ? " FOR UPDATE" : "");
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, numeroCuenta);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DominioException("No fue posible buscar la cuenta.", e);
        }
    }

    public List<Cuenta> buscarPorCliente(int clienteId) throws DominioException {
        List<Cuenta> resultado = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT numero_cuenta, cliente_id, tipo_cuenta, saldo, estado, fecha_apertura, tasa_interes, limite_sobregiro FROM cuentas WHERE cliente_id = ? ORDER BY numero_cuenta")) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw new DominioException("No fue posible listar las cuentas del cliente.", e);
        }
    }

    public void actualizarSaldo(String numeroCuenta, double nuevoSaldo) throws DominioException {
        ejecutar("UPDATE cuentas SET saldo = ? WHERE numero_cuenta = ?", ps -> {
            ps.setBigDecimal(1, BigDecimal.valueOf(nuevoSaldo));
            ps.setString(2, numeroCuenta);
        });
    }

    public void cambiarEstado(String numeroCuenta, EstadoCuenta estado) throws DominioException {
        ejecutar("UPDATE cuentas SET estado = ? WHERE numero_cuenta = ?", ps -> {
            ps.setString(1, estado.name());
            ps.setString(2, numeroCuenta);
        });
    }

    public List<Cuenta> listarTodas() throws DominioException {
        List<Cuenta> resultado = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT numero_cuenta, cliente_id, tipo_cuenta, saldo, estado, fecha_apertura, tasa_interes, limite_sobregiro FROM cuentas ORDER BY numero_cuenta");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                resultado.add(mapear(rs));
            return resultado;
        } catch (SQLException e) {
            throw new DominioException("No fue posible listar las cuentas.", e);
        }
    }

    private void ejecutar(String sql, SQLConsumer consumer) throws DominioException {
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            consumer.accept(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DominioException("No fue posible actualizar la cuenta.", e);
        }
    }

    private Cuenta mapear(ResultSet rs) throws SQLException, DominioException {
        Cliente cliente = clienteDAO.buscarPorId(rs.getInt("cliente_id"));
        if (cliente == null)
            throw new DominioException("El titular de la cuenta no existe.");
        String numero = rs.getString("numero_cuenta");
        double saldo = rs.getBigDecimal("saldo").doubleValue();
        EstadoCuenta estado = EstadoCuenta.valueOf(rs.getString("estado"));
        java.time.LocalDate fecha = rs.getDate("fecha_apertura").toLocalDate();
        Cuenta cuenta = "AHORROS".equals(rs.getString("tipo_cuenta"))
                ? new CuentaAhorros(cliente, numero, saldo, estado, fecha,
                        rs.getBigDecimal("tasa_interes").doubleValue())
                : new CuentaCorriente(cliente, numero, saldo, estado, fecha,
                        rs.getBigDecimal("limite_sobregiro").doubleValue());
        cuenta.asignarMovimientoDAO(movimientoDAO);
        cuenta.asignarCuentaDAO(this);
        cliente.asignarCuentaDAO(this);
        return cuenta;
    }

    @FunctionalInterface
    private interface SQLConsumer {
        void accept(PreparedStatement ps) throws SQLException;
    }
}