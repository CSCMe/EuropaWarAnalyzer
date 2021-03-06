package ee.tkasekamp.europawaranalyzer.core;

public class WarGoal {
	/* Definitions are directly from save file */
	private int state_province_id = 0;
	private String casus_belli = "";
	private String country = "";
	private String type = "";

	public WarGoal(int state_province_id) {
		super();
		this.state_province_id = state_province_id;
	}

	public WarGoal() {
		super();
	}

	@Override
	public String toString() {
		return "WarGoal [state_province_id=" + state_province_id
				+ ", casus_belli=" + casus_belli + ", country=" + country
				+ "]";
	}

	public int getState_province_id() {
		return state_province_id;
	}

	public void setState_province_id(int state_province_id) {
		this.state_province_id = state_province_id;
	}

	public String getCasus_belli() {
		return casus_belli;
	}

	public void setCasus_belli(String casus_belli) {
		this.casus_belli = casus_belli;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

}
