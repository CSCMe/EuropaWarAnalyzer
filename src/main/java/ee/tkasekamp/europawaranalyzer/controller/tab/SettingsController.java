package ee.tkasekamp.europawaranalyzer.controller.tab;

import ee.tkasekamp.europawaranalyzer.controller.MainController;
import ee.tkasekamp.europawaranalyzer.service.UtilService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;

public class SettingsController extends AbstractController {
	@FXML
	private ProgressIndicator progressIndicator;

	@FXML
	private TextField installTextField;

	@FXML
	private TextField modTextField;

	@FXML
	private Button directoryIssue;

	@FXML
	private Button modIssue;

	@FXML
	private TextField saveGameTextField;

	@FXML
	private Button saveGameIssue;

	@FXML
	private Button startIssue;

	@FXML
	private Label modLabel;

	@FXML
	private Label modDescription;

	@FXML
	private Label directoryLabel;

	@FXML
	private Label directoryDescription;

	@FXML
	private Label errorLabel;

	@FXML
	private CheckBox localisationCheck;

	@FXML
	private CheckBox modLocalisationCheck;
	@FXML
	private CheckBox multithreadingCheck;

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
		if (!(utilServ.getInstallFolder() == null || utilServ.getInstallFolder().equals(""))) {
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
	void modIssueFired(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Mod directory");
		/* Only if there is a path is it given to the chooser */
		if (!(utilServ.getModFolder() == null || utilServ.getModFolder().equals(""))) {
			chooser.setInitialDirectory(new File(utilServ.getModFolder()));
		}
		// Throws error when user cancels selection
		try {
			File file = chooser.showDialog(null);
			modTextField.setText(file.getPath());
		} catch (NullPointerException e) {
		}
	}

	@FXML
	void saveGameIssueFired(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("EU IV save game");
		//Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Save games", "*.eu4");
		fileChooser.getExtensionFilters().add(extFilter);
		/* Only if there is a path is it given to the filechooser */
		if (!(utilServ.getSaveGameFolder() == null || utilServ.getSaveGameFolder().equals(""))) {
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
	void localisationCheckIssueFired(ActionEvent event) {
		boolean selected = localisationCheck.isSelected();
		installTextField.setVisible(selected);
		directoryLabel.setVisible(selected);
		directoryIssue.setVisible(selected);
		directoryDescription.setVisible(selected);

		modLocalisationCheck.setVisible(selected);
		if (!selected) {
			modLocalisationCheck.setSelected(false);
			setModVisibility(false);
		}
	}

	@FXML
	void modLocalisationCheckIssueFired(ActionEvent event) {
		setModVisibility(modLocalisationCheck.isSelected());
	}

	@FXML
	void startIssueFired(ActionEvent event) {
		boolean useLocalisation = localisationCheck.isSelected();
		boolean useMultithreading = multithreadingCheck.isSelected();
		boolean useModLocalisation = modLocalisationCheck.isSelected();
		main.readSaveGame(saveGameTextField.getText(), useLocalisation, useModLocalisation,useMultithreading);
	}

	public void showAnalyzingProgress() {
		errorLabel.setText("Analyzing...");
		progressIndicator.setVisible(true);
	}

	public void stoppedAnalyzing(String message) {
		errorLabel.setText(message);
		progressIndicator.setVisible(false);
	}

	@Override
	public void reset() {
		errorLabel.setText("");
		progressIndicator.setVisible(false);
	}

	public void setFolderPaths() {
		utilServ.setFolderPaths(saveGameTextField.getText(), installTextField.getText(), modTextField.getText());
	}

	public void populate() {
		installTextField.setText(utilServ.getInstallFolder());
		saveGameTextField.setText(utilServ.getSaveGameFolder());
		modTextField.setText(utilServ.getModFolder());
		setModVisibility(modLocalisationCheck.isSelected());
	}

	private void setModVisibility(boolean selected) {
		modTextField.setVisible(selected);
		modLabel.setVisible(selected);
		modDescription.setVisible(selected);
		modIssue.setVisible(selected);
	}
}
