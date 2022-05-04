package ee.tkasekamp.europawaranalyzer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import ee.tkasekamp.europawaranalyzer.core.Country;

public class Localisation {

	private final static String ENGLISH_LANGUAGE_LOC = "_l_english.yml";

	/**
	 * Main method of this class.
	 * Manages reading from game .yml files
	 */
	public static void readLocalisation(String installPath,
			Map<String, Country> countryMap) {

		try {
			List<String> loclist = getLocalisationFiles(installPath + "/localisation");

			for (String string : loclist) {
				readYML(string, countryMap);
			}

		} catch (NullPointerException | IOException e) {}

	}

	/**
	 * Manages reading localisation from mods
	 * Checks the mod directory for mods and reads their localisation
	 * Checks the Steam directory if EUIV is installed via steam
	 * @param modsPath Path to the documents mod dir
	 * @param steamModsPath Path the the steam mod dir
	 * @param mods List of Strings with the format: "mod/[MODNAME].mod"
	 * @param countryMap CountryMap
	 */
	public static void readModLocalisation(String modsPath, String steamModsPath, ArrayList<String> mods, Map<String, Country> countryMap) {
		for (int i = 0; i < mods.size(); i++) {
			mods.set(i, mods.get(i).replace("mod", "").replace(".", ""));
		}

		for (String mod : mods) {
			String modLocPath = modsPath + mod;
			readLocalisation(modLocPath, countryMap);
		}

		if (!steamModsPath.isEmpty()) {
			for (String mod : mods) {
				String modLocPath = steamModsPath + mod.replace("ugc_", "");
				readLocalisation(modLocPath, countryMap);
			}
		}
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
			if(countryTag.length() == 3 && dataArray.length > 1) { //only do the later parts if the array is long enough
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
				if ((file.toLowerCase().endsWith(ENGLISH_LANGUAGE_LOC))) {
					locList.add(listOfFile.getAbsolutePath());
				}
			} else if (listOfFile.isDirectory()) {
				locList.addAll(getLocalisationFiles(listOfFile.getAbsolutePath()));

			}
		}
		return locList;
	}
}
