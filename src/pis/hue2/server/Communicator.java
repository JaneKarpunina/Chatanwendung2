package pis.hue2.server;

import pis.hue2.server.teilnehmer.TeilnehmerListe;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Communicator implements Transceiver {

    private static final int TIMEOUT = 3000;
    private static final int PORT = 5555;
    private TeilnehmerListe teilnehmerListe = TeilnehmerListe.getInstance();

    AusgabeClient ausgabeClient;


    @Override
    public void run() {
        try {
            startListener();
        } catch (SocketException ex) {
            System.err.println("Unable to set socket timeout");
        } catch (IOException ex) {
            System.err.println("IOException");
        }
    }

    private void startListener() throws IOException {
        ServerSocket ss = new ServerSocket(PORT);
        ss.setSoTimeout(TIMEOUT);
        while (!Thread.interrupted()) {
            startClientThread(ss);
        }
        teilnehmerListe.getTeilnehmerList().forEach(e ->
            e.getThread().interrupt()
        );
        ss.close();
    }

    private void startClientThread(ServerSocket ss) throws IOException {
        try {
            Socket s = ss.accept();
            Client client = new Client(s);
            client.setAusgabeClient(ausgabeClient);
            Thread thread = new Thread(client);
            thread.start();
        } catch (SocketTimeoutException s) {
        }
    }


    @Override
    public void setAusgabe(AusgabeClient ausgabeClient) {
        this.ausgabeClient = ausgabeClient;
    }
}
