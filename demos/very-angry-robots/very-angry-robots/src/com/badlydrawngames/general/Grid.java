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

package com.badlydrawngames.general;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import static com.badlydrawngames.general.MathUtils.*;

public class Grid {

	private final int cols;
	private final int rows;
	private final float cellWidth;
	private final float cellHeight;
	private final Array<Array<Rectangle>> grid;
	private Array<Rectangle> result;

	public Grid (int cols, int rows, float width, float height) {
		this.cols = cols;
		this.rows = rows;
		this.cellWidth = width / cols;
		this.cellHeight = height / rows;
		this.grid = new Array<Array<Rectangle>>(cols * rows);
		for (int i = 0; i < cols * rows; i++) {
			grid.add(new Array<Rectangle>());
		}
		this.result = new Array<Rectangle>();
	}

	public void clear () {
		for (Array<Rectangle> v : grid) {
			v.clear();
		}
	}

	public void add (Rectangle r) {
		int minX = max(0, cellX(r.x));
		int maxX = min(cols - 1, cellX(r.x + r.width));
		int minY = max(0, cellY(r.y));
		int maxY = min(rows - 1, cellY(r.y + r.height));
		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				int shv = getGridCell(x, y);
				Array<Rectangle> values = grid.get(shv);
				values.add(r);
			}
		}
	}

	public Array<Rectangle> get (Rectangle r) {
		result.clear();
		int minX = max(0, cellX(r.x));
		int maxX = min(cols - 1, cellX(r.x + r.width));
		int minY = max(0, cellY(r.y));
		int maxY = min(rows - 1, cellY(r.y + r.height));
		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				int shv = getGridCell(x, y);
				result.addAll(grid.get(shv));
			}
		}
		return result;
	}

	private int cellX (float x) {
		return (int)(x / cellWidth);
	}

	private int cellY (float y) {
		return (int)(y / cellHeight);
	}

	private int getGridCell (int x, int y) {
		return x + y * cols;
	}
}
