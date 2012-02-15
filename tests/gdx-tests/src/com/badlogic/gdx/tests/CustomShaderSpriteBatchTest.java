package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.tests.utils.GdxTest;

public class CustomShaderSpriteBatchTest extends GdxTest {
	SpriteBatch batch;
	ShaderProgram shader;
	Texture texture;

	@Override
	public void create () {
		batch = new SpriteBatch(10);
		ShaderProgram.pedantic = false;
		shader = new ShaderProgram(Gdx.files.internal("data/shaders/batch.vert").readString(),
											Gdx.files.internal("data/shaders/batch.frag").readString());
		batch.setShader(shader);
		texture = new Texture("data/badlogic.jpg");
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(texture, 0, 0);
		batch.end();
	}

	@Override
	public boolean needsGL20 () {
		return true;
	}
}
