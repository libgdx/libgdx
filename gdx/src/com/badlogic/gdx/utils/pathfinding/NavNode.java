
package com.badlogic.gdx.utils.pathfinding;

import com.badlogic.gdx.utils.Array;

/** A single node in the navigation graph.
 * @author hneuer */
public class NavNode {
	/** The parent of this node, how we reached it in the search */
	public NavNode parent;
	/** The list of all adjacent neighbor nodes. */
	public final Array<NavNode> neighbors = new Array<NavNode>();
	/** Algorithm specific data. */
	protected Object algoData;
}
