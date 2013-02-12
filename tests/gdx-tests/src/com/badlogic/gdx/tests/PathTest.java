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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

/** @author Xoppa */
public class PathTest extends GdxTest {
	SpriteBatch spriteBatch;
	Sprite obj;
	Array<Path<Vector2>> paths = new Array<Path<Vector2>>();
	int currentPath = 0;
	float t;
	float speed = 0.3f;
	float wait = 0f;

	@Override
	public boolean needsGL20 () {
		return false;
	}
	
	@Override
	public void create () {
		spriteBatch = new SpriteBatch();
		obj = new Sprite(new Texture(Gdx.files.internal("data/badlogicsmall.jpg")));
		
		float w = Gdx.graphics.getWidth() - obj.getWidth();
		float h = Gdx.graphics.getHeight() - obj.getHeight();
		
		paths.add(new Bezier<Vector2>(new Vector2(0,0), new Vector2(w, h)));
		paths.add(new Bezier<Vector2>(new Vector2(0,0), new Vector2(0, h), new Vector2(w, h)));
		paths.add(new Bezier<Vector2>(new Vector2(0,0), new Vector2(w, 0), new Vector2(0, h), new Vector2(w, h)));
		
		Gdx.input.setInputProcessor(this);
	}
	
	final Vector2 tmpV = new Vector2();
	@Override
	public void render () {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		if (wait > 0)
			wait -= Gdx.graphics.getDeltaTime();
		else {
			t += speed * Gdx.graphics.getDeltaTime();
			while (t >= 1f) {
				currentPath = (currentPath + 1) % paths.size;
				t -= 1f;
			}
			
			paths.get(currentPath).valueAt(tmpV, t);
			obj.setPosition(tmpV.x, tmpV.y);
		}
		
		spriteBatch.begin();
		obj.draw(spriteBatch);
		spriteBatch.end();
	}
	
	private void touch(int x, int y) {
		t = paths.get(currentPath).approximate(tmpV.set(x, Gdx.graphics.getHeight()-y));
		paths.get(currentPath).valueAt(tmpV, t);
		obj.setPosition(tmpV.x, tmpV.y);
		wait = 2f;		
	}
	
	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		touch(screenX, screenY);
		return super.touchUp(screenX, screenY, pointer, button);
	}
	
	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		touch(screenX, screenY);
		return super.touchDragged(screenX, screenY, pointer);
	}
}
