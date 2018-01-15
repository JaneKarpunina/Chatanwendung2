package pis.hue2.server.teilnehmer;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.regex.Pattern;

public class Teilnehmer {

    private Socket socket;
    private String name;
    private Thread thread;
    private DataOutputStream dataOutputStream;

    public Teilnehmer(Socket socket, String name, Thread thread, DataOutputStream dataOutputStream) {
        if (name == null || name.length() > 30 || name.length() < 3 ||
                !Pattern.compile("[^\\s:]*").matcher(name).matches())
            throw new IllegalArgumentException("Invalid client name");
        this.socket = socket;
        this.name = name;
        this.thread = thread;
        this.dataOutputStream = dataOutputStream;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public void setDataOutputStream(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }
}
