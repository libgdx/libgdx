/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.MathUtils;

/**
 * <p>
 * A SpriteBatch is used to draw 2D rectangles that reference a texture (region). The class will batch the drawing commands and
 * optimize them for processing by the GPU.
 * </p>
 * 
 * <p>
 * To draw something with a SpriteBatch one has to first call the {@link SpriteBatch#begin()} method which will setup appropriate
 * render states. When you are done with drawing you have to call {@link SpriteBatch#end()} which will actually draw the things
 * you specified.
 * </p>
 * 
 * <p>
 * All drawing commands of the SpriteBatch operate in screen coordinates. The screen coordinate system has an x-axis pointing to
 * the right, an y-axis pointing upwards and the origin is in the lower left corner of the screen. You can also provide your own
 * transformation and projection matrices if you so wish.
 * </p>
 * 
 * <p>
 * A SpriteBatch is managed. In case the OpenGL context is lost all OpenGL resources a SpriteBatch uses internally get
 * invalidated. A context is lost when a user switches to another application or receives an incoming call on Android. A
 * SpriteBatch will be automatically reloaded after the OpenGL context is restored.
 * </p>
 * 
 * <p>
 * A SpriteBatch is a pretty heavy object so you should only ever have one in your program.
 * </p>
 * 
 * <p>
 * A SpriteBatch works with OpenGL ES 1.x and 2.0. In the case of a 2.0 context it will use its own custom shader to draw all
 * provided sprites. Specifying your own shader does not work (yet).
 * </p>
 * 
 * <p>
 * A SpriteBatch has to be disposed if it is no longer used.
 * </p>
 * 
 * @author mzechner
 * 
 */
public class SpriteBatch {
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

	private ShaderProgram shader;

	float color = Color.WHITE.toFloatBits();
	private Color tempColor = new Color(1, 1, 1, 1);

	/** number of render calls **/
	public int renderCalls = 0;

	/** the maximum number of sprites rendered in one batch so far **/
	public int maxSpritesInBatch = 0;

	/**
	 * Constructs a new SpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards, x-axis
	 * point to the right and the origin being in the bottome left corner of the screen. The projection will be pixel perfect with
	 * respect to the screen resolution.
	 */
	public SpriteBatch () {
		this(1000);
	}

	/**
	 * <p>
	 * Constructs a new SpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards, x-axis
	 * point to the right and the origin being in the bottome left corner of the screen. The projection will be pixel perfect with
	 * respect to the screen resolution.
	 * </p>
	 * 
	 * <p>
	 * The size parameter specifies the maximum size of a single batch in number of sprites
	 * </p>
	 * 
	 * @param size the batch size in number of sprites
	 */
	public SpriteBatch (int size) {
		this.buffers = new Mesh[1];
		this.buffers[0] = new Mesh(VertexDataType.VertexArray, false, size * 4, size * 6, new VertexAttribute(Usage.Position, 2,
			"a_position"), new VertexAttribute(Usage.ColorPacked, 4, "a_color"), new VertexAttribute(Usage.TextureCoordinates, 2,
			"a_texCoords"));

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
		buffers[0].setIndices(indices);
		mesh = buffers[0];

		if (Gdx.graphics.isGL20Available()) createShader();
	}

	/**
	 * <p>
	 * Constructs a new SpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards, x-axis
	 * point to the right and the origin being in the bottome left corner of the screen. The projection will be pixel perfect with
	 * respect to the screen resolution.
	 * </p>
	 * 
	 * <p>
	 * The size parameter specifies the maximum size of a single batch in number of sprites
	 * </p>
	 * 
	 * @param size the batch size in number of sprites
	 * @param buffers the number of buffers to use. only makes sense with VBOs. This is an expert function.
	 */
	public SpriteBatch (int size, int buffers) {
		this.buffers = new Mesh[buffers];

		for (int i = 0; i < buffers; i++) {
			this.buffers[i] = new Mesh(false, size * 4, size * 6, new VertexAttribute(Usage.Position, 2, "a_position"),
				new VertexAttribute(Usage.ColorPacked, 4, "a_color"), new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoords"));
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

		if (Gdx.graphics.isGL20Available()) createShader();
	}

	private void createShader () {
		String vertexShader = "attribute vec4 a_position;\n" //
			+ "attribute vec4 a_color;\n" //
			+ "attribute vec2 a_texCoords;\n" //
			+ "uniform mat4 u_projectionViewMatrix;\n" //
			+ "varying vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "   v_color = a_color;\n" //
			+ "   v_texCoords = a_texCoords;\n" //
			+ "   gl_Position =  u_projectionViewMatrix * a_position;\n" //
			+ "}\n";
		String fragmentShader = "#ifdef GL_ES\n" //
			+ "precision mediump float;\n" //
			+ "#endif\n" //
			+ "varying vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "uniform sampler2D u_texture;\n" //
			+ "void main()\n"//
			+ "{\n" //
			+ "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" //
			+ "}";

		shader = new ShaderProgram(vertexShader, fragmentShader);
		if (shader.isCompiled() == false) throw new IllegalArgumentException("couldn't compile shader: " + shader.getLog());
	}

	/**
	 * Sets up the SpriteBatch for drawing. This will disable depth buffer testing and writting, culling and lighting. It enables
	 * blending and alpha testing. If you have more texture units enabled than the first one you have to disable them before
	 * calling this. Uses a screen coordinate system by default where everything is given in pixels. You can specify your own
	 * projection and modelview matrices via {@link #setProjectionMatrix(Matrix4)} and {@link #setTransformMatrix(Matrix4)}.
	 */
	public void begin () {
		if (drawing) throw new IllegalStateException("you have to call SpriteBatch.end() first");
		renderCalls = 0;

		if (Gdx.graphics.isGL20Available() == false) {
			GL10 gl = Gdx.gl10;			
			gl.glDisable(GL10.GL_LIGHTING);
			gl.glDisable(GL10.GL_DEPTH_TEST);
			gl.glDisable(GL10.GL_CULL_FACE);
			gl.glDepthMask(false);

			gl.glEnable(GL10.GL_TEXTURE_2D);
			// gl.glActiveTexture( GL10.GL_TEXTURE0 );

			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadMatrixf(projectionMatrix.val, 0);
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadMatrixf(transformMatrix.val, 0);
		} else {
			combinedMatrix.set(projectionMatrix).mul(transformMatrix);

			GL20 gl = Gdx.gl20;			
			gl.glDisable(GL20.GL_DEPTH_TEST);
			gl.glDisable(GL20.GL_CULL_FACE);
			gl.glDepthMask(false);

			gl.glEnable(GL20.GL_TEXTURE_2D);
			// gl.glActiveTexture( GL20.GL_TEXTURE0 );

			shader.begin();
			shader.setUniformMatrix("u_projectionViewMatrix", combinedMatrix);
			shader.setUniformi("u_texture", 0);
		}

		idx = 0;
		lastTexture = null;
		drawing = true;
	}

	/**
	 * Finishes off rendering. Must always be called after a call to {@link #begin()}
	 */
	public void end () {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before end.");
		if (idx > 0) renderMesh();
		lastTexture = null;
		idx = 0;
		drawing = false;

		if (Gdx.graphics.isGL20Available() == false) {
			GL10 gl = Gdx.gl10;
			gl.glDepthMask(true);
			gl.glDisable(GL10.GL_BLEND);
			gl.glDisable(GL10.GL_TEXTURE_2D);
		} else {
			shader.end();
			GL20 gl = Gdx.gl20;
			gl.glDepthMask(true);
			gl.glDisable(GL20.GL_BLEND);
			gl.glDisable(GL20.GL_TEXTURE_2D);
		}
	}

	/**
	 * Sets the color used to tint images when they are added to the SpriteBatch. Default is {@link Color#WHITE}.
	 */
	public void setColor (Color tint) {
		color = tint.toFloatBits();
	}

	/**
	 * @see #setColor(Color)
	 */
	public void setColor (float r, float g, float b, float a) {
		int intBits = (int)(255 * a) << 24 | (int)(255 * b) << 16 | (int)(255 * g) << 8 | (int)(255 * r);
		color = Float.intBitsToFloat(intBits & 0xfeffffff);
	}

	/**
	 * @see #setColor(Color)
	 * @see Color#toFloatBits()
	 */
	public void setColor (float color) {
		this.color = color;
	}

	public Color getColor () {
		int intBits = Float.floatToRawIntBits(color);
		Color color = this.tempColor;
		color.r = (intBits & 0xff) / 255f;
		color.g = ((intBits >>> 8) & 0xff) / 255f;
		color.b = ((intBits >>> 16) & 0xff) / 255f;
		color.a = ((intBits >>> 24) & 0xff) / 255f;
		return color;
	}

	/**
	 * Draws a rectangle with the top left corner at x,y having the given width and height in pixels. The rectangle is offset by
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
	 * @param flipY whether to flip the sprite vertically
	 */
	public void draw (Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX,
		float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			renderMesh();
			lastTexture = texture;
			invTexWidth = 1.0f / texture.getWidth();
			invTexHeight = 1.0f / texture.getHeight();
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

	/**
	 * Draws a rectangle with the top left corner at x,y having the given width and height in pixels. The portion of the
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
	 * @param flipY whether to flip the sprite vertically
	 */
	public void draw (Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
		int srcHeight, boolean flipX, boolean flipY) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			renderMesh();
			lastTexture = texture;
			invTexWidth = 1.0f / texture.getWidth();
			invTexHeight = 1.0f / texture.getHeight();
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

	/**
	 * Draws a rectangle with the top left corner at x,y having the given width and height in pixels. The portion of the
	 * {@link Texture} given by srcX, srcY and srcWidth, srcHeight are used. These coordinates and sizes are given in texels.
	 * 
	 * @param texture the Texture
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 * @param srcX the x-coordinate in texel space
	 * @param srcY the y-coordinate in texel space
	 * @param srcWidth the source with in texels
	 * @param srcHeight the source height in texels
	 */
	public void draw (Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			renderMesh();
			lastTexture = texture;
			invTexWidth = 1.0f / texture.getWidth();
			invTexHeight = 1.0f / texture.getHeight();
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

	/**
	 * Draws a rectangle with the top left corner at x,y having the given width and height in pixels. The portion of the
	 * {@link Texture} given by u, v and u2, v2 are used. These coordinates and sizes are given in texture size percentage. The
	 * rectangle will have the given tint {@link Color}.
	 * 
	 * @param texture the Texture
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 * @param width the width in pixels
	 * @param height the height in pixels
	 */
	public void draw (Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			renderMesh();
			lastTexture = texture;
			invTexWidth = 1.0f / texture.getWidth();
			invTexHeight = 1.0f / texture.getHeight();
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

	/**
	 * Draws a rectangle with the top left corner at x,y having the width and height of the texture.
	 * @param texture the Texture
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 */
	public void draw (Texture texture, float x, float y) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			renderMesh();
			lastTexture = texture;
			invTexWidth = 1.0f / texture.getWidth();
			invTexHeight = 1.0f / texture.getHeight();
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

	/**
	 * Draws a rectangle using the given vertices. There must be 4 vertices, each made up of 5 elements in this order: x, y, color,
	 * u, v.
	 */
	public void draw (Texture texture, float[] spriteVertices, int offset, int length) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			renderMesh();
			lastTexture = texture;
			invTexWidth = 1.0f / texture.getWidth();
			invTexHeight = 1.0f / texture.getHeight();
		} else if (idx + length >= vertices.length) renderMesh();

		System.arraycopy(spriteVertices, offset, vertices, idx, length);
		idx += length;
	}

	/**
	 * Draws a rectangle with the top left corner at x,y having the width and height of the region.
	 */
	public void draw (TextureRegion region, float x, float y) {
		draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());
	}

	/**
	 * Draws a rectangle with the top left corner at x,y and stretching the region to cover the given width and height.
	 */
	public void draw (TextureRegion region, float x, float y, float width, float height) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		Texture texture = region.texture;
		if (texture != lastTexture) {
			renderMesh();
			lastTexture = texture;
			invTexWidth = 1f / texture.getWidth();
			invTexHeight = 1f / texture.getHeight();
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

	/**
	 * Draws a rectangle with the top left corner at x,y and stretching the region to cover the given width and height. The
	 * rectangle is offset by originX, originY relative to the origin. Scale specifies the scaling factor by which the rectangle
	 * should be scaled around originX, originY. Rotation specifies the angle of counter clockwise rotation of the rectangle around
	 * originX, originY.
	 */
	public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		Texture texture = region.texture;
		if (texture != lastTexture) {
			renderMesh();
			lastTexture = texture;
			invTexWidth = 1f / texture.getWidth();
			invTexHeight = 1f / texture.getHeight();
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

	/**
	 * Causes any pending sprites to be rendered, without ending the SpriteBatch.
	 */
	public void flush () {
		renderMesh();
	}

	private void renderMesh () {
		if (idx == 0) return;

		renderCalls++;
		int spritesInBatch = idx / 20;
		if (spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch;

		lastTexture.bind();
		mesh.setVertices(vertices, 0, idx);

		if (Gdx.graphics.isGL20Available()) {
			if (blendingDisabled) {
				Gdx.gl20.glDisable(GL20.GL_BLEND);
			} else {
				GL20 gl20 = Gdx.gl20;
				gl20.glEnable(GL20.GL_BLEND);
				gl20.glBlendFunc(blendSrcFunc, blendDstFunc);
			}

			mesh.render(shader, GL10.GL_TRIANGLES, 0, spritesInBatch * 6);
		} else {
			if (blendingDisabled) {
				Gdx.gl10.glDisable(GL10.GL_BLEND);
			} else {
				GL10 gl10 = Gdx.gl10;
				gl10.glEnable(GL10.GL_BLEND);
				gl10.glBlendFunc(blendSrcFunc, blendDstFunc);
			}
			mesh.render(GL10.GL_TRIANGLES, 0, spritesInBatch * 6);
		}

		idx = 0;
		currBufferIdx++;
		if (currBufferIdx == buffers.length) currBufferIdx = 0;
		mesh = buffers[currBufferIdx];
	}

	/**
	 * Disables blending for drawing sprites. Does not disable blending for text rendering
	 */
	public void disableBlending () {
		renderMesh();
		blendingDisabled = true;
	}

	/**
	 * Enables blending for sprites
	 */
	public void enableBlending () {
		renderMesh();
		blendingDisabled = false;
	}

	/**
	 * Sets the blending function to be used when rendering sprites.
	 * 
	 * @param srcFunc the source function, e.g. GL11.GL_SRC_ALPHA
	 * @param dstFunc the destination function, e.g. GL11.GL_ONE_MINUS_SRC_ALPHA
	 */
	public void setBlendFunction (int srcFunc, int dstFunc) {
		renderMesh();
		blendSrcFunc = srcFunc;
		blendDstFunc = dstFunc;
	}

	/**
	 * Disposes all resources associated with this SpriteBatch
	 */
	public void dispose () {
		for (int i = 0; i < buffers.length; i++)
			buffers[i].dispose();
		if (shader != null) shader.dispose();
	}

	/**
	 * Returns the current projection matrix. Changing this will result in undefined behaviour.
	 * 
	 * @return the currently set projection matrix
	 */
	public Matrix4 getProjectionMatrix () {
		return projectionMatrix;
	}

	/**
	 * Returns the current transform matrix. Changing this will result in undefined behaviour.
	 * 
	 * @return the currently set transform matrix
	 */
	public Matrix4 getTransformMatrix () {
		return transformMatrix;
	}

	/**
	 * Sets the projection matrix to be used by this SpriteBatch. Can only be set outside a {@link #begin()}/{@link #end()} block.
	 * 
	 * @param projection the projection matrix
	 */
	public void setProjectionMatrix (Matrix4 projection) {
		if (drawing) throw new GdxRuntimeException("Can't set the matrix within begin()/end() block");

		projectionMatrix.set(projection);
	}

	/**
	 * Sets the transform matrix to be used by this SpriteBatch. Can only be set outside a {@link #begin()}/{@link #end()} block.
	 * 
	 * @param transform the transform matrix
	 */
	public void setTransformMatrix (Matrix4 transform) {
		if (drawing) throw new GdxRuntimeException("Can't set the matrix within begin()/end() block");

		transformMatrix.set(transform);
	}

	/**
	 * @return whether blending for sprites is enabled
	 */
	public boolean isBlendingEnabled () {
		return !blendingDisabled;
	}
}
