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

package com.badlogic.gdx.tests.gles3;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;

public class VBOGeometry implements Disposable {

	GenericAttributes atts;
	int vertexBuffer;
	int elementBuffer;
	int elementCount;

	private VBOGeometry (GL20 gl) {
		IntBuffer ib = BufferUtils.newIntBuffer(2);
		gl.glGenBuffers(2, ib);
		vertexBuffer = ib.get(0);
		elementBuffer = ib.get(1);
	}

	public void bind () {
		Gdx.gl20.glBindBuffer(GL30.GL_ARRAY_BUFFER, vertexBuffer);
		atts.bindAttributes();
		Gdx.gl20.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, elementBuffer);
	}

	public void draw () {
		Gdx.gl20.glDrawElements(GL30.GL_TRIANGLES, elementCount, GL30.GL_UNSIGNED_SHORT, 0);
	}

	public void drawInstances (int numInstances) {
		Gdx.gl30.glDrawElementsInstanced(GL30.GL_TRIANGLES, elementCount, GL30.GL_UNSIGNED_SHORT, 0, numInstances);
	}

	public void dispose () {
		IntBuffer ib = BufferUtils.newIntBuffer(2);
		ib.put(vertexBuffer);
		ib.put(elementBuffer);
		ib.position(0);
		Gdx.gl20.glDeleteBuffers(2, ib);
	}

	/** create a triangle VBO with POSITION attribute */
	public static VBOGeometry triangleV () {
		GL20 gl = Gdx.gl20;

		VBOGeometry geom = new VBOGeometry(gl);

		geom.atts = new GenericAttributes(GenericAttributes.POSITION);

		FloatBuffer vertexData = BufferUtils.newFloatBuffer(9).put(
			new float[] {0.0f, 0.5f, 0.0f, -0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f});
		vertexData.position(0);

		ShortBuffer elementData = BufferUtils.newShortBuffer(3).put(new short[] {0, 1, 2});
		elementData.position(0);

		//
		gl.glBindBuffer(GL30.GL_ARRAY_BUFFER, geom.vertexBuffer);
		gl.glBufferData(GL30.GL_ARRAY_BUFFER, vertexData.capacity() * 4, vertexData, GL30.GL_STATIC_DRAW);

		//
		gl.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, geom.elementBuffer);
		gl.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, elementData.capacity() * 4, elementData, GL30.GL_STATIC_DRAW);

		geom.elementCount = elementData.capacity();

		return geom;
	}

	/** create a quad that fills the screen when not transformed */
	public static VBOGeometry fsQuadV () {
		GL20 gl = Gdx.gl20;

		VBOGeometry geom = new VBOGeometry(gl);

		geom.atts = new GenericAttributes(GenericAttributes.POSITION);

		FloatBuffer vertexData = BufferUtils.newFloatBuffer(12).put(new float[] {1, 1, 0, -1, 1, 0, -1, -1, 0, 1, -1, 0});
		vertexData.position(0);

		ShortBuffer elementData = BufferUtils.newShortBuffer(6).put(new short[] {0, 1, 2, 0, 3, 2});
		elementData.position(0);

		//
		gl.glBindBuffer(GL30.GL_ARRAY_BUFFER, geom.vertexBuffer);
		gl.glBufferData(GL30.GL_ARRAY_BUFFER, vertexData.capacity() * 4, vertexData, GL30.GL_STATIC_DRAW);

		//
		gl.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, geom.elementBuffer);
		gl.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, elementData.capacity() * 4, elementData, GL30.GL_STATIC_DRAW);

		geom.elementCount = elementData.capacity();

		return geom;
	}

	/** create a quad moderately sized quad */
	public static VBOGeometry quadV () {
		GL20 gl = Gdx.gl20;

		VBOGeometry geom = new VBOGeometry(gl);

		geom.atts = new GenericAttributes(GenericAttributes.POSITION);

		FloatBuffer vertexData = BufferUtils.newFloatBuffer(12).put(
			new float[] {0.2f, 0.2f, 0, -0.2f, 0.2f, 0, -0.2f, -0.2f, 0, 0.2f, -0.2f, 0});
		vertexData.position(0);

		ShortBuffer elementData = BufferUtils.newShortBuffer(6).put(new short[] {0, 1, 2, 0, 3, 2});
		elementData.position(0);

		//
		gl.glBindBuffer(GL30.GL_ARRAY_BUFFER, geom.vertexBuffer);
		gl.glBufferData(GL30.GL_ARRAY_BUFFER, vertexData.capacity() * 4, vertexData, GL30.GL_STATIC_DRAW);

		//
		gl.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, geom.elementBuffer);
		gl.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, elementData.capacity() * 4, elementData, GL30.GL_STATIC_DRAW);

		geom.elementCount = elementData.capacity();

		return geom;
	}

	/** create a quad that fills the screen when not transformed */
	public static VBOGeometry tinyQuadV () {
		GL20 gl = Gdx.gl20;

		VBOGeometry geom = new VBOGeometry(gl);

		geom.atts = new GenericAttributes(GenericAttributes.POSITION);

		FloatBuffer vertexData = BufferUtils.newFloatBuffer(12).put(
			new float[] {0.05f, 0.05f, 0, -0.05f, 0.05f, 0, -0.05f, -0.05f, 0, 0.05f, -0.05f, 0});
		vertexData.position(0);

		ShortBuffer elementData = BufferUtils.newShortBuffer(6).put(new short[] {0, 1, 2, 0, 3, 2});
		elementData.position(0);

		//
		gl.glBindBuffer(GL30.GL_ARRAY_BUFFER, geom.vertexBuffer);
		gl.glBufferData(GL30.GL_ARRAY_BUFFER, vertexData.capacity() * 4, vertexData, GL30.GL_STATIC_DRAW);

		//
		gl.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, geom.elementBuffer);
		gl.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, elementData.capacity() * 4, elementData, GL30.GL_STATIC_DRAW);

		geom.elementCount = elementData.capacity();

		return geom;
	}

	/** create a smaller triangle VBO with POSITION attribute */
	public static VBOGeometry tinyTriangleV () {
		GL20 gl = Gdx.gl20;

		VBOGeometry geom = new VBOGeometry(gl);

		geom.atts = new GenericAttributes(GenericAttributes.POSITION);

		FloatBuffer vertexData = BufferUtils.newFloatBuffer(9).put(
			new float[] {0.0f, 0.05f, 0.0f, -0.05f, -0.05f, 0.0f, 0.05f, -0.05f, 0.0f});
		vertexData.position(0);

		ShortBuffer elementData = BufferUtils.newShortBuffer(3).put(new short[] {0, 1, 2});
		elementData.position(0);

		//
		gl.glBindBuffer(GL30.GL_ARRAY_BUFFER, geom.vertexBuffer);
		gl.glBufferData(GL30.GL_ARRAY_BUFFER, vertexData.capacity() * 4, vertexData, GL30.GL_STATIC_DRAW);

		//
		gl.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, geom.elementBuffer);
		gl.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, elementData.capacity() * 4, elementData, GL30.GL_STATIC_DRAW);

		geom.elementCount = elementData.capacity();

		return geom;
	}

}
