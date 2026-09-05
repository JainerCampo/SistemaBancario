package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import Model.Exceptions.DominioException;

public final class ConexionBD {
    private static final String URL = "jdbc:mysql://localhost:3306/danibanca?useSSL=false&serverTimezone=UTC";
    private static final String USUARIO = "root";
    private static final String CLAVE = "root";
    private static Connection conexion;

    private ConexionBD() {
    }

    public static synchronized Connection getInstance() throws DominioException {
        try {
            if (conexion == null || conexion.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conexion = DriverManager.getConnection(URL, USUARIO, CLAVE);
            }
            return conexion;
        } catch (ClassNotFoundException e) {
            throw new DominioException("No se encontró el driver JDBC de MySQL.", e);
        } catch (SQLException e) {
            throw new DominioException("No fue posible conectar con MySQL en la base de datos danibanca.", e);
        }
    }

    public static synchronized void cerrar() throws DominioException {
        if (conexion == null)
            return;
        try {
            conexion.close();
            conexion = null;
        } catch (SQLException e) {
            throw new DominioException("No fue posible cerrar la conexión con MySQL.", e);
        }
    }
}