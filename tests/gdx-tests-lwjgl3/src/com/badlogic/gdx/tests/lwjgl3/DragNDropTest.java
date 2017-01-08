package com.badlogic.gdx.tests.lwjgl3;

import java.awt.image.BufferedImage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Tests for GLFW's drop callback.
 * 
 * External files (e.g from the desktop) can be dragged into the GLFW window.
 * 
 * @author mbrlabs
 */
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
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
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
	
	public void addFiles(String[] files) {
		for(String file : files) {
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
				for(String file : files) {					
					Gdx.app.log("GLWF Drop", file);
				}
				test.addFiles(files);
			}
			
		});

		new Lwjgl3Application(test, config);
	}
	
}