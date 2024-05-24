package it.lupini.model;

import weka.classifiers.Evaluation;

public class ClassifierEvaluation {

    private String project;
    private int iteration;
    private double trainingPercent;
    private String classifier;
    private String featureSelection;
    private String sampling;
    private String costSensitive;
    private double precision;
    private double recall;
    private double auc;
    private double kappa;
    private double tp;
    private double fp;
    private double tn;
    private double fn;

    public ClassifierEvaluation(String project, int iteration, Evaluation evaluation, WekaClassifier classifier, double trainingPercent) {
        this.project = project;
        this.iteration = iteration;
        this.classifier = classifier.getName();
        this.featureSelection = classifier.getFeatureSelection();
        this.sampling = classifier.getSampling();
        this.costSensitive = classifier.getCostSensitive();
        this.trainingPercent = trainingPercent;
        this.precision = evaluation.precision(0);
        this.recall = evaluation.recall(0);
        this.auc = evaluation.areaUnderROC(0);
        this.kappa = evaluation.kappa();
        this.tp = evaluation.numTruePositives(0);
        this.fp = evaluation.numFalsePositives(0);
        this.tn = evaluation.numTrueNegatives(0);
        this.fn = evaluation.numFalseNegatives(0);

    }

    public String getProject() {
        return project;
    }

    public int getIteration() {
        return iteration;
    }

    public double getTrainingPercent() {
        return trainingPercent;
    }

    public String getClassifierName() {
        return classifier;
    }

    public String getFeatureSelection() {
        return featureSelection;
    }

    public String getSampling() {
        return sampling;
    }

    public String getCostSensitive() {
        return costSensitive;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getAuc() {
        return auc;
    }

    public double getKappa() {
        return kappa;
    }

    public double getTp() {
        return tp;
    }

    public double getFp() {
        return fp;
    }

    public double getTn() {
        return tn;
    }

    public double getFn() {
        return fn;
    }
}
