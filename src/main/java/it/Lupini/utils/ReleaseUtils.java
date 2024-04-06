package it.Lupini.utils;

import it.Lupini.model.Release;
import org.json.JSONArray;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
}
