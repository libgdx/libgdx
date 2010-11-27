/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.tests.utils.GdxTest;

public class MeshMultitextureTest extends GdxTest {
	Texture tex1;
	Texture tex2;
	Mesh mesh;

	@Override public void render () {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glViewport( 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glActiveTexture(GL10.GL_TEXTURE0);
		tex1.bind();
		gl.glActiveTexture(GL10.GL_TEXTURE1);
		tex2.bind();
		mesh.render(GL10.GL_TRIANGLES);
	}

	@Override public void create () {
		mesh = new Mesh(true, 3, 0, new VertexAttribute(VertexAttributes.Usage.Color, 4, "a_Color"), new VertexAttribute(
			VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoords1"), new VertexAttribute(
			VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoords2"), new VertexAttribute(VertexAttributes.Usage.Position, 3,
			"a_Position"));

		mesh.setVertices(new float[] {1, 0, 0, 1, 0, 1, 0, 1, -0.5f, -0.5f, 0,

		0, 1, 0, 1, 1, 1, 1, 1, 0.5f, -0.5f, 0,

		0, 0, 1, 1, 0.5f, 0, 0.5f, 0, 0, 0.5f, 0,});
		
		initUnmanaged();
	}
	
	void initUnmanaged() {
		Pixmap pixmap = Gdx.graphics.newPixmap(256, 256, Format.RGBA8888);
		pixmap.setColor(1, 1, 1, 1);
		pixmap.fill();
		pixmap.setColor(0, 0, 0, 1);
		pixmap.drawLine(0, 0, 256, 256);
		pixmap.drawLine(256, 0, 0, 256);
		tex1 = Gdx.graphics.newUnmanagedTexture(pixmap, TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge,
			TextureWrap.ClampToEdge);
		pixmap.dispose();

		pixmap = Gdx.graphics.newPixmap(256, 256, Format.RGBA8888);
		pixmap.setColor(1, 1, 1, 1);
		pixmap.fill();
		pixmap.setColor(0, 0, 0, 1);
		pixmap.drawLine(128, 0, 128, 256);
		tex2 = Gdx.graphics.newUnmanagedTexture(pixmap, TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge,
			TextureWrap.ClampToEdge);
		pixmap.dispose();
	}
	
	public void resume() {
		initUnmanaged();
	}

	@Override public boolean needsGL20 () {
		return false;
	}

}
