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
}
