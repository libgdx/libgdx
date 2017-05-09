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

/** A description of an implementation that can find a path from one node in an arbitrary graph to another based on information
 * provided by that graph.
 * <p>
 * Original implementation by Kevin Glass from Slick2D.
 * </p>
 * @author hneuer */
public interface PathFinder<N extends NavNode<N>> {
	/** Find a path from the start node to the target node avoiding blockages and attempting to honor costs provided by the graph.
	 * 
	 * @param mover The entity that will be moving along the path. This provides a place to pass context information about the game
	 *           entity doing the moving, e.g. can it fly? can it swim etc.
	 * 
	 * @param startNode the start node
	 * @param targetNode the target node
	 * @param out out-parameter for the navigation path. Will only be filled if a path is found, otherwise it won't get touched.
	 * @return True if a path was found. */
	public boolean findPath (Object mover, N startNode, N targetNode, NavPath<N> out);
}
