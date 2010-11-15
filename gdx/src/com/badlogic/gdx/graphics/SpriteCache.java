
package com.badlogic.gdx.graphics;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.MathUtils;

public class SpriteCache {
	static private final float[] tempVertices = new float[Sprite.SPRITE_SIZE];

	final Mesh mesh;
	boolean drawing = false;
	private final Matrix4 transformMatrix = new Matrix4();
	private final Matrix4 projectionMatrix = new Matrix4();

	private final Matrix4 combinedMatrix = new Matrix4();
	ShaderProgram shader;

	private CacheBuidler builder;

	public SpriteCache () {
		this(1000);
	}

	public SpriteCache (int size) {
		mesh = new Mesh(true, size * 4, size * 6, new VertexAttribute(Usage.Position, 2, "a_position"), new VertexAttribute(
			Usage.ColorPacked, 4, "a_color"), new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoords"));
		mesh.setAutoBind(false);

		short[] indices = new short[size * 6];
		int len = size * 6;
		short j = 0;
		for (int i = 0; i < len; i += 6, j += 4) {
			indices[i + 0] = (short)(j + 0);
			indices[i + 1] = (short)(j + 1);
			indices[i + 2] = (short)(j + 2);
			indices[i + 3] = (short)(j + 2);
			indices[i + 4] = (short)(j + 3);
			indices[i + 5] = (short)(j + 0);
		}
		mesh.setIndices(indices);

		projectionMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		if (Gdx.graphics.isGL20Available()) createShader();
	}

	private void createShader () {
		String vertexShader = "attribute vec4 a_position;\n" //
			+ "attribute vec4 a_color;\n" //
			+ "attribute vec2 a_texCoords;\n" //
			+ "uniform mat4 u_projectionViewMatrix;\n" //
			+ "varying vec4 v_color; \n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "   v_color = a_color; \n" //
			+ "   v_texCoords = a_texCoords; \n" //
			+ "   gl_Position =  u_projectionViewMatrix * a_position;\n" //
			+ "}";
		String fragmentShader = "precision mediump float;\n" //
			+ "varying vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "uniform sampler2D u_texture;\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" //
			+ "}";
		shader = new ShaderProgram(vertexShader, fragmentShader);
		if (shader.isCompiled() == false) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
	}

	public void beginCache () {
		if (builder != null) throw new IllegalStateException("endCache must be called before begin.");
		builder = new CacheBuidler(mesh.getNumVertices() / 2 * 6);
		mesh.getVerticesBuffer().compact();
	}

	public Cache endCache () {
		if (builder == null) throw new IllegalStateException("beginCache mustbe called before endCache.");
		Cache cache = builder.finish();
		builder = null;
		mesh.getVerticesBuffer().flip();
		return cache;
	}

	public void add (Texture texture, float[] vertices, int offset, int length) {
		if (builder == null) throw new IllegalStateException("beginCache mustbe called before add.");
		builder.add(texture, vertices.length / Sprite.SPRITE_SIZE);

		mesh.getVerticesBuffer().put(vertices);
	}

	public void add (Texture texture, float x, float y, int srcWidth, int srcHeight, float u, float v, float u2, float v2,
		float color) {
		final float fx2 = x + srcWidth;
		final float fy2 = y + srcHeight;

		tempVertices[0] = x;
		tempVertices[1] = y;
		tempVertices[2] = color;
		tempVertices[3] = u;
		tempVertices[4] = v;

		tempVertices[5] = x;
		tempVertices[6] = fy2;
		tempVertices[7] = color;
		tempVertices[8] = u;
		tempVertices[9] = v2;

		tempVertices[10] = fx2;
		tempVertices[11] = fy2;
		tempVertices[12] = color;
		tempVertices[13] = u2;
		tempVertices[14] = v2;

		tempVertices[15] = fx2;
		tempVertices[16] = y;
		tempVertices[17] = color;
		tempVertices[18] = u2;
		tempVertices[19] = v;

		add(texture, tempVertices, 0, 20);
	}

	public void add (Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight, Color tint) {
		float invTexWidth = 1.0f / texture.getWidth();
		float invTexHeight = 1.0f / texture.getHeight();
		final float u = srcX * invTexWidth;
		final float v = (srcY + srcHeight) * invTexHeight;
		final float u2 = (srcX + srcWidth) * invTexWidth;
		final float v2 = srcY * invTexHeight;
		final float fx2 = x + srcWidth;
		final float fy2 = y + srcHeight;

		final float color = tint.toFloatBits();

		tempVertices[0] = x;
		tempVertices[1] = y;
		tempVertices[2] = color;
		tempVertices[3] = u;
		tempVertices[4] = v;

		tempVertices[5] = x;
		tempVertices[6] = fy2;
		tempVertices[7] = color;
		tempVertices[8] = u;
		tempVertices[9] = v2;

		tempVertices[10] = fx2;
		tempVertices[11] = fy2;
		tempVertices[12] = color;
		tempVertices[13] = u2;
		tempVertices[14] = v2;

		tempVertices[15] = fx2;
		tempVertices[16] = y;
		tempVertices[17] = color;
		tempVertices[18] = u2;
		tempVertices[19] = v;

		add(texture, tempVertices, 0, 20);
	}

	public void add (Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
		int srcHeight, Color tint, boolean flipX, boolean flipY) {

		float invTexWidth = 1.0f / texture.getWidth();
		float invTexHeight = 1.0f / texture.getHeight();
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

		final float color = tint.toFloatBits();

		tempVertices[0] = x;
		tempVertices[1] = y;
		tempVertices[2] = color;
		tempVertices[3] = u;
		tempVertices[4] = v;

		tempVertices[5] = x;
		tempVertices[6] = fy2;
		tempVertices[7] = color;
		tempVertices[8] = u;
		tempVertices[9] = v2;

		tempVertices[10] = fx2;
		tempVertices[11] = fy2;
		tempVertices[12] = color;
		tempVertices[13] = u2;
		tempVertices[14] = v2;

		tempVertices[15] = fx2;
		tempVertices[16] = y;
		tempVertices[17] = color;
		tempVertices[18] = u2;
		tempVertices[19] = v;

		add(texture, tempVertices, 0, 20);
	}

	public void add (Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX,
		float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, Color tint, boolean flipX, boolean flipY) {

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

		float invTexWidth = 1.0f / texture.getWidth();
		float invTexHeight = 1.0f / texture.getHeight();
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

		final float color = tint.toFloatBits();

		tempVertices[0] = x1;
		tempVertices[1] = y1;
		tempVertices[2] = color;
		tempVertices[3] = u;
		tempVertices[4] = v;

		tempVertices[5] = x2;
		tempVertices[6] = y2;
		tempVertices[7] = color;
		tempVertices[8] = u;
		tempVertices[9] = v2;

		tempVertices[10] = x3;
		tempVertices[11] = y3;
		tempVertices[12] = color;
		tempVertices[13] = u2;
		tempVertices[14] = v2;

		tempVertices[15] = x4;
		tempVertices[16] = y4;
		tempVertices[17] = color;
		tempVertices[18] = u2;
		tempVertices[19] = v;

		add(texture, tempVertices, 0, 20);
	}

	public void add (Sprite sprite) {
		add(sprite.getTexture(), sprite.getVertices(), 0, 20);
	}

	public void begin () {
		if (drawing) throw new IllegalStateException("end must be called before begin.");

		if (Gdx.graphics.isGL20Available() == false) {
			GL10 gl = Gdx.gl10;
			gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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

			mesh.bind();
		} else {
			combinedMatrix.set(projectionMatrix).mul(transformMatrix);

			GL20 gl = Gdx.gl20;
			gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			gl.glDisable(GL20.GL_DEPTH_TEST);
			gl.glDisable(GL20.GL_CULL_FACE);
			gl.glDepthMask(false);

			gl.glEnable(GL20.GL_TEXTURE_2D);
			// gl.glActiveTexture( GL20.GL_TEXTURE0 );

			shader.begin();
			shader.setUniformMatrix("u_projectionViewMatrix", combinedMatrix);
			shader.setUniformi("u_texture", 0);

			mesh.bind(shader);
		}
		drawing = true;
	}

	public void end () {
		if (!drawing) throw new IllegalStateException("begin must be called before end.");
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
		mesh.unbind();
	}

	public void draw (Cache cache) {
		if (!drawing) throw new IllegalStateException("SpriteCache.begin must be called before draw.");

		int offset = cache.offset;
		Texture[] textures = cache.textures;
		int[] counts = cache.counts;
		if (Gdx.graphics.isGL20Available()) {
			for (int i = 0, n = textures.length; i < n; i++) {
				int count = counts[i];
				textures[i].bind();
				mesh.render(shader, GL10.GL_TRIANGLES, offset, count);
				offset += count;
			}
		} else {
			for (int i = 0, n = textures.length; i < n; i++) {
				int count = counts[i];
				textures[i].bind();
				mesh.render(GL10.GL_TRIANGLES, offset, count);
				offset += count;
			}
		}
	}

	public void dispose () {
		mesh.dispose();
		if (shader != null) shader.dispose();
	}

	public Matrix4 getProjectionMatrix () {
		return projectionMatrix;
	}

	public void setProjectionMatrix (Matrix4 projection) {
		if (drawing) throw new IllegalStateException("Can't set the matrix within begin/end.");
		projectionMatrix.set(projection);
	}

	public Matrix4 getTransformMatrix () {
		return transformMatrix;
	}

	public void setTransformMatrix (Matrix4 transform) {
		if (drawing) throw new IllegalStateException("Can't set the matrix within begin/end.");
		transformMatrix.set(transform);
	}

	private class CacheBuidler {
		private final ArrayList<Texture> textures = new ArrayList(8);
		private final ArrayList<Integer> counts = new ArrayList(8);
		private final int offset;

		CacheBuidler (int offset) {
			this.offset = offset;
		}

		void add (Texture texture, int count) {
			count *= 6;
			int lastIndex = textures.size() - 1;
			if (lastIndex < 0 || textures.get(lastIndex) != texture) {
				textures.add(texture);
				counts.add(count);
			} else
				counts.set(lastIndex, counts.get(lastIndex) + count);
		}

		Cache finish () {
			Cache cache = new Cache(offset, textures.toArray(new Texture[textures.size()]), new int[counts.size()]);
			for (int i = 0, n = counts.size(); i < n; i++)
				cache.counts[i] = counts.get(i);
			return cache;
		}
	}

	public class Cache {
		final int offset;
		final Texture[] textures;
		final int[] counts;

		Cache (int offset, Texture[] textures, int[] counts) {
			this.offset = offset;
			this.textures = textures;
			this.counts = counts;
		}
	}
}
