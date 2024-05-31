package it.lupini.model;

import weka.classifiers.Classifier;

public class WekaClassifier {

    private Classifier classifier;
    private String name;
    private String sampling;
    private String featureSelection;
    private String costSensitive;

    public WekaClassifier(Classifier classifier, String name, String sampling, String featureSelection, String costSensitive) {
        this.classifier = classifier;
        this.name = name;
        this.sampling = sampling;
        this.featureSelection = featureSelection;
        this.costSensitive = costSensitive;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public String getName() {
        return name;
    }

    public String getSampling() {
        return sampling;
    }

    public String getFeatureSelection() {
        return featureSelection;
    }

    public String getCostSensitive() {
        return costSensitive;
    }
}

