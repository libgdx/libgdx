package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.tests.utils.GdxTest;


public class GLES30Test extends GdxTest {

    SpriteBatch batch;
    Texture texture;
    ShaderProgram shaderProgram;

    @Override
    public void create() {
        batch = new SpriteBatch();
        texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
        shaderProgram = new ShaderProgram(Gdx.files.internal("data/shaders/gles30sprite.vert"), Gdx.files.internal("data/shaders/gles30sprite.frag"));
        if (!shaderProgram.isCompiled()) {
            Gdx.app.log("GLES30Test", shaderProgram.getLog());
        } else {
            batch.setShader(shaderProgram);
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(texture, 0, 0, Gdx.graphics.getWidth()/2f, Gdx.graphics.getHeight()/2f);
        batch.end();
    }

    @Override
    public void dispose() {
        texture.dispose();
        batch.dispose();
        shaderProgram.dispose();
    }
}
