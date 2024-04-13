package it.Lupini.controller;

import it.Lupini.model.JavaFile;
import it.Lupini.model.Release;
import it.Lupini.model.Ticket;
import it.Lupini.utils.ReleaseUtils;
import it.Lupini.utils.ReportUtils;
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
        ReportUtils.printReleases(project, releaseList, "AllReleases.txt");
        logger.info(project+": releases extracted.");

        ExtractFromGit gitExtractor = new ExtractFromGit(project, repoURL, releaseList);
        List<RevCommit> commitList = gitExtractor.getAllCommits(releaseList, project);
        ReportUtils.printCommits(project, commitList, "AllCommits.txt");
        logger.info(project+": commits extracted.");

        List<Ticket> ticketList = jiraExtractor.getAllTickets(releaseList);
        ReportUtils.printTickets(project, ticketList);
        logger.info(project+": tickets extracted.");

        /*
        List<RevCommit> filteredCommitsOfIssues = gitExtractor.filterCommitsOfIssues(commitList, ticketList);
        //need to update the ticket list
        ticketList = gitExtractor.getTicketList();
        ReportUtils.printCommits(project, filteredCommitsOfIssues, "FilteredCommits.txt");
        logger.info(project+": commits filtered");

        //removing half of the releases before extracting the classes
        releaseList =  ReleaseUtils.removeHalfReleases(releaseList, ticketList);
        gitExtractor.setReleaseList(releaseList);
        ReportUtils.printReleases(project, releaseList, "HalfReleases.txt");
        logger.info(project+": removed half releases.");

        List<JavaFile> classes = gitExtractor.getClasses(releaseList);
        logger.info(project+" classes extracted.");

         */

        gitExtractor.terminate();

    }
}
