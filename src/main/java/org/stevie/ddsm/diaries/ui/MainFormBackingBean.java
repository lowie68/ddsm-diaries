package org.stevie.ddsm.diaries.ui;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

@Component
public class MainFormBackingBean {
	
	private SimpleIntegerProperty diaryYearProperty;
	private SimpleIntegerProperty magazineEditionProperty;
	private SimpleObjectProperty<LocalDate> januaryEditionProperty;
	private SimpleStringProperty decaniCompilerProperty;
	private SimpleStringProperty decaniAssistantCompilerProperty;
	private SimpleStringProperty cantorisCompilerProperty;
	private SimpleStringProperty cantorisAssistantCompilerProperty;
	
	public MainFormBackingBean() {
		this.diaryYearProperty = new SimpleIntegerProperty(2000);
		this.magazineEditionProperty = new SimpleIntegerProperty(400);
		this.januaryEditionProperty = new SimpleObjectProperty<>(LocalDate.of(2000, 1, 1));
		this.decaniCompilerProperty = new SimpleStringProperty("");
		this.decaniAssistantCompilerProperty = new SimpleStringProperty("");
		this.cantorisCompilerProperty = new SimpleStringProperty("");
		this.cantorisAssistantCompilerProperty = new SimpleStringProperty("");
	}

	public int getDiaryYear() {
		return diaryYearProperty.getValue();
	}
	
	public void setDiaryYear(int diaryYear) {
		this.diaryYearProperty.setValue(diaryYear);
	}
	
	public SimpleIntegerProperty diaryYearPropertty() {
		return this.diaryYearProperty;
	}

	public int getMagazineEdition() {
		return magazineEditionProperty.getValue();
	}
	
	public void setMagazineEdition(int magazineEdition) {
		this.magazineEditionProperty.setValue(magazineEdition);
	}
	
	public SimpleIntegerProperty magazineEditionProperty() {
		return this.magazineEditionProperty;
	}

	public LocalDate getJanuaryEdition() {
		return this.januaryEditionProperty.getValue();
	}
	
	public void setJanuaryEdition(LocalDate januaryEdition) {
		this.januaryEditionProperty.setValue(januaryEdition);
	}
	
	public SimpleObjectProperty<LocalDate> januaryEditionProperty() {
		return this.januaryEditionProperty;
	}
	
	public String getDecaniCompiler() {
		return this.decaniCompilerProperty.getValue();
	}
	
	public void setDecaniCompiler(String decaniCompiler) {
		this.decaniCompilerProperty.setValue(decaniCompiler);
	}
	
	public SimpleStringProperty decaniCompilerProperty() {
		return this.decaniCompilerProperty;
	}

	public String getDecaniAssistantCompiler() {
		return this.decaniAssistantCompilerProperty.getValue();
	}
	
	public void setDecaniAssistantCompiler(String decaniAssistantCompiler) {
		this.decaniCompilerProperty.setValue(decaniAssistantCompiler);
	}
	
	public SimpleStringProperty decaniAssistantCompilerProperty() {
		return this.decaniAssistantCompilerProperty;
	}

	public String getCantorisCompiler() {
		return this.cantorisCompilerProperty.getValue();
	}
	
	public void setCantorisCompiler(String cantorisCompiler) {
		this.cantorisCompilerProperty.setValue(cantorisCompiler);
	}
	
	public SimpleStringProperty cantorisCompilerProperty() {
		return this.cantorisCompilerProperty;
	}

	public String getCantorisAssistantCompiler() {
		return this.cantorisAssistantCompilerProperty.getValue();
	}
	
	public void setCantorisAssistantCompiler(String cantorisAssistantCompiler) {
		this.cantorisAssistantCompilerProperty.setValue(cantorisAssistantCompiler);
	}
	
	public SimpleStringProperty cantorisAssistantCompilerProperty() {
		return this.cantorisAssistantCompilerProperty;
	}

}
