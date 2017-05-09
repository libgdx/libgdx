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

/** The description for the data we're pathfinding over as an arbitrary graph. This provides the contract between the data being
 * searched (i.e. the game map) and the path finding generic tools.
 * <p>
 * Original implementation by Kevin Glass from Slick2D.
 * </p>
 * @author hneuer */
public interface NavGraph<N extends NavNode<N>> {
	/** Check if the given location is blocked, i.e. blocks movement of the supplied mover. */
	public boolean blocked (NavContext<N> context, N targetNode);

	/** Get the cost of moving to the target node. This can be used to make certain areas more desirable. A simple and valid
	 * implementation of this method would be to return 1 in all cases. */
	public float getCost (NavContext<N> context, N targetNode);
}
