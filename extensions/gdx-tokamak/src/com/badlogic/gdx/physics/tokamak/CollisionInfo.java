package com.badlogic.gdx.physics.tokamak;

import com.badlogic.gdx.math.Vector3;

public class CollisionInfo {
	NativeObject bodyA;
	NativeObject bodyB;

	Geometry geometryA;
	Geometry geometryB;
	int materialIdA;
	int materialIdB;
	Vector3 bodyContactPointA;		// contact point A in body space of A
	Vector3 bodyContactPointB;		// contact point B in body space of B
	Vector3 worldContactPointA;	// contact point A in world space
	Vector3 worldContactPointB;	// contact point B in world space
	Vector3 relativeVelocity;
	Vector3 collisionNormal;
}
