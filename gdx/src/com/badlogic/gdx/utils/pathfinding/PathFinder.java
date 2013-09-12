
package com.badlogic.gdx.utils.pathfinding;

/** A description of an implementation that can find a path from one location on a tile map to another based on information
 * provided by that tile map.
 * <p>
 * Original implementation by Kevin Glass from Slick2D.
 * </p>
 * @author hneuer */
public interface PathFinder {
	/** Find a path from the starting location provided (sx,sy) to the target location (tx,ty) avoiding blockages and attempting to
	 * honor costs provided by the tile map.
	 * 
	 * @param mover The entity that will be moving along the path. This provides a place to pass context information about the game
	 *           entity doing the moving, e.g. can it fly? can it swim etc.
	 * @param sx The x coordinate of the start location
	 * @param sy The y coordinate of the start location
	 * @param tx The x coordinate of the target location
	 * @param ty The y coordinate of the target location
	 * @param out The out-parameter which will be filled with the path (if found).
	 * @return True if a path was found. */
	public boolean findPath (Object mover, int sx, int sy, int tx, int ty, NavPath out);
}
