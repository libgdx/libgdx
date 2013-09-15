
package com.badlogic.gdx.utils.pathfinding.tiled;

import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.pathfinding.NavPath;

/** Implementation of a navigation path for a tile map. It holds lists of x/y coordinates which represent the tile coordinates that
 * are part of the path.
 * @author hneuer */
public class NavPathTileBased implements NavPath<NavNodeTileBased> {
	public final IntArray x = new IntArray();
	public final IntArray y = new IntArray();

	@Override
	public void fill (NavNodeTileBased startNode, NavNodeTileBased targetNode) {
		x.clear();
		y.clear();
		NavNodeTileBased current = targetNode;
		while (current != startNode) {
			x.add(current.x);
			y.add(current.y);
			current = (NavNodeTileBased)current.parent;
		}
		x.add(current.x);
		y.add(current.y);
		x.reverse();
		y.reverse();
	}

	@Override
	public void clear () {
		x.clear();
		y.clear();
	}

	@Override
	public int getLength () {
		return x.size;
	}
}
