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

import com.badlogic.gdx.ai.pathfinding.AStarPathFinder.AStarHeuristicCalculator;
import com.badlogic.gdx.ai.pathfinding.NavContext;

/** Implementation of a heuristic calculator for a tile map. It simply calculates the Manhattan distance between two given tiles.
 * @author hneuer */
public class ManhattanDistance implements AStarHeuristicCalculator<NavNodeTileBased> {
	@Override
	public float getCost (NavContext<NavNodeTileBased> map, Object mover, NavNodeTileBased startNode, NavNodeTileBased targetNode) {
		return Math.abs(targetNode.x - startNode.x) + Math.abs(targetNode.y - startNode.y);
	}
}
