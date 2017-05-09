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

import com.badlogic.gdx.utils.Array;

/** A single node in the navigation graph. It contains an array of neighbor nodes, that way it is possible to build up arbitrary
 * navigation graphs, e.g. a tiled map with no diagonal movement may contain up to 4 neighbors for each node.
 * @author hneuer */
public class NavNode<N extends NavNode<?>> {
	/** The parent of this node, how we reached it in the search */
	public N parent;
	/** The list of all adjacent neighbor nodes. */
	public final Array<N> neighbors = new Array<N>();
	/** Algorithm specific data. */
	protected Object algoData;
}
