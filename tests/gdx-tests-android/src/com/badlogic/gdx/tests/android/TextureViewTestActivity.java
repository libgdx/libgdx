package com.badlogic.gdx.tests.android;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TextureViewTestActivity extends AndroidApplication implements ApplicationListener {

    private Texture texture;
    private Batch batch;
    private int width, height;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.texture_view_activity);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;
        config.useRotationVectorSensor = true;
        config.useTextureView = true;
        config.a = 8;
        config.b = 8;
        config.g = 8;
        config.r = 8;
        View textureView = initializeForView(this, config);
        ((ViewGroup) findViewById(R.id.tvLayout)).addView(textureView);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(0, 1, 0, 0.1f);
        batch.begin();
        batch.draw(texture, (width - texture.getWidth()) / 2f, (height - texture.getHeight()) / 2f);
        batch.end();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        texture.dispose();
        batch.dispose();
    }
}
