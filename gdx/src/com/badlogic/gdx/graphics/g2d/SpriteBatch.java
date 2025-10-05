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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;

import java.nio.Buffer;

/** Draws batched quads using indices.
 * @see Batch
 * @author mzechner
 * @author Nathan Sweet */
public class SpriteBatch implements Batch {
	/** @deprecated Do not use, this field is for testing only and is likely to be removed. Sets the {@link VertexDataType} to be
	 *             used when gles 3 is not available, defaults to {@link VertexDataType#VertexArray}. */
	@Deprecated public static VertexDataType defaultVertexDataType = VertexDataType.VertexBufferObject;

	/** Used to completely override the vertex type used by SpriteBatch. This is useful for picking a specific vertex data type on
	 * construction of the sprite batch. Recommended to reset this back to defaultVertexDataType Once the batch has been created
	 * with this flag */
	@Deprecated public static VertexDataType overrideVertexType = null;

	private VertexDataType currentDataType;

	private Mesh mesh;

	final float[] vertices;
	int idx = 0;
	Texture lastTexture = null;
	float invTexWidth = 0, invTexHeight = 0;

	boolean drawing = false;

	private final Matrix4 transformMatrix = new Matrix4();
	private final Matrix4 projectionMatrix = new Matrix4();
	private final Matrix4 combinedMatrix = new Matrix4();

	private boolean blendingDisabled = false;
	private int blendSrcFunc = GL20.GL_SRC_ALPHA;
	private int blendDstFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;
	private int blendSrcFuncAlpha = GL20.GL_SRC_ALPHA;
	private int blendDstFuncAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA;

	private final ShaderProgram shader;
	private ShaderProgram customShader = null;
	private boolean ownsShader;

	private final Color color = new Color(1, 1, 1, 1);
	float colorPacked = Color.WHITE_FLOAT_BITS;

	/** Number of render calls since the last {@link #begin()}. **/
	public int renderCalls = 0;

	/** Number of rendering calls, ever. Will not be reset unless set manually. **/
	public int totalRenderCalls = 0;

	/** The maximum number of sprites rendered in one batch so far. **/
	public int maxSpritesInBatch = 0;

	/** Constructs a new SpriteBatch with a size of 1000, one buffer, and the default shader.
	 * @see SpriteBatch#SpriteBatch(int, ShaderProgram) */
	public SpriteBatch () {
		this(1000, null);
	}

	/** Constructs a SpriteBatch with one buffer and the default shader.
	 * @see SpriteBatch#SpriteBatch(int, ShaderProgram) */
	public SpriteBatch (int size) {
		this(size, null);
	}

	/** Constructs a new SpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards, x-axis
	 * point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect with
	 * respect to the current screen resolution.
	 * <p>
	 * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
	 * the ones expect for shaders set with {@link #setShader(ShaderProgram)}. See {@link #createDefaultShader()}.
	 * @param size The max number of sprites in a single batch. Max of 8191.
	 * @param defaultShader The default shader to use. This is not owned by the SpriteBatch and must be disposed separately. */
	public SpriteBatch (int size, ShaderProgram defaultShader) {
		// 32767 is max vertex index, so 32767 / 4 vertices per sprite = 8191 sprites max.
		if (size > 8191) throw new IllegalArgumentException("Can't have more than 8191 sprites per batch: " + size);

		VertexDataType vertexDataType = (Gdx.gl30 != null) ? VertexDataType.VertexBufferObjectWithVAO : defaultVertexDataType;

		if (overrideVertexType != null) {
			vertexDataType = overrideVertexType;
		}

		currentDataType = vertexDataType;

		mesh = new Mesh(currentDataType, false, size * 4, size * 6,
			new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
			new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
			new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

		projectionMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		vertices = new float[size * Sprite.SPRITE_SIZE];

		int len = size * 6;
		short[] indices = new short[len];
		short j = 0;
		for (int i = 0; i < len; i += 6, j += 4) {
			indices[i] = j;
			indices[i + 1] = (short)(j + 1);
			indices[i + 2] = (short)(j + 2);
			indices[i + 3] = (short)(j + 2);
			indices[i + 4] = (short)(j + 3);
			indices[i + 5] = j;
		}
		mesh.setIndices(indices);

		if (defaultShader == null) {
			shader = createDefaultShader();
			ownsShader = true;
		} else
			shader = defaultShader;

		// Pre bind the mesh to force the upload of indices data.
		if (vertexDataType != VertexDataType.VertexArray) {
			mesh.getIndexData().bind();
			mesh.getIndexData().unbind();
		}
	}

	/** Returns a new instance of the default shader used by SpriteBatch for GL2 when no shader is specified. */
	static public ShaderProgram createDefaultShader () {
		String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "uniform mat4 u_projTrans;\n" //
			+ "varying vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "   v_color.a = v_color.a * (255.0/254.0);\n" //
			+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "}\n";
		String fragmentShader = "#ifdef GL_ES\n" //
			+ "#define LOWP lowp\n" //
			+ "precision mediump float;\n" //
			+ "#else\n" //
			+ "#define LOWP \n" //
			+ "#endif\n" //
			+ "varying LOWP vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "uniform sampler2D u_texture;\n" //
			+ "void main()\n"//
			+ "{\n" //
			+ "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" //
			+ "}";

		ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
		if (!shader.isCompiled()) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
		return shader;
	}

	@Override
	public void begin () {
		if (drawing) throw new IllegalStateException("SpriteBatch.end must be called before begin.");
		renderCalls = 0;

		Gdx.gl.glDepthMask(false);
		if (customShader != null)
			customShader.bind();
		else
			shader.bind();
		setupMatrices();

		drawing = true;
	}

	@Override
	public void end () {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before end.");
		if (idx > 0) flush();
		lastTexture = null;
		drawing = false;

		GL20 gl = Gdx.gl;
		gl.glDepthMask(true);
		if (isBlendingEnabled()) gl.glDisable(GL20.GL_BLEND);
	}

	@Override
	public void setColor (Color tint) {
		color.set(tint);
		colorPacked = tint.toFloatBits();
	}

	@Override
	public void setColor (float r, float g, float b, float a) {
		color.set(r, g, b, a);
		colorPacked = color.toFloatBits();
	}

	@Override
	public Color getColor () {
		return color;
	}

	@Override
	public void setPackedColor (float packedColor) {
		Color.abgr8888ToColor(color, packedColor);
		this.colorPacked = packedColor;
	}

	@Override
	public float getPackedColor () {
		return colorPacked;
	}

	@Override
	public void draw (Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX,
		float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		float[] vertices = this.vertices;

		if (texture != lastTexture)
			switchTexture(texture);
		else if (idx == vertices.length) //
			flush();

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

		float u = srcX * invTexWidth;
		float v = (srcY + srcHeight) * invTexHeight;
		float u2 = (srcX + srcWidth) * invTexWidth;
		float v2 = srcY * invTexHeight;

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

		float color = this.colorPacked;
		int idx = this.idx;
		vertices[idx] = x1;
		vertices[idx + 1] = y1;
		vertices[idx + 2] = color;
		vertices[idx + 3] = u;
		vertices[idx + 4] = v;

		vertices[idx + 5] = x2;
		vertices[idx + 6] = y2;
		vertices[idx + 7] = color;
		vertices[idx + 8] = u;
		vertices[idx + 9] = v2;

		vertices[idx + 10] = x3;
		vertices[idx + 11] = y3;
		vertices[idx + 12] = color;
		vertices[idx + 13] = u2;
		vertices[idx + 14] = v2;

		vertices[idx + 15] = x4;
		vertices[idx + 16] = y4;
		vertices[idx + 17] = color;
		vertices[idx + 18] = u2;
		vertices[idx + 19] = v;
		this.idx = idx + 20;
	}

	@Override
	public void draw (Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
		int srcHeight, boolean flipX, boolean flipY) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		float[] vertices = this.vertices;

		if (texture != lastTexture)
			switchTexture(texture);
		else if (idx == vertices.length) //
			flush();

		float u = srcX * invTexWidth;
		float v = (srcY + srcHeight) * invTexHeight;
		float u2 = (srcX + srcWidth) * invTexWidth;
		float v2 = srcY * invTexHeight;
		final float fx2 = x + width;
		final float fy2 = y + height;

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

		float color = this.colorPacked;
		int idx = this.idx;
		vertices[idx] = x;
		vertices[idx + 1] = y;
		vertices[idx + 2] = color;
		vertices[idx + 3] = u;
		vertices[idx + 4] = v;

		vertices[idx + 5] = x;
		vertices[idx + 6] = fy2;
		vertices[idx + 7] = color;
		vertices[idx + 8] = u;
		vertices[idx + 9] = v2;

		vertices[idx + 10] = fx2;
		vertices[idx + 11] = fy2;
		vertices[idx + 12] = color;
		vertices[idx + 13] = u2;
		vertices[idx + 14] = v2;

		vertices[idx + 15] = fx2;
		vertices[idx + 16] = y;
		vertices[idx + 17] = color;
		vertices[idx + 18] = u2;
		vertices[idx + 19] = v;
		this.idx = idx + 20;
	}

	@Override
	public void draw (Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		float[] vertices = this.vertices;

		if (texture != lastTexture)
			switchTexture(texture);
		else if (idx == vertices.length) //
			flush();

		final float u = srcX * invTexWidth;
		final float v = (srcY + srcHeight) * invTexHeight;
		final float u2 = (srcX + srcWidth) * invTexWidth;
		final float v2 = srcY * invTexHeight;
		final float fx2 = x + srcWidth;
		final float fy2 = y + srcHeight;

		float color = this.colorPacked;
		int idx = this.idx;
		vertices[idx] = x;
		vertices[idx + 1] = y;
		vertices[idx + 2] = color;
		vertices[idx + 3] = u;
		vertices[idx + 4] = v;

		vertices[idx + 5] = x;
		vertices[idx + 6] = fy2;
		vertices[idx + 7] = color;
		vertices[idx + 8] = u;
		vertices[idx + 9] = v2;

		vertices[idx + 10] = fx2;
		vertices[idx + 11] = fy2;
		vertices[idx + 12] = color;
		vertices[idx + 13] = u2;
		vertices[idx + 14] = v2;

		vertices[idx + 15] = fx2;
		vertices[idx + 16] = y;
		vertices[idx + 17] = color;
		vertices[idx + 18] = u2;
		vertices[idx + 19] = v;
		this.idx = idx + 20;
	}

	@Override
	public void draw (Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		float[] vertices = this.vertices;

		if (texture != lastTexture)
			switchTexture(texture);
		else if (idx == vertices.length) //
			flush();

		final float fx2 = x + width;
		final float fy2 = y + height;

		float color = this.colorPacked;
		int idx = this.idx;
		vertices[idx] = x;
		vertices[idx + 1] = y;
		vertices[idx + 2] = color;
		vertices[idx + 3] = u;
		vertices[idx + 4] = v;

		vertices[idx + 5] = x;
		vertices[idx + 6] = fy2;
		vertices[idx + 7] = color;
		vertices[idx + 8] = u;
		vertices[idx + 9] = v2;

		vertices[idx + 10] = fx2;
		vertices[idx + 11] = fy2;
		vertices[idx + 12] = color;
		vertices[idx + 13] = u2;
		vertices[idx + 14] = v2;

		vertices[idx + 15] = fx2;
		vertices[idx + 16] = y;
		vertices[idx + 17] = color;
		vertices[idx + 18] = u2;
		vertices[idx + 19] = v;
		this.idx = idx + 20;
	}

	@Override
	public void draw (Texture texture, float x, float y) {
		draw(texture, x, y, texture.getWidth(), texture.getHeight());
	}

	@Override
	public void draw (Texture texture, float x, float y, float width, float height) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		float[] vertices = this.vertices;

		if (texture != lastTexture)
			switchTexture(texture);
		else if (idx == vertices.length) //
			flush();

		final float fx2 = x + width;
		final float fy2 = y + height;
		final float u = 0;
		final float v = 1;
		final float u2 = 1;
		final float v2 = 0;

		float color = this.colorPacked;
		int idx = this.idx;
		vertices[idx] = x;
		vertices[idx + 1] = y;
		vertices[idx + 2] = color;
		vertices[idx + 3] = u;
		vertices[idx + 4] = v;

		vertices[idx + 5] = x;
		vertices[idx + 6] = fy2;
		vertices[idx + 7] = color;
		vertices[idx + 8] = u;
		vertices[idx + 9] = v2;

		vertices[idx + 10] = fx2;
		vertices[idx + 11] = fy2;
		vertices[idx + 12] = color;
		vertices[idx + 13] = u2;
		vertices[idx + 14] = v2;

		vertices[idx + 15] = fx2;
		vertices[idx + 16] = y;
		vertices[idx + 17] = color;
		vertices[idx + 18] = u2;
		vertices[idx + 19] = v;
		this.idx = idx + 20;
	}

	@Override
	public void draw (Texture texture, float[] spriteVertices, int offset, int count) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		int verticesLength = vertices.length;
		int remainingVertices = verticesLength;
		if (texture != lastTexture)
			switchTexture(texture);
		else {
			remainingVertices -= idx;
			if (remainingVertices == 0) {
				flush();
				remainingVertices = verticesLength;
			}
		}
		int copyCount = Math.min(remainingVertices, count);

		System.arraycopy(spriteVertices, offset, vertices, idx, copyCount);
		idx += copyCount;
		count -= copyCount;
		while (count > 0) {
			offset += copyCount;
			flush();
			copyCount = Math.min(verticesLength, count);
			System.arraycopy(spriteVertices, offset, vertices, 0, copyCount);
			idx += copyCount;
			count -= copyCount;
		}
	}

	@Override
	public void draw (TextureRegion region, float x, float y) {
		draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());
	}

	@Override
	public void draw (TextureRegion region, float x, float y, float width, float height) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		float[] vertices = this.vertices;

		Texture texture = region.texture;
		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) //
			flush();

		final float fx2 = x + width;
		final float fy2 = y + height;
		final float u = region.u;
		final float v = region.v2;
		final float u2 = region.u2;
		final float v2 = region.v;

		float color = this.colorPacked;
		int idx = this.idx;
		vertices[idx] = x;
		vertices[idx + 1] = y;
		vertices[idx + 2] = color;
		vertices[idx + 3] = u;
		vertices[idx + 4] = v;

		vertices[idx + 5] = x;
		vertices[idx + 6] = fy2;
		vertices[idx + 7] = color;
		vertices[idx + 8] = u;
		vertices[idx + 9] = v2;

		vertices[idx + 10] = fx2;
		vertices[idx + 11] = fy2;
		vertices[idx + 12] = color;
		vertices[idx + 13] = u2;
		vertices[idx + 14] = v2;

		vertices[idx + 15] = fx2;
		vertices[idx + 16] = y;
		vertices[idx + 17] = color;
		vertices[idx + 18] = u2;
		vertices[idx + 19] = v;
		this.idx = idx + 20;
	}

	@Override
	public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		float[] vertices = this.vertices;

		Texture texture = region.texture;
		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) //
			flush();

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

		final float u = region.u;
		final float v = region.v2;
		final float u2 = region.u2;
		final float v2 = region.v;

		float color = this.colorPacked;
		int idx = this.idx;
		vertices[idx] = x1;
		vertices[idx + 1] = y1;
		vertices[idx + 2] = color;
		vertices[idx + 3] = u;
		vertices[idx + 4] = v;

		vertices[idx + 5] = x2;
		vertices[idx + 6] = y2;
		vertices[idx + 7] = color;
		vertices[idx + 8] = u;
		vertices[idx + 9] = v2;

		vertices[idx + 10] = x3;
		vertices[idx + 11] = y3;
		vertices[idx + 12] = color;
		vertices[idx + 13] = u2;
		vertices[idx + 14] = v2;

		vertices[idx + 15] = x4;
		vertices[idx + 16] = y4;
		vertices[idx + 17] = color;
		vertices[idx + 18] = u2;
		vertices[idx + 19] = v;
		this.idx = idx + 20;
	}

	@Override
	public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation, boolean clockwise) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		float[] vertices = this.vertices;

		Texture texture = region.texture;
		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) //
			flush();

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

		float color = this.colorPacked;
		int idx = this.idx;
		vertices[idx] = x1;
		vertices[idx + 1] = y1;
		vertices[idx + 2] = color;
		vertices[idx + 3] = u1;
		vertices[idx + 4] = v1;

		vertices[idx + 5] = x2;
		vertices[idx + 6] = y2;
		vertices[idx + 7] = color;
		vertices[idx + 8] = u2;
		vertices[idx + 9] = v2;

		vertices[idx + 10] = x3;
		vertices[idx + 11] = y3;
		vertices[idx + 12] = color;
		vertices[idx + 13] = u3;
		vertices[idx + 14] = v3;

		vertices[idx + 15] = x4;
		vertices[idx + 16] = y4;
		vertices[idx + 17] = color;
		vertices[idx + 18] = u4;
		vertices[idx + 19] = v4;
		this.idx = idx + 20;
	}

	@Override
	public void draw (TextureRegion region, float width, float height, Affine2 transform) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		float[] vertices = this.vertices;

		Texture texture = region.texture;
		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) {
			flush();
		}

		// construct corner points
		float x1 = transform.m02;
		float y1 = transform.m12;
		float x2 = transform.m01 * height + transform.m02;
		float y2 = transform.m11 * height + transform.m12;
		float x3 = transform.m00 * width + transform.m01 * height + transform.m02;
		float y3 = transform.m10 * width + transform.m11 * height + transform.m12;
		float x4 = transform.m00 * width + transform.m02;
		float y4 = transform.m10 * width + transform.m12;

		float u = region.u;
		float v = region.v2;
		float u2 = region.u2;
		float v2 = region.v;

		float color = this.colorPacked;
		int idx = this.idx;
		vertices[idx] = x1;
		vertices[idx + 1] = y1;
		vertices[idx + 2] = color;
		vertices[idx + 3] = u;
		vertices[idx + 4] = v;

		vertices[idx + 5] = x2;
		vertices[idx + 6] = y2;
		vertices[idx + 7] = color;
		vertices[idx + 8] = u;
		vertices[idx + 9] = v2;

		vertices[idx + 10] = x3;
		vertices[idx + 11] = y3;
		vertices[idx + 12] = color;
		vertices[idx + 13] = u2;
		vertices[idx + 14] = v2;

		vertices[idx + 15] = x4;
		vertices[idx + 16] = y4;
		vertices[idx + 17] = color;
		vertices[idx + 18] = u2;
		vertices[idx + 19] = v;
		this.idx = idx + 20;
	}

	@Override
	public void flush () {
		if (idx == 0) return;

		renderCalls++;
		totalRenderCalls++;
		int spritesInBatch = idx / 20;
		if (spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch;
		int count = spritesInBatch * 6;

		lastTexture.bind();
		Mesh mesh = this.mesh;
		mesh.setVertices(vertices, 0, idx);

		// Only upload indices for the vertex array type
		if (currentDataType == VertexDataType.VertexArray) {
			Buffer indicesBuffer = (Buffer)mesh.getIndicesBuffer(true);
			indicesBuffer.position(0);
			indicesBuffer.limit(count);
		}

		if (blendingDisabled) {
			Gdx.gl.glDisable(GL20.GL_BLEND);
		} else {
			Gdx.gl.glEnable(GL20.GL_BLEND);
			if (blendSrcFunc != -1) Gdx.gl.glBlendFuncSeparate(blendSrcFunc, blendDstFunc, blendSrcFuncAlpha, blendDstFuncAlpha);
		}

		mesh.render(customShader != null ? customShader : shader, GL20.GL_TRIANGLES, 0, count);

		idx = 0;
	}

	@Override
	public void disableBlending () {
		if (blendingDisabled) return;
		flush();
		blendingDisabled = true;
	}

	@Override
	public void enableBlending () {
		if (!blendingDisabled) return;
		flush();
		blendingDisabled = false;
	}

	@Override
	public void setBlendFunction (int srcFunc, int dstFunc) {
		setBlendFunctionSeparate(srcFunc, dstFunc, srcFunc, dstFunc);
	}

	@Override
	public void setBlendFunctionSeparate (int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {
		if (blendSrcFunc == srcFuncColor && blendDstFunc == dstFuncColor && blendSrcFuncAlpha == srcFuncAlpha
			&& blendDstFuncAlpha == dstFuncAlpha) return;
		flush();
		blendSrcFunc = srcFuncColor;
		blendDstFunc = dstFuncColor;
		blendSrcFuncAlpha = srcFuncAlpha;
		blendDstFuncAlpha = dstFuncAlpha;
	}

	@Override
	public int getBlendSrcFunc () {
		return blendSrcFunc;
	}

	@Override
	public int getBlendDstFunc () {
		return blendDstFunc;
	}

	@Override
	public int getBlendSrcFuncAlpha () {
		return blendSrcFuncAlpha;
	}

	@Override
	public int getBlendDstFuncAlpha () {
		return blendDstFuncAlpha;
	}

	@Override
	public void dispose () {
		mesh.dispose();
		if (ownsShader && shader != null) shader.dispose();
	}

	@Override
	public Matrix4 getProjectionMatrix () {
		return projectionMatrix;
	}

	@Override
	public Matrix4 getTransformMatrix () {
		return transformMatrix;
	}

	@Override
	public void setProjectionMatrix (Matrix4 projection) {
		if (drawing) flush();
		projectionMatrix.set(projection);
		if (drawing) setupMatrices();
	}

	@Override
	public void setTransformMatrix (Matrix4 transform) {
		if (drawing) flush();
		transformMatrix.set(transform);
		if (drawing) setupMatrices();
	}

	protected void setupMatrices () {
		combinedMatrix.set(projectionMatrix).mul(transformMatrix);
		if (customShader != null) {
			customShader.setUniformMatrix("u_projTrans", combinedMatrix);
			customShader.setUniformi("u_texture", 0);
		} else {
			shader.setUniformMatrix("u_projTrans", combinedMatrix);
			shader.setUniformi("u_texture", 0);
		}
	}

	protected void switchTexture (Texture texture) {
		flush();
		lastTexture = texture;
		invTexWidth = 1.0f / texture.getWidth();
		invTexHeight = 1.0f / texture.getHeight();
	}

	@Override
	public void setShader (ShaderProgram shader) {
		if (shader == customShader) // avoid unnecessary flushing in case we are drawing
			return;
		if (drawing) {
			flush();
		}
		customShader = shader;
		if (drawing) {
			if (customShader != null)
				customShader.bind();
			else
				this.shader.bind();
			setupMatrices();
		}
	}

	@Override
	public ShaderProgram getShader () {
		if (customShader == null) {
			return shader;
		}
		return customShader;
	}

	@Override
	public boolean isBlendingEnabled () {
		return !blendingDisabled;
	}

	public boolean isDrawing () {
		return drawing;
	}
}
