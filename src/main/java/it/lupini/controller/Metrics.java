package it.lupini.controller;

import it.lupini.model.JavaClass;
import it.lupini.utils.MathUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Metrics {

    private final List<JavaClass> classes;

    private final ExtractFromGit gitExtractor;

    public Metrics(List<JavaClass> classes, ExtractFromGit gitExtractor){
        this.classes = classes;
        this.gitExtractor = gitExtractor;

    }

    public List<JavaClass> computeMetrics() throws IOException {
        countLoc();
        countComments();
        computeNR();
        computeNFix();
        computeNAuth();
        computeLOCMetrics();

        return this.classes;

    }

    private void countLoc() {
        for(JavaClass projectClass : classes) {
            String[] lines = projectClass.getContent().split("\r\n|\r|\n");
            projectClass.setLoc(lines.length);
        }
    }


    public void countComments() {

        for(JavaClass projectClass : classes) {
            int commentCount = 0;
            String[] lines = projectClass.getContent().split("\r\n|\r|\n");
            String trim;

            for (String line : lines) {

                trim = line.trim();

                if (trim.startsWith("//") || trim.startsWith("*") || trim.startsWith("/*") || trim.startsWith("*/")) {
                    commentCount++;
                }

            }

            projectClass.setCommentLines(commentCount);
        }
    }




    private void computeNR() {
        for(JavaClass projectClass : classes) {
            projectClass.setNRevisions(projectClass.getCommits().size());
        }
    }


    private void computeNFix(){
        for(JavaClass projectClass : classes) {
            projectClass.setNFix(projectClass.getFixCommits().size());
        }
    }


    private void computeNAuth() {
        for(JavaClass projectClass : classes) {
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
        for(JavaClass projectClass : classes) {

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

            projectClass.setMaxLocAdded(MathUtils.getMaxVal(locAddedByClass));
            projectClass.setMaxChurn(MathUtils.getMaxVal(churnOfClass));
            projectClass.setAvgLOCAdded(MathUtils.getAvgVal(locAddedByClass));
            projectClass.setAvgChurn(MathUtils.getAvgVal(churnOfClass));

        }
    }


}
