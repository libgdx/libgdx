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

import com.badlogic.gdx.box2deditor.utils.VectorUtils;
import com.badlogic.gdx.math.Vector2;

/**
 *
 * @author Aurelien Ribon (aurelien.ribon@gmail.com)
 */
public class BodyModel {
	public static final BodyModel EMPTY = new BodyModel() {
		@Override public void set(Vector2[][] shapes, Vector2[][] polygons) {}
	};

	// -------------------------------------------------------------------------

	private Vector2[][] shapes;
	private Vector2[][] polygons;

	public void clearAll() {
		shapes = null;
		polygons = null;
	}

	public void set(Vector2[][] shapes, Vector2[][] polygons) {
		clearAll();
		this.shapes = VectorUtils.getCopy(shapes);
		this.polygons = VectorUtils.getCopy(polygons);
	}

	public Vector2[][] getShapes() {
		return VectorUtils.getCopy(shapes);
	}

	public Vector2[][] getPolygons() {
		return VectorUtils.getCopy(polygons);
	}
}