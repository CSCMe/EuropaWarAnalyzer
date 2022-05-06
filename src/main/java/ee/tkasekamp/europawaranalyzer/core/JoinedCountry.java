package ee.tkasekamp.europawaranalyzer.core;

import ee.tkasekamp.europawaranalyzer.util.Constants;

/**
 * Class for storing "add_attacker", "add_defender", "rem_attacker", "rem_defender" data
 */
public class JoinedCountry {
	private String tag = "";
	private boolean isAttacker; // True is attacker, false is defender
	private String startDate = "";
	private String endDate = "";
	private Unit[] lostUnits;


	public JoinedCountry(String tag, boolean isAttacker, String startDate) {
		super();
		this.tag = tag;
		this.isAttacker = isAttacker;
		this.startDate = startDate;
	}


	@Override
	public String toString() {
		return "JoinedCountry [tag=" + tag + ", joinType=" + isAttacker
				+ ", startDate=" + startDate + ", endDate=" + endDate + "]";
	}


	public String getTag() {
		return tag;
	}


	public void setTag(String tag) {
		this.tag = tag;
	}


	public boolean isAttacker() {
		return isAttacker;
	}


	public void setAttacker(boolean attacker) {
		this.isAttacker = attacker;
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

	public Unit[] getLostUnits() {
		return lostUnits;
	}

	public void setLostUnits(Unit[] lostUnits) {
		this.lostUnits = lostUnits;
	}

	public long getLandLosses() {
		long total = 0;
		for (int i = 0; i < Constants.LAND_UNITS.length * 3; i++) {
			total += lostUnits[i].getNumber();
		}
		return total;
	}

	public long getNavalLosses() {
		long total = 0;
		for (int i = Constants.LAND_UNITS.length * 3; i < (Constants.NAVAL_UNITS.length + Constants.LAND_UNITS.length)* 3; i++) {
			total += lostUnits[i].getNumber();
		}
		return total;
	}

}
