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

package com.badlogic.gdx.graphics.g3d.model;

public interface AnimatedModel extends Model {
	/** Start playing the given animation at the given time in the animation and specify wether or not the animation will loop.
	 * @param animation The name of the animation in this {@link Model} that you should play.
	 * @param time The time, in seconds, of the section to start the animation.
	 * @param loop Whether or not the animation will loop if the time is after the end of the animation. (TODO what happens when
	 *           you reach the end of the animation and this is not set?) */
	public void setAnimation (String animation, float time, boolean loop);

	/** Get a specific named animation out of the model.
	 * @param name The name of the animation that you wish to get.
	 * @return The Animation that you requested; or, if the animation does not exist, null is returned. */
	public Animation getAnimation (String name);

	/** Get an array containing all of the animations in this model.
	 * @return An array containing a list of all of the animations in this model. */
	public Animation[] getAnimations ();
}
