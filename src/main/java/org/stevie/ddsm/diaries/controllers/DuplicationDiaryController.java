/**
 * <h3>Duplication Diary Dialog</h3>
 * 
 * <p>This class is the JavaFX controller for the preparation and duplication dialog (duplication.fxml). This
 * dialog presents the duplication and preparation diary to the user</p>
 * 
 * @author Stephen
 */

package org.stevie.ddsm.diaries.controllers;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;
import org.stevie.ddsm.diaries.domain.DuplicationDiaryEntry;
import org.stevie.ddsm.diaries.service.month.MonthService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

@Component
public final class DuplicationDiaryController implements Initializable {

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
	private TableView<DuplicationDiaryEntry> diaryTableView;

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
		TableColumn<DuplicationDiaryEntry, String> monthColumn = new TableColumn<>("Month");
		monthColumn.setCellValueFactory(dde -> new SimpleStringProperty(dde.getValue().month().toString()));

		/*
		 * tuesday booking in day column
		 */
		TableColumn<DuplicationDiaryEntry, String> tuesdayColumn = new TableColumn<>("Booking In Tuesday");
		tuesdayColumn.setCellValueFactory(dde -> {
			var day = dde.getValue().bookingInDate().getDayOfMonth();
			var str = Integer.toString(day) + MonthService.getEnding(day);
			return new SimpleStringProperty(str);
		});

		/*
		 * wednesday barcoding and admin column
		 */
		TableColumn<DuplicationDiaryEntry, String> wednesdayColumn = new TableColumn<>("Barcoding & Admin Wednesday");
		wednesdayColumn.setCellValueFactory(dde -> {
			var day = dde.getValue().barcodingDate().getDayOfMonth();
			var str = Integer.toString(day) + MonthService.getEnding(day);
			return new SimpleStringProperty(str);
		});

		/*
		 * thursday duplication column
		 */
		TableColumn<DuplicationDiaryEntry, String> thursdayColumn = new TableColumn<>("Duplication Thursday");
		thursdayColumn.setCellValueFactory(dde -> {
			var day = dde.getValue().duplicationDate().getDayOfMonth();
			var str = Integer.toString(day) + MonthService.getEnding(day);
			return new SimpleStringProperty(str);
		});

		diaryTableView.getColumns().clear();
		/*
		 * add columns to table view (causes type safety warning)
		 */
		diaryTableView.getColumns().addAll(monthColumn, tuesdayColumn, wednesdayColumn, thursdayColumn);
	}

	/**
	 * Set Diary Items Method
	 * 
	 * Create observable list for display in the table view
	 * 
	 * @param items domain
	 * @return none
	 */
	public void setDiaryItems(List<DuplicationDiaryEntry> items) {
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
