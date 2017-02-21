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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.IndexBufferObjectSubData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO;
import com.badlogic.gdx.graphics.glutils.VertexData;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StringBuilder;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class VBOWithVAOPerformanceTest extends GdxTest {

	ShaderProgram shader;
	Texture texture;
	Matrix4 matrix = new Matrix4();

	Mesh oldVBOWithVAOMesh;
	Mesh newVBOWithVAOMesh;

	SpriteBatch batch;
	BitmapFont bitmapFont;
	StringBuilder stringBuilder;

	WindowedMean newCounter = new WindowedMean(100);
	WindowedMean oldCounter = new WindowedMean(100);

	WindowedMean newCounterStress = new WindowedMean(100);
	WindowedMean oldCounterStress = new WindowedMean(100);

	@Override
	public void create () {
		if (Gdx.gl30 == null) {
			throw new GdxRuntimeException("GLES 3.0 profile required for this test");
		}
		String vertexShader = "attribute vec4 a_position;    \n" + "attribute vec4 a_color;\n" + "attribute vec2 a_texCoord0;\n"
			+ "uniform mat4 u_worldView;\n" + "varying vec4 v_color;" + "varying vec2 v_texCoords;"
			+ "void main()                  \n" + "{                            \n" + "   v_color = a_color; \n"
			+ "   v_texCoords = a_texCoord0; \n" + "   gl_Position =  u_worldView * a_position;  \n"
			+ "}                            \n";
		String fragmentShader = "#ifdef GL_ES\n" + "precision mediump float;\n" + "#endif\n" + "varying vec4 v_color;\n"
			+ "varying vec2 v_texCoords;\n" + "uniform sampler2D u_texture;\n" + "void main()                                  \n"
			+ "{                                            \n" + "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n"
			+ "}";

		shader = new ShaderProgram(vertexShader, fragmentShader);
		if (shader.isCompiled() == false) {
			Gdx.app.log("ShaderTest", shader.getLog());
			Gdx.app.exit();
		}

		int numSprites = 1000;
		int maxIndices =  numSprites * 6;
		int maxVertices = numSprites * 6;

		VertexAttribute[] vertexAttributes = new VertexAttribute[] {VertexAttribute.Position(), VertexAttribute.ColorUnpacked(), VertexAttribute.TexCoords(0)};

		VertexBufferObjectWithVAO newVBOWithVAO =  new VertexBufferObjectWithVAO(false, maxVertices, vertexAttributes);
		OldVertexBufferObjectWithVAO oldVBOWithVAO =  new OldVertexBufferObjectWithVAO(false, maxVertices, vertexAttributes);

		IndexBufferObjectSubData newIndices = new IndexBufferObjectSubData(false, maxIndices);
		IndexBufferObjectSubData oldIndices = new IndexBufferObjectSubData(false, maxIndices);

		newVBOWithVAOMesh = new Mesh(newVBOWithVAO, newIndices, false) {};
		oldVBOWithVAOMesh = new Mesh(oldVBOWithVAO, oldIndices, false) {};

		float[] vertexArray = new float[maxVertices * 9];
		int index = 0;
		int stride = 9 * 6;
		for (int i = 0; i < numSprites; i++) {
			addRandomSprite(vertexArray, index);
			index += stride;
		}
		short[] indexArray = new short[maxIndices];
		for (short i = 0; i < maxIndices; i++) {
			indexArray[i] = i;
		}

		newVBOWithVAOMesh.setVertices(vertexArray);
		newVBOWithVAOMesh.setIndices(indexArray);

		oldVBOWithVAOMesh.setVertices(vertexArray);
		oldVBOWithVAOMesh.setIndices(indexArray);

		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));

		batch = new SpriteBatch();
		bitmapFont = new BitmapFont();
		stringBuilder = new StringBuilder();
	}

	private void addRandomSprite (float[] vertArray, int currentIndex) {
		float width = MathUtils.random(0.05f, 0.2f);
		float height = MathUtils.random(0.05f, 0.2f);
		float x = MathUtils.random(-1f, 1f);
		float y = MathUtils.random(-1f, 1f);
		float r = MathUtils.random();
		float g = MathUtils.random();
		float b = MathUtils.random();
		float a = MathUtils.random();

		vertArray[currentIndex++] = x;
		vertArray[currentIndex++] = y;
		vertArray[currentIndex++] = 0;
		vertArray[currentIndex++] = r;
		vertArray[currentIndex++] = g;
		vertArray[currentIndex++] = b;
		vertArray[currentIndex++] = a;
		vertArray[currentIndex++] = 0;
		vertArray[currentIndex++] = 1;

		vertArray[currentIndex++] = x + width;
		vertArray[currentIndex++] = y;
		vertArray[currentIndex++] = 0;
		vertArray[currentIndex++] = r;
		vertArray[currentIndex++] = g;
		vertArray[currentIndex++] = b;
		vertArray[currentIndex++] = a;
		vertArray[currentIndex++] = 1;
		vertArray[currentIndex++] = 1;

		vertArray[currentIndex++] = x + width;
		vertArray[currentIndex++] = y + height;
		vertArray[currentIndex++] = 0;
		vertArray[currentIndex++] = r;
		vertArray[currentIndex++] = g;
		vertArray[currentIndex++] = b;
		vertArray[currentIndex++] = a;
		vertArray[currentIndex++] = 1;
		vertArray[currentIndex++] = 0;

		vertArray[currentIndex++] = x + width;
		vertArray[currentIndex++] = y + height;
		vertArray[currentIndex++] = 0;
		vertArray[currentIndex++] = r;
		vertArray[currentIndex++] = g;
		vertArray[currentIndex++] = b;
		vertArray[currentIndex++] = a;
		vertArray[currentIndex++] = 1;
		vertArray[currentIndex++] = 0;

		vertArray[currentIndex++] = x;
		vertArray[currentIndex++] = y + height;
		vertArray[currentIndex++] = 0;
		vertArray[currentIndex++] = r;
		vertArray[currentIndex++] = g;
		vertArray[currentIndex++] = b;
		vertArray[currentIndex++] = a;
		vertArray[currentIndex++] = 0;
		vertArray[currentIndex++] = 0;

		vertArray[currentIndex++] = x;
		vertArray[currentIndex++] = y;
		vertArray[currentIndex++] = 0;
		vertArray[currentIndex++] = r;
		vertArray[currentIndex++] = g;
		vertArray[currentIndex++] = b;
		vertArray[currentIndex++] = a;
		vertArray[currentIndex++] = 0;
		vertArray[currentIndex++] = 1;
	}

	@Override
	public void render () {
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl20.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);


		texture.bind();
		shader.begin();
		shader.setUniformMatrix("u_worldView", matrix);
		shader.setUniformi("u_texture", 0);

		long beforeOld = System.nanoTime();
		oldVBOWithVAOMesh.render(shader, GL20.GL_TRIANGLES);
		Gdx.gl.glFlush();
		oldCounter.addValue((System.nanoTime() - beforeOld));
		shader.end();

		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		texture.bind();
		shader.begin();
		shader.setUniformMatrix("u_worldView", matrix);
		shader.setUniformi("u_texture", 0);

		long beforeNew = System.nanoTime();
		newVBOWithVAOMesh.render(shader, GL20.GL_TRIANGLES);
		Gdx.gl.glFlush();
		newCounter.addValue((System.nanoTime() - beforeNew));
		shader.end();


		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		texture.bind();
		shader.begin();
		shader.setUniformMatrix("u_worldView", matrix);
		shader.setUniformi("u_texture", 0);

		long beforeOldStress = System.nanoTime();
		for (int i = 0; i < 100; i++)
			oldVBOWithVAOMesh.render(shader, GL20.GL_TRIANGLES);
		Gdx.gl.glFlush();
		oldCounterStress.addValue((System.nanoTime() - beforeOldStress));
		shader.end();


		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		texture.bind();
		shader.begin();
		shader.setUniformMatrix("u_worldView", matrix);
		shader.setUniformi("u_texture", 0);

		long beforeNewStress = System.nanoTime();
		for (int i = 0; i < 100; i++)
			newVBOWithVAOMesh.render(shader, GL20.GL_TRIANGLES);
		Gdx.gl.glFlush();
		newCounterStress.addValue((System.nanoTime() - beforeNewStress));
		shader.end();



		batch.begin();
		stringBuilder.setLength(0);
		stringBuilder.append("O Mean Time: ");
		stringBuilder.append(oldCounter.getMean());
		bitmapFont.draw(batch, stringBuilder, 0, 200);
		stringBuilder.setLength(0);
		stringBuilder.append("N Mean Time: ");
		stringBuilder.append(newCounter.getMean());
		bitmapFont.draw(batch, stringBuilder, 0, 200 - 20);

		float oldMean = oldCounter.getMean();
		float newMean = newCounter.getMean();

		float meanedAverage = newMean/oldMean;
		stringBuilder.setLength(0);
		stringBuilder.append("New VBO time as a percentage of Old Time: ");
		stringBuilder.append(meanedAverage);
		bitmapFont.draw(batch, stringBuilder, 0, 200 - 40);

		stringBuilder.setLength(0);
		stringBuilder.append("Stress: O Mean Time: ");
		stringBuilder.append(oldCounterStress.getMean());
		bitmapFont.draw(batch, stringBuilder, 0, 200 - 80);
		stringBuilder.setLength(0);
		stringBuilder.append("Stress: N Mean Time: ");
		stringBuilder.append(newCounterStress.getMean());
		bitmapFont.draw(batch, stringBuilder, 0, 200 - 100);

		float oldMeanStress = oldCounterStress.getMean();
		float newMeanStress = newCounterStress.getMean();

		float meanedStressAverage = newMeanStress/oldMeanStress;
		stringBuilder.setLength(0);
		stringBuilder.append("Stress: New VBO time as a percentage of Old Time: ");
		stringBuilder.append(meanedStressAverage);
		bitmapFont.draw(batch, stringBuilder, 0, 200 - 120);


		batch.end();
	}

	@Override
	public void dispose () {
		oldVBOWithVAOMesh.dispose();
		newVBOWithVAOMesh.dispose();
		texture.dispose();
		shader.dispose();
	}

	private static class OldVertexBufferObjectWithVAO implements VertexData {
		final static IntBuffer tmpHandle = BufferUtils.newIntBuffer(1);

		final VertexAttributes attributes;
		final FloatBuffer buffer;
		final ByteBuffer byteBuffer;
		int bufferHandle;
		final boolean isStatic;
		final int usage;
		boolean isDirty = false;
		boolean isBound = false;
		boolean vaoDirty = true;
		int vaoHandle = -1;

		public OldVertexBufferObjectWithVAO (boolean isStatic, int numVertices, VertexAttribute... attributes) {
			this(isStatic, numVertices, new VertexAttributes(attributes));
		}

		public OldVertexBufferObjectWithVAO (boolean isStatic, int numVertices, VertexAttributes attributes) {
			this.isStatic = isStatic;
			this.attributes = attributes;

			byteBuffer = BufferUtils.newUnsafeByteBuffer(this.attributes.vertexSize * numVertices);
			buffer = byteBuffer.asFloatBuffer();
			buffer.flip();
			byteBuffer.flip();
			bufferHandle = Gdx.gl20.glGenBuffer();
			usage = isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
		}

		@Override
		public VertexAttributes getAttributes() {
			return attributes;
		}

		@Override
		public int getNumVertices() {
			return buffer.limit() * 4 / attributes.vertexSize;
		}

		@Override
		public int getNumMaxVertices() {
			return byteBuffer.capacity() / attributes.vertexSize;
		}

		@Override
		public FloatBuffer getBuffer() {
			isDirty = true;
			return buffer;
		}

		private void bufferChanged() {
			if (isBound) {
				Gdx.gl20.glBufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
				isDirty = false;
			}
		}

		@Override
		public void setVertices(float[] vertices, int offset, int count) {
			isDirty = true;
			BufferUtils.copy(vertices, byteBuffer, count, offset);
			buffer.position(0);
			buffer.limit(count);
			bufferChanged();
		}

		@Override
		public void updateVertices(int targetOffset, float[] vertices, int sourceOffset, int count) {
			isDirty = true;
			final int pos = byteBuffer.position();
			byteBuffer.position(targetOffset * 4);
			BufferUtils.copy(vertices, sourceOffset, count, byteBuffer);
			byteBuffer.position(pos);
			buffer.position(0);
			bufferChanged();
		}

		@Override
		public void bind(ShaderProgram shader) {
			bind(shader, null);
		}

		@Override
		public void bind(ShaderProgram shader, int[] locations) {
			GL30 gl = Gdx.gl30;
			if (vaoDirty || !gl.glIsVertexArray(vaoHandle)) {
				//initialize the VAO with our vertex attributes and buffer:
				tmpHandle.clear();
				gl.glGenVertexArrays(1, tmpHandle);
				vaoHandle = tmpHandle.get(0);
				gl.glBindVertexArray(vaoHandle);
				vaoDirty = false;

			} else {
				//else simply bind the VAO.
				gl.glBindVertexArray(vaoHandle);
			}

			bindAttributes(shader, locations);

			//if our data has changed upload it:
			bindData(gl);

			isBound = true;
		}

		private void bindAttributes(ShaderProgram shader, int[] locations) {
			final GL20 gl = Gdx.gl20;
			gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandle);
			final int numAttributes = attributes.size();
			if (locations == null) {
				for (int i = 0; i < numAttributes; i++) {
					final VertexAttribute attribute = attributes.get(i);
					final int location = shader.getAttributeLocation(attribute.alias);
					if (location < 0) continue;
					shader.enableVertexAttribute(location);

					shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized, attributes.vertexSize,
						attribute.offset);
				}

			} else {
				for (int i = 0; i < numAttributes; i++) {
					final VertexAttribute attribute = attributes.get(i);
					final int location = locations[i];
					if (location < 0) continue;
					shader.enableVertexAttribute(location);

					shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized, attributes.vertexSize,
						attribute.offset);
				}
			}
		}

		private void bindData(GL20 gl) {
			if (isDirty) {
				gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandle);
				byteBuffer.limit(buffer.limit() * 4);
				gl.glBufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
				isDirty = false;
			}
		}

		@Override
		public void unbind(final ShaderProgram shader) {
			unbind(shader, null);
		}

		@Override
		public void unbind(final ShaderProgram shader, final int[] locations) {
			GL30 gl = Gdx.gl30;
			gl.glBindVertexArray(0);
			isBound = false;
		}

		@Override
		public void invalidate() {
			bufferHandle = Gdx.gl20.glGenBuffer();
			isDirty = true;
			vaoDirty = true;
		}

		@Override
		public void dispose() {
			GL30 gl = Gdx.gl30;

			gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
			gl.glDeleteBuffer(bufferHandle);
			bufferHandle = 0;
			BufferUtils.disposeUnsafeByteBuffer(byteBuffer);

			if (gl.glIsVertexArray(vaoHandle)) {
				tmpHandle.clear();
				tmpHandle.put(vaoHandle);
				tmpHandle.flip();
				gl.glDeleteVertexArrays(1, tmpHandle);
			}
		}
	}
}
