/**
 * <h3>Duplication Diary Entry Record</h3>
 * 
 * <p>The duplication diary has 12 entries one for each month. Each entry has a Tuesday, Wednesday and 
 * Thursday. The record keyword is used to reduce boiler plate code and make the class immutable.</p>
 * 
 * @author Stephen
 * @version 1.0
 */
package org.stevie.ddsm.diaries.domain;

import java.time.LocalDate;
import java.time.Month;

/**
 * Duplication Diary Entry Record
 */
public record DuplicationDiaryEntry(Month month, LocalDate collectDate, LocalDate barcodingDate, LocalDate duplicationDate) {

}
