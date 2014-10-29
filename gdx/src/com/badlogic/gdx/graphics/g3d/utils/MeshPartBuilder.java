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
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.ShortArray;

public interface MeshPartBuilder {

	/** These flags determine which faces are built and which are ignored on shapes where applicable. */
	public static final class IgnoreFaces {
		public static final int Top = 1;
		public static final int Bottom = 2;
	}

	/** @return The {@link MeshPart} currently building. */
	public MeshPart getMeshPart ();

	/** @return The {@link VertexAttributes} available for building. */
	public VertexAttributes getAttributes ();

	/** Set the color used if no vertex color is provided, or null to not use a default color. */
	public void setColor (final Color color);

	/** Set the color used if no vertex color is provided. */
	public void setColor (float r, float g, float b, float a);

	/** Set the color gradient specifying vertex color changes along the axis of a sweep or extrude
	 * 
	 * @param colors array of float triples each describing an rgb color
	 * @param followOffset whether the color is determined along the path offset or absolute across the entire path
	 */
	public void setGradientColor (float[] colors, boolean followOffset);

	/** Set the profile shape for extrudes and sweeps. The shape should be defined counter-clockwise, unless an inside-out geometry is desired.
	 * 
	 * @param shape the profile to extrude, an array of float doubles each describing a shape vertex, assumed counter-clockwise
	 * @param continuous whether the profile shape should be treated as continuous, with the last vertex connected to the first by an edge
	 * @param smooth whether to break at each profile vertex such that the resulting sides are facetted rather than smooth across each profile edge face
	 */
	public void setProfileShape (float[] shape, boolean continuous, boolean smooth);

	/** Set the profile shape for extrudes and sweeps. The shape should be defined counter-clockwise, unless an inside-out geometry is desired.
	 * 
	 * @param shape the profile to extrude, an array of float doubles each describing a shape vertex, assumed counter-clockwise
	 * @param continuous whether the profile shape should be treated as continuous, with the last vertex connected to the first by faces
	 * @param smooth whether to break at each profile vertex such that the resulting sides are facetted rather than smooth across each profile edge face
	 */
	public void setProfileShape (FloatArray shape, boolean continuous, boolean smooth);

	/** Set the profile shape for extrudes and sweeps. The shape should be defined counter-clockwise, unless an inside-out geometry is desired.
	 * 
	 * @param shape the profile to extrude, an array of float doubles each describing a shape vertex, assumed counter-clockwise
	 * @param offset offset into profile array
	 * @param size number of floats to use from profile array
	 * @param continuous whether the profile shape should be treated as continuous, with the last vertex connected to the first by faces
	 * @param smooth whether to break at each profile vertex such that the resulting sides are facetted rather than smooth across each profile edge face
	 */
	public void setProfileShape (float[] shape, int offset, int size, boolean continuous, boolean smooth);

	/** Set the scale interpolation function which scales a shape's profile along the axis of a sweep (Note use startScale = 0, endScale = 1 to have the interpolation
	 * entirely decide the scale)
	 * 
	 * @param func the interpolation function, see {@link Interpolation}
	 * @param startScale the scale to apply at the start of the path
	 * @param endScale the scale to apply at the end of the path
	 * @param followOffset whether the scale interpolation is determined along the path offset or absolute across the entire path
	 */
	public void setScaleInterpolation(Interpolation func, float startScale, float endScale, boolean followOffset);

	/** Set which faces are built and which are ignored on successive shapes, where semantics of shape apply
	 * 
	 * @param faces bit flags (see {@link IgnoreFaces}) determining set of faces to ignore when building, or 0 to reset to all faces
	 */
	public void setIgnoreFaces(int faces);

	/** Set range of texture coordinates used (default is 0,0,1,1). */
	public void setUVRange (float u1, float v1, float u2, float v2);

	/** Set range of texture coordinates from the specified TextureRegion. */
	public void setUVRange (TextureRegion r);

	/** Set range of texture coordinates used (default is 0,0,1,1). */
	public void setUVRange2 (float u1, float v1, float u2, float v2);

	/** Set range of texture coordinates from the specified TextureRegion. */
	public void setUVRange2 (TextureRegion r);

	/** Resets builder state to defaults */
	public void resetDefaults ();

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

	/** Add a rectangle. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public void patch (VertexInfo corner00, VertexInfo corner10, VertexInfo corner11, VertexInfo corner01, int divisionsU,
		int divisionsV);

	/** Add a rectangle. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public void patch (Vector3 corner00, Vector3 corner10, Vector3 corner11, Vector3 corner01, Vector3 normal, int divisionsU,
		int divisionsV);

	/** Add a rectangle. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public void patch (float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11,
		float x01, float y01, float z01, float normalX, float normalY, float normalZ, int divisionsU, int divisionsV);

	/** Add a box. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public void box (VertexInfo corner000, VertexInfo corner010, VertexInfo corner100, VertexInfo corner110, VertexInfo corner001,
		VertexInfo corner011, VertexInfo corner101, VertexInfo corner111);

	/** Add a box. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public void box (Vector3 corner000, Vector3 corner010, Vector3 corner100, Vector3 corner110, Vector3 corner001,
		Vector3 corner011, Vector3 corner101, Vector3 corner111);

	/** Add a box given the matrix. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public void box (Matrix4 transform);

	/** Add a box with the specified dimensions. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public void box (float width, float height, float depth);

	/** Add a box at the specified location, with the specified dimensions */
	public void box (float x, float y, float z, float width, float height, float depth);

	/** Add a circle */
	public void circle (float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ);

	/** Add a circle */
	public void circle (float radius, int divisions, final Vector3 center, final Vector3 normal);

	/** Add a circle */
	public void circle (float radius, int divisions, final Vector3 center, final Vector3 normal, final Vector3 tangent,
		final Vector3 binormal);

	/** Add a circle */
	public void circle (float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY, float binormalZ);

	/** Add a circle */
	public void circle (float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ, float angleFrom, float angleTo);

	/** Add a circle */
	public void circle (float radius, int divisions, final Vector3 center, final Vector3 normal, float angleFrom, float angleTo);

	/** Add a circle */
	public void circle (float radius, int divisions, final Vector3 center, final Vector3 normal, final Vector3 tangent,
		final Vector3 binormal, float angleFrom, float angleTo);

	/** Add a circle */
	public void circle (float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY, float binormalZ,
		float angleFrom, float angleTo);

	/** Add a circle */
	public void ellipse (float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX,
		float normalY, float normalZ);

	/** Add a circle */
	public void ellipse (float width, float height, int divisions, final Vector3 center, final Vector3 normal);

	/** Add a circle */
	public void ellipse (float width, float height, int divisions, final Vector3 center, final Vector3 normal,
		final Vector3 tangent, final Vector3 binormal);

	/** Add a circle */
	public void ellipse (float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX,
		float normalY, float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY,
		float binormalZ);

	/** Add a circle */
	public void ellipse (float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX,
		float normalY, float normalZ, float angleFrom, float angleTo);

	/** Add a circle */
	public void ellipse (float width, float height, int divisions, final Vector3 center, final Vector3 normal, float angleFrom,
		float angleTo);

	/** Add a circle */
	public void ellipse (float width, float height, int divisions, final Vector3 center, final Vector3 normal,
		final Vector3 tangent, final Vector3 binormal, float angleFrom, float angleTo);

	/** Add a circle */
	public void ellipse (float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX,
		float normalY, float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY,
		float binormalZ, float angleFrom, float angleTo);

	/** Add an ellipse */
	public void ellipse (float width, float height, float innerWidth, float innerHeight, int divisions, float centerX,
		float centerY, float centerZ, float normalX, float normalY, float normalZ, float tangentX, float tangentY, float tangentZ,
		float binormalX, float binormalY, float binormalZ, float angleFrom, float angleTo);

	/** Add an ellipse */
	public void ellipse (float width, float height, float innerWidth, float innerHeight, int divisions, float centerX,
		float centerY, float centerZ, float normalX, float normalY, float normalZ, float angleFrom, float angleTo);

	/** Add an ellipse */
	public void ellipse (float width, float height, float innerWidth, float innerHeight, int divisions, float centerX,
		float centerY, float centerZ, float normalX, float normalY, float normalZ);

	/** Add an ellipse */
	public void ellipse (float width, float height, float innerWidth, float innerHeight, int divisions, Vector3 center,
		Vector3 normal);

	/** Add a cylinder */
	public void cylinder (float width, float height, float depth, int divisions);

	/** Add a cylinder */
	public void cylinder (float width, float height, float depth, int divisions, float angleFrom, float angleTo);

	/** Add a cylinder */
	public void cylinder (float width, float height, float depth, int divisions, float angleFrom, float angleTo, boolean close);

	/** Add a cone */
	public void cone (float width, float height, float depth, int divisions);

	/** Add a cone */
	public void cone (float width, float height, float depth, int divisions, float angleFrom, float angleTo);

	/** Add a sphere */
	public void sphere (float width, float height, float depth, int divisionsU, int divisionsV);

	/** Add a sphere */
	public void sphere (final Matrix4 transform, float width, float height, float depth, int divisionsU, int divisionsV);

	/** Add a sphere */
	public void sphere (float width, float height, float depth, int divisionsU, int divisionsV, float angleUFrom, float angleUTo,
		float angleVFrom, float angleVTo);

	/** Add a sphere */
	public void sphere (final Matrix4 transform, float width, float height, float depth, int divisionsU, int divisionsV,
		float angleUFrom, float angleUTo, float angleVFrom, float angleVTo);

	/** Add a capsule */
	public void capsule (float radius, float height, int divisions);
	
	/** Add an arrow 
	 * @param x1 source x
	 * @param y1 source y
	 * @param z1 source z
	 * @param x2 destination x
	 * @param y2 destination y
	 * @param z2 destination z
	 * @param capLength is the height of the cap in percentage, must be in (0,1) 
	 * @param stemThickness is the percentage of stem diameter compared to cap diameter, must be in (0,1]
	 * @param divisions the amount of vertices used to generate the cap and stem ellipsoidal bases */
	public void arrow (float x1, float y1, float z1, float x2, float y2, float z2, float capLength, float stemThickness, int divisions);

	/** Add vertices from another mesh, will call begin with the supplied mesh's attributes if begin was not yet called
	 * 
	 * @param mesh the mesh to add, must have the same vertex attributes as the current mesh part
	 */
	public void mesh (Mesh mesh);

	/** Add vertices from another mesh, will call begin with the supplied mesh's attributes if begin was not yet called
	 * 
	 * @param mesh the mesh to add, must have the same vertex attributes as the current mesh part
	 * @param numVerts number of vertices to copy from this mesh
	 * @param numIndices number of indices to copy from this mesh
	 */
	public void mesh (Mesh mesh, int numVerts, int numIndices);

	/** Add vertices from a FloatArray, assumed to represent the same vertex attributes as the current mesh part
	 * 
	 * @param srcVertices
	 * @param srcIndices
	 */
	public void mesh (FloatArray srcVertices, ShortArray srcIndices);

	/** Add vertices from a float array, assumed to represent the same vertex attributes as the current mesh part
	 * 
	 * @param srcVertices the vertices to add, must have the same vertex attributes as the current mesh part
	 * @param srcIndices the indices representing the mesh in the float array
	 */
	public void mesh (float[] srcVertices, short[] srcIndices);

	/** Add vertices from a float array, assumed to represent the same vertex attributes as the current mesh part
	 * 
	 * @param srcVertices
	 * @param numVerts number of vertices to add (note this number is in vertices not floats)
	 * @param srcIndices
	 * @param numIndices
	 */
	public void mesh (float[] srcVertices, int numVerts, short[] srcIndices, int numIndices);

	/** Add an extrude of the currently set profile shape
	 * 
	 * @param distance the distance along the z-axis to extrude
	 * @param tileU texture tiling perpendicular to path (set to zero for flat mapping)
	 * @param tileV texture tiling perpendicular to path (set to zero for flat mapping)
	 */
	public void extrude (float distance, float tileU, float tileV);

	/** Add an extrude of the currently set profile shape
	 * 
	 * @param distance the distance along the z-axis to extrude
	 * @param steps the number of segments to create along the extrude (generally 1 for simple extrudes, but more may be desired for color or scale transitions)
	 * @param tileU texture tiling perpendicular to path (set to zero for flat mapping)
	 * @param tileV texture tiling perpendicular to path (set to zero for flat mapping)
	 */
	public void extrude (float distance, int steps, float tileU, float tileV);

	/** Add a sweep of the currently set profile shape
	 * 
	 * @param path the path to sweep the profile along, an array of float triples each describing a path vertex
	 * @param smooth if false, the normals of each segment along the curve are broken resulting in a facetted look (this also doubles the vertex count along the path)
	 * @param continuous whether the path should be treated as continuous. Note that unlike the profile shape, the first point should also be
	 *  duplicated at the end of the path array as this parameter only affects evaluation of the tangent to smooth the meeting ends
	 * @param tileU texture tiling perpendicular to path (set to zero for flat mapping)
	 * @param tileV texture tiling perpendicular to path (set to zero for flat mapping)
	 */
	public void sweep (Vector3[] path, boolean smooth, boolean continuous, float tileU, float tileV);

	/** Add a sweep of the currently set profile shape
	 * 
	 * @param path the path to sweep the profile along
	 * @param smooth if false, the normals of each segment along the curve are broken resulting in a facetted look (this also doubles the vertex count along the path)
	 * @param steps the number of steps to create along the path
	 * @param tileU texture tiling perpendicular to path (set to zero for flat mapping)
	 * @param tileV texture tiling perpendicular to path (set to zero for flat mapping)
	 */
	public void sweep (Path path, boolean smooth, int steps, float tileU, float tileV);

	/** Add a sweep of the currently set profile shape
	 * 
	 * @param path the path to sweep the profile along
	 * @param smooth if false, the normals of each segment along the curve are broken resulting in a facetted look (this also doubles the vertex count along the path)
	 * @param startT position along path to end sampling
	 * @param endT position along path to end sampling
	 * @param steps the number of steps to create along the path
	 * @param tileU texture tiling perpendicular to path (set to zero for flat mapping)
	 * @param tileV texture tiling perpendicular to path (set to zero for flat mapping)
	 */
	public void sweep (Path path, boolean smooth, float startT, float endT, int steps, float tileU, float tileV);

	/** Get the current vertex transformation matrix. */
	public Matrix4 getVertexTransform (Matrix4 out);

	/** Set the current vertex transformation matrix and enables vertex transformation. */
	public void setVertexTransform (Matrix4 transform);

	/** Indicates whether vertex transformation is enabled. */
	public boolean isVertexTransformationEnabled ();

	/** Sets whether vertex transformation is enabled. */
	public void setVertexTransformationEnabled (boolean enabled);

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
			if ((hasPosition = pos != null) == true) position.set(pos);
			if ((hasNormal = nor != null) == true) normal.set(nor);
			if ((hasColor = col != null) == true) color.set(col);
			if ((hasUV = uv != null) == true) this.uv.set(uv);
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
			if ((hasPosition = pos != null) == true) position.set(pos);
			return this;
		}

		public VertexInfo setNor (float x, float y, float z) {
			normal.set(x, y, z);
			hasNormal = true;
			return this;
		}

		public VertexInfo setNor (Vector3 nor) {
			if ((hasNormal = nor != null) == true) normal.set(nor);
			return this;
		}

		public VertexInfo setCol (float r, float g, float b, float a) {
			color.set(r, g, b, a);
			hasColor = true;
			return this;
		}

		public VertexInfo setCol (Color col) {
			if ((hasColor = col != null) == true) color.set(col);
			return this;
		}

		public VertexInfo setUV (float u, float v) {
			uv.set(u, v);
			hasUV = true;
			return this;
		}

		public VertexInfo setUV (Vector2 uv) {
			if ((hasUV = uv != null) == true) this.uv.set(uv);
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
}
