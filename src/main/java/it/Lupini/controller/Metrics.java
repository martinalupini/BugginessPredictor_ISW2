package it.Lupini.controller;

import it.Lupini.model.JavaFile;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Metrics {

    private final List<JavaFile> classes;

    private final ExtractFromGit gitExtractor;

    public Metrics(List<JavaFile> classes, ExtractFromGit gitExtractor){
        this.classes = classes;
        this.gitExtractor = gitExtractor;

    }

    public List<JavaFile> computeMetrics() throws IOException {
        countLoc();
        countComments();
        computeNR();
        computeNFix();
        computeNAuth();
        computeLOCMetrics();

        return this.classes;

    }

    private void countLoc() {
        for(JavaFile projectClass : classes) {
            String[] lines = projectClass.getContent().split("\r\n|\r|\n");
            projectClass.setLoc(lines.length);
        }
    }

    public void countComments() {

        for(JavaFile projectClass : classes) {
            int i = 0;
            int commentCount = 0;
            String[] lines = projectClass.getContent().split("\r\n|\r|\n");

            while(i<lines.length) {
                //single-lined comments
                if (lines[i].trim().startsWith("//")) {
                    commentCount++;
                    i++;
                    continue;
                    //multi-lined comments
                } else if (lines[i].trim().startsWith("/*")) {
                    commentCount++;
                    i++;
                    while (i<lines.length && !lines[i].trim().startsWith("*/")) {
                        commentCount++;
                        i++;
                    }

                    continue;
                }
                i++;
            }

            projectClass.setCommentLines(commentCount);
        }
    }


    private void computeNR() {
        for(JavaFile projectClass : classes) {
            projectClass.setNRevisions(projectClass.getCommits().size());
        }
    }


    private void computeNFix(){
        for(JavaFile projectClass : classes) {
            projectClass.setNFix(projectClass.getFixCommits().size());
        }
    }


    private void computeNAuth() {
        for(JavaFile projectClass : classes) {
            List<String> authorsOfClass = new ArrayList<>();
            for(RevCommit commit : projectClass.getCommits()) {
                if(!authorsOfClass.contains(commit.getAuthorIdent().getName())) {
                    authorsOfClass.add(commit.getAuthorIdent().getName());
                }
            }
            projectClass.setNAuth(authorsOfClass.size());
        }
    }


    private void computeLOCMetrics() throws IOException {
        int i;
        for(JavaFile projectClass : classes) {

            gitExtractor.extractAddedOrRemovedLOC(projectClass);

            List<Integer> locAddedByClass = projectClass.getLocAddedList();
            List<Integer> locRemovedByClass = projectClass.getLocRemovedList();
            List<Integer> churnOfClass = new ArrayList<>();

            for(i = 0; i < locAddedByClass.size(); i++) {
                int addedLineOfCode = locAddedByClass.get(i);
                int removedLineOfCode = locRemovedByClass.get(i);
                int churningFactor = Math.abs(addedLineOfCode - removedLineOfCode);
                int touchedLinesOfCode = addedLineOfCode + removedLineOfCode;

                projectClass.sumLocAdded(addedLineOfCode);
                projectClass.sumChurn(churningFactor);
                churnOfClass.add(churningFactor);
                projectClass.addLOCTouched(touchedLinesOfCode);

            }

            projectClass.setMaxLocAdded(getMaxVal(locAddedByClass));
            projectClass.setMaxChurn(getMaxVal(churnOfClass));
            projectClass.setAvgLOCAdded(getAvgVal(locAddedByClass));
            projectClass.setAvgChurn(getAvgVal(churnOfClass));

        }
    }

    private int getMaxVal(List<Integer> list) {
        int i;
        if(list.isEmpty() ) return 0;
        int max = list.get(0);
        for (i = 1; i < list.size(); i++) {
            if (max < list.get(i)) max = list.get(i);
        }

        return max;

    }

    private int getAvgVal(List<Integer> list){
        int sum = 0;

        if(list.isEmpty() ) return 0;

        for(Integer v : list){
            sum += v;
        }
        return sum/ list.size();

    }


}
