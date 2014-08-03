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

package com.badlogic.gdx.ai.astar;

import com.badlogic.gdx.utils.Array;

/** Used in combination with {@link AStar}. The nodes of the graph have to implement this interface in order to be searchable via
 * {@link AStar}.
 * @author Daniel Holderbaum */
public interface AStarNode<T> {

	/** Returns all reachable neighbours of this node. */
	Array<? extends AStarNode<T>> getNeighbours ();

	/** If the given node is a direct neighbour, this has to return the exact cost of this node to its neighbour. If it is not a
	 * neighbour, it should return the heuristical value of the expected cost to get to the given node from this node. This
	 * heuristical value must not over-estimate those costs. */
	float distanceTo (T node);

	/** Returns the correctly typed object of this node. Usually this will be implemented via {@code return this} in the class which
	 * implements the {@link AStarNode} interface. */
	T cast ();

}
