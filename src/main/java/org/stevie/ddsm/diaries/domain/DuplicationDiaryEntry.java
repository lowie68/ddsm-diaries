/**
 * Duplication Diary Entry
 * 
 * The recording diary has 12 entries one for each month. Each entry has a Tuesday, Wednesday and Thursday.
 * A record is used to reduce boiler plate code.
 * 
 */
package org.stevie.ddsm.diaries.domain;

import java.time.LocalDate;
import java.time.Month;

public record DuplicationDiaryEntry(Month month, LocalDate bookingInDate, LocalDate barcodingDate, LocalDate duplicationDate) {

}
