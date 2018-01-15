package pis.hue2.client;

import javafx.collections.ObservableList;

public interface Ausgabe {

    void setStopped(boolean stopped);

    void setNachricht(String nachricht);

    void setCommand(String command);

    void refreshClientList(ObservableList<String> data);
}
