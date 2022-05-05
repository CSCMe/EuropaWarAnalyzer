package ee.tkasekamp.europawaranalyzer.util;

public class Constants {
	/* Reference list to determine the battleType */
	public final static String[] NAVAL_UNITS = {"heavy_ship", "light_ship", "galley", "transport"};
	public final static String[] LAND_UNITS = {"infantry", "cavalry", "artillery"};
	public final static String[] ALL_UNITS = {"infantry", "cavalry", "artillery", "heavy_ship", "light_ship", "galley", "transport"};
	public final static String[] CASUS_BELLI = {"superiority", "take_province", "take_capital", "take_core",
			"defend_capital", "take_colony", "take_border", "blockade_ports"};
}
