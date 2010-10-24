/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class FixedPointTest implements RenderListener {
	Texture tex;
	Texture tex2;
	IntBuffer vertices;
	final int BYTES_PER_VERTEX = (4 + 3 + 2 + 2) * 4;
	float angle = 0;
	float angleIncrement = 0.1f;

	@Override public void dispose () {

	}

	@Override public void render () {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glRotatef(angle, 0, 0, 1);
		angle += angleIncrement;
		gl.glEnable(GL10.GL_TEXTURE_2D);

		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		vertices.position(0);
		gl.glColorPointer(4, GL10.GL_FIXED, BYTES_PER_VERTEX, vertices);

		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glClientActiveTexture(GL10.GL_TEXTURE0);
		gl.glActiveTexture(GL10.GL_TEXTURE0);
		tex.bind();
		vertices.position(4);
		gl.glTexCoordPointer(2, GL10.GL_FIXED, BYTES_PER_VERTEX, vertices);

		gl.glClientActiveTexture(GL10.GL_TEXTURE1);
		gl.glActiveTexture(GL10.GL_TEXTURE1);
		tex2.bind();
		vertices.position(6);
		gl.glTexCoordPointer(2, GL10.GL_FIXED, BYTES_PER_VERTEX, vertices);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		vertices.position(8);
		gl.glVertexPointer(3, GL10.GL_FIXED, BYTES_PER_VERTEX, vertices);

		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
	}

	@Override public void surfaceCreated () {
		if (tex != null) {
			tex.dispose();
			tex2.dispose();
		}

		ByteBuffer buffer = ByteBuffer.allocateDirect(BYTES_PER_VERTEX * 3);
		buffer.order(ByteOrder.nativeOrder());
		vertices = buffer.asIntBuffer();

		int[] verts = {fp(1), fp(0), fp(0), fp(1), fp(0), fp(1), fp(0), fp(1), fp(-0.5f), fp(-0.5f), fp(0),

		fp(0), fp(1), fp(0), fp(1), fp(1), fp(1), fp(1), fp(1), fp(0.5f), fp(-0.5f), fp(0),

		fp(0), fp(0), fp(1), fp(1), fp(0.5f), fp(0), fp(0.5f), fp(0), fp(0), fp(0.5f), fp(0),};
		vertices.put(verts);
		vertices.flip();

		Pixmap pixmap = Gdx.graphics.newPixmap(256, 256, Format.RGBA8888);
		pixmap.setColor(1, 1, 1, 1);
		pixmap.fill();
		pixmap.setColor(0, 0, 0, 1);
		pixmap.drawLine(0, 0, 256, 256);
		pixmap.drawLine(256, 0, 0, 256);
		tex = Gdx.graphics.newUnmanagedTexture(pixmap, TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge,
			TextureWrap.ClampToEdge);

		pixmap = Gdx.graphics.newPixmap(256, 256, Format.RGBA8888);
		pixmap.setColor(1, 1, 1, 1);
		pixmap.fill();
		pixmap.setColor(0, 0, 0, 1);
		pixmap.drawLine(128, 0, 128, 256);
		tex2 = Gdx.graphics.newUnmanagedTexture(pixmap, TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge,
			TextureWrap.ClampToEdge);

		Gdx.graphics.getGL10().glDisable(GL10.GL_TEXTURE_2D);
	}

	@Override public void surfaceChanged (int width, int height) {
		// TODO Auto-generated method stub

	}

	private static int fp (float value) {
		return (int)(value * 65536);
	}

}
