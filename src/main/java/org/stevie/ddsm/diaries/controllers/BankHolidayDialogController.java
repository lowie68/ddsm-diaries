/**
 * <h3>Bank Holiday Dialog Controller Class</h3>
 * 
 * <p>Controller for bank-holiday.fxml view. This is the backing controller for the bank holiday dialog.
 * The bank holiday dialog presents the bank holidays retrieved from the REST API to the user. This allows
 * the user to check the bank holidays are correct for the selected year.
 * 
 * @author Stephen
 * @version 1.0
 * 
 */
package org.stevie.ddsm.diaries.controllers;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stevie.ddsm.diaries.service.bank.BankHoliday;
import org.stevie.ddsm.diaries.service.bank.FetchBankHolidaysTask;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;


public class BankHolidayDialogController implements Initializable {
	
	/*
	 * logback logger
	 */
	private static Logger logger = LoggerFactory.getLogger(BankHolidayDialogController.class);

	/*
	 * JavaFX controls
	 */
	@FXML
	private ChoiceBox<Integer> yearChoiceBox;
	@FXML
	private Button findBankHolidaysButton;
	@FXML
	private Button manualInputButton;
	@FXML
	private TableView<BankHoliday> bankHolidayTableView;
	@FXML
	private ProgressIndicator bankHolidayProgressIndicator;

	/**
	 * Initialise Form Method
	 * 
	 * Initialise form components
	 * 
	 * @param url
	 * @param resourtces
	 * @return none
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		configureYearChoiceBox();
		configureTableView();
		
	}

	@SuppressWarnings("unchecked")
	private void configureTableView() {
		/*
		 * date column
		 */
		TableColumn<BankHoliday, String> dateColumn = new TableColumn<>("Date of Bank Holiday");
		dateColumn.setCellValueFactory(bh -> {
			var date = bh.getValue().getDate();
			var formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
			var dateString = formatter.format(date);
			return new SimpleStringProperty(dateString);
		});	
		dateColumn.setPrefWidth(200.0);

		/*
		 * bank holiday description column
		 */
		TableColumn<BankHoliday, String> descriptionColumn = new TableColumn<>("Description");
		descriptionColumn.setCellValueFactory(bh -> new SimpleStringProperty(bh.getValue().getLocalName()));
		descriptionColumn.setPrefWidth(300.0);

		bankHolidayTableView.getColumns().clear();
		
		/*
		 * add columns to table view (causes type safety warning)
		 */
		bankHolidayTableView.getColumns().addAll(dateColumn, descriptionColumn);

	}

	private void configureYearChoiceBox() {
		/*
		 * Allow user to select a year 5 years in the past or 5 years in the future
		 */
		yearChoiceBox.setItems(createYearList());
		yearChoiceBox.getSelectionModel().select(5);
		
	}
	/**
	 * Create Year List
	 * Create the options that will appear in the drop down list of the year combo box.
	 * 
	 * @return observable list of year integers
	 */
	private ObservableList<Integer> createYearList() {
		ObservableList<Integer> list = FXCollections.observableArrayList();
		int todayYear = LocalDate.now().getYear();
		for (int i = todayYear-5; i<=todayYear+5; i++) {
			list.add(i);
		}
		return list;
	}
	
	/**
	 * Handle Find Bank Holidays Button
	 * 
	 * When the user clicks this button the bank holidays are retrieved from the internet
	 * or cache for the specified year
	 * 
	 * @param event
	 * @return none
	 */
	@FXML
	public void handleFindBankHolidaysButtonAction(ActionEvent event) {

		int currentYear = yearChoiceBox.getValue();
		
		FetchBankHolidaysTask task = new FetchBankHolidaysTask(currentYear);
		
		task.setOnSucceeded(ev -> {
			try {
				List<BankHoliday> list = task.get(10, TimeUnit.SECONDS);
				var bankHolidayItems = FXCollections.observableArrayList(list);
				bankHolidayTableView.setItems(bankHolidayItems);
			} catch (InterruptedException e) {
				logger.error("Bank holiday task cancelled while trying to retrieve bank holidays for year {}", currentYear);
				Thread.currentThread().interrupt();
			} catch (ExecutionException e) {
				logger.error("Bank holiday task failed while trying to retrieve bank holidays for year {}", currentYear);
			} catch (TimeoutException e) {
				logger.error("Bank holiday task timed out");
			}
			
		});
		
		task.setOnFailed(ev -> {
			logger.error("Bank holiday task failed while trying to retrieve bank holidays for year {} ", currentYear);
		});
		
		try (ExecutorService executor = Executors.newCachedThreadPool()) {
			executor.submit(task);
		}
		
	}
	
	// Event Listener on Button[#manualInputButton].onAction
	@FXML
	public void handleManualInputButtonAction(ActionEvent event) {
		throw new UnsupportedOperationException();
	}
}
