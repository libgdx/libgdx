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

package com.badlogic.gdx.ai.steer.rays;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance.Ray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector;

/** A {@code ParallelSideRayConfiguration} uses two rays parallel to the direction of motion. The rays have the same length and
 * opposite side offset.
 * <p>
 * The parallel configuration works well in areas where corners are highly obtuse but is very susceptible to the <a
 * href="../behaviors/RaycastObstacleAvoidance.html">corner trap</a>.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class ParallelSideRayConfiguration<T extends Vector<T>> extends RayConfigurationBase<T> {

	private static final float HALF_PI = MathUtils.PI * 0.5f;

	private float length;
	private float sideOffset;

	/** Creates a {@code ParallelSideRayConfiguration} for the given owner where the two rays have the specified length and side
	 * offset.
	 * @param owner the owner of this ray configuration
	 * @param length the length of the rays.
	 * @param sideOffset the side offset of the rays. */
	public ParallelSideRayConfiguration (Steerable<T> owner, float length, float sideOffset) {
		super(owner, 2);
		this.length = length;
		this.sideOffset = sideOffset;
	}

	@Override
	public Ray<T>[] updateRays () {
		float velocityAngle = owner.vectorToAngle(owner.getLinearVelocity());

		// Update ray 0
		owner.angleToVector(rays[0].origin, velocityAngle - HALF_PI).scl(sideOffset).add(owner.getPosition());
		rays[0].direction.set(owner.getLinearVelocity()).nor().scl(length);

		// Update ray 1
		owner.angleToVector(rays[1].origin, velocityAngle + HALF_PI).scl(sideOffset).add(owner.getPosition());
		rays[1].direction.set(rays[0].direction);

		return rays;
	}

	/** Returns the length of the rays. */
	public float getLength () {
		return length;
	}

	/** Sets the length of the rays. */
	public void setLength (float length) {
		this.length = length;
	}

	/** Returns the side offset of the rays. */
	public float getSideOffset () {
		return sideOffset;
	}

	/** Sets the side offset of the rays. */
	public void setSideOffset (float sideOffset) {
		this.sideOffset = sideOffset;
	}

}
