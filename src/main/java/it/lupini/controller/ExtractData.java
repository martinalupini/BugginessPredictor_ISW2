package it.lupini.controller;

import it.lupini.model.JavaClass;
import it.lupini.model.Release;
import it.lupini.model.Ticket;
import it.lupini.utils.ReleaseUtils;
import it.lupini.utils.ReportUtils;
import it.lupini.model.ClassifierEvaluation;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;
import java.util.logging.Logger;

public class ExtractData {

    private static int fromRelease = 3;

    private ExtractData(){}

    private static final Logger logger = Logger.getLogger(ExtractData.class.getName());

    public static void buildDataset(String project) throws Exception {
        String print;

        //This first part is related to the extraction of information from Git and Jira
        ExtractFromJira jiraExtractor = new ExtractFromJira(project.toUpperCase());
        List<Release> releaseList = jiraExtractor.getAllReleases();
        print = project+": releases extracted.";
        logger.info(print);

        List<Ticket> ticketList = jiraExtractor.getAllTickets(releaseList, true);
        ReportUtils.printTickets(project, ticketList);
        print = project+": tickets extracted.";
        logger.info(print);

        ExtractFromGit gitExtractor = new ExtractFromGit(project, releaseList);
        List<RevCommit> commitList = gitExtractor.getAllCommits(releaseList);
        ReportUtils.printCommits(project, commitList, "AllCommits.csv");
        print = project+": commits extracted.";
        logger.info(print);

        List<RevCommit> filteredCommitsOfIssues = gitExtractor.filterCommitsOfIssues(commitList, ticketList);
        //need to update the ticket list
        ticketList = gitExtractor.getTicketList();
        ReportUtils.printCommits(project, filteredCommitsOfIssues, "FilteredCommits.csv");
        ReportUtils.printSummary(project, ticketList, commitList, filteredCommitsOfIssues);
        ReportUtils.printReleases(project, releaseList, "AllReleases.csv");
        print = project+": commits filtered.";
        logger.info(print);


        //removing half of the releases before extracting the classes
        int toRelease = releaseList.size() / 2;
        releaseList =  ReleaseUtils.removeHalfReleases(releaseList);
        gitExtractor.setReleaseList(releaseList);
        ReportUtils.printReleases(project, releaseList, "HalfReleases.csv");
        print = project+": removed half releases.";
        logger.info(print);

        //extracting all the classes of the project
        List<JavaClass> classes = gitExtractor.getClasses(releaseList);
        ReportUtils.printClasses(project, classes);
        print = project+": classes extracted.";
        logger.info(print);

        //computing the metrics of the classes
        Metrics metrics = new Metrics(project,classes, gitExtractor);
        List<JavaClass> classesWithMetrics = metrics.computeMetrics();
        print = project+": metrics calculated.";
        logger.info(print);

        //labeling buggyness preserving time order and creating files
        metrics.addBuggynessAndCreateFiles(classesWithMetrics, ticketList, fromRelease, toRelease);
        print = project+": csv and arff files created.";
        logger.info(print);


        ClassifyWithWeka classify = new ClassifyWithWeka(project.toLowerCase(), toRelease-2, classesWithMetrics);
        List<ClassifierEvaluation> evaluations = classify.evaluateClassifiers();
        WriteCSV.writeFinalWekaResults(project, evaluations);



    }
}
