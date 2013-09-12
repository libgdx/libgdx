
package com.badlogic.gdx.utils.pathfinding;

/** The description for the data we're pathfinding over. This provides the contract between the data being searched (i.e. the game
 * map) and the path finding generic tools.
 * <p>
 * Original implementation by Kevin Glass from Slick2D.
 * </p>
 * @author hneuer */
public interface TileBasedMap {
	/** Get the width of the tile map. The slightly odd name is used to distinguish this method from commonly used names in game
	 * maps.
	 * 
	 * @return The number of tiles across the map */
	public int getWidthInTiles ();

	/** Get the height of the tile map. The slightly odd name is used to distinguish this method from commonly used names in game
	 * maps.
	 * 
	 * @return The number of tiles up the map */
	public int getHeightInTiles ();

	/** Check if the given location is blocked, i.e. blocks movement of the supplied mover.
	 * 
	 * @param context The context describing the pathfinding at the time of this request
	 * @param tx The x coordinate of the tile we're moving to
	 * @param ty The y coordinate of the tile we're moving to
	 * @return True if the location is blocked */
	public boolean blocked (PathFindingContext context, int tx, int ty);

	/** Get the cost of moving through the given tile. This can be used to make certain areas more desirable. A simple and valid
	 * implementation of this method would be to return 1 in all cases.
	 * 
	 * @param context The context describing the pathfinding at the time of this request
	 * @param tx The x coordinate of the tile we're moving to
	 * @param ty The y coordinate of the tile we're moving to
	 * @return The relative cost of moving across the given tile */
	public float getCost (PathFindingContext context, int tx, int ty);
}
