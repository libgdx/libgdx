
package com.badlogic.gdx.utils.pathfinding.tiled;

import com.badlogic.gdx.utils.pathfinding.NavNode;

/** Implementation of a navigation node for a tile map holding the x/y coordinates of the tiles.
 * @author hneuer */
public class NavNodeTileBased extends NavNode {
	/** x coordinate of the tile */
	public final int x;
	/** y coordinate of the tile */
	public final int y;

	public NavNodeTileBased (int x, int y) {
		this.x = x;
		this.y = y;
	}
}
