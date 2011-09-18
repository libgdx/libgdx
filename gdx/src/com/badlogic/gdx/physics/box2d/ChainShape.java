package com.badlogic.gdx.physics.box2d;

import com.badlogic.gdx.math.Vector2;

public class ChainShape extends Shape {
	public ChainShape () {
		addr = newChainShape();
	}

	private native long newChainShape ();
	
	ChainShape(long addr) {
		this.addr = addr;
	}
	
	@Override
	public Type getType () {
		return Type.Chain;
	}
	
	/**
	 * Create a loop. This automatically adjusts connectivity.
	 * @param vertices an array of vertices, these are copied
	 */
	public void createLoop(Vector2[] vertices) {
		float[] verts = new float[vertices.length * 2];
		for (int i = 0, j = 0; i < vertices.length * 2; i += 2, j++) {
			verts[i] = vertices[j].x;
			verts[i + 1] = vertices[j].y;
		}
		jniCreateLoop(addr, verts);
	}

	private native void jniCreateLoop (long addr, float[] verts);

	/**
	 *  Create a chain with isolated end vertices.
	 *  @param vertices an array of vertices, these are copied
	 */
	public void createChain(Vector2[] vertices) {
		float[] verts = new float[vertices.length * 2];
		for (int i = 0, j = 0; i < vertices.length * 2; i += 2, j++) {
			verts[i] = vertices[j].x;
			verts[i + 1] = vertices[j].y;
		}
		jniCreateChain(addr, verts);
	}

	private native void jniCreateChain (long addr, float[] verts);

	/** Establish connectivity to a vertex that precedes the first vertex.
	 * Don't call this for loops.
	 */
	public void setPrevVertex(Vector2 prevVertex) {
		setPrevVertex(prevVertex.x, prevVertex.y);
	}
	
	/** Establish connectivity to a vertex that precedes the first vertex.
	 * Don't call this for loops.
	 */
	public void setPrevVertex(float prevVertexX, float prevVertexY) {
		jniSetPrevVertex(addr, prevVertexX, prevVertexY);
	}
	
	private native void jniSetPrevVertex(long addr, float x, float y);
	
	/** Establish connectivity to a vertex that follows the last vertex.
	 * Don't call this for loops.
	 */
	public void setNextVertex(Vector2 nextVertex) {
		setNextVertex(nextVertex.x, nextVertex.y);
	}
	
	/** Establish connectivity to a vertex that follows the last vertex.
	 * Don't call this for loops.
	 */
	public void setNextVertex(float nextVertexX, float nextVertexY) {
		jniSetNextVertex(addr, nextVertexX, nextVertexY);
	}
	
	private native void jniSetNextVertex(long addr, float x, float y);

	
	/** @return the number of vertices */
	public int getVertexCount () {
		return jniGetVertexCount(addr);
	}

	private native int jniGetVertexCount (long addr);

	private static float[] verts = new float[2];

	/** Returns the vertex at the given position.
	 * @param index the index of the vertex 0 <= index < getVertexCount( )
	 * @param vertex vertex */
	public void getVertex (int index, Vector2 vertex) {
		jniGetVertex(addr, index, verts);
		vertex.x = verts[0];
		vertex.y = verts[1];
	}
	
	private native void jniGetVertex (long addr, int index, float[] verts);

//		/// Implement b2Shape. Vertices are cloned using b2Alloc.
//		b2Shape* Clone(b2BlockAllocator* allocator) const;
//
//		/// @see b2Shape::GetChildCount
//		int32 GetChildCount() const;
//
//		/// Get a child edge.
//		void GetChildEdge(b2EdgeShape* edge, int32 index) const;
//
//		/// This always return false.
//		/// @see b2Shape::TestPoint
//		bool TestPoint(const b2Transform& transform, const b2Vec2& p) const;
//
//		/// Implement b2Shape.
//		bool RayCast(b2RayCastOutput* output, const b2RayCastInput& input,
//						const b2Transform& transform, int32 childIndex) const;
//
//		/// @see b2Shape::ComputeAABB
//		void ComputeAABB(b2AABB* aabb, const b2Transform& transform, int32 childIndex) const;
//
//		/// Chains have zero mass.
//		/// @see b2Shape::ComputeMass
//		void ComputeMass(b2MassData* massData, float32 density) const;
//
}
