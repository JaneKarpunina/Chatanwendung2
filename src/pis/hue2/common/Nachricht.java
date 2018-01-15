package pis.hue2.common;

import java.util.ArrayList;
import java.util.List;

public class Nachricht {

    String command;
    List<String> messages = new ArrayList<>();

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
