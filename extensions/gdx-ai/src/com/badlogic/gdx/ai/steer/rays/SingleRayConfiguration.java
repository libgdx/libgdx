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

/** As the name suggests, a {@code SingleRayConfiguration} uses just one ray cast.
 * <p>
 * This configuration is useful in concave environments but grazes convex obstacles. It is not susceptible to the <a
 * href="../behaviors/RaycastObstacleAvoidance.html#cornerTrap">corner trap</a>, though.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class SingleRayConfiguration<T extends Vector<T>> extends RayConfigurationBase<T> {

	private float length;

	/** Creates a {@code SingleRayConfiguration} for the given owner where the ray has the specified length.
	 * @param owner the owner of this configuration
	 * @param length the length of the ray */
	public SingleRayConfiguration (Steerable<T> owner, float length) {
		super(owner, 1);
		this.length = length;
	}

	@Override
	public Ray<T>[] updateRays () {
		rays[0].origin.set(owner.getPosition());
		rays[0].direction.set(owner.getLinearVelocity()).nor().scl(length);
		return rays;
	}

	/** Returns the length of the ray. */
	public float getLength () {
		return length;
	}

	/** Sets the length of the ray. */
	public void setLength (float length) {
		this.length = length;
	}

}
