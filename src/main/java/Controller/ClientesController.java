package Controller;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import Model.Banco;
import Model.Cliente;
import Model.Cuenta;
import Model.CuentaAhorros;
import Model.CuentaCorriente;
import Model.TipoIdentificacion;
import Model.Exceptions.DominioException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Controlador de la vista de gestión y búsqueda de clientes.
 *
 * La clase coordina la interacción de JavaFX con Banco, pero conserva las
 * reglas de negocio dentro del modelo. Las ayudas visuales, como el contador
 * de cuentas y el formato de fecha, se resuelven aquí porque pertenecen a la
 * presentación y no al dominio.
 */
public class ClientesController implements Initializable, BancoAware {

    /** Selector del tipo de identificación usado en la búsqueda. */
    @FXML
    private ComboBox<TipoIdentificacion> cbTipoIdentificacion;

    /** Campo donde el usuario escribe el documento que desea buscar. */
    @FXML
    private TextField txtDocumento;

    /** Botones de búsqueda y limpieza del formulario. */
    @FXML
    private Button btnBuscarCliente;
    @FXML
    private Button btnLimpiar;

    /** Paneles que alternan el resultado encontrado y el estado vacío. */
    @FXML
    private VBox panelClienteEncontrado;
    @FXML
    private VBox panelClienteNoEncontrado;

    /** Etiquetas que presentan los datos personales del cliente encontrado. */
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
    @FXML
    private Label lblDireccion;
    @FXML
    private Label lblTelefono;
    @FXML
    private Label lblCorreo;

    /** Tabla y columnas que muestran las cuentas del cliente. */
    @FXML
    private TableView<Cuenta> tablaCuentas;
    @FXML
    private TableColumn<Cuenta, String> colNumeroCuenta;
    @FXML
    private TableColumn<Cuenta, String> colTipoCuenta;
    @FXML
    private TableColumn<Cuenta, String> colEstadoCuenta;
    @FXML
    private TableColumn<Cuenta, Double> colSaldo;

    /** Mensaje del estado sin resultados y botón para registrar un cliente. */
    @FXML
    private Label lblMensajeNoEncontrado;
    @FXML
    private Button btnRegistrarCliente;

    /** Acciones disponibles para el cliente que se está mostrando. */
    @FXML
    private Button btnEditarCliente;
    @FXML
    private Button btnNuevaCuenta;

    /** Banco compartido que recibe el controlador desde la ventana principal. */
    private Banco banco;

    /** BorderPane principal usado para reemplazar la vista central. */
    private BorderPane ventanaPrincipal;

    /** Cliente actualmente encontrado y seleccionado para las acciones. */
    private Cliente clienteActual;

    /** Etiqueta creada por código para mostrar el total de cuentas asociadas. */
    private Label lblContadorCuentas;

    /** Formato uniforme para presentar fechas al usuario. */
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Configura controles, tabla y estado inicial cada vez que FXMLLoader crea
     * esta vista. El orden garantiza que el ComboBox tenga opciones antes de
     * asignarle CC y que la etiqueta dinámica se agregue una sola vez.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarComboBox();
        cbTipoIdentificacion.setValue(TipoIdentificacion.CC);
        configurarDocumento();
        configurarTabla();
        configurarEventosTabla();
        crearContadorCuentas();
        mostrarPanelInicial();
    }

    /**
     * Restringe el documento a dígitos y habilita Enter como atajo de búsqueda.
     * La validación inmediata evita que el usuario llegue al modelo con letras
     * y hace que la acción frecuente de buscar no requiera usar el mouse.
     */
    private void configurarDocumento() {
        txtDocumento.setTextFormatter(
                new TextFormatter<String>(cambio -> cambio.getControlNewText().matches("\\d*") ? cambio : null));
        txtDocumento.setOnAction(evento -> buscarCliente());
    }

    /**
     * Carga los tipos del enum y muestra sus descripciones legibles tanto en
     * la lista desplegable como en el valor seleccionado.
     */
    private void configurarComboBox() {
        cbTipoIdentificacion.setItems(FXCollections.observableArrayList(
                TipoIdentificacion.values()));
        cbTipoIdentificacion.setCellFactory(combo -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(TipoIdentificacion tipo, boolean vacio) {
                super.updateItem(tipo, vacio);
                setText(vacio || tipo == null ? null : tipo.getDescripcion());
            }
        });
        cbTipoIdentificacion.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(TipoIdentificacion tipo, boolean vacio) {
                super.updateItem(tipo, vacio);
                setText(vacio || tipo == null ? null : tipo.getDescripcion());
            }
        });
    }

    /**
     * Configura las columnas y el placeholder de la tabla. El placeholder
     * explica el estado vacío sin depender de datos ficticios en el modelo.
     */
    private void configurarTabla() {
        colNumeroCuenta.setCellValueFactory(new PropertyValueFactory<>("numeroCuenta"));
        colTipoCuenta.setCellValueFactory(datos -> {
            Cuenta cuenta = datos.getValue();
            String tipo = cuenta instanceof CuentaAhorros ? "Cuenta de ahorros"
                    : cuenta instanceof CuentaCorriente ? "Cuenta corriente" : "Cuenta";
            return new javafx.beans.property.SimpleStringProperty(tipo);
        });
        colEstadoCuenta.setCellValueFactory(datos -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(datos.getValue().getEstado())));
        colSaldo.setCellValueFactory(new PropertyValueFactory<>("saldo"));
        colSaldo.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(Double saldo, boolean vacio) {
                super.updateItem(saldo, vacio);
                setText(vacio || saldo == null ? null : String.format("$%,.2f", saldo));
            }
        });
        tablaCuentas.setPlaceholder(new Label("Este cliente no tiene cuentas registradas."));
    }

    /**
     * Añade el comportamiento de doble clic para consultar rápidamente una
     * cuenta sin crear una vista adicional en esta etapa del proyecto.
     */
    private void configurarEventosTabla() {
        tablaCuentas.addEventHandler(MouseEvent.MOUSE_CLICKED, evento -> {
            if (evento.getClickCount() == 2 && !evento.isConsumed()) {
                Cuenta cuenta = tablaCuentas.getSelectionModel().getSelectedItem();
                if (cuenta != null)
                    mostrarDetalleCuenta(cuenta);
            }
        });
    }

    /**
     * Crea y coloca sobre la tabla el contador solicitado sin modificar el FXML.
     * Se inserta justo antes de la tabla para que permanezca asociado a ella.
     */
    private void crearContadorCuentas() {
        lblContadorCuentas = new Label();
        lblContadorCuentas.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        int indiceTabla = panelClienteEncontrado.getChildren().indexOf(tablaCuentas);
        panelClienteEncontrado.getChildren().add(indiceTabla, lblContadorCuentas);
        actualizarContadorCuentas(0);
    }

    /** Actualiza el texto del contador cada vez que cambia la lista visible. */
    private void actualizarContadorCuentas(int cantidad) {
        lblContadorCuentas.setText(cantidad + (cantidad == 1
                ? " cuenta asociada"
                : " cuentas asociadas"));
    }

    /** Oculta ambos paneles hasta que exista una búsqueda válida. */
    private void mostrarPanelInicial() {
        panelClienteEncontrado.setVisible(false);
        panelClienteEncontrado.setManaged(false);
        panelClienteNoEncontrado.setVisible(false);
        panelClienteNoEncontrado.setManaged(false);
        btnNuevaCuenta.setDisable(true);
    }

    /** Recibe la instancia compartida del banco desde la ventana principal. */
    @Override
    public void setBanco(Banco banco) {
        this.banco = banco;
    }

    /** Recibe el contenedor que permite abrir vistas secundarias. */
    @Override
    public void setVentanaPrincipal(BorderPane ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;
    }

    /** Ejecuta la búsqueda y presenta el cliente o el estado no encontrado. */
    @FXML
    private void buscarCliente() {
        if (banco == null) {
            mostrarError("El banco no ha sido inicializado.");
            return;
        }
        TipoIdentificacion tipo = cbTipoIdentificacion.getValue();
        if (tipo == null) {
            mostrarError("Seleccione el tipo de identificación.");
            cbTipoIdentificacion.requestFocus();
            return;
        }
        String documento = txtDocumento.getText();
        if (documento == null || documento.isBlank()) {
            mostrarError("Ingrese el número de documento.");
            txtDocumento.requestFocus();
            return;
        }
        try {
            Cliente cliente = banco.buscarCliente(tipo, documento.trim());
            if (cliente == null) {
                mostrarClienteNoEncontrado();
            } else {
                clienteActual = cliente;
                mostrarCliente(cliente);
            }
        } catch (DominioException e) {
            mostrarError(e.getMessage());
        }
    }

    /** Muestra el panel y mensaje correspondientes a una búsqueda sin éxito. */
    private void mostrarClienteNoEncontrado() {
        panelClienteEncontrado.setVisible(false);
        panelClienteEncontrado.setManaged(false);
        panelClienteNoEncontrado.setVisible(true);
        panelClienteNoEncontrado.setManaged(true);
        lblMensajeNoEncontrado.setText("No se encontró un cliente con los datos proporcionados.");
    }

    /** Presenta los datos del cliente y actualiza sus cuentas asociadas. */
    private void mostrarCliente(Cliente cliente) {
        panelClienteNoEncontrado.setVisible(false);
        panelClienteNoEncontrado.setManaged(false);
        panelClienteEncontrado.setVisible(true);
        panelClienteEncontrado.setManaged(true);
        btnNuevaCuenta.setDisable(false);
        lblTipoIdentificacion.setText(cliente.getTipoIdentificacion().getDescripcion());
        lblDocumento.setText(cliente.getDocumento());
        lblNombres.setText(cliente.getNombres());
        lblApellidos.setText(cliente.getApellidos());
        lblFechaNacimiento.setText(cliente.getFechaNacimiento().format(FORMATO_FECHA));
        lblDireccion.setText(cliente.getDireccion() == null
                ? "-"
                : cliente.getDireccion().getDireccion());
        lblTelefono.setText(cliente.getTelefono());
        lblCorreo.setText(cliente.getCorreo());
        cargarCuentas(cliente);
    }

    /** Restaura el cliente recibido al volver desde una vista secundaria. */
    public void mostrarClienteSeleccionado(Cliente cliente) {
        if (cliente == null)
            return;

        clienteActual = cliente;
        cbTipoIdentificacion.setValue(cliente.getTipoIdentificacion());
        txtDocumento.setText(cliente.getDocumento());
        mostrarCliente(cliente);
    }

    /** Carga una copia de las cuentas y mantiene sincronizado el contador. */
    private void cargarCuentas(Cliente cliente) {
        ObservableList<Cuenta> cuentas = FXCollections.observableArrayList(cliente.getCuentas());
        tablaCuentas.setItems(cuentas);
        actualizarContadorCuentas(cuentas.size());
    }

    /** Limpia la búsqueda y restablece CC como opción predeterminada. */
    @FXML
    private void limpiarBusqueda() {
        cbTipoIdentificacion.setValue(TipoIdentificacion.CC);
        txtDocumento.clear();
        clienteActual = null;
        tablaCuentas.getItems().clear();
        actualizarContadorCuentas(0);
        mostrarPanelInicial();
        txtDocumento.requestFocus();
    }

    /**
     * Abre el formulario de registro y conserva tipo/documento para que el
     * nuevo formulario parta de la búsqueda que originó la acción. Tras volver,
     * un callback podría ejecutar buscarCliente() automáticamente si se registró.
     */
    @FXML
    private void registrarCliente() {
        if (banco == null || ventanaPrincipal == null) {
            mostrarError("La ventana principal no ha sido inicializada correctamente.");
            return;
        }
        TipoIdentificacion tipoSeleccionado = cbTipoIdentificacion.getValue();
        String documentoBuscado = txtDocumento.getText();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/crearCliente.fxml"));
            Parent vista = loader.load();
            CrearClienteController controller = loader.getController();
            controller.setBanco(banco);
            controller.setTipoIdentificacion(tipoSeleccionado);
            controller.setDocumento(documentoBuscado);
            controller.setVentanaPrincipal(ventanaPrincipal);
            ventanaPrincipal.setCenter(vista);
        } catch (IOException | RuntimeException e) {
            mostrarError("No se pudo abrir el formulario para registrar el cliente.");
        }
    }

    /** Carga el formulario de edición para el cliente actualmente encontrado. */
    @FXML
    private void editarCliente() {
        if (clienteActual == null) {
            mostrarError("Primero debe buscar y seleccionar un cliente.");
            return;
        }
        if (banco == null || ventanaPrincipal == null) {
            mostrarError("La ventana principal no ha sido inicializada correctamente.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/editarCliente.fxml"));
            Node vista = loader.load();
            EditarClienteController controller = loader.getController();
            controller.setBanco(banco);
            controller.setVentanaPrincipal(ventanaPrincipal);
            controller.setCliente(clienteActual);
            ventanaPrincipal.setCenter(vista);
        } catch (IOException | RuntimeException e) {
            mostrarError("No se pudo abrir el formulario para editar el cliente.");
        }
    }

    /** Crea una cuenta únicamente desde el contexto de un cliente encontrado. */
    @FXML
    private void nuevaCuenta() {
        if (clienteActual == null || banco == null || ventanaPrincipal == null) {
            mostrarError("Primero debe buscar y seleccionar un cliente.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/nuevaCuenta.fxml"));
            Parent vista = loader.load();
            NuevaCuentaController controller = loader.getController();
            Stage ventana = new Stage();
            ventana.setTitle("Crear nueva cuenta");
            Window propietario = ventanaPrincipal.getScene() == null
                    ? null
                    : ventanaPrincipal.getScene().getWindow();
            if (propietario != null) {
                ventana.initOwner(propietario);
            }
            ventana.initModality(Modality.APPLICATION_MODAL);
            ventana.setResizable(false);
            ventana.setScene(new Scene(vista));
            ventana.sizeToScene();
            ventana.centerOnScreen();
            controller.setBanco(banco);
            controller.setVentanaPrincipal(ventanaPrincipal);
            controller.setCliente(clienteActual);
            controller.setVentanaEmergente(ventana);
            ventana.showAndWait();
            cargarCuentas(clienteActual);
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            mostrarError("No se pudo abrir la vista para crear la cuenta: "
                    + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
        }
    }

    /** Muestra los datos principales de una cuenta seleccionada con doble clic. */
    private void mostrarDetalleCuenta(Cuenta cuenta) {
        String tipo = cuenta instanceof CuentaAhorros ? "Cuenta de ahorros"
                : cuenta instanceof CuentaCorriente ? "Cuenta corriente" : "Cuenta";
        String detalle = "Número de cuenta: " + cuenta.getNumeroCuenta()
                + "\nTipo: " + tipo
                + "\nEstado: " + cuenta.getEstado()
                + "\nSaldo: " + String.format("$%,.2f", cuenta.getSaldo());
        mostrarInformacion("Detalle de cuenta", detalle);
    }

    /** Presenta errores de forma uniforme mediante un diálogo modal. */
    private void mostrarError(String mensaje) {
        mostrarError("Error del sistema", mensaje);
    }

    /**
     * Conserva la firma anterior para permitir que otros métodos del
     * controlador indiquen un título específico junto con el mensaje.
     */
    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    /** Conserva la firma anterior para mensajes informativos con título. */
    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}