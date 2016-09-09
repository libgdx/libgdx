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

/** A line segment (edge) shape. These can be connected in chains or loops to other edge shapes. The connectivity information is
 * used to ensure correct contact normals. */
public class EdgeShape extends Shape {
	org.jbox2d.collision.shapes.EdgeShape shape;

	public EdgeShape () {
		super(new org.jbox2d.collision.shapes.EdgeShape());
		shape = (org.jbox2d.collision.shapes.EdgeShape)super.shape;
	}

	public EdgeShape (org.jbox2d.collision.shapes.EdgeShape shape) {
		super(shape);
		this.shape = shape;
	}

	/** Set this as an isolated edge. */
	public void set (Vector2 v1, Vector2 v2) {
		set(v1.x, v1.y, v2.x, v2.y);
	}

	Vec2 tmp = new Vec2();
	Vec2 tmp2 = new Vec2();

	/** Set this as an isolated edge. */
	public void set (float v1X, float v1Y, float v2X, float v2Y) {
		tmp.set(v1X, v1Y);
		tmp2.set(v2X, v2Y);
		shape.set(tmp, tmp2);
	}

	public void getVertex1 (Vector2 vec) {
		Vec2 v1 = shape.m_vertex1;
		vec.set(v1.x, v1.y);
	}

	public void getVertex2 (Vector2 vec) {
		Vec2 v2 = shape.m_vertex2;
		vec.set(v2.x, v2.y);
	}

	public void getVertex0 (Vector2 vec) {
		Vec2 v0 = shape.m_vertex0;
		vec.set(v0.x, v0.y);
	}

	public void getVertex3 (Vector2 vec) {
		Vec2 v3 = shape.m_vertex3;
		vec.set(v3.x, v3.y);
	}

	public boolean hasVertex0 () {
		return shape.m_hasVertex0;
	}

	public void setHasVertex0 (boolean hasVertex0) {
		shape.m_hasVertex0 = hasVertex0;
	}

	public boolean hasVertex3 () {
		return shape.m_hasVertex3;
	}

	public void setHasVertex3 (boolean hasVertex3) {
		shape.m_hasVertex3 = hasVertex3;
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

	@Override
	public Type getType () {
		return Type.Edge;
	}
}
