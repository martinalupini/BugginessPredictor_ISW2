package it.lupini.controller;

import it.lupini.model.Release;
import it.lupini.model.Ticket;
import it.lupini.utils.MathUtils;
import it.lupini.utils.TicketUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;

public class Proportion {

    private List<Float> proportionList;

    private float totalProportion;

    private float pColdStart;


    private enum Projects {
        AVRO,
        SYNCOPE,
        STORM,
        ZOOKEEPER
    }

    public Proportion(){
        this.proportionList = new ArrayList<>();
        this.totalProportion = 0;
        this.pColdStart = 0;
    }

    public void fixTicketWithProportion(Ticket ticket, List<Release> releaseList) throws IOException, URISyntaxException {
        int estimatedIV;
        float proportion;

        if(proportionList.size() < 5){
            proportion = coldStart(ticket.getResolutionDate());
        }else{
            proportion = increment();
        }

        estimatedIV = obtainIV(proportion, ticket);

        for(Release release : releaseList){
            if(estimatedIV == release.id()){
                ticket.setIv(release);
                ticket.addAV(release);
            }
        }

    }

    private float increment() {
        return this.totalProportion / this.proportionList.size();
    }

    private float coldStart(LocalDate resolutionDate) throws IOException, URISyntaxException {

        if(pColdStart != 0)  return pColdStart;

        List<Float> proportionListTemp = new ArrayList<>();

        for(Projects project: Projects.values()){


            ExtractFromJira jiraExtractor = new ExtractFromJira(project.toString().toUpperCase());
            List<Release> releaseList = jiraExtractor.getAllReleases();
            List<Ticket> allTickets = jiraExtractor.getAllTickets(releaseList, false);


            //need to obtain all tickets that have AV set
            List<Ticket> consistentTickets = TicketUtils.returnConsistentTickets(allTickets, resolutionDate);
            if(consistentTickets.size() >= 5){

                Proportion proportion = new Proportion();

                for(Ticket t: consistentTickets){
                    proportion.addProportion(t);
                }

                proportionListTemp.add(proportion.increment());
            }

        }

        return MathUtils.median(proportionListTemp);


    }

    private int obtainIV(float proportion, Ticket ticket){
        int ov = ticket.getOv().id();
        int fv = ticket.getFv().id();
        int estimatedIV;

        if(ov!=fv){
            estimatedIV = max(1, (int)(fv - proportion*(fv - ov)));
        }else{
            estimatedIV = max(1, (int)(fv - proportion));
        }

        return estimatedIV;
    }

    public void addProportion(Ticket ticket) {
        int denominator;
        float proportion;
        int ov = ticket.getOv().id();
        int fv = ticket.getFv().id();

        if(ov == fv){
            denominator = 1;
        }else{
            denominator = fv-ov;
        }

        proportion = (float)(fv - ticket.getIv().id())/denominator;

        this.proportionList.add(proportion);
        this.totalProportion += proportion;

    }

}
