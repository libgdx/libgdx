
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class SafeAreaInsetsTest extends GdxTest {
	private ShapeRenderer shapeRenderer;
	private SpriteBatch batch;
	private BitmapFont font;
	private Viewport viewport;

	private int screenWidth, screenHeight;
	private int top, bottom, left, right;

	@Override
	public void create () {
		shapeRenderer = new ShapeRenderer();
		batch = new SpriteBatch();
		viewport = new ScreenViewport();
		font = new BitmapFont();

		updateInsetValues();
	}

	@Override
	public void resize (int width, int height) {
		viewport.update(width, height, true);
		updateInsetValues();
		Gdx.app.log("SafeAreaInsetsTest", "resize() triggered -> Size: " + width + "x" + height + " | Insets [Top:" + top
			+ ", Bottom:" + bottom + ", Left:" + left + ", Right:" + right + "]");
	}

	private void updateInsetValues () {
		top = Gdx.graphics.getSafeInsetTop();
		bottom = Gdx.graphics.getSafeInsetBottom();
		left = Gdx.graphics.getSafeInsetLeft();
		right = Gdx.graphics.getSafeInsetRight();
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
	}

	@Override
	public void render () {

		Gdx.gl.glClearColor(0.12f, 0.12f, 0.16f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
		batch.setProjectionMatrix(viewport.getCamera().combined);

		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		// Full background (red)
		shapeRenderer.setColor(0.35f, 0.1f, 0.1f, 1f);
		shapeRenderer.rect(0, 0, screenWidth, screenHeight);

		// Safe Area (green)
		shapeRenderer.setColor(0.1f, 0.35f, 0.2f, 1f);
		shapeRenderer.rect(left, bottom, screenWidth - left - right, screenHeight - top - bottom);

		shapeRenderer.end();

		// Contour lines
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

		// Exterior red border (screen limits)
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.rect(0, 0, screenWidth, screenHeight);

		// Interior green border (Safe Area limits)
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.rect(left, bottom, screenWidth - left - right, screenHeight - top - bottom);

		shapeRenderer.end();

		batch.begin();
		font.setColor(Color.WHITE);

		float textX = Math.max(left + 25, 30f);
		float textY = screenHeight - Math.max(top + 30, 40f);

		font.draw(batch, "=== SAFE AREA TEST ===", textX, textY);
		font.draw(batch, "Screen Resolution: " + screenWidth + " x " + screenHeight, textX, textY - 30);
		font.draw(batch, "Safe Inset Top:    " + top, textX, textY - 60);
		font.draw(batch, "Safe Inset Bottom: " + bottom, textX, textY - 90);
		font.draw(batch, "Safe Inset Left:   " + left, textX, textY - 120);
		font.draw(batch, "Safe Inset Right:  " + right, textX, textY - 150);

		font.setColor(Color.YELLOW);
		font.draw(batch, "-> RED Border = Full Screen", textX, textY - 190);
		font.draw(batch, "-> GREEN Border = Safe Area (Safe Insets)", textX, textY - 215);

		batch.end();
	}

	@Override
	public void dispose () {
		shapeRenderer.dispose();
		batch.dispose();
		font.dispose();
	}
}
