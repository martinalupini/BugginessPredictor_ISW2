package it.Lupini.controller;

import it.Lupini.model.JavaFile;
import it.Lupini.model.Release;
import it.Lupini.model.Ticket;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

public class ExtractFromGit {
    private List<Ticket> ticketList;
    private List<Release> releaseList;
    private Path repoPath;
    private String repo;
    private Git git;

    public ExtractFromGit(List<Release> releaseList){
        this.releaseList = releaseList;
        this.ticketList = null;
    }

    public List<Ticket> getTicketList() {
        return ticketList;
    }
    public void setTicketList(List<Ticket> ticketList) {
        this.ticketList = ticketList;
    }

    public void setReleaseList(List<Release> releaseList) {
        this.releaseList = releaseList;
    }
    public  List<RevCommit> getAllCommits(List<Release> releaseList, String project) throws GitAPIException, IOException {

        List<RevCommit> commitList = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        /////////////////ATTENZIONE POTREBBE NON FUNZIONARE//////////////////
        if(project.toLowerCase().equals("bookkeeper")) {
            repo = "/wsl$/Ubuntu-20.04/home/martina/isw2/bookkeeper";
        }else{
            repo= "/Users/Martina/OneDrive - Universita' degli Studi di Roma Tor Vergata/ISW2/avro";
        }

        repoPath = Paths.get(repo);
        //opening existing git repository
        git = Git.open(repoPath.toFile());

        //retrieving all comments
        Iterable<RevCommit> commits = git.log().all().call();

            for (RevCommit commit : commits)
            {
                commitList.add(commit);

                //extracting date of commit
                LocalDate commitDate = LocalDate.parse(formatter.format(commit.getCommitterIdent().getWhen()));
                LocalDate lowerBoundDate = LocalDate.parse(formatter.format(new Date(0)));

                for(Release release: releaseList){

                    LocalDate releaseDate = release.releaseDate();

                    //checking if the commit date is after the release date and after the previous release. If so, the
                    //commit does not belong to that realease
                    if (commitDate.isAfter(lowerBoundDate) && !commitDate.isAfter(releaseDate)) {
                        release.addCommit(commit);
                    }
                    lowerBoundDate = releaseDate;
                }

            }

        return commitList;
    }


    public List<RevCommit> filterCommitsOfIssues(List<RevCommit> commitList, List<Ticket> ticketList) {
        this.ticketList = ticketList;
        List<RevCommit> filteredCommits = new ArrayList<>();
        for (RevCommit commit : commitList) {
            for (Ticket ticket : ticketList) {
                String commitFullMessage = commit.getFullMessage();
                String ticketKey = ticket.getTicketKey();

                //if the commit contains the ticket key then is a commit related to an issue
                if (commitFullMessage.contains(ticketKey)) {
                    filteredCommits.add(commit);
                    ticket.addCommit(commit);
                    //removing ticket not related to issues from the ticket list
                    ticketList.removeIf(t-> t.getTicketKey() == ticket.getTicketKey());
                }
            }
        }
        return filteredCommits;
    }

    public void getClasses(List<Release> releasesList) throws IOException {

        for (Release release : releasesList) {
            List<String> classesList = new ArrayList<>();
            for (RevCommit commit : release.getCommitList()) {
                //ID associated to the tree of the current commit
                ObjectId treeId = commit.getTree();
                // iterating through the tree in the current commit
                TreeWalk treeWalk = new TreeWalk(git.getRepository());
                treeWalk.reset(treeId);
                //to enter the subtrees
                treeWalk.setRecursive(true);
                while (treeWalk.next()) {
                    addJavaFile(treeWalk, release, classesList);
                }

            }
        }

        //in case a release does not add new classes but works on the priors
        for (int k = 0; k<releasesList.size(); k++) {
            if(releasesList.get(k).getClasses().isEmpty()) {
                releasesList.get(k).setClasses(releasesList.get(k-1).getClasses());
            }
        }
    }


    private void addJavaFile(TreeWalk treeWalk, Release release, List<String> fileNameList) throws IOException {

        Repository repository;
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        repository = repositoryBuilder.setGitDir(new File(this.repo+".git")).readEnvironment()
                .findGitDir()
                .setMustExist(true).build();


        //goal: aggiungo il file java nella lista di file appartenenti alla release.

        //getting the name of the file
        String filename = treeWalk.getPathString();
        //checking if it's a java file and if it's not on the list
        if (filename.endsWith(".java") && !fileNameList.contains(filename)) {
            JavaFile file = new JavaFile(filename);
            fileNameList.add(filename);
            //file.setLoc(Metrics.linesOfCode(treeWalk, repository)); ?????????????????
            release.addClass(file);
        }
    }

}
