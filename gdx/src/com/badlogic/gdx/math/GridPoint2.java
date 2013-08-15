package com.badlogic.gdx.math;

/**
 * A point in a 2D grid, with integer x and y coordinates
 * @author badlogic
 *
 */
public class GridPoint2 {
	public int x;
	public int y;
	
	public GridPoint2() {
	}
	
	public GridPoint2(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public GridPoint2(GridPoint2 point) {
		this.x = point.x;
		this.y = point.y;
	}
	
	public GridPoint2 set(GridPoint2 point) {
		this.x = point.x;
		this.y = point.y;
		return this;
	}
	
	public GridPoint2 set(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}

	@Override
	public int hashCode () {
		final int prime = 43;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals (Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof GridPoint2)) return false;
		GridPoint2 other = (GridPoint2)obj;
		return this.x == other.x && this.y == other.y;
	}
}
