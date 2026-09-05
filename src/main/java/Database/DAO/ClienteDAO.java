package Database.DAO;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import Model.Cliente;
import Model.Direccion;
import Model.TipoIdentificacion;
import Model.Exceptions.DominioException;

public class ClienteDAO {
    private final Connection conexion;
    private static final String COLUMNAS = "id, tipo_identificacion, documento, nombres, apellidos, fecha_nacimiento, direccion, telefono, correo";

    public ClienteDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public int insertar(Cliente cliente) throws DominioException {
        String sql = "INSERT INTO clientes (tipo_identificacion, documento, nombres, apellidos, fecha_nacimiento, direccion, telefono, correo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getTipoIdentificacion().name());
            ps.setString(2, cliente.getDocumento());
            ps.setString(3, cliente.getNombres());
            ps.setString(4, cliente.getApellidos());
            ps.setDate(5, Date.valueOf(cliente.getFechaNacimiento()));
            ps.setString(6, cliente.getDireccion().getDireccion());
            ps.setString(7, cliente.getTelefono());
            ps.setString(8, cliente.getCorreo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next())
                    throw new DominioException("MySQL no devolvió el ID del cliente creado.");
                int id = rs.getInt(1);
                cliente.setId(id);
                return id;
            }
        } catch (SQLException e) {
            throw new DominioException("No fue posible insertar el cliente.", e);
        }
    }

    public Cliente buscarPorDocumento(TipoIdentificacion tipo, String documento) throws DominioException {
        String sql = "SELECT " + COLUMNAS + " FROM clientes WHERE tipo_identificacion = ? AND documento = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, tipo.name());
            ps.setString(2, documento.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DominioException("No fue posible buscar el cliente por documento.", e);
        }
    }

    public Cliente buscarPorId(int id) throws DominioException {
        String sql = "SELECT " + COLUMNAS + " FROM clientes WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DominioException("No fue posible cargar el titular de la cuenta.", e);
        }
    }

    public boolean existe(String documento) throws DominioException {
        try (PreparedStatement ps = conexion.prepareStatement("SELECT COUNT(*) FROM clientes WHERE documento = ?")) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DominioException("No fue posible comprobar el documento del cliente.", e);
        }
    }

    public List<Cliente> listarTodos() throws DominioException {
        List<Cliente> clientes = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement("SELECT " + COLUMNAS + " FROM clientes ORDER BY id");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                clientes.add(mapear(rs));
            return clientes;
        } catch (SQLException e) {
            throw new DominioException("No fue posible listar los clientes.", e);
        }
    }

    public void actualizar(Cliente cliente) throws DominioException {
        try (PreparedStatement ps = conexion
                .prepareStatement("UPDATE clientes SET telefono = ?, correo = ?, direccion = ? WHERE id = ?")) {
            ps.setString(1, cliente.getTelefono());
            ps.setString(2, cliente.getCorreo());
            ps.setString(3, cliente.getDireccion().getDireccion());
            ps.setInt(4, cliente.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DominioException("No fue posible actualizar el cliente.", e);
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException, DominioException {
        Cliente cliente = new Cliente(TipoIdentificacion.valueOf(rs.getString("tipo_identificacion")),
                rs.getString("documento"), rs.getString("nombres"), rs.getString("apellidos"),
                rs.getDate("fecha_nacimiento").toLocalDate(), new Direccion(rs.getString("direccion")),
                rs.getString("telefono"), rs.getString("correo"));
        cliente.setId(rs.getInt("id"));
        return cliente;
    }
}