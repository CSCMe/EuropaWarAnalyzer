package ee.tkasekamp.europawaranalyzer.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import ee.tkasekamp.europawaranalyzer.core.Country;
import ee.tkasekamp.europawaranalyzer.core.War;
import javafx.scene.image.Image;

public interface ModelService {
	/**
	 * The main method of the analyzer. Used to parse the save game, create the
	 * model and read in all the other files.
	 *
	 * @param saveGamePath
	 * @param useLocalisation
	 * @param useMultithreading
	 * @return How the creation was handled
	 * @throws IOException
	 */
	public String createModel(String saveGamePath, boolean useLocalisation, boolean useMultithreading);

	public void setDate(String line);

	public String getDate();

	public void setPlayer(String line);

	public String getPlayer();

	public String getPlayerOfficial();

	public void setStartDate(String line);

	public String getStartDate();

	public void reset();

	/**
	 * Returns an official name for the given tag. If not found, the same tag is
	 * returned (but this is unlikely)
	 */
	public String getOfficialName(String tag);

	public Image getFlag(String tag);

	public Map<String, Country> getCountries();

	public ArrayList<War> getWars();


}
