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

package com.badlogic.gdx;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.transition.ScreenTransition;
import com.badlogic.gdx.utils.transition.TransitionListener;

/** An {@link Game} that delegates to a {@link Screen}. Allows to apply different transitions to screens. </p>
 * <p>
 * Screens are not disposed automatically. You must handle whether you want to keep screens around or dispose of them when another
 * screen is set.
 * @author iXeption */
public class FadingGame extends Game {

	protected FrameBuffer currentScreenFBO;
	protected FrameBuffer nextScreenFBO;
	protected Screen nextScreen;
	protected Batch batch;

	private List<TransitionListener> listerns;

	private float transitionDuration;
	private float currentTransitionTime;
	private boolean transitionRunning;
	private ScreenTransition screenTransition;

	public FadingGame (Batch batch) {
		this.batch = batch;
	}

	@Override
	public void create () {
		currentScreenFBO = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		nextScreenFBO = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		listerns = new ArrayList<TransitionListener>();
	}

	@Override
	public void dispose () {
		if (screen != null) screen.hide();
		if (nextScreen != null) nextScreen.hide();

	}

	@Override
	public void pause () {
		if (screen != null) screen.pause();
		if (nextScreen != null) nextScreen.pause();
	}

	@Override
	public void resume () {
		if (screen != null) screen.resume();
		if (nextScreen != null) nextScreen.resume();
	}

	@Override
	public void render () {

		float delta = Gdx.graphics.getDeltaTime();

		if (nextScreen == null) {
			// no other screen
			screen.render(delta);
		} else {
			if (transitionRunning && currentTransitionTime >= transitionDuration) {
				// is active and time limit reached
				this.screen.hide();
				this.screen = this.nextScreen;
				this.screen.resume();
				transitionRunning = false;
				this.nextScreen = null;
				notifyFinished();

			} else {
				// transition is active
				if (screenTransition != null) {
					currentScreenFBO.begin();
					this.screen.render(delta);
					currentScreenFBO.end();

					nextScreenFBO.begin();
					this.nextScreen.render(delta);
					nextScreenFBO.end();

					float percent = currentTransitionTime / transitionDuration;

					screenTransition.render(batch, currentScreenFBO.getColorBufferTexture(), nextScreenFBO.getColorBufferTexture(),
						percent);
					currentTransitionTime += delta;

				}

			}

		}

	}

	@Override
	public void resize (int width, int height) {
		boolean resize = currentScreenFBO.getHeight() != height || currentScreenFBO.getWidth() != width
			|| nextScreenFBO.getHeight() != height || nextScreenFBO.getWidth() != width;

		if (false) {
			currentScreenFBO.dispose();
			nextScreenFBO.dispose();

			currentScreenFBO = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
			nextScreenFBO = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
		}

		if (screen != null) screen.resize(width, height);
		if (nextScreen != null) nextScreen.resize(width, height);
	}

	/** Sets the current screen. {@link Screen#hide()} is called on any old screen, and {@link Screen#show()} is called on the new
	 * screen, if any.
	 * @param screen may be {@code null} */
	@Override
	public void setScreen (Screen screen) {
		if (transitionRunning) Gdx.app.log(FadingGame.class.getSimpleName(), "Changed Screen while transition in progress");
		screen.show();
		screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		if (this.screen == null) {
			this.screen = screen;
		} else {
			if (screenTransition == null) {
				this.screen.hide();
				this.screen = screen;
			} else {
				this.nextScreen = screen;
				this.screen.pause();
				this.nextScreen.pause();
				currentTransitionTime = 0;
				transitionRunning = true;
				notifyStarted();
			}

		}

	}

	/** Sets the {@link ScreenTransition} which is used. May be {@code null} to use instant switching.
	 * @param screenTransition may be {@code null}
	 * @param duration the transition duration in seconds
	 * @return {@code true} if successful false if transition is running */
	public boolean setTransition (ScreenTransition screenTransition, float duration) {
		if (transitionRunning) return false;
		this.screenTransition = screenTransition;
		this.transitionDuration = duration;
		return true;

	}

	/** @return the currently active {@link Screen}. */
	@Override
	public Screen getScreen () {
		return screen;
	}

	/** @return the next {@link Screen}. */
	public Screen getNextScreen () {
		return nextScreen;
	}

	/** @param listener to get transition events
	 * @return {@code true} if successful */
	public boolean addTransitionListener (TransitionListener listener) {
		return listerns.add(listener);
	}

	/** @param listener to remove
	 * @return {@code true} if successful */
	public boolean removeTransitionListener (TransitionListener listener) {
		return listerns.remove(listener);
	}

	private void notifyFinished () {
		for (TransitionListener transitionListener : listerns) {
			transitionListener.onTransitionFinished();
		}
	}

	private void notifyStarted () {
		for (TransitionListener transitionListener : listerns) {
			transitionListener.onTransitionStart();
		}
	}
}
