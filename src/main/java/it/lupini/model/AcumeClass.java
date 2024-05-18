package it.lupini.model;

public class AcumeClass {
    private String name;
    private int size;
    private double predictedProbability;
    private String actualValue;

    public AcumeClass(String name, int size, double predictedProbability, String actualValue) {
        this.name = name;
        this.size = size;
        this.predictedProbability = predictedProbability;
        this.actualValue = actualValue;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return String.valueOf(size);
    }

    public String getPredictedProbability() {
        return String.valueOf(predictedProbability);
    }

    public String getActualValue() {
        return actualValue;
    }
}
