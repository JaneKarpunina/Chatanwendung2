package pis.hue2.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import pis.hue2.common.Nachricht;

import java.io.IOException;
import java.util.Arrays;

public class GUIClient implements Ausgabe {

    public void setStopped(boolean stopped) {
        isStopped = stopped;
    }

    private boolean isStopped = true;

    private Thread listenerClient;

    public void setTransceiver(Transceiver transceiver) {
        this.transceiver = transceiver;
    }

    private Transceiver transceiver;

    @FXML
    TextArea nachricht;

    @FXML
    TextArea ausgehendeNachricht;

    @FXML
    TextField command;

    @FXML
    ListView<String> clientNames = new ListView<>();
    private final ObservableList<String> data =
            FXCollections.observableArrayList();

    @FXML
    protected void initialize() {
        nachricht.setEditable(false);

        clientNames.setPrefSize(200, 250);
        clientNames.setItems(data);
    }

    @FXML
    public void senden(ActionEvent event) {
        Nachricht nachricht = new Nachricht();
        nachricht.setCommand(command.getText());
        nachricht.setMessages(Arrays.asList(ausgehendeNachricht.getText()));
        if (listenerClient != null && listenerClient.isAlive()) {
            try {
                transceiver.sendNachricht(nachricht);
            } catch (IOException ex) {
                System.err.println("Error: " + ex.getMessage());
            }
        }
    }

    @FXML
    public void start(ActionEvent event) {
        transceiver.setAusgabe(this);
        if (isStopped) {
            listenerClient = new Thread(transceiver);
            listenerClient.start();
            isStopped = false;
        }
    }

    @FXML
    public void stop(ActionEvent event) {
        stopClientThread();
    }

    public void setNachricht(String nachricht) {
        this.nachricht.setText(nachricht);
    }

    public void setCommand(String command) {
        this.command.setText(command);
    }

    public void refreshClientList(ObservableList<String> data) {
        clientNames.setItems(data);
    }

    public void onCloseRequest() {
        stopClientThread();
    }

    private void stopClientThread() {
        transceiver.closeResources();
        if (listenerClient != null)
            listenerClient.interrupt();
        isStopped = true;
    }
}
