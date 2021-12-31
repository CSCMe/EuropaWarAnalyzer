package ee.tkasekamp.europawaranalyzer.parser;

import ee.tkasekamp.europawaranalyzer.core.*;
import ee.tkasekamp.europawaranalyzer.service.ModelService;
import ee.tkasekamp.europawaranalyzer.util.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Readers in order of activation in a typical save game:
 * saveGameReader
 * referenceReader
 * saveGameReader
 * ...
 * warReader
 * warGoalReader
 * battleReader
 * ...
 * battleReader
 * warReader
 * originalWarGoalReader
 * warGoalReader
 * warReader
 * saveGameReader
 * warReader
 * ...
 */
public class NormalParser extends Parser {
	/*Everything to read in wars */
	private ArrayList<War> warList; // Stores all wars in the save game
	private boolean warProcessing = false; // True when previous or active war has been found
	private int WAR_COUNTER = 0; // Used to count the list in allWars
	/* War details */
	private String dateBuffer = ""; // Stores the last date for JoinedCountry or Battle dates
	private ArrayList<JoinedCountry> countryList = new ArrayList<>();  // Stores temporarily to give to war
	private ArrayList<Battle> battleList = new ArrayList<>();  // Stores temporarily to give to war
	private WarGoal warGoal = new WarGoal();
	/* Battle details */
	private ArrayList<Unit> unitList = new ArrayList<>();  // Stores temporarily to give to battle. Used by both attacker and defender.
	private boolean battleProcessing; // True so all new lines will be read into battleReader
	private int BATTLE_COUNTER = 0; // Used to change the current battle in battleList
	private boolean attackerDefender; // True is attacker, false is defender. Changed while reading a battle as the first units are attackers, the rest defenders
	/* WarGoal details */
	private boolean warGoalProcessing; // True so all new lines will be read into warGoalReader
	private int WARGOAL_COUNTER = 0;
	private boolean casusBelliProcessing; // New for EU4. Has the same function as warGoalProcessing
	private boolean dynamicCountryListProcessing;
	private boolean countryProcessing;
	private boolean dynamicCountryProcessing; // New. True when processing a country.
	/* Various */
	private int bracketCounter = 0; // bracketCounter is uses to check if all data from the war has been read in
//	static public Reference saveGameData = new Reference(); // public so it can be used by all methods
	
	public NormalParser(ModelService modelService) {
		super(modelService);
	}

	public ArrayList<War> readSaveFile(String saveGamePath) throws IOException {

		ZipFile zipFile;
		InputStream metaStream;
		InputStream gameStateStream;
		try {
			zipFile = new ZipFile(saveGamePath);
			metaStream = zipFile.getInputStream(zipFile.getEntry("meta"));
			gameStateStream = zipFile.getInputStream(zipFile.getEntry("gamestate"));
		}
		catch (ZipException e) {
			metaStream = new FileInputStream(saveGamePath);
			gameStateStream = new FileInputStream(saveGamePath);
		}

		readMetaData(metaStream);
		return read(gameStateStream);
	}

	/**
	 * This is the main reader. It gets a path and reads line by line from it.
	 * If it find a line with a specific keyword, it passes it on to the other readers in this class.
	 * When reading a war, battle or wargoal, the Processing is set true and all lines are passed to that specific reader until
	 * the Processing is set false. War reading ends when the bracketCounter is 0 again.
	 *
	 * @param gameDataStream
	 * @return ArrayList<War>
	 * @throws IOException
	 */
	public ArrayList<War> read(InputStream gameDataStream) throws IOException {
		/* Resetting values for when user loads multiple files during a session 
		 * Might not be necessary but doing it just in case. */
//		saveGameData = new Reference();
		Country currentDynamicCountry = null;
		warList = new ArrayList<>();
		WAR_COUNTER = 0;
		BATTLE_COUNTER = 0;
		WARGOAL_COUNTER = 0;
		bracketCounter = 0;

		InputStreamReader reader = new InputStreamReader(gameDataStream, "ISO8859_1"); // This encoding seems to work for ö
		BufferedReader scanner = new BufferedReader(reader);
		String originalLine;

		// Reads start date, (dynamic) countries, skips until wars are reached.
		while (((originalLine) = scanner.readLine()) != null) {
			String line = originalLine.replaceAll("\t", "");

			/* For some reason the start date is int the actual game state data
			 * But we need it so here we go*/
			if (line.startsWith("start_date=") && modelService.getStartDate().equals("")) {
				line = nameExtractor(line, 11, false);
				modelService.setStartDate(addZerosToDate(line));
			} else if(line.startsWith("dynamic_countries=")) {
				dynamicCountryListProcessing = true;
			} else if (line.startsWith("countries=")) {
				countryProcessing = true;
			}

			if (countryProcessing) {
				bracketCounterChange(line);
				if(bracketCounter == 0) {
					countryProcessing = false;
				}
				else if(bracketCounter == 2) {
					for (Country country : dynamicCountryList) {
						if (line.contains(country.getTag())) {
							currentDynamicCountry = country;
							dynamicCountryProcessing = true;
						}
					}
					if (dynamicCountryProcessing ) {
						dynamicCountryReader(line, currentDynamicCountry);
					}
				}
			} else if (dynamicCountryListProcessing) {
				bracketCounterChange(line);
				if (bracketCounter == 0) {
					dynamicCountryListProcessing = false;
				}
				dynamicCountryList = dynamicCountryList.isEmpty() ? createDynamicCountryList(scanner.readLine()) : dynamicCountryList;
			}

			if (line.startsWith("previous_war={") || line.startsWith("active_war={")) {
				break;
			}
		}
		assert originalLine != null;
		do {
			String line = originalLine.replaceAll("\t", "");

			if (line.startsWith("previous_war={") || line.startsWith("active_war={")) {
				warProcessing = true;
		    	/* Further  check if war is active */
				if (line.startsWith("previous_war=")) {
					warList.add(new War(false));
				} else {
					warList.add(new War(true));
				}
			}

			/* Checking if the line needs to be passed on to other readers */
			if (warProcessing) {
				bracketCounterChange(line);

				if (line.startsWith("battle=") || battleProcessing) {
					battleProcessing = true;
					battleReader(line);
				} else if (line.startsWith("war_goal") || warGoalProcessing) { //War goals are only set for OLD wars (wars before game start)
					warGoalProcessing = true;
					warGoalReader(line);

				} else if (Arrays.stream(Constants.CASUS_BELLI).anyMatch(line::startsWith) || casusBelliProcessing) {
					casusBelliProcessing = true;
					casusBelliReader(line);

				} else {
					warReader(line);
				}
				/*
				 * bracketCounter will only reach 0 when the whole war has been processed. Name check is required because
				 * on the first cycle bracketcounter will still be 0.
				 */
				if (bracketCounter == 0 && !(warList.get(WAR_COUNTER).getName().equals(""))) {
					/* General housekeeping
					 * country and battle lists to proper types
					 *  */
					JoinedCountry[] countryTempList = new JoinedCountry[countryList.size()];
					JoinedCountry[] countryTempList2 = countryList.toArray(countryTempList);
					warList.get(WAR_COUNTER).setCountryList(countryTempList2);

					Battle[] battleTempList = new Battle[battleList.size()];
					Battle[] battleTempList2 = battleList.toArray(battleTempList);
					warList.get(WAR_COUNTER).setBattleList(battleTempList2);

					warList.get(WAR_COUNTER).setWarGoal(warGoal);
					warGoal = new WarGoal();

					warGoalProcessing = false;
					WARGOAL_COUNTER = 0;
					BATTLE_COUNTER = 0;
					countryList.clear();
					battleList.clear();
					WAR_COUNTER++;
					warProcessing = false;

				}
			}
		} while (((originalLine) = scanner.readLine()) != null);
		scanner.close();

		/* This part makes sure broken wars don't get processed */
		Predicate<War> filter = war->(war == null || war.getOriginalAttacker().equals("") || war.getOriginalDefender().equals(""));
		warList.removeIf(filter);

		/* Setting the start date and casus belli */
		warList.forEach(ee.tkasekamp.europawaranalyzer.core.War::setCasusBelliAndStartDate);
		return warList;

	}

	/**
	 * This processes the war. It creates a new war class and assigns values to it by reading new lines.
	 * War reading ends when the bracketCounter is 0 again.
	 *
	 * @param line
	 */
	public void warReader(String line) {
		if (line.startsWith("name") && (warList.get(WAR_COUNTER).getName().equals(""))) { // Name check required so it is not overwritten
			line = nameExtractor(line, 6, true);
			warList.get(WAR_COUNTER).setName(line);
		} else if (line.matches("([0-9]{1,4}\\.((1[0-2])|[0-9])\\.([1-3][0-9]|[0-9]))+=.*")) {
			line = line.split("=")[0];
			setDateBuffer(addZerosToDate(line));

		} else if (line.startsWith("add_attacker=")) {
			line = nameExtractor(line, 14, true);
			JoinedCountry country = new JoinedCountry(line, true, dateBuffer);
			countryList.add(country);
		} else if (line.startsWith("add_defender=")) {
			line = nameExtractor(line, 14, true);
			JoinedCountry country = new JoinedCountry(line, false, dateBuffer);
			countryList.add(country);
		} else if (line.startsWith("rem_attacker=")) {
			line = nameExtractor(line, 14, true);
			for (JoinedCountry item : countryList) {
				if (item.getTag().equals(line)) {
					item.setEndDate(dateBuffer);
				}
			}
		} else if (line.startsWith("rem_defender=")) {
			line = nameExtractor(line, 14, true);
			for (JoinedCountry item : countryList) {
				if (item.getTag().equals(line)) {
					item.setEndDate(dateBuffer);
					if (!warList.get(WAR_COUNTER).isActive()) { // If the war is not active, the date the last defender is removed will also be the war's enddate
						warList.get(WAR_COUNTER).setEndDate(dateBuffer);
					}
				}
			}
		} else if (line.startsWith("original_attacker=")) {
			line = nameExtractor(line, 19, true);
			/* Checking required for some older wars */
			if (!line.equals("---")) {
				warList.get(WAR_COUNTER).setOriginalAttacker(line);
			}

		} else if (line.startsWith("original_defender=")) {
			line = nameExtractor(line, 19, true);
			/* Checking required for some older wars */
			if (!line.equals("---")) {
				warList.get(WAR_COUNTER).setOriginalDefender(line);
			}

		} else if (line.startsWith("action")) {
			line = nameExtractor(line, 7, false);
			warList.get(WAR_COUNTER).setAction(addZerosToDate(line));
		} else if (line.startsWith("outcome")) {
			Result result = Result.UNKNOWN;
			line = nameExtractor(line, 8, false);
			switch(line) {
				case "3" : result = Result.LOST; break;
				case "2" : result = Result.WON; break;
				case "1" : result = Result.WHITE; break;
				default: result = Result.UNKNOWN;
			}
			warList.get(WAR_COUNTER).setResult(result);
		}


	}

	/**
	 * All data about the battle is sent here
	 * Since the attacker is always first, it is only required to check if the attacker already has the data
	 */
	public void battleReader(String line) {
		//Process the first part of the battle
		if (line.startsWith("name")) {
			line = nameExtractor(line, 6, true);
			Battle b = new Battle(dateBuffer, line);
			battleList.add(b);
		} else if (line.startsWith("location")) {
			line = nameExtractor(line, 9, false);
			int location = Integer.parseInt(line);
			battleList.get(BATTLE_COUNTER).setLocation(location);
		} else if (line.startsWith("result")) {
			line = nameExtractor(line, 7, false);
			if (line.equals("yes")) {
				battleList.get(BATTLE_COUNTER).setRes(Result.WON);
			} else {
				battleList.get(BATTLE_COUNTER).setRes(Result.LOST);
			}
		} else if(line.startsWith("attacker")) { //Check if it's about the attacker
			attackerDefender = true; // From now on all unit data will be about the attacker
		} else if(line.startsWith("defender")) { //Check if it's about the defender
			attackerDefender = false; // From now on all unit data will be about the defender
		} else if (line.startsWith("losses")) {
			line = nameExtractor(line, 7, false);
			int losses = Integer.parseInt(line);
			if (attackerDefender) {
				battleList.get(BATTLE_COUNTER).setAttackerLosses(losses);

			} else {
				battleList.get(BATTLE_COUNTER).setDefenderLosses(losses);
				battleList.get(BATTLE_COUNTER).setTotalLosses(losses + battleList.get(BATTLE_COUNTER).getAttackerLosses());
			}
		} else if (line.startsWith("country")) {
			line = nameExtractor(line, 9, true);
			if (attackerDefender) {
				battleList.get(BATTLE_COUNTER).setAttacker(line);
			} else {
				battleList.get(BATTLE_COUNTER).setDefender(line);
			}
		} else if (line.startsWith("commander")) {
			line = nameExtractor(line, 11, true);
			if (attackerDefender) {
				battleList.get(BATTLE_COUNTER).setLeaderAttacker(line);

				/* Housekeeping */
				Unit[] unitTempList = new Unit[unitList.size()];
				Unit[] unitTempList2 = unitList.toArray(unitTempList);
				battleList.get(BATTLE_COUNTER).setAttackerUnits(unitTempList2);
				unitList.clear();
				/* Battle type */
				battleList.get(BATTLE_COUNTER).determineType();

			} else {
				battleList.get(BATTLE_COUNTER).setLeaderDefender(line);


				Unit[] unitTempList = new Unit[unitList.size()];
				Unit[] unitTempList2 = unitList.toArray(unitTempList);
				battleList.get(BATTLE_COUNTER).setDefenderUnits(unitTempList2);
				unitList.clear();


				battleProcessing = false;
				BATTLE_COUNTER++;
			}
		} else if (!(line.equals("attacker=")) && !(line.startsWith("defender=")) && !(line.equals("{"))
				&& !(line.equals("}")) && !(line.equals("battle=")) && !(line.startsWith("war_goal"))) {
			/* All units such as "infantry=9000" will come here
			 * 
			 */

			try {
				String[] pieces = line.split("=");
				int losses = Integer.parseInt(pieces[1]);
				unitList.add(new Unit(pieces[0], losses));
			} catch (NumberFormatException e) {
				// Mainly debug if the lines which come here aren't integers
//				controller.getErrorLabel().setText(controller.getErrorLabel().getText() + "Problem with reading: " + line);

			}

		}
	}

	/**
	 * Very similar to battleReader in use
	 * If war_goal is read in, all data is sent here until the line receiver comes
	 */
	public void warGoalReader(String line) {
		/* Check required because the first line in war goal is not always the same */
		if (line.startsWith("war_goal")) {
			warGoal = new WarGoal();
		} else if (line.startsWith("province")) { // state_province_id
			line = nameExtractor(line, 9, false);
			int state = Integer.parseInt(line);
			warGoal.setState_province_id(state);
		} else if (line.startsWith("casus")) { //Moving Casus Belli Processing somewhere else
			line = nameExtractor(line, 13, true);
			warGoal.setCasus_belli(line);
		} else if (line.startsWith("country")) {
			line = nameExtractor(line, 9, true);
			warGoal.setCountry(line);
		} else if (line.startsWith("}")) {
			/* This is always the last line in a war goal 
			 * Clearing the wargoal list and passing it on to war like in battleReader*/
			WARGOAL_COUNTER++;
			warGoalProcessing = false;

		}
	}

	/**
	 * casusBelliReader
	 */
	public void casusBelliReader(String line) {
		if (line.startsWith("province")) { // state_province_id
			line = nameExtractor(line, 9, false);
			int state = Integer.parseInt(line);
			warGoal.setState_province_id(state);
		}else if (line.startsWith("tag")) {
			line = nameExtractor(line, 5, true);
			warGoal.setCountry(line);
		} else if (line.startsWith("casus_belli")) {
			/* This is always the last line in a war goal 
			 * Clearing the wargoal list and passing it on to war like in battleReader*/
			line = nameExtractor(line, 13, true);
			warGoal.setCasus_belli(line);
		} else if (line.startsWith("type")) {
			/* This is always the last line in a war goal
			 * Clearing the wargoal list and passing it on to war like in battleReader*/
			line = nameExtractor(line, 6, true);
			if(warGoal.getCasus_belli().equals("")) {
				warGoal.setCasus_belli(line);
			}
			warGoal.setType(line);
		} else if (line.startsWith("}")) {
			/* This means there is no more data to be added */
			casusBelliProcessing = false;

		}
	}

	/** Used when reading a dynamic country */
	public void dynamicCountryReader(String line, Country dynamicCountry) {
		if (line.startsWith("name=")) {
			String countryName = nameExtractor(line, 6, true);
			dynamicCountry.setOfficialName(countryName);
			dynamicCountryProcessing = false;
		}
	}

	public void bracketCounterChange(String line) {
		// Increases or decreases the bracketCounter
		if (line.contains("{")) {
			bracketCounter++;
		}
		if (line.contains("}")) {
			bracketCounter--;
		}
	}

	public String getDateBuffer() {
		return dateBuffer;
	}

	public void setDateBuffer(String dateBuffer) {
		this.dateBuffer = dateBuffer;
	}

}
