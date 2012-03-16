/*******************************************************************************
 * Copyright (c) 2011, Daniel Murphy
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package org.jbox2d.collision.shapes;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.RayCastInput;
import org.jbox2d.collision.RayCastOutput;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;

/**
 * A circle shape.
 */
public class CircleShape extends Shape {
	
	public final Vec2 m_p;
	
	private final Vec2 pool1 = new Vec2();
	private final Vec2 pool2 = new Vec2();
	private final Vec2 pool3 = new Vec2();
	
	public CircleShape() {
		super(ShapeType.CIRCLE);
		m_p = new Vec2();
		m_radius = 0;
	}
	
	public final Shape clone() {
		CircleShape shape = new CircleShape();
		shape.m_p.set(m_p);
		shape.m_radius = m_radius;
		return shape;
	}
	
	public final int getChildCount() {
	  return 1;
	}
	
	/**
	 * Get the supporting vertex index in the given direction.
	 * 
	 * @param d
	 * @return
	 */
	public final int getSupport(final Vec2 d) {
		return 0;
	}
	
	/**
	 * Get the supporting vertex in the given direction.
	 * 
	 * @param d
	 * @return
	 */
	public final Vec2 getSupportVertex(final Vec2 d) {
		return m_p;
	}
	
	/**
	 * Get the vertex count.
	 * 
	 * @return
	 */
	public final int getVertexCount() {
		return 1;
	}
	
	/**
	 * Get a vertex by index.
	 * 
	 * @param index
	 * @return
	 */
	public final Vec2 getVertex(final int index) {
		assert (index == 0);
		return m_p;
	}
	
	@Override
	public final boolean testPoint(final Transform transform, final Vec2 p) {
		final Vec2 center = pool1;
		Rot.mulToOutUnsafe(transform.q, m_p, center);
		center.addLocal(transform.p);
		
		final Vec2 d = center.subLocal(p).negateLocal();
		return Vec2.dot(d, d) <= m_radius * m_radius;
	}
	
	// Collision Detection in Interactive 3D Environments by Gino van den Bergen
	// From Section 3.1.2
	// x = s + a * r
	// norm(x) = radius
	
	@Override
	public final boolean raycast(RayCastOutput output, RayCastInput input, Transform transform, int childIndex) {
		
		final Vec2 position = pool1;
		final Vec2 s = pool2;
		final Vec2 r = pool3;
		
		Rot.mulToOutUnsafe(transform.q, m_p, position);
		position.addLocal(transform.p);
		s.set(input.p1).subLocal(position);
		final float b = Vec2.dot(s, s) - m_radius * m_radius;
		
		// Solve quadratic equation.
		r.set(input.p2).subLocal(input.p1);
		final float c = Vec2.dot(s, r);
		final float rr = Vec2.dot(r, r);
		final float sigma = c * c - rr * b;
		
		// Check for negative discriminant and short segment.
		if (sigma < 0.0f || rr < Settings.EPSILON) {
			return false;
		}
		
		// Find the point of intersection of the line with the circle.
		float a = -(c + MathUtils.sqrt(sigma));
		
		// Is the intersection point on the segment?
		if (0.0f <= a && a <= input.maxFraction * rr) {
			a /= rr;
			output.fraction = a;
			output.normal.set(r).mulLocal(a);
			output.normal.addLocal(s);
			output.normal.normalize();
			return true;
		}
		
		return false;
	}
	
	@Override
	public final void computeAABB(final AABB aabb, final Transform transform, int childIndex) {
		final Vec2 p = pool1;
		Rot.mulToOutUnsafe(transform.q, m_p, p);
		p.addLocal(transform.p);
		
		aabb.lowerBound.x = p.x - m_radius;
		aabb.lowerBound.y = p.y - m_radius;
		aabb.upperBound.x = p.x + m_radius;
		aabb.upperBound.y = p.y + m_radius;
	}
	
	@Override
	public final void computeMass(final MassData massData, final float density) {
		massData.mass = density * Settings.PI * m_radius * m_radius;
		massData.center.set(m_p);
		
		// inertia about the local origin
		massData.I = massData.mass * (0.5f * m_radius * m_radius + Vec2.dot(m_p, m_p));
	}
	
	// djm pooled from above
	/*
	 * @see Shape#computeSubmergedArea(Vec2, float, Vec2, Vec2)
	 * @Override
	 * public final float computeSubmergedArea( final Vec2 normal, final float offset,
	 * final Transform xf, final Vec2 c) {
	 * final Vec2 p = tlp.get();
	 * Transform.mulToOut(xf,m_p, p);
	 * final float l = -( Vec2.dot(normal,p) - offset);
	 * if( l < -m_radius + Settings.EPSILON){
	 * //Completely dry
	 * return 0;
	 * }
	 * if(l > m_radius){
	 * //Completely wet
	 * c.set(p);
	 * return (float)Math.PI*m_radius*m_radius;
	 * }
	 * //Magic
	 * final float r2 = m_radius*m_radius;
	 * final float l2 = l*l;
	 * //Erin TODO: write Sqrt to handle fixed point case.
	 * final float area = (float) (r2 * (Math.asin(l/m_radius) + Math.PI/2)+ l *
	 * Math.sqrt(r2 - l2));
	 * final float com = (float) (-2.0/3.0* Math.pow(r2-l2,1.5f)/area);
	 * c.x = p.x + normal.x * com;
	 * c.y = p.y + normal.y * com;
	 * return area;
	 * }
	 */
}
