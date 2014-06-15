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

package com.badlogic.gdx.ai.pathfinding.tiled;

import com.badlogic.gdx.ai.pathfinding.NavNode;

/** Implementation of a navigation node for a tile map holding the x/y coordinates of the tiles.
 * @author hneuer */
public class NavNodeTileBased extends NavNode<NavNodeTileBased> {
	/** x coordinate of the tile */
	public final int x;
	/** y coordinate of the tile */
	public final int y;

	public NavNodeTileBased (int x, int y) {
		this.x = x;
		this.y = y;
	}
}
