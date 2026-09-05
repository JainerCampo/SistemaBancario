package Controller;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import Model.Banco;
import Model.CanalMovimiento;
import Model.Cuenta;
import Model.CuentaCorriente;
import Model.Movimiento;
import Model.Exceptions.DominioException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/** Ejecuta movimientos; la vista de cuentas administra exclusivamente productos. */
public class TransaccionesController implements Initializable, BancoAware {
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(Locale.US);
    @FXML private ToggleButton tbConsignar, tbRetirar, tbTransferir;
    @FXML private VBox panelConsignar, panelRetirar, panelTransferir, panelComprobante;
    @FXML private TextField txtCuentaConsignar, txtMontoConsignar, txtDescripcionConsignar, txtCuentaRetirar, txtMontoRetirar, txtDescripcionRetirar, txtCuentaOrigen, txtCuentaDestino, txtMontoTransferir, txtDescripcionTransferir;
    @FXML private ComboBox<CanalMovimiento> cbCanalConsignar, cbCanalRetirar, cbCanalTransferir;
    @FXML private Label lblTitularConsignar, lblTitularRetirar, lblTitularOrigen, lblTitularDestino, lblComprobante;
    @FXML private TableView<Movimiento> tablaHistorial;
    @FXML private TableColumn<Movimiento, LocalTime> colHora;
    @FXML private TableColumn<Movimiento, String> colTipo, colCuentas;
    @FXML private TableColumn<Movimiento, Double> colMonto;
    @FXML private TableColumn<Movimiento, CanalMovimiento> colCanal;
    private final ObservableList<Movimiento> historial = FXCollections.observableArrayList();
    private Banco banco;
    private BorderPane ventanaPrincipal;
    private ToggleGroup grupoOperacion;

    @Override public void initialize(java.net.URL url, java.util.ResourceBundle resources) {
        MONEDA.setMaximumFractionDigits(2); grupoOperacion = new ToggleGroup(); tbConsignar.setToggleGroup(grupoOperacion); tbRetirar.setToggleGroup(grupoOperacion); tbTransferir.setToggleGroup(grupoOperacion); tbConsignar.setSelected(true); tbConsignar.setOnAction(e -> mostrarPanel(panelConsignar)); tbRetirar.setOnAction(e -> mostrarPanel(panelRetirar)); tbTransferir.setOnAction(e -> mostrarPanel(panelTransferir));
        configurarCanal(cbCanalConsignar); configurarCanal(cbCanalRetirar); configurarCanal(cbCanalTransferir); colHora.setCellValueFactory(new PropertyValueFactory<>("hora")); colTipo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTipoMovimiento().toString())); colCuentas.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(cuentas(c.getValue()))); colMonto.setCellValueFactory(new PropertyValueFactory<>("monto")); colCanal.setCellValueFactory(new PropertyValueFactory<>("canalMovimiento")); tablaHistorial.setItems(historial); panelComprobante.setVisible(false); panelComprobante.setManaged(false);
    }
    private void mostrarPanel(VBox activo) { panelConsignar.setVisible(activo == panelConsignar); panelConsignar.setManaged(activo == panelConsignar); panelRetirar.setVisible(activo == panelRetirar); panelRetirar.setManaged(activo == panelRetirar); panelTransferir.setVisible(activo == panelTransferir); panelTransferir.setManaged(activo == panelTransferir); }
    private void configurarCanal(ComboBox<CanalMovimiento> combo) { combo.setItems(FXCollections.observableArrayList(CanalMovimiento.values())); combo.getSelectionModel().select(CanalMovimiento.VENTANILLA); }
    @Override public void setBanco(Banco banco) { this.banco = banco; }
    @Override public void setVentanaPrincipal(BorderPane ventanaPrincipal) { this.ventanaPrincipal = ventanaPrincipal; }
    @FXML private void verificarConsignar() { verificar(txtCuentaConsignar, lblTitularConsignar); }
    @FXML private void verificarRetirar() { verificar(txtCuentaRetirar, lblTitularRetirar); }
    @FXML private void verificarOrigen() { verificar(txtCuentaOrigen, lblTitularOrigen); }
    @FXML private void verificarDestino() { verificar(txtCuentaDestino, lblTitularDestino); }
    private void verificar(TextField campo, Label etiqueta) { try { Cuenta cuenta = buscar(campo.getText()); etiqueta.setText(cuenta.getTitular().getNombreCompleto()); } catch (DominioException e) { error(e.getMessage()); } }
    private Cuenta buscar(String numero) throws DominioException { if (banco == null) throw new DominioException("El banco no ha sido inicializado."); if (numero == null || numero.isBlank()) throw new DominioException("Ingrese un número de cuenta."); Cuenta cuenta = banco.buscarCuentaBanco(numero.trim()); if (cuenta == null) throw new DominioException("La cuenta no existe."); return cuenta; }
    @FXML private void ejecutarConsignacion() { try { Cuenta cuenta = formulario(txtCuentaConsignar, txtMontoConsignar, txtDescripcionConsignar, cbCanalConsignar); double monto = monto(txtMontoConsignar); confirmar("Consignación", cuenta.getNumeroCuenta(), monto); cuenta.consignar(monto, cbCanalConsignar.getValue(), txtDescripcionConsignar.getText().trim()); registrar(cuenta.getMovimientos().get(cuenta.getMovimientos().size() - 1), "Consignación", cuenta.getNumeroCuenta(), monto, cbCanalConsignar.getValue()); } catch (DominioException e) { if (!"Operación cancelada.".equals(e.getMessage())) error(e.getMessage()); } }
    @FXML private void ejecutarRetiro() { try { Cuenta cuenta = formulario(txtCuentaRetirar, txtMontoRetirar, txtDescripcionRetirar, cbCanalRetirar); double monto = monto(txtMontoRetirar); double disponible = cuenta.getSaldo() + (cuenta instanceof CuentaCorriente corriente ? corriente.getLimiteSobregiro() : 0); if (monto > disponible) throw new DominioException("Saldo insuficiente para realizar el retiro."); confirmar("Retiro", cuenta.getNumeroCuenta(), monto); cuenta.retirar(monto, cbCanalRetirar.getValue(), txtDescripcionRetirar.getText().trim()); registrar(cuenta.getMovimientos().get(cuenta.getMovimientos().size() - 1), "Retiro", cuenta.getNumeroCuenta(), monto, cbCanalRetirar.getValue()); } catch (DominioException e) { if (!"Operación cancelada.".equals(e.getMessage())) error(e.getMessage()); } }
    @FXML private void ejecutarTransferencia() { try { Cuenta origen = buscar(txtCuentaOrigen.getText()); Cuenta destino = buscar(txtCuentaDestino.getText()); if (origen == destino) throw new DominioException("La cuenta origen y destino deben ser diferentes."); validarDescripcion(txtDescripcionTransferir.getText()); double valor = monto(txtMontoTransferir); if (cbCanalTransferir.getValue() == null) throw new DominioException("Seleccione un canal."); confirmar("Transferencia", origen.getNumeroCuenta() + " -> " + destino.getNumeroCuenta(), valor); banco.transferir(origen.getNumeroCuenta(), destino.getNumeroCuenta(), valor, cbCanalTransferir.getValue(), txtDescripcionTransferir.getText().trim()); registrar(origen.getMovimientos().get(origen.getMovimientos().size() - 1), "Transferencia", origen.getNumeroCuenta() + " -> " + destino.getNumeroCuenta(), valor, cbCanalTransferir.getValue()); } catch (DominioException e) { if (!"Operación cancelada.".equals(e.getMessage())) error(e.getMessage()); } }
    private Cuenta formulario(TextField cuenta, TextField monto, TextField descripcion, ComboBox<CanalMovimiento> canal) throws DominioException { Cuenta resultado = buscar(cuenta.getText()); validarDescripcion(descripcion.getText()); if (canal.getValue() == null) throw new DominioException("Seleccione un canal."); return resultado; }
    private void validarDescripcion(String valor) throws DominioException { if (valor == null || valor.trim().length() < 10) throw new DominioException("La descripción debe tener al menos 10 caracteres."); }
    private double monto(TextField campo) throws DominioException { try { double valor = Double.parseDouble(campo.getText().trim()); if (!Double.isFinite(valor) || valor <= 0) throw new NumberFormatException(); return valor; } catch (NumberFormatException e) { throw new DominioException("Ingrese un monto numérico mayor que cero."); } }
    private void confirmar(String operacion, String cuenta, double monto) throws DominioException { Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, operacion + "\nCuenta(s): " + cuenta + "\nMonto: " + MONEDA.format(monto), ButtonType.OK, ButtonType.CANCEL); if (alerta.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) throw new DominioException("Operación cancelada."); }
    private void registrar(Movimiento movimiento, String tipo, String cuentas, double monto, CanalMovimiento canal) { historial.add(movimiento); lblComprobante.setText("✓ " + tipo + " exitosa\nN° movimiento: " + movimiento.getId() + "\nCuenta(s): " + cuentas + "\nMonto: " + MONEDA.format(monto) + "\nFecha: " + LocalDate.now() + " " + movimiento.getHora() + "\nCanal: " + canal); panelComprobante.setVisible(true); panelComprobante.setManaged(true); }
    @FXML private void nuevaOperacion() { txtCuentaConsignar.clear(); txtMontoConsignar.clear(); txtDescripcionConsignar.clear(); txtCuentaRetirar.clear(); txtMontoRetirar.clear(); txtDescripcionRetirar.clear(); txtCuentaOrigen.clear(); txtCuentaDestino.clear(); txtMontoTransferir.clear(); txtDescripcionTransferir.clear(); lblTitularConsignar.setText(""); lblTitularRetirar.setText(""); lblTitularOrigen.setText(""); lblTitularDestino.setText(""); panelComprobante.setVisible(false); panelComprobante.setManaged(false); }
    private String cuentas(Movimiento movimiento) { return movimiento.getCuentaOrigen() == null ? "Cuenta de operación" : movimiento.getCuentaOrigen() + " -> " + movimiento.getCuentaDestino(); }
    private void error(String mensaje) { Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje); alerta.setTitle("Error"); alerta.showAndWait(); }
}
