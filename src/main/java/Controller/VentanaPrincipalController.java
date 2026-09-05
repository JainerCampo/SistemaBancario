package Controller;

import java.io.IOException;

import Model.Banco;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Controlador de la ventana de navegación principal.
 */
public class VentanaPrincipalController {

    /** Contenedor raíz donde se reemplaza el contenido de cada módulo. */
    @FXML
    private BorderPane ventanaPrincipal;

    /** Contenedor que conserva el panel de bienvenida inicial. */
    @FXML
    private StackPane centerContent;

    /** Panel mostrado al entrar en la opción Inicio. */
    @FXML
    private VBox panelInicio;

    /** Texto superior que identifica el módulo actualmente seleccionado. */
    @FXML
    private Label lblTituloVista;

    /** Botones disponibles en el menú lateral. */
    @FXML
    private Button btnInicio;
    @FXML
    private Button btnClientes;
    @FXML
    private Button btnCuentas;
    @FXML
    private Button btnTransacciones;
    @FXML
    private Button btnUsuarios;
    @FXML
    private Button btnCerrarSesion;

    /** Banco único inyectado por la clase Main al crear esta ventana. */
    private Banco banco;

    /** Botón que conserva actualmente el estilo de opción activa. */
    private Button botonActivo;

    /**
     * Inicializa el estado visual del menú. El banco se recibe después por
     * setter desde la clase Main, porque FXML crea primero el controlador.
     */
    @FXML
    public void initialize() {
        botonActivo = btnInicio;
    }

    /**
     * Inyecta el banco compartido por toda la aplicación.
     * Se invoca desde Main inmediatamente después de cargar el FXML.
     */
    public void setBanco(Banco banco) {
        this.banco = banco;
    }

    /**
     * Devuelve el banco compartido para conservar compatibilidad con el flujo
     * existente y permitir que otros controladores consulten esa instancia.
     */
    public Banco getBanco() {
        return banco;
    }

    /**
     * Devuelve el contenedor principal. Lo usan controladores secundarios para
     * regresar a una vista anterior sin crear otra ventana.
     */
    public BorderPane getVentanaPrincipal() {
        return ventanaPrincipal;
    }

    /**
     * Carga cualquier vista cuyo controlador implemente BancoAware, inyecta el
     * banco y el contenedor principal, y la coloca en el centro de la ventana.
     * Centralizar este flujo evita duplicar código y garantiza el mismo estado.
     */
    private <T extends BancoAware> void cargarVista(String fxmlPath,
            Class<T> controllerClass) {
        if (banco == null) {
            mostrarError("El banco no ha sido inicializado.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node vista = loader.load();
            T controller = controllerClass.cast(loader.getController());
            controller.setBanco(banco);
            controller.setVentanaPrincipal(ventanaPrincipal);
            ventanaPrincipal.setCenter(vista);
        } catch (IOException | RuntimeException e) {
            mostrarError("No se pudo cargar la vista " + fxmlPath + ": " + e.getMessage());
        }
    }

    /** Muestra el panel inicial y activa visualmente el botón Inicio. */
    @FXML
    private void mostrarInicio() {
        activarBoton(btnInicio);
        lblTituloVista.setText("Inicio");
        ventanaPrincipal.setCenter(centerContent);
        centerContent.getChildren().setAll(panelInicio);
    }

    /** Carga el módulo de clientes cuando el usuario pulsa Clientes. */
    @FXML
    private void mostrarClientes() {
        activarBoton(btnClientes);
        lblTituloVista.setText("Clientes");
        cargarVista("/View/clientes.fxml", ClientesController.class);
    }

    /** Cuentas administra productos y no ejecuta movimientos financieros. */
    @FXML
    private void mostrarCuentas() {
        activarBoton(btnCuentas);
        lblTituloVista.setText("Cuentas");
        cargarVista("/View/cuentas.fxml", CuentasController.class);
    }

    /** Transacciones ejecuta operaciones y conserva su historial de sesión. */
    @FXML
    private void mostrarTransacciones() {
        activarBoton(btnTransacciones);
        lblTituloVista.setText("Transacciones");
        cargarVista("/View/transacciones.fxml", TransaccionesController.class);
    }

    /** Deja preparado el acceso al módulo de usuarios aún no implementado. */
    @FXML
    private void mostrarUsuarios() {
        activarBoton(btnUsuarios);
        lblTituloVista.setText("Usuarios");
        mostrarErrorVistaPendiente("/View/usuarios.fxml");
    }

    /** Cierra la aplicación al terminar explícitamente la sesión. */
    @FXML
    private void cerrarSesion() {
        System.exit(0);
    }

    /** Aplica el estilo activo y restaura el estilo del botón anterior. */
    private void activarBoton(Button boton) {
        if (botonActivo != null) {
            botonActivo.setStyle("-fx-background-color: transparent; -fx-background-radius: 6px;");
        }
        boton.setStyle("-fx-background-color: #1D527B; -fx-background-radius: 6px;");
        botonActivo = boton;
    }

    /** Informa de forma uniforme cuando una vista todavía no está creada. */
    private void mostrarErrorVistaPendiente(String ruta) {
        mostrarError("La vista " + ruta + " aún no está disponible.");
    }

    /** Muestra errores de navegación sin depender de la consola. */
    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error del sistema");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}