/**
 * Recording Diary Class
 * 
 * DDSM recordings are done once a month on the first Monday or Tuesday of the month. If the date falls
 * on a bank holiday the recordings are done the following week. There are two recording teams containing one compiler and 
 * one assistant who work on alternate months. These are known as Decani team and Cantoris team.
 * The date of the first edition in a new year has to be input manually. A recording diary consists
 * of 12 entries one for each month. The builder pattern has been used when creating new objects.
 * 
 */
package org.stevie.ddsm.diaries.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stevie.ddsm.diaries.service.bank.BankHolidayService;


public final class RecordingDiary implements DdsmDiary {

	private static Logger logger = LoggerFactory.getLogger(RecordingDiary.class);
	
	private List<RecordingDiaryEntry> diaryEntries = new ArrayList<>();
	private int diaryYear;
	private LocalDate januaryEdition;
	private int edition;
	private String compiler_1;
	private String compiler_2;

	private RecordingDiary(RecordingDiaryBuilder builder) {
		this.diaryYear = builder.diaryYear;
		this.januaryEdition = builder.januaryEdition;
		this.edition = builder.edition;
		this.compiler_1 = builder.compiler_1;
		this.compiler_2 = builder.compiler_2;
		generateDiary();
	}

	/**
	 * Get Entries Method
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
	 * Get Diary Year Method
	 * 
	 * Returns the year of the diary
	 */
	@Override
	public int getDiaryYear() {
		return this.diaryYear;
	}
	
	/**
	 * Generate Diary Method
	 * 
	 * Creates the diary entries. The method generates the January edition which is specified
	 * by the user. The method then uses this date and generates the February to December entries.  
	 * 
	 */
	private void generateDiary() {
		
		/*
		 * create the January entry
		 */
		var januaryEntry = new RecordingDiaryEntry(Month.of(1), this.januaryEdition, this.edition++, this.compiler_1);
		diaryEntries.add(januaryEntry);
		
		/*
		 * create the February to December entries
		 */
		for (int month = 2; month <= 12; month++) {
			var compiler = month%2 != 0 ? this.compiler_1 : this.compiler_2;
			var current = LocalDate.of(this.diaryYear, month, 1);
			var firstMonday = current.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
			if (BankHolidayService.isBankHoliday(firstMonday)) { 
				var afterBankHoliday = BankHolidayService.getNextNonBankHoliday(firstMonday);
				var entry = new RecordingDiaryEntry(Month.of(month), afterBankHoliday, this.edition++, compiler);
				diaryEntries.add(entry);
			} else {
				var entry = new RecordingDiaryEntry(Month.of(month), firstMonday, this.edition++, compiler);
				diaryEntries.add(entry);
			}
		}

	}


	/**
	 * Print Diary To Console Method
	 * 
	 * Print the diary entries to the console
	 * 
	 */
	@Override
	public void printDiaryToConsole() {
		logger.info("RECORDING DATES {}", this.diaryYear);
		for (RecordingDiaryEntry entry : diaryEntries) {
			var sb = new StringBuilder();
			sb.append("Month=" + entry.month() + " ");
			sb.append("Date=" + entry.recordingDate() + " ");
			sb.append("(" + entry.recordingDate().getDayOfWeek() + ") ");
			sb.append("Edition=" + entry.edition() + " ");
			sb.append("Compiler=" + entry.compiler() + " ");
			if (!sb.toString().isEmpty()) {
				var str = sb.toString();
				logger.info(str);
			}
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
		private String compiler_1;
		private String compiler_2;

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

		public RecordingDiaryBuilder compiler_1(String compiler_1) {
			this.compiler_1 = compiler_1;
			return this;
		}
		
		public RecordingDiaryBuilder compiler_2(String compiler_2) {
			this.compiler_2 = compiler_2;
			return this;
		}

		public RecordingDiary build() {
			return new RecordingDiary(this);
		}

	}

}
