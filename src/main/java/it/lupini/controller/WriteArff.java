package it.lupini.controller;

import it.lupini.model.JavaClass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class WriteArff {

    private WriteArff(){}

    public static final String SLASH = "/";

    public static void createArff(String project, List<JavaClass> classes, int iteration, String type) throws IOException {
        project = project.toLowerCase();
        String buggy;
        File file = new File("arffFiles/" + project + "/iteration_" + iteration);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }

        file = new File("arffFiles/" + project + "/iteration_" + iteration + SLASH + type + ".arff");
        try (FileWriter fileWriter = new FileWriter(file)) {

            fileWriter.append("@relation " + project + "_" + type + "_" + iteration).append("\n\n")
                    .append("@attribute LOC numeric").append("\n")
                    .append("@attribute num_comments numeric").append("\n")
                    .append("@attribute num_revisions numeric").append("\n")
                    .append("@attribute num_auth numeric").append("\n")
                    .append("@attribute num_fix numeric").append("\n")
                    .append("@attribute LOC_touched numeric").append("\n")
                    .append("@attribute LOC_added numeric").append("\n")
                    .append("@attribute MAX_LOC_added numeric").append("\n")
                    .append("@attribute AVG_LOC_added numeric").append("\n")
                    .append("@attribute churn numeric").append("\n")
                    .append("@attribute MAX_churn numeric").append("\n")
                    .append("@attribute AVG_churn numeric").append("\n")
                    .append("@attribute buggy {'YES', 'NO'}").append("\n\n")
                    .append("@data").append("\n");


            for (JavaClass c : classes) {

                if (c.getBuggyness()) {
                    buggy = "YES";
                } else {
                    buggy = "NO";
                }

                fileWriter.append(String.valueOf(c.getLoc())).append(",")
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
