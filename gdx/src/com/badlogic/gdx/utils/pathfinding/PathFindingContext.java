
package com.badlogic.gdx.utils.pathfinding;

/** The context describing the current path finding state
 * <p>
 * Original implementation by Kevin Glass from Slick2D.
 * </p>
 * @author hneuer */
public interface PathFindingContext {
	/** Get the object being moved along the path if any
	 * 
	 * @return The object being moved along the path */
	public Object getMover ();

	/** Get the x coordinate of the source location
	 * 
	 * @return The x coordinate of the source location */
	public int getSourceX ();

	/** Get the y coordinate of the source location
	 * 
	 * @return The y coordinate of the source location */
	public int getSourceY ();

	/** Get the distance that has been searched to reach this point
	 * 
	 * @return The distance that has been search to reach this point */
	public int getSearchDistance ();
}
