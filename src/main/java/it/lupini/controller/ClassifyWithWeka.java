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
    private GetClassifiers getClassifiers;
    private List<JavaClass> allClasses;
    List<AcumeClass> acumeClasses;
    private static final Logger logger = Logger.getLogger(ExtractData.class.getName());

    public ClassifyWithWeka(String project, int iterations, List<JavaClass> allClasses) {
        this.project = project;
        this.iterations = iterations;
        this.getClassifiers = new GetClassifiers();
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

            List<WekaClassifier> classifiers = getClassifiers.getClassifiers(trainingSet);

            print ="Iteration "+i+" - testing set: "+testingSet.numInstances()+" training set: "+trainingSet.numInstances();
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
                ClassifierEvaluation classifierEvaluation = new ClassifierEvaluation(project, i, evaluator, wekaClassifier, trainingPercent);
                evaluations.add(classifierEvaluation);


                if(i==this.iterations){

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

                    evaluateProbabilityAndCreateAcume(name, classifier, testingSet);

                }

            }

        }

        return evaluations;
    }


    private void evaluateProbabilityAndCreateAcume(String name, Classifier classifier, Instances testingSet) throws Exception {

        int numtesting = testingSet.numInstances();

        acumeClasses.clear();
        List<JavaClass> lastReleaseClasses = new ArrayList<>(allClasses);
        lastReleaseClasses.removeIf(javaClass -> javaClass.getRelease().id() != iterations+2);

        // Loop over each test instance.
        for (int i = 0; i < numtesting; i++)
        {
            JavaClass javaClass = lastReleaseClasses.get(i);
            // Get the true class label from the instance's own classIndex.
            String trueClassLabel =
                    testingSet.instance(i).toString(testingSet.classIndex());

            // Make the prediction here.
            double predictionIndex =
                    classifier.classifyInstance(testingSet.instance(i));

            // Get the predicted class label from the predictionIndex.
            String predictedClassLabel =
                    testingSet.classAttribute().value((int) predictionIndex);

            // Get the prediction probability distribution.
            double[] predictionDistribution =
                    classifier.distributionForInstance(testingSet.instance(i));

            // Print out the true label, predicted label, and the distribution.
            //System.out.printf("%5d: true=%-10s, predicted=%-10s, distribution=", i, trueClassLabel, predictedClassLabel);


            // Get this distribution index's class label YES.
            String predictionDistributionIndexAsClassLabel = testingSet.classAttribute().value(0);

            // Get the probability.
            double predictionProbability = predictionDistribution[0];

            //System.out.printf("[%10s : %6.3f]", 0, predictionProbability );

            AcumeClass acumeClass = new AcumeClass(javaClass.getName(), javaClass.getLoc(), predictionProbability, trueClassLabel);
            acumeClasses.add(acumeClass);
        }

        WriteCSV.createAcumeFiles(project,acumeClasses, name);

    }



}
