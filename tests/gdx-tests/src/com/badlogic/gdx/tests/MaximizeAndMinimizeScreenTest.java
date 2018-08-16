package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.tests.utils.GdxTest;

public class MaximizeAndMinimizeScreenTest extends GdxTest implements InputProcessor {

    private static final String TAG = "TestGame";

    @Override
    public void create() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render() {
        if(Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.ALT_LEFT) &&
                Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            if(Gdx.graphics.isFullscreen()) {
                Gdx.app.log(TAG, "Switching to windowed mode");
                Gdx.graphics.setWindowedMode(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            }
            else {
                Gdx.app.log(TAG, "Switching to fullscreen mode");
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        Gdx.app.log(TAG, "keyDown: " + keycode);
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        Gdx.app.log(TAG, "keyUp: " + keycode);
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        Gdx.app.log(TAG, "keyTyped: " + (int)character);
        return false;
    }
}
