package it.Lupini.controller;

import it.Lupini.model.Release;
import it.Lupini.model.Ticket;
import it.Lupini.utils.TicketUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.max;

public class Proportion {

    private List<Float> proportionList;

    private float totalProportion;
    private float coldStartProportion;

    private float P_coldStart;


    private enum Projects {
        AVRO,
        SYNCOPE,
        STORM,
        ZOOKEEPER
    }

    public Proportion(){
        this.proportionList = new ArrayList<>();
        this.coldStartProportion = 0;
        this.totalProportion = 0;
        this.P_coldStart = 0;
    }

    public void fixTicketWithProportion(Ticket ticket, List<Release> releaseList) throws IOException, URISyntaxException {
        int estimatedIV;
        float proportion;

        if(proportionList.size() < 5){
            proportion = coldStart();
        }else{
            proportion = increment();
        }

        System.out.println("Proportion for ticket "+ticket.getTicketKey()+" "+proportion);

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

    private float coldStart() throws IOException, URISyntaxException {

        if(P_coldStart != 0)  return P_coldStart;

        List<Float> proportionList = new ArrayList<>();

        for(Projects project: Projects.values()){

            System.out.println(project);


            ExtractFromJira jiraExtractor = new ExtractFromJira(project.toString().toUpperCase());
            List<Release> releaseList = jiraExtractor.getAllReleases();
            List<Ticket> allTickets = jiraExtractor.getAllTickets(releaseList, false);


            //need to obtain all tickets that have AV set
            List<Ticket> consistentTickets = TicketUtils.returnConsistentTickets(allTickets);
            if(consistentTickets.size() >= 5){
                System.out.println("Calculating proportion if project "+project);
                Proportion proportion = new Proportion();

                for(Ticket t: consistentTickets){
                    proportion.addProportion(t);
                }

                proportionList.add(proportion.increment());
            }

        }

        System.out.println(proportionList);
        System.out.println("median is "+median(proportionList));
        return median(proportionList);


    }

    private int obtainIV(float proportion, Ticket ticket){
        int ov = ticket.getOv().id();
        int fv = ticket.getFv().id();
        int estimatedIV;

        if(ov==fv){
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

        proportion = (fv - ticket.getIv().id())/denominator;

        //System.out.println(proportion);
        this.proportionList.add(proportion);
        this.totalProportion += proportion;

    }

    private float median(List<Float> array){
        float median;

        Collections.sort(array);

        int size = array.size();
        if (size % 2 == 0) {
            median = (array.get((size / 2) - 1) + array.get(size / 2)) / 2;
        } else {
            median = array.get(size / 2);
        }

        return median;
    }
}
