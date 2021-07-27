package ee.tkasekamp.europawaranalyzer.parser;

import ee.tkasekamp.europawaranalyzer.core.Country;
import ee.tkasekamp.europawaranalyzer.core.War;
import ee.tkasekamp.europawaranalyzer.service.ModelService;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ThreadedParser extends Parser {
    private ArrayList<War> warList = new ArrayList<>();

    public ThreadedParser(ModelService modelService) {
        super(modelService);
    }

    @Override
    public ArrayList<War> readSaveFile(String saveGamePath) throws IOException {
        warList = new ArrayList<>();
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(saveGamePath);
        }
        catch (ZipException ignored) {
        }

        InputStream metaStream;
        InputStream gameStateStream;

        if (zipFile == null) {
            metaStream = new FileInputStream(saveGamePath);
            gameStateStream = new FileInputStream(saveGamePath);
        }
        else {
            metaStream = zipFile.getInputStream(zipFile.getEntry("meta"));
            gameStateStream = zipFile.getInputStream(zipFile.getEntry("gamestate"));
        }
        new Thread(() -> {
            try {
                readMeta(metaStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        read(gameStateStream);

        Predicate<War> filter = war->(war == null || war.getOriginalAttacker().equals("") || war.getOriginalDefender().equals(""));
        warList.removeIf(filter);

        return warList;
    }

    public void readMeta(InputStream stream) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream, "ISO8859_1");
        BufferedReader scanner = new BufferedReader(reader);

        String line;
        while((line = scanner.readLine()) != null) {
            if (line.startsWith("date=") && modelService.getDate().equals("")) {
                line = nameExtractor(line, 5, false);
                modelService.setDate(addZerosToDate(line));
            }
            /* Checking if it's empty is not needed as there is only one line with player= */
            else if (line.startsWith("player=")) {
                line = nameExtractor(line, 8, true);
                modelService.setPlayer(line);
                return;
            }
        }
        return;
    }

    public void read(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "ISO8859_1"));
        ArrayList<ArrayList<String>> warLists = new ArrayList<>();
        warLists.add(new ArrayList<>());
        int i = 0;
        String originalLine;
        boolean encounteredWars = false;
        boolean gotStartDate = false;
        while((originalLine = reader.readLine()) != null) {

            if (originalLine.contains("active_war")) {
                encounteredWars = true;
                i++;
                warLists.add(new ArrayList<>());
            }
            if (encounteredWars) {
                if (originalLine.contains("previous_war") || originalLine.contains("income")) {
                    i++;
                    warLists.add(new ArrayList<>());
                }
            }
            else {
                if (!gotStartDate && originalLine.contains("start_date=")) {
                    modelService.setStartDate(
                            addZerosToDate(
                                    nameExtractor(
                                            originalLine.replaceAll("\t", ""), 11, false)));
                    gotStartDate = true;
                }
            }
            if (i > 0) {
                warLists.get(i).add(originalLine);
            }

        }

        warLists.remove(0);
        warLists.remove(warLists.size()-1);
        ExecutorService es = Executors.newCachedThreadPool();
        for (ArrayList<String> war : warLists) {
            es.execute(new WarParser(war, this));
        }
        es.shutdown();
        try {
            es.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addWar(War war) {
        warList.add(war);
    }
}
