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

package com.badlogic.gdx.ai.pathfinding;

/** The context describing the current path finding state
 * <p>
 * Original implementation by Kevin Glass from Slick2D.
 * </p>
 * @author hneuer */
public interface NavContext<N extends NavNode<N>> {
	/** Get the object being moved along the path if any */
	public Object getMover ();

	/** Get the source node */
	public N getSourceNode ();

	/** Get the distance that has been searched to reach this point */
	public float getSearchDistance ();
}
