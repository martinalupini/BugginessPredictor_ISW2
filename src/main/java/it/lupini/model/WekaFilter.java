package it.lupini.model;

import weka.filters.Filter;

public class WekaFilter {

    private Filter filter;
    private String featureSelection;
    private String sampling;
    private String costSensitivity;

    public WekaFilter(Filter filter, String featureSelection, String sampling, String costSensitivity) {
        this.filter = filter;
        this.featureSelection = featureSelection;
        this.sampling = sampling;
        this.costSensitivity = costSensitivity;
    }

    public Filter getFilter() {
        return filter;
    }

    public String getFeatureSelection() {
        return featureSelection;
    }

    public String getSampling() {
        return sampling;
    }

    public String getCostSensitivity() {
        return costSensitivity;
    }
}
