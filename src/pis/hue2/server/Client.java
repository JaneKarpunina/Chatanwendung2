package pis.hue2.server;

import javafx.application.Platform;
import pis.hue2.common.Nachricht;
import pis.hue2.server.ereignis.Ereignis;
import pis.hue2.server.teilnehmer.Teilnehmer;
import pis.hue2.server.teilnehmer.TeilnehmerListe;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static java.time.LocalTime.now;
import static java.time.
        temporal.ChronoUnit.MINUTES;

public class Client implements Runnable {

    private static final int TIMEOUT = 3000;
    private Socket socket;
    private boolean isRegistered = false;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private LocalTime startTime;
    private LocalTime currentTime;
    private Teilnehmer teilnehmer;
    private boolean removeTeilnehmer = true;

    private AusgabeClient ausgabeClient;


    Client(Socket connection) {
        this.socket = connection;
        try {
            socket.setSoTimeout(TIMEOUT);
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        } catch (IOException se) {
            System.err.println("Unable to set socket option SO_TIMEOUT");
        }
    }

    @Override
    public void run() {
        try {
            communicateWithClient();
        } catch (IOException ex) {
            System.err.println();
        }
    }

    private void communicateWithClient() throws IOException {
        startTime = now();
        Nachricht nachricht;
        try {
            while (!Thread.interrupted()) {
                nachricht = new Nachricht();
                currentTime = now();
                if (!isRegistered && MINUTES.between(startTime, currentTime) >= 1) break;
                readMessage(nachricht);
                if (nachricht.getCommand() == null) continue;
                writeToTable(nachricht);
                answerToClient(nachricht);
            }
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            if (removeTeilnehmer) TeilnehmerListe.getInstance().entfernen(teilnehmer);
            closeResources();
        }
    }

    private void closeResources() throws IOException {
        if (inputStream != null)
            inputStream.close();
        if (outputStream != null) {
            outputStream.flush();
            outputStream.close();
        }
        socket.close();
    }

    private void readMessage(Nachricht nachricht) throws IOException {
        try {
            String text = inputStream.readUTF();
            String[] array = text.substring(0, text.length() - 1).split(":");
            if (array[0] != null) {
                nachricht.setCommand(array[0]);
                System.out.println("Message red: " + nachricht.getCommand());
                nachricht.setMessages(Arrays.asList(array).subList(1, Arrays.asList(array).size()));
            }

        } catch (SocketTimeoutException ex) {
        } catch (EOFException ex) {
            throw new RuntimeException("EOFException");
        } catch (IOException ex) {
        } finally {
            if (outputStream != null) outputStream.flush();
        }
    }

    private void writeToTable(Nachricht nachricht) {
        Platform.runLater(new Runnable() {
                              public void run() {
                                  String clientName = "";
                                  if (teilnehmer != null) clientName = teilnehmer.getName();
                                  if (nachricht.getMessages() != null && !nachricht.getMessages().isEmpty()) {
                                      StringBuilder body = new StringBuilder();
                                      nachricht.getMessages().forEach(e ->
                                              body.append(e + ":")
                                      );
                                      if (body.length() != 0) body.deleteCharAt(body.length() - 1);
                                      ausgabeClient.wirteInTable(new Ereignis(now().toString(),
                                              clientName, nachricht.getCommand(), body.toString()));
                                  }
                                  else ausgabeClient.wirteInTable(new Ereignis(now().toString(),
                                          clientName, nachricht.getCommand(), ""));
                              }
                          }

        );

    }

    private void answerToClient(Nachricht nachricht) throws IOException {
        if (!isRegistered) {
            registerClient(nachricht);
        } else {
            communicate(nachricht);
        }
    }

    private void registerClient(Nachricht nachricht) throws IOException {
        String command = nachricht.getCommand();
        if (!"connect".equals(command)) {
            closeConnection("disconnect", Arrays.asList("invalid_command"),
                    "Invalid command");
        } else {
            TeilnehmerListe teilnehmerListe = TeilnehmerListe.getInstance();
            register(nachricht);
            sendClientList(teilnehmerListe, nachricht);
            outputStream.writeUTF("connect:ok\n");
            isRegistered = true;
        }
    }

    private void communicate(Nachricht nachricht) throws IOException {
        if (!"message".equals(nachricht.getCommand()) && !"disconnect".equals(nachricht.getCommand())) {
            closeConnection("disconnect", Arrays.asList("invalid_command"),
                    "Invalid command");
        }
        if ("disconnect".equals(nachricht.getCommand())) {
            closeConnection("disconnect", Arrays.asList("ok"),
                    "Disconnect client");
        } else {
            StringBuilder messages = new StringBuilder();
            StringBuilder body = new StringBuilder();
            nachricht.getMessages().forEach(e ->
                    body.append(e + ":")
            );
            if (body.length() != 0) body.deleteCharAt(body.length() - 1);
            if (!Pattern.compile("[^\\s]*").matcher(body.toString()).matches()) {
                messages.append("invalid_message:\n");
                outputStream.writeUTF(messages.toString());
            }
            else {
                messages.append("message:");
                messages.append(teilnehmer.getName());
                if (nachricht.getMessages() != null && !nachricht.getMessages().isEmpty())
                    messages.append(":" + body.toString());
                messages.append("\n");
                sendToAllClients(messages);
            }
        }
    }

    private void sendClientList(TeilnehmerListe teilnehmerListe,
                                Nachricht nachricht) throws IOException {
        StringBuilder messages = new StringBuilder();
        messages.append("namelist:");
        teilnehmerListe.getTeilnehmerList().forEach(e -> {
            messages.append(e.getName());
            messages.append(":");
        });
        messages.append("\n");
        sendToAllClients(messages);
    }

    private void sendToAllClients(StringBuilder messages) throws IOException {
        TeilnehmerListe teilnehmerListe = TeilnehmerListe.getInstance();
        teilnehmerListe.getTeilnehmerList().forEach(e -> {
            try {
                if (!e.getName().equals(teilnehmer.getName())) {
                    e.getDataOutputStream().writeUTF(messages.toString());
                }
            } catch (IOException ex) {
                System.err.println("IOException while sending list of clients");
                ex.printStackTrace();
            }

        });
        outputStream.writeUTF(messages.toString());
    }

    private void register(Nachricht nachricht)
            throws IOException {
        TeilnehmerListe teilnehmerListe = TeilnehmerListe.getInstance();
        if (teilnehmerListe.getTeilnehmerList().size() >= 3) {
            closeConnection("refused", Arrays.asList("too_many_users"),
                    "Too many users");
        }
        try {
            if (nachricht.getMessages() != null && !nachricht.getMessages().isEmpty()) {
                if (nachricht.getMessages().size() > 1)
                    throw new IllegalArgumentException("Soll keine : enthalten");
                teilnehmer = new Teilnehmer(socket, nachricht.getMessages().get(0), Thread.currentThread(), outputStream);
            }
            else throw new RuntimeException("Client Name soll gegeben sein");
            if (!teilnehmerListe.einfugen(teilnehmer)) {
                removeTeilnehmer = false;
                closeConnection("refused", Arrays.asList("name_in_use"),
                        "Name in use");
            }

        } catch (IllegalArgumentException ex) {
            closeConnection("refused", Arrays.asList("invalid_name"),
                    "Invalid name");
        }
    }

    private void closeConnection(String command, List<String> messages,
                                 String exceptionText) throws IOException {
        Nachricht antwort = new Nachricht();
        antwort.setCommand(command);
        antwort.setMessages(messages);
        invalidMessage(antwort);
        throw new RuntimeException(exceptionText);
    }

    private void invalidMessage(Nachricht antwort) throws IOException {
        outputStream.writeUTF(antwort.getCommand() + ":"
                + antwort.getMessages().get(0) + "\n");
    }

    public AusgabeClient getAusgabeClient() {
        return ausgabeClient;
    }

    public void setAusgabeClient(AusgabeClient ausgabeClient) {
        this.ausgabeClient = ausgabeClient;
    }
}
