package it.lupini.controller;

import it.lupini.model.*;
import it.lupini.utils.JsonUtils;
import it.lupini.utils.ReleaseUtils;
import it.lupini.utils.TicketUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ExtractFromJira {

    private final String project;

    public ExtractFromJira(String project) {
        this.project = project.toUpperCase();
    }

    public List<Release> getAllReleases() throws IOException, URISyntaxException {
        List<Release> releaseList = new ArrayList<>();
        int i=0;
        String url = "https://issues.apache.org/jira/rest/api/latest/project/" + this.project;
        JSONObject json = JsonUtils.readJsonFromUrl(url);
        JSONArray versions = json.getJSONArray("versions");

        for (; i < versions.length(); i++) {
            String releaseName;
            String releaseDate;

            //obtaining single json object
            JSONObject releaseJsonObject = versions.getJSONObject(i);
            //creating Release model
            if (releaseJsonObject.has("releaseDate") && releaseJsonObject.has("name")) {
                releaseDate = releaseJsonObject.get("releaseDate").toString();
                releaseName = releaseJsonObject.get("name").toString();
                releaseList.add(new Release(releaseName, LocalDate.parse(releaseDate)));
            }
        }

        //sorting the versions based on the release date
        releaseList.sort(Comparator.comparing(Release::releaseDate));
        i = 0;
        for (Release release : releaseList) {
            release.setId(++i);
        }
        return releaseList;
    }


    public List<Ticket> getAllTickets(List<Release> releasesList, boolean fix) throws IOException, JSONException, URISyntaxException {
        List<Ticket> ticketsList = getTickets(releasesList);

        if(fix) {
            List<Ticket> fixedTicketsList;
            //after extracting all the commits they are completed by adding the AV (if missing) using proportion
            fixedTicketsList = TicketUtils.addIVandAV(ticketsList, releasesList);
            fixedTicketsList.sort(Comparator.comparing(Ticket::getResolutionDate));
            return fixedTicketsList;
        }else{
            return ticketsList;
        }

    }


    //Retrieving all tickets of type BUG with status CLOSED or RESOLVED and resolution equals to FIXED (not Unresolved or others)
    public List<Ticket> getTickets(List<Release> releasesList) throws IOException, URISyntaxException {
        int j;
        int i = 0;
        int total;
        List<Ticket> ticketsList = new ArrayList<>();
        do {
            j = i + 1000;
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + this.project + "%22AND%22issueType%22=%22Bug%22AND" +
                    "(%22status%22=%22Closed%22OR%22status%22=%22Resolved%22)" +
                    "AND%22resolution%22=%22Fixed%22&fields=key,versions,created,resolutiondate&startAt="
                    + i + "&maxResults=" + j;
            JSONObject json = JsonUtils.readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");
            for (; i < total && i < j; i++) {
                //Iterate through each bug to retrieve ID, creation date, resolution date and affected versions
                String key = issues.getJSONObject(i%1000).get("key").toString();
                JSONObject fields = issues.getJSONObject(i%1000).getJSONObject("fields");
                String creationDateString = fields.get("created").toString();
                String resolutionDateString = fields.get("resolutiondate").toString();
                LocalDate creationDate = LocalDate.parse(creationDateString.substring(0,10));
                LocalDate resolutionDate = LocalDate.parse(resolutionDateString.substring(0,10));
                JSONArray affectedVersionsArray = fields.getJSONArray("versions");

                //to obtain the opening version and the fixed version I use the creation date and the release date
                Release openingVersion = ReleaseUtils.getReleaseAfterOrEqualDate(creationDate, releasesList);
                Release fixedVersion =  ReleaseUtils.getReleaseAfterOrEqualDate(resolutionDate, releasesList);

                //obtaining the affected releases
                List<Release> affectedVersionsList = ReleaseUtils.returnAffectedVersions(affectedVersionsArray, releasesList);

                //checking if the ticket is not valid
                if(!affectedVersionsList.isEmpty()
                        && openingVersion!=null
                        && fixedVersion!=null
                        && (!affectedVersionsList.get(0).releaseDate().isBefore(openingVersion.releaseDate())
                        || openingVersion.releaseDate().isAfter(fixedVersion.releaseDate()))){
                    continue;
                }

                //the opening version must be different from the first release
                if(openingVersion != null && fixedVersion != null && openingVersion.id()!=releasesList.get(0).id()){
                    ticketsList.add(new Ticket(key, creationDate, resolutionDate, openingVersion, fixedVersion, affectedVersionsList));
                }

            }
        } while (i < total);
        ticketsList.sort(Comparator.comparing(Ticket::getResolutionDate));
        return ticketsList;
    }



}
