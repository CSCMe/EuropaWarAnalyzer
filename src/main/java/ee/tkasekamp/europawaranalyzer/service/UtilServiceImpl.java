package ee.tkasekamp.europawaranalyzer.service;

import ee.tkasekamp.europawaranalyzer.util.FolderHandler;
import javafx.scene.image.Image;

import java.io.IOException;
import java.util.ArrayList;

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
		modFolder = folders[2];
	}

	@Override
	public void writePathsToFile() throws IOException {
		FolderHandler.savePaths(new String[]{saveGameFolder, installFolder, modFolder});

	}

	@Override
	public String getSaveGameFolder() {
		return saveGameFolder;
	}

	@Override
	public String getInstallFolder() {
		return installFolder;
	}

	public String getModFolder() { return modFolder; }

	@Override
	public String getSteamModFolder() {
		if (getInstallFolder().contains("steamapps/common")) {
			return getSaveGameFolder().replace("common/Europa Universalis IV", "workshop/content/236850");
		} else {
			return "";
		}
	}

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
