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
package com.badlogic.gdx.backends.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class GdxGwtTest extends GwtApplication implements ApplicationListener {
	@Override
	public GwtApplicationConfiguration getConfig () {
		return new GwtApplicationConfiguration(500, 500);
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return this;
	}

	@Override
	public void create () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, (float)Math.random(), 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		Gdx.app.log("GdxGwtTest", "fps:" + Gdx.graphics.getFramesPerSecond() + ", " + Gdx.graphics.getDeltaTime());
	}

	@Override
	public void resize (int width, int height) {
	}

	@Override
	public void pause () {
	}

	@Override
	public void dispose () {
	}
}