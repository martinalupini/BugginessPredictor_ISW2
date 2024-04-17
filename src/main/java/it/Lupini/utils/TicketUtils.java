package it.Lupini.utils;

import it.Lupini.controller.Proportion;
import it.Lupini.model.Release;
import it.Lupini.model.Ticket;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class TicketUtils {

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

    public static List<Ticket> returnConsistentTickets(List<Ticket> ticketList){
        List<Ticket> correctTicket = new ArrayList<>();

        for(Ticket ticket: ticketList){
            if(!ticket.getAv().isEmpty())  correctTicket.add(ticket);
        }

        return correctTicket;
    }

}
