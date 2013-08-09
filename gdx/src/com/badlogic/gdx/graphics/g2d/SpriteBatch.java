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
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.NumberUtils;

/** A SpriteBatch is used to draw 2D rectangles that reference a texture (region). The class will batch the drawing commands and
 * optimize them for processing by the GPU.
 * <p>
 * To draw something with a SpriteBatch one has to first call the {@link SpriteBatch#begin()} method which will setup appropriate
 * render states. When you are done with drawing you have to call {@link SpriteBatch#end()} which will actually draw the things
 * you specified.
 * <p>
 * All drawing commands of the SpriteBatch operate in screen coordinates. The screen coordinate system has an x-axis pointing to
 * the right, an y-axis pointing upwards and the origin is in the lower left corner of the screen. You can also provide your own
 * transformation and projection matrices if you so wish.
 * <p>
 * A SpriteBatch is managed. In case the OpenGL context is lost all OpenGL resources a SpriteBatch uses internally get
 * invalidated. A context is lost when a user switches to another application or receives an incoming call on Android. A
 * SpriteBatch will be automatically reloaded after the OpenGL context is restored.
 * <p>
 * A SpriteBatch is a pretty heavy object so you should only ever have one in your program.
 * <p>
 * A SpriteBatch works with OpenGL ES 1.x and 2.0. In the case of a 2.0 context it will use its own custom shader to draw all
 * provided sprites. You can set your own custom shader via {@link #setShader(ShaderProgram)}.
 * <p>
 * A SpriteBatch has to be disposed if it is no longer used.
 * @author mzechner */
public class SpriteBatch implements Disposable {
	private Mesh mesh;
	private Mesh[] buffers;

	private Texture lastTexture = null;
	private float invTexWidth = 0;
	private float invTexHeight = 0;

	private int idx = 0;
	private int currBufferIdx = 0;
	private final float[] vertices;

	private final Matrix4 transformMatrix = new Matrix4();
	private final Matrix4 projectionMatrix = new Matrix4();
	private final Matrix4 combinedMatrix = new Matrix4();

	private boolean drawing = false;

	private boolean blendingDisabled = false;
	private int blendSrcFunc = GL11.GL_SRC_ALPHA;
	private int blendDstFunc = GL11.GL_ONE_MINUS_SRC_ALPHA;

	private final ShaderProgram shader;
	private boolean ownsShader;

	float color = Color.WHITE.toFloatBits();
	private Color tempColor = new Color(1, 1, 1, 1);

	/** number of render calls since last {@link #begin()} **/
	public int renderCalls = 0;

	/** number of rendering calls ever, will not be reset, unless it's done manually **/
	public int totalRenderCalls = 0;

	/** the maximum number of sprites rendered in one batch so far **/
	public int maxSpritesInBatch = 0;
	private ShaderProgram customShader = null;

	/** Constructs a new SpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards, x-axis
	 * point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect with
	 * respect to the screen resolution. */
	public SpriteBatch () {
		this(1000);
	}

	/** Constructs a SpriteBatch with the specified size and (if GL2) the default shader. See
	 * {@link #SpriteBatch(int, ShaderProgram)}. */
	public SpriteBatch (int size) {
		this(size, null);
	}

	/** Constructs a new SpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards, x-axis
	 * point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect with
	 * respect to the screen resolution.
	 * <p>
	 * The size parameter specifies the maximum size of a single batch in number of sprites
	 * <p>
	 * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
	 * the ones expect for shaders set with {@link #setShader(ShaderProgram)}. See the {@link #createDefaultShader()} method.
	 * @param size the batch size in number of sprites
	 * @param defaultShader the default shader to use. This is not owned by the SpriteBatch and must be disposed separately. */
	public SpriteBatch (int size, ShaderProgram defaultShader) {
		this(size, 1, defaultShader);
	}

	/** Constructs a SpriteBatch with the specified size and number of buffers and (if GL2) the default shader. See
	 * {@link #SpriteBatch(int, int, ShaderProgram)}. */
	public SpriteBatch (int size, int buffers) {
		this(size, buffers, null);
	}

	/** Constructs a new SpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards, x-axis
	 * point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect with
	 * respect to the screen resolution.
	 * <p>
	 * The size parameter specifies the maximum size of a single batch in number of sprites
	 * <p>
	 * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
	 * the ones expect for shaders set with {@link #setShader(ShaderProgram)}. See the {@link #createDefaultShader()} method.
	 * @param size the batch size in number of sprites
	 * @param buffers the number of buffers to use. only makes sense with VBOs. This is an expert function.
	 * @param defaultShader the default shader to use. This is not owned by the SpriteBatch and must be disposed separately. */
	public SpriteBatch (int size, int buffers, ShaderProgram defaultShader) {
		if (size > 5460) {
			throw new GdxRuntimeException("Can't have more than 5460 sprites per batch");
		}
		this.buffers = new Mesh[buffers];

		for (int i = 0; i < buffers; i++) {
			this.buffers[i] = new Mesh(VertexDataType.VertexArray, false, size * 4, size * 6, new VertexAttribute(Usage.Position, 2,
				ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
				new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
		}

		projectionMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		vertices = new float[size * Sprite.SPRITE_SIZE];

		int len = size * 6;
		short[] indices = new short[len];
		short j = 0;
		for (int i = 0; i < len; i += 6, j += 4) {
			indices[i + 0] = (short)(j + 0);
			indices[i + 1] = (short)(j + 1);
			indices[i + 2] = (short)(j + 2);
			indices[i + 3] = (short)(j + 2);
			indices[i + 4] = (short)(j + 3);
			indices[i + 5] = (short)(j + 0);
		}
		for (int i = 0; i < buffers; i++) {
			this.buffers[i].setIndices(indices);
		}
		mesh = this.buffers[0];

		if (Gdx.graphics.isGL20Available() && defaultShader == null) {
			shader = createDefaultShader();
			ownsShader = true;
		} else
			shader = defaultShader;
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
		if (shader.isCompiled() == false) throw new IllegalArgumentException("couldn't compile shader: " + shader.getLog());
		return shader;
	}

	/** Sets up the SpriteBatch for drawing. This will disable depth buffer writting. It enables blending and texturing. If you have
	 * more texture units enabled than the first one you have to disable them before calling this. Uses a screen coordinate system
	 * by default where everything is given in pixels. You can specify your own projection and modelview matrices via
	 * {@link #setProjectionMatrix(Matrix4)} and {@link #setTransformMatrix(Matrix4)}. */
	public void begin () {
		if (drawing) throw new IllegalStateException("you have to call SpriteBatch.end() first");
		renderCalls = 0;

		Gdx.gl.glDepthMask(false);
		if (Gdx.graphics.isGL20Available()) {
			if (customShader != null)
				customShader.begin();
			else
				shader.begin();
		} else {
			Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
		}
		setupMatrices();

		idx = 0;
		lastTexture = null;
		drawing = true;
	}

	/** Finishes off rendering. Enables depth writes, disables blending and texturing. Must always be called after a call to
	 * {@link #begin()} */
	public void end () {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before end.");
		if (idx > 0) renderMesh();
		lastTexture = null;
		idx = 0;
		drawing = false;

		GLCommon gl = Gdx.gl;
		gl.glDepthMask(true);
		if (isBlendingEnabled()) gl.glDisable(GL10.GL_BLEND);

		if (Gdx.graphics.isGL20Available()) {
			if (customShader != null)
				customShader.end();
			else
				shader.end();
		} else {
			gl.glDisable(GL10.GL_TEXTURE_2D);
		}
	}

	/** Sets the color used to tint images when they are added to the SpriteBatch. Default is {@link Color#WHITE}. */
	public void setColor (Color tint) {
		color = tint.toFloatBits();
	}

	/** @see #setColor(Color) */
	public void setColor (float r, float g, float b, float a) {
		int intBits = (int)(255 * a) << 24 | (int)(255 * b) << 16 | (int)(255 * g) << 8 | (int)(255 * r);
		color = NumberUtils.intToFloatColor(intBits);
	}

	/** @see #setColor(Color)
	 * @see Color#toFloatBits() */
	public void setColor (float color) {
		this.color = color;
	}

	/** @return the rendering color of this SpriteBatch. Manipulating the returned instance has no effect. */
	public Color getColor () {
		int intBits = NumberUtils.floatToIntColor(color);
		Color color = this.tempColor;
		color.r = (intBits & 0xff) / 255f;
		color.g = ((intBits >>> 8) & 0xff) / 255f;
		color.b = ((intBits >>> 16) & 0xff) / 255f;
		color.a = ((intBits >>> 24) & 0xff) / 255f;
		return color;
	}

	/** Draws a rectangle with the bottom left corner at x,y having the given width and height in pixels. The rectangle is offset by
	 * originX, originY relative to the origin. Scale specifies the scaling factor by which the rectangle should be scaled around
	 * originX, originY. Rotation specifies the angle of counter clockwise rotation of the rectangle around originX, originY. The
	 * portion of the {@link Texture} given by srcX, srcY and srcWidth, srcHeight is used. These coordinates and sizes are given in
	 * texels. FlipX and flipY specify whether the texture portion should be fliped horizontally or vertically.
	 * 
	 * @param texture the Texture
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 * @param originX the x-coordinate of the scaling and rotation origin relative to the screen space coordinates
	 * @param originY the y-coordinate of the scaling and rotation origin relative to the screen space coordinates
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param scaleX the scale of the rectangle around originX/originY in x
	 * @param scaleY the scale of the rectangle around originX/originY in y
	 * @param rotation the angle of counter clockwise rotation of the rectangle around originX/originY
	 * @param srcX the x-coordinate in texel space
	 * @param srcY the y-coordinate in texel space
	 * @param srcWidth the source with in texels
	 * @param srcHeight the source height in texels
	 * @param flipX whether to flip the sprite horizontally
	 * @param flipY whether to flip the sprite vertically */
	public void draw (Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX,
		float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) renderMesh();

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

		vertices[idx++] = x1;
		vertices[idx++] = y1;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = x3;
		vertices[idx++] = y3;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = x4;
		vertices[idx++] = y4;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
	}

	/** Draws a rectangle with the bottom left corner at x,y having the given width and height in pixels. The portion of the
	 * {@link Texture} given by srcX, srcY and srcWidth, srcHeight is used. These coordinates and sizes are given in texels. FlipX
	 * and flipY specify whether the texture portion should be fliped horizontally or vertically.
	 * 
	 * @param texture the Texture
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param srcX the x-coordinate in texel space
	 * @param srcY the y-coordinate in texel space
	 * @param srcWidth the source with in texels
	 * @param srcHeight the source height in texels
	 * @param flipX whether to flip the sprite horizontally
	 * @param flipY whether to flip the sprite vertically */
	public void draw (Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
		int srcHeight, boolean flipX, boolean flipY) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) renderMesh();

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

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
	}

	/** Draws a rectangle with the bottom left corner at x,y having the given width and height in pixels. The portion of the
	 * {@link Texture} given by srcX, srcY and srcWidth, srcHeight are used. These coordinates and sizes are given in texels.
	 * 
	 * @param texture the Texture
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 * @param srcX the x-coordinate in texel space
	 * @param srcY the y-coordinate in texel space
	 * @param srcWidth the source with in texels
	 * @param srcHeight the source height in texels */
	public void draw (Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) renderMesh();

		final float u = srcX * invTexWidth;
		final float v = (srcY + srcHeight) * invTexHeight;
		final float u2 = (srcX + srcWidth) * invTexWidth;
		final float v2 = srcY * invTexHeight;
		final float fx2 = x + srcWidth;
		final float fy2 = y + srcHeight;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
	}

	/** Draws a rectangle with the bottom left corner at x,y having the given width and height in pixels. The portion of the
	 * {@link Texture} given by u, v and u2, v2 are used. These coordinates and sizes are given in texture size percentage. The
	 * rectangle will have the given tint {@link Color}.
	 * 
	 * @param texture the Texture
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 * @param width the width in pixels
	 * @param height the height in pixels */
	public void draw (Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) renderMesh();

		final float fx2 = x + width;
		final float fy2 = y + height;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
	}

	/** Draws a rectangle with the bottom left corner at x,y having the width and height of the texture.
	 * @param texture the Texture
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space */
	public void draw (Texture texture, float x, float y) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) renderMesh();

		final float fx2 = x + texture.getWidth();
		final float fy2 = y + texture.getHeight();

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = 0;
		vertices[idx++] = 1;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = 0;
		vertices[idx++] = 0;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = 1;
		vertices[idx++] = 0;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = 1;
		vertices[idx++] = 1;
	}

	/** Draws a rectangle with the bottom left corner at x,y and stretching the region to cover the given width and height. */
	public void draw (Texture texture, float x, float y, float width, float height) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) //
			renderMesh();

		final float fx2 = x + width;
		final float fy2 = y + height;
		final float u = 0;
		final float v = 1;
		final float u2 = 1;
		final float v2 = 0;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
	}

	/** Draws a rectangle using the given vertices. There must be 4 vertices, each made up of 5 elements in this order: x, y, color,
	 * u, v. The {@link #getColor()} from the SpriteBatch is not applied. */
	public void draw (Texture texture, float[] spriteVertices, int offset, int count) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			switchTexture(texture);
		}

		int remainingVertices = vertices.length - idx;
		if (remainingVertices == 0) {
			renderMesh();
			remainingVertices = vertices.length;
		}
		int copyCount = Math.min(remainingVertices, count);
		System.arraycopy(spriteVertices, offset, vertices, idx, copyCount);
		idx += copyCount;
		count -= copyCount;
		while (count > 0) {
			offset += copyCount;
			renderMesh();
			copyCount = Math.min(vertices.length, count);
			System.arraycopy(spriteVertices, offset, vertices, 0, copyCount);
			idx += copyCount;
			count -= copyCount;
		}
	}

	/** Draws a rectangle with the bottom left corner at x,y having the width and height of the region. */
	public void draw (TextureRegion region, float x, float y) {
		draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());
	}

	/** Draws a rectangle with the bottom left corner at x,y and stretching the region to cover the given width and height. */
	public void draw (TextureRegion region, float x, float y, float width, float height) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		Texture texture = region.texture;
		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) //
			renderMesh();

		final float fx2 = x + width;
		final float fy2 = y + height;
		final float u = region.u;
		final float v = region.v2;
		final float u2 = region.u2;
		final float v2 = region.v;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
	}

	/** Draws a rectangle with the bottom left corner at x,y and stretching the region to cover the given width and height. The
	 * rectangle is offset by originX, originY relative to the origin. Scale specifies the scaling factor by which the rectangle
	 * should be scaled around originX, originY. Rotation specifies the angle of counter clockwise rotation of the rectangle around
	 * originX, originY. */
	public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		Texture texture = region.texture;
		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) //
			renderMesh();

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

		vertices[idx++] = x1;
		vertices[idx++] = y1;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = x3;
		vertices[idx++] = y3;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = x4;
		vertices[idx++] = y4;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
	}

	/** Draws a rectangle with the texture coordinates rotated 90 degrees. The bottom left corner at x,y and stretching the region
	 * to cover the given width and height. The rectangle is offset by originX, originY relative to the origin. Scale specifies the
	 * scaling factor by which the rectangle should be scaled around originX, originY. Rotation specifies the angle of counter
	 * clockwise rotation of the rectangle around originX, originY.
	 * @param clockwise If true, the texture coordinates are rotated 90 degrees clockwise. If false, they are rotated 90 degrees
	 *           counter clockwise. */
	public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation, boolean clockwise) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		Texture texture = region.texture;
		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) //
			renderMesh();

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

		vertices[idx++] = x1;
		vertices[idx++] = y1;
		vertices[idx++] = color;
		vertices[idx++] = u1;
		vertices[idx++] = v1;

		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = x3;
		vertices[idx++] = y3;
		vertices[idx++] = color;
		vertices[idx++] = u3;
		vertices[idx++] = v3;

		vertices[idx++] = x4;
		vertices[idx++] = y4;
		vertices[idx++] = color;
		vertices[idx++] = u4;
		vertices[idx++] = v4;
	}

	/** Causes any pending sprites to be rendered, without ending the SpriteBatch. */
	public void flush () {
		renderMesh();
	}

	private void renderMesh () {
		if (idx == 0) return;

		renderCalls++;
		totalRenderCalls++;
		int spritesInBatch = idx / 20;
		if (spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch;

		lastTexture.bind();
		mesh.setVertices(vertices, 0, idx);
		mesh.getIndicesBuffer().position(0);
		mesh.getIndicesBuffer().limit(spritesInBatch * 6);

		if (blendingDisabled) {
			Gdx.gl.glDisable(GL20.GL_BLEND);
		} else {
			Gdx.gl.glEnable(GL20.GL_BLEND);
			if (blendSrcFunc != -1) Gdx.gl.glBlendFunc(blendSrcFunc, blendDstFunc);
		}

		if (Gdx.graphics.isGL20Available()) {
			if (customShader != null)
				mesh.render(customShader, GL10.GL_TRIANGLES, 0, spritesInBatch * 6);
			else
				mesh.render(shader, GL10.GL_TRIANGLES, 0, spritesInBatch * 6);
		} else {
			mesh.render(GL10.GL_TRIANGLES, 0, spritesInBatch * 6);
		}

		idx = 0;
		currBufferIdx++;
		if (currBufferIdx == buffers.length) currBufferIdx = 0;
		mesh = buffers[currBufferIdx];
	}

	/** Disables blending for drawing sprites. */
	public void disableBlending () {
		if (blendingDisabled) return;
		renderMesh();
		blendingDisabled = true;
	}

	/** Enables blending for sprites */
	public void enableBlending () {
		if (!blendingDisabled) return;
		renderMesh();
		blendingDisabled = false;
	}

	/** Sets the blending function to be used when rendering sprites.
	 * 
	 * @param srcFunc the source function, e.g. GL11.GL_SRC_ALPHA. If set to -1, SpriteBatch won't change the blending function.
	 * @param dstFunc the destination function, e.g. GL11.GL_ONE_MINUS_SRC_ALPHA */
	public void setBlendFunction (int srcFunc, int dstFunc) {
		if (blendSrcFunc == srcFunc && blendDstFunc == dstFunc) return;
		renderMesh();
		blendSrcFunc = srcFunc;
		blendDstFunc = dstFunc;
	}

	public int getBlendSrcFunc () {
		return blendSrcFunc;
	}

	public int getBlendDstFunc () {
		return blendDstFunc;
	}

	/** Disposes all resources associated with this SpriteBatch */
	public void dispose () {
		for (int i = 0; i < buffers.length; i++)
			buffers[i].dispose();
		if (ownsShader && shader != null) shader.dispose();
	}

	/** Returns the current projection matrix. Changing this within {@link #begin()}/{@link #end()} resultsin undefined behaviour.
	 * @return the currently set projection matrix */
	public Matrix4 getProjectionMatrix () {
		return projectionMatrix;
	}

	/** Returns the current transform matrix. Changing this within {@link #begin()}/{@link #end()} results in undefined behaviour.
	 * @return the currently set transform matrix */
	public Matrix4 getTransformMatrix () {
		return transformMatrix;
	}

	/** Sets the projection matrix to be used by this SpriteBatch. If this is called inside a {@link #begin()}/{@link #end()} block.
	 * the current batch is flushed to the gpu.
	 * @param projection the projection matrix */
	public void setProjectionMatrix (Matrix4 projection) {
		if (drawing) flush();
		projectionMatrix.set(projection);
		if (drawing) setupMatrices();
	}

	/** Sets the transform matrix to be used by this SpriteBatch. If this is called inside a {@link #begin()}/{@link #end()} block.
	 * the current batch is flushed to the gpu.
	 * 
	 * @param transform the transform matrix */
	public void setTransformMatrix (Matrix4 transform) {
		if (drawing) flush();
		transformMatrix.set(transform);
		if (drawing) setupMatrices();
	}

	private void setupMatrices () {
		if (!Gdx.graphics.isGL20Available()) {
			GL10 gl = Gdx.gl10;
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadMatrixf(projectionMatrix.val, 0);
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadMatrixf(transformMatrix.val, 0);
		} else {
			combinedMatrix.set(projectionMatrix).mul(transformMatrix);
			if (customShader != null) {
				customShader.setUniformMatrix("u_projTrans", combinedMatrix);
				customShader.setUniformi("u_texture", 0);
			} else {
				shader.setUniformMatrix("u_projTrans", combinedMatrix);
				shader.setUniformi("u_texture", 0);
			}
		}
	}

	private void switchTexture (Texture texture) {
		renderMesh();
		lastTexture = texture;
		invTexWidth = 1.0f / texture.getWidth();
		invTexHeight = 1.0f / texture.getHeight();
	}

	/** Sets the shader to be used in a GLES 2.0 environment. Vertex position attribute is called "a_position", the texture
	 * coordinates attribute is called called "a_texCoord0", the color attribute is called "a_color". See
	 * {@link ShaderProgram#POSITION_ATTRIBUTE}, {@link ShaderProgram#COLOR_ATTRIBUTE} and {@link ShaderProgram#TEXCOORD_ATTRIBUTE}
	 * which gets "0" appened to indicate the use of the first texture unit. The combined transform and projection matrx is is
	 * uploaded via a mat4 uniform called "u_projTrans". The texture sampler is passed via a uniform called "u_texture".</p>
	 * 
	 * Call this method with a null argument to use the default shader.</p>
	 * 
	 * This method will flush the batch before setting the new shader, you can call it in between {@link #begin()} and
	 * {@link #end()}.
	 * 
	 * @param shader the {@link ShaderProgram} or null to use the default shader. */
	public void setShader (ShaderProgram shader) {
		if (drawing) {
			flush();
			if (customShader != null)
				customShader.end();
			else
				this.shader.end();
		}
		customShader = shader;
		if (drawing) {
			if (customShader != null)
				customShader.begin();
			else
				this.shader.begin();
			setupMatrices();
		}
	}

	/** @return whether blending for sprites is enabled */
	public boolean isBlendingEnabled () {
		return !blendingDisabled;
	}

	static public final int X1 = 0;
	static public final int Y1 = 1;
	static public final int C1 = 2;
	static public final int U1 = 3;
	static public final int V1 = 4;
	static public final int X2 = 5;
	static public final int Y2 = 6;
	static public final int C2 = 7;
	static public final int U2 = 8;
	static public final int V2 = 9;
	static public final int X3 = 10;
	static public final int Y3 = 11;
	static public final int C3 = 12;
	static public final int U3 = 13;
	static public final int V3 = 14;
	static public final int X4 = 15;
	static public final int Y4 = 16;
	static public final int C4 = 17;
	static public final int U4 = 18;
	static public final int V4 = 19;
}
