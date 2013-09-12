
package com.badlogic.gdx.utils.pathfinding;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;

/** A path determined by some path finding algorithm. A series of steps from the starting location to the target location. This
 * includes a step for the initial location.
 * <p>
 * Original implementation by Kevin Glass from Slick2D.
 * </p>
 * @author hneuer */
public class NavPath {
	private final IntArray stepsX = new IntArray();
	private final IntArray stepsY = new IntArray();

	/** Get the length of the path, i.e. the number of steps.
	 * 
	 * @return The number of steps in this path */
	public int getLength () {
		return stepsX.size;
	}

	public Vector2 getStep (int index, Vector2 out) {
		out.set(getX(index), getY(index));
		return out;
	}

	public int getX (int index) {
		return stepsX.get(index);
	}

	public int getY (int index) {
		return stepsY.get(index);
	}

	public void appendStep (int x, int y) {
		stepsX.add(x);
		stepsY.add(y);
	}

	public void reverse () {
		stepsX.reverse();
		stepsY.reverse();
	}

	public void clear () {
		stepsX.clear();
		stepsY.clear();
	}
}
