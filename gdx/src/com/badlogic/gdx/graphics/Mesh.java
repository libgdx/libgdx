package com.badlogic.gdx.graphics;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexArray;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.graphics.glutils.VertexData;

/**
 * <p>
 * A Mesh holds vertices composed of attributes specified by a
 * {@link VertexAttributes} instance. The vertices are held either in VRAM in
 * form of vertex buffer objects or in RAM in form of vertex arrays. The former
 * variant is more performant and is prefered over vertex arrays if hardware
 * supports it.
 * </p>
 * 
 * <p>
 * Meshes are automatically managed. If the OpenGL context is lost all vertex
 * buffer objects get invalidated and must be reloaded when the context is
 * recreated. This only happens on Android when a user switches to another
 * application or receives an incoming call. A managed Mesh will be reloaded
 * automagically so you don't have to do this manually.
 * </p>
 * 
 * <p>
 * A Mesh consists of vertices and optionally indices which specify which
 * vertices define a triangle. Each vertex is composed of attributes such as
 * position, normal, color or texture coordinate. Note that not all of this
 * attributes must be given, except for position which is non-optional. Each
 * attribute has an alias which is used when rendering a Mesh in OpenGL ES 2.0.
 * The alias is used to bind a specific vertex attribute to a shader attribute.
 * The shader source and the alias of the attribute must match exactly for this
 * to work. For OpenGL ES 1.x rendering this aliases are irrelevant.
 * </p>
 * 
 * <p>
 * Meshes can be used with either OpenGL ES 1.x or OpenGL ES 2.0.
 * </p>
 * 
 * @author mzechner
 * 
 * 
 */
public class Mesh {
	/** list of all meshes **/
	static final ArrayList<Mesh> meshes = new ArrayList<Mesh>();

	final VertexData vertices;
	final IndexBufferObject indices;
	boolean autoBind = true;
	final boolean isVertexArray;

	/**
	 * Creates a new Mesh with the given attributes.
	 * 
	 * @param isStatic
	 *            whether this mesh is static or not. Allows for internal
	 *            optimizations.
	 * @param maxVertices
	 *            the maximum number of vertices this mesh can hold
	 * @param maxIndices
	 *            the maximum number of indices this mesh can hold
	 * @param attributes
	 *            the {@link VertexAttribute}s. Each vertex attribute defines
	 *            one property of a vertex such as position, normal or texture
	 *            coordinate
	 */
	public Mesh(boolean isStatic, int maxVertices, int maxIndices,
			VertexAttribute... attributes) {
		if (isStatic && (Gdx.gl11 != null || Gdx.gl20 != null)) {
			vertices = new VertexBufferObject(isStatic, maxVertices, attributes);
			indices = new IndexBufferObject(isStatic, maxIndices);
			isVertexArray = false;
		} else {
			vertices = new VertexArray(maxVertices, attributes);
			indices = new IndexBufferObject(maxIndices);
			isVertexArray = true;
		}

		meshes.add(this);
	}

	/**
	 * Sets the vertices of this Mesh. The attributes are assumed to be given in
	 * float format. If this mesh is configured to use fixed point an
	 * IllegalArgumentException will be thrown.
	 * 
	 * @param vertices
	 *            the vertices.
	 */
	public void setVertices(float[] vertices) {
		this.vertices.setVertices(vertices, 0, vertices.length);
	}

	/**
	 * Sets the vertices of this Mesh. The attributes are assumed to be given in
	 * float format. If this mesh is configured to use fixed point an
	 * IllegalArgumentException will be thrown.
	 * 
	 * @param vertices
	 *            the vertices.
	 * @param offset
	 *            the offset into the vertices array
	 * @param count
	 *            the number of floats to use
	 */
	public void setVertices(float[] vertices, int offset, int count) {
		this.vertices.setVertices(vertices, offset, count);
	}

	/**
	 * Sets the indices of this Mesh
	 * 
	 * @param indices
	 *            the indices
	 */
	public void setIndices(short[] indices) {
		this.indices.setIndices(indices, 0, indices.length);
	}

	/**
	 * Sets the indices of this Mesh.
	 * 
	 * @param indices
	 *            the indices
	 * @param offset
	 *            the offset into the indices array
	 * @param count
	 *            the number of indices to copy
	 */
	public void setIndices(short[] indices, int offset, int count) {
		this.indices.setIndices(indices, offset, count);
	}

	/**
	 * @return the number of defined indices
	 */
	public int getNumIndices() {
		return indices.getNumIndices();
	}

	/**
	 * @return the number of defined vertices
	 */
	public int getNumVertices() {
		return vertices.getNumVertices();
	}

	/**
	 * @return the maximum number of vertices this mesh can hold
	 */
	public int getMaxVertices() {
		return vertices.getNumMaxVertices();
	}

	/**
	 * @return the maximum number of indices this mesh can hold
	 */
	public int getMaxIndices() {
		return indices.getNumMaxIndices();
	}

	/**
	 * @return the size of a single vertex in bytes
	 */
	public int getVertexSize() {
		return vertices.getAttributes().vertexSize;
	}

	/**
	 * Sets whether to bind the underlying {@link VertexArray} or
	 * {@link VertexBufferObject} automatically on a call to one of the
	 * {@link #render(int)} methods or not. Usually you want to use autobind.
	 * Manual binding is an expert functionality. There is a driver bug
	 * on the MSM720xa chips that will fuck up memory if you manipulate the
	 * vertices and indices of a Mesh multiple times while it is bound. Keep 
	 * this in mind.
	 * 
	 * @param autoBind whether to autobind meshes.
	 */
	public void setAutoBind(boolean autoBind) {
		this.autoBind = autoBind;
	}

	/**
	 * Binds the underlying {@link VertexArray}/{@link VertexBufferObject} and
	 * {@link IndexBufferObject} if indices were given. Use this with OpenGL ES
	 * 1.x and when auto-bind is disabled.
	 */
	public void bind() {
		if (Gdx.graphics.isGL20Available())
			throw new IllegalStateException(
					"can't use this render method with OpenGL ES 2.0");
		vertices.bind();
		if (!isVertexArray && indices.getNumIndices() > 0)
			indices.bind();
	}

	/**
	 * Unbinds the underlying {@link VertexArray}/{@link VertexBufferObject} and
	 * {@link IndexBufferObject} is indices were given. Use this with OpenGL ES
	 * 1.x and when auto-bind is disabled.
	 */
	public void unbind() {
		if (Gdx.graphics.isGL20Available())
			throw new IllegalStateException(
					"can't use this render method with OpenGL ES 2.0");
		vertices.unbind();
		if (!isVertexArray && indices.getNumIndices() > 0)
			indices.unbind();
	}

	/**
	 * Binds the underlying {@link VertexBufferObject} and
	 * {@link IndexBufferObject} if indices where given. Use this with OpenGL ES
	 * 2.0 and when auto-bind is disabled.
	 * 
	 * @param shader
	 *            the shader (does not bind the shader)
	 */
	public void bind(ShaderProgram shader) {
		if (!Gdx.graphics.isGL20Available())
			throw new IllegalStateException(
					"can't use this render method with OpenGL ES 1.x");

		((VertexBufferObject) vertices).bind(shader);
		if (indices.getNumIndices() > 0)
			indices.bind();
	}

	/**
	 * Unbinds the underlying {@link VertexBufferObject} and
	 * {@link IndexBufferObject} is indices were given. Use this with OpenGL ES
	 * 1.x and when auto-bind is disabled.
	 * 
	 * @param shader
	 *            the shader (does not unbind the shader)
	 */
	public void unbind(ShaderProgram shader) {
		if (!Gdx.graphics.isGL20Available())
			throw new IllegalStateException(
					"can't use this render method with OpenGL ES 1.x");

		((VertexBufferObject) vertices).unbind(shader);
		if (indices.getNumIndices() > 0)
			indices.unbind();
	}

	/**
	 * <p>
	 * Renders the mesh using the given primitive type. If indices are set for
	 * this mesh then getNumIndices() / #vertices per primitive primitives are
	 * rendered. If no indices are set then getNumVertices() / #vertices per
	 * primitive are rendered.
	 * </p>
	 * 
	 * <p>
	 * This method is intended for use with OpenGL ES 1.x and will throw an
	 * IllegalStateException when OpenGL ES 2.0 is used.
	 * </p>
	 * 
	 * @param primitiveType
	 *            the primitive type
	 */
	public void render(int primitiveType) {
		render(primitiveType, 0,
				indices.getNumMaxIndices() > 0 ? getNumIndices()
						: getNumVertices());
	}

	/**
	 * <p>
	 * Renders the mesh using the given primitive type. offset specifies the
	 * offset into vertex buffer and is ignored for the index buffer. Count
	 * specifies the number of vertices or indices to use thus count / #vertices
	 * per primitive primitives are rendered.
	 * </p>
	 * 
	 * <p>
	 * This method is intended for use with OpenGL ES 1.x and will throw an
	 * IllegalStateException when OpenGL ES 2.0 is used.
	 * </p>
	 * 
	 * @param primitiveType
	 *            the primitive type
	 * @param offset
	 *            the offset into the vertex buffer, ignored for indexed
	 *            rendering
	 * @param count
	 *            number of vertices or indices to use
	 */
	public void render(int primitiveType, int offset, int count) {
		if (Gdx.graphics.isGL20Available())
			throw new IllegalStateException(
					"can't use this render method with OpenGL ES 2.0");

		if (autoBind)
			bind();

		if (isVertexArray) {
			if (indices.getNumIndices() > 0) {
				int oldPosition = indices.getBuffer().position();
				indices.getBuffer().position(offset);
				Gdx.gl10.glDrawElements(primitiveType, count,
						GL10.GL_UNSIGNED_SHORT, indices.getBuffer());
				indices.getBuffer().position(oldPosition);
			} else
				Gdx.gl10.glDrawArrays(primitiveType, offset, count);
		} else {
			if (indices.getNumIndices() > 0)
				Gdx.gl11.glDrawElements(primitiveType, count,
						GL10.GL_UNSIGNED_SHORT, offset);
			else
				Gdx.gl11.glDrawArrays(primitiveType, offset, count);
		}

		if (autoBind)
			unbind();
	}

	/**
	 * <p>
	 * Renders the mesh using the given primitive type. If indices are set for
	 * this mesh then getNumIndices() / #vertices per primitive primitives are
	 * rendered. If no indices are set then getNumVertices() / #vertices per
	 * primitive are rendered.
	 * </p>
	 * 
	 * <p>
	 * This method will automatically bind each vertex attribute as specified at
	 * construction time via {@link VertexAttributes} to the respective shader
	 * attributes. The binding is based on the alias defined for each
	 * VertexAttribute.
	 * </p>
	 * 
	 * <p>
	 * This method must only be called after the {@link ShaderProgram#begin()}
	 * method has been called!
	 * </p>
	 * 
	 * <p>
	 * This method is intended for use with OpenGL ES 2.0 and will throw an
	 * IllegalStateException when OpenGL ES 1.x is used.
	 * </p>
	 * 
	 * @param primitiveType
	 *            the primitive type
	 */
	public void render(ShaderProgram shader, int primitiveType) {
		render(shader, primitiveType, 0,
				indices.getNumMaxIndices() > 0 ? getNumIndices()
						: getNumVertices());
	}

	/**
	 * <p>
	 * Renders the mesh using the given primitive type. offset specifies the
	 * offset into either the vertex buffer or the index buffer depending on
	 * whether indices are defined. count specifies the number of vertices or
	 * indices to use thus count / #vertices per primitive primitives are
	 * rendered.
	 * </p>
	 * 
	 * <p>
	 * This method will automatically bind each vertex attribute as specified at
	 * construction time via {@link VertexAttributes} to the respective shader
	 * attributes. The binding is based on the alias defined for each
	 * VertexAttribute.
	 * </p>
	 * 
	 * <p>
	 * This method must only be called after the {@link ShaderProgram#begin()}
	 * method has been called!
	 * </p>
	 * 
	 * <p>
	 * This method is intended for use with OpenGL ES 2.0 and will throw an
	 * IllegalStateException when OpenGL ES 1.x is used.
	 * </p>
	 * 
	 * @param shader
	 *            the shader to be used
	 * @param primitiveType
	 *            the primitive type
	 * @param offset
	 *            the offset into the vertex or index buffer
	 * @param count
	 *            number of vertices or indices to use
	 */
	public void render(ShaderProgram shader, int primitiveType, int offset,
			int count) {
		if (!Gdx.graphics.isGL20Available())
			throw new IllegalStateException(
					"can't use this render method with OpenGL ES 1.x");

		if (autoBind)
			bind(shader);

		if (indices.getNumIndices() > 0)
			Gdx.gl20.glDrawElements(primitiveType, count,
					GL10.GL_UNSIGNED_SHORT, offset);
		else
			Gdx.gl20.glDrawArrays(primitiveType, offset, count);

		if (autoBind)
			unbind(shader);
	}

	/**
	 * Frees all resources associated with this Mesh
	 */
	public void dispose() {
		meshes.remove(this);
		vertices.dispose();
		indices.dispose();
	}

	/**
	 * Returns the first {@link VertexAttribute} having the given {@link Usage}.
	 * 
	 * @param usage
	 *            the Usage.
	 * @return the VertexAttribute or null if no attribute with that usage was
	 *         found.
	 */
	public VertexAttribute getVertexAttribute(int usage) {
		VertexAttributes attributes = vertices.getAttributes();
		int len = attributes.size();
		for (int i = 0; i < len; i++)
			if (attributes.get(i).usage == usage)
				return attributes.get(i);

		return null;
	}

	/**
	 * @return the vertex attributes of this Mesh
	 */
	public VertexAttributes getVertexAttributes() {
		return vertices.getAttributes();
	}

	/**
	 * @return the backing FloatBuffer holding the vertices. Does not have to be
	 *         a direct buffer on Android!
	 */
	public FloatBuffer getVerticesBuffer() {
		return vertices.getBuffer();
	}

	/**
	 * @return the backing shortbuffer holding the indices. Does not have to be
	 *         a direct buffer on Android!
	 */
	public ShortBuffer getIndicesBuffer() {
		return indices.getBuffer();
	}

	/**
	 * Invalidates all meshes so the next time they are rendered new VBO handles
	 * are generated.
	 */
	public static void invalidateAllMeshes() {

		for (int i = 0; i < meshes.size(); i++) {
			if (meshes.get(i).vertices instanceof VertexBufferObject) {
				((VertexBufferObject) meshes.get(i).vertices).invalidate();
				meshes.get(i).indices.invalidate();
			}
		}
	}

	/**
	 * Will clear the managed mesh cache. I wouldn't use this if i was you :)
	 */
	public static void clearAllMeshes() {
		meshes.clear();
	}
}
