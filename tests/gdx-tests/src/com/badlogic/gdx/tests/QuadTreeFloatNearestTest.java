
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.QuadTreeFloat;

public class QuadTreeFloatNearestTest extends GdxTest {
	QuadTreeFloat q = new QuadTreeFloat();
	FloatArray points = new FloatArray(100 * 2);
	ShapeRenderer shapes;
	FloatArray results = new FloatArray(false, 16);

	public void create () {
		shapes = new ShapeRenderer();
		q.setBounds(10, 10, 400, 400);
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
		boolean found = q.nearest(x, y, results);

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		shapes.begin(ShapeType.Line);
		draw(q);
		shapes.setColor(Color.GREEN);
		if (found) {
			float radius = (float)Math.sqrt(results.get(QuadTreeFloat.DISTSQR));
			shapes.circle(x, y, radius);
			float foundX = results.get(QuadTreeFloat.X), foundY = results.get(QuadTreeFloat.Y);
			shapes.circle(foundX, foundY, 10);
		}
		shapes.end();
	}

	void draw (QuadTreeFloat q) {
		shapes.setColor(Color.WHITE);
		shapes.rect(q.x, q.y, q.width, q.height);
		if (q.values != null) {
			for (int i = 0, n = q.count; i < n; i += 3) {
				shapes.setColor(!results.isEmpty() && q.values[i] == results.get(QuadTreeFloat.VALUE) ? Color.YELLOW : Color.RED);
				shapes.x(q.values[i + 1], q.values[i + 2], 7);
			}
		}
		if (q.nw != null) draw(q.nw);
		if (q.sw != null) draw(q.sw);
		if (q.ne != null) draw(q.ne);
		if (q.se != null) draw(q.se);
	}
}
