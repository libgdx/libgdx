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

package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;

/** A MeshPart is composed of a subset of vertices of a {@link Mesh}, along with the primitive type. The vertices subset is
 * described by an offset and size. When the mesh is indexed (which is when {@link Mesh#getNumIndices()} > 0), then the
 * {@link #indexOffset} represents the offset in the indices array and {@link #numVertices} represents the number of indices. When
 * the mesh isn't indexed, then the {@link #indexOffset} member represents the offset in the vertices array and the
 * {@link #numVertices} member represents the number of vertices.</p>
 * 
 * In other words: Regardless whether the mesh is indexed or not, when {@link #primitiveType} is not a strip, then
 * {@link #numVertices} equals the number of primitives multiplied by the number of vertices per primitive. So if the MeshPart
 * represents 4 triangles ({@link #primitiveType} is GL_TRIANGLES), then the {@link #numVertices} member is 12 (4 triangles * 3
 * vertices = 12 vertices total). Likewise, if the part represents 12 lines ({@link #primitiveType} is GL_LINES), then the size is
 * 24 (12 lines * 2 vertices = 24 vertices total).</p>
 * 
 * Note that some classes might require the mesh (part) to be indexed.</p>
 * 
 * The {@link Mesh} referenced by the {@link #mesh} member must outlive the MeshPart. When the mesh is disposed, the MeshPart is
 * unusable.
 * @author badlogic, Xoppa */
public class MeshPart {
	/** Unique id within model, may be null. Will be ignored by {@link #equals(MeshPart)} **/
	public String id;
	/** The primitive type, OpenGL constant e.g: {@link GL20#GL_TRIANGLES}, {@link GL20#GL_POINTS}, {@link GL20#GL_LINES},
	 * {@link GL20#GL_LINE_STRIP}, {@link GL20#GL_TRIANGLE_STRIP} **/
	public int primitiveType;
	/** The offset in the {@link #mesh} to this part. If the mesh is indexed ({@link Mesh#getNumIndices()} > 0), this is the offset
	 * in the indices array, otherwise it is the offset in the vertices array. **/
	public int indexOffset;
	/** The size (in total number of vertices) of this part in the {@link #mesh}. When the mesh is indexed (
	 * {@link Mesh#getNumIndices()} > 0), this is the number of indices, otherwise it is the number of vertices. **/
	public int numVertices;
	/** The Mesh the part references, also stored in {@link Model} **/
	public Mesh mesh;

	/** Construct a new MeshPart, with null values. The MeshPart is unusable until you set all members. **/
	public MeshPart () {
	}

	/** Construct a new MeshPart and set all its values.
	 * @param id The id of the new part, may be null.
	 * @param mesh The mesh which holds all vertices and (optional) indices of this part.
	 * @param offset The offset within the mesh to this part.
	 * @param size The size (in total number of vertices) of the part.
	 * @param type The primitive type of the part (e.g. GL_TRIANGLES, GL_LINE_STRIP, etc.). */
	public MeshPart (final String id, final Mesh mesh, final int offset, final int size, final int type) {
		this.id = id;
		this.mesh = mesh;
		this.indexOffset = offset;
		this.numVertices = size;
		this.primitiveType = type;
	}

	/** Construct a new MeshPart which is an exact copy of the provided MeshPart.
	 * @param copyFrom The MeshPart to copy. */
	public MeshPart (final MeshPart copyFrom) {
		this(copyFrom.id, copyFrom.mesh, copyFrom.indexOffset, copyFrom.numVertices, copyFrom.primitiveType);
	}

	/** Compares this MeshPart to the specified MeshPart and returns true if they both reference the same {@link Mesh} and the
	 * {@link #indexOffset}, {@link #numVertices} and {@link #primitiveType} members are equal. The {@link #id} member is ignored.
	 * @param other The other MeshPart to compare this MeshPart to.
	 * @return True when this MeshPart equals the other MeshPart (ignoring the {@link #id} member), false otherwise. */
	public boolean equals (final MeshPart other) {
		return other == this
			|| (other != null && other.mesh == mesh && other.primitiveType == primitiveType && other.indexOffset == indexOffset && other.numVertices == numVertices);
	}

	@Override
	public boolean equals (final Object arg0) {
		if (arg0 == null) return false;
		if (arg0 == this) return true;
		if (!(arg0 instanceof MeshPart)) return false;
		return equals((MeshPart)arg0);
	}
}
