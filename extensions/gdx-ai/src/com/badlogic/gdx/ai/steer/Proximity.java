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

import com.badlogic.gdx.ai.AIUtils;
import com.badlogic.gdx.ai.steer.behaviors.Alignment;
import com.badlogic.gdx.ai.steer.behaviors.Cohesion;
import com.badlogic.gdx.ai.steer.behaviors.Separation;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.steer.behaviors.BlendedSteering;
import com.badlogic.gdx.math.Vector;

/** A {@code Proximity} defines an area that is used by group behaviors to find and process the owner's neighbors.
 * <p>
 * Typically (but not necessarily) different group behaviors share the same {@code Proximity} for a given owner. This allows you to
 * combine group behaviors so as to get a more complex behavior also known as emergent behavior. Emergent behavior is behavior
 * that looks complex and/or purposeful to the observer but is actually derived spontaneously from fairly simple rules. The
 * lower-level agents following the rules have no idea of the bigger picture; they are only aware of themselves and maybe a few of
 * their neighbors. A typical example of emergence is flocking behavior which is a combination of three group behaviors:
 * {@link Separation separation}, {@link Alignment alignment}, and {@link Cohesion cohesion}. The three behaviors are typically
 * combined through a {@link BlendedSteering blended steering}. This works okay but, because of the limited view distance of a
 * character, it's possible for an agent to become isolated from its flock. If this happens, it will just sit still and do
 * nothing. To prevent this from happening, you usually add in the {@link Wander wander} behavior too. This way, all the agents
 * keep moving all the time. Tweaking the magnitudes of each of the contributing behaviors will give you different effects such as
 * shoals of fish, loose swirling flocks of birds, or bustling close-knit herds of sheep.
 * <p>
 * Before a steering acceleration can be calculated for a combination of group behaviors, the neighbors must be determined and
 * processed. This is done by the {@link #findNeighbors} method and its callback argument.
 * <p>
 * Notes:
 * <ul>
 * <li>Sharing a {@code Proximity} instance among group behaviors having the same owner can save a little time determining the
 * neighbors only once from inside the {@code findNeighbors} method. Especially, {@code Proximity} implementation classes can check
 * the {@link AIUtils#getFrameId() frameId} of the current frame in order to calculate neighbors only once per frame.</li>
 * <li>If you want to make sure a Proximity doesn't use as a neighbor a given agent from the list, for example the evader or the
 * owner itself, you have to implement a callback that prevents it from being considered by returning {@code false} from the method
 * {@link ProximityCallback#reportNeighbor(Steerable) reportNeighbor}.</li>
 * <li>If there is some efficient way of pruning potential neighbors before they are processed, the overall performance in time
 * will improve. Spatial data structures such as multi-resolution maps, quad-trees, oct-trees, and binary space partition (BSP)
 * trees can be used to get potential neighbors more efficiently. Spatial partitioning techniques are crucial when you have to
 * deal with lots of agents. Especially, if you're using Bullet or Box2d in your game, it's recommended to implement proximities
 * that exploit their methods to query the world. Both Bullet and Box2d internally use some kind of spatial partitioning.</li>
 * </ul>
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public interface Proximity<T extends Vector<T>> {

	/** Returns the owner of this proximity. */
	public Steerable<T> getOwner ();

	/** Sets the owner of this proximity. */
	public void setOwner (Steerable<T> owner);

	/** Finds the agents that are within the immediate area of the owner. Each of those agents is passed to the
	 * {@link ProximityCallback#reportNeighbor(Steerable) reportNeighbor} method of the specified callback.
	 * @return the number of neighbors found. */
	public int findNeighbors (ProximityCallback<T> callback);

	/** The callback object used by a proximity to report the owner's neighbor.
	 * 
	 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
	 * 
	 * @author davebaol */
	public interface ProximityCallback<T extends Vector<T>> {

		/** The callback method used to report a neighbor.
		 * @param neighbor the reported neighbor.
		 * @return {@code true} if the given neighbor is valid; {@code false} otherwise. */
		public boolean reportNeighbor (Steerable<T> neighbor);

	}
}
