package ee.tkasekamp.europawaranalyzer.controller.tab;


import ee.tkasekamp.europawaranalyzer.core.War;
import ee.tkasekamp.europawaranalyzer.core.WarGoal;
import ee.tkasekamp.europawaranalyzer.service.ModelService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class WargoalController extends AbstractController {

	/* War goal table */
	@FXML
	private TableView<WarGoal> warGoalTable;
	@FXML
	private TableColumn<WarGoal, String> colWarGoalType;
	@FXML
	private TableColumn<WarGoal, String> colWarGoalCasusBelli;
	@FXML
	private TableColumn<WarGoal, Integer> colWarGoalStateID;
	@FXML
	private TableColumn<WarGoal, String> colWarGoalCountry;

	private ObservableList<WarGoal> warGoalTableContent;

	@FXML
	private Label warName2;

	private Tab tab;
	private ModelService modelService;

	public void init(Tab tab,
					 ModelService modelService) {
		this.tab = tab;
		this.modelService = modelService;
		warGoalTableContent = FXCollections.observableArrayList();
		setWarGoalTabColumnValues();
	}

	@Override
	public void reset() {
		tab.setDisable(true);

	}

	public void populate(War war) {
		warName2.setText(war.getName());
		/* Show the tab */
		tab.setDisable(false);
		warGoalTableContent.clear(); // A bit of cleaning

		// Adding to list
		warGoalTableContent.addAll(war.getWarGoal());
		setColumnVisibility(war);
		warGoalTable.setItems(warGoalTableContent);

	}

	/**
	 * Sets the column values for wargoaltab.
	 * are shown. Otherwise they are hidden.
	 */
	private void setWarGoalTabColumnValues() {
		colWarGoalType
				.setCellValueFactory(new PropertyValueFactory<>(
						"type"));
		colWarGoalCasusBelli
				.setCellValueFactory(new PropertyValueFactory<>(
						"casus_belli"));
		colWarGoalCountry
				.setCellValueFactory(new PropertyValueFactory<>(
						"country"));
		colWarGoalStateID
				.setCellValueFactory(new PropertyValueFactory<>(
						"state_province_id"));
	}

	private void setColumnVisibility(War war) {
		if(war.getWarGoal().getState_province_id() == 0) {
			colWarGoalStateID.setVisible(false);
		}
		else {
			colWarGoalStateID.setVisible(true);
		}

		if(war.getWarGoal().getCountry().equals("")) {
			colWarGoalCountry.setVisible(false);
		}
		else {
			colWarGoalCountry.setVisible(true);
		}
	}
}
