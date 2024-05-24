package it.lupini.controller;

import it.lupini.model.JavaClass;
import it.lupini.model.Release;
import it.lupini.model.Ticket;
import it.lupini.utils.ReleaseUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

public class ExtractFromGit {
    private List<Ticket> ticketList;
    private List<Release> releaseList;
    private List<Release> fullReleaseList;
    private List<RevCommit> commitList;
    private Git git;
    private Repository repository;


    public ExtractFromGit(String projName,  List<Release> releaseList) throws IOException {
        InitCommand init = Git.init();
        File file = new File("C:/Users/Martina/ISW/"+projName.toLowerCase()+"/.git");
        init.setDirectory(file);
        this.git = Git.open(file);

        repository = git.getRepository();
        this.releaseList = releaseList;
        this.ticketList = null;
        this.commitList = new ArrayList<>();
    }



    public List<Ticket> getTicketList() {
        return ticketList;
    }

    public void setReleaseList(List<Release> releaseList) {
        this.fullReleaseList = this.releaseList;
        this.releaseList = releaseList;
    }
    public  List<RevCommit> getAllCommits(List<Release> releaseList) throws GitAPIException, IOException {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");


        Iterable<RevCommit> commits = git.log().all().call();

        commits.forEach(commitList::add);

            for (RevCommit commit : commitList) {

                //extracting date of commit
                LocalDate commitDate = LocalDate.parse(formatter.format(commit.getCommitterIdent().getWhen()));
                LocalDate lowerBoundDate = LocalDate.parse(formatter.format(new Date(0)));

                for(Release release: releaseList){

                    LocalDate releaseDate = release.releaseDate();

                    //checking if the commit date is after the release date and after the previous release. If so, the
                    //commit does not belong to that release
                    if (commitDate.isAfter(lowerBoundDate) && !commitDate.isAfter(releaseDate)) {
                        release.addCommit(commit);
                    }
                    lowerBoundDate = releaseDate;
                }

            }


        releaseList.removeIf(release -> release.getCommitList().isEmpty());
        int i = 0;
        for (Release release : releaseList) {
            release.setId(++i);
        }


        return commitList;
    }


    public List<RevCommit> filterCommitsOfIssues(List<RevCommit> commitList, List<Ticket> ticketList) {
        this.ticketList = ticketList;
        List<RevCommit> filteredCommits = new ArrayList<>();
        for (RevCommit commit : commitList) {
            for (Ticket ticket : ticketList) {
                String commitMessage = commit.getFullMessage();
                String ticketKey = ticket.getTicketKey();

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                LocalDate commitDate = LocalDate.parse(formatter.format(commit.getCommitterIdent().getWhen()));

                //if the commit contains the ticket key and te date is between the opening
                // and the resolution then is a commit related to an issue
                if (commitMessage.contains(ticketKey) && !commitDate.isAfter(ticket.getResolutionDate())
                        && !commitDate.isBefore(ticket.getCreationDate())) {
                    filteredCommits.add(commit);
                    ticket.addCommit(commit);
                }
            }
        }

        //removing ticket not related to any issues from the ticket list
        ticketList.removeIf(ticket -> ticket.getCommitList().isEmpty());
        return filteredCommits;
    }

    public List<JavaClass> getClasses(List<Release> releasesList) throws IOException {

        List<JavaClass> classes = new ArrayList<>();

        for (Release release : releasesList) {
            List<String> classesList = new ArrayList<>();
            List<RevCommit> classCommits = release.getCommitList();
            //ordering from last to first
            classCommits.sort(Comparator.comparing(o -> o.getCommitterIdent().getWhen(), Comparator.reverseOrder()));

            for (RevCommit commit : classCommits) {
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

                        ObjectLoader loader = repository.open(treeWalk.getObjectId(0));
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        loader.copyTo(output);
                        String fileContent = output.toString();

                        JavaClass file = new JavaClass(filename, release, fileContent );
                        classesList.add(filename);
                        release.addClass(file);
                        classes.add(file);
                    }
                }

            }
        }

        //in case a release does not add new classes but works on the priors
        for (int k = 1; k<releasesList.size(); k++) {
            if(releasesList.get(k).getClasses().isEmpty()) {
                releasesList.get(k).setClasses(releasesList.get(k-1).getClasses());
            }
        }

        // checking on all commits (of all the releases)
        addCommitsToClass(classes, commitList);

        return classes;

    }

    public void addCommitsToClass(List<JavaClass> classes, List<RevCommit> commits) throws IOException {

        for(RevCommit commit: commits){
            Release releaseOfCommit = ReleaseUtils.getReleaseOfCommit(commit, fullReleaseList);
            List<String> modifiedClassesNames = getTouchedClassesNames(commit);
            for(String modifiedClass: modifiedClassesNames){
                for(JavaClass projectClass: classes){
                    if(projectClass.getRelease().equals(releaseOfCommit) && projectClass.getName().equals(modifiedClass) && !projectClass.getCommits().contains(commit)) {
                        projectClass.addCommit(commit);
                    }
                }
            }
        }
    }


    public void setBuggyness(List<Ticket> ticketList, List<JavaClass> allProjectClasses) throws IOException {

        //at first the buggyness is set to false
        for(JavaClass projectClass: allProjectClasses){
            projectClass.setBuggyness(false);
        }

        //need to iterate through all tickets to check if a commit related to that ticket touched that class
        for(Ticket ticket: ticketList) {
            List<RevCommit> ticketCommitList = ticket.getCommitList();
            Release injectedVersion = ticket.getIv();

            for (RevCommit commit : ticketCommitList) {
                //list of the touched classes of the commit
                List<String> modifiedClassesNames = getTouchedClassesNames(commit);
                //the release associated with the commit is the fixed version
                Release releaseOfCommit = ReleaseUtils.getReleaseOfCommit(commit, fullReleaseList);

                //here I iterate through all the modified classes of the commit and label the classes
                //in the full list if there is a match (need to check name and release since in the list the same
                //class appears several times for different releases)
                for (String modifiedClass : modifiedClassesNames) {
                    labelBuggyClasses(modifiedClass, injectedVersion, releaseOfCommit, allProjectClasses, commit);
                }

            }
        }
    }


    private static void labelBuggyClasses(String modifiedClass, Release injectedVersion, Release fixedVersion, List<JavaClass> allProjectClasses, RevCommit commit) {
        for(JavaClass projectClass: allProjectClasses){
            if(projectClass.getCommits().contains(commit) && !projectClass.getFixCommits().contains(commit)){
                projectClass.addFixCommit(commit);
            }
            if(projectClass.getName().equals(modifiedClass) && projectClass.getRelease().id() < fixedVersion.id() && projectClass.getRelease().id() >= injectedVersion.id()){
                projectClass.setBuggyness(true);
            }
        }
    }

    private List<String> getTouchedClassesNames(RevCommit commit) throws IOException {

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


    public void extractAddedOrRemovedLOC(JavaClass projectClass) throws IOException {
        for(RevCommit commit : projectClass.getCommits()) {
            try(DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                //getting the parent of the current commit
                RevCommit parentComm = commit.getParent(0);
                diffFormatter.setRepository(repository);
                diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
                //getting the differences between the current commit and its parent
                List<DiffEntry> diffEntries = diffFormatter.scan(parentComm.getTree(), commit.getTree());
                for(DiffEntry diffEntry : diffEntries) {
                    //checking if the file path of the current difference matches the class name
                    if(diffEntry.getNewPath().equals(projectClass.getName())) {
                        projectClass.addLocAdded(getAddedLines(diffFormatter, diffEntry));
                        projectClass.addLocRemoved(getDeletedLines(diffFormatter, diffEntry));
                    }
                }
            } catch(ArrayIndexOutOfBoundsException ignored) {
                //ignoring when no parent is found
            }
        }
    }

    private int getAddedLines(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {
        int addedLines = 0;
        for(Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {
            addedLines += edit.getEndB() - edit.getBeginB();
        }
        return addedLines;
    }

    private int getDeletedLines(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {
        int deletedLines = 0;
        for(Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {
            deletedLines += edit.getEndA() - edit.getBeginA();
        }
        return deletedLines;
    }

}
