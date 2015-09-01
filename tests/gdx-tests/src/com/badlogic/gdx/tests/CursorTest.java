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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;

/** Simple test case for mouse cursor change
 * Switch between two cursors every frame, a third cursor is used when a mouse button is pressed
 * @author haedri */
public class CursorTest extends GdxTest {
	Cursor cursor1;
	Cursor cursor2;
	Cursor cursor3;
	boolean cursorActive = false;

	public void create () {

		Pixmap pixmap1 = new Pixmap(Gdx.files.internal("data/bobargb8888-32x32.png"));
		cursor1 = Gdx.graphics.newCursor(pixmap1, 16, 16);
		
		Pixmap pixmap2 = new Pixmap(32, 32, Format.RGBA8888);
		pixmap2.setColor(Color.RED);
		pixmap2.fillCircle(16, 16, 8);
		cursor2 = Gdx.graphics.newCursor(pixmap2, 16, 16);
		
		Pixmap pixmap3 = new Pixmap(32, 32, Format.RGBA8888);
		pixmap3.setColor(Color.BLUE);
		pixmap3.fillCircle(16, 16, 8);
		cursor3 = Gdx.graphics.newCursor(pixmap3, 16, 16);

	}

	public void render () {
		// set the clear color and clear the screen.
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (Gdx.input.isTouched()) {
			Gdx.graphics.setCursor(cursor1);
		} else {
			cursorActive = !cursorActive;
			if (cursorActive) {
				Gdx.graphics.setCursor(cursor2);
			} else {
				Gdx.graphics.setCursor(cursor3);
			}
		}
	}
}
