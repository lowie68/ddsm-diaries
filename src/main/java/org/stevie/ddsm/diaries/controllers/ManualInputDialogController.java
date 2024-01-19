/**
 * <h3>Manual Input Dialog Controller</h3>
 * 
 * <p>This class is an MVC Controller for the manual input dialog. This dialog allows the user to enter
 * the bank holidays for a particular year. The dialog has a table view on the left hand side
 * and a form on the right.</p>
 * 
 * @author Stephen
 * @version 1.0
 * 
 */
package org.stevie.ddsm.diaries.controllers;


import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;
import org.stevie.ddsm.diaries.messages.ErrorMessages;
import org.stevie.ddsm.diaries.service.bank.BankHoliday;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Manual Input Dialog Class
 * 
 * JavaFX controller backing the view in manual-input.fxml.
 * 
 */
@Component
public class ManualInputDialogController implements Initializable {

	/*
	 * table view model
	 */
	private ObservableList<BankHoliday> bankHolidayItems;
	
	/*
	 * ensure that the table view is kept in ascending date order
	 */
	private SortedList<BankHoliday> sortedbankHolidayItems;
	
	/*
	 * active year
	 */
	private int currentYear;
	
	/*
	 * JavaFX controls
	 */
    @FXML
    private TableColumn<BankHoliday, String> bankHolidayDateColumn;
    @FXML
    private DatePicker bankHolidayDatePicker;
    @FXML
    private TableColumn<BankHoliday, String> bankHolidayDescriptionColumn;
    @FXML
    private TextField bankHolidayDescriptionTextField;
    @FXML
    private TableView<BankHoliday> bankHolidaysTableView;
    @FXML
    private Button deleteBankHolidayButton;
    @FXML
    private Button newBankHolidayButton;
    @FXML
    private Button saveBankHolidayButton;
    @FXML
    private Label dialogHeaderLabel;
      
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
		 * when form is first presented there are no items in the table view
		 */
		this.bankHolidayItems = FXCollections.observableArrayList();
		/*
		 * make sure the table view keeps the bank holidays in ascending date order
		 */
    	this.sortedbankHolidayItems = new SortedList<BankHoliday>(bankHolidayItems);
    	/*
    	 * set comparator used to keep the items in order
    	 */
    	this.sortedbankHolidayItems.setComparator((bh1, bh2) -> bh1.getDate().compareTo(bh2.getDate()));
    	/*
    	 * link the table view to the model
    	 */
    	this.bankHolidaysTableView.setItems(sortedbankHolidayItems);	
    	/*
    	 * When the dialog is first displayed initialise description text field to blank
    	 */
    	bankHolidayDescriptionTextField.setText("");
    	/*
    	 * configure table view
    	 */
    	configureTableView();
    	
    	
	}

	/**
	 * Configure Table View Method
	 * 
	 * This method configures the JavaFX {@code TableView}. The {@code TableView} has a date column
	 * and a description column. The values for these columns are set using a cell value factory.
	 * 
	 * @since 1.0
	 */
	private void configureTableView() {
		
		/*
		 * generate values for the date column
		 */
		bankHolidayDateColumn.setCellValueFactory(bh -> {
			var date = bh.getValue().getDate();
			var formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
			var dateString = formatter.format(date);
			return new SimpleStringProperty(dateString);
		});	

		/*
		 * generate values for the bank holiday description column
		 */
		bankHolidayDescriptionColumn.setCellValueFactory(bh -> new SimpleStringProperty(bh.getValue().getLocalName()));

		/*
		 * update the form when the user clicks a selection in the table view
		 */
		bankHolidaysTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue)-> {
			if (newValue != null) {
				bankHolidayDatePicker.setValue(newValue.getDate());
		    	bankHolidayDescriptionTextField.setText(newValue.getName());
			}
		});
	}

	/**
	 * Delete Bank Holiday Button
	 * Event Handler
	 * 
	 * This method allows users to delete a bank holiday from the {@code TableView}. It first checks
	 * that an item has been selected. If it has, the method removes it from the underlying
	 * {@code ObservableList} model. If there is no selection, an error message is presented to the user.
	 * 
	 * @param button click event 
	 * 
	 * @since 1.0
	 */
    @FXML
    void handleDeleteBankHolidayButtonAction(ActionEvent event) {
    	
    	if (!bankHolidaysTableView.getSelectionModel().isEmpty()) {
    		var bh = bankHolidaysTableView.getSelectionModel().getSelectedItem();
        	bankHolidayItems.remove(bh);
    	}
    	else {
    		var alert = new Alert(AlertType.ERROR);
    		alert.setTitle(ErrorMessages.BANK_HOLIDAY_ERROR);
    		alert.setHeaderText(ErrorMessages.BANK_HOLIDAY_DELETION_ERROR);
    		alert.setContentText("Please select a bank holiday");
    		alert.showAndWait();
    	}
    }

    /**
     * New Bank Holiday Button
     * Event Handler
     * 
     * This method resets the form fields ready to input new values
     * 
     * @param button click event
     * @since 1.0
     */
    @FXML
    void handleNewBankHolidayButtonAction(ActionEvent event) {
    	
    	/*
    	 * initialise the date picker to the 1st January
    	 */
    	LocalDate firstJanuary = LocalDate.of(this.currentYear, 1, 1);
    	bankHolidayDatePicker.setValue(firstJanuary);
    	
    	/*
    	 * initialise the description to blank string
    	 */
    	bankHolidayDescriptionTextField.setText("");
    }

    /**
     * Save Bank Holiday Button
     * Event Handler
     * 
     * This method is invoked when the user clicks the save button. It first checks
     * that the input values are valid. It then creates a {@link BankHoliday} domain
     * object. Once the {@link BankHoliday} object has been constructed, the method
     * checks if it is already in the list. If it is, a confirmation dialog is presented the
     * user asking for permission to overwrite it. If not, it is added to the {@link ObservableList}.
     *   
     * @param button click event
     * @since 1.0
     */
    @FXML
    void handleSaveBankHolidayButtonAction(ActionEvent event) {
 	
    	/*
    	 * validate input values
    	 */
    	if (!ValidateForm()) return;
    	
    	/*
    	 * build a bank holiday object from the input values
    	 */
    	var bh = new BankHoliday.Builder()
    			.date(bankHolidayDatePicker.getValue())
    			.name(bankHolidayDescriptionTextField.getText())
    			.localName(bankHolidayDescriptionTextField.getText())
    			.counties(new String[] {"GB-ENG"})
    			.build();
    	
    	/*
    	 * check if bank holiday already in the list
    	 */
    	if (bankHolidayItems.contains(bh)) { // if it is
    		/*
    		 * display a confirmation dialog
    		 */
    		var alert = new Alert(AlertType.CONFIRMATION);
    		alert.getButtonTypes().remove(ButtonType.OK);
    		alert.getButtonTypes().remove(ButtonType.CANCEL);
    		alert.getButtonTypes().add(ButtonType.YES);
    		alert.getButtonTypes().add(ButtonType.NO);
    		alert.setTitle(ErrorMessages.BANK_HOLIDAY_ERROR);
    		alert.setHeaderText(ErrorMessages.DUPLICATE_BANK_HOLIDAY);
    		alert.setContentText("This bank holiday is already in the list. Do you want to overwrite it?");
    		Optional<ButtonType> bt = alert.showAndWait();
    		bt.filter((b1) -> b1 == ButtonType.YES ).ifPresent((b2) -> {
    			int idx = bankHolidayItems.indexOf(bh);
    			bankHolidayItems.set(idx, bh);
    		});
    	} else { //it isn't
    		/* 
    		 * add the bank holiday to the model (updates the {@link TableView})
    		 */
        	bankHolidayItems.add(bh);
    	}
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
		/*
		 * update the title
		 */
		dialogHeaderLabel.setText(dialogHeaderLabel.getText()+ " " + Integer.toString(year));
		/*
		 * store active year
		 */
		this.currentYear = year;
		
		/*
		 * initialise the {@link DatePicker} to a default value of first day of the first month 
		 */
    	var januaryFirst = LocalDate.of(year, 1, 1);
    	bankHolidayDatePicker.setValue(januaryFirst);
	}
	
	/**
	 * Get Bank Holiday Items Method
	 * 
	 * This method is used to retrieve all the bank holidays input by the user. These are subsequently
	 * displayed in the bank holiday dialog.
	 * 
	 * @return all the bank holidays input using the dialog
	 * 
	 * @since 1.0
	 */
	public ObservableList<BankHoliday> getBankHolidayItems() {
		
		/*
		 * return a defensive copy to prevent giving the user of the class a reference to the 
		 * private collection
		 */
		return FXCollections.observableArrayList(sortedbankHolidayItems);
	}

	/**
	 * Validate Form Method
	 * 
	 * This method checks that the users input values are valid. If all the inputs
	 * are valid, the method returns true. If there are errors in the inputs, an error
	 * message is displayed the the user and the method returns false.
	 * 
	 * @return true if all inputs are valid
	 * 
	 * @since 1.0
	 */
    private boolean ValidateForm() {
    	
    	/*
    	 * check date has been selected
    	 */
    	if (bankHolidayDatePicker.getValue() == null) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(ErrorMessages.BANK_HOLIDAY_ERROR);
			alert.setHeaderText(ErrorMessages.BANK_HOLIDAY_FORM_VALIDATION_ERROR);
			alert.setContentText("Please select a date");
			alert.showAndWait();
			return false;
		}

    	/*
    	 * make sure selected date falls in the active year
    	 */
    	if (bankHolidayDatePicker.getValue().getYear() != this.currentYear) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(ErrorMessages.BANK_HOLIDAY_ERROR);
			alert.setHeaderText(ErrorMessages.BANK_HOLIDAY_FORM_VALIDATION_ERROR);
			alert.setContentText("The selected year should match the year you are inputting for");
			alert.showAndWait();
			return false;
		}

    	/*
    	 * make sure description is not blank
    	 */
    	if (bankHolidayDescriptionTextField.getText().isBlank()) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(ErrorMessages.BANK_HOLIDAY_ERROR);
			alert.setHeaderText(ErrorMessages.BANK_HOLIDAY_FORM_VALIDATION_ERROR);
			alert.setContentText("Please enter a description");
			alert.showAndWait();
			return false;
		}

    	return true;
	}
}

