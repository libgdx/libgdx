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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;

/** Draws batched quads using indices.
 * <p>
 * <b>Experimental: This Batch requires GLES3.0! Enable this by setting useGL30=true in your application configuration(s). GL30 is
 * supported by most Android devices (starting with Android 4.3 Jelly Bean, 2012) and PCs with appropriate OpenGL support.</b>
 * <p>
 * This is an optimized version of the {@link SpriteBatch} that maintains an texture-cache inside a GL_TEXTURE_2D_ARRAY to combine
 * draw calls with different textures effectively. This will avoid costly (especially on mobile) batch flushes that would usually
 * occur when your render with more then one texture.
 * <p>
 * Use this Batch if you frequently utilize more than a single texture between calling {@link#begin()} and {@link#end()}. An
 * example would be if your Atlas is spread over multiple Textures or if you draw with individual Textures.
 * <p>
 * Using this Batch to render to a Frame Buffer Object (FBO) is not allowed on WebGL because of current WebGL and LibGDX API
 * limitations. Other platforms may use this Batch to render to a FBO as the state is saved and restored recursively.
 * 
 * @see Batch
 * @see SpriteBatch
 * 
 * @author mzechner (Original SpriteBatch)
 * @author Nathan Sweet (Original SpriteBatch)
 * @author VaTTeRGeR (ArrayTextureSpriteBatch) */

public class ArrayTextureSpriteBatch implements Batch {

	private int idx = 0;

	private final Mesh mesh;

	private final float[] vertices;

	private final int spriteVertexSize = Sprite.VERTEX_SIZE;
	private final int spriteFloatSize = Sprite.SPRITE_SIZE;

	/** The maximum number of available texture slots for the fragment shader */
	private final int maxTextureSlots;

	/** WebGL requires special handling for FBOs */
	private static final boolean isWebGL = Gdx.app.getType().equals(ApplicationType.WebGL);

	/** Textures in use (index: Texture Slot, value: Texture) */
	private final Texture[] usedTextures;

	/** LFU Array (index: Texture Slot - value: Access frequency) */
	private final int[] usedTexturesLFU;

	/** LFU Array of the previous begin-draw-end cycle (index: Texture Slot - value: Access frequency) */
	private final int[] usedTexturesLFUPrevious;

	private final IntBuffer FBO_READ_INTBUFF;

	private final FrameBuffer copyFramebuffer;

	private int arrayTextureHandle;
	private int arrayTextureMagFilter;
	private int arrayTextureMinFilter;

	private int maxTextureWidth, maxTextureHeight;

	private float invTexWidth, invTexHeight;
	private float invMaxTextureWidth, invMaxTextureHeight;
	private float subImageScaleWidth, subImageScaleHeight;

	private boolean drawing = false;
	private boolean useMipMaps = true;
	private boolean mipMapsDirty = true;

	private final Matrix4 transformMatrix = new Matrix4();
	private final Matrix4 projectionMatrix = new Matrix4();
	private final Matrix4 combinedMatrix = new Matrix4();

	private boolean blendingDisabled = false;
	private int blendSrcFunc = GL30.GL_SRC_ALPHA;
	private int blendDstFunc = GL30.GL_ONE_MINUS_SRC_ALPHA;
	private int blendSrcFuncAlpha = GL30.GL_SRC_ALPHA;
	private int blendDstFuncAlpha = GL30.GL_ONE_MINUS_SRC_ALPHA;

	private ShaderProgram shader = null;
	private ShaderProgram customShader = null;

	private boolean ownsShader;

	private final Color color = new Color(1, 1, 1, 1);
	private float colorPacked = Color.WHITE_FLOAT_BITS;

	/** Number of render calls since the last {@link #begin()}. **/
	public int renderCalls = 0;

	/** Number of rendering calls, ever. Will not be reset unless set manually. **/
	public int totalRenderCalls = 0;

	/** The maximum number of sprites rendered in one batch so far. **/
	public int maxSpritesInBatch = 0;

	/** The current number of textures in the LFU cache. **/
	private int currentTextureLFUSize = 0;

	/** The current number of texture swaps in the LFU cache. Gets reset when calling {@link#begin()} **/
	private int currentTextureLFUSwaps = 0;

	private final LifecycleListener contextRestoreListener;

	/** Constructs a new ArrayTextureSpriteBatch with the default shader, texture cache size and texture filters.
	 * @see ArrayTextureSpriteBatch#ArrayTextureSpriteBatch(int, int, int, int, int, int, ShaderProgram) */
	public ArrayTextureSpriteBatch (int maxSprites, int maxTextureWidth, int maxTextureHeight, int maxConcurrentTextures,
		int texFilterMag, int texFilterMin) throws IllegalStateException {
		this(maxSprites, maxTextureWidth, maxTextureHeight, maxConcurrentTextures, texFilterMag, texFilterMin, null);
	}

	/** Constructs a new ArrayTextureSpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point
	 * upwards, x-axis point to the right and the origin being in the bottom left corner of the screen. The projection will be
	 * pixel perfect with respect to the current screen resolution.
	 * <p>
	 * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
	 * the ones expected for shaders set with {@link #setShader(ShaderProgram)}.
	 * <p>
	 * <b>Remember: VRAM usage will be roughly maxTextureWidth * maxTextureHeight * maxConcurrentTextures * 4 byte plus some
	 * overhead!</b>
	 * 
	 * @param maxSprites The maximum number of sprites in a single batched draw call. Upper limit of 8191.
	 * @param maxTextureWidth Set as wide as your widest texture.
	 * @param maxTextureHeight Set as tall as your tallest texture.
	 * @param maxConcurrentTextures Set to the maximum number of textures you want to use ideally, grossly oversized values waste
	 *           VRAM.
	 * @param texFilterMag The OpenGL texture magnification filter. See {@link #setArrayTextureFilter(int, int)}.
	 * @param texFilterMin The OpenGL texture minification filter. See {@link #setArrayTextureFilter(int, int)}.
	 * @param defaultShader The default shader to use. This is not owned by the ArrayTextureSpriteBatch and must be disposed
	 *           separately. Remember to incorporate the fragment-/vertex-shader changes required for the use of an array texture
	 *           as demonstrated by the default shader.
	 * @throws IllegalStateException Thrown if the device does not support GLES 3.0 and by extension: GL_TEXTURE_2D_ARRAY and
	 *            Framebuffer Objects. Make sure to implement a Fallback to {@link SpriteBatch} in case Texture Arrays are not
	 *            supported on a device. */
	public ArrayTextureSpriteBatch (int maxSprites, int maxTextureWidth, int maxTextureHeight, int maxConcurrentTextures,
		int texFilterMag, int texFilterMin, ShaderProgram defaultShader) throws IllegalStateException {

		if (Gdx.gl30 == null) {
			throw new IllegalStateException("GL30 is not available. Remember to set \"useGL30 = true\" in your application config.");
		}

		// 32767 is max vertex index, so 32767 / 4 vertices per sprite = 8191 sprites max.
		if (maxSprites > 8191) {
			throw new IllegalArgumentException("Can't have more than 8191 sprites per batch: " + maxSprites);
		}

		if (maxConcurrentTextures < 1 || maxConcurrentTextures > 256) {
			throw new IllegalArgumentException("maxConcurrentTextures out of range [1,256]: " + maxConcurrentTextures);
		}

		if (maxTextureWidth < 1 || maxTextureHeight < 1) {
			throw new IllegalArgumentException(
				"Maximum Texture width / height must both be greater than zero: " + maxTextureWidth + " / " + maxTextureHeight);
		}

		maxTextureSlots = maxConcurrentTextures;

		this.maxTextureWidth = maxTextureWidth;
		this.maxTextureHeight = maxTextureHeight;

		invMaxTextureWidth = 1f / maxTextureWidth;
		invMaxTextureHeight = 1f / maxTextureHeight;

		if (defaultShader == null) {
			shader = createDefaultShader();
			ownsShader = true;

		} else {
			shader = defaultShader;
			ownsShader = false;
		}

		FBO_READ_INTBUFF = ByteBuffer.allocateDirect(16 * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();

		usedTextures = new Texture[maxTextureSlots];
		usedTexturesLFU = new int[maxTextureSlots];
		usedTexturesLFUPrevious = new int[maxTextureSlots];

		arrayTextureMagFilter = texFilterMag;
		arrayTextureMinFilter = texFilterMin;

		initializeArrayTexture();

		copyFramebuffer = new FrameBuffer(Format.RGBA8888, maxTextureWidth, maxTextureHeight, false, false);

		// The vertex data is extended with one float for the texture index.
		mesh = new Mesh(VertexDataType.VertexBufferObjectWithVAO, false, maxSprites * 4, maxSprites * 6,
			new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
			new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
			new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"),
			new VertexAttribute(Usage.Generic, 1, "texture_index"));

		projectionMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		vertices = new float[maxSprites * (spriteFloatSize + 4)];

		int len = maxSprites * 6;
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

		contextRestoreListener = new LifecycleListener() {

			final ApplicationType appType = Gdx.app.getType();

			@Override
			public void resume () {
				if (appType == ApplicationType.Android) {
					initializeArrayTexture();
				}
			}

			@Override
			public void pause () {
				if (appType == ApplicationType.Android) {
					disposeArrayTexture();
				}
			}

			@Override
			public void dispose () {
			}
		};

		Gdx.app.addLifecycleListener(contextRestoreListener);
	}

	private void initializeArrayTexture () {

		// This forces a re-population of the Array Texture
		currentTextureLFUSize = 0;

		arrayTextureHandle = Gdx.gl30.glGenTexture();

		Gdx.gl30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, arrayTextureHandle);

		Gdx.gl30.glTexImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, GL30.GL_RGBA, maxTextureWidth, maxTextureHeight, maxTextureSlots, 0,
			GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, null);

		setArrayTextureFilter(arrayTextureMagFilter, arrayTextureMinFilter);

		Gdx.gl30.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL30.GL_TEXTURE_WRAP_S, GL30.GL_REPEAT);
		Gdx.gl30.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL30.GL_TEXTURE_WRAP_T, GL30.GL_REPEAT);

		Gdx.gl30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, GL30.GL_NONE);
	}

	private void disposeArrayTexture () {
		Gdx.gl30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, GL30.GL_NONE);
		Gdx.gl30.glDeleteTexture(arrayTextureHandle);
	}

	@Override
	public void dispose () {

		Gdx.app.removeLifecycleListener(contextRestoreListener);

		disposeArrayTexture();

		copyFramebuffer.dispose();

		mesh.dispose();

		if (ownsShader && shader != null) {
			shader.dispose();
		}
	}

	/** Sets the OpenGL texture filtering modes. MipMaps will be generated on the GPU, this takes additional time when first
	 * loading or swapping textures. Dimension the {@link ArrayTextureSpriteBatch} accordingly to avoid stuttering.
	 * <p>
	 * <b>Default magnification: GL30.GL_NEAREST -> Pixel perfect when going to close<br>
	 * Default minification: GL30.GL_LINEAR_MIPMAP_LINEAR -> Smooth when zooming out.</b>
	 * @param glTextureMagFilter The filtering mode used when zooming into the texture.
	 * @param glTextureMinFilter The filtering mode used when zooming away from the texture.
	 * @see <a href="https://www.khronos.org/opengl/wiki/Sampler_Object#Filtering">OpenGL Wiki: Sampler Object - Filtering</a> */
	public void setArrayTextureFilter (int glTextureMagFilter, int glTextureMinFilter) {

		arrayTextureMagFilter = glTextureMagFilter;
		arrayTextureMinFilter = glTextureMinFilter;

		Gdx.gl30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, arrayTextureHandle);

		Gdx.gl30.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL30.GL_TEXTURE_MAG_FILTER, glTextureMagFilter);
		Gdx.gl30.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL30.GL_TEXTURE_MIN_FILTER, glTextureMinFilter);

		if (glTextureMagFilter >= GL30.GL_NEAREST_MIPMAP_NEAREST && glTextureMagFilter <= GL30.GL_LINEAR_MIPMAP_LINEAR) {
			useMipMaps = true;
		} else if (glTextureMinFilter >= GL30.GL_NEAREST_MIPMAP_NEAREST && glTextureMinFilter <= GL30.GL_LINEAR_MIPMAP_LINEAR) {
			useMipMaps = true;
		} else {
			useMipMaps = false;
		}

		mipMapsDirty = useMipMaps;
	}

	/** Returns a new instance of the default shader used by ArrayTextureSpriteBatch when no shader is specified. */
	public static ShaderProgram createDefaultShader () {

		// The texture index is just passed to the fragment shader, maybe there's an more elegant way.
		String vertexShader = "in vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "in vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "in vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "in float texture_index;\n" //
			+ "uniform mat4 u_projTrans;\n" //
			+ "out vec4 v_color;\n" //
			+ "out vec2 v_texCoords;\n" //
			+ "out float v_texture_index;\n" //
			+ "\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "   v_color.a = v_color.a * (255.0/254.0);\n" //
			+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "   v_texture_index = texture_index;\n" //
			+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "}\n";

		// The texture is simply selected from an array of textures
		String fragmentShader = "#ifdef GL_ES\n" //
			+ "#define LOWP lowp\n" //
			+ "precision mediump float;\n" //
			+ "#else\n" //
			+ "#define LOWP\n" //
			+ "#endif\n" //
			+ "in LOWP vec4 v_color;\n" //
			+ "in vec2 v_texCoords;\n" //
			+ "in float v_texture_index;\n" //
			+ "uniform sampler2DArray u_texturearray;\n" //
			+ "out vec4 diffuseColor;\n" + "void main()\n"//
			+ "{\n" //
			+ "  diffuseColor = v_color * texture(u_texturearray, vec3(v_texCoords, v_texture_index));\n" //
			+ "}";

		final ApplicationType appType = Gdx.app.getType();

		if (appType == ApplicationType.Android || appType == ApplicationType.iOS || appType == ApplicationType.WebGL) {
			vertexShader = "#version 300 es\n" + vertexShader;
			fragmentShader = "#version 300 es\n" + fragmentShader;
		} else {
			vertexShader = "#version 150\n" + vertexShader;
			fragmentShader = "#version 150\n" + fragmentShader;
		}

		ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);

		if (!shader.isCompiled()) {
			throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
		}

		return shader;
	}

	@Override
	public void begin () {

		if (drawing) throw new IllegalStateException("ArrayTextureSpriteBatch.end must be called before begin.");

		renderCalls = 0;

		currentTextureLFUSwaps = 0;

		// We use this data to decide which texture to swap out if space is needed
		System.arraycopy(usedTexturesLFU, 0, usedTexturesLFUPrevious, 0, maxTextureSlots);
		Arrays.fill(usedTexturesLFU, 0);

		Gdx.gl30.glDepthMask(false);

		Gdx.gl30.glActiveTexture(GL30.GL_TEXTURE0);

		Gdx.gl30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, arrayTextureHandle);

		if (customShader != null) {
			customShader.begin();
		} else {
			shader.begin();
		}

		setupMatrices();

		drawing = true;
	}

	@Override
	public void end () {

		if (!drawing) throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before end.");

		if (idx > 0) flush();

		drawing = false;

		Gdx.gl30.glDepthMask(true);

		if (isBlendingEnabled()) {
			Gdx.gl30.glDisable(GL30.GL_BLEND);
		}

		if (customShader != null) {
			customShader.end();
		} else {
			shader.end();
		}
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
		if (!drawing) throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");

		float[] vertices = this.vertices;

		flushIfFull();

		final float ti = activateTexture(texture);

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

		final float color = this.colorPacked;

		vertices[idx++] = x1;
		vertices[idx++] = y1;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;
		vertices[idx++] = ti;

		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;
		vertices[idx++] = ti;

		vertices[idx++] = x3;
		vertices[idx++] = y3;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;
		vertices[idx++] = ti;

		vertices[idx++] = x4;
		vertices[idx++] = y4;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
		vertices[idx++] = ti;
	}

	@Override
	public void draw (Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
		int srcHeight, boolean flipX, boolean flipY) {

		if (!drawing) throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");

		float[] vertices = this.vertices;

		flushIfFull();

		final float ti = activateTexture(texture);

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

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;
		vertices[idx++] = ti;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;
		vertices[idx++] = ti;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;
		vertices[idx++] = ti;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
		vertices[idx++] = ti;
	}

	@Override
	public void draw (Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
		if (!drawing) throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");

		float[] vertices = this.vertices;

		flushIfFull();

		final float ti = activateTexture(texture);

		final float u = srcX * invTexWidth;
		final float v = (srcY + srcHeight) * invTexHeight;
		final float u2 = (srcX + srcWidth) * invTexWidth;
		final float v2 = srcY * invTexHeight;
		final float fx2 = x + srcWidth;
		final float fy2 = y + srcHeight;

		float color = this.colorPacked;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;
		vertices[idx++] = ti;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;
		vertices[idx++] = ti;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;
		vertices[idx++] = ti;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
		vertices[idx++] = ti;
	}

	@Override
	public void draw (Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {

		if (!drawing) throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");

		float[] vertices = this.vertices;

		flushIfFull();

		final float ti = activateTexture(texture);

		final float fx2 = x + width;
		final float fy2 = y + height;

		float color = this.colorPacked;

		u *= subImageScaleWidth;
		v *= subImageScaleHeight;
		u2 *= subImageScaleWidth;
		v2 *= subImageScaleHeight;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;
		vertices[idx++] = ti;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;
		vertices[idx++] = ti;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;
		vertices[idx++] = ti;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
		vertices[idx++] = ti;
	}

	@Override
	public void draw (Texture texture, float x, float y) {
		draw(texture, x, y, texture.getWidth(), texture.getHeight());
	}

	@Override
	public void draw (Texture texture, float x, float y, float width, float height) {

		if (!drawing) throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");

		float[] vertices = this.vertices;

		flushIfFull();

		final float ti = activateTexture(texture);

		final float fx2 = x + width;
		final float fy2 = y + height;
		final float u = 0;
		final float v = subImageScaleWidth;
		final float u2 = subImageScaleHeight;
		final float v2 = 0;

		float color = this.colorPacked;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;
		vertices[idx++] = ti;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;
		vertices[idx++] = ti;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;
		vertices[idx++] = ti;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
		vertices[idx++] = ti;
	}

	@Override
	public void draw (Texture texture, float[] spriteVertices, int offset, int count) {

		if (!drawing) {
			throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");
		}

		flushIfFull();

		// Assigns a texture unit to this texture, flushing if none is available
		final float ti = (float)activateTexture(texture);

		// spriteVertexSize is the number of floats an unmodified input vertex consists of,
		// therefore this loop iterates over the vertices stored in parameter spriteVertices.
		for (int srcPos = 0; srcPos < count; srcPos += spriteVertexSize) {

			// Copy the vertices
			System.arraycopy(spriteVertices, srcPos, vertices, idx, spriteVertexSize);

			// Advance idx by vertex float count
			idx += spriteVertexSize - 2;

			// Scale UV coordinates to fit array texture
			vertices[idx++] *= subImageScaleWidth;
			vertices[idx++] *= subImageScaleHeight;

			// Inject texture unit index and advance idx
			vertices[idx++] = ti;
		}
	}

	@Override
	public void draw (TextureRegion region, float x, float y) {
		draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());
	}

	@Override
	public void draw (TextureRegion region, float x, float y, float width, float height) {

		if (!drawing) throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");

		float[] vertices = this.vertices;

		flushIfFull();

		final float ti = activateTexture(region.texture);

		final float fx2 = x + width;
		final float fy2 = y + height;
		final float u = region.u * subImageScaleWidth;
		final float v = region.v2 * subImageScaleHeight;
		final float u2 = region.u2 * subImageScaleWidth;
		final float v2 = region.v * subImageScaleHeight;

		float color = this.colorPacked;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;
		vertices[idx++] = ti;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;
		vertices[idx++] = ti;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;
		vertices[idx++] = ti;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
		vertices[idx++] = ti;
	}

	@Override
	public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation) {

		if (!drawing) throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");

		float[] vertices = this.vertices;

		flushIfFull();

		final float ti = activateTexture(region.texture);

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

		final float u = region.u * subImageScaleWidth;
		final float v = region.v2 * subImageScaleHeight;
		final float u2 = region.u2 * subImageScaleWidth;
		final float v2 = region.v * subImageScaleHeight;

		float color = this.colorPacked;

		vertices[idx++] = x1;
		vertices[idx++] = y1;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;
		vertices[idx++] = ti;

		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;
		vertices[idx++] = ti;

		vertices[idx++] = x3;
		vertices[idx++] = y3;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;
		vertices[idx++] = ti;

		vertices[idx++] = x4;
		vertices[idx++] = y4;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
		vertices[idx++] = ti;
	}

	@Override
	public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation, boolean clockwise) {

		if (!drawing) throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");

		float[] vertices = this.vertices;

		flushIfFull();

		final float ti = activateTexture(region.texture);

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

		u1 *= subImageScaleWidth;
		u2 *= subImageScaleWidth;
		u3 *= subImageScaleWidth;
		u4 *= subImageScaleWidth;

		v1 *= subImageScaleHeight;
		v2 *= subImageScaleHeight;
		v3 *= subImageScaleHeight;
		v4 *= subImageScaleHeight;

		float color = this.colorPacked;

		vertices[idx++] = x1;
		vertices[idx++] = y1;
		vertices[idx++] = color;
		vertices[idx++] = u1;
		vertices[idx++] = v1;
		vertices[idx++] = ti;

		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;
		vertices[idx++] = ti;

		vertices[idx++] = x3;
		vertices[idx++] = y3;
		vertices[idx++] = color;
		vertices[idx++] = u3;
		vertices[idx++] = v3;
		vertices[idx++] = ti;

		vertices[idx++] = x4;
		vertices[idx++] = y4;
		vertices[idx++] = color;
		vertices[idx++] = u4;
		vertices[idx++] = v4;
		vertices[idx++] = ti;
	}

	@Override
	public void draw (TextureRegion region, float width, float height, Affine2 transform) {
		if (!drawing) throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");

		float[] vertices = this.vertices;

		flushIfFull();

		final float ti = activateTexture(region.texture);

		// construct corner points
		float x1 = transform.m02;
		float y1 = transform.m12;
		float x2 = transform.m01 * height + transform.m02;
		float y2 = transform.m11 * height + transform.m12;
		float x3 = transform.m00 * width + transform.m01 * height + transform.m02;
		float y3 = transform.m10 * width + transform.m11 * height + transform.m12;
		float x4 = transform.m00 * width + transform.m02;
		float y4 = transform.m10 * width + transform.m12;

		float u = region.u * subImageScaleWidth;
		float v = region.v2 * subImageScaleHeight;
		float u2 = region.u2 * subImageScaleWidth;
		float v2 = region.v * subImageScaleHeight;

		float color = this.colorPacked;

		vertices[idx++] = x1;
		vertices[idx++] = y1;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;
		vertices[idx++] = ti;

		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;
		vertices[idx++] = ti;

		vertices[idx++] = x3;
		vertices[idx++] = y3;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;
		vertices[idx++] = ti;

		vertices[idx++] = x4;
		vertices[idx++] = y4;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
		vertices[idx++] = ti;
	}

	/** Convenience method to flush if the Batches vertex-array cannot hold an additional sprite ((spriteVertexSize + 1) * 4
	 * vertices) anymore. */
	private void flushIfFull () {
		// original Sprite attribute size plus one extra float per sprite vertex
		if (vertices.length - idx < spriteFloatSize + spriteFloatSize / spriteVertexSize) {
			flush();
		}
	}

	@Override
	public void flush () {

		if (idx == 0) return;

		renderCalls++;
		totalRenderCalls++;

		int spritesInBatch = idx / (spriteFloatSize + 4);
		if (spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch;
		int count = spritesInBatch * 6;

		if (useMipMaps && mipMapsDirty) {
			Gdx.gl30.glGenerateMipmap(GL30.GL_TEXTURE_2D_ARRAY);
			mipMapsDirty = false;
		}

		Mesh mesh = this.mesh;

		mesh.setVertices(vertices, 0, idx);

		mesh.getIndicesBuffer().position(0);
		mesh.getIndicesBuffer().limit(count);

		if (blendingDisabled) {
			Gdx.gl.glDisable(GL30.GL_BLEND);
		} else {
			Gdx.gl.glEnable(GL30.GL_BLEND);
			if (blendSrcFunc != -1) Gdx.gl.glBlendFuncSeparate(blendSrcFunc, blendDstFunc, blendSrcFuncAlpha, blendDstFuncAlpha);
		}

		if (customShader != null) {
			mesh.render(customShader, GL30.GL_TRIANGLES, 0, count);
		} else {
			mesh.render(shader, GL30.GL_TRIANGLES, 0, count);
		}

		idx = 0;
	}

	/** Assigns space on the Array Texture, sets up Texture scaling and manages the LFU cache.
	 * @param texture The texture that shall be loaded into the cache, if it is not already loaded.
	 * @return The texture slot that has been allocated to the selected texture */
	private int activateTexture (Texture texture) {

		subImageScaleWidth = texture.getWidth() * invMaxTextureWidth;
		subImageScaleHeight = texture.getHeight() * invMaxTextureHeight;

		if (subImageScaleWidth > 1f || subImageScaleHeight > 1f) {
			throw new IllegalStateException("Texture " + texture.getTextureObjectHandle() + " is larger than the Array Texture: ["
				+ texture.getWidth() + "," + texture.getHeight() + "] > [" + maxTextureWidth + "," + maxTextureHeight + "]");
		}

		invTexWidth = subImageScaleWidth / texture.getWidth();
		invTexHeight = subImageScaleHeight / texture.getHeight();

		// This is our identifier for the textures
		final int textureHandle = texture.getTextureObjectHandle();

		// First try to see if the texture is already cached
		for (int i = 0; i < currentTextureLFUSize; i++) {

			// getTextureObjectHandle() just returns an int,
			// it's fine to call this method instead of caching the value.
			if (textureHandle == usedTextures[i].getTextureObjectHandle()) {

				// Increase the access counter.
				usedTexturesLFU[i]++;

				return i;
			}
		}

		// If a free texture unit is available we just use it
		// If not we have to flush and then throw out the least accessed one.
		if (currentTextureLFUSize < maxTextureSlots) {

			// Put the texture into the next free slot
			usedTextures[currentTextureLFUSize] = texture;

			// Increase the access counter.
			usedTexturesLFU[currentTextureLFUSize]++;

			copyTextureIntoArrayTexture(texture, currentTextureLFUSize);

			currentTextureLFUSwaps++;

			return currentTextureLFUSize++;

		} else {

			// We try to find an unused (since calling begin()) or otherwise the least-used slot when swapping.
			int slot = 0;
			int slotValPrev = usedTexturesLFUPrevious[slot];

			// We search for the best candidate for a swap (least accessed) and collect some data
			for (int i = 1; i < maxTextureSlots; i++) {

				final int val = usedTexturesLFUPrevious[i];

				if (val <= slotValPrev) {
					slot = i;
					slotValPrev = val;
				}

				// Early exit when a texture was found that hasn't yet been used in this or the previous rendering cycle
				if (slotValPrev == 0 && usedTexturesLFU[slot] == 0) {
					break;
				}
			}

			// We have to flush if there is something in the pipeline using this texture already,
			// otherwise the texture index of previously rendered sprites gets invalidated
			if (idx > 0 && usedTexturesLFU[slot] > 0) {
				flush();
			}

			// This texture was used once now.
			usedTexturesLFU[slot] = 1;

			usedTextures[slot] = texture;

			copyTextureIntoArrayTexture(texture, slot);

			// For statistics
			currentTextureLFUSwaps++;

			return slot;
		}
	}

	/** Copies a Texture to the internally managed Array Texture.
	 * @param texture The Texture to copy onto the Array Texture.
	 * @param slot The slice of the Array Texture to copy the texture onto. */
	private void copyTextureIntoArrayTexture (Texture texture, int slot) {

		int previousFrameBufferHandle = 0;

		if (!isWebGL) {
			// Query current Framebuffer configuration
			Gdx.gl30.glGetIntegerv(GL30.GL_FRAMEBUFFER_BINDING, FBO_READ_INTBUFF);

			previousFrameBufferHandle = FBO_READ_INTBUFF.get(0);
		}

		// Bind CopyFrameBuffer
		copyFramebuffer.bind();

		Gdx.gl30.glFramebufferTexture2D(GL30.GL_READ_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D,
			texture.getTextureObjectHandle(), 0);

		Gdx.gl30.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);

		Gdx.gl30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, arrayTextureHandle);

		Gdx.gl30.glCopyTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, slot, 0, 0, copyFramebuffer.getWidth(),
			copyFramebuffer.getHeight());

		if (!isWebGL) {
			// Restore previous FrameBuffer configuration
			Gdx.gl30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFrameBufferHandle);
		}

		if (useMipMaps) {
			mipMapsDirty = true;
		}
	}

	/** @return The number of texture swaps the texture cache performed since calling {@link #begin()}. <b>Texture swaps are
	 *         extremely expensive operations. Always try to make the texture cache large enough (Parameter called
	 *         maxConcurrentTextures in the constructor) otherwise you'll loose all performance gains!</b> */
	public int getTextureLFUSwaps () {
		return currentTextureLFUSwaps;
	}

	/** @return The current number of textures residing in the texture cache. */
	public int getTextureLFUSize () {
		return currentTextureLFUSize;
	}

	/** @return The maximum number of textures that the texture cache can hold. */
	public int getTextureLFUCapacity () {
		return maxTextureSlots;
	}

	@Override
	public void disableBlending () {

		if (blendingDisabled) return;

		flush();

		blendingDisabled = true;
	}

	@Override
	public void enableBlending () {

		if (!blendingDisabled) {
			return;
		}

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
			&& blendDstFuncAlpha == dstFuncAlpha) {
			return;
		}

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
	public boolean isBlendingEnabled () {
		return !blendingDisabled;
	}

	@Override
	public boolean isDrawing () {
		return drawing;
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

		if (drawing) {
			flush();
		}

		projectionMatrix.set(projection);

		if (drawing) {
			setupMatrices();
		}
	}

	@Override
	public void setTransformMatrix (Matrix4 transform) {

		if (drawing) {
			flush();
		}

		transformMatrix.set(transform);

		if (drawing) {
			setupMatrices();
		}
	}

	private void setupMatrices () {

		combinedMatrix.set(projectionMatrix).mul(transformMatrix);

		if (customShader != null) {
			customShader.setUniformMatrix("u_projTrans", combinedMatrix);

		} else {
			shader.setUniformMatrix("u_projTrans", combinedMatrix);
		}
	}

	/** Sets the shader to be used in a GLES 3.0 environment. Vertex position attribute is called "a_position", the texture
	 * coordinates attribute is called "a_texCoord0", the color attribute is called "a_color", texture index is called
	 * "texture_index". See {@link ShaderProgram#POSITION_ATTRIBUTE}, {@link ShaderProgram#COLOR_ATTRIBUTE} and
	 * {@link ShaderProgram#TEXCOORD_ATTRIBUTE} which gets "0" appended to indicate the use of the first texture unit. The combined
	 * transform and projection matrix is uploaded via a mat4 uniform called "u_projTrans". See
	 * {@link ArrayTextureSpriteBatch#createDefaultShader(int)} for reference.
	 * <p>
	 * Call this method with a null argument to use the default shader.
	 * <p>
	 * This method will flush the batch before setting the new shader, you can call it in between {@link #begin()} and
	 * {@link #end()}.
	 * @param shader the {@link ShaderProgram} or null to use the default shader.
	 * @See {@link#createDefaultShader()} */
	@Override
	public void setShader (ShaderProgram shader) {

		if (drawing) {

			flush();

			if (customShader != null) {
				customShader.end();
			} else {
				this.shader.end();
			}
		}

		customShader = shader;

		if (drawing) {

			if (customShader != null) {
				customShader.begin();
			} else {
				this.shader.begin();
			}

			setupMatrices();
		}
	}

	@Override
	public ShaderProgram getShader () {

		if (customShader != null) {
			return customShader;
		} else {
			return shader;
		}
	}
}
