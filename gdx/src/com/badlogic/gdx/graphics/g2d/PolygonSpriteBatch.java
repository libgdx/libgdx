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

import static com.badlogic.gdx.graphics.g2d.Sprite.*;

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
import com.badlogic.gdx.utils.NumberUtils;

/** A PolygonSpriteBatch is used to draw 2D polygons that reference a texture (region). The class will batch the drawing commands
 * and optimize them for processing by the GPU.
 * <p>
 * To draw something with a PolygonSpriteBatch one has to first call the {@link PolygonSpriteBatch#begin()} method which will
 * setup appropriate render states. When you are done with drawing you have to call {@link PolygonSpriteBatch#end()} which will
 * actually draw the things you specified.
 * <p>
 * All drawing commands of the PolygonSpriteBatch operate in screen coordinates. The screen coordinate system has an x-axis
 * pointing to the right, an y-axis pointing upwards and the origin is in the lower left corner of the screen. You can also
 * provide your own transformation and projection matrices if you so wish.
 * <p>
 * A PolygonSpriteBatch is managed. In case the OpenGL context is lost all OpenGL resources a PolygonSpriteBatch uses internally
 * get invalidated. A context is lost when a user switches to another application or receives an incoming call on Android. A
 * SpritPolygonSpriteBatcheBatch will be automatically reloaded after the OpenGL context is restored.
 * <p>
 * A PolygonSpriteBatch is a pretty heavy object so you should only ever have one in your program.
 * <p>
 * A PolygonSpriteBatch works with OpenGL ES 1.x and 2.0. In the case of a 2.0 context it will use its own custom shader to draw
 * all provided sprites. You can set your own custom shader via {@link #setShader(ShaderProgram)}.
 * <p>
 * A PolygonSpriteBatch has to be disposed if it is no longer used.
 * @author mzechner
 * @author Stefan Bachmann
 * @author Nathan Sweet */
public class PolygonSpriteBatch {
	private Mesh mesh;
	private Mesh[] buffers;
	private int bufferIndex;

	private final float[] vertices;
	private final short[] triangles;
	private int vertexIndex, triangleIndex;
	private Texture lastTexture;
	private boolean drawing;

	private final Matrix4 transformMatrix = new Matrix4();
	private final Matrix4 projectionMatrix = new Matrix4();
	private final Matrix4 combinedMatrix = new Matrix4();

	private boolean blendingDisabled;
	private int blendSrcFunc = GL11.GL_SRC_ALPHA;
	private int blendDstFunc = GL11.GL_ONE_MINUS_SRC_ALPHA;

	private final ShaderProgram shader;
	private ShaderProgram customShader;
	private boolean ownsShader;

	float color = Color.WHITE.toFloatBits();
	private Color tempColor = new Color(1, 1, 1, 1);

	/** Number of render calls since the last {@link #begin()}. **/
	public int renderCalls = 0;

	/** Number of rendering calls, ever. Will not be reset unless set manually. **/
	public int totalRenderCalls = 0;

	/** The maximum number of triangles rendered in one batch so far. **/
	public int maxTrianglesInBatch = 0;

	/** Constructs a new PolygonSpriteBatch with a size of 2000, the default shader, and one buffer.
	 * @see PolygonSpriteBatch#PolygonSpriteBatch(int, int, ShaderProgram) */
	public PolygonSpriteBatch () {
		this(2000, null);
	}

	/** Constructs a PolygonSpriteBatch with the default shader and one buffer.
	 * @see PolygonSpriteBatch#PolygonSpriteBatch(int, int, ShaderProgram) */
	public PolygonSpriteBatch (int size) {
		this(size, 1, null);
	}

	/** Constructs a new PolygonSpriteBatch with one buffer.
	 * @see PolygonSpriteBatch#PolygonSpriteBatch(int, int, ShaderProgram) */
	public PolygonSpriteBatch (int size, ShaderProgram defaultShader) {
		this(size, 1, defaultShader);
	}

	/** Constructs a PolygonSpriteBatch with the default shader.
	 * @see PolygonSpriteBatch#PolygonSpriteBatch(int, int, ShaderProgram) */
	public PolygonSpriteBatch (int size, int buffers) {
		this(size, buffers, null);
	}

	/** Constructs a new PolygonSpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards,
	 * x-axis point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect
	 * with respect to the current screen resolution.
	 * <p>
	 * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
	 * the ones expect for shaders set with {@link #setShader(ShaderProgram)}. See {@link SpriteBatch#createDefaultShader()}.
	 * @param size The max number of vertices and number of triangles in a single batch. Max of 10920.
	 * @param buffers The number of meshes to use. This is an expert function. It only makes sense with VBOs (see
	 *           {@link Mesh#forceVBO}).
	 * @param defaultShader The default shader to use. This is not owned by the PolygonSpriteBatch and must be disposed separately. */
	public PolygonSpriteBatch (int size, int buffers, ShaderProgram defaultShader) {
		// 32767 is max index, so 32767 / 3 - (32767 / 3 % 3) = 10920.
		if (size > 10920) throw new IllegalArgumentException("Can't have more than 10920 triangles per batch: " + size);

		this.buffers = new Mesh[buffers];
		for (int i = 0; i < buffers; i++) {
			this.buffers[i] = new Mesh(VertexDataType.VertexArray, false, size, size * 3, new VertexAttribute(Usage.Position, 2,
				ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
				new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
		}
		mesh = this.buffers[0];

		vertices = new float[size * VERTEX_SIZE];
		triangles = new short[size * 3];

		if (Gdx.graphics.isGL20Available() && defaultShader == null) {
			shader = SpriteBatch.createDefaultShader();
			ownsShader = true;
		} else
			shader = defaultShader;

		projectionMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	/** Sets up the PolygonSpriteBatch for drawing. This will disable depth buffer writting. It enables blending and texturing. If
	 * you have more texture units enabled than the first one you have to disable them before calling this. Uses a screen
	 * coordinate system by default where everything is given in pixels. You can specify your own projection and modelview matrices
	 * via {@link #setProjectionMatrix(Matrix4)} and {@link #setTransformMatrix(Matrix4)}. */
	public void begin () {
		if (drawing) throw new IllegalStateException("PolygonSpriteBatch.end must be called before begin.");
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

		drawing = true;
	}

	/** Finishes off rendering. Enables depth writes, disables blending and texturing. Must always be called after a call to
	 * {@link #begin()} */
	public void end () {
		if (!drawing) throw new IllegalStateException("PolygonSpriteBatch.begin must be called before end.");
		if (vertexIndex > 0) flush();
		lastTexture = null;
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

	/** Sets the color used to tint images when they are added to the PolygonSpriteBatch. Default is {@link Color#WHITE}. */
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

	/** @return the rendering color of this PolygonSpriteBatch. Manipulating the returned instance has no effect. */
	public Color getColor () {
		int intBits = NumberUtils.floatToIntColor(color);
		Color color = this.tempColor;
		color.r = (intBits & 0xff) / 255f;
		color.g = ((intBits >>> 8) & 0xff) / 255f;
		color.b = ((intBits >>> 16) & 0xff) / 255f;
		color.a = ((intBits >>> 24) & 0xff) / 255f;
		return color;
	}

	/** Draws a polygon region with the bottom left corner at x,y having the width and height of the region. */
	public void draw (PolygonRegion region, float x, float y) {
		if (!drawing) throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");

		final short[] triangles = this.triangles;
		final short[] regionTriangles = region.triangles;
		final int regionTrianglesLength = regionTriangles.length;
		final float[] regionVertices = region.vertices;
		final int regionVerticesLength = regionVertices.length;

		final Texture texture = region.region.texture;
		if (texture != lastTexture)
			switchTexture(texture);
		else if (triangleIndex + regionTrianglesLength > triangles.length || vertexIndex + regionVerticesLength > vertices.length)
			flush();

		int triangleIndex = this.triangleIndex;
		int vertexIndex = this.vertexIndex;
		final int startVertex = vertexIndex / VERTEX_SIZE;

		for (int i = 0; i < regionTrianglesLength; i++)
			triangles[triangleIndex++] = (short)(regionTriangles[i] + startVertex);
		this.triangleIndex = triangleIndex;

		final float[] vertices = this.vertices;
		final float color = this.color;
		final float[] textureCoords = region.textureCoords;

		for (int i = 0; i < regionVerticesLength; i += 2) {
			vertices[vertexIndex++] = regionVertices[i] + x;
			vertices[vertexIndex++] = regionVertices[i + 1] + y;
			vertices[vertexIndex++] = color;
			vertices[vertexIndex++] = textureCoords[i];
			vertices[vertexIndex++] = textureCoords[i + 1];
		}
		this.vertexIndex = vertexIndex;
	}

	/** Draws a polygon region with the bottom left corner at x,y and stretching the region to cover the given width and height. */
	public void draw (PolygonRegion region, float x, float y, float width, float height) {
		if (!drawing) throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");

		final short[] triangles = this.triangles;
		final short[] regionTriangles = region.triangles;
		final int regionTrianglesLength = regionTriangles.length;
		final float[] regionVertices = region.vertices;
		final int regionVerticesLength = regionVertices.length;
		final TextureRegion textureRegion = region.region;

		final Texture texture = textureRegion.texture;
		if (texture != lastTexture)
			switchTexture(texture);
		else if (triangleIndex + regionTrianglesLength > triangles.length || vertexIndex + regionVerticesLength > vertices.length)
			flush();

		int triangleIndex = this.triangleIndex;
		int vertexIndex = this.vertexIndex;
		final int startVertex = vertexIndex / VERTEX_SIZE;

		for (int i = 0, n = regionTriangles.length; i < n; i++)
			triangles[triangleIndex++] = (short)(regionTriangles[i] + startVertex);
		this.triangleIndex = triangleIndex;

		final float[] vertices = this.vertices;
		final float color = this.color;
		final float[] textureCoords = region.textureCoords;
		final float sX = width / textureRegion.regionWidth;
		final float sY = height / textureRegion.regionHeight;

		for (int i = 0; i < regionVerticesLength; i += 2) {
			vertices[vertexIndex++] = regionVertices[i] * sX + x;
			vertices[vertexIndex++] = regionVertices[i + 1] * sY + y;
			vertices[vertexIndex++] = color;
			vertices[vertexIndex++] = textureCoords[i];
			vertices[vertexIndex++] = textureCoords[i + 1];
		}
		this.vertexIndex = vertexIndex;
	}

	/** Draws the polygon region with the bottom left corner at x,y and stretching the region to cover the given width and height.
	 * The polygon region is offset by originX, originY relative to the origin. Scale specifies the scaling factor by which the
	 * polygon region should be scaled around originX, originY. Rotation specifies the angle of counter clockwise rotation of the
	 * rectangle around originX, originY. */
	public void draw (PolygonRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation) {
		if (!drawing) throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");

		final short[] triangles = this.triangles;
		final short[] regionTriangles = region.triangles;
		final int regionTrianglesLength = regionTriangles.length;
		final float[] regionVertices = region.vertices;
		final int regionVerticesLength = regionVertices.length;
		final TextureRegion textureRegion = region.region;

		Texture texture = textureRegion.texture;
		if (texture != lastTexture)
			switchTexture(texture);
		else if (triangleIndex + regionTrianglesLength > triangles.length || vertexIndex + regionVerticesLength > vertices.length)
			flush();

		int triangleIndex = this.triangleIndex;
		int vertexIndex = this.vertexIndex;
		final int startVertex = vertexIndex / VERTEX_SIZE;

		for (int i = 0; i < regionTrianglesLength; i++)
			triangles[triangleIndex++] = (short)(regionTriangles[i] + startVertex);
		this.triangleIndex = triangleIndex;

		final float[] vertices = this.vertices;
		final float color = this.color;
		final float[] textureCoords = region.textureCoords;

		final float worldOriginX = x + originX;
		final float worldOriginY = y + originY;
		final float sX = width / textureRegion.regionWidth;
		final float sY = height / textureRegion.regionHeight;
		final float cos = MathUtils.cosDeg(rotation);
		final float sin = MathUtils.sinDeg(rotation);

		float fx, fy;
		for (int i = 0; i < regionVerticesLength; i += 2) {
			fx = (regionVertices[i] * sX - originX) * scaleX;
			fy = (regionVertices[i + 1] * sY - originY) * scaleY;
			vertices[vertexIndex++] = cos * fx - sin * fy + worldOriginX;
			vertices[vertexIndex++] = sin * fx + cos * fy + worldOriginY;
			vertices[vertexIndex++] = color;
			vertices[vertexIndex++] = textureCoords[i];
			vertices[vertexIndex++] = textureCoords[i + 1];
		}
		this.vertexIndex = vertexIndex;
	}

	/** Draws the polygon using the given vertices and triangles. Each vertices must be made up of 5 elements in this order: x, y,
	 * color, u, v. */
	public void draw (Texture texture, float[] polygonVertices, int verticesOffset, int verticesCount, short[] polygonTriangles,
		int trianglesOffset, int trianglesCount) {
		if (!drawing) throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");

		final short[] triangles = this.triangles;
		final float[] vertices = this.vertices;

		if (texture != lastTexture)
			switchTexture(texture);
		else if (triangleIndex + trianglesCount > triangles.length || vertexIndex + verticesCount > vertices.length) //
			flush();

		int triangleIndex = this.triangleIndex;
		final int vertexIndex = this.vertexIndex;
		final int startVertex = vertexIndex / VERTEX_SIZE;

		for (int i = trianglesOffset, n = i + trianglesCount; i < n; i++)
			triangles[triangleIndex++] = (short)(polygonTriangles[i] + startVertex);
		this.triangleIndex = triangleIndex;

		System.arraycopy(polygonVertices, verticesOffset, vertices, vertexIndex, verticesCount);
		this.vertexIndex += verticesCount;
	}

	/** Causes any pending sprites to be rendered, without ending the PolygonSpriteBatch. */
	public void flush () {
		if (vertexIndex == 0) return;

		renderCalls++;
		totalRenderCalls++;
		int trianglesInBatch = triangleIndex;
		if (trianglesInBatch > maxTrianglesInBatch) maxTrianglesInBatch = trianglesInBatch;

		lastTexture.bind();
		Mesh mesh = this.mesh;
		mesh.setVertices(vertices, 0, vertexIndex);
		mesh.setIndices(triangles, 0, triangleIndex);

		if (blendingDisabled) {
			Gdx.gl.glDisable(GL20.GL_BLEND);
		} else {
			Gdx.gl.glEnable(GL20.GL_BLEND);
			if (blendSrcFunc != -1) Gdx.gl.glBlendFunc(blendSrcFunc, blendDstFunc);
		}

		if (Gdx.graphics.isGL20Available())
			mesh.render(customShader != null ? customShader : shader, GL10.GL_TRIANGLES, 0, trianglesInBatch);
		else
			mesh.render(GL10.GL_TRIANGLES, 0, trianglesInBatch);

		vertexIndex = 0;
		triangleIndex = 0;
		bufferIndex++;
		if (bufferIndex == buffers.length) bufferIndex = 0;
		this.mesh = buffers[bufferIndex];
	}

	/** Disables blending for drawing sprites. Calling this within {@link #begin()}/{@link #end()} will flush the batch. */
	public void disableBlending () {
		flush();
		blendingDisabled = true;
	}

	/** Enables blending for sprites. Calling this within {@link #begin()}/{@link #end()} will flush the batch. */
	public void enableBlending () {
		flush();
		blendingDisabled = false;
	}

	/** Sets the blending function to be used when rendering sprites.
	 * @param srcFunc the source function, e.g. GL11.GL_SRC_ALPHA. If set to -1, PolygonSpriteBatch won't change the blending
	 *           function.
	 * @param dstFunc the destination function, e.g. GL11.GL_ONE_MINUS_SRC_ALPHA */
	public void setBlendFunction (int srcFunc, int dstFunc) {
		if (blendSrcFunc == srcFunc && blendDstFunc == dstFunc) return;
		flush();
		blendSrcFunc = srcFunc;
		blendDstFunc = dstFunc;
	}

	/** Disposes all resources associated with this PolygonSpriteBatch. */
	public void dispose () {
		for (int i = 0; i < buffers.length; i++)
			buffers[i].dispose();
		if (ownsShader && shader != null) shader.dispose();
	}

	/** Returns the current projection matrix. Changing this within {@link #begin()}/{@link #end()} results in undefined behaviour. */
	public Matrix4 getProjectionMatrix () {
		return projectionMatrix;
	}

	/** Returns the current transform matrix. Changing this within {@link #begin()}/{@link #end()} results in undefined behaviour. */
	public Matrix4 getTransformMatrix () {
		return transformMatrix;
	}

	/** Sets the projection matrix to be used by this PolygonSpriteBatch. If this is called inside a {@link #begin()}/{@link #end()}
	 * block, the current batch is flushed to the gpu. */
	public void setProjectionMatrix (Matrix4 projection) {
		if (drawing) flush();
		projectionMatrix.set(projection);
		if (drawing) setupMatrices();
	}

	/** Sets the transform matrix to be used by this PolygonSpriteBatch. If this is called inside a {@link #begin()}/{@link #end()}
	 * block, the current batch is flushed to the gpu. */
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
		flush();
		lastTexture = texture;
	}

	/** @see SpriteBatch#setShader(ShaderProgram) */
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
}
