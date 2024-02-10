/**
 * <h3>Open File Chooser Class</h3>
 * <p>This class is an abstraction around the standard O/S open file dialog. It corrects
 * the return value from the open dialog to an optional. If there is no selected file 
 * it returns an empty optional. It also converts the File return value to the modern NIO Path.</p>
 * 
 * @author Stephen
 * @version 1.0
 */
package org.stevie.ddsm.diaries.file;

import java.nio.file.Path;
import java.util.Optional;

import javafx.stage.Stage;

/**
 * Open File Chooser Class
 * 
 * This class wraps the {@link FileChooser} class. It corrects deficiencies in the 
 * standard API.
 */
public final class OpenFileChooser extends AbstractFileChooser {

	/**
	 * Copy Constructor
	 * Calls the parent constructor and set properties to configure the {@link FileChooser}
	 * 
	 * @param parent window
	 * @since 1.0
	 */
	public OpenFileChooser(Stage parentWindow) {
		super(parentWindow);
		chooser.setTitle("Import Bank Holidays");
	}

	/**
	 * Show Chooser Method
	 * 
	 * Shows the correct chooser and returns the path of the selected file. If the user
	 * does'nt select a file or cancels the dialog an empty optional is returned.
	 * 
	 * @return selected path empty if none selected
	 * @since 1.0
	 */
	@Override
	public Optional<Path> showChooser() {
		var file = chooser.showOpenDialog(getParentWindow()); 
		if (file == null) {
			return Optional.empty();
		}
		return Optional.of(file.toPath());
	}

}
