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

/** A line segment (edge) shape. These can be connected in chains or loops to other edge shapes. The connectivity information is
 * used to ensure correct contact normals. */
public class EdgeShape extends Shape {
	// @off
	/*JNI
#include <box2d/box2d.h>
	 */ // @on

	public EdgeShape () {
		addr = newEdgeShape();
	}

	private native long newEdgeShape (); /*
		// @off
		return (jlong)(new b2EdgeShape());
	*/ // @on

	EdgeShape (long addr) {
		this.addr = addr;
	}

	/** Set this as an isolated edge. */
	public void setTwoSided (Vector2 v1, Vector2 v2) {
		setTwoSided(v1.x, v1.y, v2.x, v2.y);
	}

	/** @deprecated use {@link #setTwoSided} instead */
	@Deprecated
	public void set (Vector2 v1, Vector2 v2) {
		setTwoSided(v1, v2);
	}

	/** Set this as an isolated edge. Collision is two-sided. */
	public void setTwoSided (float v1X, float v1Y, float v2X, float v2Y) {
		jniSetTwoSided(addr, v1X, v1Y, v2X, v2Y);
	}

	private native void jniSetTwoSided (long addr, float v1x, float v1y, float v2x, float v2y); /*
		// @off
		b2EdgeShape* edge = (b2EdgeShape*)addr;
		edge->SetTwoSided(b2Vec2(v1x, v1y), b2Vec2(v2x, v2y));
	*/ // @on

	/** Set this as a part of a sequence. Vertex v0 precedes the edge and vertex v3 follows. These extra vertices are used to
	 * provide smooth movement across junctions. This also makes the collision one-sided. The edge normal points to the right
	 * looking from v1 to v2. */
	public void setOneSided (Vector2 v0, Vector2 v1, Vector2 v2, Vector2 v3) {
		setOneSided(v0.x, v0.y, v1.x, v1.y, v2.x, v2.y, v3.x, v3.y);
	}

	/** Set this as an isolated edge. */
	public void setOneSided (float v0X, float v0Y, float v1X, float v1Y, float v2X, float v2Y, float v3X, float v3Y) {
		jniSetOneSided(addr, v0X, v0Y, v1X, v1Y, v2X, v2Y, v3X, v3Y);
	}

	private native void jniSetOneSided (long addr, float v0x, float v0y, float v1x, float v1y, float v2x, float v2y, float v3x,
		float v3y); /*
		// @off
		b2EdgeShape* edge = (b2EdgeShape*)addr;
		edge->SetOneSided(b2Vec2(v0x, v0y), b2Vec2(v1x, v1y), b2Vec2(v2x, v2y), b2Vec2(v3x, v3y));
	*/ // @on

	static final float[] vertex = new float[2];

	public void getVertex1 (Vector2 vec) {
		jniGetVertex1(addr, vertex);
		vec.x = vertex[0];
		vec.y = vertex[1];
	}

	private native void jniGetVertex1 (long addr, float[] vertex); /*
		// @off
		b2EdgeShape* edge = (b2EdgeShape*)addr;
		vertex[0] = edge->m_vertex1.x;
		vertex[1] = edge->m_vertex1.y;
	*/ // @on

	public void getVertex2 (Vector2 vec) {
		jniGetVertex2(addr, vertex);
		vec.x = vertex[0];
		vec.y = vertex[1];
	}

	private native void jniGetVertex2 (long addr, float[] vertex); /*
		// @off
		b2EdgeShape* edge = (b2EdgeShape*)addr;
		vertex[0] = edge->m_vertex2.x;
		vertex[1] = edge->m_vertex2.y;
	*/ // @on

	public void getVertex0 (Vector2 vec) {
		jniGetVertex0(addr, vertex);
		vec.x = vertex[0];
		vec.y = vertex[1];
	}

	private native void jniGetVertex0 (long addr, float[] vertex); /*
		// @off
		b2EdgeShape* edge = (b2EdgeShape*)addr;
		vertex[0] = edge->m_vertex0.x;
		vertex[1] = edge->m_vertex0.y;
	*/ // @on

	public void setVertex0 (Vector2 vec) {
		jniSetVertex0(addr, vec.x, vec.y);
	}

	public void setVertex0 (float x, float y) {
		jniSetVertex0(addr, x, y);
	}

	private native void jniSetVertex0 (long addr, float x, float y); /*
		// @off
		b2EdgeShape* edge = (b2EdgeShape*)addr;
		edge->m_vertex0.x = x;
		edge->m_vertex0.y = y;
	*/ // @on

	public void getVertex3 (Vector2 vec) {
		jniGetVertex3(addr, vertex);
		vec.x = vertex[0];
		vec.y = vertex[1];
	}

	private native void jniGetVertex3 (long addr, float[] vertex); /*
		// @off
		b2EdgeShape* edge = (b2EdgeShape*)addr;
		vertex[0] = edge->m_vertex3.x;
		vertex[1] = edge->m_vertex3.y;
	*/ // @on

	public void setVertex3 (Vector2 vec) {
		jniSetVertex3(addr, vec.x, vec.y);
	}

	public void setVertex3 (float x, float y) {
		jniSetVertex3(addr, x, y);
	}

	private native void jniSetVertex3 (long addr, float x, float y); /*
		// @off
		b2EdgeShape* edge = (b2EdgeShape*)addr;
		edge->m_vertex3.x = x;
		edge->m_vertex3.y = y;
	*/ // @on

	@Override
	public Type getType () {
		return Type.Edge;
	}

}
