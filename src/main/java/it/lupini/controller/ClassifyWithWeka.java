package it.lupini.controller;


import it.lupini.model.AcumeClass;
import it.lupini.model.ClassifierEvaluation;
import it.lupini.model.JavaClass;
import it.lupini.model.WekaClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ClassifyWithWeka {

    private String project;
    private int iterations;
    private List<JavaClass> allClasses;
    List<AcumeClass> acumeClasses;
    private static final Logger logger = Logger.getLogger(ClassifyWithWeka.class.getName());

    public ClassifyWithWeka(String project, int iterations, List<JavaClass> allClasses) {
        this.project = project;
        this.iterations = iterations;
        this.allClasses = allClasses;
        this.acumeClasses = new ArrayList<>();

    }

    public List<ClassifierEvaluation> evaluateClassifiers() throws Exception {
        List<ClassifierEvaluation> evaluations = new ArrayList<>();
        String print;

        for(int i = 1; i <= this.iterations; i++){

            //getting training and testing set
            Instances trainingSet = ConverterUtils.DataSource.read("arffFiles/"+project+"/iteration_"+i+"/Training.arff");
            Instances testingSet = ConverterUtils.DataSource.read("arffFiles/"+project+"/iteration_"+i+"/Testing.arff");

            //setting the last column as the variable of interest
            int numAttr = trainingSet.numAttributes();
            trainingSet.setClassIndex(numAttr - 1);
            testingSet.setClassIndex(numAttr - 1);

            List<WekaClassifier> classifiers =GetClassifiers.getClassifiers(trainingSet);

            print =project.toUpperCase()+ ": Iteration "+i+" - testing set: "+testingSet.numInstances()+" training set: "+trainingSet.numInstances();
            logger.info(print);

            for(WekaClassifier wekaClassifier : classifiers){
                Classifier classifier = wekaClassifier.getClassifier();

                //training classifier
                classifier.buildClassifier(trainingSet);

                //testing classifier
                Evaluation evaluator = new Evaluation(testingSet) ;
                evaluator.evaluateModel(classifier, testingSet);

                //collecting the results
                double trainingPercent = 100.0 * trainingSet.numInstances() / (trainingSet.numInstances() + testingSet.numInstances());
                ClassifierEvaluation classifierEvaluation = new ClassifierEvaluation(project, i, evaluator, wekaClassifier, trainingPercent, 1.0, 10.0);
                evaluations.add(classifierEvaluation);

                //retrieving probability of predictions
                String name = getNameOfFile(wekaClassifier, i);
                evaluateProbabilityAndCreateAcume(name, classifier, testingSet, i);


            }

        }

        return evaluations;
    }


    private String getNameOfFile(WekaClassifier wekaClassifier, int iteration){
        String name = wekaClassifier.getName();
        if(!wekaClassifier.getFeatureSelection().equals("none")){
            name = name + "_"+ wekaClassifier.getFeatureSelection();
        }
        if(!wekaClassifier.getSampling().equals("none")){
            name = name + "_"+ wekaClassifier.getSampling();
        }
        if(!wekaClassifier.getCostSensitive().equals("none")){
            name = name + "_"+ wekaClassifier.getCostSensitive();
        }
        name= name+"_"+iteration;
        return name;
    }

    private void evaluateProbabilityAndCreateAcume(String name, Classifier classifier, Instances testingSet, int iteration) throws Exception {

        int numtesting = testingSet.numInstances();
        int id =0;

        acumeClasses.clear();
        List<JavaClass> lastReleaseClasses = new ArrayList<>(allClasses);
        lastReleaseClasses.removeIf(javaClass -> javaClass.getRelease().id() != iteration+2);


        // Loop over each test instance.
        for (int i = 0; i < numtesting; i++)
        {
            JavaClass javaClass = lastReleaseClasses.get(i);
            // Get the true class label from the instance's own classIndex.
            String trueClassLabel =
                    testingSet.instance(i).toString(testingSet.classIndex());

            // Get the prediction probability distribution.
            double[] predictionDistribution =
                    classifier.distributionForInstance(testingSet.instance(i));

            // Get the probability.
            double predictionProbability = predictionDistribution[0];

            AcumeClass acumeClass = new AcumeClass(id, javaClass.getLoc(), predictionProbability, trueClassLabel);
            acumeClasses.add(acumeClass);

            id++;
        }

        WriteCSV.createAcumeFiles(project,acumeClasses, name);

    }



}
