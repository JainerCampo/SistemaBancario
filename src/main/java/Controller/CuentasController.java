package Controller;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import Model.Banco;
import Model.Cliente;
import Model.Cuenta;
import Model.CuentaAhorros;
import Model.CuentaCorriente;
import Model.EstadoCuenta;
import Model.Movimiento;
import Model.TipoIdentificacion;
import Model.Exceptions.DominioException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

/** Gestiona productos bancarios; las operaciones viven en TransaccionesController. */
public class CuentasController implements Initializable, BancoAware {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(Locale.US);
    @FXML private ComboBox<TipoIdentificacion> cbTipoIdentificacion;
    @FXML private TextField txtDocumento, txtNumeroCuenta;
    @FXML private ToggleButton tbPorNumero, tbPorCliente;
    @FXML private RadioButton rbTodas, rbActivas, rbBloqueadas, rbSuspendidas, rbCanceladas;
    @FXML private ToggleGroup tgModoBusqueda, tgFiltroEstado;
    @FXML private HBox formPorNumero, formPorCliente;
    @FXML private TableView<Cuenta> tablaCuentas;
    @FXML private TableColumn<Cuenta, String> colNumeroCuenta, colTitular, colTipo;
    @FXML private TableColumn<Cuenta, EstadoCuenta> colEstado;
    @FXML private TableColumn<Cuenta, Double> colSaldo;
    @FXML private TableColumn<Cuenta, LocalDate> colFechaApertura;
    @FXML private VBox panelDetalle;
    @FXML private Label lblNumeroDetalle, lblTitularDetalle, lblDocumentoDetalle, lblTipoDetalle, lblEstadoDetalle, lblSaldoDetalle, lblFechaDetalle, lblParametroDetalle, lblValorParametro;
    @FXML private TableView<Movimiento> tablaMovimientos;
    @FXML private TableColumn<Movimiento, LocalDate> colFechaMovimiento;
    @FXML private TableColumn<Movimiento, String> colTipoMovimiento, colCanalMovimiento;
    @FXML private TableColumn<Movimiento, Double> colMontoMovimiento;
    @FXML private Button btnAplicarIntereses;
    private Banco banco;
    private BorderPane ventanaPrincipal;
    private Cuenta cuentaSeleccionada;
    @Override public void initialize(java.net.URL url, java.util.ResourceBundle resources) {
        MONEDA.setMaximumFractionDigits(2);
        configurarCamposNumericos();
        cbTipoIdentificacion.setItems(FXCollections.observableArrayList(TipoIdentificacion.values()));
        cbTipoIdentificacion.getSelectionModel().select(TipoIdentificacion.CC);
        cbTipoIdentificacion.setCellFactory(combo -> new ListCell<>() {
            @Override protected void updateItem(TipoIdentificacion item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDescripcion());
            }
        });
        cbTipoIdentificacion.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(TipoIdentificacion item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDescripcion());
            }
        });
        // El FXML inyecta los grupos; el respaldo mantiene el controller reutilizable.
        if (tgModoBusqueda == null) tgModoBusqueda = new ToggleGroup();
        if (!tgModoBusqueda.getToggles().contains(tbPorNumero)) tgModoBusqueda.getToggles().addAll(tbPorNumero, tbPorCliente);
        if (tgFiltroEstado == null) tgFiltroEstado = new ToggleGroup();
        if (!tgFiltroEstado.getToggles().contains(rbTodas)) tgFiltroEstado.getToggles().addAll(rbTodas, rbActivas, rbBloqueadas, rbSuspendidas, rbCanceladas);
        tgModoBusqueda.selectToggle(tbPorNumero);
        tgFiltroEstado.selectToggle(rbTodas);
        tgModoBusqueda.selectedToggleProperty().addListener((observable, anterior, seleccionado) -> {
            if (seleccionado == null) {
                tgModoBusqueda.selectToggle(tbPorNumero);
                return;
            }
            boolean porNumero = seleccionado == tbPorNumero;
            alternarBusqueda(porNumero);
            actualizarEstiloModo();
        });
        tgFiltroEstado.selectedToggleProperty().addListener((observable, anterior, seleccionado) -> filtrarPorEstado());
        alternarBusqueda(true);
        actualizarEstiloModo();
        btnAplicarIntereses.setDisable(true);
        configurarTablas(); panelDetalle.setVisible(false); panelDetalle.setManaged(false);
    }

    /** Limita los campos de consulta a dígitos, como exige el modelo. */
    private void configurarCamposNumericos() {
        txtNumeroCuenta.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,10}") ? change : null));
        txtDocumento.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,20}") ? change : null));
    }
    /** Alterna la sección visible y enfoca el campo principal del modo activo. */
    private void alternarBusqueda(boolean porNumero) {
        formPorNumero.setVisible(porNumero);
        formPorNumero.setManaged(porNumero);
        formPorCliente.setVisible(!porNumero);
        formPorCliente.setManaged(!porNumero);
        if (porNumero) txtNumeroCuenta.requestFocus();
        else cbTipoIdentificacion.requestFocus();
    }

    /** Mantiene un contraste claro entre el modo activo y el inactivo. */
    private void actualizarEstiloModo() {
        tbPorNumero.setStyle(tbPorNumero.isSelected()
                ? "-fx-background-color:#123B5D;-fx-text-fill:white;"
                : "-fx-background-color:transparent;-fx-text-fill:#333;");
        tbPorCliente.setStyle(tbPorCliente.isSelected()
                ? "-fx-background-color:#123B5D;-fx-text-fill:white;"
                : "-fx-background-color:transparent;-fx-text-fill:#333;");
    }
    private void configurarTablas() {
        colNumeroCuenta.setCellValueFactory(new PropertyValueFactory<>("numeroCuenta"));
        colTitular.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitular().getNombreCompleto()));
        colTipo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(tipo(c.getValue())));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setCellFactory(col -> new TableCell<>() { @Override protected void updateItem(EstadoCuenta item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item.toString()); setStyle(empty || item == null ? "" : estiloEstado(item)); } });
        colSaldo.setCellValueFactory(new PropertyValueFactory<>("saldo"));
        colSaldo.setCellFactory(col -> new TableCell<>() { @Override protected void updateItem(Double item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : MONEDA.format(item)); } });
        colFechaApertura.setCellValueFactory(new PropertyValueFactory<>("fechaApertura"));
        colFechaApertura.setCellFactory(col -> new TableCell<>() { @Override protected void updateItem(LocalDate item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item.format(FECHA)); } });
        colFechaMovimiento.setCellValueFactory(new PropertyValueFactory<>("fecha")); colTipoMovimiento.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTipoMovimiento().toString())); colMontoMovimiento.setCellValueFactory(new PropertyValueFactory<>("monto")); colCanalMovimiento.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCanalMovimiento().toString()));
        tablaCuentas.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> mostrarDetalle(value));
    }
    @Override public void setBanco(Banco banco) { this.banco = banco; cargarCuentas(banco == null ? List.of() : banco.listarCuentas()); }
    @Override public void setVentanaPrincipal(BorderPane ventanaPrincipal) { this.ventanaPrincipal = ventanaPrincipal; }

    @FXML private void buscarCuenta() {
        if (banco == null) { error("El banco no ha sido inicializado."); return; }
        try {
            if (tbPorNumero.isSelected()) {
                String numero = txtNumeroCuenta.getText().trim();
                if (!numero.matches("\\d{10}")) {
                    marcarInvalido(txtNumeroCuenta);
                    error("El número de cuenta debe contener exactamente 10 dígitos.");
                    return;
                }
                limpiarInvalido(txtNumeroCuenta);
                Cuenta cuenta = banco.buscarCuentaBanco(numero);
                cargarCuentas(cuenta == null ? List.of() : List.of(cuenta));
            } else {
                String documento = txtDocumento.getText().trim();
                boolean tipoValido = cbTipoIdentificacion.getValue() != null;
                boolean documentoValido = documento.matches("\\d+");
                if (!tipoValido || !documentoValido) {
                    if (!tipoValido) marcarInvalido(cbTipoIdentificacion);
                    if (!documentoValido) marcarInvalido(txtDocumento);
                    error("Seleccione el tipo de identificación e ingrese un documento numérico.");
                    return;
                }
                limpiarInvalido(cbTipoIdentificacion);
                limpiarInvalido(txtDocumento);
                Cliente cliente = banco.buscarCliente(cbTipoIdentificacion.getValue(), documento);
                cargarCuentas(cliente == null ? List.of() : cliente.getCuentas());
            }
        } catch (DominioException e) { error(e.getMessage()); }
    }

    private void marcarInvalido(javafx.scene.control.Control control) {
        control.setStyle(control.getStyle() + ";-fx-border-color:#B91C1C;-fx-border-width:2px;");
    }

    private void limpiarInvalido(javafx.scene.control.Control control) {
        control.setStyle(control.getStyle().replace(";-fx-border-color:#B91C1C;-fx-border-width:2px;", ""));
    }
    /** Filtra la tabla según el único RadioButton de estado seleccionado. */
    private void filtrarPorEstado() {
        if (rbActivas.isSelected()) filtrar(EstadoCuenta.ACTIVO);
        else if (rbBloqueadas.isSelected()) filtrar(EstadoCuenta.BLOQUEADO);
        else if (rbSuspendidas.isSelected()) filtrar(EstadoCuenta.SUSPENDIDO);
        else if (rbCanceladas.isSelected()) filtrar(EstadoCuenta.CANCELADO);
        else cargarCuentas(banco == null ? List.of() : banco.listarCuentas());
    }
    private void filtrar(EstadoCuenta estado) { cargarCuentas(banco == null ? List.of() : banco.listarCuentas().stream().filter(c -> c.getEstado() == estado).collect(Collectors.toList())); }
    private void cargarCuentas(List<Cuenta> cuentas) { tablaCuentas.setItems(FXCollections.observableArrayList(cuentas)); tablaCuentas.getSelectionModel().clearSelection(); }

    private void mostrarDetalle(Cuenta cuenta) {
        cuentaSeleccionada = cuenta;
        btnAplicarIntereses.setDisable(!(cuenta instanceof CuentaAhorros));
        boolean visible = cuenta != null;
        panelDetalle.setVisible(visible);
        panelDetalle.setManaged(visible);
        if (!visible) return;
        lblNumeroDetalle.setText(cuenta.getNumeroCuenta()); lblTitularDetalle.setText(cuenta.getTitular().getNombreCompleto()); lblDocumentoDetalle.setText(cuenta.getTitular().getTipoIdentificacion() + " " + cuenta.getTitular().getDocumento()); lblTipoDetalle.setText(tipo(cuenta)); lblEstadoDetalle.setText(cuenta.getEstado().toString()); lblSaldoDetalle.setText(MONEDA.format(cuenta.getSaldo())); lblFechaDetalle.setText(cuenta.getFechaApertura().format(FECHA));
        if (cuenta instanceof CuentaAhorros ahorros) { lblParametroDetalle.setText("Tasa interés"); lblValorParametro.setText(String.format(Locale.US, "%.2f%%", ahorros.getTasaInteres())); } else { lblParametroDetalle.setText("Límite sobregiro"); lblValorParametro.setText(cuenta instanceof CuentaCorriente corriente ? MONEDA.format(corriente.getLimiteSobregiro()) : "-"); }
        List<Movimiento> movimientos = cuenta.getMovimientos(); tablaMovimientos.setItems(FXCollections.observableArrayList(movimientos.subList(Math.max(0, movimientos.size() - 10), movimientos.size())));
    }
    @FXML private void verExtracto() { if (cuentaSeleccionada == null) { error("Seleccione una cuenta."); return; } TableView<Movimiento> tabla = new TableView<>(); TableColumn<Movimiento, String> tipo = new TableColumn<>("Tipo"); tipo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTipoMovimiento().toString())); TableColumn<Movimiento, Double> monto = new TableColumn<>("Monto"); monto.setCellValueFactory(new PropertyValueFactory<>("monto")); TableColumn<Movimiento, String> canal = new TableColumn<>("Canal"); canal.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCanalMovimiento().toString())); tabla.getColumns().addAll(tipo, monto, canal); tabla.setItems(FXCollections.observableArrayList(cuentaSeleccionada.getMovimientos())); tabla.setPrefSize(620, 300); Dialog<ButtonType> dialogo = new Dialog<>(); dialogo.setTitle("Extracto completo"); dialogo.getDialogPane().setContent(tabla); dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE); dialogo.showAndWait(); }
    @FXML private void cambiarEstado() {
        if (cuentaSeleccionada == null) { error("Seleccione una cuenta."); return; } ComboBox<EstadoCuenta> estado = new ComboBox<>(FXCollections.observableArrayList(EstadoCuenta.values())); TextArea motivo = new TextArea(); motivo.setPromptText("Motivo (mínimo 10 caracteres)"); GridPane contenido = new GridPane(); contenido.setHgap(12); contenido.setVgap(12); contenido.add(new Label("Nuevo estado"), 0, 0); contenido.add(estado, 1, 0); contenido.add(new Label("Motivo"), 0, 1); contenido.add(motivo, 1, 1);
        Dialog<Pair<EstadoCuenta, String>> dialogo = new Dialog<>(); ButtonType aplicar = new ButtonType("Continuar", javafx.scene.control.ButtonBar.ButtonData.OK_DONE); dialogo.getDialogPane().getButtonTypes().addAll(aplicar, ButtonType.CANCEL); dialogo.getDialogPane().setContent(contenido); dialogo.setResultConverter(b -> b == aplicar ? new Pair<>(estado.getValue(), motivo.getText().trim()) : null);
        dialogo.showAndWait().ifPresent(resultado -> { if (resultado.getKey() == null || resultado.getValue().length() < 10) { error("Seleccione un estado y escriba un motivo de al menos 10 caracteres."); return; } Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "Cuenta: " + cuentaSeleccionada.getNumeroCuenta() + "\nNuevo estado: " + resultado.getKey() + "\nMotivo: " + resultado.getValue(), ButtonType.OK, ButtonType.CANCEL); if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return; try { banco.cambiarEstadoCuenta(cuentaSeleccionada.getNumeroCuenta(), resultado.getKey(), resultado.getValue()); mostrarDetalle(cuentaSeleccionada); tablaCuentas.refresh(); } catch (DominioException e) { error(e.getMessage()); } });
    }
    @FXML private void aplicarIntereses() { if (!(cuentaSeleccionada instanceof CuentaAhorros ahorros)) { error("Seleccione una cuenta de ahorros."); return; } Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "Aplicar intereses a " + cuentaSeleccionada.getNumeroCuenta() + "?", ButtonType.OK, ButtonType.CANCEL); if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return; try { ahorros.aplicarIntereses(); mostrarDetalle(cuentaSeleccionada); tablaCuentas.refresh(); } catch (DominioException e) { error(e.getMessage()); } }
    private String tipo(Cuenta cuenta) { return cuenta instanceof CuentaAhorros ? "Ahorros" : "Corriente"; }
    private String estiloEstado(EstadoCuenta estado) { return switch (estado) { case ACTIVO -> "-fx-background-color:#DCFCE7;"; case BLOQUEADO -> "-fx-background-color:#FECACA;"; case SUSPENDIDO -> "-fx-background-color:#FEF08A;"; default -> ""; }; }
    private void error(String mensaje) { Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje); alerta.setTitle("Error"); alerta.showAndWait(); }
}
