package it.lupini;

import it.lupini.controller.ExtractData;

public class Main {
    public static void main(String[] args) throws Exception {
        ExtractData.buildDataset("BOOKKEEPER");
        ExtractData.buildDataset("AVRO");
    }

}