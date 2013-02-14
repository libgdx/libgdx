package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.tests.utils.GdxTest;

public class Vector2dTest extends GdxTest {
	private ShapeRenderer renderer;
	private OrthographicCamera camera;

	private Vector2 rotating = new Vector2(Vector2.X);
	private Vector2 scalingX = new Vector2(Vector2.Y);
	private Vector2 scalingY = new Vector2(Vector2.X);
	private Vector2 lerping1 = new Vector2(Vector2.X);
	private Vector2 lerpTarget = new Vector2(Vector2.Y);
	private Vector2 sum = new Vector2().add(Vector2.X).add(Vector2.Y).nor();
	private Vector2 mash = new Vector2(Vector2.Y);

	
	private final long start = System.currentTimeMillis();
	
	@Override
	public void create () {
		renderer = new ShapeRenderer();
	}
	
	private void renderVectorAt(float x, float y, Vector2 v) {
		renderer.line(x, y, x + v.x, y + v.y);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		renderer.setProjectionMatrix(camera.combined);
		
		// Render the 'lerp' vector target as a circle
		renderer.begin(ShapeType.Filled);
		renderer.setColor(1.0f, 0, 0, 0.3f);
		renderer.circle(-2 + lerpTarget.x, 2 + lerpTarget.y , 0.08f, 16);
		renderer.end();

		renderer.begin(ShapeType.Line);
		
		// Render the three fixed X, Y and sum vectors:
		renderer.setColor(Color.RED);
		renderVectorAt(0, 0, Vector2.X);
		renderer.setColor(Color.GREEN);
		renderVectorAt(0, 0, Vector2.Y);
		renderer.setColor(Color.YELLOW);
		renderVectorAt(0, 0, sum);
		
		final float changeRate = Gdx.graphics.getDeltaTime();
		renderer.setColor(Color.WHITE);
		
		renderVectorAt(2, 2, rotating);
		rotating.rotate(93 * changeRate);
		
		renderVectorAt(2, -2, scalingX);
		scalingX.set(0, MathUtils.sin((System.currentTimeMillis() - start)/520.0f));
		renderVectorAt(2, -2, scalingY);
		scalingY.set(MathUtils.cos((System.currentTimeMillis() - start)/260.0f), 0);
		
		renderVectorAt(-2, 2, lerping1);
		lerping1.lerp(lerpTarget, 0.025f);
		if (lerping1.epsilonEquals(lerpTarget, 0.05f)) {
			lerpTarget.set(-1.0f + MathUtils.random(2.0f), -1.0f + MathUtils.random(2.0f)).nor();
		}

		renderVectorAt(-2, -2, mash);
		mash.set(0, 0).add(rotating).add(scalingX).add(scalingY).add(lerping1);
	
		renderer.end();
	}

	@Override
	public void resize (int width, int height) {
		float ratio = ((float)Gdx.graphics.getWidth() / (float)Gdx.graphics.getHeight());
		int h = 10;
		int w = (int)(h * ratio);
		camera = new OrthographicCamera(w, h);
	}
	
	@Override
	public boolean needsGL20 () {
		return false;
	}
}
