package ee.tkasekamp.europawaranalyzer.controller;

import ee.tkasekamp.europawaranalyzer.controller.tab.*;
import ee.tkasekamp.europawaranalyzer.core.Battle;
import ee.tkasekamp.europawaranalyzer.core.War;
import ee.tkasekamp.europawaranalyzer.service.ModelService;
import ee.tkasekamp.europawaranalyzer.service.ModelServiceImpl;
import ee.tkasekamp.europawaranalyzer.service.UtilService;
import ee.tkasekamp.europawaranalyzer.service.UtilServiceImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;

import java.io.IOException;

public class MainController {
	@FXML
	private BattleController battleController;
	@FXML
	private SettingsController settingsController;
	@FXML
	private WarDetailsController warDetailsController;
	@FXML
	private WargoalController wargoalController;
	@FXML
	private WarListController warListController;
	@FXML
	private Tab warListTab;
	@FXML
	private Tab warDetailsTab;
	@FXML
	private Tab battleTab;
	@FXML
	private Tab wargoalTab;

	private ModelService modelServ;
	private UtilService utilServ;

	@FXML
	public void initialize() {
		utilServ = new UtilServiceImpl();
		modelServ = new ModelServiceImpl(utilServ);
		try {
			utilServ.guessFolders();
		} catch (IOException e) {
			setErrorText(e.getMessage());
		}

		battleController.init(this, modelServ, battleTab);
		settingsController.init(this, utilServ);
		warDetailsController.init(this, modelServ, warDetailsTab);
		wargoalController.init(wargoalTab, modelServ);
		warListController.init(this, modelServ, warListTab);

	}

	public void setErrorText(String text) {
		settingsController.setErrorText(text);
	}

	public void readSaveGame(String saveGamePath, boolean useLocalisation, boolean useMultithreading) {
		reset();
		settingsController.setFolderPaths();
		String text = modelServ.createModel(saveGamePath, useLocalisation, useMultithreading);
		setErrorText(text);
		warListController.populate();
	}

	public void populateWarTab(War war) {
		warDetailsController.populate(war);
		populateWargoalTab(war);
		battleController.reset();
	}

	public void populateBattleTab(Battle battle) {
		battleController.populate(battle);
	}

	public void populateWargoalTab(War war) {
		wargoalController.populate(war);
	}

	private void reset() {
		battleController.reset();
		settingsController.reset();
		warDetailsController.reset();
		wargoalController.reset();
		warListController.reset();

		modelServ.reset();
		utilServ.reset();
	}
}
