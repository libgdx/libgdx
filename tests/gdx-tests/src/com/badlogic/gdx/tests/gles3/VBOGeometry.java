
package com.badlogic.gdx.tests.gles3;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.BufferUtils;

public class VBOGeometry {

	GenericAttributes atts;
	int vertexBuffer;
	int elementBuffer;

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
		Gdx.gl20.glDrawElements(GL30.GL_TRIANGLES, 3, GL30.GL_UNSIGNED_SHORT, 0);
	}

	public void drawInstances (int numInstances) {
		Gdx.gl30.glDrawElementsInstanced(GL30.GL_TRIANGLES, 3, GL30.GL_UNSIGNED_SHORT, 0, numInstances);
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

		return geom;
	}

}
