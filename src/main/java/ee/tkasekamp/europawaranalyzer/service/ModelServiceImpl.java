package ee.tkasekamp.europawaranalyzer.service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import ee.tkasekamp.europawaranalyzer.controller.MainController;
import ee.tkasekamp.europawaranalyzer.core.Country;
import ee.tkasekamp.europawaranalyzer.core.JoinedCountry;
import ee.tkasekamp.europawaranalyzer.core.War;
import ee.tkasekamp.europawaranalyzer.parser.Parser;
import ee.tkasekamp.europawaranalyzer.parser.NormalParser;
import ee.tkasekamp.europawaranalyzer.parser.ThreadedParser;
import ee.tkasekamp.europawaranalyzer.util.Localisation;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;

public class ModelServiceImpl implements ModelService {

	private String date = "";
	private String player = "";
	private String startDate = "";
	private ConcurrentSkipListMap<String, Country> countryMap;
	private ArrayList<War> warList;
	private ArrayList<String> modList;

	private final UtilService utilServ;
	public ModelServiceImpl(UtilService utilServ) {
		this.utilServ = utilServ;
		countryMap = new ConcurrentSkipListMap<>();
		warList = new ArrayList<>();
		modList = new ArrayList<>();
	}

	@Override
	public String createModel(String saveGamePath, boolean useLocalisation, boolean useModLocalisation, boolean useMultithreading) {
		Parser parser;
		if(useMultithreading) {
			parser = new ThreadedParser(this);
		}
		else {
			parser = new NormalParser(this);
		}
		long start = System.nanoTime();
		try {
			warList = parser.readSaveFile(saveGamePath);
		} catch (IOException e1) {
			return "Couldn't read save game";
		}
		/* Generating a list of countries from warList */
		createUniqueCountryList();
		double analyzeTime = (System.nanoTime() - start)/1000000000.00;
		start = System.nanoTime();
		/* Localisation */
		if (useLocalisation) {
			Localisation.readLocalisation(utilServ.getInstallFolder(), countryMap);
			if (useModLocalisation) {
				String modFolder = utilServ.getSteamModFolder();
				Localisation.readModLocalisation(utilServ.getModFolder(), utilServ.getSteamModFolder(), modList, countryMap);
			}
			parser.getDynamicCountries().forEach(x -> {
				Country country = countryMap.getOrDefault(x.getTag(), new Country("---"));
				country.setOfficialName(x.getOfficialName());
			});
		}
		double localisationTime = (System.nanoTime() - start)/1000000000.00;
		start = System.nanoTime();
		try {
			if (useMultithreading) {
				utilServ.writePathsToFile();
			}
		} catch (IOException e) {
			return "Couldn't write paths to file";
		}

		countryMap.forEach((tag, country) -> country.setFlag(utilServ.loadFlag(tag)));
		/* User Feedback */
		String returnString = "Analyzed " + warList.size() + " wars in ~" + analyzeTime + "s\n";
		if (useLocalisation) {
			returnString += "Localisation loaded in ~" + localisationTime + "s\t";
		}
		return returnString;
	}

	@Override
	public void reset() {
		date = "";
		player = "";
		startDate = "";
		countryMap.clear();
		warList.clear();
		modList.clear();
	}

	@Override
	public String getOfficialName(String tag) {
		try {
			return countryMap.get(tag).getOfficialName();
		} catch (NullPointerException e) {
			return tag;
		}

	}

	/**
	 * Creating a list that will contain every unique country.
	 */
	private void createUniqueCountryList() {
		Set<String> set = new HashSet<>();
		for (War item : warList) {
			for (JoinedCountry country : item.getCountryList()) {
				set.add(country.getTag());
			}
		}
		set.forEach(x -> countryMap.put(x, new Country(x)));
	}

	@Override
	public void setDate(String line) {
		this.date = line;

	}

	@Override
	public void setPlayer(String line) {
		this.player = line;

	}

	@Override
	public void setStartDate(String line) {
		this.startDate = line;

	}

	@Override
	public String getDate() {
		return date;
	}

	@Override
	public String getPlayer() {
		return player;
	}

	@Override
	public String getStartDate() {
		return startDate;
	}

	@Override
	public String getPlayerOfficial() {
		return getOfficialName(player);
	}

	@Override
	public Map<String, Country> getCountries() {
		return countryMap;
	}

	@Override
	public ArrayList<War> getWars() {
		return warList;
	}

	public void setModList(ArrayList<String> modList) {
		this.modList = modList;
	}

	@Override
	public Image getFlag(String tag) {
		return countryMap.get(tag).getFlag();
	}

}
