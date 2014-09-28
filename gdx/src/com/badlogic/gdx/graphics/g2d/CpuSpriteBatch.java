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

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** CpuSpriteBatch behaves like SpriteBatch, except it doesn't flush automatically whenever the transformation matrix changes.
 * Instead, the vertices get adjusted on subsequent draws to match the running batch. This can improve performance through longer
 * batches, for example when drawing Groups with transform enabled.
 *
 * @see SpriteBatch#renderCalls
 * @see com.badlogic.gdx.scenes.scene2d.Group#setTransform(boolean) Group.setTransform()
 * @author Valentin Milea */
public class CpuSpriteBatch extends SpriteBatch {

	private final Matrix4 virtualMatrix = new Matrix4();
	private final Affine2 adjustAffine = new Affine2();
	private boolean haveVirtualMatrix;

	private final FloatArray vertexBuffer = new FloatArray();
	private final Affine2 tmpAffine = new Affine2();

	/** Constructs a CpuSpriteBatch with a size of 1000 and the default shader.
	 * @see SpriteBatch#SpriteBatch() */
	public CpuSpriteBatch () {
		this(1000);
	}

	/** Constructs a CpuSpriteBatch with the default shader.
	 * @see SpriteBatch#SpriteBatch(int) */
	public CpuSpriteBatch (int size) {
		this(size, null);
	}

	/** Constructs a CpuSpriteBatch with a custom shader.
	 * @see SpriteBatch#SpriteBatch(int, ShaderProgram) */
	public CpuSpriteBatch (int size, ShaderProgram defaultShader) {
		super(size, defaultShader);
		vertexBuffer.ensureCapacity(Sprite.SPRITE_SIZE);
	}

	@Override
	public void flush () {
		super.flush();

		if (haveVirtualMatrix && vertexBuffer.size == 0) {
			// done rendering, safe now to replace matrix
			haveVirtualMatrix = false;
			super.setTransformMatrix(virtualMatrix);
		}
	}

	@Override
	public Matrix4 getTransformMatrix () {
		return (haveVirtualMatrix ? virtualMatrix : super.getTransformMatrix());
	}

	/** Sets the transform matrix to be used by this Batch. Even if this is called inside a {@link #begin()}/{@link #end()} block,
	 * the current batch is <em>not</em> flushed to the gpu. Instead, for every subsequent draw() the vertices will be transformed
	 * on the CPU to match the original batch matrix.
	 * <p>
	 * After the batch is flushed or the original transform restored, this ajustment is no longer needed, so regular behavior
	 * (identical to SpriteBatch) resumes.
	 * </p> */
	@Override
	public void setTransformMatrix (Matrix4 transform) {
		Matrix4 realMatrix = super.getTransformMatrix();

		if (checkEqual(realMatrix, transform)) {
			haveVirtualMatrix = false;
		} else {
			if (isDrawing()) {
				virtualMatrix.set(transform);
				haveVirtualMatrix = true;

				// adjust = inverse(real) x virtual
				// real x adjust x vertex = virtual x vertex

				adjustAffine.set(realMatrix).inv();
				tmpAffine.set(transform);
				adjustAffine.mul(tmpAffine);
			} else {
				realMatrix.set(transform);
			}
		}
	}

	/** Sets the transform matrix to be used by this Batch. Even if this is called inside a {@link #begin()}/{@link #end()} block,
	 * the current batch is <em>not</em> flushed to the gpu. Instead, for every subsequent draw() the vertices will be transformed
	 * on the CPU to match the original batch matrix.
	 * <p>
	 * After the batch is flushed or the original transform restored, this ajustment is no longer needed, so regular behavior
	 * (identical to SpriteBatch) resumes.
	 * </p> */
	public void setTransformMatrix (Affine2 transform) {
		Matrix4 realMatrix = super.getTransformMatrix();

		if (checkEqual(realMatrix, transform)) {
			haveVirtualMatrix = false;
		} else {
			virtualMatrix.set(transform);

			if (isDrawing()) {
				haveVirtualMatrix = true;

				// adjust = inverse(real) x virtual
				// real x adjust x vertex = virtual x vertex

				adjustAffine.set(realMatrix).inv().mul(transform);
			} else {
				realMatrix.set(transform);
			}
		}
	}

	@Override
	public void draw (Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX,
		float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
		if (!haveVirtualMatrix) {
			super.draw(texture, x, y, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY, srcWidth, srcHeight,
				flipX, flipY);
		} else {
			fillVertices(texture, x, y, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY, srcWidth, srcHeight,
				flipX, flipY);
			adjustVertices();
			drawVertices(texture);
		}
	}

	@Override
	public void draw (Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
		int srcHeight, boolean flipX, boolean flipY) {
		if (!haveVirtualMatrix) {
			super.draw(texture, x, y, width, height, srcX, srcY, srcWidth, srcHeight, flipX, flipY);
		} else {
			fillVertices(texture, x, y, 0, 0, width, height, 1, 1, 0, srcX, srcY, srcWidth, srcHeight, flipX, flipY);
			adjustVertices();
			drawVertices(texture);
		}
	}

	@Override
	public void draw (Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
		if (!haveVirtualMatrix) {
			super.draw(texture, x, y, srcX, srcY, srcWidth, srcHeight);
		} else {
			fillVertices(texture, x, y, 0, 0, texture.getWidth(), texture.getHeight(), 1, 1, 0, srcX, srcY, srcWidth, srcHeight,
				false, false);
			adjustVertices();
			drawVertices(texture);
		}
	}

	@Override
	public void draw (Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
		if (!haveVirtualMatrix) {
			super.draw(texture, x, y, width, height, u, v, u2, v2);
		} else {
			fillVerticesUV(texture, x, y, 0, 0, texture.getWidth(), texture.getHeight(), 1, 1, 0, u, v, u2, v2, false, false);
			adjustVertices();
			drawVertices(texture);
		}
	}

	@Override
	public void draw (Texture texture, float x, float y) {
		if (!haveVirtualMatrix) {
			super.draw(texture, x, y);
		} else {
			fillVertices(texture, x, y, 0, 0, texture.getWidth(), texture.getHeight(), 1, 1, 0, 0, 1, 1, 0, false, false);
			adjustVertices();
			drawVertices(texture);
		}
	}

	@Override
	public void draw (Texture texture, float x, float y, float width, float height) {
		if (!haveVirtualMatrix) {
			super.draw(texture, x, y, width, height);
		} else {
			fillVertices(texture, x, y, 0, 0, width, height, 1, 1, 0, 0, 1, 1, 0, false, false);
			adjustVertices();
			drawVertices(texture);
		}
	}

	@Override
	public void draw (TextureRegion region, float x, float y) {
		if (!haveVirtualMatrix) {
			super.draw(region, x, y);
		} else {
			fillVertices(region, x, y, 0, 0, region.getRegionWidth(), region.getRegionHeight(), 1, 1, 0);
			adjustVertices();
			drawVertices(region.getTexture());
		}
	}

	@Override
	public void draw (TextureRegion region, float x, float y, float width, float height) {
		if (!haveVirtualMatrix) {
			super.draw(region, x, y, width, height);
		} else {
			fillVertices(region, x, y, 0, 0, width, height, 1, 1, 0);
			adjustVertices();
			drawVertices(region.getTexture());
		}
	}

	@Override
	public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation) {
		if (!haveVirtualMatrix) {
			super.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
		} else {
			fillVertices(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
			adjustVertices();
			drawVertices(region.getTexture());
		}
	}

	@Override
	public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation, boolean clockwise) {

		draw(region, x, y, originX, originY, width, height, scaleX, scaleY, (clockwise ? -rotation : rotation));
	}

	@Override
	public void draw (Texture texture, float[] spriteVertices, int offset, int count) {
		if (count % Sprite.SPRITE_SIZE != 0) throw new GdxRuntimeException("invalid vertex count");

		if (!haveVirtualMatrix) {
			super.draw(texture, spriteVertices, offset, count);
		} else {
			fillVertices(spriteVertices, offset, count);
			adjustVertices();
			drawVertices(texture);
		}

	}

	private void drawVertices (Texture texture) {
		// draw the adjusted vertices
		super.draw(texture, vertexBuffer.items, 0, vertexBuffer.size);
		vertexBuffer.clear();
	}

	private void fillVertices (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation) {

		fillVertices(region.getTexture(), x, y, originX, originY, width, height, scaleX, scaleY, rotation, region.getRegionX(),
			region.getRegionY(), region.getRegionWidth(), region.getRegionHeight(), false, false);
	}

	private void fillVertices (Texture texture, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {

		float invTexWidth = 1.0f / texture.getWidth();
		float invTexHeight = 1.0f / texture.getHeight();

		float u = srcX * invTexWidth;
		float v = (srcY + srcHeight) * invTexHeight;
		float u2 = (srcX + srcWidth) * invTexWidth;
		float v2 = srcY * invTexHeight;

		fillVerticesUV(texture, x, y, originX, originY, width, height, scaleX, scaleY, rotation, u, v, u2, v2, flipX, flipY);
	}

	private void fillVerticesUV (Texture texture, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation, float u, float v, float u2, float v2, boolean flipX, boolean flipY) {

		float[] vertices = this.vertexBuffer.items;

		// bottom left and top right corner points relative to origin
		final float worldOriginX = x + originX;
		final float worldOriginY = y + originY;
		float fx = -originX;
		float fy = -originY;
		float fx2 = width - originX;
		float fy2 = height - originY;

		// scale
		if (scaleX != 1 || scaleY != 1) {
			fx *= scaleX;
			fy *= scaleY;
			fx2 *= scaleX;
			fy2 *= scaleY;
		}

		// construct corner points, start from top left and go counter clockwise
		final float p1x = fx;
		final float p1y = fy;
		final float p2x = fx;
		final float p2y = fy2;
		final float p3x = fx2;
		final float p3y = fy2;
		final float p4x = fx2;
		final float p4y = fy;

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
			final float cos = MathUtils.cosDeg(rotation);
			final float sin = MathUtils.sinDeg(rotation);

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

		if (flipX) {
			float tmp = u;
			u = u2;
			u2 = tmp;
		}
		if (flipY) {
			float tmp = v;
			v = v2;
			v2 = tmp;
		}

		vertices[0] = x1;
		vertices[1] = y1;
		vertices[2] = color;
		vertices[3] = u;
		vertices[4] = v;

		vertices[5] = x2;
		vertices[6] = y2;
		vertices[7] = color;
		vertices[8] = u;
		vertices[9] = v2;

		vertices[10] = x3;
		vertices[11] = y3;
		vertices[12] = color;
		vertices[13] = u2;
		vertices[14] = v2;

		vertices[15] = x4;
		vertices[16] = y4;
		vertices[17] = color;
		vertices[18] = u2;
		vertices[19] = v;

		vertexBuffer.size = Sprite.SPRITE_SIZE;
	}

	private void fillVertices (float[] spriteVertices, int offset, int count) {
		vertexBuffer.size = 0;
		vertexBuffer.ensureCapacity(count);
		System.arraycopy(spriteVertices, offset, vertexBuffer.items, 0, count);

		vertexBuffer.size = count;
	}

	private void adjustVertices () {
		final float m00 = adjustAffine.m00;
		final float m10 = adjustAffine.m10;
		final float m01 = adjustAffine.m01;
		final float m11 = adjustAffine.m11;
		final float m02 = adjustAffine.m02;
		final float m12 = adjustAffine.m12;

		final float[] vertices = vertexBuffer.items;
		final int end = vertexBuffer.size;

		for (int i = 0; i < end; i += Sprite.VERTEX_SIZE) {
			float x = vertices[i];
			float y = vertices[i + 1];

			vertices[i] = m00 * x + m01 * y + m02;
			vertices[i + 1] = m10 * x + m11 * y + m12;
		}
	}

	private static boolean checkEqual (Matrix4 a, Matrix4 b) {
		if (a == b) return true;

		for (int i = 0; i < 16; i++) {
			if (a.val[i] != b.val[i]) return false;
		}
		return true;
	}

	private static boolean checkEqual (Matrix4 matrix, Affine2 affine) {
		float[] val = matrix.getValues();

		return (val[Matrix4.M00] == affine.m00 && val[Matrix4.M10] == affine.m10 && val[Matrix4.M01] == affine.m01
			&& val[Matrix4.M11] == affine.m11 && val[Matrix4.M03] == affine.m02 && val[Matrix4.M13] == affine.m12);
	}
}
