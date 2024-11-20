
package com.badlogic.gdx.tests.lwjgl3;

import java.awt.image.BufferedImage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

/** Tests for GLFW's drop callback.
 * 
 * External files (e.g from the desktop) can be dragged into the GLFW window.
 * 
 * This test uses libGDX to manage the UI and event handling. When files are dropped, their paths are shown on the screen as
 * labels.
 * @author mbrlabs */
public class DragNDropTest extends GdxTest {

	private Skin skin;
	private Stage stage;
	private Table root;

	/** Initializes the test by setting up the stage, skin, and input processor. Creates a root Table that will hold the labels for
	 * dropped files. */
	@Override
	public void create () {
		// Create a simple image (not used but initialized for testing)
		BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_4BYTE_ABGR);
		// Set up the stage for UI components
		stage = new Stage();
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		// Set the input processor to the stage for handling input events
		Gdx.input.setInputProcessor(stage);
		// Initialize the root Table to hold file labels
		root = new Table();
		root.setFillParent(true); // Make the table fill the entire stage
		root.align(Align.left | Align.top); // Align the table's contents to the top-left
		stage.addActor(root); // Add the root table to the stage
	}

	/** Renders the UI and updates the stage. This method is called every frame.
	 * 
	 * @see com.badlogic.gdx.ApplicationListener#render() */
	@Override
	public void render () {
		ScreenUtils.clear(1, 0, 0, 1);

		stage.act();
		stage.draw();
	}

	/** This method is called when the window is resized. It is not used in this test, but it must be implemented to fulfill the
	 * ApplicationListener interface.
	 * 
	 * @param width The new width of the window.
	 * @param height The new height of the window. */
	@Override
	public void resize (int width, int height) {
	}

	/** This method is called when the application is resumed from a paused state. It is not used in this test, but it must be
	 * implemented to fulfill the ApplicationListener interface. */
	@Override
	public void resume () {
	}

	/** This method is called when the application is paused. It is not used in this test, but it must be implemented to fulfill
	 * the ApplicationListener interface. */
	@Override
	public void pause () {
	}

	/** This method is called when the application is disposed of (e.g., when the window is closed). It is not used in this test,
	 * but it must be implemented to fulfill the ApplicationListener interface. */
	@Override
	public void dispose () {
	}

	/** Adds file paths to the UI as labels within the root table. Each file path dropped into the window is displayed as a label
	 * in the window.
	 * 
	 * @param files An array of file paths to display. */
	public void addFiles (String[] files) {
		for (String file : files) {
			root.add(new Label(file, skin)).left().row();
		}
	}

	/** The entry point of the application. Configures the window and creates the test application. Sets up the GLFW window and
	 * registers a file drop callback that adds dropped files to the UI.
	 * 
	 * @param argv Command-line arguments (not used).
	 * @throws NoSuchFieldException If there is an issue with reflection.
	 * @throws SecurityException If there is a security issue.
	 * @throws ClassNotFoundException If the application class cannot be found. */
	public static void main (String[] argv) throws NoSuchFieldException, SecurityException, ClassNotFoundException {
		final DragNDropTest test = new DragNDropTest();

		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(640, 480);
		config.setTitle("Drag files in this window");
		config.setWindowListener(new Lwjgl3WindowAdapter() {
			@Override
			public void filesDropped (String[] files) {
				for (String file : files) {
					Gdx.app.log("GLWF Drop", file);
				}
				test.addFiles(files);
			}

		});

		// Create the application instance with the configured settings
		new Lwjgl3Application(test, config);
	}

}
