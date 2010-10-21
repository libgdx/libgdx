
package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.MathUtils;

public class Sprite {
	Texture texture;
	private float[] vertices = new float[20];
	private float x, y;
	private float width, height;
	private float originX, originY;
	private float rotation;
	private float scaleX = 1, scaleY = 1;
	private boolean dirty;

	public Sprite (Texture texture) {
		this(texture, 0, 0, texture.getWidth(), texture.getHeight());
	}

	public Sprite (Texture texture, int srcWidth, int srcHeight) {
		this(texture, 0, 0, srcWidth, srcHeight);
	}

	public Sprite (Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
		this.texture = texture;
		setTextureRegion(srcX, srcY, srcWidth, srcHeight);
		setColor(1, 1, 1, 1);
		setBounds(0, 0, srcWidth, srcHeight);
		setOrigin(srcWidth / 2, srcHeight / 2);
	}

	public void setOrigin (float originX, float originY) {
		this.originX = originX;
		this.originY = originY;
		dirty = true;
	}

	public void setPosition (float x, float y) {
		translate(x - this.x, y - this.y);
	}

	public void setBounds (float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		float x2 = x + width;
		float y2 = y + height;
		float[] vertices = this.vertices;
		vertices[X1] = x;
		vertices[Y1] = y;

		vertices[X2] = x;
		vertices[Y2] = y2;

		vertices[X3] = x2;
		vertices[Y3] = y2;

		vertices[X4] = x2;
		vertices[Y4] = y;

		if (rotation != 0 || scaleX != 1 || scaleY != 1) dirty = true;
	}

	public void translate (float xAmount, float yAmount) {
		x += xAmount;
		y += yAmount;

		float[] vertices = this.vertices;
		vertices[X1] += xAmount;
		vertices[Y1] += yAmount;

		vertices[X2] += xAmount;
		vertices[Y2] += yAmount;

		vertices[X3] += xAmount;
		vertices[Y3] += yAmount;

		vertices[X4] += xAmount;
		vertices[Y4] += yAmount;
	}

	public void setTextureRegion (int srcX, int srcY, int srcWidth, int srcHeight) {
		float invTexWidth = 1.0f / texture.getWidth();
		float invTexHeight = 1.0f / texture.getHeight();
		float u = srcX * invTexWidth;
		float v = (srcY + srcHeight) * invTexHeight;
		float u2 = (srcX + srcWidth) * invTexWidth;
		float v2 = srcY * invTexHeight;

		float[] vertices = this.vertices;
		vertices[U1] = u;
		vertices[V1] = v;

		vertices[U2] = u;
		vertices[V2] = v2;

		vertices[U3] = u2;
		vertices[V3] = v2;

		vertices[U4] = u2;
		vertices[V4] = v;
	}

	public void setTextureRepeat (boolean x, boolean y) {
		texture.bind();
		GL10 gl = Gdx.graphics.getGL10();
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, x ? GL10.GL_REPEAT : GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, y ? GL10.GL_REPEAT : GL10.GL_CLAMP_TO_EDGE);
	}

	public void setColor (Color tint) {
		float color = tint.toFloatBits();
		float[] vertices = this.vertices;
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
		float[] vertices = this.vertices;
		vertices[C1] = color;
		vertices[C2] = color;
		vertices[C3] = color;
		vertices[C4] = color;
	}

	public void flip (boolean x, boolean y) {
		float[] vertices = this.vertices;
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
		float[] vertices = this.vertices;
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

	public void setRotation (float rotation) {
		this.rotation = rotation;
		dirty = true;
	}

	public void rotate (float degrees) {
		rotation += degrees;
		dirty = true;
	}

	public void setScale (float scale) {
		this.scaleX = this.scaleY = scale;
		dirty = true;
	}

	public void setScale (float scaleX, float scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		dirty = true;
	}

	public void scale (float amount) {
		this.scaleX += amount;
		this.scaleY += amount;
		dirty = true;
	}

	protected final void computeVertices (final float[] out, final int offset) {
		if (dirty) {
			dirty = false;

			float[] vertices = this.vertices;
			float localX = -originX * scaleX;
			float localY = -originY * scaleY;
			float localX2 = (-originX + width) * scaleX;
			float localY2 = (-originY + height) * scaleY;
			float cos = MathUtils.cosDeg(rotation);
			float sin = MathUtils.sinDeg(rotation);
			float worldOriginX = this.x + originX;
			float worldOriginY = this.y + originY;

			vertices[X1] = localX * cos - localY * sin + worldOriginX;
			vertices[Y1] = localY * cos + localX * sin + worldOriginY;

			vertices[X2] = localX * cos - localY2 * sin + worldOriginX;
			vertices[Y2] = localY2 * cos + localX * sin + worldOriginY;

			vertices[X3] = localX2 * cos - localY2 * sin + worldOriginX;
			vertices[Y3] = localY2 * cos + localX2 * sin + worldOriginY;

			vertices[X4] = localX2 * cos - localY * sin + worldOriginX;
			vertices[Y4] = localY * cos + localX2 * sin + worldOriginY;
		}
		System.arraycopy(vertices, 0, out, offset, 20);
	}

	public Texture getTexture () {
		return texture;
	}

	public float getX () {
		return x;
	}

	public float getY () {
		return y;
	}

	public float getWidth () {
		return width;
	}

	public float getHeight () {
		return height;
	}

	public float getOriginX () {
		return originX;
	}

	public float getOriginY () {
		return originY;
	}

	public float getRotation () {
		return rotation;
	}

	public float getScaleX () {
		return scaleX;
	}

	public float getScaleY () {
		return scaleY;
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
