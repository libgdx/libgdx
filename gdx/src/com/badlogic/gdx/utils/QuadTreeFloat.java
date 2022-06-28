/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.utils;

import java.util.Arrays;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Pool.Poolable;

/** A quad tree that stores a float for each point.
 * @author Nathan Sweet */
public class QuadTreeFloat implements Poolable {
	static public final int VALUE = 0, X = 1, Y = 2, DISTSQR = 3;

	static private final Pool<QuadTreeFloat> pool = new Pool(128, 4096) {
		protected Object newObject () {
			return new QuadTreeFloat();
		}
	};

	public final int maxValues, maxDepth;
	public float x, y, width, height;
	public int depth;
	public @Null QuadTreeFloat nw, ne, sw, se;

	/** For each entry, stores the value, x, and y. */
	public float[] values;

	/** The number of elements stored in {@link #values} (3 values per quad tree entry). */
	public int count;

	/** Creates a quad tree with 16 for maxValues and 8 for maxDepth. */
	public QuadTreeFloat () {
		this(16, 8);
	}

	/** @param maxValues The maximum number of values stored in each quad tree node. When exceeded, the node is split into 4 child
	 *           nodes. If the maxDepth has been reached, more than maxValues may be stored.
	 * @param maxDepth The maximum depth of the tree nodes. Nodes at the maxDepth will not be split and may store more than
	 *           maxValues number of entries. */
	public QuadTreeFloat (int maxValues, int maxDepth) {
		this.maxValues = maxValues * 3;
		this.maxDepth = maxDepth;
		values = new float[this.maxValues];
	}

	public void setBounds (float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void add (float value, float valueX, float valueY) {
		int count = this.count;
		if (count == -1) {
			addToChild(value, valueX, valueY);
			return;
		}
		if (depth < maxDepth) {
			if (count == maxValues) {
				split(value, valueX, valueY);
				return;
			}
		} else if (count == values.length) //
			values = Arrays.copyOf(values, growValues());
		values[count] = value;
		values[count + 1] = valueX;
		values[count + 2] = valueY;
		this.count += 3;
	}

	private void split (float value, float valueX, float valueY) {
		float[] values = this.values;
		for (int i = 0; i < maxValues; i += 3)
			addToChild(values[i], values[i + 1], values[i + 2]);
		// values isn't nulled because the trees are pooled.
		count = -1;
		addToChild(value, valueX, valueY);
	}

	private void addToChild (float value, float valueX, float valueY) {
		QuadTreeFloat child;
		float halfWidth = width / 2, halfHeight = height / 2;
		if (valueX < x + halfWidth) {
			if (valueY < y + halfHeight)
				child = sw != null ? sw : (sw = obtainChild(x, y, halfWidth, halfHeight, depth + 1));
			else
				child = nw != null ? nw : (nw = obtainChild(x, y + halfHeight, halfWidth, halfHeight, depth + 1));
		} else {
			if (valueY < y + halfHeight)
				child = se != null ? se : (se = obtainChild(x + halfWidth, y, halfWidth, halfHeight, depth + 1));
			else
				child = ne != null ? ne : (ne = obtainChild(x + halfWidth, y + halfHeight, halfWidth, halfHeight, depth + 1));
		}
		child.add(value, valueX, valueY);
	}

	private QuadTreeFloat obtainChild (float x, float y, float width, float height, int depth) {
		QuadTreeFloat child = pool.obtain();
		child.x = x;
		child.y = y;
		child.width = width;
		child.height = height;
		child.depth = depth;
		return child;
	}

	/** Returns a new length for {@link #values} when it is not enough to hold all the entries after {@link #maxDepth} has been
	 * reached. */
	protected int growValues () {
		return count + 10 * 3;
	}

	/** @param results For each entry found within the radius, if any, the value, x, y, and square of the distance to the entry are
	 *           added to this array. See {@link #VALUE}, {@link #X}, {@link #Y}, and {@link #DISTSQR}. */
	public void query (float centerX, float centerY, float radius, FloatArray results) {
		query(centerX, centerY, radius * radius, centerX - radius, centerY - radius, radius * 2, results);
	}

	private void query (float centerX, float centerY, float radiusSqr, float rectX, float rectY, float rectSize,
		FloatArray results) {
		if (!(x < rectX + rectSize && x + width > rectX && y < rectY + rectSize && y + height > rectY)) return;
		int count = this.count;
		if (count != -1) {
			float[] values = this.values;
			for (int i = 1; i < count; i += 3) {
				float px = values[i], py = values[i + 1];
				float dx = px - centerX, dy = py - centerY;
				float d = dx * dx + dy * dy;
				if (d <= radiusSqr) {
					results.add(values[i - 1]);
					results.add(px);
					results.add(py);
					results.add(d);
				}
			}
		} else {
			if (nw != null) nw.query(centerX, centerY, radiusSqr, rectX, rectY, rectSize, results);
			if (sw != null) sw.query(centerX, centerY, radiusSqr, rectX, rectY, rectSize, results);
			if (ne != null) ne.query(centerX, centerY, radiusSqr, rectX, rectY, rectSize, results);
			if (se != null) se.query(centerX, centerY, radiusSqr, rectX, rectY, rectSize, results);
		}
	}

	/** @param results For each entry found within the rectangle, if any, the value, x, and y of the entry are added to this array.
	 *           See {@link #VALUE}, {@link #X}, and {@link #Y}. */
	public void query (Rectangle rect, FloatArray results) {
		if (x >= rect.x + rect.width || x + width <= rect.x || y >= rect.y + rect.height || y + height <= rect.y) return;
		int count = this.count;
		if (count != -1) {
			float[] values = this.values;
			for (int i = 1; i < count; i += 3) {
				float px = values[i], py = values[i + 1];
				if (rect.contains(px, py)) {
					results.add(values[i - 1]);
					results.add(px);
					results.add(py);
				}
			}
		} else {
			if (nw != null) nw.query(rect, results);
			if (sw != null) sw.query(rect, results);
			if (ne != null) ne.query(rect, results);
			if (se != null) se.query(rect, results);
		}
	}

	/** @param result For the entry nearest to the specified point, the value, x, y, and square of the distance to the value are
	 *           added to this array after it is cleared. See {@link #VALUE}, {@link #X}, {@link #Y}, and {@link #DISTSQR}.
	 * @return false if no entry was found because the quad tree was empty or the specified point is farther than the larger of the
	 *         quad tree's width or height from an entry. If false is returned the result array is empty. */
	public boolean nearest (float x, float y, FloatArray result) {
		// Find nearest value in a cell that contains the point.
		result.clear();
		result.add(0);
		result.add(0);
		result.add(0);
		result.add(Float.POSITIVE_INFINITY);
		findNearestInternal(x, y, result);
		float nearValue = result.first(), nearX = result.get(1), nearY = result.get(2), nearDist = result.get(3);
		boolean found = nearDist != Float.POSITIVE_INFINITY;
		if (!found) {
			nearDist = Math.max(width, height);
			nearDist *= nearDist;
		}

		// Check for a nearer value in a neighboring cell.
		result.clear();
		query(x, y, (float)Math.sqrt(nearDist), result);
		for (int i = 3, n = result.size; i < n; i += 4) {
			float dist = result.get(i);
			if (dist < nearDist) {
				nearDist = dist;
				nearValue = result.get(i - 3);
				nearX = result.get(i - 2);
				nearY = result.get(i - 1);
			}
		}
		if (!found && result.isEmpty()) return false;
		result.clear();
		result.add(nearValue);
		result.add(nearX);
		result.add(nearY);
		result.add(nearDist);
		return true;
	}

	private void findNearestInternal (float x, float y, FloatArray result) {
		if (!(this.x < x && this.x + width > x && this.y < y && this.y + height > y)) return;

		int count = this.count;
		if (count != -1) {
			float nearValue = result.first(), nearX = result.get(1), nearY = result.get(2), nearDist = result.get(3);
			float[] values = this.values;
			for (int i = 1; i < count; i += 3) {
				float px = values[i], py = values[i + 1];
				float dx = px - x, dy = py - y;
				float dist = dx * dx + dy * dy;
				if (dist < nearDist) {
					nearDist = dist;
					nearValue = values[i - 1];
					nearX = px;
					nearY = py;
				}
			}
			result.set(0, nearValue);
			result.set(1, nearX);
			result.set(2, nearY);
			result.set(3, nearDist);
		} else {
			if (nw != null) nw.findNearestInternal(x, y, result);
			if (sw != null) sw.findNearestInternal(x, y, result);
			if (ne != null) ne.findNearestInternal(x, y, result);
			if (se != null) se.findNearestInternal(x, y, result);
		}
	}

	public void reset () {
		if (count == -1) {
			if (nw != null) {
				pool.free(nw);
				nw = null;
			}
			if (sw != null) {
				pool.free(sw);
				sw = null;
			}
			if (ne != null) {
				pool.free(ne);
				ne = null;
			}
			if (se != null) {
				pool.free(se);
				se = null;
			}
		}
		count = 0;
		if (values.length > maxValues) values = new float[maxValues];
	}
}
