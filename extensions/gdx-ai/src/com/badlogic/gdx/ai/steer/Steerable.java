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

import com.badlogic.gdx.math.Vector;

/** The {@code Steerable} interface gives access to the character's information required by steering behaviors.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public interface Steerable<T extends Vector<T>> {

	/** Returns the vector indicating the position of this Steerable. */
	public T getPosition ();

	/** Returns the float value indicating the orientation of this Steerable. The orientation is the angle in radians representing
	 * the direction that this Steerable is facing.
	 * <p>
	 * Notice that there is nothing to connect the direction that a Steerable is moving and the direction it is facing. For
	 * instance, a character can be oriented along the x-axis but be traveling directly along the y-axis. */
	public float getOrientation ();

	/** Returns the vector indicating the linear velocity of this Steerable. */
	public T getLinearVelocity ();

	/** Returns the float value indicating the the angular velocity in radians of this Steerable. */
	public float getAngularVelocity ();

	/** Returns the bounding radius of this Steerable. */
	public float getBoundingRadius ();

	/** Returns {@code true} if this Steerable is tagged; {@code false} otherwise. */
	public boolean isTagged ();

	/** Tag/untag this Steerable. This is a generic flag utilized in a variety of ways.
	 * @param tagged the boolean value to set */
	public void setTagged (boolean tagged);

	/** Returns a new vector.
	 * <p>
	 * This method is used by steering behaviors to instantiate vectors of the correct type parameter {@code T}. This technique
	 * keeps the API simple and makes steering behaviors easier to use with the GWT backend because avoids the use of reflection. */
	public T newVector ();

	/** Returns the angle in radians pointing along the specified vector.
	 * @param vector the vector */
	public float vectorToAngle (T vector);

	/** Returns the unit vector in the direction of the specified angle expressed in radians.
	 * @param outVector the output vector.
	 * @param angle the angle in radians.
	 * @return the output vector for chaining. */
	public T angleToVector (T outVector, float angle);

}
