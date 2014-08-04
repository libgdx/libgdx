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
import com.badlogic.gdx.math.Vector;

/** A {@code CentralRayWithWhiskersConfiguration} uses a long central ray and two shorter whiskers.
 * <p>
 * A central ray with short whiskers is often the best initial configuration to try but can make it impossible for the character
 * to move down tight passages. Also, it is still susceptible to the <a
 * href="../behaviors/RaycastObstacleAvoidance.html#cornerTrap">corner trap</a>, far less than the parallel configuration though.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class CentralRayWithWhiskersConfiguration<T extends Vector<T>> extends RayConfigurationBase<T> {

	private float rayLength;
	private float whiskerLength;
	private float whiskerAngle;

	/** Creates a {@code CentralRayWithWhiskersConfiguration} for the given owner where the central ray has the specified length and
	 * the two whiskers have the specified length and angle.
	 * @param owner the owner of this configuration
	 * @param rayLength the length of the central ray
	 * @param whiskerLength the length of the two whiskers (usually shorter than the central ray)
	 * @param whiskerAngle the angle in radians of the whiskers from the central ray */
	public CentralRayWithWhiskersConfiguration (Steerable<T> owner, float rayLength, float whiskerLength, float whiskerAngle) {
		super(owner, 3);
		this.rayLength = rayLength;
		this.whiskerLength = whiskerLength;
		this.whiskerAngle = whiskerAngle;
	}

	@Override
	public Ray<T>[] updateRays () {
		T ownerPosition = owner.getPosition();
		T ownerVelocity = owner.getLinearVelocity();

		float velocityAngle = owner.vectorToAngle(ownerVelocity);

		// Update central ray
		rays[0].origin.set(ownerPosition);
		rays[0].direction.set(ownerVelocity).nor().scl(rayLength);

		// Update left ray
		rays[1].origin.set(ownerPosition);
		owner.angleToVector(rays[1].direction, velocityAngle - whiskerAngle).scl(whiskerLength);

		// Update right ray
		rays[2].origin.set(ownerPosition);
		owner.angleToVector(rays[2].direction, velocityAngle + whiskerAngle).scl(whiskerLength);

		return rays;
	}

	/** Returns the length of the central ray. */
	public float getRayLength () {
		return rayLength;
	}

	/** Sets the length of the central ray. */
	public void setRayLength (float rayLength) {
		this.rayLength = rayLength;
	}

	/** Returns the length of the two whiskers. */
	public float getWhiskerLength () {
		return whiskerLength;
	}

	/** Sets the length of the two whiskers. */
	public void setWhiskerLength (float whiskerLength) {
		this.whiskerLength = whiskerLength;
	}

	/** Returns the angle in radians of the whiskers from the central ray. */
	public float getWhiskerAngle () {
		return whiskerAngle;
	}

	/** Sets the angle in radians of the whiskers from the central ray. */
	public void setWhiskerAngle (float whiskerAngle) {
		this.whiskerAngle = whiskerAngle;
	}

}
