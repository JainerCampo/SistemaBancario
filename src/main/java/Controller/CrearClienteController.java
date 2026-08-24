package Controller;

import java.io.IOException;
import java.time.LocalDate;

import Model.Banco;
import Model.Cliente;
import Model.Direccion;
import Model.TipoIdentificacion;
import Model.Exceptions.DominioException;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;

public class CrearClienteController implements BancoAware {

    // =========================================================
    // MODELO
    // =========================================================

    private Banco banco;

    // =========================================================
    // VENTANA PRINCIPAL
    // =========================================================

    private BorderPane ventanaPrincipal;

    // =========================================================
    // IDENTIFICACIÓN
    // =========================================================

    @FXML
    private ComboBox<TipoIdentificacion> cbTipoIdentificacion;

    @FXML
    private TextField txtDocumento;

    // =========================================================
    // INFORMACIÓN PERSONAL
    // =========================================================

    @FXML
    private TextField txtNombres;

    @FXML
    private TextField txtApellidos;

    @FXML
    private DatePicker dpFechaNacimiento;

    // =========================================================
    // INFORMACIÓN DE CONTACTO
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
    private Button btnRegistrarCliente;

    @FXML
    private Button btnCancelar;

    // =========================================================
    // INICIALIZACIÓN
    // =========================================================

    @FXML
    public void initialize() {

        cargarTiposIdentificacion();
        configurarCamposTexto();
    }

        // Restringe los nombres a letras y separadores válidos, y el teléfono a dígitos.
        private void configurarCamposTexto() {
        TextFormatter<String> nombresFormatter = new TextFormatter<>(cambio ->
            cambio.getControlNewText().matches("[\\p{L} '\\-]*") ? cambio : null);
        TextFormatter<String> apellidosFormatter = new TextFormatter<>(cambio ->
            cambio.getControlNewText().matches("[\\p{L} '\\-]*") ? cambio : null);
        TextFormatter<String> telefonoFormatter = new TextFormatter<>(cambio ->
            cambio.getControlNewText().matches("\\d*") ? cambio : null);
        TextFormatter<String> documentoFormatter = new TextFormatter<>(cambio ->
            cambio.getControlNewText().matches("\\d*") ? cambio : null);

        txtNombres.setTextFormatter(nombresFormatter);
        txtApellidos.setTextFormatter(apellidosFormatter);
        txtTelefono.setTextFormatter(telefonoFormatter);
        txtDocumento.setTextFormatter(documentoFormatter);
        }

    // =========================================================
    // RECIBIR BANCO
    // =========================================================

    public void setBanco(Banco banco) {

        this.banco = banco;
    }

    public void setTipoIdentificacion(
            TipoIdentificacion tipoIdentificacion) {

        if (tipoIdentificacion != null) {
            cbTipoIdentificacion.setValue(tipoIdentificacion);
        }
    }

    public void setDocumento(String documento) {

        txtDocumento.setText(documento);
    }

    // =========================================================
    // RECIBIR VENTANA PRINCIPAL
    // =========================================================

    public void setVentanaPrincipal(
            BorderPane ventanaPrincipal) {

        this.ventanaPrincipal = ventanaPrincipal;
    }

    // =========================================================
    // CARGAR TIPOS DE IDENTIFICACIÓN
    // =========================================================

    private void cargarTiposIdentificacion() {

        cbTipoIdentificacion.setItems(
                FXCollections.observableArrayList(
                        TipoIdentificacion.values()));

        /*
         * Mostramos la descripción definida
         * en el enum.
         *
         * CC -> Cédula de ciudadanía
         * TI -> Tarjeta de identidad
         * PAS -> Pasaporte
         */

        cbTipoIdentificacion.setCellFactory(
                comboBox -> new javafx.scene.control.ListCell<>() {

                    @Override
                    protected void updateItem(
                            TipoIdentificacion item,
                            boolean empty) {

                        super.updateItem(item, empty);

                        if (empty || item == null) {

                            setText(null);

                        } else {

                            setText(
                                    item.getDescripcion());
                        }
                    }
                });

        cbTipoIdentificacion.setButtonCell(
                new javafx.scene.control.ListCell<>() {

                    @Override
                    protected void updateItem(
                            TipoIdentificacion item,
                            boolean empty) {

                        super.updateItem(item, empty);

                        if (empty || item == null) {

                            setText(null);

                        } else {

                            setText(
                                    item.getDescripcion());
                        }
                    }
                });
    }

    // =========================================================
    // REGISTRAR CLIENTE
    // =========================================================

    @FXML
    private void registrarCliente() {

        // Primero validamos la interfaz
        if (!validarCampos()) {
            return;
        }

        // Solo llegamos aquí si TODOS los campos
        // tienen información.

        try {

            TipoIdentificacion tipo = cbTipoIdentificacion.getValue();

            String documento = txtDocumento.getText().trim();

            String nombres = txtNombres.getText().trim();

            String apellidos = txtApellidos.getText().trim();

            LocalDate fechaNacimiento = dpFechaNacimiento.getValue();

            Direccion direccion = new Direccion(
                    txtDireccion.getText().trim());

            String telefono = txtTelefono.getText().trim();

            String correo = txtCorreo.getText().trim();

            // Confirmamos todos los datos antes de crear y guardar el cliente.
            if (!confirmarDatos(tipo, documento, nombres, apellidos, fechaNacimiento,
                    direccion.getDireccion(), telefono, correo)) {
                return;
            }

            Cliente cliente = new Cliente(
                    tipo,
                    documento,
                    nombres,
                    apellidos,
                    fechaNacimiento,
                    direccion,
                    telefono,
                    correo);

            // El Banco aplica las reglas del negocio
            banco.agregarCliente(cliente);

            mostrarInformacion(
                    "Cliente registrado",
                    "El cliente fue registrado correctamente.");

            volverAClientes(cliente);

        } catch (DominioException e) {

            // Error producido por el modelo
            mostrarError(
                    "No se pudo registrar el cliente",
                    e.getMessage());
        }
    }

    /** Muestra los datos ingresados y devuelve si el usuario autoriza el registro. */
    private boolean confirmarDatos(TipoIdentificacion tipo, String documento,
            String nombres, String apellidos, LocalDate fechaNacimiento,
            String direccion, String telefono, String correo) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar registro");
        alerta.setHeaderText("¿Está seguro de que los datos ingresados son correctos?");
        alerta.setContentText(
                "Tipo de identificación: " + tipo.getDescripcion()
                        + "\nNúmero de identificación: " + documento
                        + "\nNombres: " + nombres
                        + "\nApellidos: " + apellidos
                        + "\nFecha de nacimiento: " + fechaNacimiento
                        + "\nDirección: " + direccion
                        + "\nTeléfono: " + telefono
                        + "\nCorreo electrónico: " + correo);
        return alerta.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    // =========================================================
    // CANCELAR
    // =========================================================

    @FXML
    private void cancelar() {

        /*
         * Cancelar NO modifica el Banco.
         *
         * Simplemente regresamos a clientes.fxml.
         */

        volverAClientes(null);
    }

    // =========================================================
    // VOLVER A CLIENTES
    // =========================================================

    private void volverAClientes(Cliente clienteSeleccionado) {

        if (ventanaPrincipal == null) {

            mostrarError(
                    "Error del sistema",
                    "No se ha inicializado la ventana principal.");

            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/View/clientes.fxml"));

            Node vista = loader.load();

            /*
             * Obtener nuevamente ClientesController.
             */

            ClientesController controller = loader.getController();

            /*
             * Pasar el mismo Banco.
             */

            controller.setBanco(banco);

            /*
             * Pasar el mismo BorderPane.
             */

            controller.setVentanaPrincipal(
                    ventanaPrincipal);

            if (clienteSeleccionado != null) {
                controller.mostrarClienteSeleccionado(clienteSeleccionado);
            }

            /*
             * Mostrar clientes.fxml.
             */

            ventanaPrincipal.setCenter(vista);

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "Error",
                    "No se pudo volver a Gestión de Clientes.");
        }
    }

    // =========================================================
    // MENSAJE DE INFORMACIÓN
    // =========================================================

    private void mostrarInformacion(
            String titulo,
            String mensaje) {

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION);

        alert.setTitle(titulo);

        alert.setHeaderText(null);

        alert.setContentText(mensaje);

        alert.showAndWait();
    }

    // =========================================================
    // MENSAJE DE ERROR
    // =========================================================

    private void mostrarError(
            String titulo,
            String mensaje) {

        Alert alert = new Alert(
                Alert.AlertType.ERROR);

        alert.setTitle(titulo);

        alert.setHeaderText(null);

        alert.setContentText(mensaje);

        alert.showAndWait();
    }

    private boolean validarCampos() {

        if (cbTipoIdentificacion.getValue() == null) {

            mostrarError(
                    "Campo obligatorio",
                    "Debe seleccionar un tipo de identificación.");

            cbTipoIdentificacion.requestFocus();

            return false;
        }

        if (txtDocumento.getText() == null
                || txtDocumento.getText().trim().isEmpty()) {

            mostrarError(
                    "Campo obligatorio",
                    "Debe ingresar el número de documento.");

            txtDocumento.requestFocus();

            return false;
        }

        if (txtNombres.getText() == null
                || txtNombres.getText().trim().isEmpty()) {

            mostrarError(
                    "Campo obligatorio",
                    "Debe ingresar los nombres del cliente.");

            txtNombres.requestFocus();

            return false;
        }

        if (txtApellidos.getText() == null
                || txtApellidos.getText().trim().isEmpty()) {

            mostrarError(
                    "Campo obligatorio",
                    "Debe ingresar los apellidos del cliente.");

            txtApellidos.requestFocus();

            return false;
        }

        if (dpFechaNacimiento.getValue() == null) {

            mostrarError(
                    "Campo obligatorio",
                    "Debe seleccionar la fecha de nacimiento.");

            dpFechaNacimiento.requestFocus();

            return false;
        }

        if (txtDireccion.getText() == null
                || txtDireccion.getText().trim().isEmpty()) {

            mostrarError(
                    "Campo obligatorio",
                    "Debe ingresar la dirección.");

            txtDireccion.requestFocus();

            return false;
        }

        if (txtTelefono.getText() == null
                || txtTelefono.getText().trim().isEmpty()) {

            mostrarError(
                    "Campo obligatorio",
                    "Debe ingresar el número de teléfono.");

            txtTelefono.requestFocus();

            return false;
        }

        if (txtCorreo.getText() == null
                || txtCorreo.getText().trim().isEmpty()) {

            mostrarError(
                    "Campo obligatorio",
                    "Debe ingresar el correo electrónico.");

            txtCorreo.requestFocus();

            return false;
        }

        return true;
    }
}