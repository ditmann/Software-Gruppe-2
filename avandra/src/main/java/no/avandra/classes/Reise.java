package no.avandra.classes;

import java.time.LocalDate;

public class Reise {

    private Destinasjon destinasjon_obj;
    private String reiseNavn_str;
    private LocalDate startTid_LocalDate;

    public Reise(Destinasjon destinasjon_obj, String reiseNavn_str, LocalDate startTid_LocalDate) {
        this.destinasjon_obj = destinasjon_obj;
        this.reiseNavn_str = reiseNavn_str;
        this.startTid_LocalDate = startTid_LocalDate;
    }

    public Destinasjon getDestinasjon_obj() {
        return destinasjon_obj;
    }

    public void setDestinasjon_obj(Destinasjon destinasjon_obj) {
        this.destinasjon_obj = destinasjon_obj;
    }

    public String getReiseNavn_str() {
        return reiseNavn_str;
    }

    public void setReiseNavn_str(String reiseNavn_str) {
        this.reiseNavn_str = reiseNavn_str;
    }

    public LocalDate getStartTid_LocalDate() {
        return startTid_LocalDate;
    }

    public void setStartTid_LocalDate(LocalDate startTid_LocalDate) {
        this.startTid_LocalDate = startTid_LocalDate;
    }
}
