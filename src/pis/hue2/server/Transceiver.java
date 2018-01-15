package pis.hue2.server;

public interface Transceiver extends Runnable {

    void setAusgabe(AusgabeClient ausgabeClient);
}
