package Controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

import Model.Banco;
import Model.Cliente;
import Model.Exceptions.DominioException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;

/** Controlador del formulario para editar la información permitida de un cliente. */
public class EditarClienteController implements BancoAware {

    // =========================================================
    // MODELO Y NAVEGACIÓN
    // =========================================================

    private Banco banco;
    private BorderPane ventanaPrincipal;
    private Cliente cliente;

    // =========================================================
    // DATOS NO EDITABLES
    // =========================================================

    @FXML
    private Label lblTipoIdentificacion;
    @FXML
    private Label lblDocumento;
    @FXML
    private Label lblNombres;
    @FXML
    private Label lblApellidos;
    @FXML
    private Label lblFechaNacimiento;

    // =========================================================
    // DATOS EDITABLES
    // =========================================================

    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtCorreo;

    // =========================================================
    // BOTONES
    // =========================================================

    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnCancelar;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // =========================================================
    // INICIALIZACIÓN Y DATOS RECIBIDOS
    // =========================================================

    @FXML
    public void initialize() {
        // La información se carga cuando el controlador recibe el cliente.
        configurarCamposTexto();
    }

    // Restringe el teléfono a números antes de enviarlo al modelo.
    private void configurarCamposTexto() {
        txtTelefono.setTextFormatter(new TextFormatter<String>(cambio ->
                cambio.getControlNewText().matches("\\d*") ? cambio : null));
    }

    @Override
    public void setBanco(Banco banco) {
        this.banco = banco;
    }

    @Override
    public void setVentanaPrincipal(BorderPane ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;
    }

    /** Recibe el cliente seleccionado y carga sus datos en el formulario. */
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
        cargarDatos();
    }

    /** Presenta los datos protegidos y los valores actuales que sí se pueden editar. */
    private void cargarDatos() {
        if (cliente == null)
            return;

        lblTipoIdentificacion.setText(cliente.getTipoIdentificacion().getDescripcion());
        lblDocumento.setText(cliente.getDocumento());
        lblNombres.setText(cliente.getNombres());
        lblApellidos.setText(cliente.getApellidos());
        lblFechaNacimiento.setText(cliente.getFechaNacimiento().format(FORMATO_FECHA));
        txtDireccion.setText(cliente.getDireccion().getDireccion());
        txtTelefono.setText(cliente.getTelefono());
        txtCorreo.setText(cliente.getCorreo());
    }

    // =========================================================
    // GUARDAR CAMBIOS
    // =========================================================

    @FXML
    private void guardarCambios() {
        if (cliente == null) {
            mostrarError("Error del sistema", "No se ha seleccionado un cliente.");
            return;
        }
        if (campoVacio(txtDireccion) || campoVacio(txtTelefono) || campoVacio(txtCorreo)) {
            mostrarError("Datos incompletos", "Todos los campos editables son obligatorios.");
            return;
        }

        try {
            cliente.getDireccion().cambiarDireccion(txtDireccion.getText().trim());
            cliente.cambiarTelefono(txtTelefono.getText().trim());
            cliente.cambiarCorreo(txtCorreo.getText().trim());
            mostrarInformacion("Información actualizada", "Datos actualizados correctamente");
            volverAClientes();
        } catch (DominioException e) {
            mostrarError("No se pudieron actualizar los datos", e.getMessage());
        }
    }

    /** Determina si un campo no contiene información útil para el usuario. */
    private boolean campoVacio(TextField campo) {
        return campo.getText() == null || campo.getText().trim().isEmpty();
    }

    // =========================================================
    // CANCELAR Y REGRESAR
    // =========================================================

    @FXML
    private void cancelar() {
        volverAClientes();
    }

    /** Carga nuevamente la vista de clientes en el contenedor principal. */
    private void volverAClientes() {
        if (ventanaPrincipal == null) {
            mostrarError("Error del sistema", "No se ha inicializado la ventana principal.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/clientes.fxml"));
            Node vista = loader.load();
            ClientesController controller = loader.getController();
            controller.setBanco(banco);
            controller.setVentanaPrincipal(ventanaPrincipal);
            controller.mostrarClienteSeleccionado(cliente);
            ventanaPrincipal.setCenter(vista);
        } catch (IOException | RuntimeException e) {
            mostrarError("Error del sistema", "No se pudo cargar la vista de clientes.");
        }
    }

    // =========================================================
    // MENSAJES
    // =========================================================

    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
