package ee.tkasekamp.europawaranalyzer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ee.tkasekamp.europawaranalyzer.core.Country;

public class Localisation {

	private static String ENGLISH_LANGUAGE = "english";
	/**
	 * Main method of this class. Manages the reading from csv
	 */
	public static void readLocalisation(String installPath,
			Map<String, Country> countryMap) {

		try {
			List<String> loclist = getAllLocalisationFiles(installPath);

			for (String string : loclist) {
				readYML(string, countryMap);
			}

		} catch (NullPointerException | IOException e) {}

	}

	private static void readYML(String filename, Map<String, Country> countryMap)
			throws IOException {
		/* The same reader as in SaveGameReader */
		InputStreamReader reader = new InputStreamReader(new FileInputStream(filename),
				StandardCharsets.UTF_8);
		BufferedReader scanner = new BufferedReader(reader);

		String line;
		while ((line = scanner.readLine()) != null) {
			String[] dataArray = line.split(":[0-9] "); // Splitting the line
			String countryTag = dataArray[0].replace(" ", ""); //Gets the country tag properly
			if(dataArray.length > 1) { //only do the later parts if the array is long enough
				String countryName = dataArray[1].replace("\"","");
				if (countryMap.containsKey(countryTag)) {
					countryMap.get(countryTag).setOfficialName(countryName);
				}
			}
		}
		// Close the file once all data has been read.
		scanner.close();
	}

	/**
	 * Reads the given file and for any given line checks if the tag is equal to the one in
	 * countryList.
	 */
	private static void readCSV(String filename, TreeMap<String, Country> countryTreeMap)
			throws IOException {
		/* The same reader as in SaveGameReader */
		InputStreamReader reader = new InputStreamReader(new FileInputStream(filename),
				"ISO8859_1"); // This encoding seems to work for ö
		// and ü
		BufferedReader scanner = new BufferedReader(reader);

		String line;
		while ((line = scanner.readLine()) != null) {
			String[] dataArray = line.split(";"); // Splitting the line

			if (countryTreeMap.containsKey(dataArray[0])) {
				countryTreeMap.get(dataArray[0]).setOfficialName(dataArray[1]);
			}


		}
		// Close the file once all data has been read.
		scanner.close();

	}

	private static List<String> getLocalisationFiles(String path) throws NullPointerException {
		List<String> locList = new ArrayList<>();

		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		String file;

		for (File listOfFile : listOfFiles) {
			if (listOfFile.isFile()) {
				file = listOfFile.getName(); //only gets the english names
				if ((file.endsWith(ENGLISH_LANGUAGE + ".yml") || file.endsWith(ENGLISH_LANGUAGE + ".YML"))) {
					locList.add(listOfFile.getAbsolutePath());
				}
			}
		}

		return locList;

	}
	private static List<String> getAllLocalisationFiles(String path) throws NullPointerException {
		List<String> locList = new ArrayList<>();
		File folder = new File(path + "/localisation");
		File[] listOfFiles = folder.listFiles();
		String file;

		for (File listOfFile : listOfFiles) {
			if (listOfFile.isFile()) {
				file = listOfFile.getName(); //only gets the english names
				if ((file.endsWith(ENGLISH_LANGUAGE + ".yml") || file.endsWith(ENGLISH_LANGUAGE + ".YML"))) {
					locList.add(listOfFile.getAbsolutePath());
				}
			} else if (listOfFile.isDirectory()) {
				locList.addAll(getLocalisationFiles(listOfFile.getAbsolutePath()));

			}
		}
		return locList;
	}
}
