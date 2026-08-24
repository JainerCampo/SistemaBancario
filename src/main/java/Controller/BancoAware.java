package Controller;

import Model.Banco;
import javafx.scene.layout.BorderPane;

/**
 * Contrato para los controladores de vistas que necesitan acceder al banco
 * compartido y navegar dentro de la ventana principal.
 */
public interface BancoAware {

    /** Recibe la instancia única del banco compartida por la aplicación. */
    void setBanco(Banco banco);

    /** Recibe el contenedor principal para permitir navegación entre vistas. */
    void setVentanaPrincipal(BorderPane ventanaPrincipal);
}
