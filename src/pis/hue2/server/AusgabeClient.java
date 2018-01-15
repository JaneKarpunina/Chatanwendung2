package pis.hue2.server;

import pis.hue2.server.ereignis.Ereignis;

public interface AusgabeClient {

    void wirteInTable(Ereignis ereignis);
}
