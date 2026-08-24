
package Controller;

import Model.Banco;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class LoginController {

    // =========================================================
    // MODELO
    // =========================================================

    private Banco banco;


    // =========================================================
    // CAMPOS DEL LOGIN
    // =========================================================

    @FXML
    private TextField txtUserSignIn;

    @FXML
    private PasswordField txtPasswordSignIn;

    @FXML
    private TextField txtPasswordSignInMask;

    @FXML
    private CheckBox checkViewPassSignIn;

    @FXML
    private Button btnClean;


    // =========================================================
    // INICIALIZACIÓN
    // =========================================================

    @FXML
    private void initialize() {

        checkViewPassSignIn.setOnAction(
                this::actionEvent
        );
    }


    // =========================================================
    // RECIBIR BANCO
    // =========================================================

    public void setBanco(Banco banco) {

        this.banco = banco;
    }


    // =========================================================
    // EVENTOS
    // =========================================================

    @FXML
    private void actionEvent(ActionEvent event) {

        if (event.getSource() == btnClean) {

            limpiar();

        } else if (
                event.getSource() == checkViewPassSignIn
        ) {

            alternarVisibilidadPassword();

        } else {

            ingresar();
        }
    }


    // =========================================================
    // ENTER
    // =========================================================

    @FXML
    private void eventKey(KeyEvent event) {

        if ("\r".equals(event.getCharacter())) {

            ingresar();
        }
    }


    // =========================================================
    // INICIAR SESIÓN
    // =========================================================

    private void ingresar() {

        String usuario =
                txtUserSignIn.getText();

        String password =
                obtenerPassword();


        if (
                usuario.equals("admin")
                && password.equals("1234")
        ) {

            try {

                /*
                 * Cargar ventana principal.
                 */

                FXMLLoader loader =
                        new FXMLLoader(
                                getClass().getResource(
                                        "/View/ventanaPrincipal.fxml"
                                )
                        );


                Parent root =
                        loader.load();


                /*
                 * Obtener el controller de
                 * ventanaPrincipal.fxml.
                 */

                VentanaPrincipalController controller =
                        loader.getController();


                /*
                 * MUY IMPORTANTE:
                 *
                 * Le entregamos el mismo Banco
                 * que fue creado en App.
                 */

                controller.setBanco(banco);


                /*
                 * Crear ventana principal.
                 */

                Stage ventanaPrincipal =
                        new Stage();


                ventanaPrincipal.setTitle(
                        "DaniBanca"
                );


                ventanaPrincipal.setScene(
                        new Scene(root)
                );


                ventanaPrincipal.setWidth(
                        1200
                );


                ventanaPrincipal.setHeight(
                        700
                );


                ventanaPrincipal.setMinWidth(
                        1000
                );


                ventanaPrincipal.setMinHeight(
                        600
                );


                ventanaPrincipal.show();


                /*
                 * Cerrar ventana de Login.
                 */

                Stage ventanaLogin =
                        (Stage) txtUserSignIn
                                .getScene()
                                .getWindow();


                ventanaLogin.close();


            } catch (Exception e) {

                e.printStackTrace();
            }


        } else {

            mostrarError(
                    "Usuario o contraseña incorrectos"
            );
        }
    }


    // =========================================================
    // LIMPIAR
    // =========================================================

    private void limpiar() {

        txtUserSignIn.clear();

        txtPasswordSignIn.clear();

        txtPasswordSignInMask.clear();

        checkViewPassSignIn.setSelected(false);


        txtPasswordSignIn.setVisible(true);

        txtPasswordSignIn.setManaged(true);


        txtPasswordSignInMask.setVisible(false);

        txtPasswordSignInMask.setManaged(false);
    }


    // =========================================================
    // MOSTRAR / OCULTAR PASSWORD
    // =========================================================

    private void alternarVisibilidadPassword() {

        if (
                checkViewPassSignIn.isSelected()
        ) {

            txtPasswordSignInMask.setText(
                    txtPasswordSignIn.getText()
            );


            txtPasswordSignIn.setVisible(false);

            txtPasswordSignIn.setManaged(false);


            txtPasswordSignInMask.setVisible(true);

            txtPasswordSignInMask.setManaged(true);


        } else {

            txtPasswordSignIn.setText(
                    txtPasswordSignInMask.getText()
            );


            txtPasswordSignInMask.setVisible(false);

            txtPasswordSignInMask.setManaged(false);


            txtPasswordSignIn.setVisible(true);

            txtPasswordSignIn.setManaged(true);
        }
    }


    // =========================================================
    // OBTENER PASSWORD
    // =========================================================

    private String obtenerPassword() {

        return checkViewPassSignIn.isSelected()
                ? txtPasswordSignInMask.getText()
                : txtPasswordSignIn.getText();
    }


    // =========================================================
    // MOSTRAR ERROR
    // =========================================================

    private void mostrarError(String mensaje) {

        Alert alerta =
                new Alert(
                        Alert.AlertType.ERROR
                );


        alerta.setTitle(
                "Error de inicio de sesión"
        );


        alerta.setHeaderText(null);


        alerta.setContentText(
                mensaje
        );


        alerta.showAndWait();
    }
}