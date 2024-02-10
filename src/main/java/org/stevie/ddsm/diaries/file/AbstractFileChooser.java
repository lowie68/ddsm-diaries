/**
 * <h3>Abstract File Chooser</h3>
 * 
 * <p>This file is a wrapper for the {@link FileChooser} class. It corrects the out-dated
 * null return value if no file was selected and replaces it with a JDK 8 optional.
 * The only permitted sub-classes are {@link OpenFileChooser} and {@link SaveFileChooser}.
 * Common functionality is added at this level.</p>
 * 
 * @author Stephen
 * @version 1.0
 */
package org.stevie.ddsm.diaries.file;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Abstract File Chooser
 * 
 * Used as a base class for the standard file chooser dialogs which are dependent on the
 * host O/S.
 * 
 */
public abstract sealed class AbstractFileChooser permits OpenFileChooser, SaveFileChooser {
	
	/*
	 * the JavaFx FileChooser class. marked as protected so sub-classes can
	 * access it. 
	 */
	protected FileChooser chooser; 
	
	/*
	 * parent stage required by the show* methods.
	 */
	private Stage parentWindow;
	
	/**
	 * Copy Constructor
	 * 
	 * This constructor initialises the JavaFX FileChooser and sets properties that are
	 * common to the {@link SaveFileChooser} and {@link OpenFileChooser}

	 * @param parent window
	 * @since 1.0
	 */
	public AbstractFileChooser(Stage parentWindow) {
		/*
		 * create the object
		 */
		chooser = new FileChooser();
		/*
		 * only deal with text files
		 */
        chooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files", "*.txt"));
        
        /*
         * find the users home directory (C://Users/steph on my machine )
         */
        var userDir = System.getProperty("user.home");
        /*
         * find the path to the documents directory. Currently only works on
         * my machine. need's further thought.
         */
		var defaultDirecory = Paths.get(userDir, "OneDrive", "Documents");
        chooser.setInitialDirectory(defaultDirecory.toFile());

	}
	
	/**
	 * Parent Window Getter Method
	 * 
	 * Return the parent window property. Use a defensive copy to protect invariants
	 * @return the parent window of this file chooser
	 * @since 1.0
	 */
	public Stage getParentWindow() {
		return this.parentWindow;
	}
	
	/**
	 * Show Chooser Method
	 * 
	 * Implement this method to show the correct dialog.
	 * 
	 * @return the path selected by the user {@code Optional.empty()} if no file was selected
	 * @since 1.0
	 */
	public abstract Optional<Path> showChooser();
}
