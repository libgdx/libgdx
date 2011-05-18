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

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.tests.utils.GdxTest;

public class TextureRenderTest extends GdxTest {

	private OrthographicCamera camera;
	private Mesh mesh;
	private Texture texture;

	private ArrayList<SimpleRect> rects = new ArrayList<SimpleRect>();
	Color color = new Color(Color.GREEN);

	@Override public void create () {
		camera = new OrthographicCamera(480, 320);		
		camera.position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0);

		Pixmap pixmap = new Pixmap(Gdx.files.internal("data/badlogic.jpg"));
		texture = new Texture(pixmap);
		pixmap.dispose();

		float invTexWidth = 1.0f / texture.getWidth();
		float invTexHeight = 1.0f / texture.getHeight();

		rects = createRects();

		if (this.mesh == null)
			this.mesh = new Mesh(false, 6 * 4 * rects.size(), 0, new VertexAttribute(VertexAttributes.Usage.Position, 2,
				"a_position"), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord"));

		final float[] vertices = new float[rects.size() * 6 * 4];
		int idx = 0;

		for (int i = 0; i < rects.size(); i++) {
			SimpleRect rect = rects.get(i);

			float u = rect.x * invTexWidth;
			float v = rect.y * invTexHeight;
			float u2 = (rect.x + rect.width) * invTexWidth;
			float v2 = (rect.y + rect.height) * invTexHeight;
			float fx = rect.x;
			float fy = rect.y;
			float fx2 = (rect.x + rect.width);
			float fy2 = (rect.y - rect.height);

			vertices[idx++] = fx;
			vertices[idx++] = fy;
			vertices[idx++] = u;
			vertices[idx++] = v;

			vertices[idx++] = fx;
			vertices[idx++] = fy2;
			vertices[idx++] = u;
			vertices[idx++] = v2;

			vertices[idx++] = fx2;
			vertices[idx++] = fy2;
			vertices[idx++] = u2;
			vertices[idx++] = v2;

			vertices[idx++] = fx2;
			vertices[idx++] = fy2;
			vertices[idx++] = u2;
			vertices[idx++] = v2;

			vertices[idx++] = fx2;
			vertices[idx++] = fy;
			vertices[idx++] = u2;
			vertices[idx++] = v;

			vertices[idx++] = fx;
			vertices[idx++] = fy;
			vertices[idx++] = u;
			vertices[idx++] = v;

		}
		this.mesh.setVertices(vertices);

	}

	@Override public void render () {

		GL10 gl = Gdx.graphics.getGL10();
		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		gl.glEnable(GL10.GL_TEXTURE_2D);

		camera.update();
		camera.apply(gl);

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		gl.glColor4f(color.r, color.g, color.b, color.a);

		gl.glColor4f(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0.5F);

		texture.bind();

		for (int i = 0; i < rects.size(); i++) {
			SimpleRect rect = rects.get(i);
			gl.glPushMatrix();

// float x = (rect.index + 1) * 60F;
			gl.glTranslatef(100, 100F, 0F);

			mesh.render(GL10.GL_TRIANGLES, rect.index * 24, 24);

			gl.glPopMatrix();
		}

	}

	private ArrayList<SimpleRect> createRects () {
		ArrayList<SimpleRect> l = new ArrayList<SimpleRect>();
		l.add(new SimpleRect(0, 10, 0, 50, 50));
		l.add(new SimpleRect(1, 60, 0, 50, 50));
		l.add(new SimpleRect(2, 110, 0, 50, 50));
		return l;
	}

	private static class SimpleRect {
		public int index;
		public float x;
		public float y;
		public float height;
		public float width;

		SimpleRect (int index, float x, float y, float width, float height) {
			this.index = index;
			this.x = x;
			this.y = y;
			this.height = height;
			this.width = width;
		}
	}

	@Override public boolean needsGL20 () {
		return false;
	}

}
