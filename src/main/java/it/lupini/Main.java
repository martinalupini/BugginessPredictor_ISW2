package it.lupini;

import it.lupini.controller.ExtractData;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws GitAPIException, IOException, URISyntaxException {
        ExtractData.buildDataset("BOOKKEEPER");
        ExtractData.buildDataset("AVRO");
    }

}