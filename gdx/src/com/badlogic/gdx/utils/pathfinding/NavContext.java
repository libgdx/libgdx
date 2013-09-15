
package com.badlogic.gdx.utils.pathfinding;

/** The context describing the current path finding state
 * <p>
 * Original implementation by Kevin Glass from Slick2D.
 * </p>
 * @author hneuer */
public interface NavContext<N extends NavNode> {
	/** Get the object being moved along the path if any */
	public Object getMover ();

	/** Get the source node */
	public N getSourceNode ();

	/** Get the distance that has been searched to reach this point */
	public float getSearchDistance ();
}
