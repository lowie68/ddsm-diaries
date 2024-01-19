/**
 * <h3>Bank Holiday Dialog Controller</h3>
 * 
 * <p>This class is an MVC Controller for the bank holiday dialog. The user can choose to download
 * the bank holidays online to retrieve the bank holidays for a particular year. If no Internet
 * is available, the user can choose to input them manually or import them from a text file.
 * 
 * @author Stephen
 * @version 1.0
 * 
 */
package org.stevie.ddsm.diaries.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.stevie.ddsm.diaries.messages.ErrorMessages;
import org.stevie.ddsm.diaries.service.bank.BankHoliday;
import org.stevie.ddsm.diaries.service.bank.BankHolidayCache;
import org.stevie.ddsm.diaries.service.bank.FetchBankHolidaysTask;
import org.stevie.ddsm.diaries.service.internet.InternetService;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

/**
 * Bank Holiday Dialog Class
 * 
 * JavaFX controller backing the view in bank-holiday.fxml.
 * 
 */
@Component
public class BankHolidayDialogController implements Initializable {
	
	/*
	 * logger
	 */
	private static Logger logger = LoggerFactory.getLogger(BankHolidayDialogController.class);
	
	/*
	 * JavaFX controls
	 */
	@FXML
	private Button findOnlineButton;
	@FXML
	private Button manualInputButton;
	@FXML
	private Button importFromFileButton;
	@FXML
	private Button exportToFileButton;
	@FXML
	private Label headerLabel;
	@FXML
	private TableView<BankHoliday> bankHolidayTableView;
    @FXML
    private TableColumn<BankHoliday, String> bankHolidayDateColumn;
    @FXML
    private TableColumn<BankHoliday, String> bankHolidayDescriptionColumn;
    
    /*
     * the year the dialog is related to
     */
    private int currentYear;

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
		 * configure the table view
		 */
		configureTableView();
		
		
	}

	/**
	 * Configure Table View Method
	 * 
	 * The table view displays all the bank holidays in a given year. The user can download them from
	 * online, input them manually or import them from a text file.
	 * 
	 * @since 1.0
	 */
	private void configureTableView() {
		
		/*
		 * the date column displays the date of the bank holiday including the day that the bank holiday
		 * falls on. The cell value factory needs to be set to specify how to populate all
		 * cells within the date column.
		 */
		bankHolidayDateColumn.setCellValueFactory(bh -> {
			var date = bh.getValue().getDate();
			var formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
			var dateString = formatter.format(date);
			return new SimpleStringProperty(dateString);
		});	

		/*
		 * the bank holiday description column is a description of the bank holiday e.g. Christmas Day or
		 * May Bank Holiday etc.
		 */
		bankHolidayDescriptionColumn.setCellValueFactory(bh -> new SimpleStringProperty(bh.getValue().getLocalName()));

		/*
		 * make sure no bank holidays are displayed when the dialog is first loaded.
		 */
		bankHolidayTableView.setItems(FXCollections.observableArrayList());

	}

	/**
	 * Find Bank Holidays Online Button
	 * Event Handler
	 * 
	 * This method allows the user to download bank holidays using the nager date REST API.
	 * It first checks that an Internet connection is available. If so, it starts a background task
	 * which fetches the dates over the network. On task completion the retrieved dates are 
	 * stored in the bank holiday cache for subsequent use during rota generation.
	 * 
	 * @param button click event 
	 * @throws IOException if Internet status check fails
	 * 
	 * @since 1.0
	 */
	@FXML
	public void handleFindOnlineButtonAction(ActionEvent event) throws IOException {
		
		/*
		 * check Internet is available
		 */
		if (!InternetService.isInternetAvailable()) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle("Find Bank Holidays");
			alert.setHeaderText("No Internet Available");
			alert.setContentText("Unable to retrieve bank holidays from online API nager date. Please input them manually.");
			alert.showAndWait();
			return;
		}
		
		/*
		 * create background task
		 */
		FetchBankHolidaysTask task = new FetchBankHolidaysTask(this.currentYear);
		
		/*
		 * The onSucceeded event handler is called whenever the Task state
    	 * transitions to the SUCCEEDED state. It calls the get method which
    	 * blocks until the result is available. If the get method times out after waiting 10 seconds
    	 * a {@code TimeoutException} is raised and an error message is output to the log.
    	 * If the get fails with an exception an {@code ExecutionException} is raised and an error
    	 * is output to the log. Once the task has completed successfully the underlying data model 
    	 * for the TableView is set to the list. 
		 */
		task.setOnSucceeded(ev -> {
			try {
				List<BankHoliday> list = task.get(10, TimeUnit.SECONDS);
				var bankHolidayItems = FXCollections.observableArrayList(list);
				bankHolidayTableView.setItems(bankHolidayItems);
				BankHolidayCache.putBankHolidaysInCache(list, this.currentYear);
			} catch (InterruptedException e) {
				logger.error("Bank holiday task cancelled while trying to retrieve bank holidays for year {}", this.currentYear);
				Thread.currentThread().interrupt();
			} catch (ExecutionException e) {
				logger.error("Bank holiday task failed while trying to retrieve bank holidays with exception {}", e.getCause().getMessage());
			} catch (TimeoutException e) {
				logger.error("Bank holiday task timed out");
			}
			
		});
		
		/*
		 * The onFailed event handler is called whenever the Task state
    	 * transitions to the FAILED state. 
		 */
		task.setOnFailed(ev -> {
			logger.error("Bank holiday task failed while trying to retrieve bank holidays for year {} ", this.currentYear);
		});
		/*
		 * submit the task for execution
		 */
		try (ExecutorService executor = Executors.newCachedThreadPool()) {
			executor.submit(task);
		}
		
	}
	
	/**
	 * Manual Input Bank Holidays Button
	 * Action Event Handler
	 * 
	 * This event handler is invoked when the end user clicks the manual input dialog button.
	 * The dialog pane is loaded from the fxml file and is wrapped in a Dialog instance. The dialog
	 * has an Apply and Cancel button. It returns a result of {@code ObservableList<BankHoliday>} which 
	 * contains the bank holidays input by the user. </p>
	 * 
	 * @param button click event
	 * 
	 * @since 1.0
	 */
	@FXML
	public void handleManualInputButtonAction(ActionEvent event) {
		/*
		 * display manual input dialog
		 */
		try {
			/*
			 * create the loader & parse fxml file
			 */
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/manual-input.fxml"));
			DialogPane pane = loader.load();
			/*
			 * add dialog buttons 
			 */
			pane.getButtonTypes().add(ButtonType.CANCEL);
			pane.getButtonTypes().add(ButtonType.APPLY);
			/*
			 * get controller reference & set year parameter
			 */
			ManualInputDialogController controller = loader.getController();
			controller.setYear(this.currentYear);
			/*
			 * create dialog instance, set window title and pane root
			 */
			Dialog<ObservableList<BankHoliday>> dialog = new Dialog<>();
			dialog.setTitle("Bank Holidays Manual Input");
			dialog.setDialogPane(pane);
			/*
			 * The dialog returns a result of {@code ObservableList<BankHoliday>}. If the 
			 * {@code ButtonType.APPLY} button was clicked.
			 */
			dialog.setResultConverter(new Callback<ButtonType, ObservableList<BankHoliday>>() {
			    @Override
			    public ObservableList<BankHoliday> call(ButtonType b) {
			        if (b == ButtonType.APPLY) {
			        	return controller.getBankHolidayItems();
			        }

			        return null;
			    }
			});
			
			/*
			 * This will block until the dialog is closed.  
			 */
			Optional<ObservableList<BankHoliday>> result = dialog.showAndWait();
			/*
			 * if the optional contains a value, the underlying data model for the TableView 
			 * is set to the list and the cache is updated.
			 */
			if (result.isPresent()) {
				var bankHolidayList = result.get();
				bankHolidayTableView.setItems(bankHolidayList);
				BankHolidayCache.putBankHolidaysInCache(bankHolidayList, this.currentYear);
			}			
		/*
		 * if an {@code IOException} is raised during the form loading process an error message
		 * is output to the log and an error box is presented to the user. The program then exits.	
		 */
		} catch (IOException e) {
			logger.error("I/O exception occurred while loading manual input dialog - {}",  e.getMessage());
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(ErrorMessages.FATAL_ERROR);
			alert.setHeaderText(ErrorMessages.FORM_LOAD_ERROR);
			alert.setContentText("An exception occurred during the form loading process. See log file for details!");
			alert.showAndWait();
			Platform.exit();	
		}
	}

    @FXML
    void handleImportFromFileButtonAction(ActionEvent event) {
    	throw new UnsupportedOperationException();
    }

    @FXML
	void handleExportToFileButtonAction(ActionEvent event) {
		throw new UnsupportedOperationException();
	}

    
	/**
	 * Set Year Method
	 * 
	 * This method is called after the dialog has been constructed. It updates an instance field
	 * which is used elsewhere in the class. The view contains a {@code Label} which displays the 
	 * active year to the end user.
	 *  
	 * @param active year
	 * 
	 * @since 1.0
	 */
	public void setYear(int year) {
		this.currentYear = year;
		headerLabel.setText("Bank Holidays For Year " + this.currentYear);
	}
}

