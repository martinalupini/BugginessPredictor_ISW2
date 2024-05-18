package it.lupini.controller;

import it.lupini.model.WekaClassifier;
import weka.attributeSelection.BestFirst;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SMOTE;

import java.util.ArrayList;
import java.util.List;

public class GetClassifiers {

    private List<WekaClassifier> classifiers;

    private String featureType;


    public GetClassifiers() {
        this.classifiers = new ArrayList<>();
    }

    public List<WekaClassifier> getClassifiers(Instances trainingSet) throws Exception {

        noFilters();
        featureSelection(trainingSet);
        sampling(trainingSet);
        costSensitive();
        featureAndSampling(trainingSet);
        featureAndCost(trainingSet);

        return classifiers;

    }


    private void featureSelection(Instances trainingSet) throws Exception {
        List<Classifier> baseClassifiers = getBaseClassifiers();

        for (Classifier classifier : baseClassifiers) {

            FilteredClassifier fc = getFilteredPlusFeature(classifier, trainingSet);

            WekaClassifier wekaClassifier = new WekaClassifier(fc, classifier.getClass().getSimpleName(), "none", this.featureType, "none");
            classifiers.add(wekaClassifier);
        }

    }


    private void sampling(Instances trainingSet) throws Exception {

        List<Classifier> baseClassifiers = getBaseClassifiers();

        for (Classifier classifier : baseClassifiers) {

            FilteredClassifier fc = new FilteredClassifier();
            fc.setClassifier(classifier);

            SMOTE smote = new SMOTE();
            smote.setInputFormat(trainingSet);
            fc.setFilter(smote);


            WekaClassifier wekaClassifier = new WekaClassifier(fc, classifier.getClass().getSimpleName(), smote.getClass().getSimpleName(), "none", "none");
            classifiers.add(wekaClassifier);
        }


    }


    private void costSensitive(){
        List<Classifier> baseClassifiers = getBaseClassifiers();

        for (Classifier classifier : baseClassifiers) {
            CostSensitiveClassifier costSensitiveClassifier = getCostSensitiveClassifier();
            costSensitiveClassifier.setClassifier(classifier);

            WekaClassifier wekaClassifier = new WekaClassifier(costSensitiveClassifier, classifier.getClass().getSimpleName(), "none", "none", "SensitiveThreshold");
            classifiers.add(wekaClassifier);
        }
    }

    private void featureAndSampling(Instances trainingSet) throws Exception {
        List<Classifier> baseClassifiers = getBaseClassifiers();

        for (Classifier classifier : baseClassifiers) {

            //feature selection
            FilteredClassifier fc1 = getFilteredPlusFeature(classifier, trainingSet);

            //sampling
            FilteredClassifier fc = new FilteredClassifier();
            fc.setClassifier(fc1);

            SMOTE smote = new SMOTE();
            smote.setInputFormat(trainingSet);
            fc.setFilter(smote);

            WekaClassifier wekaClassifier = new WekaClassifier(fc, classifier.getClass().getSimpleName(), smote.getClass().getSimpleName(), this.featureType, "none" );
            classifiers.add(wekaClassifier);

        }

    }


    private void featureAndCost(Instances trainingSet) throws Exception {

        List<Classifier> baseClassifiers = getBaseClassifiers();

        for (Classifier classifier : baseClassifiers) {

            //cost sensitivity
            CostSensitiveClassifier costSensitiveClassifier = getCostSensitiveClassifier();
            costSensitiveClassifier.setClassifier(classifier);

            //feature selection
            FilteredClassifier fc = getFilteredPlusFeature(costSensitiveClassifier, trainingSet);


            WekaClassifier wekaClassifier = new WekaClassifier(fc, classifier.getClass().getSimpleName(), "none", this.featureType, "SensitiveThreshold" );
            classifiers.add(wekaClassifier);

        }

    }




    private void noFilters(){
        List<Classifier> baseClassifiers = getBaseClassifiers();
        for (Classifier classifier : baseClassifiers) {
            WekaClassifier wekaClassifier = new WekaClassifier(classifier, classifier.getClass().getSimpleName(), "none", "none", "none");
            classifiers.add(wekaClassifier);
        }
    }


    private CostSensitiveClassifier getCostSensitiveClassifier(){
        CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier() ;
        CostMatrix costMatrix = new CostMatrix(2) ;

        costSensitiveClassifier.setMinimizeExpectedCost(false);
        costMatrix.setCell(0,0,0.0);
        costMatrix.setCell(1,0,1.0);
        costMatrix.setCell(0,1, 10.0);
        costMatrix.setCell(1,1, 0.0);

        costSensitiveClassifier.setCostMatrix(costMatrix);

        return costSensitiveClassifier;
    }



    private static List<Classifier> getBaseClassifiers(){
        List<Classifier> classifiers = new ArrayList<>();
        classifiers.add(new RandomForest());
        classifiers.add(new IBk());
        classifiers.add(new NaiveBayes());

        return classifiers;
    }

    private FilteredClassifier getFilteredPlusFeature(Classifier classifier, Instances trainingSet) throws Exception {
        AttributeSelection attributeSelection = new AttributeSelection();
        BestFirst bestFirst = new BestFirst();
        bestFirst.setDirection(new SelectedTag(0, bestFirst.getDirection().getTags()));
        attributeSelection.setSearch(bestFirst);
        attributeSelection.setInputFormat(trainingSet);

        FilteredClassifier fc = new FilteredClassifier();
        fc.setClassifier(classifier);
        fc.setFilter(attributeSelection);

        this.featureType = attributeSelection.getSearch().getClass().getSimpleName()+"(backward)";

        return fc;
    }
}
