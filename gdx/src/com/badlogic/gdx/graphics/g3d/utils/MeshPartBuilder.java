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

package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool.Poolable;

public interface MeshPartBuilder {
	/** @return The {@link MeshPart} currently building. */
	public MeshPart getMeshPart ();

	/** @return The primitive type used for building, e.g. {@link GL20#GL_TRIANGLES} or {@link GL20#GL_LINES}. */
	public int getPrimitiveType ();

	/** @return The {@link VertexAttributes} available for building. */
	public VertexAttributes getAttributes ();

	/** Set the color used to tint the vertex color, defaults to white. Only applicable for {@link Usage#ColorPacked} or
	 * {@link Usage#ColorUnpacked}. */
	public void setColor (final Color color);

	/** Set the color used to tint the vertex color, defaults to white. Only applicable for {@link Usage#ColorPacked} or
	 * {@link Usage#ColorUnpacked}. */
	public void setColor (float r, float g, float b, float a);

	/** Set range of texture coordinates used (default is 0,0,1,1). */
	public void setUVRange (float u1, float v1, float u2, float v2);

	/** Set range of texture coordinates from the specified TextureRegion. */
	public void setUVRange (TextureRegion r);

	/** Get the current vertex transformation matrix. */
	public Matrix4 getVertexTransform (Matrix4 out);

	/** Set the current vertex transformation matrix and enables vertex transformation. */
	public void setVertexTransform (Matrix4 transform);

	/** Indicates whether vertex transformation is enabled. */
	public boolean isVertexTransformationEnabled ();

	/** Sets whether vertex transformation is enabled. */
	public void setVertexTransformationEnabled (boolean enabled);

	/** Increases the size of the backing vertices array to accommodate the specified number of additional vertices. Useful before
	 * adding many vertices to avoid multiple backing array resizes.
	 * @param numVertices The number of vertices you are about to add */
	public void ensureVertices (int numVertices);

	/** Increases the size of the backing indices array to accommodate the specified number of additional indices. Useful before
	 * adding many indices to avoid multiple backing array resizes.
	 * @param numIndices The number of indices you are about to add */
	public void ensureIndices (int numIndices);

	/** Increases the size of the backing vertices and indices arrays to accommodate the specified number of additional vertices and
	 * indices. Useful before adding many vertices and indices to avoid multiple backing array resizes.
	 * @param numVertices The number of vertices you are about to add
	 * @param numIndices The number of indices you are about to add */
	public void ensureCapacity (int numVertices, int numIndices);

	/** Increases the size of the backing indices array to accommodate the specified number of additional triangles. Useful before
	 * adding many triangles using {@link #triangle(short, short, short)} to avoid multiple backing array resizes. The actual
	 * number of indices accounted for depends on the primitive type (see {@link #getPrimitiveType()}).
	 * @param numTriangles The number of triangles you are about to add */
	public void ensureTriangleIndices (int numTriangles);

	/** Increases the size of the backing indices array to accommodate the specified number of additional rectangles. Useful before
	 * adding many rectangles using {@link #rect(short, short, short, short)} to avoid multiple backing array resizes.
	 * @param numRectangles The number of rectangles you are about to add */
	public void ensureRectangleIndices (int numRectangles);

	/** Add one or more vertices, returns the index of the last vertex added. The length of values must a power of the vertex size. */
	public short vertex (final float... values);

	/** Add a vertex, returns the index. Null values are allowed. Use {@link #getAttributes} to check which values are available. */
	public short vertex (Vector3 pos, Vector3 nor, Color col, Vector2 uv);

	/** Add a vertex, returns the index. Use {@link #getAttributes} to check which values are available. */
	public short vertex (final VertexInfo info);

	/** @return The index of the last added vertex. */
	public short lastIndex ();

	/** Add an index, MeshPartBuilder expects all meshes to be indexed. */
	public void index (final short value);

	/** Add multiple indices, MeshPartBuilder expects all meshes to be indexed. */
	public void index (short value1, short value2);

	/** Add multiple indices, MeshPartBuilder expects all meshes to be indexed. */
	public void index (short value1, short value2, short value3);

	/** Add multiple indices, MeshPartBuilder expects all meshes to be indexed. */
	public void index (short value1, short value2, short value3, short value4);

	/** Add multiple indices, MeshPartBuilder expects all meshes to be indexed. */
	public void index (short value1, short value2, short value3, short value4, short value5, short value6);

	/** Add multiple indices, MeshPartBuilder expects all meshes to be indexed. */
	public void index (short value1, short value2, short value3, short value4, short value5, short value6, short value7,
		short value8);

	/** Add a line by indices. Requires GL_LINES primitive type. */
	public void line (short index1, short index2);

	/** Add a line. Requires GL_LINES primitive type. */
	public void line (VertexInfo p1, VertexInfo p2);

	/** Add a line. Requires GL_LINES primitive type. */
	public void line (Vector3 p1, Vector3 p2);

	/** Add a line. Requires GL_LINES primitive type. */
	public void line (float x1, float y1, float z1, float x2, float y2, float z2);

	/** Add a line. Requires GL_LINES primitive type. */
	public void line (Vector3 p1, Color c1, Vector3 p2, Color c2);

	/** Add a triangle by indices. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public void triangle (short index1, short index2, short index3);

	/** Add a triangle. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public void triangle (VertexInfo p1, VertexInfo p2, VertexInfo p3);

	/** Add a triangle. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public void triangle (Vector3 p1, Vector3 p2, Vector3 p3);

	/** Add a triangle. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public void triangle (Vector3 p1, Color c1, Vector3 p2, Color c2, Vector3 p3, Color c3);

	/** Add a rectangle by indices. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public void rect (short corner00, short corner10, short corner11, short corner01);

	/** Add a rectangle. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public void rect (VertexInfo corner00, VertexInfo corner10, VertexInfo corner11, VertexInfo corner01);

	/** Add a rectangle. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public void rect (Vector3 corner00, Vector3 corner10, Vector3 corner11, Vector3 corner01, Vector3 normal);

	/** Add a rectangle Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public void rect (float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11,
		float x01, float y01, float z01, float normalX, float normalY, float normalZ);

	/** Copies a mesh to the mesh (part) currently being build.
	 * @param mesh The mesh to copy, must have the same vertex attributes and must be indexed. */
	public void addMesh (Mesh mesh);

	/** Copies a MeshPart to the mesh (part) currently being build.
	 * @param meshpart The MeshPart to copy, must have the same vertex attributes, primitive type and must be indexed. */
	public void addMesh (MeshPart meshpart);

	/** Copies a (part of a) mesh to the mesh (part) currently being build.
	 * @param mesh The mesh to (partly) copy, must have the same vertex attributes and must be indexed.
	 * @param indexOffset The zero-based offset of the first index of the part of the mesh to copy.
	 * @param numIndices The number of indices of the part of the mesh to copy. */
	public void addMesh (Mesh mesh, int indexOffset, int numIndices);

	/** Copies a mesh to the mesh (part) currently being build. The entire vertices array is added, even if some of the vertices are
	 * not indexed by the indices array. If you want to add only the vertices that are actually indexed, then use the
	 * {@link #addMesh(float[], short[], int, int)} method instead.
	 * @param vertices The vertices to copy, must be in the same vertex layout as the mesh being build.
	 * @param indices Array containing the indices to copy, each index should be valid in the vertices array. */
	public void addMesh (float[] vertices, short[] indices);

	/** Copies a (part of a) mesh to the mesh (part) currently being build.
	 * @param vertices The vertices to (partly) copy, must be in the same vertex layout as the mesh being build.
	 * @param indices Array containing the indices to (partly) copy, each index should be valid in the vertices array.
	 * @param indexOffset The zero-based offset of the first index of the part of indices array to copy.
	 * @param numIndices The number of indices of the part of the indices array to copy. */
	public void addMesh (float[] vertices, short[] indices, int indexOffset, int numIndices);

	/** Class that contains all vertex information the builder can use.
	 * @author Xoppa */
	public static class VertexInfo implements Poolable {
		public final Vector3 position = new Vector3();
		public boolean hasPosition;
		public final Vector3 normal = new Vector3(0, 1, 0);
		public boolean hasNormal;
		public final Color color = new Color(1, 1, 1, 1);
		public boolean hasColor;
		public final Vector2 uv = new Vector2();
		public boolean hasUV;

		@Override
		public void reset () {
			position.set(0, 0, 0);
			normal.set(0, 1, 0);
			color.set(1, 1, 1, 1);
			uv.set(0, 0);
		}

		public VertexInfo set (Vector3 pos, Vector3 nor, Color col, Vector2 uv) {
			reset();
			hasPosition = pos != null;
			if (hasPosition) position.set(pos);
			hasNormal = nor != null;
			if (hasNormal) normal.set(nor);
			hasColor = col != null;
			if (hasColor) color.set(col);
			hasUV = uv != null;
			if (hasUV) this.uv.set(uv);
			return this;
		}

		public VertexInfo set (final VertexInfo other) {
			if (other == null) return set(null, null, null, null);
			hasPosition = other.hasPosition;
			position.set(other.position);
			hasNormal = other.hasNormal;
			normal.set(other.normal);
			hasColor = other.hasColor;
			color.set(other.color);
			hasUV = other.hasUV;
			uv.set(other.uv);
			return this;
		}

		public VertexInfo setPos (float x, float y, float z) {
			position.set(x, y, z);
			hasPosition = true;
			return this;
		}

		public VertexInfo setPos (Vector3 pos) {
			hasPosition = pos != null;
			if (hasPosition) position.set(pos);
			return this;
		}

		public VertexInfo setNor (float x, float y, float z) {
			normal.set(x, y, z);
			hasNormal = true;
			return this;
		}

		public VertexInfo setNor (Vector3 nor) {
			hasNormal = nor != null;
			if (hasNormal) normal.set(nor);
			return this;
		}

		public VertexInfo setCol (float r, float g, float b, float a) {
			color.set(r, g, b, a);
			hasColor = true;
			return this;
		}

		public VertexInfo setCol (Color col) {
			hasColor = col != null;
			if (hasColor) color.set(col);
			return this;
		}

		public VertexInfo setUV (float u, float v) {
			uv.set(u, v);
			hasUV = true;
			return this;
		}

		public VertexInfo setUV (Vector2 uv) {
			hasUV = uv != null;
			if (hasUV) this.uv.set(uv);
			return this;
		}

		public VertexInfo lerp (final VertexInfo target, float alpha) {
			if (hasPosition && target.hasPosition) position.lerp(target.position, alpha);
			if (hasNormal && target.hasNormal) normal.lerp(target.normal, alpha);
			if (hasColor && target.hasColor) color.lerp(target.color, alpha);
			if (hasUV && target.hasUV) uv.lerp(target.uv, alpha);
			return this;
		}
	}

	// TODO: The following methods are deprecated and will be removed in a future release


	/** @deprecated use PatchShapeBuilder.build instead. */
	@Deprecated
	public void patch (VertexInfo corner00, VertexInfo corner10, VertexInfo corner11, VertexInfo corner01, int divisionsU,
		int divisionsV);

	/** @deprecated use PatchShapeBuilder.build instead. */
	@Deprecated
	public void patch (Vector3 corner00, Vector3 corner10, Vector3 corner11, Vector3 corner01, Vector3 normal, int divisionsU,
		int divisionsV);

	/** @deprecated use PatchShapeBuilder.build instead. */
	@Deprecated
	public void patch (float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11,
		float x01, float y01, float z01, float normalX, float normalY, float normalZ, int divisionsU, int divisionsV);

	/** @deprecated use BoxShapeBuilder.build instead. */
	@Deprecated
	public void box (VertexInfo corner000, VertexInfo corner010, VertexInfo corner100, VertexInfo corner110, VertexInfo corner001,
		VertexInfo corner011, VertexInfo corner101, VertexInfo corner111);

	/** @deprecated use BoxShapeBuilder.build instead. */
	@Deprecated
	public void box (Vector3 corner000, Vector3 corner010, Vector3 corner100, Vector3 corner110, Vector3 corner001,
		Vector3 corner011, Vector3 corner101, Vector3 corner111);

	/** @deprecated use BoxShapeBuilder.build instead. */
	@Deprecated
	public void box (Matrix4 transform);

	/** @deprecated use BoxShapeBuilder.build instead. */
	@Deprecated
	public void box (float width, float height, float depth);

	/** @deprecated use BoxShapeBuilder.build instead. */
	@Deprecated
	public void box (float x, float y, float z, float width, float height, float depth);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void circle (float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void circle (float radius, int divisions, final Vector3 center, final Vector3 normal);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void circle (float radius, int divisions, final Vector3 center, final Vector3 normal, final Vector3 tangent,
		final Vector3 binormal);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void circle (float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY, float binormalZ);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void circle (float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ, float angleFrom, float angleTo);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void circle (float radius, int divisions, final Vector3 center, final Vector3 normal, float angleFrom, float angleTo);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void circle (float radius, int divisions, final Vector3 center, final Vector3 normal, final Vector3 tangent,
		final Vector3 binormal, float angleFrom, float angleTo);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void circle (float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY, float binormalZ,
		float angleFrom, float angleTo);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void ellipse (float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX,
		float normalY, float normalZ);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void ellipse (float width, float height, int divisions, final Vector3 center, final Vector3 normal);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void ellipse (float width, float height, int divisions, final Vector3 center, final Vector3 normal,
		final Vector3 tangent, final Vector3 binormal);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void ellipse (float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX,
		float normalY, float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY,
		float binormalZ);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void ellipse (float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX,
		float normalY, float normalZ, float angleFrom, float angleTo);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void ellipse (float width, float height, int divisions, final Vector3 center, final Vector3 normal, float angleFrom,
		float angleTo);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void ellipse (float width, float height, int divisions, final Vector3 center, final Vector3 normal,
		final Vector3 tangent, final Vector3 binormal, float angleFrom, float angleTo);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void ellipse (float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX,
		float normalY, float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY,
		float binormalZ, float angleFrom, float angleTo);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void ellipse (float width, float height, float innerWidth, float innerHeight, int divisions, float centerX,
		float centerY, float centerZ, float normalX, float normalY, float normalZ, float tangentX, float tangentY, float tangentZ,
		float binormalX, float binormalY, float binormalZ, float angleFrom, float angleTo);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void ellipse (float width, float height, float innerWidth, float innerHeight, int divisions, float centerX,
		float centerY, float centerZ, float normalX, float normalY, float normalZ, float angleFrom, float angleTo);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void ellipse (float width, float height, float innerWidth, float innerHeight, int divisions, float centerX,
		float centerY, float centerZ, float normalX, float normalY, float normalZ);

	/** @deprecated Use EllipseShapeBuilder.build instead. */
	@Deprecated
	public void ellipse (float width, float height, float innerWidth, float innerHeight, int divisions, Vector3 center,
		Vector3 normal);
	
	/** @deprecated Use CylinderShapeBuilder.build instead. */
	@Deprecated
	public void cylinder (float width, float height, float depth, int divisions);

	/** @deprecated Use CylinderShapeBuilder.build instead. */
	@Deprecated
	public void cylinder (float width, float height, float depth, int divisions, float angleFrom, float angleTo);

	/** @deprecated Use CylinderShapeBuilder.build instead. */
	@Deprecated
	public void cylinder (float width, float height, float depth, int divisions, float angleFrom, float angleTo, boolean close);

	/** @deprecated Use ConeShapeBuilder.build instead. */
	@Deprecated
	public void cone (float width, float height, float depth, int divisions);

	/** @deprecated Use ConeShapeBuilder.build instead. */
	@Deprecated
	public void cone (float width, float height, float depth, int divisions, float angleFrom, float angleTo);

	/** @deprecated Use SphereShapeBuilder.build instead. */
	@Deprecated
	public void sphere (float width, float height, float depth, int divisionsU, int divisionsV);

	/** @deprecated Use SphereShapeBuilder.build instead. */
	@Deprecated
	public void sphere (final Matrix4 transform, float width, float height, float depth, int divisionsU, int divisionsV);

	/** @deprecated Use SphereShapeBuilder.build instead. */
	@Deprecated
	public void sphere (float width, float height, float depth, int divisionsU, int divisionsV, float angleUFrom, float angleUTo,
		float angleVFrom, float angleVTo);

	/** @deprecated Use SphereShapeBuilder.build instead. */
	@Deprecated
	public void sphere (final Matrix4 transform, float width, float height, float depth, int divisionsU, int divisionsV,
		float angleUFrom, float angleUTo, float angleVFrom, float angleVTo);

	/** @deprecated Use CapsuleShapeBuilder.build instead. */
	@Deprecated
	public void capsule (float radius, float height, int divisions);
	
	/** @deprecated Use ArrowShapeBuilder.build instead. */
	@Deprecated
	public void arrow (float x1, float y1, float z1, float x2, float y2, float z2, float capLength, float stemThickness,
		int divisions);
}
