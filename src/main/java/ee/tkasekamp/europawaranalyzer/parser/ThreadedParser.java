package ee.tkasekamp.europawaranalyzer.parser;

import ee.tkasekamp.europawaranalyzer.core.Country;
import ee.tkasekamp.europawaranalyzer.core.War;
import ee.tkasekamp.europawaranalyzer.service.ModelService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.*;
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

        ZipFile zipFile = null;
        InputStream gameStateStream;
        InputStream metaStream;
        InputStream countryStream;
        try {
            zipFile = new ZipFile(saveGamePath);
            metaStream = zipFile.getInputStream(zipFile.getEntry("meta"));
            gameStateStream = zipFile.getInputStream(zipFile.getEntry("gamestate"));
            countryStream = zipFile.getInputStream(zipFile.getEntry("gamestate"));
        }
        catch (ZipException e) {
            metaStream = new FileInputStream(saveGamePath);
            gameStateStream = new FileInputStream(saveGamePath);
            countryStream = new FileInputStream(saveGamePath);
        }

        warList = new ArrayList<>();

        InputStream finalMetaStream = metaStream;
        InputStream finalGameStateStream = gameStateStream;
        InputStream finalCountryStream = countryStream;
        ExecutorService service = Executors.newCachedThreadPool();
        service.execute(() -> {
            try {
                readMetaData(finalMetaStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        service.execute(() -> {
            try {
                read(finalGameStateStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        service.execute(() -> {
            try {
                readDynamicCountries(finalCountryStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        try {
            service.shutdown();
            service.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Predicate<War> filter = war->(war == null || war.getOriginalAttacker().equals("") || war.getOriginalDefender().equals(""));
        warList.removeIf(filter);

        return warList;
    }

    public void read(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "ISO8859_1"));
        ArrayList<ArrayList<String>> warLists = new ArrayList<>();
        warLists.add(new ArrayList<>());
        int i = 0;
        String originalLine = "";
        while ((originalLine = reader.readLine()) != null) {
            if (originalLine.contains("start_date=") && modelService.getStartDate().equals("")) {
                modelService.setStartDate(addZerosToDate(
                        nameExtractor(originalLine.replaceAll("\t", ""), 11, false)));
            } else if (originalLine.equals("active_war={") || originalLine.equals("previous_war={")) {
                break;
            }
        }
        do {
            switch (originalLine) {
                case "active_war={":
                case "previous_war={":
                    i++;
                    warLists.add(new ArrayList<>());
                default:
                    if (i > 0) {
                        warLists.get(i).add(originalLine);
                    }
                    break;
            }
        } while((originalLine = reader.readLine()) != null);

        reader.close();
        warLists.remove(0);

        ExecutorService es = Executors.newCachedThreadPool();
        ArrayList<Future<War>> futures = new ArrayList<>();
        for (ArrayList<String> war : warLists) {
            FutureTask<War> currentTask = new FutureTask<War>(new BetterWarParser(war));
            futures.add(currentTask);
            es.execute(currentTask);
        }

        for (Future<War> warFuture : futures) {
            try {
                warList.add(warFuture.get(1, TimeUnit.SECONDS));
            } catch (ExecutionException | TimeoutException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        es.shutdown();
    }

    public void readDynamicCountries(InputStream stream) throws IOException{
        BufferedReader reader;
        reader = new BufferedReader(new InputStreamReader(stream, "ISO8859_1"));
        String originalLine;
        boolean countryProcessing = false;
        Country dynamicCountry = new Country("---");

        while((originalLine = reader.readLine()) != null) {
            if (originalLine.startsWith("dynamic_countries=")) {
                dynamicCountryList = dynamicCountryList.isEmpty() ? createDynamicCountryList(reader.readLine()) : dynamicCountryList;

            } else if (originalLine.startsWith("countries={")) {
                break;
            }
        }

        while((originalLine = reader.readLine()) != null) {
            if (originalLine.startsWith("\t\t\t")) {
                continue;
            }
            String line = originalLine.replaceAll("\t", "");
            if (countryProcessing) {
                if (line.startsWith("name=")) {
                    dynamicCountry.setOfficialName(nameExtractor(line, 6, true));
                    countryProcessing = false;
                }
            } else if (originalLine.matches("\t[A-Z][0-9]{2}=\\{")) {
                for (Country country : dynamicCountryList) {
                    if (line.contains(country.getTag())) {
                        dynamicCountry = country;
                        countryProcessing = true;
                    }
                }
            } else if (originalLine.matches("active_advisors=\\{")) {
                break;
            }
        }
        reader.close();
    }

    public synchronized void addWar(War war) {
        warList.add(war);
    }
}
