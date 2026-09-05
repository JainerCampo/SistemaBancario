package Database.DAO;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import Model.*;
import Model.Exceptions.DominioException;

public class MovimientoDAO {
    private final Connection conexion;

    public MovimientoDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public void insertar(Movimiento movimiento, String numeroCuenta) throws DominioException {
        String sql = "INSERT INTO movimientos (cuenta_id, tipo_movimiento, canal, monto, fecha, hora, descripcion, cuenta_origen, cuenta_destino) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, numeroCuenta);
            ps.setString(2, movimiento.getTipoMovimiento().name());
            ps.setString(3, movimiento.getCanalMovimiento().name());
            ps.setDouble(4, movimiento.getMonto());
            ps.setDate(5, Date.valueOf(movimiento.getFecha()));
            ps.setTime(6, Time.valueOf(movimiento.getHora()));
            ps.setString(7, movimiento.getDescripcion());
            ps.setString(8, movimiento.getCuentaOrigen());
            ps.setString(9, movimiento.getCuentaDestino());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next())
                    throw new DominioException("MySQL no devolvió el ID del movimiento.");
                movimiento.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new DominioException("No fue posible insertar el movimiento.", e);
        }
    }

    public List<Movimiento> listarPorCuenta(String numeroCuenta) throws DominioException {
        return listar(numeroCuenta, null);
    }

    public List<Movimiento> listarPorCuentaLimit(String numeroCuenta, int limite) throws DominioException {
        if (limite <= 0)
            return List.of();
        return listar(numeroCuenta, limite);
    }

    private List<Movimiento> listar(String numeroCuenta, Integer limite) throws DominioException {
        String sql = "SELECT id, tipo_movimiento, canal, monto, fecha, hora, descripcion, cuenta_origen, cuenta_destino FROM movimientos WHERE cuenta_id = ? ORDER BY fecha DESC, hora DESC"
                + (limite == null ? "" : " LIMIT ?");
        List<Movimiento> resultado = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, numeroCuenta);
            if (limite != null)
                ps.setInt(2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw new DominioException("No fue posible listar los movimientos.", e);
        }
    }

    private Movimiento mapear(ResultSet rs) throws SQLException, DominioException {
        return Movimiento.desdePersistencia(rs.getInt("id"), TipoMovimiento.valueOf(rs.getString("tipo_movimiento")),
                CanalMovimiento.valueOf(rs.getString("canal")), rs.getDouble("monto"),
                rs.getDate("fecha").toLocalDate(), rs.getTime("hora").toLocalTime(), rs.getString("descripcion"),
                rs.getString("cuenta_origen"), rs.getString("cuenta_destino"));
    }
}