package ee.tkasekamp.europawaranalyzer.controller.tab;

import ee.tkasekamp.europawaranalyzer.controller.MainController;
import ee.tkasekamp.europawaranalyzer.service.UtilService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;

public class SettingsController extends AbstractController {
	@FXML
	private TextField installTextField;

	@FXML
	private Button directoryIssue;

	@FXML
	private TextField saveGameTextField;

	@FXML
	private Button saveGameIssue;

	@FXML
	private Button startIssue;

	@FXML
	private Label errorLabel;

	@FXML
	private CheckBox localisationCheck;

	private MainController main;
	private UtilService utilServ;

	public void init(MainController mainController, UtilService utilServ) {
		main = mainController;
		this.utilServ = utilServ;
		populate();

	}

	public void setErrorText(String text) {
		errorLabel.setText(text);
	}

	@FXML
	void directoryIssueFired(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("EU IV directory");
		/* Only if there is a path is it given to the chooser */
		if (!utilServ.getInstallFolder().equals("")) {
			chooser.setInitialDirectory(new File(utilServ.getInstallFolder()));
		}
		// Throws error when user cancels selection
		try {
			File file = chooser.showDialog(null);
			installTextField.setText(file.getPath());
		} catch (NullPointerException e) {
		}
	}

	@FXML
	void saveGameIssueFired(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("EU IV save game");
		//Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Save games", "*.eu4", "gamestate");
		fileChooser.getExtensionFilters().add(extFilter);
		/* Only if there is a path is it given to the filechooser */
		if (!utilServ.getSaveGameFolder().equals("")) {
			fileChooser.setInitialDirectory(new File(utilServ.getSaveGameFolder()));
		}

		// Throws error when user cancels selection
		try {
			File file = fileChooser.showOpenDialog(null);
			saveGameTextField.setText(file.getPath());
		} catch (NullPointerException e) {
		}
	}

	@FXML
	void startIssueFired(ActionEvent event) {
		boolean useLocalisation = localisationCheck.isSelected();
		main.readSaveGame(saveGameTextField.getText(), useLocalisation);
	}

	@Override
	public void reset() {
		errorLabel.setText("");

	}

	public void setFolderPaths() {
		utilServ.setFolderPaths(saveGameTextField.getText(), installTextField.getText());
	}

	public void populate() {
		installTextField.setText(utilServ.getInstallFolder());
		saveGameTextField.setText(utilServ.getSaveGameFolder());

	}
}
