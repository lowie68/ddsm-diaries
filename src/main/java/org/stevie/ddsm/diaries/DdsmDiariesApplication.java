/**
 * 
 * DDSM Dairy Application
 * 
 * DDSM stands for Diocesan Digest Sound Magazine which is a sound magazine published once a month for blind people in the 
 * diocese of Lichfield. The magazine is based in Lichfield Cathedral. This project is a small utility tool used for 
 * generating two annual diaries scheduling due dates for recording and duplication of the sound magazine.
 * 
 */
package org.stevie.ddsm.diaries;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javafx.application.Application;

@SpringBootApplication
@ComponentScan(basePackages = {"org.stevie.ddsm.diaries", "org.stevie.ddsm.diaries.controllers", "org.stevie.ddsm.diaries.service.internet","org.stevie.ddsm.diaries.service.month", "org.stevie.ddsm.diaries.service.bank"})
public class DdsmDiariesApplication {
	public static void main(String[] args) {
		Application.launch(DiaryApplicationFX.class, args);
	}

}
