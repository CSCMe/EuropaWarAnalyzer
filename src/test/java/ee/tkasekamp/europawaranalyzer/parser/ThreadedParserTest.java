package ee.tkasekamp.europawaranalyzer.parser;

import ee.tkasekamp.europawaranalyzer.core.War;
import ee.tkasekamp.europawaranalyzer.service.ModelService;
import ee.tkasekamp.europawaranalyzer.service.ModelServiceImpl;
import ee.tkasekamp.europawaranalyzer.service.UtilServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ThreadedParserTest {
    private static ThreadedParser threadedParser;
    private static NormalParser normalParser;
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
        ModelService model = new ModelServiceImpl(new UtilServiceImpl());
        normalParser = new NormalParser(model);
        threadedParser = new ThreadedParser(model);
    }

    @ParameterizedTest
    @MethodSource("getSaves")
    public void threadedParserTest(File file) {
        ArrayList<War> normalList = new ArrayList<>();
        ArrayList<War> threadedList = new ArrayList<>();
        ArrayList<War> anomalies = new ArrayList<>();

            try {
                normalList = new ArrayList<>(normalParser.readSaveFile(file.getAbsolutePath()));
                threadedList = new ArrayList<>(threadedParser.readSaveFile(file.getAbsolutePath()));
            } catch (IOException e) {
                Assertions.fail(e);
            }
            if (normalList.isEmpty() || threadedList.isEmpty()) {
                Assertions.fail();
                return;
            }
            for (War war : normalList) {
                if (!threadedList.contains(war)) {
                    anomalies.add(war);
                }
            }
            for (War war : threadedList) {
                if (!normalList.contains(war)) {
                    anomalies.add(war);
                }
            }
        Assertions.assertTrue(normalList.containsAll(threadedList) && threadedList.containsAll(normalList), normalList.size() + "," + threadedList.size() + " anoms: " + anomalies.size());
        System.out.println(normalList.size());
    }
    private static ArrayList<File> getSaves() {
        return testSaves;
    }
}
