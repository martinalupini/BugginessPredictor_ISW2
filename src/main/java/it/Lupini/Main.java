package it.Lupini;

import it.Lupini.controller.ExtractData;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws GitAPIException, IOException, URISyntaxException {
        ExtractData.buildDataset("BOOKKEEPER", "https://github.com/martinalupini/bookkeeper.git");
        ExtractData.buildDataset("AVRO", "https://github.com/martinalupini/avro.git");
    }

}