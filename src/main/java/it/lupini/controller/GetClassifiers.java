package it.lupini.controller;

import it.lupini.model.WekaClassifier;
import it.lupini.model.WekaFilter;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
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
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;

import java.util.ArrayList;
import java.util.List;

public class GetClassifiers {

    private List<WekaClassifier> classifiers;

    private int majorityDimension;
    private int minorityDimension;

    List<WekaFilter> featureSelectionFilters;
    List<WekaFilter> samplingFilters;


    public GetClassifiers() {
        this.classifiers = new ArrayList<>();
    }

    public List<WekaClassifier> getClassifiers(Instances trainingSet){

        AttributeStats attStats = trainingSet.attributeStats(trainingSet.numAttributes()-1);
        majorityDimension = attStats.nominalCounts[1];
        minorityDimension = attStats.nominalCounts[0];

        featureSelectionFilters = featureSelectionFilters();
        samplingFilters = samplingFilters();

        basicClassifiers();

        applyOneFilter("FEATURE");
        applyOneFilter("SAMPLING");
        applyOneFilter("COST");

        applyTwoFilters("FEATURE AND SAMPLING");
        applyTwoFilters("FEATURE AND COST");
        applyTwoFilters("SAMPLING AND COST");

        applyAll();



        return classifiers;

    }

    private void applyOneFilter(String type){
        List<Classifier> baseClassifiers = getBaseClassifiers();

        switch(type) {
            case "FEATURE":
                applyOne(baseClassifiers, featureSelectionFilters);

                break;

            case "SAMPLING":
                applyOne(baseClassifiers, samplingFilters);
                break;

            case "COST":
                for (Classifier classifier : baseClassifiers) {
                    CostSensitiveClassifier costSensitiveClassifier = getCostSensitiveClassifier();
                    costSensitiveClassifier.setClassifier(classifier);

                    WekaClassifier wekaClassifier = new WekaClassifier(costSensitiveClassifier, classifier.getClass().getSimpleName(), "none", "none", "Cost sensitive");
                    classifiers.add(wekaClassifier);
                }

                break;

        }

    }


    private void applyTwoFilters(String filters){
        List<Classifier> baseClassifiers = getBaseClassifiers();

        switch(filters) {
            case "FEATURE AND SAMPLING":
                for (Classifier classifier : baseClassifiers) {
                    for(WekaFilter samplingFilter : samplingFilters){
                        for(WekaFilter featureFilter : featureSelectionFilters){
                            FilteredClassifier firstClassifier = new FilteredClassifier();
                            FilteredClassifier secondClassifier = new FilteredClassifier();

                            //creating a first filtered classifier with only the sampling filter
                            firstClassifier.setClassifier(classifier);
                            firstClassifier.setFilter(samplingFilter.getFilter());

                            //creating the final classifier by setting the previous classifier as the classifier
                            secondClassifier.setClassifier(firstClassifier);
                            secondClassifier.setFilter(samplingFilter.getFilter());

                            classifiers.add(new WekaClassifier(secondClassifier, classifier.getClass().getSimpleName(), samplingFilter.getSampling(), featureFilter.getFeatureSelection(), featureFilter.getCostSensitivity()));

                        }
                    }
                }
                break;

            case "FEATURE AND COST":
                applyTwo(baseClassifiers, featureSelectionFilters);

                break;

            case "SAMPLING AND COST":
                applyTwo(baseClassifiers, samplingFilters);
                break;

        }

    }


    private void applyAll(){
        List<Classifier> baseClassifiers = getBaseClassifiers();

        for(Classifier classifier : baseClassifiers) {
            CostSensitiveClassifier costSensitiveClassifier = getCostSensitiveClassifier();
            for(WekaFilter sampling : samplingFilters){
              for(WekaFilter feature: featureSelectionFilters){

                  //first the cost sensitive classifier
                  costSensitiveClassifier.setClassifier(classifier);

                  //second the sampling classifier
                  FilteredClassifier firstClassifier = new FilteredClassifier();
                  firstClassifier.setFilter(sampling.getFilter());
                  firstClassifier.setClassifier(costSensitiveClassifier);

                  //third the feature selection classifier
                  FilteredClassifier finalClassifier = new FilteredClassifier();
                  finalClassifier.setFilter(feature.getFilter());
                  finalClassifier.setClassifier(firstClassifier);

                  classifiers.add(new WekaClassifier(finalClassifier, classifier.getClass().getSimpleName(), sampling.getSampling(), feature.getFeatureSelection(), "Cost sensitive"));


              }

            }

        }

    }

    private void applyOne(List<Classifier> baseClassifiers, List<WekaFilter> filters){
        for (WekaFilter f : filters) {
            for (Classifier classifier : baseClassifiers) {
                FilteredClassifier filteredClassifier = new FilteredClassifier();
                filteredClassifier.setFilter(f.getFilter());
                filteredClassifier.setClassifier(classifier);

                WekaClassifier wekaClassifier = new WekaClassifier(filteredClassifier, classifier.getClass().getSimpleName(), f.getSampling(), f.getFeatureSelection(), f.getCostSensitivity());
                classifiers.add(wekaClassifier);
            }
        }
    }

    private void applyTwo(List<Classifier> baseClassifiers, List<WekaFilter> filters){
        for(Classifier classifier : baseClassifiers) {
            CostSensitiveClassifier costSensitiveClassifier = getCostSensitiveClassifier();
            for(WekaFilter filter : filters){
                FilteredClassifier filteredClassifier = new FilteredClassifier();
                filteredClassifier.setFilter(filter.getFilter());
                costSensitiveClassifier.setClassifier(classifier);
                filteredClassifier.setClassifier(costSensitiveClassifier);


                classifiers.add(new WekaClassifier(filteredClassifier, classifier.getClass().getSimpleName(), filter.getSampling(), filter.getFeatureSelection(), filter.getCostSensitivity()));


            }

        }
    }




    private void basicClassifiers(){
        List<Classifier> baseClassifiers = getBaseClassifiers();
        for (Classifier classifier : baseClassifiers) {
            WekaClassifier wekaClassifier = new WekaClassifier(classifier, classifier.getClass().getSimpleName(), "none", "none", "none");
            classifiers.add(wekaClassifier);
        }
    }


    private List<WekaFilter> samplingFilters(){
        List<WekaFilter> filters = new ArrayList<>();

        // Oversampling
        Resample oversampling = new Resample() ;
        oversampling.setNoReplacement(false);
        oversampling.setBiasToUniformClass(1.0);

        double resamplePercentage = (((double) majorityDimension) / (majorityDimension + minorityDimension)) * 100 ;

        oversampling.setSampleSizePercent(2 * resamplePercentage);
        filters.add(new WekaFilter(oversampling, "none", oversampling.getClass().getSimpleName(), "none"));


        //Undersampling
        SpreadSubsample undersampling = new SpreadSubsample() ;
        undersampling.setDistributionSpread(1.0);
        filters.add(new WekaFilter(undersampling, "none", undersampling.getClass().getSimpleName(), "none"));

        //SMOTE
        SMOTE smote = new SMOTE() ;
        double smotePercentage;
        if (minorityDimension == 0) {
            smotePercentage = 0 ;
        }
        else {
            smotePercentage = ((majorityDimension - minorityDimension) / ((double) minorityDimension)) * 100.0 ;
        }
        smote.setPercentage(smotePercentage);
        smote.setClassValue("1");
        filters.add(new WekaFilter(smote, "none", smote.getClass().getSimpleName(), "none"));

        return filters;
    }


    private List<WekaFilter> featureSelectionFilters(){
        List<WekaFilter> filters = new ArrayList<>();

        //Best first (forward search)
        AttributeSelection forward = new AttributeSelection();
        BestFirst bestFirst = new BestFirst();
        bestFirst.setDirection(new SelectedTag(2, bestFirst.getDirection().getTags()));
        forward.setSearch(bestFirst);


        //Backward search
        AttributeSelection backward = new AttributeSelection();
        CfsSubsetEval evaluator = new CfsSubsetEval();
        GreedyStepwise search = new GreedyStepwise();
        search.setSearchBackwards(true);
        backward.setEvaluator(evaluator);
        backward.setSearch(search);


        filters.add(new WekaFilter(forward, forward.getSearch().getClass().getSimpleName(), "none", "none"));
        filters.add(new WekaFilter(backward, backward.getSearch().getClass().getSimpleName(), "none", "none"));

        return filters;

    }

    private CostSensitiveClassifier getCostSensitiveClassifier(){
        CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier() ;
        CostMatrix costMatrix = new CostMatrix(2) ;

        costSensitiveClassifier.setMinimizeExpectedCost(false);
        costMatrix.setCell(0,0,0.0);
        costMatrix.setCell(1,1,0.0);
        costMatrix.setCell(0,1, 1.0);
        costMatrix.setCell(1,0, 10.0);

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
}
