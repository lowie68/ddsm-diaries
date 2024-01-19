/**
 * <h3>Duplication Rota Dialog</h3>
 * 
 * <p>This class is the MVC controller for the preparation and duplication dialog.
 * The dialog presents the duplication and preparation rota to the end user</p>
 * 
 * @author Stephen
 * @version 1.0
 * 
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

/**
 * Duplication Rota Dialog Class
 * 
 * JavaFX controller backing the view in duplication.fxml.
 * 
 */
@Component
public final class DuplicationDiaryController implements Initializable {
	/*
	 * JavaFX controls
	 */
	@FXML
	private Label dialogHeaderLabel;
	@FXML
	private TableView<DuplicationDiaryEntry> diaryTableView;
    @FXML
    private TableColumn<DuplicationDiaryEntry, String> tuesdayColumn;
    @FXML
    private TableColumn<DuplicationDiaryEntry, String> thursdayColumn;
    @FXML
    private TableColumn<DuplicationDiaryEntry, String> monthColumn;
    @FXML
    private TableColumn<DuplicationDiaryEntry, String> wednesdayColumn;

    /**
 	 * Initialise Controller
 	 * 
 	 * Called to initialise a controller after its root element has been completely processed.
 	 * 
 	 * @param url
 	 * @param resources
 	 * @since 1.0
 	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		/*
		 * call private method
		 */
		configureTableView();
		
	}

	/**
	 * Configure Table View Method
	 * 
	 * This method configures the JavaFX {@code TableView}. The {@code TableView} has three columns.
	 * for Tuesday, Wednesday and Thursdays a week after the recording. The values for these columns
	 * are set using a cell value factory.
	 * 
	 * @since 1.0
	 */
	private void configureTableView() {
		
		/*
		 * month column January-December
		 */
		monthColumn.setCellValueFactory(dde -> new SimpleStringProperty(dde.getValue().month().toString()));

		/*
		 * tuesday collect wallets from St Mary's house column
		 */
		tuesdayColumn.setCellValueFactory(dde -> {
			var day = dde.getValue().collectDate().getDayOfMonth();
			var str = Integer.toString(day) + MonthService.getEnding(day);
			return new SimpleStringProperty(str);
		});

		/*
		 * wednesday barcoding and admin
		 */
		wednesdayColumn.setCellValueFactory(dde -> {
			var day = dde.getValue().barcodingDate().getDayOfMonth();
			var str = Integer.toString(day) + MonthService.getEnding(day);
			return new SimpleStringProperty(str);
		});

		/*
		 * thursday duplication
		 */
		thursdayColumn.setCellValueFactory(dde -> {
			var day = dde.getValue().duplicationDate().getDayOfMonth();
			var str = Integer.toString(day) + MonthService.getEnding(day);
			return new SimpleStringProperty(str);
		});

	}

	/**
	 * Set Diary Items Method
	 * 
	 * This method sets the {@link TableView} model. It is called before the stage is
	 * displayed from the {@link MainFormController}.

	 * @param collection of diary items
	 * @since 1.0
	 */
	public void setDiaryItems(List<DuplicationDiaryEntry> items) {
		/*
		 * raise exception if argument null 
		 */
		Objects.requireNonNull(items);
		/*
		 * set table view model
		 */
		var diaryItems = FXCollections.observableArrayList(items);
		diaryTableView.setItems(diaryItems);
	}
	
	/**
	 * Set Diary Year Method
	 * 
	 * This method sets the active year in the dialog header. It is called before the stage
	 * is displayed by the {@link MainFormController}.
	 * 
	 * @param active year
	 * @since 1.0
	 */
	public void setYear(int year) {
		/*
		 * set the Label text property
		 */
		dialogHeaderLabel.setText(dialogHeaderLabel.getText()+ " " + Integer.toString(year));
	}
}
