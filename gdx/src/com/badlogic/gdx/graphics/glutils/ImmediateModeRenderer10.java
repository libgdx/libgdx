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

package com.badlogic.gdx.graphics.glutils;

import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** An ImmediateModeRenderer allows you to perform immediate mode rendering as you were accustomed to in your desktop OpenGL
 * environment. In order to draw something you first have to call {@link ImmediateModeRenderer10#begin(int)} with the primitive
 * type you want to render. Next you specify as many vertices as you want by first defining the vertex color, normal and texture
 * coordinates followed by the vertex position which finalizes the definition of a single vertex. When you are done specifying the
 * geometry you have to call {@link ImmediateModeRenderer10#end()} to make the renderer render the geometry. Internally the
 * renderer uses vertex arrays to render the provided geometry. This is not the best performing way to do this so use this class
 * only for non performance critical low vertex count geometries while debugging.
 * 
 * Note that this class of course only works with OpenGL ES 1.x.
 * 
 * @author mzechner */
public class ImmediateModeRenderer10 implements ImmediateModeRenderer {
	/** the primitive type **/
	private int primitiveType;

	/** the vertex position array and buffer **/
	private float[] positions;
	private FloatBuffer positionsBuffer;

	/** the vertex color array and buffer **/
	private float[] colors;
	private FloatBuffer colorsBuffer;

	/** the vertex normal array and buffer **/
	private float[] normals;
	private FloatBuffer normalsBuffer;

	/** the texture coordinate array and buffer **/
	private float[] texCoords;
	private FloatBuffer texCoordsBuffer;

	/** the current vertex attribute indices **/
	private int idxPos = 0;
	private int idxCols = 0;
	private int idxNors = 0;
	private int idxTexCoords = 0;

	private boolean hasCols;
	private boolean hasNors;
	private boolean hasTexCoords;

	private final int maxVertices;
	private int numVertices;

	/** Constructs a new ImmediateModeRenderer */
	public ImmediateModeRenderer10 () {
		this(2000);
	}

	/** Constructs a new ImmediateModeRenderer */
	public ImmediateModeRenderer10 (int maxVertices) {
		this.maxVertices = maxVertices;
		if (Gdx.graphics.isGL20Available())
			throw new GdxRuntimeException("ImmediateModeRenderer can only be used with OpenGL ES 1.0/1.1");

		this.positions = new float[3 * maxVertices];
		this.positionsBuffer = BufferUtils.newFloatBuffer(3 * maxVertices);
		this.colors = new float[4 * maxVertices];
		this.colorsBuffer = BufferUtils.newFloatBuffer(4 * maxVertices);
		this.normals = new float[3 * maxVertices];
		this.normalsBuffer = BufferUtils.newFloatBuffer(3 * maxVertices);
		this.texCoords = new float[2 * maxVertices];
		this.texCoordsBuffer = BufferUtils.newFloatBuffer(2 * maxVertices);
	}

	public void begin (Matrix4 projModelView, int primitiveType) {
		GL10 gl = Gdx.gl10;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadMatrixf(projModelView.val, 0);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		begin(primitiveType);
	}

	/** Starts a new list of primitives. The primitiveType specifies which primitives to draw. Can be any of GL10.GL_TRIANGLES,
	 * GL10.GL_LINES and so on. A maximum of 6000 vertices can be drawn at once.
	 * 
	 * @param primitiveType the primitive type. */
	public void begin (int primitiveType) {
		this.primitiveType = primitiveType;
		numVertices = 0;
		idxPos = 0;
		idxCols = 0;
		idxNors = 0;
		idxTexCoords = 0;
		hasCols = false;
		hasNors = false;
		hasTexCoords = false;
	}

	/** Specifies the color of the current vertex
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 * @param a the alpha component */
	public void color (float r, float g, float b, float a) {
		colors[idxCols] = r;
		colors[idxCols + 1] = g;
		colors[idxCols + 2] = b;
		colors[idxCols + 3] = a;
		hasCols = true;
	}

	/** Specifies the normal of the current vertex
	 * @param x the x component
	 * @param y the y component
	 * @param z the z component */
	public void normal (float x, float y, float z) {
		normals[idxNors] = x;
		normals[idxNors + 1] = y;
		normals[idxNors + 2] = z;
		hasNors = true;
	}

	/** Specifies the texture coordinates of the current vertex
	 * @param u the u coordinate
	 * @param v the v coordinate */
	public void texCoord (float u, float v) {
		texCoords[idxTexCoords] = u;
		texCoords[idxTexCoords + 1] = v;
		hasTexCoords = true;
	}

	/** Specifies the position of the current vertex and finalizes it. After a call to this method you will effectively define a new
	 * vertex afterwards.
	 * 
	 * @param x the x component
	 * @param y the y component
	 * @param z the z component */
	public void vertex (float x, float y, float z) {
		positions[idxPos++] = x;
		positions[idxPos++] = y;
		positions[idxPos++] = z;

		if (hasCols) idxCols += 4;
		if (hasNors) idxNors += 3;
		if (hasTexCoords) idxTexCoords += 2;
		numVertices++;
	}

	public int getNumVertices () {
		return numVertices;
	}

	public int getMaxVertices () {
		return maxVertices;
	}

	/** Renders the primitives just defined. */
	public void end () {
		if (idxPos == 0) return;

		GL10 gl = Gdx.gl10;
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		positionsBuffer.clear();
		BufferUtils.copy(positions, positionsBuffer, idxPos, 0);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, positionsBuffer);

		if (hasCols) {
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
			colorsBuffer.clear();
			BufferUtils.copy(colors, colorsBuffer, idxCols, 0);
			gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorsBuffer);
		}

		if (hasNors) {
			gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
			normalsBuffer.clear();
			BufferUtils.copy(normals, normalsBuffer, idxNors, 0);
			gl.glNormalPointer(GL10.GL_FLOAT, 0, normalsBuffer);
		}

		if (hasTexCoords) {
			gl.glClientActiveTexture(GL10.GL_TEXTURE0);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			texCoordsBuffer.clear();
			BufferUtils.copy(texCoords, texCoordsBuffer, idxTexCoords, 0);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texCoordsBuffer);
		}

		gl.glDrawArrays(primitiveType, 0, idxPos / 3);

		if (hasCols) gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		if (hasNors) gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		if (hasTexCoords) gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}

	public void vertex (Vector3 point) {
		vertex(point.x, point.y, point.z);
	}

	@Override
	public void dispose () {
	}
}
