/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Bresenham2;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

public class Bresenham2Test extends GdxTest {
	SpriteBatch batch;
	Texture result;
	Pixmap pixmap;
	Bresenham2 bresenham;
	Array<GridPoint2> line;
	int lastX;
	int lastY;

	/** If any of the green pixels this draws are still visible after Bresenham2's line is drawn, then the implementation in
	 * Bresenham2 is wrong. <br>
	 * This is almost exactly taken from http://rosettacode.org/wiki/Bitmap/Bresenham%27s_line_algorithm#Java ; only the Swing/AWT
	 * code was changed to use Pixmap here. The Rosetta Code version doesn't use pooling for its points, so the algorithm in
	 * Bresenham2 should be more efficient, but this can be used as a reference implementation that's probably been picked over and
	 * run many times.
	 * @param x1 start x
	 * @param y1 start y
	 * @param x2 end x
	 * @param y2 end y */
	public void drawRosettaCodeLine (int x1, int y1, int x2, int y2) {
		pixmap.setColor(Color.GREEN);

		int d = 0;

		int dx = Math.abs(x2 - x1);
		int dy = Math.abs(y2 - y1);

		int dx2 = 2 * dx; // slope scaling factors to
		int dy2 = 2 * dy; // avoid floating point

		int ix = x1 < x2 ? 1 : -1; // increment direction
		int iy = y1 < y2 ? 1 : -1;

		int x = x1;
		int y = y1;

		if (dx >= dy) {
			while (true) {
				pixmap.drawPixel(x, y);
				if (x == x2) break;
				x += ix;
				d += dy2;
				if (d > dx) {
					y += iy;
					d -= dx2;
				}
			}
		} else {
			while (true) {
				pixmap.drawPixel(x, y);
				if (y == y2) break;
				y += iy;
				d += dx2;
				if (d > dy) {
					x += ix;
					d -= dy2;
				}
			}
		}

	}

	@Override
	public void create () {
		pixmap = new Pixmap(160, 120, Format.RGBA8888);
		pixmap.setColor(Color.WHITE);

		lastX = -1;
		lastY = -1;
		drawRosettaCodeLine(79, 59, 80, 60);
		bresenham = new Bresenham2();
		line = bresenham.line(79, 59, 80, 60);
		for (GridPoint2 point : line)
			pixmap.drawPixel(point.x, point.y);

		result = new Texture(pixmap);
		batch = new SpriteBatch();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		int x = Gdx.input.getX() >> 2, y = Gdx.input.getY() >> 2;
		if ((lastX != x || lastY != y) && x >= 0 && x < 160 && y >= 0 && y < 120) {
			lastX = x;
			lastY = y;
			pixmap.setColor(Color.BLACK);
			pixmap.fill();
			drawRosettaCodeLine(79, 59, x, y);
			pixmap.setColor(Color.WHITE);
			line = bresenham.line(79, 59, x, y);
			for (GridPoint2 point : line)
				pixmap.drawPixel(point.x, point.y);
			result.draw(pixmap, 0, 0);
		}
		batch.begin();
		batch.draw(result, 0, 0, 640, 480);
		batch.end();
	}
}
