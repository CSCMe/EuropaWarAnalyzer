package ee.tkasekamp.europawaranalyzer.parser;


import ee.tkasekamp.europawaranalyzer.core.Country;
import ee.tkasekamp.europawaranalyzer.core.War;
import ee.tkasekamp.europawaranalyzer.service.ModelService;
import ee.tkasekamp.europawaranalyzer.service.ModelServiceImpl;
import ee.tkasekamp.europawaranalyzer.service.UtilServiceImpl;
import javafx.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThreadedParserTest {
    private static final String SAVES_PATH = "src/test/resources/savegames";
    private static ArrayList<File> testSaves;

    @BeforeAll
    static void setup() {
        testSaves = new ArrayList<>();
        File dir = new File(SAVES_PATH);
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                testSaves.addAll(Arrays.asList(file.listFiles()));
            }
            else {
                testSaves.add(file);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getModels")
    public void threadedParserTest(Pair<ModelService, ModelService> servicePair) {
        ArrayList<War> normalList = new ArrayList<>();
        ArrayList<War> threadedList = new ArrayList<>();
        ArrayList<War> anomalies = new ArrayList<>();

        normalList = new ArrayList<>(servicePair.getKey().getWars());
        threadedList = new ArrayList<>(servicePair.getValue().getWars());

        if (normalList.isEmpty() || threadedList.isEmpty()) {
            assertEquals(normalList.size(), threadedList.size());
            System.out.print("No wars detected: ");
        }
        assertTrue(normalList.containsAll(threadedList) && threadedList.containsAll(normalList), normalList.size() + "," + threadedList.size());
        System.out.println("wars: " + normalList.size() + ":" + threadedList.size());
    }

    @ParameterizedTest
    @MethodSource("getModels")
    public void testDynamicCountries(Pair<ModelService, ModelService> servicePair) {
        Map<String, Country> normalCountries = servicePair.getKey().getCountries();
        Map<String, Country> threadedCountries = servicePair.getValue().getCountries();

        if (normalCountries.isEmpty() || threadedCountries.isEmpty()) {
            assertEquals(normalCountries.size(), threadedCountries.size());
            System.out.print("No dynamic countries detected: ");
        }
        assertEquals(normalCountries.size(), threadedCountries.size());
        assertEquals(normalCountries, threadedCountries);
        //assertTrue(normalCountries.keySet().containsAll(threadedCountries.keySet())
       //         && threadedCountries.keySet().containsAll(normalCountries.keySet()));
       // assertTrue(normalCountries.values().containsAll(threadedCountries.values())
        //        && threadedCountries.values().containsAll(normalCountries.values()));
        System.out.println("countries: " + normalCountries.size() + ":" + threadedCountries.size());
    }

    private static ArrayList<File> getSaves() {
        return testSaves;
    }

    private static ArrayList<Pair<ModelService, ModelService>> getModels() {
        ArrayList<Pair<ModelService, ModelService>> models = new ArrayList<>();
        for (File file : testSaves) {
            ModelService normalModel = new ModelServiceImpl(new UtilServiceImpl());
            ModelService threadedModel = new ModelServiceImpl(new UtilServiceImpl());
            normalModel.createModel(file.getAbsolutePath(), false, false,false);
            threadedModel.createModel(file.getAbsolutePath(), false, false,true);
            models.add(new Pair<>(normalModel, threadedModel));
        }
        return models;
    }
}
