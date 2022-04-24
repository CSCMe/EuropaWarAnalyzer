package ee.tkasekamp.europawaranalyzer.service;

import javafx.scene.image.Image;

import java.io.IOException;

public interface UtilService {
	/**
	 * Try to find the install and save game folders.
	 *
	 * @throws IOException
	 */
	public void guessFolders() throws IOException;

	public void writePathsToFile() throws IOException;

	public String getSaveGameFolder();

	public String getInstallFolder();

	public String getModFolder();

	public void setSaveGameFolder(String pathToFolder);

	public void setInstallFolder(String pathToFolder);

	public void setFolderPaths(String saveFolder, String installFolder, String modFolder);

	public void reset();

	public Image loadFlag(String tag);

}
