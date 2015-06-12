/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *	 http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests;

import java.util.ArrayList;

import com.badlogic.gdx.FadingGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.transition.AlphaFadingTransition;
import com.badlogic.gdx.utils.transition.ColorFadeTransition;
import com.badlogic.gdx.utils.transition.RotatingTransition;
import com.badlogic.gdx.utils.transition.RotatingTransition.TransitionScaling;
import com.badlogic.gdx.utils.transition.ScreenTransition;
import com.badlogic.gdx.utils.transition.SlicingTransition;
import com.badlogic.gdx.utils.transition.SlidingTransition;
import com.badlogic.gdx.utils.transition.TransitionListener;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

public class ScreenTransitionTest extends GdxTest {

	final String tag = ScreenTransitionTest.class.getSimpleName();

	FadingGame fadingGame;
	Screen firstScreen;
	Screen secondScreen;

	boolean toggle = false;
	int index = 0;

	ArrayList<ScreenTransition> transitions = new ArrayList<ScreenTransition>();

	public class SimpleScreen extends ScreenAdapter {

		Stage simpleStage;
		String name;
		boolean paused = false;

		public SimpleScreen (String name, String bgImage, final float offset) {
			this.name = name;
			simpleStage = new Stage(new ScalingViewport(Scaling.fill, 640, 480));
			final TextureRegion region = new TextureRegion(new Texture("data/badlogic.jpg"));
			final Actor actor = new Actor() {

				public void draw (Batch batch, float parentAlpha) {
					Color color = getColor();
					batch.setColor(color.r, color.g, color.b, parentAlpha);
					batch.draw(region, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(),
						getRotation());
				}

				@Override
				public void act (float delta) {
					if (!paused) {
						setPosition(getX() + 50 * delta, offset);
						if (getX() > Gdx.graphics.getWidth()) setX(0);
					}
				}
			};

			actor.setBounds(15, 15, 200, 200);
			actor.setOrigin(50, 50);

			final TextureRegion regionBG = new TextureRegion(new Texture(bgImage));
			final Actor bg = new Actor() {

				public void draw (Batch batch, float parentAlpha) {
					Color color = getColor();
					batch.setColor(color.r, color.g, color.b, parentAlpha);
					batch.draw(regionBG, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(),
						getScaleY(), getRotation());
				}
			};

			bg.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

			simpleStage.addActor(bg);
			simpleStage.addActor(actor);

			simpleStage.addListener(new ClickListener(Input.Buttons.LEFT) {
				@Override
				public void clicked (InputEvent event, float x, float y) {
					Gdx.app.debug(tag, "Toggle Screen");
					fadingGame.setScreen(getNextScreen());

				}
			});

			simpleStage.addListener(new ClickListener(Input.Buttons.RIGHT) {
				@Override
				public void clicked (InputEvent event, float x, float y) {
					Gdx.app.debug(tag, "Change Transition");
					if (fadingGame.setTransition(getNextTransition(), 2))
						Gdx.app.debug(tag, "Sucess");
					else
						Gdx.app.error(tag, "failed");

				}
			});

		}

		@Override
		public void render (float delta) {
			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			simpleStage.act(delta);
			simpleStage.draw();
		}

		@Override
		public void resize (int width, int height) {
			Gdx.app.debug(tag, "Resize W:" + width + " H: " + height + " " + name);
			simpleStage.getViewport().update(width, height, true);
		}

		@Override
		public void show () {
			Gdx.app.debug(tag, "Show " + name);
			Gdx.input.setInputProcessor(simpleStage);
		}

		@Override
		public void hide () {
			Gdx.app.debug(tag, "Hide " + name);
		}

		@Override
		public void resume () {
			Gdx.app.debug(tag, "Resume " + name);
			paused = false;

		}

		@Override
		public void pause () {
			Gdx.app.debug(tag, "Pause " + name);
			paused = true;
		}

	}

	@Override
	public void create () {
		Gdx.app.setLogLevel(Logger.DEBUG);
		fadingGame = new FadingGame(new SpriteBatch());
		fadingGame.create();
		firstScreen = new SimpleScreen("Screen1", "data/stones.jpg", 10);
		secondScreen = new SimpleScreen("Screen2", "data/planet_earth.png", 200);
		transitions.add(new AlphaFadingTransition());
		transitions.add(new SlidingTransition(SlidingTransition.Direction.LEFT, Interpolation.linear, true));
		transitions.add(new SlidingTransition(SlidingTransition.Direction.UP, Interpolation.bounce, false));
		transitions.add(new SlicingTransition(SlicingTransition.Direction.UPDOWN, 128, Interpolation.pow4));
		transitions.add(new SlicingTransition(SlicingTransition.Direction.DOWN, 8, Interpolation.bounce));
		transitions.add(new RotatingTransition(Interpolation.pow2Out, 720, TransitionScaling.IN));
		transitions.add(new RotatingTransition(Interpolation.bounce, 360, TransitionScaling.IN));
		transitions.add(new ColorFadeTransition(Color.WHITE, Interpolation.sine));

		fadingGame.addTransitionListener(new TransitionListener() {

			@Override
			public void onTransitionStart () {
				Gdx.app.debug(tag, "TransitionListener: Start detected");

			}

			@Override
			public void onTransitionFinished () {
				Gdx.app.debug(tag, "TransitionListener: Finish detected");

			}
		});

		fadingGame.setScreen(firstScreen);

	}

	@Override
	public void render () {
		fadingGame.render();
	}

	@Override
	public void resize (int width, int height) {
		fadingGame.resize(width, height);
	}

	@Override
	public void dispose () {
		fadingGame.dispose();
	}

	@Override
	public void pause () {
		fadingGame.pause();
	}

	@Override
	public void resume () {
		fadingGame.resume();
	}

	Screen getNextScreen () {
		toggle = !toggle;
		if (toggle) {
			return secondScreen;
		} else
			return firstScreen;

	}

	ScreenTransition getNextTransition () {
		if (++index >= transitions.size()) index = 0;
		return transitions.get(index);

	}

}
