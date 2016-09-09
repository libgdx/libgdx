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

import com.badlogic.gdx.math.Vector2;

/** This is used to compute the current state of a contact manifold. */
public class WorldManifold {
	protected final Vector2 normal = new Vector2();
	protected final Vector2[] points = {new Vector2(), new Vector2()};
	protected final float[] separations = new float[2];
	protected int numContactPoints;

	protected WorldManifold () {
	}

	/** Returns the normal of this manifold */
	public Vector2 getNormal () {
		return normal;
	}

	/** Returns the contact points of this manifold. Use getNumberOfContactPoints to determine how many contact points there are
	 * (0,1 or 2) */
	public Vector2[] getPoints () {
		return points;
	}

	/** Returns the separations of this manifold, a negative value indicates overlap, in meters. Use getNumberOfContactPoints to
	 * determine how many separations there are (0,1 or 2) */
	public float[] getSeparations () {
		return separations;
	}

	/** @return the number of contact points */
	public int getNumberOfContactPoints () {
		return numContactPoints;
	}
}
