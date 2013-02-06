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

package com.badlogic.gdx.physics.tokamak;

import com.badlogic.gdx.math.Vector3;

public class CollisionInfo {
	NativeObject bodyA;
	NativeObject bodyB;

	Geometry geometryA;
	Geometry geometryB;
	int materialIdA;
	int materialIdB;
	Vector3 bodyContactPointA; // contact point A in body space of A
	Vector3 bodyContactPointB; // contact point B in body space of B
	Vector3 worldContactPointA; // contact point A in world space
	Vector3 worldContactPointB; // contact point B in world space
	Vector3 relativeVelocity;
	Vector3 collisionNormal;
}
