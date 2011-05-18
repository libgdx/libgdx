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
package com.badlogicgames.superjumper;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;

public abstract class Game implements ApplicationListener {
	Screen screen;
	
	public void setScreen (Screen helpScreen2) {
		screen.pause();
		screen.dispose();
		screen = helpScreen2;
	}
	
	public abstract Screen getStartScreen();
	
	@Override public void create () {
		screen = getStartScreen();
	}

	@Override public void resume () {
		screen.resume();
	}

	@Override public void render () {
		screen.update(Gdx.graphics.getDeltaTime());
		screen.present(Gdx.graphics.getDeltaTime());
	}

	@Override public void resize (int width, int height) {
		
	}

	@Override public void pause () {
		screen.pause();
	}

	@Override public void dispose () {
		screen.dispose();
	}
}
