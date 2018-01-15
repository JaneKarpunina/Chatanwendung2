package pis.hue2.client;

import pis.hue2.common.Nachricht;

import java.io.IOException;

public interface Transceiver extends Runnable {

    void setAusgabe(Ausgabe ausgabeClient);
    void sendNachricht(Nachricht nachricht) throws IOException;
    void closeResources();

}
