
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
 * @author mbrlabs */
public class DragNDropTest extends GdxTest {

	private Skin skin;
	private Stage stage;
	private Table root;

	@Override
	public void create () {
		BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_4BYTE_ABGR);
		stage = new Stage();
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		Gdx.input.setInputProcessor(stage);
		root = new Table();
		root.setFillParent(true);
		root.align(Align.left | Align.top);
		stage.addActor(root);
	}

	@Override
	public void render () {
		ScreenUtils.clear(1, 0, 0, 1);

		stage.act();
		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
	}

	@Override
	public void resume () {
	}

	@Override
	public void pause () {
	}

	@Override
	public void dispose () {
	}

	public void addFiles (String[] files) {
		for (String file : files) {
			root.add(new Label(file, skin)).left().row();
		}
	}

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

		new Lwjgl3Application(test, config);
	}

}
