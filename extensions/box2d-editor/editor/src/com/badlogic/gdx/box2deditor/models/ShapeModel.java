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
package com.badlogic.gdx.box2deditor.models;

import com.badlogic.gdx.box2deditor.utils.ShapeUtils;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Aurelien Ribon (aurelien.ribon@gmail.com)
 */
public class ShapeModel {
    private final List<Vector2> points = new ArrayList<Vector2>();
	private boolean isClosed = false;

	public ShapeModel() {
	}

	public ShapeModel(Vector2[] points) {
		this.points.addAll(Arrays.asList(points));
	}

	public void addPoint(Vector2 p) {
		points.add(p);
	}

	public void addPoint(int idx, Vector2 p) {
		points.add(idx, p);
	}

	public void removePoint(Vector2 p) {
		points.remove(p);
	}

	public void removePoint(int idx) {
		points.remove(idx);
	}

	public void setPoints(Vector2[] points) {
		this.points.addAll(Arrays.asList(points));
	}

	public Vector2[] getPoints() {
		return points.toArray(new Vector2[points.size()]);
	}

	public Vector2 getPoint(int idx) {
		return points.get(idx);
	}

	public Vector2 getLastPoint() {
		return points.get(points.size()-1);
	}

	public void close() {
		isClosed = true;
		if (ShapeUtils.isPolygonCCW(points.toArray(new Vector2[points.size()])))
			Collections.reverse(points);
	}

	public boolean isClosed() {
		return isClosed;
	}

	public int getPointCount() {
		return points.size();
	}
}