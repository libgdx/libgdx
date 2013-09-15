
package com.badlogic.gdx.utils.pathfinding.tiled;

import com.badlogic.gdx.utils.pathfinding.AStarPathFinder.AStarHeuristicCalculator;
import com.badlogic.gdx.utils.pathfinding.NavContext;

/** Implementation of a heuristic calculator for a tile map. It simply calculates the Manhattan distance between two given tiles.
 * @author hneuer */
public class ManhattanDistance implements AStarHeuristicCalculator<NavNodeTileBased> {
	@Override
	public float getCost (NavContext<NavNodeTileBased> map, Object mover, NavNodeTileBased startNode, NavNodeTileBased targetNode) {
		return Math.abs(targetNode.x - startNode.x) + Math.abs(targetNode.y - startNode.y);
	}
}
