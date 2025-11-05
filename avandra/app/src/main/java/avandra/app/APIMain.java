package avandra.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import avandra.core.adapter.IpGeolocationAdapter;
import avandra.core.port.DBConnectionPort;
import avandra.storage.adapter.MongoDBHandlerPort;
import org.bson.Document;

import avandra.storage.adapter.MongoDBConnectionPort;
import com.fasterxml.jackson.databind.ObjectMapper;

import avandra.api.EnturHttpClient;
import avandra.core.adapter.RandomLocationAdapter;
import avandra.core.domain.Coordinate;
import avandra.core.domain.TripPart;
import avandra.core.port.DBHandlerPort;
import avandra.core.port.EnturClient;
import avandra.core.port.LocationPort;
import avandra.storage.adapter.TripFileHandler;

public class APIMain {

    public static void main(String[] args) throws Exception {

        Scanner in = new Scanner(System.in);

        // lager "services" vi bruker videre
        String clientName = "HIOFsTUD-AVANDRA";
        EnturClient entur = new EnturHttpClient(clientName); // snakker med entur
        DBConnectionPort connection = new MongoDBConnectionPort();
        LocationPort location = null;
        DBHandlerPort db = new MongoDBHandlerPort(connection);                 // snakker med mongodb
        TripFileHandler files = new TripFileHandler(entur, new ObjectMapper()); // lager reiseplaner

        // ytre while: gjør at vi kan logge ut og logge inn som en annen bruker
        while (true) {
            System.out.println("=== Velkommen til Avandra ===");
            System.out.println("");

            // henter ALLE brukerne fra databasen (kan inneholde duplikater)
            ArrayList<Document> alleBrukereRaw = (ArrayList<Document>) db.retrieveAllData();
            if (alleBrukereRaw == null || alleBrukereRaw.isEmpty()) {
                System.out.println("Ingen brukere funnet i databasen. Avslutter.");
                return;
            }

            // fjerner duplikater basert på id (navn)
            ArrayList<Document> alleBrukere = new ArrayList<Document>();
            ArrayList<String> settAvNavn = new ArrayList<String>();

            for (Document d : alleBrukereRaw) {
                String navn = d.getString("id");
                if (navn == null) {
                    continue;
                }
                if (!settAvNavn.contains(navn)) {
                    settAvNavn.add(navn);
                    alleBrukere.add(d);
                }
            }

            if (alleBrukere.isEmpty()) {
                System.out.println("Fant ingen gyldige brukere.");
                return;
            }

            // viser lista over brukere en gang (nå uten duplikater)
            System.out.println("Hvem vil du logge inn som?");
            for (int i = 0; i < alleBrukere.size(); i++) {
                Document d = alleBrukere.get(i);

                Object alder = d.get("alder"); // noen har alder, noen har ikke

                if (alder != null) {
                    System.out.println((i + 1) + ") " + d.get("id") + " (alder: " + alder + ")");
                } else {
                    System.out.println((i + 1) + ") " + d.get("id"));
                }
            }

            // brukeren velger hvem de logger inn som (tall fra lista)
            int brukerValg = lesValg(in, 1, alleBrukere.size());
            Document aktivBruker = alleBrukere.get(brukerValg - 1);

            // henter navnet og admin-flag
            String aktivNavn = aktivBruker.getString("id");
            boolean erAdmin = isAdmin(aktivBruker);

            System.out.println("");
            System.out.println("Logget inn som: " + aktivNavn + " (admin=" + erAdmin + ")");
            System.out.println("");

            // indre while: meny for DEN brukeren vi er logget inn som
            // vi holder oss i denne til du velger "logg ut"
            while (true) {

                // skriver meny
                System.out.println("Hva vil du gjøre?");
                System.out.println("1) Reise til en destinasjon");

                // disse valgene kun for admin-brukere
                if (erAdmin) {
                    System.out.println("2) Legge til ny favoritt (for meg)");
                    System.out.println("3) Fjerne en favoritt (for meg)");

                    // admin kan få lov til å styre andre brukere (litebrukere)
                    if (aktivBruker.containsKey("litebrukere")) {
                        System.out.println("4) Administrer litebrukere");
                    }
                }

                System.out.println("0) Logg ut");

                // bestem hva som er høyeste gyldige valg i menyen akkurat nå
                int maxValg;
                if (erAdmin) {
                    // hvis admin OG har litebrukere, meny går til 4
                    if (aktivBruker.containsKey("litebrukere")) {
                        maxValg = 4;
                    } else {
                        // admin men uten litebrukere -> stopper på 3
                        maxValg = 3;
                    }
                } else {
                    // vanlig bruker -> bare valg 1 (reise)
                    maxValg = 1;
                }

                // les valg fra bruker
                int valg = lesValg(in, 0, maxValg);

                // 0 = logg ut av denne brukeren, gå tilbake til outer while
                if (valg == 0) {
                    break;
                }

                // 1 = reis til en av favorittene dine
                if (valg == 1) {
                    System.out.println("(1) Random location");
                    System.out.println("(2) IP based loaction");
                    location = ipPortPicker(in, clientName);
                    reis(in, aktivBruker, db, files, location);
                }

                // 2 = legg til ny favoritt (bare admin får lov)
                if (valg == 2 && erAdmin) {
                    leggTilFavoritt(in, aktivBruker, db);
                }

                // 3 = fjern en favoritt (bare admin)
                if (valg == 3 && erAdmin) {
                    fjernFavoritt(in, aktivBruker, db);
                }

                // 4 = adminstyr brukere som ligger i "litebrukere"
                if (valg == 4 && erAdmin && aktivBruker.containsKey("litebrukere")) {
                    adminMeny(in, aktivBruker, alleBrukere, db);
                }
            }

            // når vi har brutt ut fra indre while er vi "logget ut"
            // nå spør vi om hele programmet skal stoppe
            System.out.println("");
            System.out.println("Vil du avslutte hele programmet? (j/n)");
            if (!skalFortsette(in)) {
                System.out.println("Ha det!");
                return;
            }
            System.out.println("");
        }
    }

    // ======================= FUNKSJONER =======================

    // planlegg reise til en av favorittene
    private static void reis(Scanner in, Document bruker, DBHandlerPort db,
                             TripFileHandler files, LocationPort location) throws Exception {

        String navn = bruker.getString("id");

        // henter favoritter til brukeren
        Object favObj = bruker.get("favoritter");
        if (!(favObj instanceof Document)) {
            System.out.println("Ingen favoritter registrert.");
            return;
        }

        Document favDoc = (Document) favObj;
        if (favDoc.isEmpty()) {
            System.out.println("Ingen favoritter registrert.");
            return;
        }

        // lager en liste av destinasjonsnavnene (keyene i favoritter-objektet)
        List<String> favorittNavn = new ArrayList<String>(favDoc.keySet());

        // la brukeren velge hvilken favoritt å reise til
        System.out.println("Velg destinasjon:");
        for (int i = 0; i < favorittNavn.size(); i++) {
            System.out.println((i + 1) + ") " + favorittNavn.get(i));
        }

        int valg = lesValg(in, 1, favorittNavn.size());
        String valgtDest = favorittNavn.get(valg - 1);

        // hent koordinatene (latitude/longitude) for den valgte destinasjonen
        Coordinate dest = db.searchDestination(navn, valgtDest);
        if (dest == null) {
            System.out.println("Fant ikke koordinater.");
            return;
        }

        // generer en tilfeldig startposisjon
        Coordinate start = location.currentCoordinate();

        System.out.println("Reiser fra: " + start.getLatitudeNum() + ", " + start.getLongitudeNUM());
        System.out.println("Til: " + valgtDest + " (" + dest.getLatitudeNum() + ", " + dest.getLongitudeNUM() + ")");
        System.out.println("");

        // spør TripFileHandler (som bruker EnturHttpClient) om en konkret rute
        File tripJson = files.planTripCoordsToFile(
                start.getLatitudeNum(), start.getLongitudeNUM(),
                dest.getLatitudeNum(), dest.getLongitudeNUM(),
                1,   // antall rute-forslag vi vil ha
                true // ta med ekstra info
        );

        // TripPart.tripParts() leser den fila og oversetter til "steg"
        List<TripPart> deler = TripPart.tripParts(tripJson);
        if (deler == null || deler.isEmpty()) {
            System.out.println("Fant ingen rute.");
            return;
        }

        System.out.println("=== Reiseplan ===");
        for (TripPart del : deler) {
            System.out.println(del.toString());
        }

        System.out.println("God tur " + navn + "!");
        System.out.println("");
    }

    // legger til en favoritt for deg selv (kun admin)
    private static void leggTilFavoritt(Scanner in, Document bruker, DBHandlerPort db) {
        if (!isAdmin(bruker)) {
            System.out.println("Du har ikke rettigheter til å legge til favoritter.");
            return;
        }

        String navn = bruker.getString("id");

        System.out.println("Navn på ny favoritt:");
        String destNavn = in.nextLine();

        System.out.println("Latitude:");
        String latStr = in.nextLine();

        System.out.println("Longitude:");
        String lonStr = in.nextLine();

        double lat = Double.parseDouble(latStr);
        double lon = Double.parseDouble(lonStr);

        // addDestinationToFavorites(brukerId, destNavn, adresse, lat, lon)
        // vi bruker destNavn også som "adresse"
        ((MongoDBHandlerPort) db).addDestinationToFavorites(navn, destNavn, destNavn, lat, lon);

        System.out.println("Favoritt '" + destNavn + "' lagt til for " + navn);
        System.out.println("");
    }

    // fjerner en favoritt for deg selv (kun admin)
    private static void fjernFavoritt(Scanner in, Document bruker, DBHandlerPort db) {
        if (!isAdmin(bruker)) {
            System.out.println("Du har ikke rettigheter til å fjerne favoritter.");
            return;
        }

        String navn = bruker.getString("id");

        // henter favoritter fra dokumentet
        Object favObj = bruker.get("favoritter");
        if (!(favObj instanceof Document)) {
            System.out.println("Ingen favoritter å fjerne.");
            return;
        }

        Document favDoc = (Document) favObj;
        if (favDoc.isEmpty()) {
            System.out.println("Ingen favoritter å fjerne.");
            return;
        }

        // liste med eksisterende favoritt-navn
        List<String> favorittNavn = new ArrayList<String>(favDoc.keySet());

        System.out.println("Velg destinasjon som skal fjernes:");
        for (int i = 0; i < favorittNavn.size(); i++) {
            System.out.println((i + 1) + ") " + favorittNavn.get(i));
        }

        int valg = lesValg(in, 1, favorittNavn.size());
        String valgtDest = favorittNavn.get(valg - 1);

        // removeData(collection, fieldToRemove, userId)
        ((MongoDBHandlerPort) db).removeData("brukere", "favoritter." + valgtDest, navn);

        System.out.println("Favoritt '" + valgtDest + "' fjernet for " + navn);
        System.out.println("");
    }

    // admin-meny: admin kan styre brukere i sin "litebrukere"-liste
    private static void adminMeny(Scanner in, Document admin, ArrayList<Document> alleBrukere, DBHandlerPort db) {

        // sikkerhet: sjekk at du faktisk er admin
        if (!isAdmin(admin)) {
            System.out.println("Du har ikke rettigheter til å administrere andre.");
            return;
        }

        // henter lista med litebrukere (f.eks. ["Christian", "Victoria", ...])
        List<String> litebrukere = admin.getList("litebrukere", String.class);
        if (litebrukere == null || litebrukere.isEmpty()) {
            System.out.println("Du har ingen litebrukere.");
            return;
        }

        System.out.println("Du kan administrere disse litebrukerne:");
        for (int i = 0; i < litebrukere.size(); i++) {
            System.out.println((i + 1) + ") " + litebrukere.get(i));
        }

        System.out.println("Velg hvilken litebruker du vil styre:");
        int valgBruker = lesValg(in, 1, litebrukere.size());
        String valgtLitebrukerNavn = litebrukere.get(valgBruker - 1);

        // finn Document-objektet til den brukeren vi valgte
        Document målBruker = null;
        for (Document b : alleBrukere) {
            String bNavn = b.getString("id");
            if (bNavn != null && bNavn.equals(valgtLitebrukerNavn)) {
                målBruker = b;
            }
        }

        if (målBruker == null) {
            System.out.println("Fant ikke brukeren " + valgtLitebrukerNavn);
            return;
        }

        System.out.println("");
        System.out.println("Hva vil du gjøre med " + valgtLitebrukerNavn + "?");
        System.out.println("1) Legge til favoritt");
        System.out.println("2) Fjerne favoritt");
        System.out.println("3) Reise med denne brukeren");
        int valg = lesValg(in, 1, 3);

        if (valg == 1) {
            leggTilFavorittForAndre(in, målBruker, db);
        } else if (valg == 2) {
            fjernFavorittForAndre(in, målBruker, db);
        } else if (valg == 3) {
            try {
                reis(
                        in,
                        målBruker,
                        db,
                        new TripFileHandler(new EnturHttpClient("HIOFsTUD-AVANDRA"), new ObjectMapper()),
                        new RandomLocationAdapter()
                );
            } catch (Exception e) {
                System.out.println("Feil under reiseplanlegging: " + e.getMessage());
            }
        }

        System.out.println("");
    }

    // admin legger til favoritt på en litebruker
    private static void leggTilFavorittForAndre(Scanner in, Document bruker, DBHandlerPort db) {
        String navn = bruker.getString("id");

        System.out.println("Navn på ny favoritt:");
        String destNavn = in.nextLine();

        System.out.println("Latitude:");
        String latStr = in.nextLine();

        System.out.println("Longitude:");
        String lonStr = in.nextLine();

        double lat = Double.parseDouble(latStr);
        double lon = Double.parseDouble(lonStr);

        ((MongoDBHandlerPort) db).addDestinationToFavorites(navn, destNavn, destNavn, lat, lon);

        System.out.println("Favoritt '" + destNavn + "' lagt til for " + navn);
    }

    // admin fjerner favoritt på en litebruker
    private static void fjernFavorittForAndre(Scanner in, Document bruker, DBHandlerPort db) {
        String navn = bruker.getString("id");
        Object favObj = bruker.get("favoritter");

        if (!(favObj instanceof Document)) {
            System.out.println("Ingen favoritter å fjerne.");
            return;
        }

        Document favDoc = (Document) favObj;
        if (favDoc.isEmpty()) {
            System.out.println("Ingen favoritter å fjerne.");
            return;
        }

        List<String> favorittNavn = new ArrayList<String>(favDoc.keySet());

        System.out.println("Velg destinasjon som skal fjernes:");
        for (int i = 0; i < favorittNavn.size(); i++) {
            System.out.println((i + 1) + ") " + favorittNavn.get(i));
        }

        int valg = lesValg(in, 1, favorittNavn.size());
        String valgtDest = favorittNavn.get(valg - 1);

        ((MongoDBHandlerPort) db).removeData("brukere", "favoritter." + valgtDest, navn);

        System.out.println("Favoritt '" + valgtDest + "' fjernet for " + navn);
    }

    // ======================= HJELPEFUNKSJONER =======================

    // sjekker om en Document-bruker har admin=true
    private static boolean isAdmin(Document bruker) {
        if (bruker.containsKey("admin")) {
            Object a = bruker.get("admin");
            if (a instanceof Boolean) {
                return (Boolean) a;
            }
        }
        return false;
    }

    // leser et tallvalg fra konsollen, nekter alt som er utenfor min..max
    private static int lesValg(Scanner in, int min, int max) {
        while (true) {
            String s = in.nextLine();
            try {
                int v = Integer.parseInt(s);
                if (v >= min && v <= max) return v;
            } catch (Exception e) {
                // ignorer bare, vi spør igjen under
            }
            System.out.println("Skriv et tall mellom " + min + " og " + max + ": ");
        }
    }

    // spør bruker om ja/nei, returnerer true hvis "j" / "ja"
    private static boolean skalFortsette(Scanner in) {
        while (true) {
            String s = in.nextLine().trim().toLowerCase();
            if (s.equals("j") || s.equals("ja")) return true;
            if (s.equals("n") || s.equals("nei")) return false;
            System.out.println("Skriv j eller n:");
        }
    }
    private static LocationPort ipPortPicker(Scanner in, String clientName) {
        while (true) {
            String s = in.nextLine().trim().toLowerCase();
            if (s.equals("1")) {  return new RandomLocationAdapter();} // gir random posisjon
            if (s.equals("2")) { return new IpGeolocationAdapter(clientName); // gir posisjon basert på ip
            }

        }
    }
}

