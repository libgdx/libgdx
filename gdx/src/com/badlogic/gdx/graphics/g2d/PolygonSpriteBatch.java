
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
import com.badlogic.gdx.utils.NumberUtils;

/** <p>
 * A PolyongSpriteBatch is used to draw 2D polygons that reference a texture (region). The class will batch the drawing commands
 * and optimize them for processing by the GPU.
 * </p>
 * 
 * THIS STUFF IS WIP
 * 
 * <p>
 * To draw something with a PolyongSpriteBatch one has to first call the {@link PolygonSpriteBatch#begin()} method which will
 * setup appropriate render states. When you are done with drawing you have to call {@link PolygonSpriteBatch#end()} which will
 * actually draw the things you specified.
 * </p>
 * 
 * <p>
 * All drawing commands of the PolyongSpriteBatch operate in screen coordinates. The screen coordinate system has an x-axis
 * pointing to the right, an y-axis pointing upwards and the origin is in the lower left corner of the screen. You can also
 * provide your own transformation and projection matrices if you so wish.
 * </p>
 * 
 * <p>
 * A PolyongSpriteBatch is managed. In case the OpenGL context is lost all OpenGL resources a PolyongSpriteBatch uses internally
 * get invalidated. A context is lost when a user switches to another application or receives an incoming call on Android. A
 * SpritPolyongSpriteBatcheBatch will be automatically reloaded after the OpenGL context is restored.
 * </p>
 * 
 * <p>
 * A PolyongSpriteBatch is a pretty heavy object so you should only ever have one in your program.
 * </p>
 * 
 * <p>
 * A PolyongSpriteBatch works with OpenGL ES 1.x and 2.0. In the case of a 2.0 context it will use its own custom shader to draw
 * all provided sprites. You can set your own custom shader via {@link #setShader(ShaderProgram)}.
 * </p>
 * 
 * <p>
 * A PolyongSpriteBatch has to be disposed if it is no longer used.
 * </p>
 * 
 * 
 * @author mzechner
 * @author Stefan Bachmann */
public class PolygonSpriteBatch {
	private Mesh mesh;
	private Mesh[] buffers;

	private Texture lastTexture = null;

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
	public int maxVerticesInBatch = 0;
	private ShaderProgram customShader = null;

	/** Constructs a new PolygonSpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards,
	 * x-axis point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect
	 * with respect to the screen resolution. */
	public PolygonSpriteBatch () {
		this(4000);
	}

	/** Constructs a PolygonSpriteBatch with the specified size in vertices and (if GL2) the default shader. See
	 * {@link #PolygonSpriteBatch(int, ShaderProgram)}. */
	public PolygonSpriteBatch (int size) {
		this(size, null);
	}

	/** <p>
	 * Constructs a new PolygonSpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards,
	 * x-axis point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect
	 * with respect to the screen resolution.
	 * </p>
	 * 
	 * <p>
	 * The size parameter specifies the maximum size of a single batch in number of vertices(!)
	 * </p>
	 * 
	 * <p>
	 * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
	 * the ones expect for shaders set with {@link #setShader(ShaderProgram)}. See the {@link #createDefaultShader()} method.
	 * </p>
	 * 
	 * @param size the batch size in number of vertices(!)
	 * @param defaultShader the default shader to use. This is not owned by the SpriteBatch and must be disposed separately. */
	public PolygonSpriteBatch (int size, ShaderProgram defaultShader) {
		this(size, 1, defaultShader);
	}

	/** Constructs a PolygonSpriteBatch with the specified size and number of buffers and (if GL2) the default shader. See
	 * {@link #PolygonSpriteBatch(int, int, ShaderProgram)}. */
	public PolygonSpriteBatch (int size, int buffers) {
		this(size, buffers, null);
	}

	/** <p>
	 * Constructs a new PolygonSpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards,
	 * x-axis point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect
	 * with respect to the screen resolution.
	 * </p>
	 * 
	 * <p>
	 * The size parameter specifies the maximum size of a single batch in number of vertices(!)
	 * </p>
	 * 
	 * <p>
	 * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
	 * the ones expect for shaders set with {@link #setShader(ShaderProgram)}. See the {@link #createDefaultShader()} method.
	 * </p>
	 * 
	 * @param size the batch size in number of vertices(!)
	 * @param buffers the number of buffers to use. only makes sense with VBOs. This is an expert function.
	 * @param defaultShader the default shader to use. This is not owned by the SpriteBatch and must be disposed separately. */
	public PolygonSpriteBatch (int size, int buffers, ShaderProgram defaultShader) {
		this.buffers = new Mesh[buffers];

		for (int i = 0; i < buffers; i++) {
			this.buffers[i] = new Mesh(VertexDataType.VertexArray, false, size, 0, new VertexAttribute(Usage.Position, 2,
				ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
				new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
		}

		projectionMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		vertices = new float[size * Sprite.VERTEX_SIZE];

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
			+ "uniform mat4 u_projectionViewMatrix;\n" //
			+ "varying vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "   gl_Position =  u_projectionViewMatrix * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
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
		if (drawing) throw new IllegalStateException("you have to call PolygonSpriteBatch.end() first");
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
		if (!drawing) throw new IllegalStateException("PolygonSpriteBatch.begin must be called before end.");
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
		draw(region, x, y, region.getRegion().getRegionWidth(), region.getRegion().getRegionHeight());
	}

	/** Draws a polygon region with the bottom left corner at x,y and stretching the region to cover the given width and height. */
	public void draw (PolygonRegion region, float x, float y, float width, float height) {
		if (!drawing) throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");

		Texture texture = region.getRegion().texture;
		if (texture != lastTexture) {
			switchTexture(texture);
		}

		float[] localVertices = region.getLocalVertices();
		float[] texCoords = region.getTextureCoords();

		if (idx + localVertices.length > vertices.length) renderMesh();

		float sX = width / region.getRegion().getRegionWidth();
		float sY = height / region.getRegion().getRegionHeight();

		for (int i = 0; i < localVertices.length; i += 2) {
			vertices[idx++] = ((localVertices[i]) * sX) + x;
			vertices[idx++] = ((localVertices[i + 1]) * sY) + y;
			vertices[idx++] = color;
			vertices[idx++] = texCoords[i];
			vertices[idx++] = texCoords[i + 1];
		}
	}

	/** Draws the polygon region with the bottom left corner at x,y and stretching the region to cover the given width and height.
	 * The polygon region is offset by originX, originY relative to the origin. Scale specifies the scaling factor by which the
	 * polygon region should be scaled around originX, originY. Rotation specifies the angle of counter clockwise rotation of the
	 * rectangle around originX, originY. */
	public void draw (PolygonRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation) {
		if (!drawing) throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");

		Texture texture = region.getRegion().texture;
		if (texture != lastTexture) {
			switchTexture(texture);
		}

		float[] localVertices = region.getLocalVertices();
		float[] texCoords = region.getTextureCoords();

		if (idx + localVertices.length > vertices.length) renderMesh();

		final float worldOriginX = x + originX;
		final float worldOriginY = y + originY;
		float sX = width / region.getRegion().getRegionWidth();
		float sY = height / region.getRegion().getRegionHeight();
		float fx, rx;
		float fy, ry;

		final float cos = MathUtils.cosDeg(rotation);
		final float sin = MathUtils.sinDeg(rotation);

		for (int i = 0; i < localVertices.length; i += 2) {
			fx = localVertices[i] * sX;
			fy = localVertices[i + 1] * sY;

			fx -= originX;
			fy -= originY;

			if (scaleX != 1 || scaleY != 1) {
				fx *= scaleX;
				fy *= scaleY;
			}

			rx = cos * fx - sin * fy;
			ry = sin * fx + cos * fy;

			rx += worldOriginX;
			ry += worldOriginY;

			vertices[idx++] = rx;
			vertices[idx++] = ry;
			vertices[idx++] = color;
			vertices[idx++] = texCoords[i];
			vertices[idx++] = texCoords[i + 1];
		}
	}

	/** Draws the polygon region using the given vertices. Each vertices must be made up of 5 elements in this order: x, y, color,
	 * u, v. */
	public void draw (PolygonRegion region, float[] spriteVertices, int offset, int length) {
		if (!drawing) throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");

		Texture texture = region.getRegion().texture;
		if (texture != lastTexture) {
			switchTexture(texture);
		}

		if (idx + length > vertices.length) renderMesh();

		if (length <= vertices.length) {
			System.arraycopy(spriteVertices, offset, vertices, idx, length);
			idx += length;
		} else {
			// ay captain, need to split it across multiple batches. Who sends humangous streams like this?!
		}
	}

	/** Causes any pending sprites to be rendered, without ending the PolygonSpriteBatch. */
	public void flush () {
		renderMesh();
	}

	private void renderMesh () {
		if (idx == 0) return;

		renderCalls++;
		totalRenderCalls++;
		int verticesInBatch = idx / Sprite.VERTEX_SIZE;
		if (verticesInBatch > maxVerticesInBatch) maxVerticesInBatch = verticesInBatch;

		lastTexture.bind();
		mesh.setVertices(vertices, 0, idx);

		if (blendingDisabled) {
			Gdx.gl.glDisable(GL20.GL_BLEND);
		} else {
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(blendSrcFunc, blendDstFunc);
		}

		if (Gdx.graphics.isGL20Available()) {
			if (customShader != null)
				mesh.render(customShader, GL10.GL_TRIANGLES, 0, verticesInBatch);
			else
				mesh.render(shader, GL10.GL_TRIANGLES, 0, verticesInBatch);
		} else {
			mesh.render(GL10.GL_TRIANGLES, 0, verticesInBatch);
		}

		idx = 0;
		currBufferIdx++;
		if (currBufferIdx == buffers.length) currBufferIdx = 0;
		mesh = buffers[currBufferIdx];
	}

	/** Disables blending for drawing sprites. Does not disable blending for text rendering */
	public void disableBlending () {
		renderMesh();
		blendingDisabled = true;
	}

	/** Enables blending for sprites */
	public void enableBlending () {
		renderMesh();
		blendingDisabled = false;
	}

	/** Sets the blending function to be used when rendering sprites.
	 * 
	 * @param srcFunc the source function, e.g. GL11.GL_SRC_ALPHA
	 * @param dstFunc the destination function, e.g. GL11.GL_ONE_MINUS_SRC_ALPHA */
	public void setBlendFunction (int srcFunc, int dstFunc) {
		renderMesh();
		blendSrcFunc = srcFunc;
		blendDstFunc = dstFunc;
	}

	/** Disposes all resources associated with this SpriteBatch */
	public void dispose () {
		for (int i = 0; i < buffers.length; i++)
			buffers[i].dispose();
		if (ownsShader && shader != null) shader.dispose();
	}

	/** Returns the current projection matrix. Changing this will result in undefined behaviour.
	 * 
	 * @return the currently set projection matrix */
	public Matrix4 getProjectionMatrix () {
		return projectionMatrix;
	}

	/** Returns the current transform matrix. Changing this will result in undefined behaviour.
	 * 
	 * @return the currently set transform matrix */
	public Matrix4 getTransformMatrix () {
		return transformMatrix;
	}

	/** Sets the projection matrix to be used by this SpriteBatch. If this is called inside a {@link #begin()}/{@link #end()} block.
	 * the current batch is flushed to the gpu.
	 * 
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
				customShader.setUniformMatrix("u_proj", projectionMatrix);
				customShader.setUniformMatrix("u_trans", transformMatrix);
				customShader.setUniformMatrix("u_projTrans", combinedMatrix);
				customShader.setUniformi("u_texture", 0);
			} else {
				shader.setUniformMatrix("u_projectionViewMatrix", combinedMatrix);
				shader.setUniformi("u_texture", 0);
			}
		}
	}

	private void switchTexture (Texture texture) {
		renderMesh();
		lastTexture = texture;
	}

	/** Sets the shader to be used in a GLES 2.0 environment. Vertex position attribute is called "a_position", the texture
	 * coordinates attribute is called called "a_texCoords0", the color attribute is called "a_color". See
	 * {@link ShaderProgram#POSITION_ATTRIBUTE}, {@link ShaderProgram#COLOR_ATTRIBUTE} and {@link ShaderProgram#TEXCOORD_ATTRIBUTE}
	 * which gets "0" appened to indicate the use of the first texture unit. The projection matrix is uploaded via a mat4 uniform
	 * called "u_proj", the transform matrix is uploaded via a uniform called "u_trans", the combined transform and projection
	 * matrx is is uploaded via a mat4 uniform called "u_projTrans". The texture sampler is passed via a uniform called
	 * "u_texture".</p>
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
}
