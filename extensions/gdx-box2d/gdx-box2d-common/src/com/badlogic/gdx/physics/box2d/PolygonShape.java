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

package com.badlogic.gdx.physics.box2d;

import com.badlogic.gdx.math.Vector2;

public class PolygonShape extends Shape {
	// @off
	/*JNI
     #include <Box2D/Box2D.h>
	 */
	
	/** Constructs a new polygon */
	public PolygonShape () {
		addr = newPolygonShape();
	}

	protected PolygonShape (long addr) {
		this.addr = addr;
	}

	private native long newPolygonShape (); /*
		b2PolygonShape* poly = new b2PolygonShape();
		return (jlong)poly;
	*/

	/** {@inheritDoc} */
	@Override
	public Type getType () {
		return Type.Polygon;
	}

	/** Copy vertices. This assumes the vertices define a convex polygon. It is assumed that the exterior is the the right of each
	 * edge. */
	public void set (Vector2[] vertices) {
		float[] verts = new float[vertices.length * 2];
		for (int i = 0, j = 0; i < vertices.length * 2; i += 2, j++) {
			verts[i] = vertices[j].x;
			verts[i + 1] = vertices[j].y;
		}
		jniSet(addr, verts, 0, verts.length);
	}

	/** Copy vertices from the given float array. It is assumed the vertices are in x,y order and define a convex polygon. It is
	 * assumed that the exterior is the the right of each edge. */
	public void set (float[] vertices) {
		jniSet(addr, vertices, 0, vertices.length);
	}

	/** Copy vertices from the given float array, taking into account the offset and length. It is assumed the vertices are in
	 * x,y order and define a convex polygon. It is assumed that the exterior is the the right of each edge. */
	public void set (float[] vertices, int offset, int len) {
		jniSet(addr, vertices, offset, len);
	}

	private native void jniSet (long addr, float[] verts, int offset, int len); /*
		b2PolygonShape* poly = (b2PolygonShape*)addr;
		int numVertices = len / 2;
		b2Vec2* verticesOut = new b2Vec2[numVertices];
		for(int i = 0; i < numVertices; i++) { 
			verticesOut[i] = b2Vec2(verts[(i<<1) + offset], verts[(i<<1) + offset + 1]);
		}
		poly->Set(verticesOut, numVertices);
		delete[] verticesOut;
	 */
	
	/** Build vertices to represent an axis-aligned box.
	 * @param hx the half-width.
	 * @param hy the half-height. */
	public void setAsBox (float hx, float hy) {
		jniSetAsBox(addr, hx, hy);
	}

	private native void jniSetAsBox (long addr, float hx, float hy); /*
		b2PolygonShape* poly = (b2PolygonShape*)addr;
		poly->SetAsBox(hx, hy);
	*/

	/** Build vertices to represent an oriented box.
	 * @param hx the half-width.
	 * @param hy the half-height.
	 * @param center the center of the box in local coordinates.
	 * @param angle the rotation in radians of the box in local coordinates. */
	public void setAsBox (float hx, float hy, Vector2 center, float angle) {
		jniSetAsBox(addr, hx, hy, center.x, center.y, angle);
	}

	private native void jniSetAsBox (long addr, float hx, float hy, float centerX, float centerY, float angle); /*
		b2PolygonShape* poly = (b2PolygonShape*)addr;
		poly->SetAsBox( hx, hy, b2Vec2( centerX, centerY ), angle );
	*/

	/** @return the number of vertices */
	public int getVertexCount () {
		return jniGetVertexCount(addr);
	}

	private native int jniGetVertexCount (long addr); /*
		b2PolygonShape* poly = (b2PolygonShape*)addr;
		return poly->GetVertexCount();
	*/

	private static float[] verts = new float[2];

	/** Returns the vertex at the given position.
	 * @param index the index of the vertex 0 <= index < getVertexCount( )
	 * @param vertex vertex */
	public void getVertex (int index, Vector2 vertex) {
		jniGetVertex(addr, index, verts);
		vertex.x = verts[0];
		vertex.y = verts[1];
	}

	private native void jniGetVertex (long addr, int index, float[] verts); /*
		b2PolygonShape* poly = (b2PolygonShape*)addr;
		const b2Vec2 v = poly->GetVertex( index );
		verts[0] = v.x;
		verts[1] = v.y;
	*/
}
