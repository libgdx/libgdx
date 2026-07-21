
package com.badlogic.gdx.tests;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.ScreenUtils;

public class GLES30Test extends GdxTest {

	SpriteBatch batch;
	Texture texture;
	ShaderProgram shaderProgram;

	@Override
	public void create () {
		app.log("GLES30Test", "GL_VERSION = " + gl.glGetString(GL20.GL_VERSION));
		batch = new SpriteBatch();
		texture = new Texture(files.internal("data/badlogic.jpg"));
		shaderProgram = new ShaderProgram(files.internal("data/shaders/gles30sprite.vert"),
			files.internal("data/shaders/gles30sprite.frag"));
		app.log("GLES30Test", shaderProgram.getLog());
		if (shaderProgram.isCompiled()) {
			app.log("GLES30Test", "Shader compiled");
			batch.setShader(shaderProgram);
		}
	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);

		batch.begin();
		batch.draw(texture, 0, 0, graphics.getWidth() / 2f, graphics.getHeight() / 2f);
		batch.end();
	}

	@Override
	public void dispose () {
		texture.dispose();
		batch.dispose();
		shaderProgram.dispose();
	}
}
