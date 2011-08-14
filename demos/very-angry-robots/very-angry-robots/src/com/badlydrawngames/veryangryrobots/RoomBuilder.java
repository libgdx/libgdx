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

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pool;
import com.badlydrawngames.general.Pools;

public class RoomBuilder {
	private static final float WALL_WIDTH = World.WALL_WIDTH;
	private static final float WALL_HEIGHT = World.WALL_HEIGHT;
	private static final float HALF_HEIGHT = WALL_HEIGHT / 2;
	private static final float ADJUSTMENT = World.OUTER_WALL_ADJUST;
	private static final int MAX_RECTANGLES = 256;
	private static final int MAX_DOORS = 4;
	private static final int MAX_WALLS = MAX_RECTANGLES - MAX_DOORS;

	private final MazeGenerator mazeGenerator;
	private final Pool<Rectangle> rectanglePool;
	private Array<Rectangle> doorRects;
	private Array<Rectangle> wallRects;
	private final int hcells;
	private final int vcells;

	public RoomBuilder (int hcells, int vcells) {
		this.hcells = hcells;
		this.vcells = vcells;
		mazeGenerator = new MazeGenerator(hcells, vcells);
		rectanglePool = new Pool<Rectangle>(MAX_RECTANGLES) {
			@Override
			protected Rectangle newObject () {
				return new Rectangle();
			}
		};
	}

	public void build (int doorPosition) {
		mazeGenerator.rebuild(doorPosition);
		createWallsAndDoors();
	}

	public Array<Rectangle> getWalls () {
		return wallRects;
	}

	public Array<Rectangle> getDoors () {
		return doorRects;
	}

	private void createWallsAndDoors () {
		createWalls();
		createDoors();
	}

	private void createWalls () {
		IntArray wallDefs = mazeGenerator.getWalls();
		int n = wallDefs.size / 4;
		wallRects = Pools.makeArrayFromPool(wallRects, rectanglePool, MAX_WALLS);
		for (int i = 0, j = 0; i < n; i++, j += 4) {
			Rectangle wallRect = createWall(wallDefs, j);
			wallRects.add(wallRect);
		}
	}

	private void createDoors () {
		IntArray doorDefs = mazeGenerator.getDoors();
		int n = doorDefs.size / 4;
		doorRects = Pools.makeArrayFromPool(doorRects, rectanglePool, MAX_DOORS);
		for (int i = 0, j = 0; i < n; i++, j += 4) {
			Rectangle doorRect = createDoor(doorDefs, j);
			doorRects.add(doorRect);
		}
	}

	private Rectangle createWall (IntArray wallDefs, int i) {
		int x1 = wallDefs.get(i);
		int y1 = wallDefs.get(i + 1);
		int x2 = wallDefs.get(i + 2);
		int y2 = wallDefs.get(i + 3);
		return (x1 == x2) ? createVWall(x1, y1, y2) : createHWall(x1, x2, y1);
	}

	private Rectangle createHWall (int x1, int x2, int y1) {
		float x = coordMinusHalfHeight(x1, hcells);
		float y = coordMinusHalfHeight(y1, vcells);
		float t = coordMinusHalfHeight(x2, hcells);
		float w = (t - x) + WALL_HEIGHT;
		float h = WALL_HEIGHT;
		return newRectangle(x, y, w, h);
	}

	private Rectangle createVWall (int x1, int y1, int y2) {
		float x = coordMinusHalfHeight(x1, hcells);
		float y = coordMinusHalfHeight(y1, vcells);
		float t = coordMinusHalfHeight(y2, vcells);
		float w = WALL_HEIGHT;
		float h = (t - y) + WALL_HEIGHT;
		return newRectangle(x, y, w, h);
	}

	private Rectangle createDoor (IntArray doorDefs, int i) {
		int x1 = doorDefs.get(i);
		int y1 = doorDefs.get(i + 1);
		int x2 = doorDefs.get(i + 2);
		return (x1 == x2) ? createVDoor(x1, y1) : createHDoor(x1, y1);
	}

	private Rectangle createHDoor (int x1, int y1) {
		float x = coordPlusHalfHeight(x1, hcells);
		float y = coordMinusHalfHeight(y1, vcells);
		float w = WALL_WIDTH - WALL_HEIGHT;
		float h = WALL_HEIGHT;
		return newRectangle(x, y, w, h);
	}

	private Rectangle createVDoor (int x1, int y1) {
		float x = coordMinusHalfHeight(x1, hcells);
		float y = coordPlusHalfHeight(y1, vcells);
		float w = WALL_HEIGHT;
		float h = WALL_WIDTH - WALL_HEIGHT;
		return newRectangle(x, y, w, h);
	}

	private float coordPlusHalfHeight (int c, int limit) {
		float n = ADJUSTMENT;
		if (c == 0) {
			return n + HALF_HEIGHT;
		} else if (c < limit) {
			return n + WALL_WIDTH - ADJUSTMENT + ((c - 1) * WALL_WIDTH) + HALF_HEIGHT;
		} else {
			return n + 2 * (WALL_WIDTH - ADJUSTMENT) + ((c - 2) * WALL_WIDTH) + HALF_HEIGHT;
		}
	}

	private float coordMinusHalfHeight (int c, int limit) {
		float n = ADJUSTMENT;
		if (c == 0) {
			return n - HALF_HEIGHT;
		} else if (c < limit) {
			return n + WALL_WIDTH - ADJUSTMENT + ((c - 1) * WALL_WIDTH) - HALF_HEIGHT;
		} else {
			return n + 2 * (WALL_WIDTH - ADJUSTMENT) + ((c - 2) * WALL_WIDTH) - HALF_HEIGHT;
		}
	}

	private Rectangle newRectangle (float x, float y, float w, float h) {
		Rectangle r = rectanglePool.obtain();
		r.x = x;
		r.y = y;
		r.width = w;
		r.height = h;
		return r;
	}
}
