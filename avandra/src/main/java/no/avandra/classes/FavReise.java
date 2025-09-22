package no.avandra.classes;

import java.time.LocalDate;

public class FavReise extends Reise{

    private String navn_str;
    private String ikon_str; //ref til bildefil

    public FavReise(Destinasjon destinasjon_obj, String reiseNavn_str, LocalDate startTid_LocalDate, String navn_str, String ikon_str) {
        super(destinasjon_obj, reiseNavn_str, startTid_LocalDate);
        this.navn_str = navn_str;
        this.ikon_str = ikon_str;
    }

    public String getNavn_str() {
        return navn_str;
    }

    public void setNavn_str(String navn_str) {
        this.navn_str = navn_str;
    }

    public String getIkon_str() {
        return ikon_str;
    }

    public void setIkon_str(String ikon_str) {
        this.ikon_str = ikon_str;
    }
}
