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

package com.badlogic.gdx.graphics;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.IndexArray;
import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.IndexBufferObjectSubData;
import com.badlogic.gdx.graphics.glutils.IndexData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexArray;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectSubData;
import com.badlogic.gdx.graphics.glutils.VertexData;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** <p>
 * A Mesh holds vertices composed of attributes specified by a {@link VertexAttributes} instance. The vertices are held either in
 * VRAM in form of vertex buffer objects or in RAM in form of vertex arrays. The former variant is more performant and is prefered
 * over vertex arrays if hardware supports it.
 * </p>
 * 
 * <p>
 * Meshes are automatically managed. If the OpenGL context is lost all vertex buffer objects get invalidated and must be reloaded
 * when the context is recreated. This only happens on Android when a user switches to another application or receives an incoming
 * call. A managed Mesh will be reloaded automagically so you don't have to do this manually.
 * </p>
 * 
 * <p>
 * A Mesh consists of vertices and optionally indices which specify which vertices define a triangle. Each vertex is composed of
 * attributes such as position, normal, color or texture coordinate. Note that not all of this attributes must be given, except
 * for position which is non-optional. Each attribute has an alias which is used when rendering a Mesh in OpenGL ES 2.0. The alias
 * is used to bind a specific vertex attribute to a shader attribute. The shader source and the alias of the attribute must match
 * exactly for this to work. For OpenGL ES 1.x rendering this aliases are irrelevant.
 * </p>
 * 
 * <p>
 * Meshes can be used with either OpenGL ES 1.x or OpenGL ES 2.0.
 * </p>
 * 
 * @author mzechner, Dave Clayton <contact@redskyforge.com> */
public class Mesh implements Disposable {
	public enum VertexDataType {
		VertexArray, VertexBufferObject, VertexBufferObjectSubData,
	}

	/** list of all meshes **/
	static final Map<Application, List<Mesh>> meshes = new HashMap<Application, List<Mesh>>();

	/** used for benchmarking **/
	public static boolean forceVBO = false;

	final VertexData vertices;
	final IndexData indices;
	boolean autoBind = true;
	final boolean isVertexArray;

	/** Creates a new Mesh with the given attributes.
	 * 
	 * @param isStatic whether this mesh is static or not. Allows for internal optimizations.
	 * @param maxVertices the maximum number of vertices this mesh can hold
	 * @param maxIndices the maximum number of indices this mesh can hold
	 * @param attributes the {@link VertexAttribute}s. Each vertex attribute defines one property of a vertex such as position,
	 *           normal or texture coordinate */
	public Mesh (boolean isStatic, int maxVertices, int maxIndices, VertexAttribute... attributes) {
		if (Gdx.gl20 != null || Gdx.gl11 != null || Mesh.forceVBO) {
			vertices = new VertexBufferObject(isStatic, maxVertices, attributes);
			indices = new IndexBufferObject(isStatic, maxIndices);
			isVertexArray = false;
		} else {
			vertices = new VertexArray(maxVertices, attributes);
			indices = new IndexArray(maxIndices);
			isVertexArray = true;
		}

		addManagedMesh(Gdx.app, this);
	}

	/** Creates a new Mesh with the given attributes.
	 * 
	 * @param isStatic whether this mesh is static or not. Allows for internal optimizations.
	 * @param maxVertices the maximum number of vertices this mesh can hold
	 * @param maxIndices the maximum number of indices this mesh can hold
	 * @param attributes the {@link VertexAttributes}. Each vertex attribute defines one property of a vertex such as position,
	 *           normal or texture coordinate */
	public Mesh (boolean isStatic, int maxVertices, int maxIndices, VertexAttributes attributes) {
		if (Gdx.gl20 != null || Gdx.gl11 != null || Mesh.forceVBO) {
			vertices = new VertexBufferObject(isStatic, maxVertices, attributes);
			indices = new IndexBufferObject(isStatic, maxIndices);
			isVertexArray = false;
		} else {
			vertices = new VertexArray(maxVertices, attributes);
			indices = new IndexArray(maxIndices);
			isVertexArray = true;
		}

		addManagedMesh(Gdx.app, this);
	}

	/** by jw:
	 * Creates a new Mesh with the given attributes. 
	 * Adds extra optimizations for dynamic (frequently modified) meshes.
	 * 
	 * @param staticVertices whether vertices of this mesh are static or not. Allows for internal optimizations.
	 * @param staticIndices whether indices of this mesh are static or not. Allows for internal optimizations.
	 * @param maxVertices the maximum number of vertices this mesh can hold
	 * @param maxIndices the maximum number of indices this mesh can hold
	 * @param attributes the {@link VertexAttributes}. Each vertex attribute defines one property of a vertex such as position,
	 *           normal or texture coordinate 
	 *           
	 * @author Jaroslaw Wisniewski <j.wisniewski@appsisle.com>           
	 **/
	public Mesh (boolean staticVertices, boolean staticIndices, int maxVertices, int maxIndices, VertexAttributes attributes) {
		if (Gdx.gl20 != null || Gdx.gl11 != null || Mesh.forceVBO) {
			
			// buffers do not update when initialized with ..ObjectSubData classes
			/*if (staticVertices) 
				vertices = new VertexBufferObject(staticVertices, maxVertices, attributes);
			else 
				vertices = new VertexBufferObjectSubData(staticVertices, maxVertices, attributes);	// when updating vertices - updates buffer instead recreating it

			if (staticIndices) 
				indices = new IndexBufferObject(staticIndices, maxIndices);
			else 
				indices = new IndexBufferObjectSubData(staticIndices, maxIndices);	// when updating indices - updates buffer instead recreating it
			*/
			
			vertices = new VertexBufferObject(staticVertices, maxVertices, attributes);
			indices = new IndexBufferObject(staticIndices, maxIndices);
			isVertexArray = false;
		} else {
			vertices = new VertexArray(maxVertices, attributes);
			indices = new IndexArray(maxIndices);
			isVertexArray = true;
		}

		addManagedMesh(Gdx.app, this);
	}
	
	/** Creates a new Mesh with the given attributes. This is an expert method with no error checking. Use at your own risk.
	 * 
	 * @param type the {@link VertexDataType} to be used, VBO or VA.
	 * @param isStatic whether this mesh is static or not. Allows for internal optimizations.
	 * @param maxVertices the maximum number of vertices this mesh can hold
	 * @param maxIndices the maximum number of indices this mesh can hold
	 * @param attributes the {@link VertexAttribute}s. Each vertex attribute defines one property of a vertex such as position,
	 *           normal or texture coordinate */
	public Mesh (VertexDataType type, boolean isStatic, int maxVertices, int maxIndices, VertexAttribute... attributes) {
// if (type == VertexDataType.VertexArray && Gdx.graphics.isGL20Available()) type = VertexDataType.VertexBufferObject;

		if (type == VertexDataType.VertexBufferObject || Mesh.forceVBO) {
			vertices = new VertexBufferObject(isStatic, maxVertices, attributes);
			indices = new IndexBufferObject(isStatic, maxIndices);
			isVertexArray = false;
		} else if (type == VertexDataType.VertexBufferObjectSubData) {
			vertices = new VertexBufferObjectSubData(isStatic, maxVertices, attributes);
			indices = new IndexBufferObjectSubData(isStatic, maxIndices);
			isVertexArray = false;
		} else {
			vertices = new VertexArray(maxVertices, attributes);
			indices = new IndexArray(maxIndices);
			isVertexArray = true;
		}
		addManagedMesh(Gdx.app, this);
	}
	
	/**
	 * Create a new Mesh that is a combination of transformations of the supplied base mesh.
	 * Not all primitive types, like line strip and triangle strip, can be combined.
	 * @param isStatic whether this mesh is static or not. Allows for internal optimizations.
	 * @param transformations the transformations to apply to the meshes 
	 * @return the combined mesh
	 */
	public static Mesh create(boolean isStatic, final Mesh base, final Matrix4[] transformations) {
		final VertexAttribute posAttr = base.getVertexAttribute(Usage.Position);
		final int offset = posAttr.offset / 4;
		final int numComponents = posAttr.numComponents;
		final int numVertices = base.getNumVertices();
		final int vertexSize = base.getVertexSize() / 4;
		final int baseSize = numVertices * vertexSize;
		final int numIndices = base.getNumIndices();
		
		final float vertices[] = new float[numVertices * vertexSize * transformations.length];
		final short indices[] = new short[numIndices * transformations.length];
		
		base.getIndices(indices);
		
		for (int i = 0; i < transformations.length; i++) {
			base.getVertices(0, baseSize, vertices, baseSize * i);
			transform(transformations[i], vertices, vertexSize, offset, numComponents, numVertices * i, numVertices);
			if (i > 0)
			for (int j = 0; j < numIndices; j++)
				indices[(numIndices * i) + j] = (short)(indices[j] + (numVertices * i)); 
		}
		
		final Mesh result = new Mesh(isStatic, vertices.length/vertexSize, indices.length, base.getVertexAttributes());
		result.setVertices(vertices);
		result.setIndices(indices);
		return result;
	}
	
	/**
	 * Create a new Mesh that is a combination of the supplied meshes. The meshes must have the same VertexAttributes signature.
	 * Not all primitive types, like line strip and triangle strip, can be combined.
	 * @param isStatic whether this mesh is static or not. Allows for internal optimizations.
	 * @param meshes the meshes to combine
	 * @return the combined mesh
	 */
	public static Mesh create(boolean isStatic, final Mesh[] meshes) {
		return create(isStatic, meshes, null);
	}

	/**
	 * Create a new Mesh that is a combination of the supplied meshes. The meshes must have the same VertexAttributes signature.
	 * If transformations is supplied, it must have the same length as meshes. 
	 * Not all primitive types, like line strip and triangle strip, can be combined.
	 * @param isStatic whether this mesh is static or not. Allows for internal optimizations.
	 * @param meshes the meshes to combine
	 * @param transformations the transformations to apply to the meshes 
	 * @return the combined mesh
	 */
	public static Mesh create(boolean isStatic, final Mesh[] meshes, final Matrix4[] transformations) {
		if (transformations != null && transformations.length < meshes.length)
			throw new IllegalArgumentException("Not enough transformations specified");
		final VertexAttributes attributes = meshes[0].getVertexAttributes();
		int vertCount = meshes[0].getNumVertices();
		int idxCount = meshes[0].getNumIndices();
		for (int i = 1; i < meshes.length; i++) {
			if (!meshes[i].getVertexAttributes().equals(attributes))
				throw new IllegalArgumentException("Inconsistent VertexAttributes");
			vertCount += meshes[i].getNumVertices();
			idxCount += meshes[i].getNumIndices();
		}
		final VertexAttribute posAttr = meshes[0].getVertexAttribute(Usage.Position);
		final int offset = posAttr.offset / 4;
		final int numComponents = posAttr.numComponents;
		final int vertexSize = attributes.vertexSize / 4;
		
		final float vertices[] = new float[vertCount * vertexSize];
		final short indices[] = new short[idxCount];
		
		meshes[0].getVertices(vertices);
		meshes[0].getIndices(indices);
		
		int voffset = meshes[0].getNumVertices() * vertexSize;
		int ioffset = meshes[0].getNumIndices();
		for (int i = 1; i < meshes.length; i++) {
			final Mesh mesh = meshes[i];
			final int vsize = mesh.getNumVertices() * vertexSize;
			final int isize = mesh.getNumIndices();
			mesh.getVertices(0, vsize, vertices, voffset);
			if (transformations != null)
				transform(transformations[i], vertices, vertexSize, offset, numComponents, voffset / vertexSize, vsize / vertexSize);
			mesh.getIndices(indices, ioffset);
			for (int j = 0; j < isize; i++)
				indices[ioffset+j] = (short)(indices[ioffset+j] + voffset);
			voffset += vsize;
			ioffset += isize;
		}
		
		final Mesh result = new Mesh(isStatic, vertices.length/vertexSize, indices.length, attributes);
		result.setVertices(vertices);
		result.setIndices(indices);
		return result;
	}

	/** Sets the vertices of this Mesh. The attributes are assumed to be given in float format. If this mesh is configured to use
	 * fixed point an IllegalArgumentException will be thrown.
	 * 
	 * @param vertices the vertices.
	 * @return the mesh for invocation chaining.*/
	public Mesh setVertices (float[] vertices) {
		this.vertices.setVertices(vertices, 0, vertices.length);
		
		return this;
	}

	/** Sets the vertices of this Mesh. The attributes are assumed to be given in float format. If this mesh is configured to use
	 * fixed point an IllegalArgumentException will be thrown.
	 * 
	 * @param vertices the vertices.
	 * @param offset the offset into the vertices array
	 * @param count the number of floats to use 
	 * @return the mesh for invocation chaining.*/
	public Mesh setVertices (float[] vertices, int offset, int count) {
		this.vertices.setVertices(vertices, offset, count);
		
		return this;
	}

	/** Copies the vertices from the Mesh to the float array. The float array must be large enough to hold all the Mesh's vertices.
	 * @param vertices the array to copy the vertices to */
	public void getVertices (float[] vertices) {
		getVertices(0, -1, vertices);
	}
	
	/** Copies the the remaining vertices from the Mesh to the float array. The float array must be large enough to hold the remaining vertices.
	 * @param srcOffset the offset (in number of floats) of the vertices in the mesh to copy
	 * @param vertices the array to copy the vertices to */
	public void getVertices (int srcOffset, float[] vertices) {
		getVertices(srcOffset, -1, vertices);
	}

	/** Copies the specified vertices from the Mesh to the float array. The float array must be large enough to hold count vertices.
	 * @param srcOffset the offset (in number of floats) of the vertices in the mesh to copy
	 * @param count the amount of floats to copy
	 * @param vertices the array to copy the vertices to */
	public void getVertices (int srcOffset, int count, float[] vertices) {
		getVertices(srcOffset, count, vertices, 0);
	}
	
	/** Copies the specified vertices from the Mesh to the float array. The float array must be large enough to hold destOffset+count vertices.
	 * @param srcOffset the offset (in number of floats) of the vertices in the mesh to copy
	 * @param count the amount of floats to copy
	 * @param vertices the array to copy the vertices to
	 * @param destOffset the offset (in floats) in the vertices array to start copying */
	public void getVertices (int srcOffset, int count, float[] vertices, int destOffset) {
		// TODO: Perhaps this method should be vertexSize aware??
		final int max = getNumVertices() * getVertexSize() / 4;
		if (count == -1) {
			count = max - srcOffset;
			if (count > vertices.length - destOffset)
				count = vertices.length - destOffset;
		}
		if (srcOffset < 0 || count <= 0 || (srcOffset + count) > max || destOffset < 0 || destOffset >= vertices.length)
			throw new IndexOutOfBoundsException();
		if ((vertices.length - destOffset) < count)
			throw new IllegalArgumentException("not enough room in vertices array, has " + vertices.length + " floats, needs " + count);
		int pos = getVerticesBuffer().position();
		getVerticesBuffer().position(srcOffset);
		getVerticesBuffer().get(vertices, destOffset, count);
		getVerticesBuffer().position(pos);
	}

	/** Sets the indices of this Mesh
	 * 
	 * @param indices the indices
	 * @return the mesh for invocation chaining. */
	public Mesh setIndices (short[] indices) {
		this.indices.setIndices(indices, 0, indices.length);
		
		return this;
	}

	/** Sets the indices of this Mesh.
	 * 
	 * @param indices the indices
	 * @param offset the offset into the indices array
	 * @param count the number of indices to copy
	 * @return the mesh for invocation chaining. */
	public Mesh setIndices (short[] indices, int offset, int count) {
		this.indices.setIndices(indices, offset, count);
		
		return this;
	}

	/** Copies the indices from the Mesh to the short array. The short array must be large enough to hold all the Mesh's indices.
	 * @param indices the array to copy the indices to */
	public void getIndices (short[] indices) {
		getIndices(indices, 0);
	}

	/** Copies the indices from the Mesh to the short array. The short array must be large enough to hold destOffset + all the Mesh's indices.
	 * @param indices the array to copy the indices to 
	 * @param destOffset the offset in the indices array to start copying */
	public void getIndices(short[] indices, int destOffset) {
		if ((indices.length - destOffset) < getNumIndices())
			throw new IllegalArgumentException("not enough room in indices array, has " + indices.length + " floats, needs "
				+ getNumIndices());
		int pos = getIndicesBuffer().position();
		getIndicesBuffer().position(0);
		getIndicesBuffer().get(indices, destOffset, getNumIndices());
		getIndicesBuffer().position(pos);
	}

	/** @return the number of defined indices */
	public int getNumIndices () {
		return indices.getNumIndices();
	}

	/** @return the number of defined vertices */
	public int getNumVertices () {
		return vertices.getNumVertices();
	}

	/** @return the maximum number of vertices this mesh can hold */
	public int getMaxVertices () {
		return vertices.getNumMaxVertices();
	}

	/** @return the maximum number of indices this mesh can hold */
	public int getMaxIndices () {
		return indices.getNumMaxIndices();
	}

	/** @return the size of a single vertex in bytes */
	public int getVertexSize () {
		return vertices.getAttributes().vertexSize;
	}

	/** Sets whether to bind the underlying {@link VertexArray} or {@link VertexBufferObject} automatically on a call to one of the
	 * {@link #render(int)} methods or not. Usually you want to use autobind. Manual binding is an expert functionality. There is a
	 * driver bug on the MSM720xa chips that will fuck up memory if you manipulate the vertices and indices of a Mesh multiple
	 * times while it is bound. Keep this in mind.
	 * 
	 * @param autoBind whether to autobind meshes. */
	public void setAutoBind (boolean autoBind) {
		this.autoBind = autoBind;
	}

	/** Binds the underlying {@link VertexArray}/{@link VertexBufferObject} and {@link IndexBufferObject} if indices were given. Use
	 * this with OpenGL ES 1.x and when auto-bind is disabled. */
	public void bind () {
		if (Gdx.graphics.isGL20Available()) throw new IllegalStateException("can't use this render method with OpenGL ES 2.0");
		vertices.bind();
		if (!isVertexArray && indices.getNumIndices() > 0) indices.bind();
	}

	/** Unbinds the underlying {@link VertexArray}/{@link VertexBufferObject} and {@link IndexBufferObject} is indices were given.
	 * Use this with OpenGL ES 1.x and when auto-bind is disabled. */
	public void unbind () {
		if (Gdx.graphics.isGL20Available()) throw new IllegalStateException("can't use this render method with OpenGL ES 2.0");
		vertices.unbind();
		if (!isVertexArray && indices.getNumIndices() > 0) indices.unbind();
	}

	/** Binds the underlying {@link VertexBufferObject} and {@link IndexBufferObject} if indices where given. Use this with OpenGL
	 * ES 2.0 and when auto-bind is disabled.
	 * 
	 * @param shader the shader (does not bind the shader) */
	public void bind (ShaderProgram shader) {
		if (!Gdx.graphics.isGL20Available()) throw new IllegalStateException("can't use this render method with OpenGL ES 1.x");

		vertices.bind(shader);
		if (indices.getNumIndices() > 0) indices.bind();
	}

	/** Unbinds the underlying {@link VertexBufferObject} and {@link IndexBufferObject} is indices were given. Use this with OpenGL
	 * ES 1.x and when auto-bind is disabled.
	 * 
	 * @param shader the shader (does not unbind the shader) */
	public void unbind (ShaderProgram shader) {
		if (!Gdx.graphics.isGL20Available()) {
			throw new IllegalStateException("can't use this render method with OpenGL ES 1.x");
		}

		vertices.unbind(shader);
		if (indices.getNumIndices() > 0) indices.unbind();
	}

	/** <p>
	 * Renders the mesh using the given primitive type. If indices are set for this mesh then getNumIndices() / #vertices per
	 * primitive primitives are rendered. If no indices are set then getNumVertices() / #vertices per primitive are rendered.
	 * </p>
	 * 
	 * <p>
	 * This method is intended for use with OpenGL ES 1.x and will throw an IllegalStateException when OpenGL ES 2.0 is used.
	 * </p>
	 * 
	 * @param primitiveType the primitive type */
	public void render (int primitiveType) {
		render(primitiveType, 0, indices.getNumMaxIndices() > 0 ? getNumIndices() : getNumVertices());
	}

	/** <p>
	 * Renders the mesh using the given primitive type. offset specifies the offset into vertex buffer and is ignored for the index
	 * buffer. Count specifies the number of vertices or indices to use thus count / #vertices per primitive primitives are
	 * rendered.
	 * </p>
	 * 
	 * <p>
	 * This method is intended for use with OpenGL ES 1.x and will throw an IllegalStateException when OpenGL ES 2.0 is used.
	 * </p>
	 * 
	 * @param primitiveType the primitive type
	 * @param offset the offset into the vertex buffer, ignored for indexed rendering
	 * @param count number of vertices or indices to use */
	public void render (int primitiveType, int offset, int count) {
		if (Gdx.graphics.isGL20Available()) throw new IllegalStateException("can't use this render method with OpenGL ES 2.0");
		if (count == 0) return;
		if (autoBind) bind();

		if (isVertexArray) {
			if (indices.getNumIndices() > 0) {
				ShortBuffer buffer = indices.getBuffer();
				int oldPosition = buffer.position();
				int oldLimit = buffer.limit();
				buffer.position(offset);
				buffer.limit(offset + count);
				Gdx.gl10.glDrawElements(primitiveType, count, GL10.GL_UNSIGNED_SHORT, buffer);
				buffer.position(oldPosition);
				buffer.limit(oldLimit);
			} else
				Gdx.gl10.glDrawArrays(primitiveType, offset, count);
		} else {
			if (indices.getNumIndices() > 0)
				Gdx.gl11.glDrawElements(primitiveType, count, GL10.GL_UNSIGNED_SHORT, offset * 2);
			else
				Gdx.gl11.glDrawArrays(primitiveType, offset, count);
		}

		if (autoBind) unbind();
	}

	/** <p>
	 * Renders the mesh using the given primitive type. If indices are set for this mesh then getNumIndices() / #vertices per
	 * primitive primitives are rendered. If no indices are set then getNumVertices() / #vertices per primitive are rendered.
	 * </p>
	 * 
	 * <p>
	 * This method will automatically bind each vertex attribute as specified at construction time via {@link VertexAttributes} to
	 * the respective shader attributes. The binding is based on the alias defined for each VertexAttribute.
	 * </p>
	 * 
	 * <p>
	 * This method must only be called after the {@link ShaderProgram#begin()} method has been called!
	 * </p>
	 * 
	 * <p>
	 * This method is intended for use with OpenGL ES 2.0 and will throw an IllegalStateException when OpenGL ES 1.x is used.
	 * </p>
	 * 
	 * @param primitiveType the primitive type */
	public void render (ShaderProgram shader, int primitiveType) {
		render(shader, primitiveType, 0, indices.getNumMaxIndices() > 0 ? getNumIndices() : getNumVertices());
	}

	/** <p>
	 * Renders the mesh using the given primitive type. offset specifies the offset into either the vertex buffer or the index
	 * buffer depending on whether indices are defined. count specifies the number of vertices or indices to use thus count /
	 * #vertices per primitive primitives are rendered.
	 * </p>
	 * 
	 * <p>
	 * This method will automatically bind each vertex attribute as specified at construction time via {@link VertexAttributes} to
	 * the respective shader attributes. The binding is based on the alias defined for each VertexAttribute.
	 * </p>
	 * 
	 * <p>
	 * This method must only be called after the {@link ShaderProgram#begin()} method has been called!
	 * </p>
	 * 
	 * <p>
	 * This method is intended for use with OpenGL ES 2.0 and will throw an IllegalStateException when OpenGL ES 1.x is used.
	 * </p>
	 * 
	 * @param shader the shader to be used
	 * @param primitiveType the primitive type
	 * @param offset the offset into the vertex or index buffer
	 * @param count number of vertices or indices to use */
	public void render (ShaderProgram shader, int primitiveType, int offset, int count) {
		if (!Gdx.graphics.isGL20Available()) throw new IllegalStateException("can't use this render method with OpenGL ES 1.x");
		if (count == 0) return;

		if (autoBind) bind(shader);

		if (isVertexArray) {
			if (indices.getNumIndices() > 0) {
				ShortBuffer buffer = indices.getBuffer();
				int oldPosition = buffer.position();
				int oldLimit = buffer.limit();
				buffer.position(offset);
				buffer.limit(offset + count);
				Gdx.gl20.glDrawElements(primitiveType, count, GL10.GL_UNSIGNED_SHORT, buffer);
				buffer.position(oldPosition);
				buffer.limit(oldLimit);
			} else {
				Gdx.gl20.glDrawArrays(primitiveType, offset, count);
			}
		} else {
			if (indices.getNumIndices() > 0)
				Gdx.gl20.glDrawElements(primitiveType, count, GL10.GL_UNSIGNED_SHORT, offset * 2);
			else
				Gdx.gl20.glDrawArrays(primitiveType, offset, count);
		}

		if (autoBind) unbind(shader);
	}

	/** Frees all resources associated with this Mesh */
	public void dispose () {
		if (meshes.get(Gdx.app) != null) meshes.get(Gdx.app).remove(this);
		vertices.dispose();
		indices.dispose();
	}

	/** Returns the first {@link VertexAttribute} having the given {@link Usage}.
	 * 
	 * @param usage the Usage.
	 * @return the VertexAttribute or null if no attribute with that usage was found. */
	public VertexAttribute getVertexAttribute (int usage) {
		VertexAttributes attributes = vertices.getAttributes();
		int len = attributes.size();
		for (int i = 0; i < len; i++)
			if (attributes.get(i).usage == usage) return attributes.get(i);

		return null;
	}

	/** @return the vertex attributes of this Mesh */
	public VertexAttributes getVertexAttributes () {
		return vertices.getAttributes();
	}

	/** @return the backing FloatBuffer holding the vertices. Does not have to be a direct buffer on Android! */
	public FloatBuffer getVerticesBuffer () {
		return vertices.getBuffer();
	}

	/** Calculates the {@link BoundingBox} of the vertices contained in this mesh. In case no vertices are defined yet a
	 * {@link GdxRuntimeException} is thrown. This method creates a new BoundingBox instance.
	 * 
	 * @return the bounding box. */
	public BoundingBox calculateBoundingBox () {
		BoundingBox bbox = new BoundingBox();
		calculateBoundingBox(bbox);
		return bbox;
	}

	/** Calculates the {@link BoundingBox} of the vertices contained in this mesh. In case no vertices are defined yet a
	 * {@link GdxRuntimeException} is thrown.
	 * 
	 * @param bbox the bounding box to store the result in. */
	public void calculateBoundingBox (BoundingBox bbox) {
		final int numVertices = getNumVertices();
		if (numVertices == 0) throw new GdxRuntimeException("No vertices defined");

		final FloatBuffer verts = vertices.getBuffer();
		bbox.inf();
		final VertexAttribute posAttrib = getVertexAttribute(Usage.Position);
		final int offset = posAttrib.offset / 4;
		final int vertexSize = vertices.getAttributes().vertexSize / 4;
		int idx = offset;

		switch (posAttrib.numComponents) {
		case 1:
			for (int i = 0; i < numVertices; i++) {
				bbox.ext(verts.get(idx), 0, 0);
				idx += vertexSize;
			}
			break;
		case 2:
			for (int i = 0; i < numVertices; i++) {
				bbox.ext(verts.get(idx), verts.get(idx + 1), 0);
				idx += vertexSize;
			}
			break;
		case 3:
			for (int i = 0; i < numVertices; i++) {
				bbox.ext(verts.get(idx), verts.get(idx + 1), verts.get(idx + 2));
				idx += vertexSize;
			}
			break;
		}
	}
	
	/** Calculate the {@link BoundingBox} of the specified part.
	 * @param out the bounding box to store the result in. 
	 * @param offset the start index of the part.
	 * @param count the amount of indices the part contains. 
	 * @return the value specified by out. */
	public BoundingBox calculateBoundingBox(final BoundingBox out, int offset, int count) {
		return extendBoundingBox(out.inf(), offset, count);
	}
	
	/** Calculate the {@link BoundingBox} of the specified part.
	 * @param out the bounding box to store the result in. 
	 * @param offset the start index of the part.
	 * @param count the amount of indices the part contains. 
	 * @return the value specified by out. */
	public BoundingBox calculateBoundingBox(final BoundingBox out, int offset, int count, final Matrix4 transform) {
		return extendBoundingBox(out.inf(), offset, count, transform);
	}

	/** Extends the specified {@link BoundingBox} with the specified part.
	 * @param out the bounding box to store the result in. 
	 * @param offset the start index of the part.
	 * @param count the amount of indices the part contains. 
	 * @return the value specified by out. */
	public BoundingBox extendBoundingBox(final BoundingBox out, int offset, int count) {
		return extendBoundingBox(out, offset, count, null);
	}
	
	private final Vector3 tmpV = new Vector3();
	/** Extends the specified {@link BoundingBox} with the specified part.
	 * @param out the bounding box to store the result in. 
	 * @param offset the start index of the part.
	 * @param count the amount of indices the part contains. 
	 * @return the value specified by out. */
	public BoundingBox extendBoundingBox(final BoundingBox out, int offset, int count, final Matrix4 transform) {
		int numIndices = getNumIndices();
		if (offset < 0 || count < 1 || offset + count > numIndices)
			throw new GdxRuntimeException("Not enough indices");
		
		final FloatBuffer verts = vertices.getBuffer();
		final ShortBuffer index = indices.getBuffer();
		final VertexAttribute posAttrib = getVertexAttribute(Usage.Position);
		final int posoff = posAttrib.offset / 4;
		final int vertexSize = vertices.getAttributes().vertexSize / 4;
		final int end = offset + count;
		
		switch (posAttrib.numComponents) {
		case 1:
			for (int i = offset; i < end; i++) {
				final int idx = index.get(i) * vertexSize + posoff;
				tmpV.set(verts.get(idx), 0, 0);
				if (transform != null)
					tmpV.mul(transform);
				out.ext(tmpV);
			}
			break;
		case 2:
			for (int i = offset; i < end; i++) {
				final int idx = index.get(i) * vertexSize + posoff;
				tmpV.set(verts.get(idx), verts.get(idx + 1), 0);
				if (transform != null)
					tmpV.mul(transform);
				out.ext(tmpV);
			}
			break;
		case 3:
			for (int i = offset; i < end; i++) {
				final int idx = index.get(i) * vertexSize + posoff;
				tmpV.set(verts.get(idx), verts.get(idx + 1), verts.get(idx + 2));
				if (transform != null)
					tmpV.mul(transform);
				out.ext(tmpV);
			}
			break;
		}
		return out;
	}

	/** @return the backing shortbuffer holding the indices. Does not have to be a direct buffer on Android! */
	public ShortBuffer getIndicesBuffer () {
		return indices.getBuffer();
	}

	private static void addManagedMesh (Application app, Mesh mesh) {
		List<Mesh> managedResources = meshes.get(app);
		if (managedResources == null) managedResources = new ArrayList<Mesh>();
		managedResources.add(mesh);
		meshes.put(app, managedResources);
	}

	/** Invalidates all meshes so the next time they are rendered new VBO handles are generated.
	 * @param app */
	public static void invalidateAllMeshes (Application app) {
		List<Mesh> meshesList = meshes.get(app);
		if (meshesList == null) return;
		for (int i = 0; i < meshesList.size(); i++) {
			if (meshesList.get(i).vertices instanceof VertexBufferObject) {
				((VertexBufferObject)meshesList.get(i).vertices).invalidate();
			}
			meshesList.get(i).indices.invalidate();
		}
	}

	/** Will clear the managed mesh cache. I wouldn't use this if i was you :) */
	public static void clearAllMeshes (Application app) {
		meshes.remove(app);
	}

	public static String getManagedStatus () {
		StringBuilder builder = new StringBuilder();
		int i = 0;
		builder.append("Managed meshes/app: { ");
		for (Application app : meshes.keySet()) {
			builder.append(meshes.get(app).size());
			builder.append(" ");
		}
		builder.append("}");
		return builder.toString();
	}

	/** Method to scale the positions in the mesh. Normals will be kept as is. This is a potentially slow operation, use with care.
	 * It will also create a temporary float[] which will be garbage collected.
	 * 
	 * @param scaleX scale on x
	 * @param scaleY scale on y
	 * @param scaleZ scale on z */
	public void scale (float scaleX, float scaleY, float scaleZ) {
		final VertexAttribute posAttr = getVertexAttribute(Usage.Position);
		final int offset = posAttr.offset / 4;
		final int numComponents = posAttr.numComponents;
		final int numVertices = getNumVertices();
		final int vertexSize = getVertexSize() / 4;

		final float[] vertices = new float[numVertices * vertexSize];
		getVertices(vertices);

		int idx = offset;
		switch (numComponents) {
		case 1:
			for (int i = 0; i < numVertices; i++) {
				vertices[idx] *= scaleX;
				idx += vertexSize;
			}
			break;
		case 2:
			for (int i = 0; i < numVertices; i++) {
				vertices[idx] *= scaleX;
				vertices[idx + 1] *= scaleY;
				idx += vertexSize;
			}
			break;
		case 3:
			for (int i = 0; i < numVertices; i++) {
				vertices[idx] *= scaleX;
				vertices[idx + 1] *= scaleY;
				vertices[idx + 2] *= scaleZ;
				idx += vertexSize;
			}
			break;
		}

		setVertices(vertices);
	}
	
	/** 
	 * Method to transform the positions in the mesh. Normals will be kept as is. This is a potentially slow operation, use with care.
	 * It will also create a temporary float[] which will be garbage collected.
	 * 
	 * @param matrix the transformation matrix */
	public void transform(final Matrix4 matrix) {
		transform(matrix, 0, getNumVertices());
	}
	
	// TODO: Protected for now, because transforming a portion works but still copies all vertices
	protected void transform(final Matrix4 matrix, final int start, final int count) {
		final VertexAttribute posAttr = getVertexAttribute(Usage.Position);
		final int offset = posAttr.offset / 4;
		final int vertexSize = getVertexSize() / 4;
		final int numComponents = posAttr.numComponents;
		final int numVertices = getNumVertices();
		
		final float[] vertices = new float[numVertices * vertexSize];
		// TODO: getVertices(vertices, start * vertexSize, count * vertexSize);
		getVertices(0, vertices.length, vertices);
		transform(matrix, vertices, vertexSize, offset, numComponents, start, count);
		setVertices(vertices, 0, vertices.length);
		// TODO: setVertices(start * vertexSize, vertices, 0, vertices.length);
	}
	
	/**
	 * Method to transform the positions in the float array. Normals will be kept as is. This is a potentially slow operation, use with care.
	 * @param matrix the transformation matrix
	 * @param vertices the float array
	 * @param vertexSize the number of floats in each vertex
	 * @param offset the offset within a vertex to the position
	 * @param dimensions the size of the position
	 * @param start the vertex to start with
	 * @param count the amount of vertices to transform
	 */
	public static void transform(final Matrix4 matrix, final float[] vertices, int vertexSize, int offset, int dimensions, int start, int count) {
		if (offset < 0 || dimensions < 1 || (offset + dimensions) > vertexSize)
			throw new IndexOutOfBoundsException();
		if (start < 0 || count < 1 || ((start + count) * vertexSize) > vertices.length)
			throw new IndexOutOfBoundsException("start = "+start+", count = "+count+", vertexSize = "+vertexSize+", length = "+vertices.length);
		
		final Vector3 tmp = new Vector3();
		
		int idx = offset + (start * vertexSize);
		switch(dimensions) {
		case 1:
			for (int i = 0; i < count; i++) {
				tmp.set(vertices[idx], 0, 0).mul(matrix);
				vertices[idx] = tmp.x;
				idx += vertexSize;
			}
			break;
		case 2:
			for (int i = 0; i < count; i++) {
				tmp.set(vertices[idx], vertices[idx + 1], 0).mul(matrix);
				vertices[idx] = tmp.x;
				vertices[idx+1] = tmp.y;
				idx += vertexSize;
			}
			break;
		case 3:
			for (int i = 0; i < count; i++) {
				tmp.set(vertices[idx], vertices[idx + 1], vertices[idx + 2]).mul(matrix);
				vertices[idx] = tmp.x;
				vertices[idx+1] = tmp.y;
				vertices[idx+2] = tmp.z;
				idx += vertexSize;
			}
			break;
		}
	}
	
	/** 
	 * Method to transform the texture coordinates in the mesh. This is a potentially slow operation, use with care.
	 * It will also create a temporary float[] which will be garbage collected.
	 * 
	 * @param matrix the transformation matrix */
	public void transformUV(final Matrix3 matrix) {
		transformUV(matrix, 0, getNumVertices());
	}
	
	// TODO: Protected for now, because transforming a portion works but still copies all vertices
	protected void transformUV(final Matrix3 matrix, final int start, final int count) {
		final VertexAttribute posAttr = getVertexAttribute(Usage.TextureCoordinates);
		final int offset = posAttr.offset / 4;
		final int vertexSize = getVertexSize() / 4;
		final int numVertices = getNumVertices();
		
		final float[] vertices = new float[numVertices * vertexSize];
		// TODO: getVertices(vertices, start * vertexSize, count * vertexSize);
		getVertices(0, vertices.length, vertices);
		transformUV(matrix, vertices, vertexSize, offset, start, count);
		setVertices(vertices, 0, vertices.length);
		// TODO: setVertices(start * vertexSize, vertices, 0, vertices.length);
	}
	
	/**
	 * Method to transform the texture coordinates (UV) in the float array. This is a potentially slow operation, use with care.
	 * @param matrix the transformation matrix
	 * @param vertices the float array
	 * @param vertexSize the number of floats in each vertex
	 * @param offset the offset within a vertex to the texture location
	 * @param start the vertex to start with
	 * @param count the amount of vertices to transform
	 */
	public static void transformUV(final Matrix3 matrix, final float[] vertices, int vertexSize, int offset, int start, int count) {
		if (start < 0 || count < 1 || ((start + count) * vertexSize) > vertices.length)
			throw new IndexOutOfBoundsException("start = "+start+", count = "+count+", vertexSize = "+vertexSize+", length = "+vertices.length);
		
		final Vector2 tmp = new Vector2();
		
		int idx = offset + (start * vertexSize);
		for (int i = 0; i < count; i++) {
			tmp.set(vertices[idx], vertices[idx+1]).mul(matrix);
			vertices[idx] = tmp.x;
			vertices[idx+1] = tmp.y;
			idx += vertexSize;
		}
	}
	
	/** Copies this mesh optionally removing duplicate vertices and/or reducing the amount of attributes.
	 * @param isStatic whether the new mesh is static or not. Allows for internal optimizations.
	 * @param removeDuplicates whether to remove duplicate vertices if possible. Only the vertices specified by usage are checked.
	 * @param usage which attributes (if available) to copy
	 * @return the copy of this mesh
	 */
	public Mesh copy(boolean isStatic, boolean removeDuplicates, final int[] usage) {
		// TODO move this to a copy constructor?
		// TODO duplicate the buffers without double copying the data if possible.
		// TODO perhaps move this code to JNI if it turns out being too slow.
		final int vertexSize = getVertexSize() / 4;
		int numVertices = getNumVertices();
		float[] vertices = new float[numVertices * vertexSize];
		getVertices(0, vertices.length, vertices);
		short[] checks = null;
		VertexAttribute[] attrs = null;
		int newVertexSize = 0;
		if (usage != null) {
			int size = 0;
			int as = 0;
			for (int i = 0; i < usage.length; i++)
				if (getVertexAttribute(usage[i]) != null) {
					size += getVertexAttribute(usage[i]).numComponents;
					as++;
				}
			if (size > 0) {
				attrs = new VertexAttribute[as];
				checks = new short[size];
				int idx = -1;
				int ai = -1;
				for (int i = 0; i < usage.length; i++) {
					VertexAttribute a = getVertexAttribute(usage[i]);
					if (a == null)
						continue;
					for (int j = 0; j < a.numComponents; j++)
						checks[++idx] = (short)(a.offset + j);
					attrs[++ai] = new VertexAttribute(a.usage, a.numComponents, a.alias);
					newVertexSize += a.numComponents;
				}
			}
		}
		if (checks == null) {
			checks = new short[vertexSize];
			for (short i = 0; i < vertexSize; i++)
				checks[i] = i;
			newVertexSize = vertexSize;
		}
		
		int numIndices = getNumIndices();
		short[] indices = null;	
		if (numIndices > 0) {
			indices = new short[numIndices];
			getIndices(indices);
			if (removeDuplicates || newVertexSize != vertexSize) {
				float[] tmp = new float[vertices.length];
				int size = 0;
				for (int i = 0; i < numIndices; i++) {
					final int idx1 = indices[i] * vertexSize;
					short newIndex = -1;
					if (removeDuplicates) {
						for (short j = 0; j < size && newIndex < 0; j++) {
							final int idx2 = j*newVertexSize;
							boolean found = true;
							for (int k = 0; k < checks.length && found; k++) {
								if (tmp[idx2+k] != vertices[idx1+checks[k]])
									found = false;
							}
							if (found)
								newIndex = j;
						}
					}
					if (newIndex > 0)
						indices[i] = newIndex;
					else {
						final int idx = size * newVertexSize;
						for (int j = 0; j < checks.length; j++)
							tmp[idx+j] = vertices[idx1+checks[j]];
						indices[i] = (short)size;
						size++;
					}
				}
				vertices = tmp;
				numVertices = size;
			}
		}
		
		Mesh result;
		if (attrs == null)
			result = new Mesh(isStatic, numVertices, indices == null ? 0 : indices.length, getVertexAttributes());
		else
			result = new Mesh(isStatic, numVertices, indices == null ? 0 : indices.length, attrs);
		result.setVertices(vertices, 0, numVertices * newVertexSize);
		result.setIndices(indices);
		return result;
	}
	
	/** Copies this mesh.
	 * @param isStatic whether the new mesh is static or not. Allows for internal optimizations.
	 * @return the copy of this mesh
	 */
	public Mesh copy(boolean isStatic) {
		return copy(isStatic, false, null);
	}
}
