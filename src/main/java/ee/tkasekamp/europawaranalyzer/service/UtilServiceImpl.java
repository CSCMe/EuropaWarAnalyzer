package ee.tkasekamp.europawaranalyzer.service;

import ee.tkasekamp.europawaranalyzer.util.FolderHandler;
import javafx.scene.image.Image;

import java.io.IOException;

public class UtilServiceImpl implements UtilService {
	public static final String FLAGPATH = "/flags/";
	// Have to be static so they can be saved at the end
	private String saveGameFolder = "";
	private String installFolder = "";
	private String modFolder = "";

	@Override
	public void guessFolders() throws IOException {
		String[] folders = FolderHandler.getFolders();
		saveGameFolder = folders[0];
		installFolder = folders[1];

	}

	@Override
	public void writePathsToFile() throws IOException {
		FolderHandler.savePaths(saveGameFolder, installFolder);

	}

	@Override
	public String getSaveGameFolder() {
		return saveGameFolder;
	}

	@Override
	public String getInstallFolder() {
		return installFolder;
	}

	@Override
	public String getModFolder() { return modFolder; }

	@Override
	public void reset() {

	}

	@Override
	public void setSaveGameFolder(String pathToFolder) {
		this.saveGameFolder = pathToFolder;
	}

	@Override
	public void setInstallFolder(String pathToFolder) {
		this.installFolder = pathToFolder;
	}

	@Override
	public void setFolderPaths(String saveFolder, String installFolder, String modFolder) {
		this.installFolder = installFolder.replace("\\", "/");
		this.saveGameFolder = FolderHandler.getDirectoryOnly(saveFolder.replace("\\", "/"));
		this.modFolder = modFolder.replace("\\", "/");
	}

	@Override
	public Image loadFlag(String tag) {
		try {
			return new Image(this.getClass().getResourceAsStream(FLAGPATH + tag + ".PNG"));
		} catch (NullPointerException e) {
			return null;
		}
	}

}
