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

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Texture;

/** A PolygonBatch is an extension of the Batch interface that provides additional render methods specifically for rendering
 * polygons
 *
 * <p>
 * @author mzechner
 * @author Nathan Sweet */
public interface PolygonBatch extends Batch {

	/** Draws a polygon region with the bottom left corner at x,y having the width and height of the region. */
	void draw (PolygonRegion region, float x, float y);

	/** Draws a polygon region with the bottom left corner at x,y and stretching the region to cover the given width and height. */
	void draw (PolygonRegion region, float x, float y, float width, float height);

	/** Draws the polygon region with the bottom left corner at x,y and stretching the region to cover the given width and height.
	 * The polygon region is offset by originX, originY relative to the origin. Scale specifies the scaling factor by which the
	 * polygon region should be scaled around originX, originY. Rotation specifies the angle of counter clockwise rotation of the
	 * rectangle around originX, originY. */
	void draw (PolygonRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX,
		float scaleY, float rotation);

	/** Draws the polygon using the given vertices and triangles. Each vertices must be made up of 5 elements in this order: x, y,
	 * color, u, v. */
	void draw (Texture texture, float[] polygonVertices, int verticesOffset, int verticesCount, short[] polygonTriangles,
		int trianglesOffset, int trianglesCount);
}
