package Controller;

import Model.Banco;
import Model.Cliente;
import Model.Exceptions.DominioException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Vista dedicada a crear cuentas para un cliente previamente encontrado.
 * La creación se delega a Banco y las reglas de cada cuenta permanecen en el
 * dominio.
 */
public class NuevaCuentaController implements BancoAware {

    @FXML private Label lblCliente;
    @FXML private Label lblIdentificacion;
    @FXML private Label lblTipoDescripcion;
    @FXML private Label lblParametroTitulo;
    @FXML private Label lblParametroAyuda;
    @FXML private ToggleButton tbAhorros;
    @FXML private ToggleButton tbCorriente;
    @FXML private TextField txtParametro;
    @FXML private Button btnCrear;
    @FXML private Button btnCancelar;

    private Banco banco;
    private Cliente cliente;
    private Stage ventanaEmergente;
    private ToggleGroup grupoTipo;

    @FXML
    private void initialize() {
        grupoTipo = new ToggleGroup();
        tbAhorros.setToggleGroup(grupoTipo);
        tbCorriente.setToggleGroup(grupoTipo);
        tbAhorros.setSelected(true);
        txtParametro.setTextFormatter(new TextFormatter<String>(cambio ->
                cambio.getControlNewText().matches("\\d{0,8}(\\.\\d{0,2})?") ? cambio : null));
        tbAhorros.setOnAction(evento -> actualizarParametro());
        tbCorriente.setOnAction(evento -> actualizarParametro());
        actualizarParametro();
    }

    @Override
    public void setBanco(Banco banco) {
        this.banco = banco;
    }

    @Override
    public void setVentanaPrincipal(BorderPane ventanaPrincipal) {
        // La vista es modal y no navega directamente por el contenedor principal.
    }

    /** Recibe la ventana modal que controla el ciclo de vida de esta vista. */
    public void setVentanaEmergente(Stage ventanaEmergente) {
        this.ventanaEmergente = ventanaEmergente;
    }

    /** Recibe el cliente seleccionado desde Gestión de clientes. */
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
        if (cliente != null) {
            lblCliente.setText(cliente.getNombreCompleto());
            lblIdentificacion.setText(cliente.getTipoIdentificacion().getDescripcion()
                    + " - " + cliente.getDocumento());
        }
    }

    private void actualizarParametro() {
        boolean ahorro = tbAhorros.isSelected();
        if (ahorro) {
            txtParametro.setPromptText("Ej: 2.50");
            txtParametro.setAccessibleText("Tasa de interés porcentual");
            lblTipoDescripcion.setText("Cuenta para ahorrar y generar rendimientos según la tasa configurada.");
            lblParametroTitulo.setText("Tasa de interés (%)");
            lblParametroAyuda.setText("Porcentaje de interés que usará la cuenta de ahorros.");
            tbAhorros.setStyle("-fx-background-color:#123B5D;-fx-text-fill:white;-fx-font-weight:bold;");
            tbCorriente.setStyle("-fx-background-color:white;-fx-text-fill:#374151;-fx-border-color:#CBD5E1;");
        } else {
            txtParametro.setPromptText("Ej: 500000");
            txtParametro.setAccessibleText("Límite de sobregiro");
            lblTipoDescripcion.setText("Cuenta para operar con posibilidad de sobregiro hasta el límite autorizado.");
            lblParametroTitulo.setText("Límite de sobregiro");
            lblParametroAyuda.setText("Valor máximo adicional que puede utilizarse cuando el saldo no alcanza.");
            tbAhorros.setStyle("-fx-background-color:white;-fx-text-fill:#374151;-fx-border-color:#CBD5E1;");
            tbCorriente.setStyle("-fx-background-color:#123B5D;-fx-text-fill:white;-fx-font-weight:bold;");
        }
    }

    @FXML
    private void crearCuenta() {
        if (banco == null || cliente == null) {
            mostrarError("No existe un cliente válido para crear la cuenta.");
            return;
        }
        String texto = txtParametro.getText().trim();
        if (texto.isEmpty()) {
            marcarInvalido();
            mostrarError("Ingrese el parámetro de la cuenta.");
            return;
        }

        double parametro;
        try {
            parametro = Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            marcarInvalido();
            mostrarError("Ingrese un valor numérico válido.");
            return;
        }

        if (!Double.isFinite(parametro)
                || (tbAhorros.isSelected() && parametro < 0)
                || (tbCorriente.isSelected() && parametro <= 0)) {
            marcarInvalido();
            mostrarError(tbAhorros.isSelected()
                    ? "La tasa de interés no puede ser negativa."
                    : "El límite de sobregiro debe ser mayor que cero.");
            return;
        }
        limpiarInvalido();

        String tipo = tbAhorros.isSelected() ? "Cuenta de ahorros" : "Cuenta corriente";
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "Cliente: " + cliente.getNombreCompleto()
                        + "\nTipo: " + tipo
                        + "\nParámetro: " + parametro,
                ButtonType.OK, ButtonType.CANCEL);
        confirmacion.setTitle("Confirmar creación de cuenta");
        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK)
            return;

        try {
            if (tbAhorros.isSelected()) {
                banco.crearCuentaAhorros(cliente.getTipoIdentificacion(), cliente.getDocumento(), parametro);
            } else {
                banco.crearCuentaCorriente(cliente.getTipoIdentificacion(), cliente.getDocumento(), parametro);
            }
            cerrarDialogo();
        } catch (DominioException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        cerrarDialogo();
    }

    private void cerrarDialogo() {
        if (ventanaEmergente != null) {
            ventanaEmergente.close();
        } else {
            mostrarError("La ventana de creación no ha sido inicializada.");
        }
    }

    private void marcarInvalido() {
        txtParametro.setStyle("-fx-border-color:#B91C1C;-fx-border-width:2px;");
    }

    private void limpiarInvalido() {
        txtParametro.setStyle("");
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
