package it.Lupini.utils;

import it.Lupini.model.Release;
import it.Lupini.model.Ticket;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

public class ReleaseUtils {

    public static List<Release> returnAffectedVersions(JSONArray affectedVersionsArray, List<Release> releasesList) {
        List<Release> existingAffectedVersions = new ArrayList<>();

        //iterating through the names of the affected versions
        for (int i = 0; i < affectedVersionsArray.length(); i++) {
            String affectedVersionName = affectedVersionsArray.getJSONObject(i).get("name").toString();

            //iterating through the releases to find the corresponding one
            for (Release release : releasesList) {
                if (Objects.equals(affectedVersionName, release.releaseName())) {
                    existingAffectedVersions.add(release);
                    break;
                }
            }
        }
        existingAffectedVersions.sort(Comparator.comparing(Release::releaseDate));
        return existingAffectedVersions;
    }

    public static Release getReleaseAfterOrEqualDate(LocalDate specificDate, List<Release> releasesList) {

        //sorting the releases by their date
        releasesList.sort(Comparator.comparing(Release::releaseDate));

        //the first release which has a date after or equal to the one given is returned
        for (Release release : releasesList) {
            if (!release.releaseDate().isBefore(specificDate)) {
                return release;
            }
        }
        return null;
    }


    public static List<Release> removeHalfReleases(List<Release> releasesList, List<Ticket> ticketList) {
        List<Release> half = new ArrayList<>();

        float releaseNumber = releasesList.size();
        int halfRelease = (int) Math.floor(releaseNumber / 2);

        releasesList.sort(Comparator.comparing(Release::releaseDate));

        for(int i =0; i< halfRelease; i++){
            half.add(releasesList.get(i));
        }

        return half;
    }

    public static Release getReleaseOfCommit(RevCommit commit, List<Release> releaseList) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        LocalDate commitDate = LocalDate.parse(formatter.format(commit.getCommitterIdent().getWhen()));
        LocalDate lowerBoundDate = LocalDate.parse(formatter.format(new Date(0)));
        for (Release release : releaseList) {
            LocalDate dateOfRelease = release.releaseDate();
            if (commitDate.isAfter(lowerBoundDate) && !commitDate.isAfter(dateOfRelease)) {
                return release;
            }
            lowerBoundDate = dateOfRelease;
        }
        return null;
    }

    public static void completeAV(Ticket ticket, List<Release> releases){
        int iv = ticket.getIv().id();
        int fv = ticket.getFv().id();

        for(Release release : releases){
            if(release.id() > iv && release.id() < fv ){
                ticket.addAV(release);
            }
        }

    }


}
