package ee.tkasekamp.europawaranalyzer.core;

import java.util.Locale;

/**
 * Everything from infantry to ships
 */
public enum Unit {
	INFANTRY ("Infantry", Type.LAND),
	CAVALRY ("Cavalry", Type.LAND),
	ARTILLERY ("Artillery", Type.LAND),
	HEAVY_SHIP ("Heavy Ship", Type.NAVAL),
	LIGHT_SHIP ("Light Ship", Type.NAVAL),
	GALLEY ("Galley", Type.NAVAL),
	TRANSPORT ("Transport", Type.NAVAL);

	private Type type;
	private String name;
	private int number;


	private Unit(String name, Type type) {
		this.name = name;
		this.type = type;
		this.number = 0;
	}

	@Override
	public String toString() {
		return "Unit [type="+ this.getType() + ", name=" + this.getName() + ", number=" + number + "]";
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public static Unit createUnit(String name, int number) {
		Unit unit = Unit.valueOf(name.toUpperCase(Locale.ROOT));
		unit.setNumber(number);
		return unit;
	}
}
