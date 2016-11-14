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
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonRegionLoader;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Disposable;

public class PolygonRegionTest extends GdxTest {

	PolygonSpriteBatch batch;
	PolygonRegionDebugRenderer debugRenderer;

	Texture texture;
	OrthographicCamera camera;
	PolygonRegion region;
	PolygonRegion region2;

	boolean usePolygonBatch = true;

	@Override
	public void create () {
		texture = new Texture(Gdx.files.internal("data/tree.png"));

		PolygonRegionLoader loader = new PolygonRegionLoader();
		region = loader.load(new TextureRegion(texture), Gdx.files.internal("data/tree.psh"));

		// create a region from an arbitrary set of vertices (a triangle in this case)
		region2 = new PolygonRegion(new TextureRegion(texture), new float[] {0, 0, 100, 100, 0, 100}, new short[] {0, 1, 2});

		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.x = 0;
		camera.position.y = 0;

		batch = new PolygonSpriteBatch();
		debugRenderer = new PolygonRegionDebugRenderer();

		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void resize (int width, int height) {
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		camera.update();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glClearColor(0.25f, 0.25f, 0.25f, 1.0f);

		camera.update();
		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		// draw bot regions side-by-side
		float width = 256;
		float x = -width;
		batch.draw(region, x, -128, 256, 256);
		batch.draw(region2, x + width + 10, -128, 256, 256);

		batch.end();

		debugRenderer.setProjectionMatrix(camera.combined);
		debugRenderer.draw(region, x, -128, 0, 0, 256, 256, 1, 1, 0);
		debugRenderer.draw(region2, x + width + 10, -128, 0, 0, 256, 256, 1, 1, 0);
	}

	@Override
	public void dispose () {
		debugRenderer.dispose();
		texture.dispose();
		batch.dispose();
	}

	public class PolygonRegionDebugRenderer implements Disposable {
		ShapeRenderer renderer;

		public PolygonRegionDebugRenderer () {
			renderer = new ShapeRenderer();
		}

		public void draw (PolygonRegion region, float x, float y, float originX, float originY, float width, float height,
			float scaleX, float scaleY, float rotation) {

			float[] vertices = region.getVertices();
			short[] triangles = region.getTriangles();
			float[] textureCoords = region.getTextureCoords();

			// bottom left and top right corner points relative to origin
			final float worldOriginX = x + originX;
			final float worldOriginY = y + originY;
			float sX = width / region.getRegion().getRegionWidth();
			float sY = height / region.getRegion().getRegionHeight();
			float fx1, fx2, fx3, px1, px2, px3;
			float fy1, fy2, fy3, py1, py2, py3;

			final float cos = MathUtils.cosDeg(rotation);
			final float sin = MathUtils.sinDeg(rotation);

			renderer.setColor(Color.RED);
			renderer.begin(ShapeType.Line);

			for (int i = 0, n = triangles.length; i < n; i += 3) {
				int p1 = triangles[i] * 2;
				int p2 = triangles[i + 1] * 2;
				int p3 = triangles[i + 2] * 2;
				fx1 = vertices[p1] * sX;
				fy1 = vertices[p1 + 1] * sY;
				fx2 = vertices[p2] * sX;
				fy2 = vertices[p2 + 1] * sY;
				fx3 = vertices[p3] * sX;
				fy3 = vertices[p3 + 1] * sY;

				fx1 -= originX;
				fy1 -= originY;
				fx2 -= originX;
				fy2 -= originY;
				fx3 -= originX;
				fy3 -= originY;

				if (scaleX != 1 || scaleY != 1) {
					fx1 *= scaleX;
					fy1 *= scaleY;
					fx2 *= scaleX;
					fy2 *= scaleY;
					fx3 *= scaleX;
					fy3 *= scaleY;
				}

				px1 = cos * fx1 - sin * fy1;
				py1 = sin * fx1 + cos * fy1;
				px2 = cos * fx2 - sin * fy2;
				py2 = sin * fx2 + cos * fy2;
				px3 = cos * fx3 - sin * fy3;
				py3 = sin * fx3 + cos * fy3;

				px1 += worldOriginX;
				py1 += worldOriginY;
				px2 += worldOriginX;
				py2 += worldOriginY;
				px3 += worldOriginX;
				py3 += worldOriginY;

				renderer.line(px1, py1, px2, py2);
				renderer.line(px2, py2, px3, py3);
				renderer.line(px3, py3, px1, py1);
			}

			renderer.end();

			renderer.setColor(Color.BLUE);
			renderer.begin(ShapeType.Filled);

			renderer.circle(worldOriginX, worldOriginY, 4);

			renderer.end();

			// Calculate the bounding rect, is there a better way?!
			// bottom left and top right corner points relative to origin
			fx1 = -originX;
			fy1 = -originY;
			fx2 = width - originX;
			fy2 = height - originY;

			// scale
			if (scaleX != 1 || scaleY != 1) {
				fx1 *= scaleX;
				fy1 *= scaleY;
				fx2 *= scaleX;
				fy2 *= scaleY;
			}

			// construct corner points, start from top left and go counter clockwise
			final float p1x = fx1;
			final float p1y = fy1;
			final float p2x = fx1;
			final float p2y = fy2;
			final float p3x = fx2;
			final float p3y = fy2;
			final float p4x = fx2;
			final float p4y = fy1;

			float x1;
			float y1;
			float x2;
			float y2;
			float x3;
			float y3;
			float x4;
			float y4;

			// rotate
			if (rotation != 0) {
				x1 = cos * p1x - sin * p1y;
				y1 = sin * p1x + cos * p1y;

				x2 = cos * p2x - sin * p2y;
				y2 = sin * p2x + cos * p2y;

				x3 = cos * p3x - sin * p3y;
				y3 = sin * p3x + cos * p3y;

				x4 = x1 + (x3 - x2);
				y4 = y3 - (y2 - y1);
			} else {
				x1 = p1x;
				y1 = p1y;

				x2 = p2x;
				y2 = p2y;

				x3 = p3x;
				y3 = p3y;

				x4 = p4x;
				y4 = p4y;
			}

			x1 += worldOriginX;
			y1 += worldOriginY;
			x2 += worldOriginX;
			y2 += worldOriginY;
			x3 += worldOriginX;
			y3 += worldOriginY;
			x4 += worldOriginX;
			y4 += worldOriginY;

			// Draw the bounding rectangle
			renderer.setColor(Color.GREEN);
			renderer.begin(ShapeType.Line);

			renderer.line(x1, y1, x2, y2);
			renderer.line(x2, y2, x3, y3);
			renderer.line(x3, y3, x4, y4);
			renderer.line(x4, y4, x1, y1);

			renderer.end();
		}

		public void setProjectionMatrix (Matrix4 matrix) {
			this.renderer.setProjectionMatrix(matrix);
		}

		@Override
		public void dispose () {
			renderer.dispose();
		}
	}
}
