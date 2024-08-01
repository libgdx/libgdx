package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * A test for mainly for checking if predictive back gesture is working on
 * Android. Tap the screen to increment the counter. Go back to decrement the
 * counter. If the counter is 0, the test will be exited.
 */
public class BackTest extends GdxTest {

  private SpriteBatch batch;
  private BitmapFont font;
  private final Viewport viewport = new FitViewport(160, 90);

  private int backs;

  @Override
  public void create() {
    batch = new SpriteBatch();
    font = new BitmapFont();
    Gdx.input.setInputProcessor(new InputAdapter() {

      @Override
      public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        int screenWidth = Gdx.graphics.getBackBufferWidth();
        float safeZone = screenWidth * .1f;
        if (screenX >= safeZone && screenX < screenWidth - safeZone) {
          backs++;
          Gdx.input.setCatchKey(Input.Keys.BACK, backs > 0);
          return true;
        }
        return false;
      }

      @Override
      public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.BACK) backs--;
        Gdx.input.setCatchKey(Input.Keys.BACK, backs > 0);
        return true;
      }
    });
  }

  @Override
  public void render() {
    ScreenUtils.clear(Color.BLACK);
    batch.begin();
    font.draw(batch, "Back depth: " + backs, 20, 50);
    batch.end();
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, true);
    batch.setProjectionMatrix(viewport.getCamera().combined);
  }

}
