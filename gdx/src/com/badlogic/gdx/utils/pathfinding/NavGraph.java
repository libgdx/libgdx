
package com.badlogic.gdx.utils.pathfinding;

/** The description for the data we're pathfinding over as an arbitrary graph. This provides the contract between the data being
 * searched (i.e. the game map) and the path finding generic tools.
 * <p>
 * Original implementation by Kevin Glass from Slick2D.
 * </p>
 * @author hneuer */
public interface NavGraph<N extends NavNode> {
	/** Check if the given location is blocked, i.e. blocks movement of the supplied mover. */
	public boolean blocked (NavContext<N> context, N targetNode);

	/** Get the cost of moving to the target node. This can be used to make certain areas more desirable. A simple and valid
	 * implementation of this method would be to return 1 in all cases. */
	public float getCost (NavContext<N> context, N targetNode);
}
