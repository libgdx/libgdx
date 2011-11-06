
package com.badlogic.gdx.physics.box2d;

import com.badlogic.gdx.math.Vector2;

/** A line segment (edge) shape. These can be connected in chains or loops to other edge shapes. The connectivity information is
 * used to ensure correct contact normals. */
public class EdgeShape extends Shape {
	public EdgeShape () {
		addr = newEdgeShape();
	}

	private native long newEdgeShape ();

	EdgeShape (long addr) {
		this.addr = addr;
	}

	/** Set this as an isolated edge. */
	public void set (Vector2 v1, Vector2 v2) {
		set(v1.x, v1.y, v2.x, v2.y);
	}

	/** Set this as an isolated edge. */
	public void set (float v1X, float v1Y, float v2X, float v2Y) {
		jniSet(addr, v1X, v1Y, v2X, v2Y);
	}

	private native void jniSet (long addr, float v1X, float v1Y, float v2X, float v2Y);

	static final float[] vertex = new float[2];

	public void getVertex1 (Vector2 vec) {
		jniGetVertex1(addr, vertex);
		vec.x = vertex[0];
		vec.y = vertex[1];
	}

	private native void jniGetVertex1 (long addr, float[] vertex);

	public void getVertex2 (Vector2 vec) {
		jniGetVertex2(addr, vertex);
		vec.x = vertex[0];
		vec.y = vertex[1];
	}

	private native void jniGetVertex2 (long addr, float[] vertex);

// /// @see b2Shape::TestPoint
// bool TestPoint(const b2Transform& transform, const b2Vec2& p) const;
//
// /// Implement b2Shape.
// bool RayCast(b2RayCastOutput* output, const b2RayCastInput& input,
// const b2Transform& transform, int32 childIndex) const;
//
// /// @see b2Shape::ComputeAABB
// void ComputeAABB(b2AABB* aabb, const b2Transform& transform, int32 childIndex) const;
//
// /// @see b2Shape::ComputeMass
// void ComputeMass(b2MassData* massData, float32 density) const;
//
// /// These are the edge vertices
// b2Vec2 m_vertex1, m_vertex2;
//
// /// Optional adjacent vertices. These are used for smooth collision.
// b2Vec2 m_vertex0, m_vertex3;
// bool m_hasVertex0, m_hasVertex3;

	@Override
	public Type getType () {
		return Type.Edge;
	}
}
