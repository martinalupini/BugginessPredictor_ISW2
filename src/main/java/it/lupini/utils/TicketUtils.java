package it.lupini.utils;

import it.lupini.controller.Proportion;
import it.lupini.model.Release;
import it.lupini.model.Ticket;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TicketUtils {

    private TicketUtils(){}

    public static List<Ticket> addIVandAV(List<Ticket> ticketsList, List<Release> releasesList) throws IOException, URISyntaxException {
        List<Ticket> finalTicketsList = new ArrayList<>();

        Proportion proportion = new Proportion();

        for(Ticket ticket: ticketsList){
            if(ticket.getAv().isEmpty()){
                proportion.fixTicketWithProportion(ticket, releasesList);
                ReleaseUtils.completeAV(ticket, releasesList);
            }else{
                proportion.addProportion(ticket);
                ReleaseUtils.completeAV(ticket, releasesList);
            }
            finalTicketsList.add(ticket);
        }

        return finalTicketsList;
    }

    public static List<Ticket> returnConsistentTickets(List<Ticket> ticketList, LocalDate resolutionDate){
        List<Ticket> correctTicket = new ArrayList<>();

        for(Ticket ticket: ticketList){
            if(!ticket.getAv().isEmpty() && ticket.getResolutionDate().isBefore(resolutionDate))  correctTicket.add(ticket);
        }

        return correctTicket;
    }

}
