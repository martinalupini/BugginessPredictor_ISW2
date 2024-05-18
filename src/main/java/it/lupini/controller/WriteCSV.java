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

        file = new File("csvFiles/" + project+ "/iteration_"+iteration+"/"+type+".csv");
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


    public static void writeFinalWekaResults(String project, List<ClassifierEvaluation> evaluations) throws IOException {
        project = project.toLowerCase();
        File file = new File("finalWekaResults/" + project);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }


        file = new File("finalWekaResults/" + project+ "/results.csv");
        try(FileWriter fileWriter = new FileWriter(file)) {

            fileWriter.append("Iteration,%TrainingInstances,Classifier,Feature Selection,Sampling,Cost Sensitive,Precision,Recall,AUC,Kappa,TP,FP,TN,FN").append("\n");
            for (ClassifierEvaluation e: evaluations){

                fileWriter.append(String.valueOf(e.getIteration())).append(",")
                        .append(String.valueOf(e.getTrainingPercent())).append(",")
                        .append(e.getClassifierName()).append(",")
                        .append(e.getFeatureSelection()).append(",")
                        .append(e.getSampling()).append(",")
                        .append(e.getCostSensitive()).append(",")
                        .append(String.valueOf(e.getPrecision())).append(",")
                        .append(String.valueOf(e.getRecall())).append(",")
                        .append(String.valueOf(e.getAUC())).append(",")
                        .append(String.valueOf(e.getKappa())).append(",")
                        .append(String.valueOf(e.getTP())).append(",")
                        .append(String.valueOf(e.getFP())).append(",")
                        .append(String.valueOf(e.getTN())).append(",")
                        .append(String.valueOf(e.getFN())).append("\n");
            }

            fileWriter.flush();
        } catch (IOException e) {
            //ignore
        }
    }

    public static void createAcumeFiles(String project, List<AcumeClass> classes, String name) throws IOException {
        project = project.toLowerCase();
        String buggy;
        File file = new File("acumeFiles/" + project);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }

        file = new File("acumeFiles/" + project+ "/"+name+".csv");
        try(FileWriter fileWriter = new FileWriter(file)) {

            fileWriter.append("Name,Size,Predicted %,Actual value").append("\n");
            for (AcumeClass c: classes){


                fileWriter.append(c.getName()).append(",")
                        .append(c.getSize()).append(",")
                        .append(c.getPredictedProbability()).append(",")
                        .append(c.getActualValue()).append("\n");
            }

            fileWriter.flush();
        } catch (IOException e) {
            //ignore
        }
    }
}
