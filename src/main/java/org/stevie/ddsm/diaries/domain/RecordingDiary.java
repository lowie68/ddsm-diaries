/**
 * <h3>Recording Diary Class</h3>
 * 
 * <p>DDSM recordings are done once a month on the first Monday of the month. If the date falls
 * on a bank holiday the recordings are done the following week. 
 * The date of the first edition in a new year has to be input manually. A recording diary consists
 * of 12 entries one for each month. The builder pattern has been used when creating new objects.</p>
 * 
 * @author Stephen
 * @version 1.0
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

/**
 * Recording Diary Class
 * 
 * This class is part of the model. It is marked with the final keyword to prevent inheritance.
 * 
 */
public final class RecordingDiary extends AbstractDiary<RecordingDiaryEntry> {

	/*
	 * logging
	 */
	private static Logger logger = LoggerFactory.getLogger(RecordingDiary.class);
	/*
	 * diary entries (12 per year)
	 */
	private List<RecordingDiaryEntry> diaryEntries = new ArrayList<>();
	/*
	 * date of january edition input manually
	 */
	private LocalDate januaryEdition;
	/*
	 * edition is incremented by 1 every month and rolls over the following year
	 */
	private int edition;
	/*
	 * there are two compilers who alternate each month
	 */
	private String compiler_1;
	private String compiler_2;

	/**
	 * Copy Constructor
	 * 
	 * This constructor creates a recording diary object and generates it's entries
	 * It is marked private to make sure that other classes can only create {@link RecordingDiary}
	 * objects by using the builder pattern.  
	 * 
	 * @since 1.0
	 */
	private RecordingDiary(RecordingDiaryBuilder builder) {
		/*
		 * set properties
		 */
		this.januaryEdition = builder.januaryEdition;
		this.edition = builder.edition;
		this.compiler_1 = builder.compiler_1;
		this.compiler_2 = builder.compiler_2;
	}

	/**
	 * Get Entries Method
	 * 
	 * Returns all the entries in the diary. The method returns a defensive copy to preserve the 
	 * invariants of the class.
	 * 
	 * @return list of entries (12 in number) 
	 * 
	 */
	@Override
	public List<RecordingDiaryEntry> getEntries() {
		return new ArrayList<>(diaryEntries);
	}
	
	/**
	 * Generate Diary Method
	 * 
	 * This method generates all the entries for this years diary (12 in total).
	 * 
	 * @since 1.0
	 */
	@Override
	public void generateDiary() {
		
		/*
		 * create the January entry
		 */
		var januaryEntry = new RecordingDiaryEntry(Month.of(1), this.januaryEdition, this.edition++, this.compiler_1);
		diaryEntries.add(januaryEntry);
		
		/*
		 * create the February to December entries
		 */
		for (int month = 2; month <= 12; month++) {
			/*
			 * compiler alternates between compiler 1 and compiler 2
			 */
			var compiler = month%2 != 0 ? this.compiler_1 : this.compiler_2;
			/*
			 * current date
			 */
			var current = LocalDate.of(getYear(), month, 1);
			/*
			 * calculate first Monday in the month
			 */
			var firstMonday = current.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
			/*
			 * check whether first Monday is a bank holiday
			 */
			if (BankHolidayService.isBankHoliday(firstMonday)) { //if the Monday is a bank holiday
				/*
				 * find the next non bank holiday
				 */
				var afterBankHoliday = BankHolidayService.getNextNonBankHoliday(firstMonday);
				/*
				 * create an entry and add it to the diary entries collection
				 */
				var entry = new RecordingDiaryEntry(Month.of(month), afterBankHoliday, this.edition++, compiler);
				diaryEntries.add(entry);
			} else { // it is not a bank holiday
				/*
				 * just create an entry and add it to the entries collection
				 */
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
	 * @since 1.0
	 */
	@Override
	public void printDiaryToConsole() {
		logger.info("RECORDING DATES {}", getYear());
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
	 * @author Stephen
	 *
	 */
	public static class RecordingDiaryBuilder {
		private LocalDate januaryEdition;
		private int edition;
		private String compiler_1;
		private String compiler_2;

		public RecordingDiaryBuilder januaryEdition(LocalDate januaryEdition) {
			this.januaryEdition = januaryEdition;
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
