package it.lupini.utils;

import it.lupini.model.JavaClass;
import it.lupini.model.Release;
import it.lupini.model.Ticket;
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

    private ReportUtils(){}

    public static final String DELIMITER = "\n";
    public static final String CLASS = ReportUtils.class.getName();
    private static final Logger logger = Logger.getLogger(CLASS);
    private static final String MAINDIR = "reportFiles/";
    public static final String SLASH = "/";
    public static final String ERROR = "Error in writeOnReportFiles when trying to create directory";

    public static void printSummary(String project, List<Ticket> ticketList, List<RevCommit> commitList, List<RevCommit> filteredCommitsOfIssues) throws IOException {
        project = project.toLowerCase();
        File file = new File(MAINDIR + project);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }

        file = new File(MAINDIR + project + SLASH+ "Summary.txt");
        try(FileWriter fileWriter = new FileWriter(file)) {

            fileWriter.append(String.valueOf(ticketList.size())).append(" TICKETS \n")
                    .append(String.valueOf(commitList.size())).append(" TOTAL COMMITS \n")
                    .append(String.valueOf(filteredCommitsOfIssues.size())).append(" COMMITS CONTAINING BUG-ISSUES");

            flushAndCloseFW(fileWriter, logger, CLASS);
        } catch (IOException e) {
            logger.info(ERROR);
        }
    }

    public static void printCommits( String project, List<RevCommit> commitList, String name) throws IOException {
        project = project.toLowerCase();
        File file = new File(MAINDIR + project);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }

        file = new File(MAINDIR + project+ SLASH + name);
        try(FileWriter fileWriter = new FileWriter(file)) {

            fileWriter.append("id,committer,creationDate\n");
            for (RevCommit commit: commitList){
                fileWriter.append(commit.getName()).append(",")
                        .append(commit.getCommitterIdent().getName()).append(",")
                        //.append(", message= ").append(commit.getFullMessage())
                        .append(String.valueOf(LocalDate.parse((new SimpleDateFormat("yyyy-MM-dd").format(commit.getCommitterIdent().getWhen()))))).append(DELIMITER);
            }

            flushAndCloseFW(fileWriter, logger, CLASS);
        } catch (IOException e) {
            logger.info(ERROR);
        }
    }

    public static void printTickets(String project, List<Ticket> ticketList) throws IOException {
        project = project.toLowerCase();
        File file = new File(MAINDIR + project);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }

        file = new File(MAINDIR + project+ SLASH+ "AllTickets.csv");
        try(FileWriter fileWriter = new FileWriter(file)) {

            fileWriter.append("key,creationDate,resolutionDate,injectedVersion,openingVersion,fixedVersion,affectedVersion,numOfCommits\n");

            List<Ticket> ticketOrderedByCreation = new ArrayList<>(ticketList);
            ticketOrderedByCreation.sort(Comparator.comparing(Ticket::getCreationDate));
            for (Ticket ticket : ticketOrderedByCreation) {
                List<String> iDs = new ArrayList<>();
                for(Release release : ticket.getAv()) {
                    iDs.add(release.releaseName());
                }
                fileWriter.append(ticket.getTicketKey()).append(",")
                        .append(String.valueOf(ticket.getCreationDate())).append(",")
                        .append(String.valueOf(ticket.getResolutionDate())).append(",")
                        .append(ticket.getIv().releaseName()).append(",")
                        .append(ticket.getOv().releaseName()).append(",")
                        .append(ticket.getFv().releaseName()).append(",")
                        .append(String.valueOf(iDs)).append(",")
                        .append(String.valueOf(ticket.getCommitList().size())).append(DELIMITER);
            }

            flushAndCloseFW(fileWriter, logger, CLASS);
        } catch (IOException e) {
            logger.info(ERROR);
        }
    }

    public static void printReleases(String project, List<Release> releaseList, String name) throws IOException {
        project = project.toLowerCase();
        File file = new File(MAINDIR + project);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }

        file = new File(MAINDIR + project + SLASH+ name);
        try(FileWriter fileWriter = new FileWriter(file)) {

            fileWriter.append("id,releaseName,releaseDate,numOfCommits\n");

            for (Release release : releaseList) {
                fileWriter.append(String.valueOf(release.id())).append(",")
                        .append(release.releaseName()).append(",")
                        .append(String.valueOf(release.releaseDate())).append(",")
                        .append(String.valueOf(release.getCommitList().size())).append(DELIMITER);
            }

            flushAndCloseFW(fileWriter, logger, CLASS);
        } catch (IOException e) {
            logger.info(ERROR);
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


    public static void printClasses(String project, List<JavaClass> classes) throws IOException {
        project = project.toLowerCase();
        File file = new File(MAINDIR + project);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }

        file = new File(MAINDIR + project+ "/Classes.csv");
        try(FileWriter fileWriter = new FileWriter(file)) {

            fileWriter.append("name,firstCommit,#Commits\n");

            for (JavaClass c: classes){
                String commit;
                if(c.getCommits().isEmpty())  commit = "";
                else commit = c.getCommits().get(0).toString();
                fileWriter.append(c.getName()).append(",")
                        .append(commit).append(",")
                        .append(String.valueOf(c.getCommits().size())).append(DELIMITER);
            }

            flushAndCloseFW(fileWriter, logger, CLASS);
        } catch (IOException e) {
            logger.info(ERROR);
        }
    }

}
