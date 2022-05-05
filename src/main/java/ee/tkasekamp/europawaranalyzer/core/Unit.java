package ee.tkasekamp.europawaranalyzer.core;

import ee.tkasekamp.europawaranalyzer.util.Constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

/**
 * Everything from infantry to ships
 * TODO: return to how it was before (Class)
 */
public class Unit {
	private enum UnitType {
		INFANTRY ("Infantry", Type.LAND),
		CAVALRY ("Cavalry", Type.LAND),
		ARTILLERY ("Artillery", Type.LAND),
		HEAVY_SHIP ("Heavy Ship", Type.NAVAL),
		LIGHT_SHIP ("Light Ship", Type.NAVAL),
		GALLEY ("Galley", Type.NAVAL),
		TRANSPORT ("Transport", Type.NAVAL);

		private final Type type;
		private final String name;

		private UnitType(String name, Type type) {
			this.type = type;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public Type getType() {
			return type;
		}
	}

	private long number;
	private final UnitType unitType;

	public Unit(String name, long number) {
		this.unitType = UnitType.valueOf(name.replace(" ", "_").toUpperCase());
		this.number = number;
	}

	@Override
	public String toString() {
		return "Unit [type="+ this.getType() + ", name=" + this.getName() + ", number=" + number + "]";
	}

	public Type getType() {
		return unitType.getType();
	}

	public String getName() {
		return unitType.getName();
	}

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}
}
