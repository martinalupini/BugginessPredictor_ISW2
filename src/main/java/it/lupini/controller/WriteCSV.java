package it.lupini.controller;

import it.lupini.model.JavaClass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class WriteCSV {

    private WriteCSV(){}

    public static void createCSV(String project, List<JavaClass> classes) throws IOException {
        project = project.toLowerCase();
        String buggy;
        File file = new File("csvFiles/" + project);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }

        file = new File("csvFiles/" + project+ "/Dataset.csv");
        try(FileWriter fileWriter = new FileWriter(file)) {

            fileWriter.append("Name, Release, LOC, #Comments, #Revisions, #Auth, #Fix, LOC touched, LOC added, MAX LOC added, AVG LOC added, churn, MAX churn, AVG churn, Buggy").append("\n");
            for (JavaClass c: classes){

                if(c.getBuggyness()){
                    buggy = "YES";
                }else{
                    buggy = "NO";
                }

                fileWriter.append(c.getName()).append(",")
                        .append(String.valueOf(c.getRelease().id())).append(",")
                        .append(String.valueOf(c.getLoc())).append(",")
                        .append(String.valueOf(c.getCommentLines())).append(",")
                        .append(String.valueOf(c.getNRevisions())).append(",")
                        .append(String.valueOf(c.getNAuth())).append(",")
                        .append(String.valueOf(c.getNFix())).append(",")
                        .append(String.valueOf(c.getLOCtouched())).append(",")
                        .append(String.valueOf(c.getLOCadded())).append(",")
                        .append(String.valueOf(c.getMaxlocAdded())).append(",")
                        .append(String.valueOf(c.getAvglocAdded())).append(",")
                        .append(String.valueOf(c.getChurn())).append(",")
                        .append(String.valueOf(c.getMAXChurn())).append(",")
                        .append(String.valueOf(c.getAVGChurn())).append(",")
                        .append(buggy).append("\n");
            }

            fileWriter.flush();
        } catch (IOException e) {
            //ignore
        }
    }
}
