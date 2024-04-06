package it.Lupini.controller;

import it.Lupini.model.*;
import it.Lupini.utils.JsonUtils;
import org.json.JSONArray;
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

    public List<Release> extractAllReleases() throws IOException, URISyntaxException {
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
}
