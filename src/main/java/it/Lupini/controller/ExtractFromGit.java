package it.Lupini.controller;

import it.Lupini.model.Release;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExtractFromGit {
    public static List<RevCommit> getAllCommits(List<Release> releaseList, String project) throws GitAPIException, IOException {

        List<RevCommit> commitList = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Path repository;

        /////////////////ATTENZIONE POTREBBE NON FUNZIONARE//////////////////
        if(project.toLowerCase().equals("bookkeeper")) {
            repository = Paths.get("/wsl$/Ubuntu-20.04/home/martina/isw2/bookkeeper");
        }else{
            repository = Paths.get("/Users/Martina/OneDrive - Universita' degli Studi di Roma Tor Vergata/ISW2/avro");
        }

        //opening existing git repository
        Git git = Git.open(repository.toFile());

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
}
