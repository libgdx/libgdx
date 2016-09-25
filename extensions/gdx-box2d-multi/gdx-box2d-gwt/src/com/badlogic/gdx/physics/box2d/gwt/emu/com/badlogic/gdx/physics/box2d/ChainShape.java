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

public class ChainShape extends Shape {
	org.jbox2d.collision.shapes.ChainShape shape;
	boolean isLooped = false;

	public ChainShape () {
		super(new org.jbox2d.collision.shapes.ChainShape());
		shape = (org.jbox2d.collision.shapes.ChainShape)super.shape;
	}

	ChainShape (org.jbox2d.collision.shapes.ChainShape shape) {
		super(shape);
		this.shape = shape;
	}

	@Override
	public Type getType () {
		return Type.Chain;
	}

	public void createLoop (float[] vertices) {
		Vec2[] v = new Vec2[vertices.length / 2];
		for (int i = 0, vi = 0; i < vertices.length; i += 2, vi++) {
			v[vi] = new Vec2(vertices[i], vertices[i + 1]);
		}		
		shape.createLoop(v, v.length);
		isLooped = true;
	}
	
	/** Create a loop. This automatically adjusts connectivity.
	 * @param vertices an array of vertices, these are copied */
	public void createLoop (Vector2[] vertices) {
		Vec2[] v = new Vec2[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			v[i] = new Vec2(vertices[i].x, vertices[i].y);
		}		
		shape.createLoop(v, v.length);
		isLooped = true;
	}

	public void createChain (float[] vertices) {
		Vec2[] v = new Vec2[vertices.length / 2];
		for (int i = 0, vi = 0; i < vertices.length; i += 2, vi++) {
			v[vi] = new Vec2(vertices[i], vertices[i + 1]);
		}
		shape.createChain(v, v.length);
		isLooped = false;
	}
	
	/** Create a chain with isolated end vertices.
	 * @param vertices an array of vertices, these are copied */
	public void createChain (Vector2[] vertices) {
		Vec2[] v = new Vec2[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			v[i] = new Vec2(vertices[i].x, vertices[i].y);
		}
		shape.createChain(v, v.length);
		isLooped = false;
	}

	/** Establish connectivity to a vertex that precedes the first vertex. Don't call this for loops. */
	public void setPrevVertex (Vector2 prevVertex) {
		shape.setPrevVertex(new Vec2(prevVertex.x, prevVertex.y));
	}

	/** Establish connectivity to a vertex that precedes the first vertex. Don't call this for loops. */
	public void setPrevVertex (float prevVertexX, float prevVertexY) {
		shape.setPrevVertex(new Vec2(prevVertexX, prevVertexY));
	}

	/** Establish connectivity to a vertex that follows the last vertex. Don't call this for loops. */
	public void setNextVertex (Vector2 nextVertex) {
		shape.setNextVertex(new Vec2(nextVertex.x, nextVertex.y));
	}

	/** Establish connectivity to a vertex that follows the last vertex. Don't call this for loops. */
	public void setNextVertex (float nextVertexX, float nextVertexY) {
		shape.setNextVertex(new Vec2(nextVertexX, nextVertexY));
	}

	/** @return the number of vertices */
	public int getVertexCount () {
		return shape.m_count;
	}

	/** Returns the vertex at the given position.
	 * @param index the index of the vertex 0 <= index < getVertexCount( )
	 * @param vertex vertex */
	public void getVertex (int index, Vector2 vertex) {
		Vec2 v = shape.m_vertices[index];
		vertex.set(v.x, v.y);
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

	public boolean isLooped () {
		return isLooped;
	}
}
