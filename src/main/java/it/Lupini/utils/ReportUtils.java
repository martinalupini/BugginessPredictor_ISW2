package it.Lupini.utils;

import it.Lupini.model.Release;
import it.Lupini.model.Ticket;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class ReportUtils {

    public static final String DELIMITER = "\n------------------------------------------------------------------------------------------------\n";
    public static final String CLASS = ReportUtils.class.getName();
    private static final Logger logger = Logger.getLogger(CLASS);

    public static void printSummary(String project, List<Release> releaseList, List<Ticket> ticketList, List<RevCommit> commitList, List<RevCommit> filteredCommitsOfIssues) throws IOException {
        project = project.toLowerCase();
        File file = new File("reportFiles/" + project);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }

        file = new File("reportFiles/" + project + "Summary.txt");
        try(FileWriter fileWriter = new FileWriter(file)) {

            fileWriter.append("----------------------------------------------------------\n")
                    .append("EXTRACTION INFO:\n")
                    .append(String.valueOf(releaseList.size())).append(" RELEASES \n")
                    .append(String.valueOf(ticketList.size())).append(" TICKETS \n")
                    .append(String.valueOf(commitList.size())).append(" TOTAL COMMITS \n")
                    .append(String.valueOf(filteredCommitsOfIssues.size())).append(" COMMITS CONTAINING BUG-ISSUES")
                    .append("\n----------------------------------------------------------\n\n");

            flushAndCloseFW(fileWriter, logger, CLASS);
        } catch (IOException e) {
            logger.info("Error in writeOnReportFiles when trying to create directory");
        }
    }

    public static void printCommits( String project, List<RevCommit> commitList, String name) throws IOException {
        project = project.toLowerCase();
        File file = new File("reportFiles/" + project);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }

        file = new File("reportFiles/" + project+ "/" + name);
        try(FileWriter fileWriter = new FileWriter(file)) {

            for (RevCommit commit: commitList){
                fileWriter.append("id= ").append(commit.getName())
                        .append(", committer= ").append(commit.getCommitterIdent().getName())
                        .append(", message= ").append(commit.getFullMessage())
                        .append(", creationDate= ").append(String.valueOf(LocalDate.parse((new SimpleDateFormat("yyyy-MM-dd").format(commit.getCommitterIdent().getWhen()))))).append(DELIMITER);
            }

            flushAndCloseFW(fileWriter, logger, CLASS);
        } catch (IOException e) {
            logger.info("Error in writeOnReportFiles when trying to create directory");
        }
    }

    public static void printTickets(String project, List<Ticket> ticketList) throws IOException {
        project = project.toLowerCase();
        File file = new File("reportFiles/" + project);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }

        file = new File("reportFiles/" + project+ "/"+ "AllTickets.txt");
        try(FileWriter fileWriter = new FileWriter(file)) {

            List<Ticket> ticketOrderedByCreation = new ArrayList<>(ticketList);
            ticketOrderedByCreation.sort(Comparator.comparing(Ticket::getCreationDate));
            for (Ticket ticket : ticketOrderedByCreation) {
                List<String> iDs = new ArrayList<>();
                for(Release release : ticket.getAv()) {
                    iDs.add(release.releaseName());
                }
                fileWriter.append("key= ").append(ticket.getTicketKey())
                        .append(", injectedVersion= ") .append(ticket.getIv().releaseName())
                        .append(", openingVersion= ").append(ticket.getOv().releaseName())
                        .append(", fixedVersion= ") .append(ticket.getFv().releaseName())
                        .append(", affectedVersions= ").append(String.valueOf(iDs))
                        .append(", numOfCommits= ").append(String.valueOf(ticket.getCommitList().size()))
                        .append(", creationDate= ").append(String.valueOf(ticket.getCreationDate()))
                        .append(", resolutionDate= ").append(String.valueOf(ticket.getResolutionDate())).append(DELIMITER);
            }

            flushAndCloseFW(fileWriter, logger, CLASS);
        } catch (IOException e) {
            logger.info("Error in writeOnReportFiles when trying to create directory");
        }
    }

    public static void printReleases(String project, List<Release> releaseList, String name) throws IOException {
        project = project.toLowerCase();
        File file = new File("reportFiles/" + project);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }

        file = new File("reportFiles/" + project + "/"+ name);
        try(FileWriter fileWriter = new FileWriter(file)) {

            for (Release release : releaseList) {
                fileWriter.append("id= ").append(String.valueOf(release.id()))
                        .append(", releaseName= ").append(release.releaseName())
                        .append(", releaseDate= ").append(String.valueOf(release.releaseDate()))
                        .append(", numOfCommits= ").append(String.valueOf(release.getCommitList().size())).append(DELIMITER);
            }

            flushAndCloseFW(fileWriter, logger, CLASS);
        } catch (IOException e) {
            logger.info("Error in writeOnReportFiles when trying to create directory");
        }

    }

    private static void flushAndCloseFW(FileWriter fileWriter, Logger logger, String className) {
        try {
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            logger.info("Error in " + className + " while flushing/closing fileWriter !!!");
        }
    }
}
