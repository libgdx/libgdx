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
	private boolean adjustNeeded;
	private boolean haveIdentityRealMatrix = true;

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
	}

	/** <p>
	 * Flushes the batch and realigns the real matrix on the GPU. Subsequent draws won't need adjustment and will be slightly
	 * faster as long as the transform matrix is not {@link #setTransformMatrix(Matrix4) changed}.
	 * </p>
	 * <p>
	 * Note: The real transform matrix <em>must</em> be invertible. If a singular matrix is detected, GdxRuntimeException will be
	 * thrown.
	 * </p>
	 * @see SpriteBatch#flush() */
	public void flushAndSyncTransformMatrix () {
		flush();

		if (adjustNeeded) {
			// vertices flushed, safe now to replace matrix
			haveIdentityRealMatrix = checkIdt(virtualMatrix);

			if (!haveIdentityRealMatrix && virtualMatrix.det() == 0)
				throw new GdxRuntimeException("Transform matrix is singular, can't sync");

			adjustNeeded = false;
			super.setTransformMatrix(virtualMatrix);
		}
	}

	@Override
	public Matrix4 getTransformMatrix () {
		return (adjustNeeded ? virtualMatrix : super.getTransformMatrix());
	}

	/** Sets the transform matrix to be used by this Batch. Even if this is called inside a {@link #begin()}/{@link #end()} block,
	 * the current batch is <em>not</em> flushed to the GPU. Instead, for every subsequent draw() the vertices will be transformed
	 * on the CPU to match the original batch matrix. This adjustment must be performed until the matrices are realigned by
	 * restoring the original matrix, or by calling {@link #flushAndSyncTransformMatrix()}. */
	@Override
	public void setTransformMatrix (Matrix4 transform) {
		Matrix4 realMatrix = super.getTransformMatrix();

		if (checkEqual(realMatrix, transform)) {
			adjustNeeded = false;
		} else {
			if (isDrawing()) {
				virtualMatrix.setAsAffine(transform);
				adjustNeeded = true;

				// adjust = inverse(real) x virtual
				// real x adjust x vertex = virtual x vertex

				if (haveIdentityRealMatrix) {
					adjustAffine.set(transform);
				} else {
					tmpAffine.set(transform);
					adjustAffine.set(realMatrix).inv().mul(tmpAffine);
				}
			} else {
				realMatrix.setAsAffine(transform);
				haveIdentityRealMatrix = checkIdt(realMatrix);
			}
		}
	}

	/** Sets the transform matrix to be used by this Batch. Even if this is called inside a {@link #begin()}/{@link #end()} block,
	 * the current batch is <em>not</em> flushed to the GPU. Instead, for every subsequent draw() the vertices will be transformed
	 * on the CPU to match the original batch matrix. This adjustment must be performed until the matrices are realigned by
	 * restoring the original matrix, or by calling {@link #flushAndSyncTransformMatrix()} or {@link #end()}. */
	public void setTransformMatrix (Affine2 transform) {
		Matrix4 realMatrix = super.getTransformMatrix();

		if (checkEqual(realMatrix, transform)) {
			adjustNeeded = false;
		} else {
			virtualMatrix.setAsAffine(transform);

			if (isDrawing()) {
				adjustNeeded = true;

				// adjust = inverse(real) x virtual
				// real x adjust x vertex = virtual x vertex

				if (haveIdentityRealMatrix) {
					adjustAffine.set(transform);
				} else {
					adjustAffine.set(realMatrix).inv().mul(transform);
				}
			} else {
				realMatrix.setAsAffine(transform);
				haveIdentityRealMatrix = checkIdt(realMatrix);
			}
		}
	}

	@Override
	public void draw (Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX,
		float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
		if (!adjustNeeded) {
			super.draw(texture, x, y, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY, srcWidth, srcHeight,
				flipX, flipY);
		} else {
			drawAdjusted(texture, x, y, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY, srcWidth, srcHeight,
				flipX, flipY);
		}
	}

	@Override
	public void draw (Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
		int srcHeight, boolean flipX, boolean flipY) {
		if (!adjustNeeded) {
			super.draw(texture, x, y, width, height, srcX, srcY, srcWidth, srcHeight, flipX, flipY);
		} else {
			drawAdjusted(texture, x, y, 0, 0, width, height, 1, 1, 0, srcX, srcY, srcWidth, srcHeight, flipX, flipY);
		}
	}

	@Override
	public void draw (Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
		if (!adjustNeeded) {
			super.draw(texture, x, y, srcX, srcY, srcWidth, srcHeight);
		} else {
			drawAdjusted(texture, x, y, 0, 0, srcWidth, srcHeight, 1, 1, 0, srcX, srcY, srcWidth, srcHeight,
				false, false);
		}
	}

	@Override
	public void draw (Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
		if (!adjustNeeded) {
			super.draw(texture, x, y, width, height, u, v, u2, v2);
		} else {
			drawAdjustedUV(texture, x, y, 0, 0, width, height, 1, 1, 0, u, v, u2, v2, false, false);
		}
	}

	@Override
	public void draw (Texture texture, float x, float y) {
		if (!adjustNeeded) {
			super.draw(texture, x, y);
		} else {
			drawAdjusted(texture, x, y, 0, 0, texture.getWidth(), texture.getHeight(), 1, 1, 0, 0, 1, 1, 0, false, false);
		}
	}

	@Override
	public void draw (Texture texture, float x, float y, float width, float height) {
		if (!adjustNeeded) {
			super.draw(texture, x, y, width, height);
		} else {
			drawAdjusted(texture, x, y, 0, 0, width, height, 1, 1, 0, 0, 1, 1, 0, false, false);
		}
	}

	@Override
	public void draw (TextureRegion region, float x, float y) {
		if (!adjustNeeded) {
			super.draw(region, x, y);
		} else {
			drawAdjusted(region, x, y, 0, 0, region.getRegionWidth(), region.getRegionHeight(), 1, 1, 0);
		}
	}

	@Override
	public void draw (TextureRegion region, float x, float y, float width, float height) {
		if (!adjustNeeded) {
			super.draw(region, x, y, width, height);
		} else {
			drawAdjusted(region, x, y, 0, 0, width, height, 1, 1, 0);
		}
	}

	@Override
	public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation) {
		if (!adjustNeeded) {
			super.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
		} else {
			drawAdjusted(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
		}
	}

	@Override
	public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation, boolean clockwise) {
		if (!adjustNeeded) {
			super.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, clockwise);
		} else {
			drawAdjusted(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, clockwise);
		}
	}

	@Override
	public void draw (Texture texture, float[] spriteVertices, int offset, int count) {
		if (count % Sprite.SPRITE_SIZE != 0) throw new GdxRuntimeException("invalid vertex count");

		if (!adjustNeeded) {
			super.draw(texture, spriteVertices, offset, count);
		} else {
			drawAdjusted(texture, spriteVertices, offset, count);
		}
	}

	@Override
	public void draw (TextureRegion region, float width, float height, Affine2 transform) {
		if (!adjustNeeded) {
			super.draw(region, width, height, transform);
		} else {
			drawAdjusted(region, width, height, transform);
		}
	}

	private void drawAdjusted (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation) {
		// v must be flipped
		drawAdjustedUV(region.texture, x, y, originX, originY, width, height, scaleX, scaleY, rotation, region.u, region.v2,
			region.u2, region.v, false, false);
	}

	private void drawAdjusted (Texture texture, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
		float invTexWidth = 1.0f / texture.getWidth();
		float invTexHeight = 1.0f / texture.getHeight();

		float u = srcX * invTexWidth;
		float v = (srcY + srcHeight) * invTexHeight;
		float u2 = (srcX + srcWidth) * invTexWidth;
		float v2 = srcY * invTexHeight;

		drawAdjustedUV(texture, x, y, originX, originY, width, height, scaleX, scaleY, rotation, u, v, u2, v2, flipX, flipY);
	}

	private void drawAdjustedUV (Texture texture, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation, float u, float v, float u2, float v2, boolean flipX, boolean flipY) {
		if (!drawing) throw new IllegalStateException("CpuSpriteBatch.begin must be called before draw.");

		if (texture != lastTexture)
			switchTexture(texture);
		else if (idx == vertices.length) super.flush();

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

		Affine2 t = adjustAffine;

		vertices[idx + 0] = t.m00 * x1 + t.m01 * y1 + t.m02;
		vertices[idx + 1] = t.m10 * x1 + t.m11 * y1 + t.m12;
		vertices[idx + 2] = color;
		vertices[idx + 3] = u;
		vertices[idx + 4] = v;

		vertices[idx + 5] = t.m00 * x2 + t.m01 * y2 + t.m02;
		vertices[idx + 6] = t.m10 * x2 + t.m11 * y2 + t.m12;
		vertices[idx + 7] = color;
		vertices[idx + 8] = u;
		vertices[idx + 9] = v2;

		vertices[idx + 10] = t.m00 * x3 + t.m01 * y3 + t.m02;
		vertices[idx + 11] = t.m10 * x3 + t.m11 * y3 + t.m12;
		vertices[idx + 12] = color;
		vertices[idx + 13] = u2;
		vertices[idx + 14] = v2;

		vertices[idx + 15] = t.m00 * x4 + t.m01 * y4 + t.m02;
		vertices[idx + 16] = t.m10 * x4 + t.m11 * y4 + t.m12;
		vertices[idx + 17] = color;
		vertices[idx + 18] = u2;
		vertices[idx + 19] = v;

		idx += Sprite.SPRITE_SIZE;
	}

	private void drawAdjusted (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation, boolean clockwise) {
		if (!drawing) throw new IllegalStateException("CpuSpriteBatch.begin must be called before draw.");

		if (region.texture != lastTexture)
			switchTexture(region.texture);
		else if (idx == vertices.length) super.flush();

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

		float u1, v1, u2, v2, u3, v3, u4, v4;
		if (clockwise) {
			u1 = region.u2;
			v1 = region.v2;
			u2 = region.u;
			v2 = region.v2;
			u3 = region.u;
			v3 = region.v;
			u4 = region.u2;
			v4 = region.v;
		} else {
			u1 = region.u;
			v1 = region.v;
			u2 = region.u2;
			v2 = region.v;
			u3 = region.u2;
			v3 = region.v2;
			u4 = region.u;
			v4 = region.v2;
		}

		Affine2 t = adjustAffine;

		vertices[idx + 0] = t.m00 * x1 + t.m01 * y1 + t.m02;
		vertices[idx + 1] = t.m10 * x1 + t.m11 * y1 + t.m12;
		vertices[idx + 2] = color;
		vertices[idx + 3] = u1;
		vertices[idx + 4] = v1;

		vertices[idx + 5] = t.m00 * x2 + t.m01 * y2 + t.m02;
		vertices[idx + 6] = t.m10 * x2 + t.m11 * y2 + t.m12;
		vertices[idx + 7] = color;
		vertices[idx + 8] = u2;
		vertices[idx + 9] = v2;

		vertices[idx + 10] = t.m00 * x3 + t.m01 * y3 + t.m02;
		vertices[idx + 11] = t.m10 * x3 + t.m11 * y3 + t.m12;
		vertices[idx + 12] = color;
		vertices[idx + 13] = u3;
		vertices[idx + 14] = v3;

		vertices[idx + 15] = t.m00 * x4 + t.m01 * y4 + t.m02;
		vertices[idx + 16] = t.m10 * x4 + t.m11 * y4 + t.m12;
		vertices[idx + 17] = color;
		vertices[idx + 18] = u4;
		vertices[idx + 19] = v4;

		idx += Sprite.SPRITE_SIZE;
	}

	private void drawAdjusted (TextureRegion region, float width, float height, Affine2 transform) {
		if (!drawing) throw new IllegalStateException("CpuSpriteBatch.begin must be called before draw.");

		if (region.texture != lastTexture)
			switchTexture(region.texture);
		else if (idx == vertices.length) super.flush();

		Affine2 t = transform;

		// construct corner points
		float x1 = t.m02;
		float y1 = t.m12;
		float x2 = t.m01 * height + t.m02;
		float y2 = t.m11 * height + t.m12;
		float x3 = t.m00 * width + t.m01 * height + t.m02;
		float y3 = t.m10 * width + t.m11 * height + t.m12;
		float x4 = t.m00 * width + t.m02;
		float y4 = t.m10 * width + t.m12;

		// v must be flipped
		float u = region.u;
		float v = region.v2;
		float u2 = region.u2;
		float v2 = region.v;

		t = adjustAffine;

		vertices[idx + 0] = t.m00 * x1 + t.m01 * y1 + t.m02;
		vertices[idx + 1] = t.m10 * x1 + t.m11 * y1 + t.m12;
		vertices[idx + 2] = color;
		vertices[idx + 3] = u;
		vertices[idx + 4] = v;

		vertices[idx + 5] = t.m00 * x2 + t.m01 * y2 + t.m02;
		vertices[idx + 6] = t.m10 * x2 + t.m11 * y2 + t.m12;
		vertices[idx + 7] = color;
		vertices[idx + 8] = u;
		vertices[idx + 9] = v2;

		vertices[idx + 10] = t.m00 * x3 + t.m01 * y3 + t.m02;
		vertices[idx + 11] = t.m10 * x3 + t.m11 * y3 + t.m12;
		vertices[idx + 12] = color;
		vertices[idx + 13] = u2;
		vertices[idx + 14] = v2;

		vertices[idx + 15] = t.m00 * x4 + t.m01 * y4 + t.m02;
		vertices[idx + 16] = t.m10 * x4 + t.m11 * y4 + t.m12;
		vertices[idx + 17] = color;
		vertices[idx + 18] = u2;
		vertices[idx + 19] = v;

		idx += Sprite.SPRITE_SIZE;
	}

	private void drawAdjusted (Texture texture, float[] spriteVertices, int offset, int count) {
		if (!drawing) throw new IllegalStateException("CpuSpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) switchTexture(texture);

		Affine2 t = adjustAffine;

		int copyCount = Math.min(vertices.length - idx, count);
		do {
			count -= copyCount;
			while (copyCount > 0) {
				float x = spriteVertices[offset];
				float y = spriteVertices[offset + 1];

				vertices[idx] = t.m00 * x + t.m01 * y + t.m02; // x
				vertices[idx + 1] = t.m10 * x + t.m11 * y + t.m12; // y
				vertices[idx + 2] = spriteVertices[offset + 2]; // color
				vertices[idx + 3] = spriteVertices[offset + 3]; // u
				vertices[idx + 4] = spriteVertices[offset + 4]; // v

				idx += Sprite.VERTEX_SIZE;
				offset += Sprite.VERTEX_SIZE;
				copyCount -= Sprite.VERTEX_SIZE;
			}

			if (count > 0) {
				super.flush();
				copyCount = Math.min(vertices.length, count);
			}
		} while (count > 0);
	}

	private static boolean checkEqual (Matrix4 a, Matrix4 b) {
		if (a == b) return true;

		// matrices are assumed to be 2D transformations
		return (a.val[Matrix4.M00] == b.val[Matrix4.M00] && a.val[Matrix4.M10] == b.val[Matrix4.M10]
			&& a.val[Matrix4.M01] == b.val[Matrix4.M01] && a.val[Matrix4.M11] == b.val[Matrix4.M11]
			&& a.val[Matrix4.M03] == b.val[Matrix4.M03] && a.val[Matrix4.M13] == b.val[Matrix4.M13]);
	}

	private static boolean checkEqual (Matrix4 matrix, Affine2 affine) {
		final float[] val = matrix.getValues();

		// matrix is assumed to be 2D transformation
		return (val[Matrix4.M00] == affine.m00 && val[Matrix4.M10] == affine.m10 && val[Matrix4.M01] == affine.m01
			&& val[Matrix4.M11] == affine.m11 && val[Matrix4.M03] == affine.m02 && val[Matrix4.M13] == affine.m12);
	}

	private static boolean checkIdt (Matrix4 matrix) {
		final float[] val = matrix.getValues();

		// matrix is assumed to be 2D transformation
		return (val[Matrix4.M00] == 1 && val[Matrix4.M10] == 0 && val[Matrix4.M01] == 0 && val[Matrix4.M11] == 1
			&& val[Matrix4.M03] == 0 && val[Matrix4.M13] == 0);
	}
}
