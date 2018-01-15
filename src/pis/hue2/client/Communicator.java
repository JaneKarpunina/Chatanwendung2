package pis.hue2.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import pis.hue2.common.Nachricht;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class Communicator implements Transceiver {

    private static final int TIMEOUT = 5000;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Ausgabe ausgabe;

    @Override
    public void setAusgabe(Ausgabe ausgabe) {
        this.ausgabe = ausgabe;
    }

    Communicator() {
    }

    @Override
    public void run() {
        try {
            this.socket = new Socket("localhost", 5555);
            socket.setSoTimeout(TIMEOUT);
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            receiveAndShowMessages();
        } catch (IOException ex) {
            System.err.println("IOException has occured");
            ausgabe.setStopped(true);
        }
    }

    private void receiveAndShowMessages() throws IOException {
        Nachricht nachricht;
        try {
            while (!Thread.interrupted()) {
                nachricht = new Nachricht();
                readMessage(nachricht);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            System.out.println("in finally");
            closeResources();
        }
    }

    public void closeResources() {
        try {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
            if (inputStream != null) inputStream.close();
            if (socket != null) socket.close();
        } catch (IOException ex) {
            System.err.println("");
        }
        ausgabe.refreshClientList(FXCollections.observableArrayList());
        if (ausgabe != null) ausgabe.setStopped(true);
    }

    public void sendNachricht(Nachricht nachricht) throws IOException {
        try {
            StringBuilder messages = new StringBuilder();
            messages.append(nachricht.getCommand())
                    .append(":");
            if (!nachricht.getMessages().isEmpty())
                messages.append(nachricht.getMessages().get(0));
            messages.append("\n");
            outputStream.writeUTF(messages.toString());
        } catch (IOException ex) {
            System.out.println("IOException ocured");
            ex.printStackTrace();

        } finally {
            if (outputStream != null)
                outputStream.flush();
        }
    }

    private void readMessage(Nachricht nachricht) {
        try {
            parseMessage(nachricht);
            setGUI(nachricht);
            if ("refused".equals(nachricht.getCommand()) || "disconnect".equals(nachricht.getCommand()))
                throw new RuntimeException("Close socket");
        } catch (SocketTimeoutException ex) {
            //System.out.println("socket timeout reached");

        } catch (EOFException eof) {
            System.out.println("EOFException");
            throw new RuntimeException("EOFException: " + eof.getMessage());
        } catch (IOException ex) {
        }
    }

    private void parseMessage(Nachricht nachricht) throws IOException {
        String text = inputStream.readUTF();
        String text1 = text.substring(0, text.length() - 1);
        String command = text.substring(0, text1.indexOf(":"));
        nachricht.setCommand(command);
        if ("message".equals(command))
            nachricht.setMessages(Arrays.asList(text1.substring(text1.indexOf(":") + 1)));
        else {
            nachricht.setMessages(Arrays.asList(text1.split(":"))
                    .subList(1, Arrays.asList(text1.split(":")).size()));
        }
    }

    private void setGUI(Nachricht nachricht) {
        if (nachricht.getCommand() != null && !nachricht.getCommand().isEmpty()) {
            StringBuffer message = new StringBuffer();
            nachricht.getMessages().forEach(e ->
                    message.append(e)
                            .append(":")
            );
            if (message.length() != 0) message.deleteCharAt(message.length() - 1);
            if ("namelist".equals(nachricht.getCommand()))
                Platform.runLater(new Runnable() {
                                      public void run() {
                                          ausgabe.refreshClientList(FXCollections.observableArrayList
                                                  (Arrays.asList(message.toString().split(":"))));
                                      }
                                  }
                );
            else
                Platform.runLater(new Runnable() {
                                      public void run() {
                                          ausgabe.setCommand(nachricht.getCommand());
                                          ausgabe.setNachricht(message.toString());
                                      }
                                  }


                );
        }
    }
}
