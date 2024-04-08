package it.Lupini.utils;

import it.Lupini.model.Release;
import it.Lupini.model.Ticket;
import org.json.JSONArray;

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

        //removeAndSetAVTickets(halfRelease, ticketList);

        return half;
    }


    //SECONDO ME NON SERVE
    /*
    public static void removeAndSetAVTickets(int halfRelease, List<Ticket> ticketList) {

        Iterator<Ticket> iterator = ticketList.iterator();
        while (iterator.hasNext()) {
            Ticket t = iterator.next();
            if (t.getIV() > halfRelease) {    //se IV > halfRelease --> tolgo ticket
                iterator.remove();
            }
            if (t.getOV() > halfRelease || t.getFV() > halfRelease) {       //Se IV < half, ma OV o FV sono > half? tutte le versioni da IV < half ad half sono AV.
                List<Integer> affectedVersionsListByTicket = new ArrayList<>();
                for (int k = t.getIV(); k < halfRelease; k++) {
                    affectedVersionsListByTicket.add(k);
                }
                t.setAV(affectedVersionsListByTicket);
            }
        }
    }

     */

}
