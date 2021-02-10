package ee.tkasekamp.europawaranalyzer.controller.tab;

import ee.tkasekamp.europawaranalyzer.controller.MainController;
import ee.tkasekamp.europawaranalyzer.controller.box.WarCountryBox;
import ee.tkasekamp.europawaranalyzer.core.Battle;
import ee.tkasekamp.europawaranalyzer.core.Battle.Result;
import ee.tkasekamp.europawaranalyzer.core.Battle.Type;
import ee.tkasekamp.europawaranalyzer.core.War;
import ee.tkasekamp.europawaranalyzer.service.ModelService;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class WarDetailsController extends AbstractController {

	@FXML
	private TableView<Battle> battleTable;

	@FXML
	private TableColumn<Battle, String> colNameBattle;

	@FXML
	private TableColumn<Battle, String> colDateBattle;

	@FXML
	private TableColumn<Battle, String> colAttackerBattle;

	@FXML
	private TableColumn<Battle, String> colDefenderBattle;

	@FXML
	private TableColumn<Battle, Type> colTypeBattle;

	@FXML
	private TableColumn<Battle, Result> colBattleResult;

	@FXML
	private TableColumn<Battle, Integer> colBattleTotalLosses;

	@FXML
	private Label warNameLabel;

	@FXML
	private Label warStartDateLabel;

	@FXML
	private Label warEndDateLabel;

	@FXML
	private Label warTotalLossesLabel;

	@FXML
	private Label warTotalShipLossesLabel;

	@FXML
	private Label warActionLabel;

	@FXML
	private Label warHasEndedLabel;

	@FXML
	private Label warGoalActorLabel;

	@FXML
	private Label warGoalReceiverLabel;

	@FXML
	private Label warGoalCBLabel;

	@FXML
	private Label warGoalCountryLabel;

	@FXML
	private Label warGoalScoreHelper;

	@FXML
	private Label warGoalScoreLabel;

	@FXML
	private Label warGoalChangeHelper;

	@FXML
	private Label warGoalChangeLabel;

	@FXML
	private Label warGoalDateHelper;

	@FXML
	private Label warGoalDateLabel;

	@FXML
	private Label warGoalFulfilledHelper;

	@FXML
	private Label warGoalFulfilledLabel;

	@FXML
	private Label warGoalStateLabel;

	@FXML
	private WarCountryBox attackerBoxController;
	@FXML
	private WarCountryBox defenderBoxController;

	private ObservableList<Battle> battleTableContent;
	private MainController main;
	private Tab tab;
	private ModelService modelService;

	public void init(MainController mainController, ModelService modelService,
					 Tab tab) {
		main = mainController;
		this.tab = tab;
		this.modelService = modelService;
		setColumnValues();
		attackerBoxController.init(modelService, "Attacker");
		defenderBoxController.init(modelService, "Defender");
		battleTableContent = FXCollections.observableArrayList();

		/* Listening to selections in battleTable */
		ObservableList<Battle> battleTableSelection = battleTable
				.getSelectionModel().getSelectedItems();
		battleTableSelection.addListener(battleTableSelectionChanged);
	}

	@Override
	public void reset() {
		tab.setDisable(true);
		tab.setText("War");

	}

	public void populate(War war) {
		/* Set the name of the tab */
		tab.setText(war.getName());
		tab.setDisable(false);
		/* Basic information about the war */
		warNameLabel.setText(war.getName());
		warStartDateLabel.setText(war.getStartDate());
		warEndDateLabel.setText(war.getEndDate());
		warActionLabel.setText(war.getAction());
		if (war.isActive()) {
			warHasEndedLabel.setText("No");
		} else {
			warHasEndedLabel.setText("Yes");
		}

		attackerBoxController.populate(war);
		defenderBoxController.populate(war);
		battleTablePopulate(war);
		lossesPopulate(war);
		originalWarGoalPopulate(war);
	}

	private void setColumnValues() {
		colNameBattle
				.setCellValueFactory(new PropertyValueFactory<>(
						"name"));
		colDateBattle
				.setCellValueFactory(new PropertyValueFactory<>(
						"date"));
		colDefenderBattle
				.setCellValueFactory(new PropertyValueFactory<>(
						"defender"));

		colDefenderBattle
				.setCellFactory(column -> getCell());
		colAttackerBattle
				.setCellValueFactory(new PropertyValueFactory<>(
						"attacker"));
		colAttackerBattle
				.setCellFactory(column -> getCell());
		colTypeBattle
				.setCellValueFactory(new PropertyValueFactory<>(
						"battleType"));
		colBattleResult
				.setCellValueFactory(new PropertyValueFactory<>(
						"res"));
		colBattleTotalLosses
				.setCellValueFactory(new PropertyValueFactory<>(
						"totalLosses"));

	}

	private TableCell<Battle, String> getCell() {
		return new TableCell<Battle, String>() {

			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				setText(modelService.getOfficialName(item));
			}
		};
	}

	/**
	 * Sets all labels at the attackers side except losses
	 */
	private void battleTablePopulate(War war) {
		battleTableContent.clear();

		/* Adding battles to list */
		battleTableContent.addAll(war.getBattleList());

		/* Displaying battles in table */
		battleTable.setItems(battleTableContent);

	}

	/**
	 * Sets labels for losses
	 */
	private void lossesPopulate(War war) {
		int [] losses = war.getLosses();

		/* Attacker losses */
		attackerBoxController.setTotalLosses(losses[0],
				losses[1]);
		/* Defender losses */
		defenderBoxController.setTotalLosses(losses[2],
				losses[3]);
		/* Total losses */
		warTotalLossesLabel.setText(Integer.toString(losses[0]
				+ losses[2]));
		warTotalShipLossesLabel.setText(Integer
				.toString(losses[1] + losses[3]));

	}

	/**
	 * Gives values to the labels related to wargoals
	 */
	private void originalWarGoalPopulate(War war) {

		/* True if the version is HoD. Otherwise some labels will be hidden */
		if (modelService.isHOD()) {
			/*
			 * Checking if this particular war has an original wargoal.
			 * Otherwise the first wargoal in the wargoalList will be used
			 */
			if (!(war.getOriginalWarGoal().getActor().equals(""))) {
				warGoalActorLabel.setText(modelService.getOfficialName(war
						.getOriginalWarGoal().getActor()));
				warGoalReceiverLabel.setText(modelService.getOfficialName(war
						.getOriginalWarGoal().getReceiver()));
				warGoalCBLabel.setText(war.getOriginalWarGoal()
						.getCasus_belli());
				warGoalCountryLabel.setText(modelService.getOfficialName(war
						.getOriginalWarGoal().getCountry()));
				warGoalStateLabel.setText(Integer.toString(war
						.getOriginalWarGoal().getState_province_id()));
				warGoalDateLabel.setText(war.getOriginalWarGoal().getDate());
				warGoalScoreLabel.setText(Double.toString(war
						.getOriginalWarGoal().getScore()));
				warGoalChangeLabel.setText(Double.toString(war
						.getOriginalWarGoal().getChange()));
				warGoalFulfilledLabel.setText(war.getOriginalWarGoal()
						.getFulfilled().toString());
			} else {
				warGoalActorLabel.setText(modelService.getOfficialName(war
						.getWarGoalList()[0].getActor()));
				warGoalReceiverLabel.setText(modelService.getOfficialName(war
						.getWarGoalList()[0].getReceiver()));
				warGoalCBLabel
						.setText(war.getWarGoalList()[0].getCasus_belli());
				warGoalCountryLabel.setText(modelService.getOfficialName(war
						.getWarGoalList()[0].getCountry()));
				warGoalStateLabel
						.setText(Integer.toString(war.getWarGoalList()[0]
								.getState_province_id()));
				warGoalDateLabel.setText(war.getWarGoalList()[0].getDate());
				warGoalScoreLabel
						.setText(Double.toString(war.getWarGoalList()[0]
								.getScore()));
				warGoalChangeLabel
						.setText(Double.toString(war.getWarGoalList()[0]
								.getChange()));
				warGoalFulfilledLabel.setText(war.getWarGoalList()[0]
						.getFulfilled().toString());
			}
			/* Showing the disabled labels */
			warGoalDateLabel.setVisible(true);
			warGoalDateHelper.setVisible(true);
			warGoalScoreLabel.setVisible(true);
			warGoalScoreHelper.setVisible(true);
			warGoalChangeLabel.setVisible(true);
			warGoalChangeHelper.setVisible(true);
			warGoalFulfilledLabel.setVisible(true);
			warGoalFulfilledHelper.setVisible(true);
		} else {
			try {
				warGoalActorLabel.setText(modelService.getOfficialName(war
						.getWarGoalList()[0].getActor()));
				warGoalReceiverLabel.setText(modelService.getOfficialName(war
						.getWarGoalList()[0].getReceiver()));
				warGoalCBLabel
						.setText(war.getWarGoalList()[0].getCasus_belli());
				warGoalCountryLabel.setText(war.getWarGoalList()[0]
						.getCountry());
				warGoalStateLabel
						.setText(Integer.toString(war.getWarGoalList()[0]
								.getState_province_id()));
			} catch (IndexOutOfBoundsException e) {
				/* Setting values to blank as some wars don't have any wargoals */
				warGoalActorLabel.setText("");
				warGoalReceiverLabel.setText("");
				warGoalCBLabel.setText("");
				warGoalCountryLabel.setText("");
				warGoalStateLabel.setText("");
			}
			/* Disabling unused labels */
			warGoalDateLabel.setVisible(false);
			warGoalDateHelper.setVisible(false);
			warGoalScoreLabel.setVisible(false);
			warGoalScoreHelper.setVisible(false);
			warGoalChangeLabel.setVisible(false);
			warGoalChangeHelper.setVisible(false);
			warGoalFulfilledLabel.setVisible(false);
			warGoalFulfilledHelper.setVisible(false);
		}
	}

	/**
	 * Battle table selection listener in the war details tab
	 */
	private final ListChangeListener<Battle> battleTableSelectionChanged = new ListChangeListener<Battle>() {
		@Override
		public void onChanged(Change<? extends Battle> c) {
			if (!battleTable.getSelectionModel().getSelectedItems().isEmpty()) {
				main.populateBattleTab((Battle) battleTable.getSelectionModel()
						.getSelectedItems().toArray()[0]);
			}
		}
	};

}
