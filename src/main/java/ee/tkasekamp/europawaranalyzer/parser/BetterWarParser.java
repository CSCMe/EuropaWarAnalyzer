package ee.tkasekamp.europawaranalyzer.parser;

import ee.tkasekamp.europawaranalyzer.core.*;
import ee.tkasekamp.europawaranalyzer.util.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class BetterWarParser implements Callable<War> {
    private final ArrayList<String> lines;

    /* Relevant for the parser */
    private int curlyBalance;
    /* Relevant for the war */
    private final ArrayList<Battle> battleList;
    private final HashMap<String, JoinedCountry> joinedCountryMap;
    private final War war;

    public BetterWarParser(ArrayList<String> lines) {
        this.lines = lines;
        war = new War(lines.get(0).contains("active"));
        war.setWarGoal(new WarGoal());
        battleList = new ArrayList<>();
        joinedCountryMap = new HashMap<>();
    }

    @Override
    public War call() throws Exception {

        /*
        Useful Methods: Parser.getName();, Parser.nameExtractor()
         */
        BufferedReader reader = new BufferedReader(new StringReader(String.join("\n",lines)));
        String originalLine = "";
        while ((originalLine = reader.readLine()) != null) {
            String line = originalLine.replaceAll("\t","");
            curlyBalance(line);
            String[] parts = line.split("=");
            if (curlyBalance > 1) {
                // history, participant casualties, war goal
                switch (parts[0]) {
                    case "participants":
                        participantParser(reader);
                        break;
                    case "history":
                        historyParser(reader);
                        war.setCountryList(joinedCountryMap.values().toArray(new JoinedCountry[0]));
                        break;
                    case "type":
                        WarGoal goal = warGoalParser(reader);
                        goal.setType(parts[1]);
                        war.setWarGoal(goal);
                        break;
                }
            } else if (curlyBalance == 1) {
                switch (parts[0]) {
                    case "name":
                        war.setName(quoteExtractor(parts[1]));
                        break;
                    case "original_attacker":
                        war.setOriginalAttacker(quoteExtractor(parts[1]));
                        break;
                    case "original_defender":
                        war.setOriginalDefender(quoteExtractor(parts[1]));
                        break;
                    case "action":
                        war.setAction(Parser.addZerosToDate(parts[1]));
                        break;
                    case "outcome":
                        war.setResult(Result.getResultFromNumber(parts[1]));
                        break;

                }
            } else {
                break;
            }
        }
        if (war.getOriginalAttacker().equals("---")) {
            return null;
        }
        war.setBattleList(battleList.toArray(new Battle[0]));
        war.setCasusBelliAndStartDate();
        return war;
    }

    private void participantParser(BufferedReader reader) throws IOException {
        String originalLine = "";
        String dateBuffer = "";
        JoinedCountry country = new JoinedCountry("---",false, "");
        double participationScore = 0;
        /* Order is important for this expression */
        while (curlyBalance > 1 && (originalLine = reader.readLine()) != null) {
            String line = originalLine.replaceAll("\t", "");
            curlyBalance(line);
            String[] parts = line.split("=");
            switch (parts[0]) {
                case "value":
                    participationScore = Double.parseDouble(parts[1]);
                    break;
                case "tag":
                    country = joinedCountryMap.get(quoteExtractor(parts[1]));
                    country.setParticipationScore(participationScore);
                    break;
                case "members":
                    country.setLostUnits(parseParticipantLosses(reader.readLine().replaceAll("\t", "")));
                    return;
            }
        }
    }

    private Unit[] parseParticipantLosses(String line) {
        Unit[] units = new Unit[21]; //21 numbers, three per type
        String[] losses = line.split(" ");
        String[] unitTypes = Constants.ALL_UNITS;

        for (int i = 0; i < unitTypes.length; i++) {
            for (int k = 0; k < 3; k++) {
                units[i * 3 + k] = new Unit(unitTypes[i], Integer.parseInt(losses[i * 3 + k]));
            }
        }
        return units;
    }

    /**
     * Parses history part of the war
     * @param reader
     * @throws IOException
     */
    private void historyParser(BufferedReader reader) throws IOException {
        String originalLine = "";
        String dateBuffer = "";
        /* Order is important for this expression */
        while (curlyBalance > 1 && (originalLine = reader.readLine()) != null) {
            String line = originalLine.replaceAll("\t","");
            curlyBalance(line);
            String[] parts = line.split("=");
            if (parts[0].matches("([0-9]{1,4}\\.((1[0-2])|[0-9])\\.([1-3][0-9]|[0-9]))+")) {
                dateBuffer = Parser.addZerosToDate(parts[0]);
            }
            boolean isAttacker = true;
            String tag = "";
            switch (parts[0]) {
                case "battle":
                    battleList.add(battleParser(reader, dateBuffer));
                    break;
                case "add_defender":
                    isAttacker = false;
                case "add_attacker":
                    tag = quoteExtractor(parts[1]);
                    joinedCountryMap.put(tag, new JoinedCountry(tag, isAttacker, dateBuffer));
                    break;
                case "rem_defender":
                    isAttacker = false;
                case "rem_attacker":
                    tag = quoteExtractor(parts[1]);
                    JoinedCountry country = joinedCountryMap.getOrDefault(tag, new JoinedCountry(tag, isAttacker, "?"));
                    country.setEndDate(dateBuffer);
                    joinedCountryMap.putIfAbsent(tag, country);
                    break;
            }
        }
        if (!war.isActive()) {
            war.setEndDate(dateBuffer);
        }
    }

    private Battle battleParser(BufferedReader reader, String date) throws IOException {
        String originalLine = "";
        Battle battle = new Battle();
        boolean parsingAttacker = false;
        boolean parsingDefender = false;

        /* Order is important for this expression */
        while (curlyBalance > 2 && (originalLine = reader.readLine()) != null) {
            String line = originalLine.replaceAll("\t","");
            curlyBalance(line);
            String[] parts = line.split("=");
            if (parsingAttacker ) {
                switch (parts[0]) {
                    case "losses":
                        battle.setAttackerLosses(Integer.parseInt(parts[1]));
                        break;
                    case "country":
                        battle.setAttacker(quoteExtractor(parts[1]));
                        break;
                    case "commander":
                        battle.setLeaderAttacker(quoteExtractor(parts[1]));
                        parsingAttacker = false;
                        break;
                }
            } else if (parsingDefender) { //same as above but for defender, yes, this could be better
                switch (parts[0]) {
                    case "losses":
                        battle.setDefenderLosses(Integer.parseInt(parts[1]));
                        break;
                    case "country":
                        battle.setDefender(quoteExtractor(parts[1]));
                        break;
                    case "commander":
                        battle.setLeaderDefender(quoteExtractor(parts[1]));
                        parsingDefender = false;
                        break;
                }
            } else {
                switch (parts[0]) {
                    case "name":
                        battle.setName(quoteExtractor(parts[1]));
                        break;
                    case "location":
                        battle.setLocation(Integer.parseInt(parts[1]));
                        break;
                    case "result":
                        battle.setRes(Result.getResultFromYesNo(parts[1]));
                        break;
                    case "attacker":
                        battle.setAttackerUnits(unitParser(reader));
                        reader.reset();
                        parsingAttacker = true;
                        break;
                    case "defender":
                        battle.setDefenderUnits(unitParser(reader));
                        reader.reset();
                        parsingDefender = true;
                        break;
                }
            }
        }
        battle.setDate(date);
        battle.battleProcessing();
        return battle;
    }

    private Unit[] unitParser(BufferedReader reader) throws IOException {
        String originalLine = "";
        ArrayList<Unit> units = new ArrayList<>();
        while ((originalLine = reader.readLine()) != null) {
            String line = originalLine.replaceAll("\t","");
            String[] parts = line.split("=");
            if (parts[0].equals("losses")) {
                return units.toArray(new Unit[0]);
            } else {
                units.add(new Unit(parts[0],Integer.parseInt(parts[1])));
            }
            reader.mark(1000);
        }
        return units.toArray(new Unit[0]);
    }

    private WarGoal warGoalParser(BufferedReader reader) throws IOException {
        String originalLine = "";
        ArrayList<Unit> units = new ArrayList<>();
        WarGoal goal = new WarGoal();
        while (curlyBalance > 1 && (originalLine = reader.readLine()) != null) {
            String line = originalLine.replaceAll("\t","");
            curlyBalance(line);
            String[] parts = line.split("=");
            switch (parts[0]) {
                case "province":
                    goal.setState_province_id(Integer.parseInt(parts[1]));
                    break;
                case "tag":
                    goal.setCountry(quoteExtractor(parts[1]));
                    break;
                case "casus_belli":
                    goal.setCasus_belli(quoteExtractor(parts[1]));
                    break;
            }
        }
        return goal;
    }

    private String quoteExtractor(String line) {
        if (line.contains("\"")) {
            return line.split("\"").length > 1 ? line.split("\"")[1] : "";
        }
        System.out.println("Error, oh god: " + line);
        return "ERROR";
    }

    private void curlyBalance(String line) {
        switch(line.charAt(line.length() - 1)) {
            case '{':
                curlyBalance++;
                break;
            case '}':
                curlyBalance--;
            default:
                break;
        }
    }
}
