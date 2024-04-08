package it.Lupini.controller;

import it.Lupini.model.JavaFile;
import it.Lupini.model.Release;
import it.Lupini.model.Ticket;
import it.Lupini.utils.ReleaseUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

public class ExtractData {

    private static final Logger logger = Logger.getLogger(ExtractData.class.getName());

    public static void buildDataset(String project, String repoURL) throws IOException, URISyntaxException, GitAPIException {

        //This first part is related to the extraction of information from Git and Jira
        ExtractFromJira jiraExtractor = new ExtractFromJira(project.toUpperCase());
        List<Release> releaseList = jiraExtractor.getAllReleases();
        logger.info(project+" releases extracted.");

        ExtractFromGit gitExtractor = new ExtractFromGit(releaseList);
        List<RevCommit> commitList = gitExtractor.getAllCommits(releaseList, project);
        List<Ticket> ticketList = jiraExtractor.getAllTickets(releaseList);
        List<RevCommit> filteredCommitsOfIssues = gitExtractor.filterCommitsOfIssues(commitList, ticketList);
        //need to update the ticket list
        ticketList = gitExtractor.getTicketList();
        //removing half of the releases before extracting the classes
        releaseList =  ReleaseUtils.removeHalfReleases(releaseList, ticketList);
        gitExtractor.setReleaseList(releaseList);

        List<JavaFile> classes = gitExtractor.getClasses(releaseList);

    }
}
