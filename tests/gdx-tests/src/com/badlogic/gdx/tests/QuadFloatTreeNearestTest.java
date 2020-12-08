
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.QuadFloatTree;

public class QuadFloatTreeNearestTest extends GdxTest {
	QuadFloatTree q = new QuadFloatTree();
	FloatArray points = new FloatArray(100 * 2);
	ShapeRenderer shapes;
	FloatArray results = new FloatArray(false, 16);
	BitmapFont font;
	SpriteBatch batch;

	public void create () {
		shapes = new ShapeRenderer();
		font = new BitmapFont();
		batch = new SpriteBatch();
		q.set(10, 10, 400, 400);
		for (int i = 0; i < 30; i++) {
			float x = MathUtils.random(10, 400);
			float y = MathUtils.random(10, 400);
			points.add(x, y);
			q.add(i, x, y);
		}
	}

	public void render () {
		float x = Gdx.input.getX(), y = Gdx.graphics.getHeight() - Gdx.input.getY();
		results.clear();
		q.findNearest(x, y, results);

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		shapes.begin(ShapeType.Line);
		draw(q);
		shapes.setColor(Color.GREEN);
		if (results.get(0) != -1) {
			float radius = (float)Math.sqrt(results.get(1));
			shapes.circle(x, y, radius);
		}
		shapes.end();

		batch.begin();
		font.draw(batch, "Tested: " + results.get(2), 0, Gdx.graphics.getHeight() - 20);
		batch.end();
	}

	void draw (QuadFloatTree q) {
		shapes.setColor(Color.WHITE);
		shapes.rect(q.x, q.y, q.width, q.height);
		if (q.values != null) {
			for (int i = 0, n = q.count; i < n; i += 3) {
				shapes.setColor(q.values[i + 2] == results.get(0) ? Color.YELLOW : Color.RED);
				shapes.x(q.values[i], q.values[i + 1], 7);
			}
		}
		if (q.nw != null) draw(q.nw);
		if (q.sw != null) draw(q.sw);
		if (q.ne != null) draw(q.ne);
		if (q.se != null) draw(q.se);
	}
}
