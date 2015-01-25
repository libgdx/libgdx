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

import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Vec2;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Shape.Type;

public class Fixture {
	org.jbox2d.dynamics.Fixture fixture;
	private Body body;
	protected Shape shape;
	protected Object userData;

	/** Constructs a new fixture
	 * @param addr the address of the fixture */
	protected Fixture (Body body, org.jbox2d.dynamics.Fixture fixture) {
		this.body = body;
		this.fixture = fixture;
	}

	protected void reset (Body body, org.jbox2d.dynamics.Fixture fixture) {
		this.body = body;
		this.fixture = fixture;
		this.shape = null;
		this.userData = null;
	}

	/** Get the type of the child shape. You can use this to down cast to the concrete shape.
	 * @return the shape type. */
	public Type getType () {
		ShapeType type = fixture.getType();
		if (type == ShapeType.CIRCLE) return Type.Circle;
		if (type == ShapeType.EDGE) return Type.Edge;
		if (type == ShapeType.POLYGON) return Type.Polygon;
		if (type == ShapeType.CHAIN) return Type.Chain;
		return Type.Circle;
	}

	/** Returns the shape of this fixture */
	public Shape getShape () {
		if (shape == null) {
			org.jbox2d.collision.shapes.Shape shape2 = fixture.getShape();
			ShapeType type = shape2.getType();
			if (type == ShapeType.CHAIN) shape = new ChainShape((org.jbox2d.collision.shapes.ChainShape)shape2);
			if (type == ShapeType.CIRCLE) shape = new CircleShape((org.jbox2d.collision.shapes.CircleShape)shape2);
			if (type == ShapeType.EDGE) shape = new EdgeShape((org.jbox2d.collision.shapes.EdgeShape)shape2);
			if (type == ShapeType.POLYGON) shape = new PolygonShape((org.jbox2d.collision.shapes.PolygonShape)shape2);
		}
		return shape;
	}

	/** Set if this fixture is a sensor. */
	public void setSensor (boolean sensor) {
		fixture.setSensor(sensor);
	}

	/** Is this fixture a sensor (non-solid)?
	 * @return the true if the shape is a sensor. */
	public boolean isSensor () {
		return fixture.isSensor();
	}

	/** Set the contact filtering data. This will not update contacts until the next time step when either parent body is active and
	 * awake. This automatically calls Refilter. */
	public void setFilterData (Filter filter) {
		org.jbox2d.dynamics.Filter f = new org.jbox2d.dynamics.Filter();
		f.categoryBits = filter.categoryBits;
		f.groupIndex = filter.groupIndex;
		f.maskBits = filter.maskBits;
		fixture.setFilterData(f);
	}

	/** Get the contact filtering data. */
	private final Filter filter = new Filter();

	public Filter getFilterData () {
		org.jbox2d.dynamics.Filter f = fixture.getFilterData();
		filter.categoryBits = (short)f.categoryBits;
		filter.maskBits = (short)f.maskBits;
		filter.groupIndex = (short)f.groupIndex;
		return filter;
	}

	/** Call this if you want to establish collision that was previously disabled by b2ContactFilter::ShouldCollide. */
	public void refilter () {
		fixture.refilter();
	}

	/** Get the parent body of this fixture. This is NULL if the fixture is not attached. */
	public Body getBody () {
		return body;
	}

	final Vec2 tmp = new Vec2();

	/** Test a point for containment in this fixture.
	 * @param p a point in world coordinates. */
	public boolean testPoint (Vector2 p) {
		tmp.set(p.x, p.y);
		return fixture.testPoint(tmp);
	}

	/** Test a point for containment in this fixture.
	 * @param x the x-coordinate
	 * @param y the y-coordinate */
	public boolean testPoint (float x, float y) {
		tmp.set(x, y);
		return fixture.testPoint(tmp);
	}

	/** Set the density of this fixture. This will _not_ automatically adjust the mass of the body. You must call
	 * b2Body::ResetMassData to update the body's mass. */
	public void setDensity (float density) {
		fixture.setDensity(density);
	}

	/** Get the density of this fixture. */
	public float getDensity () {
		return fixture.getDensity();
	}

	/** Get the coefficient of friction. */
	public float getFriction () {
		return fixture.getFriction();
	}

	/** Set the coefficient of friction. */
	public void setFriction (float friction) {
		fixture.setFriction(friction);
	}

	/** Get the coefficient of restitution. */
	public float getRestitution () {
		return fixture.getRestitution();
	}

	/** Set the coefficient of restitution. */
	public void setRestitution (float restitution) {
		fixture.setRestitution(restitution);
	}

	/** Sets custom user data. */
	public void setUserData (Object userData) {
		this.userData = userData;
	}

	/** @return custom user data */
	public Object getUserData () {
		return userData;
	}
}
