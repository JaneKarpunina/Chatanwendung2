package pis.hue2.server;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import pis.hue2.server.ereignis.Ereignis;

import java.util.ArrayList;
import java.util.List;

public class GUIServer implements AusgabeClient {

    public void setTransceiver(Transceiver transceiver) {
        this.transceiver = transceiver;
    }

    private Transceiver transceiver;

    private boolean isStopped = true;

    private Thread listener;

    @FXML
    GridPane gridPane;

    @FXML
    Button starten;

    @FXML
    Button stoppen;

    @FXML
    TableView<Ereignis> ereignisse = new TableView<>();
    private final ObservableList<Ereignis> data =
            FXCollections.observableArrayList();

    @FXML
    protected void initialize() {

        TableColumn formattedDate = new TableColumn("Datum");
        formattedDate.setMinWidth(100);
        formattedDate.setCellValueFactory(
                new PropertyValueFactory<Ereignis, String>("formattedDate"));

        TableColumn name = new TableColumn("Kundenname");
        name.setMinWidth(100);
        name.setCellValueFactory(
                new PropertyValueFactory<Ereignis, String>("name"));

        TableColumn command = new TableColumn("Command");
        command.setMinWidth(200);
        command.setCellValueFactory(
                new PropertyValueFactory<Ereignis, String>("command"));

        //todo: do this probably with tooltip
        TableColumn nachricht = new TableColumn("Nachricht");
        nachricht.setMinWidth(200);
        nachricht.setCellValueFactory(
                new PropertyValueFactory<Ereignis, String>("nachricht"));

        ereignisse.setItems(data);
        ereignisse.getColumns().addAll(formattedDate, name, command, nachricht);

    }

    private void removeData(int begin) {
        data.remove(0, begin);
    }


    @FXML
    public void start(ActionEvent event) {
        transceiver.setAusgabe(this);
        if (isStopped) {
            listener = new Thread(transceiver);
            listener.start();
            isStopped = false;
        }
    }

    @FXML
    public void stop(ActionEvent event) {
        stopServerThread();
    }

    @Override
    public void wirteInTable(Ereignis ereignis) {
        if (data.size() > 25) removeData(15);
        data.add(ereignis);
    }

    void onCloseRequest() {
        stopServerThread();
    }

    private void stopServerThread() {
        if(listener != null) listener.interrupt();
        isStopped = true;
    }

}
