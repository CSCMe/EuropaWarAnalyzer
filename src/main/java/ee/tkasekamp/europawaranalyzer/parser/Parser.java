package ee.tkasekamp.europawaranalyzer.parser;

import ee.tkasekamp.europawaranalyzer.core.Country;
import ee.tkasekamp.europawaranalyzer.core.War;
import ee.tkasekamp.europawaranalyzer.service.ModelService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

public abstract class Parser {
    protected Collection<Country> dynamicCountryList = new ArrayList<>();
    protected ModelService modelService;

    public Parser(ModelService modelService) {
        this.modelService = modelService;
    }

    abstract public ArrayList<War> readSaveFile(String saveGamePath) throws IOException;

    public void readMetaData(InputStream metaDataStream) throws IOException {
        InputStreamReader reader = new InputStreamReader(metaDataStream, "ISO8859_1");
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
        reader.close();
    }


    public Collection<Country> getDynamicCountries() {
        return dynamicCountryList;
    }

    /**
     * Extracts the valuable data from a line by deleting the unimportant characters.
     *
     * @param line
     * @param index      Last character to be removed
     * @param removeLast If true, removes the last character (used for ")
     * @return The correct line
     */
    protected String nameExtractor(String line, int index, boolean removeLast) {
        StringBuilder sb = new StringBuilder(line);
        sb.delete(0, index);
        if (removeLast) {
            sb.setLength(sb.length() - 1); // Removes last "
        }
        return sb.toString();
    }

    protected ArrayList<Country> createDynamicCountryList(String line) {
        ArrayList<Country> list = new ArrayList<>();
        line = line.replaceAll("\t", "");
        String[] splitLine = line.split("\\s");
        for (String countryTag : splitLine) {
            list.add(new Country(countryTag));
        }
        return list;
    }

    protected String addZerosToDate(String date) {
        String[] splitDate = date.split("\\.");
        for(int i = splitDate[0].length(); i < 4; i++) {
            splitDate[0] = "0" + splitDate[0];
        }
        for(int i = splitDate[1].length(); i < 2; i++) {
            splitDate[1] = "0" + splitDate[1];
        }
        for(int i = splitDate[2].length(); i < 2; i++) {
            splitDate[2] = "0" + splitDate[2];
        }
        return splitDate[0] + "." + splitDate[1] + "." + splitDate[2];
    }
}
