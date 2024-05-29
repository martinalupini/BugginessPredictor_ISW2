package it.lupini.controller;

import it.lupini.model.AcumeClass;
import it.lupini.model.ClassifierEvaluation;
import it.lupini.model.JavaClass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class WriteCSV {

    private WriteCSV(){}

    public static final String DELIMITER = "\n";
    public static final String COMMA = ",";
    public static final String SLASH = "/";

    public static void writeDataset(String project, List<JavaClass> classes, int iteration, String type) throws IOException {
        project = project.toLowerCase();
        String buggy;
        File file = new File("csvFiles/" + project+ "/iteration_" + iteration );
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }

        file = new File("csvFiles/" + project+ "/iteration_"+iteration+SLASH+type+".csv");
        try(FileWriter fileWriter = new FileWriter(file)) {

            fileWriter.append("Name, Release, LOC, #Comments, #Revisions, #Auth, #Fix, LOC touched, LOC added, MAX LOC added, AVG LOC added, churn, MAX churn, AVG churn, Buggy").append(DELIMITER);
            for (JavaClass c: classes){

                if(c.getBuggyness()){
                    buggy = "YES";
                }else{
                    buggy = "NO";
                }

                fileWriter.append(c.getName()).append(COMMA)
                        .append(String.valueOf(c.getRelease().id())).append(COMMA)
                        .append(String.valueOf(c.getLoc())).append(COMMA)
                        .append(String.valueOf(c.getCommentLines())).append(COMMA)
                        .append(String.valueOf(c.getNRevisions())).append(COMMA)
                        .append(String.valueOf(c.getNAuth())).append(COMMA)
                        .append(String.valueOf(c.getNFix())).append(COMMA)
                        .append(String.valueOf(c.getLOCtouched())).append(COMMA)
                        .append(String.valueOf(c.getLOCadded())).append(COMMA)
                        .append(String.valueOf(c.getMaxlocAdded())).append(COMMA)
                        .append(String.valueOf(c.getAvglocAdded())).append(COMMA)
                        .append(String.valueOf(c.getChurn())).append(COMMA)
                        .append(String.valueOf(c.getMAXChurn())).append(COMMA)
                        .append(String.valueOf(c.getAVGChurn())).append(COMMA)
                        .append(buggy).append(DELIMITER);
            }

            fileWriter.flush();
        } catch (IOException e) {
            //ignore
        }
    }


    public static void writeFinalWekaResults(String project, List<ClassifierEvaluation> evaluations) throws IOException {
        project = project.toLowerCase();
        File file = new File("finalWekaResults/");
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }


        file = new File("finalWekaResults/" + project+ "_results.csv");
        try(FileWriter fileWriter = new FileWriter(file)) {

            fileWriter.append("Iteration,%TrainingInstances,Classifier,Feature Selection,Sampling,Cost Sensitive,Precision,Recall,AUC,Kappa,TP,FP,TN,FN,Cost").append(DELIMITER);
            for (ClassifierEvaluation e: evaluations){

                fileWriter.append(String.valueOf(e.getIteration())).append(COMMA)
                        .append(String.valueOf(e.getTrainingPercent())).append(COMMA)
                        .append(e.getClassifierName()).append(COMMA)
                        .append(e.getFeatureSelection()).append(COMMA)
                        .append(e.getSampling()).append(COMMA)
                        .append(e.getCostSensitive()).append(COMMA)
                        .append(String.valueOf(e.getPrecision())).append(COMMA)
                        .append(String.valueOf(e.getRecall())).append(COMMA)
                        .append(String.valueOf(e.getAuc())).append(COMMA)
                        .append(String.valueOf(e.getKappa())).append(COMMA)
                        .append(String.valueOf(e.getTp())).append(COMMA)
                        .append(String.valueOf(e.getFp())).append(COMMA)
                        .append(String.valueOf(e.getTn())).append(COMMA)
                        .append(String.valueOf(e.getFn())).append(COMMA)
                        .append(String.valueOf(e.getCost())).append(DELIMITER);
            }

            fileWriter.flush();
        } catch (IOException e) {
            //ignore
        }
    }

    public static void createAcumeFiles(String project, List<AcumeClass> classes, String name) throws IOException {
        project = project.toLowerCase();

        File file = new File("acumeFiles/" + project);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }

        file = new File("acumeFiles/" + project+ SLASH+name+".csv");
        try(FileWriter fileWriter = new FileWriter(file)) {

            fileWriter.append("ID,Size,Predicted %,Actual value").append(DELIMITER);
            for (AcumeClass c: classes){


                fileWriter.append(String.valueOf(c.getId())).append(COMMA)
                        .append(c.getSize()).append(COMMA)
                        .append(c.getPredictedProbability()).append(COMMA)
                        .append(c.getActualValue()).append(DELIMITER);
            }

            fileWriter.flush();
        } catch (IOException e) {
            //ignore
        }
    }
}
