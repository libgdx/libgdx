
package com.badlogic.gdx.utils.pathfinding;

/** A navigation path.
 * @author hneuer */
public interface NavPath<N extends NavNode> {
	/** Fills the navigation path between the start and target node.
	 * <p>
	 * Note that current implementations have to follow the path backward from the targetNode to the startNode (following the
	 * parent relation).
	 * <p> */
	public void fill (N startNode, N targetNode);

	/** Returns the length of the path, i.e. the number of reached nodes. */
	public int getLength ();

	/** Clear the path. */
	public void clear ();
}
