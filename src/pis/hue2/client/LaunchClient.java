package pis.hue2.client;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import pis.hue2.client.Communicator;
import pis.hue2.client.Transceiver;

public class LaunchClient extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        //todo: probably copy past
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client.fxml"));
        Parent root = loader.load();
        GUIClient controller = loader.getController();
        Transceiver transceiver = new Communicator();
        controller.setTransceiver(transceiver);
        primaryStage.setTitle("ChatAnwendung");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(500);
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
