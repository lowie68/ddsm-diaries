
/**
 * DDSM Diary Interface
 * 
 * Recording Diary and DuplicatioDiary inherit from this interface
 */
package org.stevie.ddsm.diaries.domain;

public sealed interface DdsmDiary permits RecordingDiary, DuplicationDiary {
	public void printDiaryToConsole();
	int getDiaryYear();
}
