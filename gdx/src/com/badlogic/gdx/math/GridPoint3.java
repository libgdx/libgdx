package com.badlogic.gdx.math;

/**
 * A point in a 3D grid, with integer x and y coordinates
 * @author badlogic
 *
 */
public class GridPoint3 {
	public int x;
	public int y;
	public int z;
	
	public GridPoint3() {
	}
	
	public GridPoint3(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public GridPoint3(GridPoint3 point) {
		this.x = point.x;
		this.y = point.y;
		this.z = point.z;
	}
	
	public GridPoint3 set(GridPoint3 point) {
		this.x = point.x;
		this.y = point.y;
		this.z = point.z;
		return this;
	}
	
	public GridPoint3 set(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	@Override
	public int hashCode () {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals (Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof GridPoint3)) return false;
		GridPoint3 other = (GridPoint3)obj;
		return this.x == other.x && this.y == other.y && this.z == other.z;
	}
}
