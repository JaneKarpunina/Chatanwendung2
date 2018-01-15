package pis.hue2.server.teilnehmer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeilnehmerListe {

    private static volatile TeilnehmerListe instance;

    private List<Teilnehmer> teilnehmerList = new ArrayList<>();

    private TeilnehmerListe () {}

    public static TeilnehmerListe getInstance() {
        TeilnehmerListe localInstance = instance;
        if (localInstance == null) {
            synchronized (TeilnehmerListe.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new TeilnehmerListe();
                }
            }
        }
        return localInstance;
    }

    public synchronized boolean einfugen(Teilnehmer teilnehmer) {
        if (teilnehmerList.stream().map(Teilnehmer::getName)
                .filter(e -> e.equals(teilnehmer.getName()))
                .collect(Collectors.toList()).isEmpty()) {
            teilnehmerList.add(teilnehmer);
            return true;
        }
        return false;
    }

    public synchronized boolean entfernen(Teilnehmer teilnehmer) {
        List<Teilnehmer> teilnehmerList1 = teilnehmerList.stream().filter(e ->
                (!e.getName().equals(teilnehmer.getName()))).collect(Collectors.toList());
        if (teilnehmerList1.size() != teilnehmerList.size()) {
            teilnehmerList = teilnehmerList1;
            return true;
        }
        return false;
    }

    public List<Teilnehmer> getTeilnehmerList() {
        return teilnehmerList;
    }
}
