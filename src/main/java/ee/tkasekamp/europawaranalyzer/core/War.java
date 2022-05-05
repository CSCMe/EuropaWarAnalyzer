package ee.tkasekamp.europawaranalyzer.core;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.Objects;

/**
 * War class. All critical info and a list about war events.
 */
public class War {

	private String name = "";
	private String originalAttacker = ""; // like EST
	private String attacker = "";
	private String originalDefender = "";
	private String defender = "";
	private String action = ""; // Date
	private boolean isActive;
	private Battle[] battleList;
	private JoinedCountry[] joinedCountryList;
	private WarGoal warGoal;
	private String startDate = ""; // Set after reading
	private String endDate = ""; // Set during reading
	private String casus_belli = ""; // Primary casus belli displayed in table. Set after reading
	private Result result = Result.UNKNOWN;

	public War() {
		super();
	}

	public War(boolean isActive) {
		super();
		this.isActive = isActive;
		if (isActive) this.result = Result.UNFINISHED;
	}

	public void setCasusBelliAndStartDate() {
		//Checks if its not an older war (that doesn't say shit about attackers or defenders for some reason
		if(joinedCountryList != null && joinedCountryList.length != 0) {
			// The first one in the list has the oldest startDate
			this.startDate = joinedCountryList[0].getStartDate();
			this.casus_belli = warGoal.getCasus_belli();
		}
	}

	/**
	 * Calculate the losses for a country in this war.
	 * Iterates through every battle and returns a sum of the total man and ship losses.
	 *
	 * @return <code>int[]{countryTotalLosses, countryTotalShipLosses}</code>
	 */
	public int[] getCountryLosses(JoinedCountry joinedCountry) {
		int countryTotalLosses = 0;
		int countryTotalShipLosses = 0;

		/* Goes through each battle in the war and calculates the total losses for the given country */

		for (Battle battle : battleList) { //for each battle
			if (battle.getAttacker().equals(joinedCountry.getTag())) { //checks if said country attacked in the battle
				if (battle.getBattleType() == Type.LAND) {
					countryTotalLosses += battle.getAttackerLosses();
				} else {
					countryTotalShipLosses += battle.getAttackerLosses();
				}
			}

			if (battle.getDefender().equals(joinedCountry.getTag())) { // (missing else?) checks if said country is a defender in the battle
				if (battle.getBattleType() == Type.LAND) {
					countryTotalLosses += battle.getDefenderLosses();
				} else {
					countryTotalShipLosses += battle.getDefenderLosses();
				}
			}
		}

		return new int[]{countryTotalLosses, countryTotalShipLosses};
	}

	/**
	 * Calculate the losses for this war.
	 * Iterates through every country and adds its losses to the attacking or defending side.
	 *
	 * @return <code>int[]{attackerTotalLosses, attackerTotalShipLosses, defenderTotalLosses,
	 * defenderTotalShipLosses}</code>
	 */
	public int[] getLosses() {

		int attackerTotalLosses = 0;
		int attackerTotalShipLosses = 0;
		int defenderTotalLosses = 0;
		int defenderTotalShipLosses = 0;

		/*
		Goes through each country that participated in the war and adds its losses to either the attacking
		or the defending side, depending on which side it was on
		*/
		for (JoinedCountry joinedCountry : joinedCountryList) {
			int[] countryLosses = getCountryLosses(joinedCountry);
			if (joinedCountry.isAttacker()) {
				attackerTotalLosses += countryLosses[0];
				attackerTotalShipLosses += countryLosses[1];
			} else {
				defenderTotalLosses += countryLosses[0];
				defenderTotalShipLosses += countryLosses[1];
			}
		}
		return new int[]{attackerTotalLosses, attackerTotalShipLosses, defenderTotalLosses,
				defenderTotalShipLosses};
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOriginalAttacker() {
		return originalAttacker;
	}

	public void setOriginalAttacker(String originalAttacker) {
		this.originalAttacker = originalAttacker;
	}

	public String getOriginalDefender() {
		return originalDefender;
	}

	public void setOriginalDefender(String originalDefender) {
		this.originalDefender = originalDefender;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Battle[] getBattleList() {
		return battleList;
	}

	public void setBattleList(Battle[] battleList) {
		this.battleList = battleList;
	}

	public JoinedCountry[] getCountryList() {
		return joinedCountryList;
	}

	public void setCountryList(JoinedCountry[] countryList) {
		this.joinedCountryList = countryList;
	}

	public WarGoal getWarGoal() {
		return warGoal;
	}

	public void setWarGoal(WarGoal warGoal) {
		this.warGoal = warGoal;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getCasus_belli() {
		return casus_belli;
	}

	public void setCasus_belli(String casus_belli) {
		this.casus_belli = casus_belli;
	}

	public String getAttacker() {
		return attacker;
	}

	public void setAttacker(String attacker) {
		this.attacker = attacker;
	}

	public String getDefender() {
		return defender;
	}

	public void setDefender(String defender) {
		this.defender = defender;
	}

	@Override
	public String toString() {
		return "War [name=" + name + ", originalAttacker=" + originalAttacker + ", attacker=" +
				attacker + ", originalDefender=" + originalDefender + ", defender=" + defender +
				", action=" + action + ", isActive=" + isActive + ", battleList=" +
				Arrays.toString(battleList) + ", joinedCountryList=" +
				Arrays.toString(joinedCountryList) + ", warGoal=" +
				warGoal.toString() + ", startDate=" + startDate + ", endDate=" + endDate +
				", casus_belli=" + casus_belli + "]";
	}


	public int getCasualties() {
		int[] casualties = this.getLosses();
		return casualties[0] + casualties[2];
	}

	public int getShipCasualties() {
		int[] lostUnits = this.getLosses();
		return lostUnits[1] + lostUnits[3];
	}

	public double getLength() {
		if (endDate.equals("")) return 0.00;
		LocalDate startDateDate = LocalDate.parse(startDate.replace(".", "-"));
		LocalDate endDateDate = LocalDate.parse(endDate.replace(".", "-"));
		Period interval = Period.between(startDateDate, endDateDate);
		return Math.round((interval.toTotalMonths() / 12.00 + interval.getDays() / 365.00) * 1000) / 1000.00;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public Result getResult() {
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		War war = (War) o;
		return isActive() == war.isActive()
				&& getCasualties() == war.getCasualties()
				&& getResult() == war.getResult()
				&& getStartDate().equals(war.getStartDate())
				&& getAttacker().equals(war.getAttacker())
				&& getDefender().equals(war.getDefender())
				&& getName().equals(war.getName())
				&& getCasus_belli().equals(war.getCasus_belli())
				&& getLength() ==  war.getLength();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getCasualties(), isActive(), getResult(), getStartDate(), getAttacker(), getDefender(), getName(), getCasus_belli(), getLength());
	}
}
