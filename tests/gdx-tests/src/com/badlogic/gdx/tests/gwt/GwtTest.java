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

package com.badlogic.gdx.tests.gwt;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class GwtTest extends GdxTest {
	ShaderProgram shader;
	Mesh mesh;
	Matrix4 matrix = new Matrix4();
	SpriteBatch batch;
	Texture texture;
	List<Vector2> positions;
	Sprite sprite;
	BitmapFont font;
	BitmapFontCache cache;
	TextureAtlas atlas;
	int numSprites;

	@Override
	public void create () {
		Preferences pref = Gdx.app.getPreferences("test");
		boolean resultb = pref.getBoolean("test");
		int resulti = pref.getInteger("test");

		shader = new ShaderProgram(Gdx.files.internal("data/shaders/shader-vs.glsl"),
			Gdx.files.internal("data/shaders/shader-fs.glsl"));
		if (!shader.isCompiled()) throw new GdxRuntimeException(shader.getLog());
		mesh = new Mesh(VertexDataType.VertexBufferObject, true, 6, 0, VertexAttribute.Position(), VertexAttribute.TexCoords(0));
		mesh.setVertices(new float[] {-0.5f, -0.5f, 0, 0, 1, 0.5f, -0.5f, 0, 1, 1, 0.5f, 0.5f, 0, 1, 0, 0.5f, 0.5f, 0, 1, 0, -0.5f,
			0.5f, 0, 0, 0, -0.5f, -0.5f, 0, 0, 1});

		texture = new Texture(new Pixmap(Gdx.files.internal("data/badlogic.jpg")), true);
		texture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);

		String params = Gdx.files.internal("data/gwttestparams.txt").readString();
		numSprites = Integer.parseInt(params);

		batch = new SpriteBatch();
		positions = new ArrayList<Vector2>();
		for (int i = 0; i < numSprites; i++) {
			positions.add(new Vector2(MathUtils.random() * Gdx.graphics.getWidth(), MathUtils.random() * Gdx.graphics.getHeight()));
		}
		sprite = new Sprite(texture);
		sprite.setSize(64, 64);
		sprite.setOrigin(32, 32);

		font = new BitmapFont(Gdx.files.internal("data/arial-15.fnt"), false);
		cache = font.newFontCache();
		cache.setColor(Color.RED);
		cache.setText("This is a Test", 0, 0);

		atlas = new TextureAtlas(Gdx.files.internal("data/pack"));
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		texture.bind(0);
		shader.begin();
		shader.setUniformMatrix("u_projView", matrix);
		shader.setUniformi("u_texture", 0);
		mesh.render(shader, GL20.GL_TRIANGLES);
		shader.end();

		batch.begin();
		batch.draw(atlas.findRegion("font"), 0, 100);
		sprite.rotate(Gdx.graphics.getDeltaTime() * 45);
		for (Vector2 position : positions) {
			sprite.setPosition(position.x, position.y);
			sprite.draw(batch);
		}
		font.draw(batch, "fps:" + Gdx.graphics.getFramesPerSecond() + ", delta: " + Gdx.graphics.getDeltaTime() + ", #sprites: "
			+ numSprites, 0, 30);
		cache.setPosition(200, 200);
		cache.draw(batch);
		batch.end();
	}

	@Override
	public void resume () {
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
