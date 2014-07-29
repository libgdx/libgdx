package com.badlogic.gdx.math;

import com.badlogic.gdx.math.collision.*;

public interface Shape {
	public BoundingBox getAABB();
	public Sphere getBoundingSphere();
	public Class getShapeType();
	public Vector3 getCenter();
}
