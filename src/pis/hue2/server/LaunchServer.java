package pis.hue2.server;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class LaunchServer extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("server.fxml"));
        Parent root = loader.load();
        GUIServer controller = loader.getController();
        Transceiver transceiver = new Communicator();
        controller.setTransceiver(transceiver);
        primaryStage.setTitle("ChatAnwendung");
        Scene scene  = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(700);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                controller.onCloseRequest();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
