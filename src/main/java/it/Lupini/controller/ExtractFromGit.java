package it.Lupini.controller;

import it.Lupini.model.JavaFile;
import it.Lupini.model.Release;
import it.Lupini.model.Ticket;
import it.Lupini.utils.ReleaseUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

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

    private List<RevCommit> commitList;
    private Path repoPath;
    private String repo;
    private Git git;

    public ExtractFromGit(List<Release> releaseList){
        this.releaseList = releaseList;
        this.ticketList = null;
        this.commitList = new ArrayList<>();
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

            for (RevCommit commit : commits) {
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

    public List<JavaFile> getClasses(List<Release> releasesList) throws IOException {

        List<JavaFile> classes = new ArrayList<>();

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
                    //getting the name of the file
                    String filename = treeWalk.getPathString();
                    //checking if it's a java file (not a test) and if it's not on the list
                    if (filename.endsWith(".java") && !classesList.contains(filename) && !filename.contains("/test/")) {
                        JavaFile file = new JavaFile(filename);
                        classesList.add(filename);
                        release.addClass(file);
                        classes.add(file);
                    }
                }

            }
        }

        //in case a release does not add new classes but works on the priors
        for (int k = 0; k<releasesList.size(); k++) {
            if(releasesList.get(k).getClasses().isEmpty()) {
                releasesList.get(k).setClasses(releasesList.get(k-1).getClasses());
            }
        }

        setBuggyness(ticketList, classes);
        keepTrackOfCommitsThatTouchTheClass(classes, commitList);

        return classes;

    }


    public void setBuggyness(List<Ticket> ticketList, List<JavaFile> allProjectClasses) throws IOException {
        for(JavaFile projectClass: allProjectClasses){
            projectClass.setBuggyness(false);
        }
        for(Ticket ticket: ticketList) {
            List<RevCommit> commitsContainingTicket = ticket.getCommitList();
            Release injectedVersion = ticket.getIv();
            for (RevCommit commit : commitsContainingTicket) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                LocalDate commitDate = LocalDate.parse(formatter.format(commit.getCommitterIdent().getWhen()));
                if (!commitDate.isAfter(ticket.getResolutionDate())
                        && !commitDate.isBefore(ticket.getCreationDate())) {
                    List<String> modifiedClassesNames = getTouchedClassesNames(commit);
                    Release releaseOfCommit = ReleaseUtils.getRelease(commitDate, releaseList);
                    for (String modifiedClass : modifiedClassesNames) {
                        labelBuggyClasses(modifiedClass, injectedVersion, releaseOfCommit, allProjectClasses);
                    }
                }
            }
        }
    }

    private void keepTrackOfCommitsThatTouchTheClass(List<JavaFile> allProjectClasses, List<RevCommit> commitList) throws IOException {
        for(RevCommit commit: commitList){
            List<String> modifiedClassesNames = getTouchedClassesNames(commit);
            for(String modifiedClass: modifiedClassesNames){
                for(JavaFile projectClass: allProjectClasses){
                    if(projectClass.getName().equals(modifiedClass) && !projectClass.getCommits().contains(commit)) {
                        projectClass.addCommit(commit);
                    }
                }
            }
        }
    }

    private static void labelBuggyClasses(String modifiedClass, Release injectedVersion, Release fixedVersion, List<JavaFile> allProjectClasses) {
        for(JavaFile projectClass: allProjectClasses){
            if(projectClass.getName().equals(modifiedClass) && projectClass.getRelease().id() < fixedVersion.id() && projectClass.getRelease().id() >= injectedVersion.id()){
                projectClass.setBuggyness(true);
            }
        }
    }

    private List<String> getTouchedClassesNames(RevCommit commit) throws IOException {

        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        Repository repository = repositoryBuilder.setGitDir(new File(repo)).readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .setMustExist(true).build();

        List<String> touchedClassesNames = new ArrayList<>();
        try(DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            ObjectId newTree = commit.getTree();
            newTreeIter.reset(reader, newTree);
            RevCommit commitParent = commit.getParent(0);
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            ObjectId oldTree = commitParent.getTree();
            oldTreeIter.reset(reader, oldTree);
            diffFormatter.setRepository(repository);
            List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);
            for(DiffEntry entry : entries) {
                if(entry.getNewPath().contains(".java") && !entry.getNewPath().contains("/test/")) {
                    touchedClassesNames.add(entry.getNewPath());
                }
            }
        } catch(ArrayIndexOutOfBoundsException ignored) {
            //ignoring when no parent is found
        }
        return touchedClassesNames;
    }

}
