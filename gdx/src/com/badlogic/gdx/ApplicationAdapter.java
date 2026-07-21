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

package com.badlogic.gdx;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.GL31;
import com.badlogic.gdx.graphics.GL32;

/** Convenience implementation of {@link ApplicationListener}. Derive from this and only override what you need.
 * @author mzechner */
public abstract class ApplicationAdapter implements ApplicationListener {
	/** Set by {@link #create(Application)}; prefer these over {@link Gdx} statics. */
	protected Application app;
	protected Graphics graphics;
	protected Audio audio;
	protected Input input;
	protected Files files;
	protected Net net;
	protected GL20 gl;
	protected GL20 gl20;
	protected GL30 gl30;
	protected GL31 gl31;
	protected GL32 gl32;

	@Override
	public void create () {
	}

	@Override
	public void create (Application app) {
		bind(app);
		create();
	}

	protected void bind (Application app) {
		this.app = app;
		this.graphics = app.getGraphics();
		this.audio = app.getAudio();
		this.input = app.getInput();
		this.files = app.getFiles();
		this.net = app.getNet();
		refreshGl();
	}

	protected void refreshGl () {
		if (graphics == null) return;
		gl32 = graphics.getGL32();
		gl31 = gl32 != null ? gl32 : graphics.getGL31();
		gl30 = gl31 != null ? gl31 : graphics.getGL30();
		gl20 = gl30 != null ? gl30 : graphics.getGL20();
		gl = gl20;
	}

	@Override
	public void resize (int width, int height) {
	}

	@Override
	public void render () {
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void dispose () {
	}
}
