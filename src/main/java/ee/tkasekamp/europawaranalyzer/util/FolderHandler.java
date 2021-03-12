package ee.tkasekamp.europawaranalyzer.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FolderHandler {
	public static final String PROGRAM_FILES_X86 = "C:/Program Files (x86)";
	public static final String PROGRAM_FILES = "C:/Program Files";
	public static final String STEAM = "Steam/steamapps/common";
	public static final String PARADOX_FOLDER = "Paradox Interactive";
	public static final String EUIV = "Europa Universalis IV";
	public static final String SLASH = "/";

	public static final String PATHS = "./paths.txt";

	/**
	 * Checking if the path file exists. If not, attempts to guess the default
	 * save game and install folders.
	 *
	 * @throws IOException
	 */
	public static String[] getFolders() throws IOException {
		String[] paths = new String[2];
		if ((new File(PATHS)).exists()) {
			paths = readPaths();
		} else {
			paths[0] = checkSaveGameFolder();
			paths[1] = checkInstallFolder();
		}
		return paths;
	}

	/**
	 * Gets the user of the system and constructs a default save game path. If
	 * it is not found, sets the path to "".
	 */
	private static String checkSaveGameFolder() {
		String user = System.getProperty("user.name");
		String saveGameFolder = "C:/Users/" + user
				+ "/Documents/Paradox Interactive/Europa Universalis IV/save games/";
		if (new File(saveGameFolder).exists())
			return saveGameFolder;
		else
			return "";

	}

	/**
	 * Checks several places where I think the game directory could be. Starting
	 * from the newest version.
	 */
	private static String checkInstallFolder() {
		/* Vanilla */
		if (new File(PROGRAM_FILES + SLASH + PARADOX_FOLDER + SLASH
				+ EUIV).exists()) {
			return PROGRAM_FILES + SLASH + PARADOX_FOLDER + SLASH + EUIV;
		} else if ((new File(PROGRAM_FILES + SLASH + STEAM + SLASH + EUIV))
				.exists()) {
			return PROGRAM_FILES + SLASH + STEAM + SLASH + EUIV;
		} else if ((new File(PROGRAM_FILES_X86 + SLASH + STEAM + SLASH
				+ EUIV)).exists()) {
			return PROGRAM_FILES_X86 + SLASH + STEAM + SLASH + EUIV;
		} else if ((new File(PROGRAM_FILES_X86 + SLASH + PARADOX_FOLDER + SLASH
				+ EUIV)).exists()) {
			return PROGRAM_FILES_X86 + SLASH + PARADOX_FOLDER + SLASH
					+ EUIV;
		} else {
			return "";
		}
	}

	/**
	 * Takes the path of the full path of the savegame and return the directory
	 * it was in
	 *
	 * @param path
	 */
	public static String getDirectoryOnly(String path) {
		StringBuilder line = new StringBuilder(path);
		int index = line.lastIndexOf("/");

		line.delete(index + 1, line.length());
		return line.toString();
	}

	/**
	 * This method saves the paths so the user does not have to choose the file
	 * every time.
	 *
	 * @throws IOException
	 */
	public static void savePaths(String saveGameFolder, String installFolder) throws IOException {
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("paths.txt"), StandardCharsets.UTF_8));
			out.write(saveGameFolder);
			out.write("\n");
			out.write(installFolder);
			out.close();
		} catch (IOException e) {
			throw new IOException("Could not save the paths.txt.");

		}

	}

	/**
	 * Reads file {@link Constants#PATHS}. First line is the SAVEGAMEPATH,
	 * second the INSTALLPATH
	 *
	 * @throws IOException
	 */
	private static String[] readPaths() throws IOException {
		String[] paths = new String[2];

		InputStreamReader reader = new InputStreamReader(new FileInputStream(
				PATHS), StandardCharsets.UTF_8);
		BufferedReader scanner = new BufferedReader(reader);

		String line;
		int counter = 0;
		while ((line = scanner.readLine()) != null) {
			if (counter == 0 | counter == 1)
				paths[counter] = line;

			counter++;

		}
		scanner.close();

		return paths;
	}

}
