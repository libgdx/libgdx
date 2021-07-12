/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests.lwjgl3;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3AWTCanvas;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3AWTFrame;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.tests.utils.CommandLineOptions;
import com.badlogic.gdx.tests.utils.GdxTests;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class Lwjgl3AwtTestStarter {

	static CommandLineOptions options;

	/**
	 * Runs libgdx tests.
	 * 
	 * some options can be passed, see {@link CommandLineOptions}
	 * 
	 * @param argv command line arguments
	 */
	public static void main (String[] argv) {
		// System.setProperty("java.awt.headless", "true");

		options = new CommandLineOptions(argv);

		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		if (options.gl30) {
			config.useOpenGL3(true, 3, 2);
		}

		// new Lwjgl3Application(new TestChooser(), config);
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Lwjgl3AWTCanvas lwjgl3AWTCanvas = new Lwjgl3AWTCanvas(new TestChooser(), config);
		lwjgl3AWTCanvas.getCanvas().setPreferredSize(new Dimension(640, 480));
		frame.getContentPane().add(new JLabel("North"), BorderLayout.NORTH);
		frame.getContentPane().add(lwjgl3AWTCanvas.getCanvas(), BorderLayout.CENTER);
		frame.getContentPane().add(new JLabel("South"), BorderLayout.SOUTH);
		frame.setVisible(true);
		frame.pack();
	}

	static class TestChooser extends ApplicationAdapter {
		private Stage stage;
		private Skin skin;
		TextButton lastClickedTestButton;

		public void create () {
			final Preferences prefs = Gdx.app.getPreferences("lwjgl3-tests");

			stage = new Stage(new ScreenViewport());
			Gdx.input.setInputProcessor(stage);
			skin = new Skin(Gdx.files.internal("data/uiskin.json"));

			Table container = new Table();
			stage.addActor(container);
			container.setFillParent(true);

			Table table = new Table();

			ScrollPane scroll = new ScrollPane(table, skin);
			scroll.setSmoothScrolling(false);
			scroll.setFadeScrollBars(false);
			stage.setScrollFocus(scroll);

			int tableSpace = 4;
			table.pad(10).defaults().expandX().space(tableSpace);
			for (final String testName : GdxTests.getNames()) {
				final TextButton testButton = new TextButton(testName, skin);
				testButton.setDisabled(!options.isTestCompatible(testName));
				testButton.setName(testName);
				table.add(testButton).fillX();
				table.row();
				testButton.addListener(new ChangeListener() {
					@Override
					public void changed (ChangeEvent event, Actor actor) {
						ApplicationListener test = GdxTests.newTest(testName);
						Lwjgl3AWTFrame frame = new Lwjgl3AWTFrame(test, testName, 640, 480);
						frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
						// winConfig.setWindowPosition(((Lwjgl3Graphics)Gdx.graphics).getWindow().getPositionX() + 40,
						// ((Lwjgl3Graphics)Gdx.graphics).getWindow().getPositionY() + 40);
						frame.setVisible(true);
						System.out.println("Started test: " + testName);
						prefs.putString("LastTest", testName);
						prefs.flush();
						if (testButton != lastClickedTestButton) {
							testButton.setColor(Color.CYAN);
							if (lastClickedTestButton != null) {
								lastClickedTestButton.setColor(Color.WHITE);
							}
							lastClickedTestButton = testButton;
						}
					}
				});
			}

			container.add(scroll).expand().fill();
			container.row();

			lastClickedTestButton = (TextButton)table.findActor(prefs.getString("LastTest"));
			if (lastClickedTestButton != null) {
				lastClickedTestButton.setColor(Color.CYAN);
				scroll.layout();
				float scrollY = lastClickedTestButton.getY() + scroll.getScrollHeight() / 2 + lastClickedTestButton.getHeight() / 2
					+ tableSpace * 2 + 20;
				scroll.scrollTo(0, scrollY, 0, 0, false, false);

				// Since ScrollPane takes some time for scrolling to a position, we just "fake" time
				stage.act(1f);
				stage.act(1f);
				stage.draw();
			}
		}

		@Override
		public void render () {
			Gdx.gl.glClearColor(0, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			stage.act();
			stage.draw();
		}

		@Override
		public void resize (int width, int height) {
			stage.getViewport().update(width, height, true);
		}

		@Override
		public void dispose () {
			skin.dispose();
			stage.dispose();
		}
	}
}
