package ee.tkasekamp.europawaranalyzer.controller.box;

import ee.tkasekamp.europawaranalyzer.core.Battle;
import ee.tkasekamp.europawaranalyzer.core.Unit;
import ee.tkasekamp.europawaranalyzer.service.ModelService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;

public class BattleCountryBox {
	@FXML
	private TableView<Unit> unitsTable;
	@FXML
	private TableColumn<Unit, String> colUnitType;
	@FXML
	private TableColumn<Unit, Integer> colUnitNumber;

	@FXML
	private Label losses;

	@FXML
	private Label armySize;

	@FXML
	private Label country;

	@FXML
	private ImageView flag;

	@FXML
	private Label leader;

	// Helper labels that describe side of the country
	@FXML
	private Label sideHelper;
	@FXML
	private Label leaderHelper;

	private ObservableList<Unit> unitsTableContent;

	private String side;
	private ModelService modelService;

	public void init(ModelService modelService, String side) {
		unitsTableContent = FXCollections.observableArrayList();
		this.side = side;
		this.modelService = modelService;
		setHelperLabels();
		setColumnValues();
	}

	public void populate(Battle battle) {
		unitsTableContent.clear();
		if (side.equals("Attacker"))
			populateBox(battle.getAttacker(), battle.getAttackerUnits(),
					battle.getAttackerLosses(), battle.getLeaderAttacker());

		else
			populateBox(battle.getDefender(), battle.getDefenderUnits(),
					battle.getDefenderLosses(), battle.getLeaderDefender());
	}

	private void populateBox(String sideTag, Unit[] units, int sideLosses, String sideLeader) {
		flag.setImage(modelService.getFlag(sideTag));

		country.setText(modelService.getOfficialName(sideTag));

		/* Army size and adding to table list */
		int size = Battle.getUnitSize(units);
		unitsTableContent.addAll(units);
		unitsTable.setItems(unitsTableContent);

		armySize.setText(Integer.toString(size));
		losses.setText(Integer.toString(sideLosses));
		leader.setText(sideLeader);
	}

	private void setHelperLabels() {
		sideHelper.setText(side + ":");
		leaderHelper.setText(side + " leader:");
	}

	private void setColumnValues() {
		/* Attacker side */
		colUnitType.setCellValueFactory(new PropertyValueFactory<>("type"));
		colUnitNumber.setCellValueFactory(new PropertyValueFactory<>("number"));

	}
}
