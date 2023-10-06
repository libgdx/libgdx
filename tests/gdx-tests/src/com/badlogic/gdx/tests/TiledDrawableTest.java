
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

public class TiledDrawableTest extends GdxTest {

	private static final float SCALE_CHANGE = 0.25f;

	private Stage stage;
	private Batch batch;
	private BitmapFont font;
	private TextureAtlas atlas;
	private TiledDrawable tiledDrawable;

	@Override
	public void create () {
		stage = new Stage();
		batch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("data/lsans-15.fnt"), false);

		// Must be a texture atlas so uv is not just 0 and 1
		atlas = new TextureAtlas(Gdx.files.internal("data/testAtlas.atlas"));
		tiledDrawable = new TiledDrawable(atlas.findRegion("tileTester"));

		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render () {
		ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);

		final Batch batch = stage.getBatch();
		batch.begin();

		font.draw(batch,
			"Scale: " + tiledDrawable.getScale() + "  (to change scale press: 'A' -" + SCALE_CHANGE + ", 'D' +" + SCALE_CHANGE + ")",
			8, 20);

		final float leftSpacingX = 40;
		final float spacingX = 80;
		final float bottomSpacing = 60;
		final float spacingY = 40;
		float inputX = Gdx.input.getX();
		float inputY = Gdx.graphics.getHeight() - Gdx.input.getY();

		final float clusterWidth = Math.max(13, (inputX - leftSpacingX - (2 * spacingX)) / 3f);
		final float clusterHeight = Math.max(13, (inputY - bottomSpacing - (2 * spacingY)) / 3f);

		final float leftX = leftSpacingX;
		final float centerX = leftSpacingX + spacingX + clusterWidth;
		final float rightX = leftSpacingX + (2 * spacingX) + (2 * clusterWidth);
		final float topY = bottomSpacing + (2 * spacingY) + (2 * clusterHeight);
		final float centerY = bottomSpacing + spacingY + clusterHeight;
		final float bottomY = bottomSpacing;

		drawTiledDrawableCluster(batch, leftX, topY, clusterWidth, clusterHeight, Align.topLeft);
		drawTiledDrawableCluster(batch, centerX, topY, clusterWidth, clusterHeight, Align.top);
		drawTiledDrawableCluster(batch, rightX, topY, clusterWidth, clusterHeight, Align.topRight);

		drawTiledDrawableCluster(batch, leftX, centerY, clusterWidth, clusterHeight, Align.left);
		drawTiledDrawableCluster(batch, centerX, centerY, clusterWidth, clusterHeight, Align.center);
		drawTiledDrawableCluster(batch, rightX, centerY, clusterWidth, clusterHeight, Align.right);

		drawTiledDrawableCluster(batch, leftX, bottomY, clusterWidth, clusterHeight, Align.bottomLeft);
		drawTiledDrawableCluster(batch, centerX, bottomY, clusterWidth, clusterHeight, Align.bottom);
		drawTiledDrawableCluster(batch, rightX, bottomY, clusterWidth, clusterHeight, Align.bottomRight);

		batch.end();
	}

	private final void drawTiledDrawableCluster (Batch batch, float x, float y, float clusterWidth, float clusterHeight,
		int align) {
		tiledDrawable.setAlign(align);
		tiledDrawable.draw(batch, x, y, clusterWidth, clusterHeight);
		font.draw(batch, Align.toString(align), x, y - 5);
	}

	@Override
	public boolean keyDown (int keycode) {
		if (keycode == Input.Keys.A) {
			tiledDrawable.setScale(Math.max(SCALE_CHANGE, tiledDrawable.getScale() - SCALE_CHANGE));
		} else if (keycode == Input.Keys.D) {
			tiledDrawable.setScale(tiledDrawable.getScale() + SCALE_CHANGE);
		}
		return true;
	}

	@Override
	public void resize (int width, int height) {
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose () {
		stage.dispose();
		font.dispose();
		atlas.dispose();
	}
}
