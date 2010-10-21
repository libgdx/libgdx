
package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.MathUtils;

public class Sprite {
	public Texture texture;
	public float width, height;
	public float originX, originY;
	public float[] vertices = new float[20];

	public Sprite (String path) {
		this(Gdx.files.getFileHandle(path, FileType.Internal), -1, -1);
	}

	public Sprite (String path, int width, int height) {
		this(Gdx.files.getFileHandle(path, FileType.Internal), width, height);
	}

	public Sprite (FileHandle file) {
		this(file, -1, -1);
	}

	public Sprite (FileHandle file, int width, int height) {
		texture = Gdx.graphics.newTexture(file, TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge,
			TextureWrap.ClampToEdge);
		if (width == -1) width = texture.getWidth();
		if (height == -1) height = texture.getHeight();
		setBounds(0, 0, width, height);
		setTextureBounds(0, 0, width, height);
		setColor(1, 1, 1, 1);
		flip(false, true);
	}

	public Sprite (Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
		this.texture = texture;
		setTextureBounds(srcX, srcY, srcWidth, srcHeight);
		setColor(1, 1, 1, 1);
		flip(false, true);
	}

	/**
	 * Sets the screen coordinates where the sprite will be drawn. Invalidates the origin (see {@link #translate(float, float)}).
	 */
	public void setPosition (float x, float y) {
		float x2 = x + width;
		float y2 = y + height;

		vertices[X1] = x;
		vertices[Y1] = y;

		vertices[X2] = x;
		vertices[Y2] = y2;

		vertices[X3] = x2;
		vertices[Y3] = y2;

		vertices[X4] = x2;
		vertices[Y4] = y;
	}

	/**
	 * Sets the screen coordinates where the sprite will be drawn. Invalidates the origin (see {@link #translate(float, float)}).
	 */
	public void setBounds (float x, float y, float width, float height) {
		this.width = width;
		this.height = height;

		float x2 = x + width;
		float y2 = y + height;

		vertices[X1] = x;
		vertices[Y1] = y;

		vertices[X2] = x;
		vertices[Y2] = y2;

		vertices[X3] = x2;
		vertices[Y3] = y2;

		vertices[X4] = x2;
		vertices[Y4] = y;
	}

	/**
	 * Shifts the screen coordinates where the sprite will be drawn. Preserves the origin.
	 */
	public void translate (float xAmount, float yAmount) {
		originX += xAmount;
		originY += yAmount;

		float x = vertices[X1] + xAmount;
		float y = vertices[Y1] + yAmount;
		float x2 = x + width;
		float y2 = y + height;

		vertices[X1] = x;
		vertices[Y1] = y;

		vertices[X2] = x;
		vertices[Y2] = y2;

		vertices[X3] = x2;
		vertices[Y3] = y2;

		vertices[X4] = x2;
		vertices[Y4] = y;
	}

	public void setTextureBounds (int srcX, int srcY, int srcWidth, int srcHeight) {
		float invTexWidth = 1.0f / texture.getWidth();
		float invTexHeight = 1.0f / texture.getHeight();
		float u = srcX * invTexWidth;
		float v = (srcY + srcHeight) * invTexHeight;
		float u2 = (srcX + srcWidth) * invTexWidth;
		float v2 = srcY * invTexHeight;

		vertices[U1] = u;
		vertices[V1] = v;

		vertices[U2] = u;
		vertices[V2] = v2;

		vertices[U3] = u2;
		vertices[V3] = v2;

		vertices[U4] = u2;
		vertices[V4] = v;
	}

	public void setColor (Color tint) {
		float color = tint.toFloatBits();
		vertices[C1] = color;
		vertices[C2] = color;
		vertices[C3] = color;
		vertices[C4] = color;
	}

	public void setColor (float r, float g, float b, float a) {
		int intBits = ((int)(255 * a) << 24) | //
			((int)(255 * b) << 16) | //
			((int)(255 * g) << 8) | //
			((int)(255 * r));
		float color = Float.intBitsToFloat(intBits);
		vertices[C1] = color;
		vertices[C2] = color;
		vertices[C3] = color;
		vertices[C4] = color;
	}

	public void setWrap (boolean x, boolean y) {
		texture.bind();
		GL10 gl = Gdx.graphics.getGL10();
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, x ? GL10.GL_REPEAT : GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, y ? GL10.GL_REPEAT : GL10.GL_CLAMP_TO_EDGE);
	}

	public void flip (boolean x, boolean y) {
		if (x) {
			float u = vertices[U1];
			float u2 = vertices[U3];
			vertices[U1] = u2;
			vertices[U2] = u2;
			vertices[U3] = u;
			vertices[U4] = u;
		}
		if (y) {
			float v = vertices[V1];
			float v2 = vertices[V3];
			vertices[V1] = v2;
			vertices[V2] = v;
			vertices[V3] = v;
			vertices[V4] = v2;
		}
	}

	public void scrollTexture (float xAmount, float yAmount) {
		if (xAmount > 0) {
			float u = (vertices[2] + xAmount) % 1;
			float u2 = u + width / texture.getWidth();
			vertices[U1] = u;
			vertices[U2] = u;
			vertices[U3] = u2;
			vertices[U4] = u2;
		}
		if (yAmount > 0) {
			float v = (vertices[V1] + yAmount) % 1;
			float v2 = v + height / texture.getHeight();
			vertices[V1] = v;
			vertices[V2] = v2;
			vertices[V3] = v2;
			vertices[V4] = v;
		}
	}

	public void setOrigin (float x, float y) {
		originX = x;
		originY = y;
	}

	public void rotate (float degrees) {
		float cos = MathUtils.cosDeg(degrees);
		float sin = MathUtils.sinDeg(degrees);
		float x, y;

		x = vertices[X1] - originX;
		y = vertices[Y1] - originY;
		vertices[X1] = x * cos - y * sin + originX;
		vertices[Y1] = y * cos + x * sin + originY;

		x = vertices[X2] - originX;
		y = vertices[Y2] - originY;
		vertices[X2] = x * cos - y * sin + originX;
		vertices[Y2] = y * cos + x * sin + originY;

		x = vertices[X3] - originX;
		y = vertices[Y3] - originY;
		vertices[X3] = x * cos - y * sin + originX;
		vertices[Y3] = y * cos + x * sin + originY;

		x = vertices[X4] - originX;
		y = vertices[Y4] - originY;
		vertices[X4] = x * cos - y * sin + originX;
		vertices[Y4] = y * cos + x * sin + originY;
	}

	public void scale (float scale) {
		vertices[X1] = originX + (vertices[X1] - originX) * scale;
		vertices[Y1] = originY + (vertices[Y1] - originY) * scale;

		vertices[X2] = originX + (vertices[X2] - originX) * scale;
		vertices[Y2] = originY + (vertices[Y2] - originY) * scale;

		vertices[X3] = originX + (vertices[X3] - originX) * scale;
		vertices[Y3] = originY + (vertices[Y3] - originY) * scale;

		vertices[X4] = originX + (vertices[X4] - originX) * scale;
		vertices[Y4] = originY + (vertices[Y4] - originY) * scale;
	}

	public void destroy () {
		texture.dispose(); // BOZO - Child images?
	}

	static private final int X1 = 0;
	static private final int Y1 = 1;
	static private final int C1 = 2;
	static private final int U1 = 3;
	static private final int V1 = 4;
	static private final int X2 = 5;
	static private final int Y2 = 6;
	static private final int C2 = 7;
	static private final int U2 = 8;
	static private final int V2 = 9;
	static private final int X3 = 10;
	static private final int Y3 = 11;
	static private final int C3 = 12;
	static private final int U3 = 13;
	static private final int V3 = 14;
	static private final int X4 = 15;
	static private final int Y4 = 16;
	static private final int C4 = 17;
	static private final int U4 = 18;
	static private final int V4 = 19;
}
