package it.lupini.controller;


import it.lupini.model.ClassifierEvaluation;
import it.lupini.model.WekaClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.ArrayList;
import java.util.List;

public class ClassifyWithWeka {

    private String project;
    private int iterations;
    private GetClassifiers getClassifiers;

    public ClassifyWithWeka(String project, int iterations) {
        this.project = project;
        this.iterations = iterations;
        this.getClassifiers = new GetClassifiers();

    }

    public List<ClassifierEvaluation> evaluateClassifiers() throws Exception {
        List<ClassifierEvaluation> evaluations = new ArrayList<>();
        for(int i = 1; i <= this.iterations; i++){

            //getting training and testing set
            Instances trainingSet = ConverterUtils.DataSource.read("arffFiles/"+project+"/iteration_"+i+"/Training.arff");
            Instances testingSet = ConverterUtils.DataSource.read("arffFiles/"+project+"/iteration_"+i+"/Testing.arff");

            //setting the last column as the variable of interest
            int numAttr = trainingSet.numAttributes();
            trainingSet.setClassIndex(numAttr - 1);
            testingSet.setClassIndex(numAttr - 1);

            List<WekaClassifier> classifiers = getClassifiers.getClassifiers(trainingSet);

            for(WekaClassifier wekaClassifier : classifiers){
                Classifier classifier = wekaClassifier.getClassifier();

                //training classifier
                classifier.buildClassifier(trainingSet);

                //testing classifier
                Evaluation evaluator = new Evaluation(testingSet) ;
                evaluator.evaluateModel(classifier, testingSet);

                //collecting the results
                double trainingPercent = 100.0 * (trainingSet.numInstances() / (trainingSet.numInstances() + testingSet.numInstances()));
                ClassifierEvaluation classifierEvaluation = new ClassifierEvaluation(project, i, evaluator, wekaClassifier, trainingPercent);
                evaluations.add(classifierEvaluation);

            }

        }

        return evaluations;
    }



}
