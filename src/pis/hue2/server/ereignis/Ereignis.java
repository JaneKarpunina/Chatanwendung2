package pis.hue2.server.ereignis;

import javafx.beans.property.SimpleStringProperty;
/**
 * Dieser Klass wird an der Serverseite benutzt um alle Eriegnisse z. B.
* Verbindungsversuch oder ausgehende Nachricht zu zeigen.
* @author Jane
* */
public class Ereignis {

    private final SimpleStringProperty formattedDate;
    private final SimpleStringProperty name;
    private final SimpleStringProperty command;
    private final SimpleStringProperty nachricht;

    public Ereignis(String formattedDate, String name, String command, String nachricht) {
        this.formattedDate = new SimpleStringProperty(formattedDate);
        this.name = new SimpleStringProperty(name);
        this.command = new SimpleStringProperty(command);
        this.nachricht = new SimpleStringProperty(nachricht);
    }

    public String getFormattedDate() {
        return formattedDate.get();
    }

    public SimpleStringProperty formattedDateProperty() {
        return formattedDate;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate.set(formattedDate);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getCommand() {
        return command.get();
    }

    public SimpleStringProperty commandProperty() {
        return command;
    }

    public void setCommand(String command) {
        this.command.set(command);
    }

    public String getNachricht() {
        return nachricht.get();
    }

    public SimpleStringProperty nachrichtProperty() {
        return nachricht;
    }

    public void setNachricht(String nachricht) {
        this.nachricht.set(nachricht);
    }
}
