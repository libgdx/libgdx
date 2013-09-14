
package com.badlogic.gdx.utils.pathfinding;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;

/** A navigation path.
 * @author hneuer */
public interface NavPath<N extends NavNode> {
	/** Fills the navigation path between the start and target node.
	 * <p>
	 * Note that current implementations have to follow the path backward from the targetNode to the startNode (following the
	 * parent relation).
	 * <p> */
	public void fill (N startNode, N targetNode);
}
