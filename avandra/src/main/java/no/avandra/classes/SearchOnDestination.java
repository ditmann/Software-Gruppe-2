package no.avandra.classes;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SearchOnDestination {

    private List<Destinasjon> all;

    public SearchOnDestination() {
        this.all = loadFromJson();
    }

    public List<Destinasjon> searchByName(String search) {
        if (search == null)
            return all;
        search = search.trim();
        if (search.isEmpty()) return all;





}

