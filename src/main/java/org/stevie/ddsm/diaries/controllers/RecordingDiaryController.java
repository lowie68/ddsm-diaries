package org.stevie.ddsm.diaries.controllers;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;
import org.stevie.ddsm.diaries.domain.RecordingDiaryEntry;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

@Component
public final class RecordingDiaryController implements Initializable {
	
	private ObservableList<RecordingDiaryEntry> diaryItems;
	private SimpleStringProperty diaryYear = new SimpleStringProperty();
	
	@FXML
	private Label diaryYearLabel;
	@FXML
	private TableView<RecordingDiaryEntry> diaryTableView;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		diaryYearLabel.textProperty().bind(diaryYear);

		TableColumn<RecordingDiaryEntry, String> recordingDateColumn = new TableColumn<>("First Monday or Tuesday in Month");
		recordingDateColumn.setCellValueFactory(rde -> {
			var date = rde.getValue().recordingDate();
			var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			var dateString = formatter.format(date);
			var resultString = "(" + date.getDayOfWeek().toString() + ") " + dateString;
			return new SimpleStringProperty(resultString);
		});	
	
		TableColumn<RecordingDiaryEntry, Integer> editionColumn = new TableColumn<>("Edition");
		editionColumn.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().edition()).asObject());

		TableColumn<RecordingDiaryEntry, String> teamColumn = new TableColumn<>("Team");
		teamColumn.setCellValueFactory(rde -> new SimpleStringProperty(rde.getValue().team().toString()));
		
		TableColumn<RecordingDiaryEntry, String> compilerColumn = new TableColumn<>("Compiler");
		compilerColumn.setCellValueFactory(rde -> new SimpleStringProperty(rde.getValue().compiler()));

		TableColumn<RecordingDiaryEntry, String> assistantColumn = new TableColumn<>("Assistant Compiler");
		assistantColumn.setCellValueFactory(rde -> new SimpleStringProperty(rde.getValue().assistantCompiler()));

		diaryTableView.getColumns().clear();
		diaryTableView.getColumns().addAll(recordingDateColumn, editionColumn, teamColumn, compilerColumn, assistantColumn);
		
	}
	
	public void setDiaryItems(List<RecordingDiaryEntry> items) {
		Objects.requireNonNull(items);
		diaryItems = FXCollections.observableArrayList(items);
		diaryTableView.setItems(diaryItems);
	}
	
	public void setYear(int year) {
		diaryYear.set(Integer.toString(year));
	}

}
