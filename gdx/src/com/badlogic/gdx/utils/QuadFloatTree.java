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

import com.badlogic.gdx.utils.Pool.Poolable;

/** @author Nathan Sweet */
public class QuadFloatTree implements Poolable {
	static private final int MAX_VALUES = 16 * 3;
	static public final int MAX_DEPTH = 8;

	static private final Pool<QuadFloatTree> pool = new Pool(128, 4096) {
		protected Object newObject () {
			return new QuadFloatTree();
		}
	};

	public float x, y, width, height;
	public int depth, count;
	public float[] values = new float[MAX_VALUES];
	public QuadFloatTree nw, ne, sw, se;

	public void set (float x, float y, float width, float height) {
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
		if (depth < MAX_DEPTH) {
			if (count == MAX_VALUES) {
				split(value, valueX, valueY);
				return;
			}
		} else if (count == values.length) //
			grow();
		values[count] = valueX;
		values[count + 1] = valueY;
		values[count + 2] = value;
		this.count += 3;
	}

	private void split (float value, float valueX, float valueY) {
		float[] values = this.values;
		for (int i = 0; i < MAX_VALUES; i += 3)
			addToChild(values[i + 2], values[i], values[i + 1]);
		// values isn't nulled because the trees are pooled.
		count = -1;
		addToChild(value, valueX, valueY);
	}

	private void addToChild (float value, float valueX, float valueY) {
		QuadFloatTree child;
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

	private QuadFloatTree obtainChild (float x, float y, float width, float height, int depth) {
		QuadFloatTree child = pool.obtain();
		child.x = x;
		child.y = y;
		child.width = width;
		child.height = height;
		child.depth = depth;
		return child;
	}

	private void grow () {
		values = Arrays.copyOf(values, count + 10 * 3);
	}

	public void query (float centerX, float centerY, float radius, FloatArray results) {
		query(centerX, centerY, radius * radius, centerX - radius, centerY - radius, radius * 2, results);
	}

	private void query (float centerX, float centerY, float radiusSqr, float rectX, float rectY, float rectSize,
		FloatArray results) {
		if (!(x < rectX + rectSize && x + width > rectX && y < rectY + rectSize && y + height > rectY)) return;
		int count = this.count;
		if (count != -1) {
			float[] values = this.values;
			for (int i = 0; i < count; i += 3) {
				float dx = centerX - values[i], dy = centerY - values[i + 1];
				float d = dx * dx + dy * dy;
				if (d <= radiusSqr) {
					results.add(values[i + 2]);
					results.add(1 - (float)Math.sqrt(d / radiusSqr));
				}
			}
		} else {
			if (nw != null) nw.query(centerX, centerY, radiusSqr, rectX, rectY, rectSize, results);
			if (sw != null) sw.query(centerX, centerY, radiusSqr, rectX, rectY, rectSize, results);
			if (ne != null) ne.query(centerX, centerY, radiusSqr, rectX, rectY, rectSize, results);
			if (se != null) se.query(centerX, centerY, radiusSqr, rectX, rectY, rectSize, results);
		}
	}

	static int nearestTested = 0;

	public void findNearest (float x, float y, FloatArray result) {
		nearestTested = 0;
		result.clear();
		result.add(-1);
		result.add(Float.POSITIVE_INFINITY);
		result.add(0);

		findNearestInternal(x, y, result);

		float nearestValue = result.get(0);
		float nearestDist = result.get(1);

		if (nearestDist == Float.POSITIVE_INFINITY) {
			nearestDist = width * width;
		}

		result.clear();
		querySquared(x, y, (float)Math.sqrt(nearestDist), result);
		for (int i = 0, n = result.size; i < n; i += 2) {
			float dist = result.get(i + 1);
			if (dist < nearestDist) {
				nearestDist = dist;
				nearestValue = result.get(i);
			}
		}

		result.clear();
		result.add(nearestValue);
		result.add(nearestDist);
		result.add(nearestTested);
	}

	private void findNearestInternal (float x, float y, FloatArray result) {
		if (!(this.x < x && this.x + width > x && this.y < y && this.y + height > y)) return;

		float nearestValue = result.get(0);
		float nearestDist = result.get(1);

		int count = this.count;
		if (count != -1) {
			float[] values = this.values;
			for (int i = 0; i < count; i += 3) {
				float px = values[i];
				float py = values[i + 1];
				float dx = px - x, dy = py - y;
				float dst = dx * dx + dy * dy;
				if (dst < nearestDist) {
					nearestDist = dst;
					nearestValue = values[i + 2];
				}
				nearestTested++;
			}
			result.set(0, nearestValue);
			result.set(1, nearestDist);
		} else {
			if (nw != null) nw.findNearestInternal(x, y, result);
			if (sw != null) sw.findNearestInternal(x, y, result);
			if (ne != null) ne.findNearestInternal(x, y, result);
			if (se != null) se.findNearestInternal(x, y, result);
		}
	}

	public void querySquared (float centerX, float centerY, float radius, FloatArray results) {
		querySquared(centerX, centerY, radius * radius, centerX - radius, centerY - radius, radius * 2, results);
	}

	private void querySquared (float centerX, float centerY, float radiusSqr, float rectX, float rectY, float rectSize,
		FloatArray results) {
		if (!(x < rectX + rectSize && x + width > rectX && y < rectY + rectSize && y + height > rectY)) return;
		int count = this.count;
		if (count != -1) {
			float[] values = this.values;
			for (int i = 0; i < count; i += 3) {
				float dx = centerX - values[i], dy = centerY - values[i + 1];
				float d = dx * dx + dy * dy;
				if (d <= radiusSqr) {
					results.add(values[i + 2]);
					results.add(d);
				}
				nearestTested++;
			}
		} else {
			if (nw != null) nw.querySquared(centerX, centerY, radiusSqr, rectX, rectY, rectSize, results);
			if (sw != null) sw.querySquared(centerX, centerY, radiusSqr, rectX, rectY, rectSize, results);
			if (ne != null) ne.querySquared(centerX, centerY, radiusSqr, rectX, rectY, rectSize, results);
			if (se != null) se.querySquared(centerX, centerY, radiusSqr, rectX, rectY, rectSize, results);
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
		if (values.length > MAX_VALUES) values = new float[MAX_VALUES];
	}
}
