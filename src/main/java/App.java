import Controller.LoginController;
import Model.Banco;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        /*
         * Creamos UNA sola instancia de Banco.
         *
         * Esta instancia será utilizada durante
         * toda la ejecución de la aplicación.
         */

        Banco banco = new Banco();


        /*
         * Cargar Login
         */

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "/View/login.fxml"
                )
        );


        Parent root = loader.load();


        /*
         * Obtener el controller del Login.
         *
         * Más adelante podremos pasarle el Banco
         * si el login necesita utilizarlo.
         */

        LoginController controller =
                loader.getController();

        controller.setBanco(banco);


        /*
         * Crear escena
         */

        Scene scene = new Scene(root);


        stage.setTitle("Login");

        stage.setScene(scene);

        stage.setWidth(900);

        stage.setHeight(600);

        stage.setResizable(false);

        stage.show();
    }


    public static void main(String[] args) {

        launch(args);
    }
}