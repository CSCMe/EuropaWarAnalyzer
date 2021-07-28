package ee.tkasekamp.europawaranalyzer.parser;

import ee.tkasekamp.europawaranalyzer.core.*;
import ee.tkasekamp.europawaranalyzer.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;

public class WarParser implements Runnable{
    private ArrayList<String> lines;
    private ThreadedParser parser;

    private String dateBuffer = ""; // Stores the last date for JoinedCountry or Battle dates
    private ArrayList<JoinedCountry> countryList = new ArrayList<>();  // Stores temporarily to give to war
    private ArrayList<Battle> battleList = new ArrayList<>();  // Stores temporarily to give to war
    private int BATTLE_COUNTER = 0; // Used to change the current battle in battleList
    private WarGoal warGoal = new WarGoal();
    private boolean battleProcessing = false;
    private boolean warGoalProcessing = false;
    private boolean casusBelliProcessing = false;

    private boolean attacker; // True is attacker, false is defender. Changed while reading a battle as the first units are attackers, the rest defenders
    /* Battle details */
    private ArrayList<Unit> unitList = new ArrayList<>();
    
    
    private War war;
    
    WarParser(ArrayList<String> lines, ThreadedParser parser) {
        this.lines = lines;
        this.parser = parser;
    }
    @Override
    public void run() {
        war = new War(lines.get(0).contains("active"));
        for (String actualLine : lines) {
            String line = actualLine.replaceAll("\t", "");
             if (battleProcessing || line.startsWith("battle=")) {
                battleProcessing = true;
                battleReader(line);
            } else if (warGoalProcessing || line.startsWith("war_goal")) { //War goals are only set for OLD wars (wars before game start)
                warGoalProcessing = true;
                warGoalReader(line);

            } else if (casusBelliProcessing || Arrays.stream(Constants.CASUS_BELLI).anyMatch(line::startsWith)) {
                casusBelliProcessing = true;
                casusBelliReader(line);

            } else if(line.contains("income")) {
                break;
            } else {
                warReader(line);
            }
        }
        JoinedCountry[] countryTempList = new JoinedCountry[countryList.size()];
        JoinedCountry[] countryTempList2 = countryList.toArray(countryTempList);
        war.setCountryList(countryTempList2);

        Battle[] battleTempList = new Battle[battleList.size()];
        Battle[] battleTempList2 = battleList.toArray(battleTempList);
        war.setBattleList(battleTempList2);

        war.setWarGoal(warGoal);
        warGoal = new WarGoal();
        warGoalProcessing = false;
        BATTLE_COUNTER = 0;
        countryList.clear();
        battleList.clear();
        war.setCasusBelliAndStartDate();
        parser.addWar(war);
    }

    /**
     * This processes the war. It creates a new war class and assigns values to it by reading new lines.
     * War reading ends when the bracketCounter is 0 again.
     *
     * @param line
     */
    private void warReader(String line) {
        if (line.startsWith("name") && (war.getName().equals(""))) { // Name check required so it is not overwritten
            line = parser.nameExtractor(line, 6, true);
            war.setName(line);
        } else if (line.matches("([0-9]{1,4}\\.((1[0-2])|[0-9])\\.([1-3][0-9]|[0-9]))+=.*")) {
            line = line.split("=")[0];
            dateBuffer = parser.addZerosToDate(line);

        } else if (line.startsWith("add_attacker=")) {
            line = parser.nameExtractor(line, 14, true);
            JoinedCountry country = new JoinedCountry(line, true, dateBuffer);
            countryList.add(country);
        } else if (line.startsWith("add_defender=")) {
            line = parser.nameExtractor(line, 14, true);
            JoinedCountry country = new JoinedCountry(line, false, dateBuffer);
            countryList.add(country);
        } else if (line.startsWith("rem_attacker=")) {
            line = parser.nameExtractor(line, 14, true);
            for (JoinedCountry item : countryList) {
                if (item.getTag().equals(line)) {
                    item.setEndDate(dateBuffer);
                }
            }
        } else if (line.startsWith("rem_defender=")) {
            line = parser.nameExtractor(line, 14, true);
            for (JoinedCountry item : countryList) {
                if (item.getTag().equals(line)) {
                    item.setEndDate(dateBuffer);
                    if (!war.isActive()) { // If the war is not active, the date the last defender is removed will also be the war's enddate
                        war.setEndDate(dateBuffer);
                    }
                }
            }
        } else if (line.startsWith("attacker=")) {
            /* Checking if it's empty so only the first one is the attacker */
            if (war.getAttacker().equals("")) {
                line = parser.nameExtractor(line, 10, true);
                war.setAttacker(line);
            }
        } else if (line.startsWith("defender=")) {
            /* Checking if it's empty so only the first one is the defender */
            if (war.getDefender().equals("")) {
                line = parser.nameExtractor(line, 10, true);
                war.setDefender(line);
            }
        } else if (line.startsWith("original_attacker=")) {
            line = parser.nameExtractor(line, 19, true);
            /* Checking required for some older wars */
            if (!line.equals("---")) {
                war.setOriginalAttacker(line);
            }

        } else if (line.startsWith("original_defender=")) {
            line = parser.nameExtractor(line, 19, true);
            /* Checking required for some older wars */
            if (!line.equals("---")) {
                war.setOriginalDefender(line);
            }

        } else if (line.startsWith("action")) {
            line = parser.nameExtractor(line, 7, false);
            war.setAction(parser.addZerosToDate(line));
        } else if (line.startsWith("outcome")) {
            Result result = Result.UNKNOWN;
            line = parser.nameExtractor(line, 8, false);
            switch(line) {
                case "3" : result = Result.LOST; break;
                case "2" : result = Result.WON; break;
                case "1" : result = Result.WHITE; break;
                default: result = Result.UNKNOWN;
            }
            war.setResult(result);
        }


    }

    /**
     * All data about the battle is sent here
     * Since the attacker is always first, it is only required to check if the attacker already has the data
     */
    private void battleReader(String line) {
        //Process the first part of the battle
        if (line.startsWith("name")) {
            line = parser.nameExtractor(line, 6, true);
            Battle b = new Battle(dateBuffer, line);
            battleList.add(b);
        } else if (line.startsWith("location")) {
            line = parser.nameExtractor(line, 9, false);
            int location = Integer.parseInt(line);
            battleList.get(BATTLE_COUNTER).setLocation(location);
        } else if (line.startsWith("result")) {
            line = parser.nameExtractor(line, 7, false);
            if (line.equals("yes")) {
                battleList.get(BATTLE_COUNTER).setRes(Result.WON);
            } else {
                battleList.get(BATTLE_COUNTER).setRes(Result.LOST);
            }
        } else if(line.startsWith("attacker")) { //Check if it's about the attacker
            attacker = true; // From now on all unit data will be about the attacker
        } else if(line.startsWith("defender")) { //Check if it's about the defender
            attacker = false; // From now on all unit data will be about the defender
        } else if (line.startsWith("losses")) {
            line = parser.nameExtractor(line, 7, false);
            int losses = Integer.parseInt(line);
            if (attacker) {
                battleList.get(BATTLE_COUNTER).setAttackerLosses(losses);

            } else {
                battleList.get(BATTLE_COUNTER).setDefenderLosses(losses);
                battleList.get(BATTLE_COUNTER).setTotalLosses(losses + battleList.get(BATTLE_COUNTER).getAttackerLosses());
            }
        } else if (line.startsWith("country")) {
            line = parser.nameExtractor(line, 9, true);
            if (attacker) {
                battleList.get(BATTLE_COUNTER).setAttacker(line);
            } else {
                battleList.get(BATTLE_COUNTER).setDefender(line);
            }
        } else if (line.startsWith("commander")) {
            line = parser.nameExtractor(line, 11, true);
            if (attacker) {
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
        } else if (Arrays.stream(Constants.LAND_UNITS).anyMatch(line::startsWith) || Arrays.stream(Constants.NAVAL_UNITS).anyMatch(line::startsWith)) {
            /* All units such as "infantry=9000" will come here
             *
             */

            try {
                String[] pieces = line.split("=");
                int losses = Integer.parseInt(pieces[1]);
                unitList.add(new Unit(pieces[0], losses));
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                // Mainly debug if the lines which come here aren't integers
				//controller.getErrorLabel().setText(controller.getErrorLabel().getText() + "Problem with reading: " + line);
                System.out.println(line);
            }

        }
    }

    /**
     * Very similar to battleReader in use
     * If war_goal is read in, all data is sent here until the line receiver comes
     */
    private void warGoalReader(String line) {
        /* Check required because the first line in war goal is not always the same */
        if (line.startsWith("war_goal")) {
            warGoal = new WarGoal();
        } else if (line.startsWith("province")) { // state_province_id
            line = parser.nameExtractor(line, 9, false);
            int state = Integer.parseInt(line);
            warGoal.setState_province_id(state);
        } else if (line.startsWith("casus")) { //Moving Casus Belli Processing somewhere else
            line = parser.nameExtractor(line, 13, true);
            warGoal.setCasus_belli(line);
        } else if (line.startsWith("country")) {
            line = parser.nameExtractor(line, 9, true);
            warGoal.setCountry(line);
        } else if (line.startsWith("}")) {
            /* This is always the last line in a war goal
             * Clearing the wargoal list and passing it on to war like in battleReader*/
            warGoalProcessing = false;

        }
    }

    /**
     * casusBelliReader
     */
    private void casusBelliReader(String line) {
        if (line.startsWith("province")) { // state_province_id
            line = parser.nameExtractor(line, 9, false);
            int state = Integer.parseInt(line);
            warGoal.setState_province_id(state);
        }else if (line.startsWith("tag")) {
            line = parser.nameExtractor(line, 5, true);
            warGoal.setCountry(line);
        } else if (line.startsWith("casus_belli")) {
            /* This is always the last line in a war goal
             * Clearing the wargoal list and passing it on to war like in battleReader*/
            line = parser.nameExtractor(line, 13, true);
            warGoal.setCasus_belli(line);
        } else if (line.startsWith("type")) {
            /* This is always the last line in a war goal
             * Clearing the wargoal list and passing it on to war like in battleReader*/
            line = parser.nameExtractor(line, 6, true);
            if(warGoal.getCasus_belli().equals("")) {
                warGoal.setCasus_belli(line);
            }
            warGoal.setType(line);
        } else if (line.startsWith("}")) {
            /* This means there is no more data to be added */
            casusBelliProcessing = false;

        }
    }
}
