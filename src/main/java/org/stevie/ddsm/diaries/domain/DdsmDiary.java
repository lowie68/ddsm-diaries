
/**
 * DDSM Diary Interface
 * 
 * Recording Diary and DuplicatioDiary inherit from this interface
 */
package org.stevie.ddsm.diaries.domain;

public interface DdsmDiary {
	public void generateDiary();
	public void printDiaryToConsole();
	int getDiaryYear();
}
