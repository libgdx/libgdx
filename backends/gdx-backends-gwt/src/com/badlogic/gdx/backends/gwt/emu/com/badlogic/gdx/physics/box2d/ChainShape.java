
package com.badlogic.gdx.physics.box2d;

import org.jbox2d.common.Vec2;

import com.badlogic.gdx.math.Vector2;

public class ChainShape extends Shape {
	org.jbox2d.collision.shapes.ChainShape shape;
	
	public ChainShape () {
		shape = new org.jbox2d.collision.shapes.ChainShape();
	}
	
	ChainShape(org.jbox2d.collision.shapes.ChainShape shape) {
		this.shape = shape;
	}

	@Override
	public Type getType () {
		return Type.Chain;
	}

	/** Create a loop. This automatically adjusts connectivity.
	 * @param vertices an array of vertices, these are copied */
	public void createLoop (Vector2[] vertices) {
		Vec2[] v = new Vec2[vertices.length];
		for(int i = 0; i < vertices.length; i++) {
			v[i] = new Vec2(vertices[i].x, vertices[i].y);
		}
		shape.createLoop(v, v.length);
	}

	/** Create a chain with isolated end vertices.
	 * @param vertices an array of vertices, these are copied */
	public void createChain (Vector2[] vertices) {
		Vec2[] v = new Vec2[vertices.length];
		for(int i = 0; i < vertices.length; i++) {
			v[i] = new Vec2(vertices[i].x, vertices[i].y);
		}
		shape.createChain(v, v.length);
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
}
