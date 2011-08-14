/*
 * Copyright 2011 Rod Hyde (rod@badlydrawngames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlydrawngames.veryangryrobots;

import com.badlogic.gdx.utils.IntArray;

import static com.badlogic.gdx.math.MathUtils.*;

/** This class makes a maze using the recursive division method described on Wikipedia. {@link http
 * ://en.wikipedia.org/wiki/Maze_generation_algorithm} */
public final class MazeGenerator {

	private int width;
	private int height;
	private int doorPos;

	private IntArray walls;
	private IntArray doors;

	public MazeGenerator (int width, int height) {
		// Pre: width is odd
		// Pre: height is odd
		// Pre: width > 2
		// Pre: height > 2
		this.width = width;
		this.height = height;
		walls = new IntArray();
		doors = new IntArray();
	}

	public MazeGenerator rebuild (int doorPos) {
		createMaze(doorPos);
		this.doorPos = doorPos;
		return this;
	}

	public int getWidth () {
		return width;
	}

	public int getHeight () {
		return height;
	}

	public int getDoorPos () {
		return doorPos;
	}

	public IntArray getWalls () {
		return walls;
	}

	public IntArray getDoors () {
		return doors;
	}

	private void createMaze (int doorPos) {
		walls.clear();
		doors.clear();
		addOuterWalls();
		addDoors(doorPos);
		subdivideChamber(0, 0, width, height);
	}

	private void addOuterWalls () {
		addHWall(0, width / 2, 0);
		addHWall(width / 2 + 1, width, 0);
		addHWall(0, width / 2, height);
		addHWall(width / 2 + 1, width, height);
		addVWall(0, 0, height / 2);
		addVWall(0, height / 2 + 1, height);
		addVWall(width, 0, height / 2);
		addVWall(width, height / 2 + 1, height);
	}

	private void subdivideChamber (int x1, int y1, int x2, int y2) {
		// Get the size of the chamber and bail if it is too small to divide.
		int w = x2 - x1;
		int h = y2 - y1;
		if (w < 2 || h < 2) return;

		// Pick a point in the middle of the chamber.
		int cx = random(x1 + 1, x2 - 1);
		int cy = random(y1 + 1, y2 - 1);

		// Choose the solid wall.
		int solid = random(0, 3);

		// Add the internal walls.
		addHWall(x1, cx, cy, solid == 0);
		addHWall(cx, x2, cy, solid == 1);
		addVWall(cx, y1, cy, solid == 2);
		addVWall(cx, cy, y2, solid == 3);

		// Create the subchambers.
		subdivideChamber(x1, y1, cx, cy);
		subdivideChamber(cx, y1, x2, cy);
		subdivideChamber(x1, cy, cx, y2);
		subdivideChamber(cx, cy, x2, y2);
	}

	private void addHWall (int x1, int x2, int y, boolean solid) {
		int length = x2 - x1;
		if (length < 2) return;
		if (!solid) {
			int x = random(x1, x2 - 1);
			addHWall(x1, x, y);
			addHWall(x + 1, x2, y);
		} else {
			addHWall(x1, x2, y);
		}
	}

	private void addHWall (int x1, int x2, int y) {
		if (x2 <= x1) return;
		addWall(x1, y, x2, y);
	}

	private void addVWall (int x, int y1, int y2, boolean solid) {
		int length = y2 - y1;
		if (length < 2) return;
		if (!solid) {
			int y = random(y1, y2 - 1);
			addVWall(x, y1, y);
			addVWall(x, y + 1, y2);
		} else {
			addVWall(x, y1, y2);
		}
	}

	private void addVWall (int x, int y1, int y2) {
		if (y2 <= y1) return;
		addWall(x, y1, x, y2);
	}

	private void addWall (int x1, int y1, int x2, int y2) {
		walls.add(x1);
		walls.add(y1);
		walls.add(x2);
		walls.add(y2);
	}

	private void addDoors (int doorPos) {
		if ((doorPos & DoorPositions.MIN_Y) == DoorPositions.MIN_Y) {
			addHDoor(width / 2, 0);
		}
		if ((doorPos & DoorPositions.MAX_Y) == DoorPositions.MAX_Y) {
			addHDoor(width / 2, height);
		}
		if ((doorPos & DoorPositions.MIN_X) == DoorPositions.MIN_X) {
			addVDoor(0, height / 2);
		}
		if ((doorPos & DoorPositions.MAX_X) == DoorPositions.MAX_X) {
			addVDoor(width, height / 2);
		}
	}

	private void addHDoor (int x, int y) {
		addDoor(x, y, x + 1, y);
	}

	private void addVDoor (int x, int y) {
		addDoor(x, y, x, y + 1);
	}

	private void addDoor (int x1, int y1, int x2, int y2) {
		doors.add(x1);
		doors.add(y1);
		doors.add(x2);
		doors.add(y2);
	}
}
