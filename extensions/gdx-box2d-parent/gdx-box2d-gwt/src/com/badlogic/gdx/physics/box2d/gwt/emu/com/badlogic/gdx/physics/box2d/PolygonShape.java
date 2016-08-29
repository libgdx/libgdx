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

import org.jbox2d.common.Vec2;

import com.badlogic.gdx.math.Vector2;

public class PolygonShape extends Shape {
	org.jbox2d.collision.shapes.PolygonShape shape;

	/** Constructs a new polygon */
	public PolygonShape () {
		super(new org.jbox2d.collision.shapes.PolygonShape());
		shape = (org.jbox2d.collision.shapes.PolygonShape)super.shape;
	}

	public PolygonShape (org.jbox2d.collision.shapes.PolygonShape shape) {
		super(shape);
		this.shape = shape;
	}

	/** {@inheritDoc} */
	@Override
	public Type getType () {
		return Type.Polygon;
	}

	/** Copy vertices. This assumes the vertices define a convex polygon. It is assumed that the exterior is the the right of each
	 * edge. */
	public void set (Vector2[] vertices) {
		Vec2[] v = new org.jbox2d.common.Vec2[vertices.length];
		for (int i = 0; i < v.length; i++) {
			v[i] = new Vec2(vertices[i].x, vertices[i].y);
		}
		shape.set(v, v.length);
	}

	/** Copy vertices from the given float array. It is assumed the vertices are in x,y order and define a convex polygon. It is
	 * assumed that the exterior is the the right of each edge. */
	public void set (float[] vertices) {
		set(vertices, 0, vertices.length);
	}

	/** Copy vertices from the given float array, taking into account the offset and length. It is assumed the vertices are in x,y
	 * order and define a convex polygon. It is assumed that the exterior is the the right of each edge. */
	public void set (float[] vertices, int offset, int len) {
		Vec2[] v = new org.jbox2d.common.Vec2[len / 2];
		for (int i = 0; i < v.length; i++) {
			v[i] = new Vec2(vertices[i * 2], vertices[i * 2 + 1]);
		}
		shape.set(v, v.length);
	}

	/** Build vertices to represent an axis-aligned box.
	 * @param hx the half-width.
	 * @param hy the half-height. */
	public void setAsBox (float hx, float hy) {
		shape.setAsBox(hx, hy);
	}

	final Vec2 tmp = new Vec2();

	/** Build vertices to represent an oriented box.
	 * @param hx the half-width.
	 * @param hy the half-height.
	 * @param center the center of the box in local coordinates.
	 * @param angle the rotation in radians of the box in local coordinates. */
	public void setAsBox (float hx, float hy, Vector2 center, float angle) {
		tmp.set(center.x, center.y);
		shape.setAsBox(hx, hy, tmp, angle);
	}

	/** @return the number of vertices */
	public int getVertexCount () {
		return shape.m_count;
	}

	@Override
	public float getRadius () {
		return shape.getRadius();
	}

	@Override
	public void setRadius (float radius) {
		shape.setRadius(radius);
	}

	@Override
	public int getChildCount () {
		return shape.getChildCount();
	}

	/** Returns the vertex at the given position.
	 * @param index the index of the vertex 0 <= index < getVertexCount( )
	 * @param vertex vertex */
	public void getVertex (int index, Vector2 vertex) {
		Vec2 v = shape.m_vertices[index];
		vertex.set(v.x, v.y);
	}
}
