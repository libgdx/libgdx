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
/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.tests.utils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.GL31;
import com.badlogic.gdx.graphics.GL32;

public abstract class GdxTest extends InputAdapter implements ApplicationListener {
	/** Set by {@link #create(Application)}; prefer these over {@link com.badlogic.gdx.Gdx} statics. */
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
	public void create (Application app) {
		bind(app);
		create();
	}

	/** Binds module fields from the given application (also used when a test is started from a collection UI). */
	public void bind (Application app) {
		this.app = app;
		this.graphics = app.getGraphics();
		this.audio = app.getAudio();
		this.input = app.getInput();
		this.files = app.getFiles();
		this.net = app.getNet();
		refreshGl();
	}

	/** Refresh GL aliases from {@link #graphics} (e.g. after context recreation). */
	protected void refreshGl () {
		if (graphics == null) return;
		gl32 = graphics.getGL32();
		gl31 = gl32 != null ? gl32 : graphics.getGL31();
		gl30 = gl31 != null ? gl31 : graphics.getGL30();
		gl20 = gl30 != null ? gl30 : graphics.getGL20();
		gl = gl20;
	}

	public void create () {
	}

	public void resume () {
	}

	public void render () {
	}

	public void resize (int width, int height) {
	}

	public void pause () {
	}

	public void dispose () {
	}
}
