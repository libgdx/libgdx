
package com.badlogic.gdx.tests.lwjgl;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.TimeUtils;

public class BugfixTest extends Game {

	@Override
	public void create () {
		setScreen(new ScreenTest());
	}

	@Override
	public void dispose () {
		super.dispose();
	}

	@Override
	public void pause () {
		super.pause();
	}

	@Override
	public void resume () {
		super.resume();
	}

	@Override
	public void render () {
		super.render();
	}

	@Override
	public void resize (int width, int height) {
		super.resize(width, height);
	}

	public void setScreen (Screen screen) {
		System.out.println("setScreen");
		super.setScreen(screen);
	}

	private class ScreenTest implements Screen {

		private Stage stage;

		@Override
		public void render (float delta) {
			Gdx.gl.glClearColor(0, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

			stage.act(delta);
			stage.draw();
		}

		@Override
		public void resize (int width, int height) {
			stage.setViewport(width, height, false);
		}

		@Override
		public void show () {
			stage = new Stage();

			Gdx.input.setInputProcessor(stage);

			TextButtonStyle tbs = new TextButtonStyle();
			tbs.font = new BitmapFont();
			tbs.font.setScale(7);
			tbs.fontColor = Color.WHITE;
			tbs.overFontColor = Color.YELLOW;
			tbs.downFontColor = Color.RED;

			TextButton button = new TextButton("test", tbs);
			button.setPosition(stage.getWidth() / 2 - button.getWidth() / 2, stage.getHeight() / 2 - button.getHeight() / 2);
			button.addListener(new ClickListener() {

				@Override
				public void clicked (InputEvent event, float x, float y) {
					System.out.println("test @ " + TimeUtils.millis());
					// if you don't put the Runnable in a sequence, it ALWAYS works
					stage.addAction(Actions.sequence(Actions.run(new Runnable() {

						@Override
						public void run () {
							((Game)Gdx.app.getApplicationListener()).setScreen(new ScreenTest());
						}
					})));
				}
			});

			stage.addActor(button);

			// if you use this, it ALWAYS works
			// stage.addAction(Actions.moveTo(-stage.getWidth(), 0));
			// stage.addAction(Actions.moveTo(0, 0, .5f));

			// if you use this sequence, it works only SOMETIMES if a sequence is used in the button's listener (see comment there)
			// therefore, I think it has something to do with Actions#sequence(Action...) / SequenceAction
			stage.addAction(Actions.sequence(Actions.moveTo(-stage.getWidth(), 0), Actions.moveTo(0, 0, .5f)));
		}

		@Override
		public void hide () {
			dispose();
		}

		@Override
		public void pause () {
		}

		@Override
		public void resume () {
		}

		@Override
		public void dispose () {
			stage.dispose();
		}

	}

	public static void main (String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "SomeLibgdxTests";
		cfg.vSyncEnabled = true;
		cfg.useGL20 = true;
		cfg.width = 1280;
		cfg.height = 720;

		new LwjglApplication(new BugfixTest(), cfg);
	}

}
