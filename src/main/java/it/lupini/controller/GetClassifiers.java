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
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SMOTE;

import java.util.ArrayList;
import java.util.List;

public class GetClassifiers {

    private GetClassifiers() {
    }

    public static final String FEATURE_SELECTION = "BestFirst(backward)";

    public static List<WekaClassifier> getClassifiers(Instances trainingSet){

        List<WekaClassifier> classifiers = new ArrayList<>();


        int numAttributes = trainingSet.numAttributes();
        AttributeStats attributeStats = trainingSet.attributeStats(numAttributes-1);
        int majorityClassSize = attributeStats.nominalCounts[1];
        int minorityClassSize = attributeStats.nominalCounts[0];

        double percentageSmote  =  (100.0*(majorityClassSize-minorityClassSize))/minorityClassSize;

        noFilters(classifiers);
        featureSelection(classifiers);
        smote(classifiers, percentageSmote);
        costSensitive(classifiers);
        featureAndSmote(classifiers, percentageSmote);
        featureAndCost(classifiers);

        return classifiers;

    }


    private static void featureSelection(List<WekaClassifier> classifiers){
        List<Classifier> baseClassifiers = getBaseClassifiers();

        for (Classifier classifier : baseClassifiers) {

            FilteredClassifier fc = getFilteredPlusFeature(classifier);

            WekaClassifier wekaClassifier = new WekaClassifier(fc, classifier.getClass().getSimpleName(), "none", FEATURE_SELECTION, "none");
            classifiers.add(wekaClassifier);
        }

    }


    private static void smote(List<WekaClassifier> classifiers, double percentageSmote){

        List<Classifier> baseClassifiers = getBaseClassifiers();

        for (Classifier classifier : baseClassifiers) {

            FilteredClassifier fc = new FilteredClassifier();
            fc.setClassifier(classifier);

            SMOTE smote = new SMOTE();
            smote.setPercentage(percentageSmote);
            fc.setFilter(smote);


            WekaClassifier wekaClassifier = new WekaClassifier(fc, classifier.getClass().getSimpleName(), smote.getClass().getSimpleName(), "none", "none");
            classifiers.add(wekaClassifier);
        }


    }


    private static void costSensitive(List<WekaClassifier> classifiers){
        List<Classifier> baseClassifiers = getBaseClassifiers();

        for (Classifier classifier : baseClassifiers) {
            CostSensitiveClassifier costSensitiveClassifier = getCostSensitiveClassifier();
            costSensitiveClassifier.setClassifier(classifier);

            WekaClassifier wekaClassifier = new WekaClassifier(costSensitiveClassifier, classifier.getClass().getSimpleName(), "none", "none", "SensitiveThreshold");
            classifiers.add(wekaClassifier);
        }
    }

    private static void featureAndSmote(List<WekaClassifier> classifiers, double percentageSmote){
        List<Classifier> baseClassifiers = getBaseClassifiers();

        for (Classifier classifier : baseClassifiers) {

            //feature selection
            FilteredClassifier fc1 = getFilteredPlusFeature(classifier);

            //smote
            FilteredClassifier fc = new FilteredClassifier();

            SMOTE smote = new SMOTE();
            smote.setPercentage(percentageSmote);
            fc.setFilter(smote);

            fc.setClassifier(fc1);

            WekaClassifier wekaClassifier = new WekaClassifier(fc, classifier.getClass().getSimpleName(), smote.getClass().getSimpleName(), FEATURE_SELECTION, "none" );
            classifiers.add(wekaClassifier);

        }

    }


    private static void featureAndCost(List<WekaClassifier> classifiers){

        List<Classifier> baseClassifiers = getBaseClassifiers();

        for (Classifier classifier : baseClassifiers) {

            //cost sensitivity
            CostSensitiveClassifier costSensitiveClassifier = getCostSensitiveClassifier();
            costSensitiveClassifier.setClassifier(classifier);

            //feature selection
            FilteredClassifier fc = getFilteredPlusFeature(costSensitiveClassifier);


            WekaClassifier wekaClassifier = new WekaClassifier(fc, classifier.getClass().getSimpleName(), "none", FEATURE_SELECTION, "SensitiveThreshold" );
            classifiers.add(wekaClassifier);

        }

    }




    private static void noFilters(List<WekaClassifier> classifiers){
        List<Classifier> baseClassifiers = getBaseClassifiers();
        for (Classifier classifier : baseClassifiers) {
            WekaClassifier wekaClassifier = new WekaClassifier(classifier, classifier.getClass().getSimpleName(), "none", "none", "none");
            classifiers.add(wekaClassifier);
        }
    }


    private static CostSensitiveClassifier getCostSensitiveClassifier(){
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

    private static FilteredClassifier getFilteredPlusFeature(Classifier classifier){
        AttributeSelection attributeSelection = new AttributeSelection();
        BestFirst bestFirst = new BestFirst();
        bestFirst.setDirection(new SelectedTag(0, bestFirst.getDirection().getTags()));
        attributeSelection.setSearch(bestFirst);

        FilteredClassifier fc = new FilteredClassifier();
        fc.setClassifier(classifier);
        fc.setFilter(attributeSelection);

        return fc;
    }
}
