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
package com.badlogic.rtm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class LevelRenderer implements ApplicationListener {
	PerspectiveCamera camera;
	Texture tiles;
	Mesh floorMesh;
	Mesh wallMesh;
	SpriteBatch batch;
	BitmapFont font;
	ImmediateModeRenderer renderer;
	float angle = -90;

	@Override public void create () {
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = 1;
		camera.far = 2000;				

		batch = new SpriteBatch();
		font = new BitmapFont();
		load();
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
	}

	private void load () {
		try {
			tiles = new Texture(Gdx.files.internal("data/tiles-3.png"));
			tiles.setFilter(TextureFilter.MipMapLinearNearest, TextureFilter.Nearest);

			TextureAtlas atlas = new TextureAtlas();
			for (int i = 0; i < 12; i++) {
				TextureRegion region = new TextureRegion(tiles, i % 4 * 64 + 1, i / 4 * 64 + 1, 64, 64);
				atlas.addRegion("" + i, region);
			}
			float uSize = 62.0f / 256.0f;
			float vSize = 62.0f / 256.0f;

			BufferedReader reader = new BufferedReader(new InputStreamReader(Gdx.files.internal("data/level.map").read()));
			String line = reader.readLine();
			String tokens[] = line.split(",");
			camera.position.set(Float.parseFloat(tokens[0]), 0, Float.parseFloat(tokens[1]));
			int floors = Integer.parseInt(reader.readLine());
			int walls = Integer.parseInt(reader.readLine());
			float[] floorVertices = new float[floors * 20];
			float[] wallVertices = new float[walls * 20];
			short[] floorIndices = new short[floors * 6];
			short[] wallIndices = new short[walls * 6];

			int idx = 0;
			for (int i = 0, j = 0; i < floors; i++) {
				for (int k = 0; k < 4; k++) {
					tokens = reader.readLine().split(",");
					floorVertices[j++] = Float.parseFloat(tokens[0]);
					floorVertices[j++] = Float.parseFloat(tokens[1]);
					floorVertices[j++] = Float.parseFloat(tokens[2]);
					floorVertices[j++] = 0;
					floorVertices[j++] = 0;
				}

				short startIndex = (short)(i * 4);
				floorIndices[idx++] = startIndex;
				floorIndices[idx++] = (short)(startIndex + 1);
				floorIndices[idx++] = (short)(startIndex + 2);
				floorIndices[idx++] = (short)(startIndex + 2);
				floorIndices[idx++] = (short)(startIndex + 3);
				floorIndices[idx++] = startIndex;

				int type = Integer.parseInt(reader.readLine());
				String textureId = reader.readLine();
				TextureRegion region = atlas.findRegion(textureId);
				float u = region.getU();
				float v = region.getV();

				floorVertices[j - 2] = u + uSize;
				floorVertices[j - 1] = v;
				floorVertices[j - 2 - 5] = u + uSize;
				floorVertices[j - 1 - 5] = v + vSize;
				floorVertices[j - 2 - 10] = u;
				floorVertices[j - 1 - 10] = v + vSize;
				floorVertices[j - 2 - 15] = u;
				floorVertices[j - 1 - 15] = v;
			}

			idx = 0;
			short startIndex = 0;
			for (int i = 0, j = 0; i < walls; i++) {
				tokens = reader.readLine().split(",");
				if (!tokens[1].equals("0")) {
					for (int k = 0; k < 4; k++) {
						wallVertices[j++] = Float.parseFloat(tokens[0]);
						wallVertices[j++] = Float.parseFloat(tokens[1]);
						wallVertices[j++] = Float.parseFloat(tokens[2]);
						wallVertices[j++] = 0;
						wallVertices[j++] = 0;
						if (k < 3) tokens = reader.readLine().split(",");
					}

					wallIndices[idx++] = startIndex;
					wallIndices[idx++] = (short)(startIndex + 1);
					wallIndices[idx++] = (short)(startIndex + 2);
					wallIndices[idx++] = (short)(startIndex + 2);
					wallIndices[idx++] = (short)(startIndex + 3);
					wallIndices[idx++] = startIndex;
					startIndex += 4;

					int type = Integer.parseInt(reader.readLine());
					String textureId = reader.readLine();
					TextureRegion region = atlas.findRegion(textureId);
					float u = region.getU();
					float v = region.getV();

					wallVertices[j - 2] = u + uSize;
					wallVertices[j - 1] = v;
					wallVertices[j - 2 - 5] = u + vSize;
					wallVertices[j - 1 - 5] = v + vSize;
					wallVertices[j - 2 - 10] = u;
					wallVertices[j - 1 - 10] = v + vSize;
					wallVertices[j - 2 - 15] = u;
					wallVertices[j - 1 - 15] = v;
				} else {
					reader.readLine();
					reader.readLine();
					reader.readLine();

					int type = Integer.parseInt(reader.readLine());
					int textureId = Integer.parseInt(reader.readLine());
				}
			}

			floorMesh = new Mesh(true, floors * 4, floors * 6,
				new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"), new VertexAttribute(
					VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord"));
			floorMesh.setVertices(floorVertices);
			floorMesh.setIndices(floorIndices);

			wallMesh = new Mesh(true, walls * 4, walls * 6, new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord"));

			wallMesh.setVertices(wallVertices);
			wallMesh.setIndices(wallIndices);

			reader.close();
		} catch (IOException ex) {
			throw new GdxRuntimeException(ex);
		}
	}

	@Override public void resume () {
	}

	@Override public void render () {
		GL10 gl = Gdx.gl10;

		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		camera.update();
		camera.apply(gl);

		tiles.bind();
		gl.glColor4f(1, 1, 1, 1);
		floorMesh.render(GL10.GL_TRIANGLES);
		wallMesh.render(GL10.GL_TRIANGLES);

		batch.begin();
		batch.setBlendFunction(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond() + ", delta:" + Gdx.graphics.getDeltaTime(), 10, 25);
		batch.end();

		processInput();
	}

	private void processInput () {
		float delta = Gdx.graphics.getDeltaTime();
//
// if (Gdx.input.isKeyPressed(Keys.ANY_KEY)==false)
// Gdx.app.log("RTM", "No key pressed");

		if (Gdx.input.isKeyPressed(Keys.W)) camera.position.add(camera.direction.tmp().mul(80 * delta));
		if (Gdx.input.isKeyPressed(Keys.S)) camera.position.add(camera.direction.tmp().mul(-80 * delta));
		if (Gdx.input.isKeyPressed(Keys.A)) angle -= 90 * delta;
		if (Gdx.input.isKeyPressed(Keys.D)) angle += 90 * delta;

		if (Gdx.input.isTouched()) {
			float x = Gdx.input.getX();
			float y = Gdx.input.getY();
			if (x > Gdx.graphics.getWidth() / 2 + Gdx.graphics.getWidth() / 4) angle += 90 * delta;
			if (x < Gdx.graphics.getWidth() / 2 - Gdx.graphics.getWidth() / 4) angle -= 90 * delta;
			if (y > Gdx.graphics.getHeight() / 2 + Gdx.graphics.getHeight() / 4)
				camera.position.add(camera.direction.tmp().mul(80 * delta));
			if (y < Gdx.graphics.getHeight() / 2 - Gdx.graphics.getHeight() / 4)
				camera.position.add(camera.direction.tmp().mul(-80 * delta));
		}

		camera.direction.set((float)Math.cos(Math.toRadians(angle)), 0, (float)Math.sin(Math.toRadians(angle)));
	}

	@Override public void resize (int width, int height) {

	}

	@Override public void pause () {

	}

	@Override public void dispose () {

	}
}
