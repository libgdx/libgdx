
package com.badlogic.gdx.twl.renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;

class TextureAreaBase {
	protected final float tx0;
	protected final float ty0;
	protected final float tx1;
	protected final float ty1;
	protected final short width;
	protected final short height;

	FloatBuffer texCoords = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	FloatBuffer vertices = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	final TwlRenderer renderer;

	TextureAreaBase (TwlRenderer renderer, int x, int y, int width, int height, float texWidth, float texHeight) {
		this.renderer = renderer;
		// negative size allows for flipping
		this.width = (short)Math.abs(width);
		this.height = (short)Math.abs(height);
		float fx = x;
		float fy = y;
		if (width == 1) {
			fx += 0.5f;
			width = 0;
		}
		if (height == 1) {
			fy += 0.5f;
			height = 0;
		}
		this.tx0 = fx / texWidth;
		this.ty0 = fy / texHeight;
		this.tx1 = tx0 + width / texWidth;
		this.ty1 = ty0 + height / texHeight;
	}

	TextureAreaBase (TextureAreaBase src) {
		this.renderer = src.renderer;
		this.tx0 = src.tx0;
		this.ty0 = src.ty0;
		this.tx1 = src.tx1;
		this.ty1 = src.ty1;
		this.width = src.width;
		this.height = src.height;
	}

	public int getWidth () {
		return width;
	}

	public int getHeight () {
		return height;
	}

	void drawQuad (int x, int y, int w, int h) {
		texCoords.clear();
		vertices.clear();

		texCoords.put(tx0);
		texCoords.put(ty0);
		vertices.put(x);
		vertices.put(y);

		texCoords.put(tx0);
		texCoords.put(ty1);
		vertices.put(x);
		vertices.put(y + h);

		texCoords.put(tx1);
		texCoords.put(ty0);
		vertices.put(x + w);
		vertices.put(y);

		texCoords.put(tx1);
		texCoords.put(ty1);
		vertices.put(x + w);
		vertices.put(y + h);

		texCoords.flip();
		vertices.flip();

		GL10 gl = Gdx.graphics.getGL10();
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertices);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texCoords);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
	}
}
