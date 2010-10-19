
package com.badlogic.gdx.twl.renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.SupportsDrawRepeat;

class TextureArea extends TextureAreaBase implements Image, SupportsDrawRepeat {
	private static final FloatBuffer texCoords = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	private static final FloatBuffer vertices = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

	private static final int REPEAT_CACHE_SIZE = 10;

	private final GdxTexture texture;
	private final Color tintColor;
	private final boolean tile;
	private int repeatCacheID = -1;

	public TextureArea (GdxTexture texture, int x, int y, int width, int height, Color tintColor, boolean tile) {
		super(texture.renderer, x, y, width, height, texture.getWidth(), texture.getHeight());
		this.texture = texture;
		this.tintColor = (tintColor == null) ? Color.WHITE : tintColor;
		this.tile = tile;
	}

	TextureArea (TextureArea src, Color tintColor) {
		super(src);
		this.texture = src.texture;
		this.tintColor = tintColor;
		this.tile = src.tile;
	}

	public void draw (AnimationState as, int x, int y) {
		draw(as, x, y, width, height);
	}

	public void draw (AnimationState as, int x, int y, int w, int h) {
		if (texture.bind(tintColor)) {
			if (tile) {
				drawTiled(x, y, w, h);
			} else {
				drawQuad(x, y, w, h);
			}
		}
	}

	public void draw (AnimationState as, int x, int y, int width, int height, int repeatCountX, int repeatCountY) {
		if (texture.bind(tintColor)) {
			if ((repeatCountX * this.width != width) || (repeatCountY * this.height != height)) {
				drawRepeatSlow(x, y, width, height, repeatCountX, repeatCountY);
				return;
			}
			drawRepeat(x, y, repeatCountX, repeatCountY);
		}
	}

	private void drawRepeatSlow (int x, int y, int width, int height, int repeatCountX, int repeatCountY) {
		while (repeatCountY > 0) {
			int rowHeight = height / repeatCountY;

			int cx = 0;
			for (int xi = 0; xi < repeatCountX;) {
				int nx = ++xi * width / repeatCountX;
				drawQuad(x + cx, y, nx - cx, rowHeight);
				cx = nx;
			}

			y += rowHeight;
			height -= rowHeight;
			repeatCountY--;
		}
	}

	private void drawRepeat (int x, int y, int repeatCountX, int repeatCountY) {
		final int w = width;
		final int h = height;
		while (repeatCountY-- > 0) {
			int curX = x;
			int cntX = repeatCountX;
			while (cntX-- > 0) {
				drawQuad(curX, y, w, h);
				curX += w;
			}
			y += h;
		}
	}

	private void drawTiled (int x, int y, int width, int height) {
		int repeatCountX = width / this.width;
		int repeatCountY = height / this.height;

		drawRepeat(x, y, repeatCountX, repeatCountY);

		int drawnX = repeatCountX * this.width;
		int drawnY = repeatCountY * this.height;
		int restWidth = width - drawnX;
		int restHeight = height - drawnY;
		if (restWidth > 0 || restHeight > 0) {
			if (restWidth > 0 && repeatCountY > 0) {
				drawClipped(x + drawnX, y, restWidth, this.height, 1, repeatCountY);
			}
			if (restHeight > 0) {
				if (repeatCountX > 0) {
					drawClipped(x, y + drawnY, this.width, restHeight, repeatCountX, 1);
				}
				if (restWidth > 0) {
					drawClipped(x + drawnX, y + drawnY, restWidth, restHeight, 1, 1);
				}
			}
		}
	}

	private void drawClipped (int x, int y, int width, int height, int repeatCountX, int repeatCountY) {
		float ctx0 = tx0;
		float cty0 = ty0;
		float ctx1 = tx1;
		float cty1 = ty1;
		if (this.width > 1) {
			ctx1 = ctx0 + width / (float)texture.getWidth();
		}
		if (this.height > 1) {
			cty1 = cty0 + height / (float)texture.getHeight();
		}

		texCoords.clear();
		vertices.clear();

		GL10 gl = Gdx.graphics.getGL10();

		while (repeatCountY-- > 0) {
			int y1 = y + height;
			int x0 = x;
			for (int cx = repeatCountX; cx-- > 0;) {
				int x1 = x0 + width;

				texCoords.put(ctx0);
				texCoords.put(cty0);
				vertices.put(x0);
				vertices.put(y);

				texCoords.put(ctx0);
				texCoords.put(cty1);
				vertices.put(x0);
				vertices.put(y1);

				texCoords.put(ctx1);
				texCoords.put(cty1);
				vertices.put(x1);
				vertices.put(y1);

				texCoords.put(ctx1);
				texCoords.put(cty0);
				vertices.put(x1);
				vertices.put(y);

				texCoords.flip();
				vertices.flip();

				gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertices);
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texCoords);
				gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

				x0 = x1;
			}
			y = y1;
		}
	}

	public Image createTintedVersion (Color color) {
		if (color == null) {
			throw new NullPointerException("color");
		}
		Color newTintColor = tintColor.multiply(color);
		if (newTintColor.equals(tintColor)) {
			return this;
		}
		return new TextureArea(this, newTintColor);
	}

}
