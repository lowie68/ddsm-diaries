
/**
 * <h3>Recording Diary Dialog</h3>
 * 
 * <p>This class is the JavaFX controller for the recording diary dialog (recording.fxml). This
 * dialog presents the recording diary to the user</p>
 * 
 * @author Stephen
 */
package org.stevie.ddsm.diaries.controllers;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;
import org.stevie.ddsm.diaries.domain.RecordingDiaryEntry;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

@Component
public final class RecordingDiaryController implements Initializable {

	/*
	 * year of diary
	 */
	private SimpleStringProperty diaryYear = new SimpleStringProperty();
	
	/*
	 * JavaFX Controls
	 */
	@FXML
	private Label diaryYearLabel;
	@FXML
	private TableView<RecordingDiaryEntry> diaryTableView;

	/**
	 * Initialise Method
	 * 
	 * Initialise dialog prior to display
	 * 
	 * @param location
	 * @param resource bundle
	 * 
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		diaryYearLabel.textProperty().bind(diaryYear);

		configureTableView();
	}

	/**
	 * Configure Table View Method
	 * 
	 * Create the columns to be displayed in the JavaFX TableView control 
	 * 
	 * @param none
	 * @return none
	 */
	@SuppressWarnings("unchecked") //suppress type safety warning
	private void configureTableView() {

		/*
		 * month column Jan-Dec
		 */
		TableColumn<RecordingDiaryEntry, String> monthColumn = new TableColumn<>("Month");
		monthColumn.setCellValueFactory(rde -> new SimpleStringProperty(rde.getValue().month().toString()));
	
		/*
		 * date column (date + day of week)
		 */
		TableColumn<RecordingDiaryEntry, String> dateColumn = new TableColumn<>("First Monday or Tuesday in Month");
		dateColumn.setCellValueFactory(rde -> {
			var date = rde.getValue().recordingDate();
			var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			var dateString = formatter.format(date);
			var resultString = "(" + date.getDayOfWeek().toString() + ") " + dateString;
			return new SimpleStringProperty(resultString);
		});	
		
		/*
		 * edition column
		 */
		TableColumn<RecordingDiaryEntry, Integer> editionColumn = new TableColumn<>("Edition");
		editionColumn.setCellValueFactory(rde -> new SimpleIntegerProperty(rde.getValue().edition()).asObject());

		/*
		 * team column
		 */
		TableColumn<RecordingDiaryEntry, String> teamColumn = new TableColumn<>("Team");
		teamColumn.setCellValueFactory(rde -> new SimpleStringProperty(rde.getValue().team().toString()));
		
		/*
		 * compiler column
		 */
		TableColumn<RecordingDiaryEntry, String> compilerColumn = new TableColumn<>("Compiler");
		compilerColumn.setCellValueFactory(rde -> new SimpleStringProperty(rde.getValue().compiler()));

		/*
		 * assistant compiler column
		 */
		TableColumn<RecordingDiaryEntry, String> assistantColumn = new TableColumn<>("Assistant Compiler");
		assistantColumn.setCellValueFactory(rde -> new SimpleStringProperty(rde.getValue().assistantCompiler()));

		diaryTableView.getColumns().clear();
		
		/*
		 * add columns to table view (causes type safety warning)
		 */
		diaryTableView.getColumns().addAll(monthColumn, dateColumn, editionColumn, teamColumn, compilerColumn, assistantColumn);
	}

	/**
	 * Set Diary Items Method
	 * 
	 * Create observable list for display in the table view
	 * 
	 * @param items domain
	 * @return none
	 */
	public void setDiaryItems(List<RecordingDiaryEntry> items) {
		Objects.requireNonNull(items);
		var diaryItems = FXCollections.observableArrayList(items);
		diaryTableView.setItems(diaryItems);
	}
	
	/**
	 * Set Diary Year Method
	 * 
	 * Set the diary year for display in the dialog header
	 * 
	 * @param year
	 * @return none
	 */
	public void setYear(int year) {
		diaryYear.set(Integer.toString(year));
	}

}
