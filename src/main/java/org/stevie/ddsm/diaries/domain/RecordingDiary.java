/**
 * Recording Diary Class
 * 
 * DDSM recordings are done once a month on the first Monday or Tuesday of the month. If the date falls
 * the recordings are done the following week. There are two recording teams containing one compiler and 
 * one assistant who work on alternate months. These are known as Decani team and Cantoris team.
 * The date of the first edition in a new year has to be input manually. A recording diary consists
 * of 12 entries one for each month. The builder pattern has been used when creating new objects.
 * 
 */
package org.stevie.ddsm.diaries.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import org.stevie.ddsm.diaries.service.BankHolidayService;


public final class RecordingDiary implements DdsmDiary {
	
	private List<RecordingDiaryEntry> diaryEntries = new ArrayList<>();
	private int diaryYear;
	private LocalDate januaryEdition;
	private int edition;
	private Team team;
	private String decaniCompiler;
	private String decaniAssistant;
	private String cantorisCompiler;
	private String cantorisAssistant;

	private RecordingDiary(RecordingDiaryBuilder builder) {
		this.diaryYear = builder.diaryYear;
		this.januaryEdition = builder.januaryEdition;
		this.edition = builder.edition;
		this.team = builder.team;
		this.decaniCompiler = builder.decaniCompiler;
		this.decaniAssistant = builder.decaniAssistant;
		this.cantorisCompiler = builder.cantorisCompiler;
		this.cantorisAssistant = builder.cantorisAssistant;
	}

	/**
	 * Get Entries
	 * 
	 * Returns all the entries in the diary. These are copied to an observable list for presentation in
	 * the UI. The method returns a defensive copy to preserve the invariants of the class.
	 * 
	 * @return list of entries 12 in number 
	 * 
	 */
	public List<RecordingDiaryEntry> getEntries() {
		return new ArrayList<>(diaryEntries);
	}
	
	/**
	 * Get Diary Year
	 * 
	 * Returns the year of the diary
	 */
	@Override
	public int getDiaryYear() {
		return this.diaryYear;
	}
	
	/**
	 * Generate Diary
	 * 
	 * Creates the diary entries. The method generates the January edition which is specified
	 * by the user. The method then uses this date and generates the February to December entries.  
	 * 
	 */
	@Override
	public void generateDiary() {
		
		/*
		 * use toggle to flip to alternate team
		 */
		boolean toggle = false;

		/*
		 * create the january entry
		 */
		if (januaryEdition.getDayOfWeek() == DayOfWeek.MONDAY) {
			createMondayEntry(januaryEdition);
			toggle = true;
		}
		else {
			createTuesdayEntry(januaryEdition);
			toggle = false;
		}
		
		/*
		 * create the February to December entries
		 */
		for (int month = 2; month <= 12; month++) {
			var current = LocalDate.of(this.diaryYear, month, 1);
			if (toggle) { 
				var firstTuesday = current.with(TemporalAdjusters.firstInMonth(DayOfWeek.TUESDAY));
				createTuesdayEntry(firstTuesday);
			} else {
				var firstMonday = current.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
				createMondayEntry(firstMonday);
			}
			toggle = !toggle;
		}

	}

	/**
	 * Create Monday Entry
	 * 
	 * If the first Monday of the month falls on a bank holiday, the recording is done on the following week.
	 * 
	 * @param monday date
	 */
	private void createMondayEntry(LocalDate monday) {
		if (BankHolidayService.isBankHoliday(monday)) { 
			var afterBankHoliday = BankHolidayService.getNextNonBankHoliday(monday);
			var entry = new RecordingDiaryEntry(afterBankHoliday, this.edition++, Team.CANTORIS, this.cantorisCompiler, this.cantorisAssistant);
			diaryEntries.add(entry);
		}
		else {
			var entry = new RecordingDiaryEntry(monday, this.edition++, Team.CANTORIS, this.cantorisCompiler, this.cantorisAssistant);
			diaryEntries.add(entry);
		}
	}

	/**
	 * Create Tuesday Entry
	 * 
	 * If the first Tuesday of the month falls on a bank holiday, the recording is done on the following week.
	 * 
	 * @param tuesday date
	 */
	private void createTuesdayEntry(LocalDate tuesday) {
		if (BankHolidayService.isBankHoliday(tuesday)) { 
			var afterBankHoliday = BankHolidayService.getNextNonBankHoliday(tuesday);
			var entry = new RecordingDiaryEntry(afterBankHoliday, this.edition++, Team.DECANI, this.decaniCompiler, this.decaniAssistant);
			diaryEntries.add(entry);
		}
		else {
			var entry = new RecordingDiaryEntry(tuesday, this.edition++, Team.DECANI, this.decaniCompiler, this.decaniAssistant);
			diaryEntries.add(entry);
		}
	}

	/**
	 * Print Diary
	 * 
	 * Print the diary entries to the console. To be upgraded later.
	 * 
	 */
	@Override
	public void printDiaryToConsole() {
		System.out.println("RECORDING DATES " + this.diaryYear);
		for (RecordingDiaryEntry entry : diaryEntries) {
			var sb = new StringBuilder();
			sb.append("Date=" + entry.recordingDate() + " ");
			sb.append("(" + entry.recordingDate().getDayOfWeek() + ") ");
			sb.append("Edition=" + entry.edition() + " ");
			sb.append("Team=" + entry.team() + " ");
			sb.append("Compiler=" + entry.compiler() + " ");
			sb.append("Assistant=" + entry.assistantCompiler());
			System.out.println(sb);
		}
	}
	/**
	 * Builder Pattern
	 * 
	 * This class is nested class which is used as part of the builder pattern
	 * 
	 * @author steph
	 *
	 */
	public static class RecordingDiaryBuilder {
		private LocalDate januaryEdition;
		private int diaryYear;
		private int edition;
		private Team team;
		private String decaniCompiler;
		private String decaniAssistant;
		private String cantorisCompiler;
		private String cantorisAssistant;

		public RecordingDiaryBuilder januaryEdition(LocalDate januaryEdition) {
			this.januaryEdition = januaryEdition;
			return this;
		}

		public RecordingDiaryBuilder diaryYear(int diaryYear) {
			this.diaryYear = diaryYear;
			return this;
		}

		public RecordingDiaryBuilder edition(int edition) {
			this.edition = edition;
			return this;
		}

		public RecordingDiaryBuilder team(Team team) {
			this.team = team;
			return this;
		}

		public RecordingDiaryBuilder decaniCompiler(String decaniCompiler) {
			this.decaniCompiler = decaniCompiler;
			return this;
		}
		
		public RecordingDiaryBuilder decaniAssistant(String decaniAssistant) {
			this.decaniAssistant = decaniAssistant;
			return this;
		}

		public RecordingDiaryBuilder cantorisCompiler(String cantorisCompiler) {
			this.cantorisCompiler = cantorisCompiler;
			return this;
		}
		
		public RecordingDiaryBuilder cantorisAssistant(String cantorisAssistant) {
			this.cantorisAssistant = cantorisAssistant;
			return this;
		}

		public RecordingDiary build() {
			RecordingDiary diary = new RecordingDiary(this);
			validateDiaryObject(diary);
			return diary;
		}

		private void validateDiaryObject(RecordingDiary diary) {
			// TODO Auto-generated method stub
		}

	}

}
