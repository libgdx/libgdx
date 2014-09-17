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

package com.badlogic.gdx.ai.steer;

import com.badlogic.gdx.ai.steer.Proximity.ProximityCallback;
import com.badlogic.gdx.math.Vector;

/** {@code GroupBehavior} is the base class for the steering behaviors that take into consideration the agents in the game world
 * that are within the immediate area of the owner. This immediate area is defined by a {@link Proximity} that is in charge of
 * finding and processing the owner's neighbors through the given {@link ProximityCallback}.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public abstract class GroupBehavior<T extends Vector<T>> extends SteeringBehavior<T> {

	/** The proximity decides which agents are considered neighbors. */
	protected Proximity<T> proximity;

	/** Creates a GroupBehavior for the specified owner and proximity.
	 * @param owner the owner of this behavior.
	 * @param proximity the proximity to detect the owner's neighbors */
	public GroupBehavior (Steerable<T> owner, Proximity<T> proximity) {
		super(owner);
		this.proximity = proximity;
	}

	/** Returns the proximity of this group behavior */
	public Proximity<T> getProximity () {
		return proximity;
	}

	/** Sets the proximity of this group behavior
	 * @param proximity the proximity to set */
	public void setProximity (Proximity<T> proximity) {
		this.proximity = proximity;
	}

}
