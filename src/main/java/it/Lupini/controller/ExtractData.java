package it.Lupini.controller;

import it.Lupini.model.Release;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

public class ExtractData {

    private static final Logger logger = Logger.getLogger(ExtractData.class.getName());

    public static void extractData(String project, String repoURL) throws IOException, URISyntaxException, GitAPIException {

        ExtractFromJira jiraExtractor = new ExtractFromJira(project.toUpperCase());
        List<Release> releaseList = jiraExtractor.extractAllReleases();

        logger.info(project+" releases extracted.");

        ExtractFromGit gitExtractor = new ExtractFromGit();
        List<RevCommit> commitList = gitExtractor.getAllCommits(releaseList, project);

    }
}
