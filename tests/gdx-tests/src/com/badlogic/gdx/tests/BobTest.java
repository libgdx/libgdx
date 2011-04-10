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

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.tests.utils.GdxTest;

public class BobTest extends GdxTest {

	@Override public boolean needsGL20 () {
		return false;
	}

	static final int NUM_BOBS = 100;
	Texture bobTexture;
	Mesh bobModel;
	Bob[] bobs;

	public void create () {
		bobTexture = new Texture(Gdx.files.internal("data/bobargb8888-32x32.png"));

		bobModel = new Mesh(true, 4, 6, new VertexAttribute(Usage.Position, 2, "a_pos"), new VertexAttribute(
			Usage.TextureCoordinates, 2, "a_tex"));
		bobModel.setVertices(new float[] {-16, -16, 0, 1, 16, -16, 1, 1, 16, 16, 1, 0, -16, 16, 0, 0,}, 0, 16);
		bobModel.setIndices(new short[] {0, 1, 2, 2, 3, 0}, 0, 6);
		bobModel.setAutoBind(false);

		bobs = new Bob[100];
		for (int i = 0; i < 100; i++) {
			bobs[i] = new Bob();
		}
	}

	@Override public void render () {
		float deltaTime = Math.min(Gdx.graphics.getDeltaTime(), 0.1f);
		for (int i = 0; i < NUM_BOBS; i++) {
			bobs[i].update(deltaTime);
		}

		GL10 gl = Gdx.gl10;
		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glClearColor(1, 0, 0, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrthof(0, 320, 0, 480, 1, -1);

		gl.glMatrixMode(GL10.GL_MODELVIEW);

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		gl.glEnable(GL10.GL_TEXTURE_2D);
		bobTexture.bind();

		bobModel.bind();
		for (int i = 0; i < NUM_BOBS; i++) {
			gl.glLoadIdentity();
			gl.glTranslatef(Math.round(bobs[i].x), Math.round(bobs[i].y), 0);
			System.out.println(Math.round(bobs[i].x) + ", " + (int)bobs[i].x);
			bobModel.render(GL10.GL_TRIANGLES, 0, 6);
		}
		bobModel.unbind();
	}

	static class Bob {
		static final Random rand = new Random();
		public float x, y;
		float dirX, dirY;

		public Bob () {
			x = rand.nextFloat() * 320;
			y = rand.nextFloat() * 480;
			dirX = 10;
			dirY = 10;
		}

		public void update (float deltaTime) {
			x = x + dirX * deltaTime;
			y = y + dirY * deltaTime;

			if (x < 0) {
				dirX = -dirX;
				x = 0;
			}

			if (x > 320) {
				dirX = -dirX;
				x = 320;
			}

			if (y < 0) {
				dirY = -dirY;
				y = 0;
			}

			if (y > 480) {
				dirY = -dirY;
				y = 480;
			}
		}
	}
}
