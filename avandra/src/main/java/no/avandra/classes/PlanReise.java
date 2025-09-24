package no.avandra.classes;

import java.time.LocalDate;

public class PlanReise extends Reise{

    private LocalDate ankomsttid_LocalDate;

    public PlanReise(Destinasjon destinasjon_obj, String reiseNavn_str, LocalDate startTid_LocalDate, LocalDate ankomsttid_LocalDate) {
        super(destinasjon_obj, reiseNavn_str, startTid_LocalDate);
        this.ankomsttid_LocalDate = ankomsttid_LocalDate;
    }

    public LocalDate getAnkomsttid_LocalDate() {
        return ankomsttid_LocalDate;
    }


    public void setAnkomsttid_LocalDate(LocalDate ankomsttid_LocalDate) {
        this.ankomsttid_LocalDate = ankomsttid_LocalDate;
    }
}
