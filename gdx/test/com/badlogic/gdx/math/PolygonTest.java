/*******************************************************************************
 * Copyright 2024 See AUTHORS file.
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

package com.badlogic.gdx.math;

import static org.junit.Assert.*;

import org.junit.Test;

public class PolygonTest {

	@Test
	public void testZeroRotation () {
		float[] vertices = {0, 0, 3, 0, 3, 4};
		Polygon polygon = new Polygon(vertices);
		polygon.rotate(0);
		assertArrayEquals("The polygon's vertices don't correspond.", polygon.getTransformedVertices(), polygon.getVertices(), 1f);
	}

	@Test
	public void test360Rotation () {
		float[] vertices = {0, 0, 3, 0, 3, 4};
		Polygon polygon = new Polygon(vertices);
		polygon.rotate(360);
		assertArrayEquals("The polygon's vertices don't correspond.", polygon.getTransformedVertices(), polygon.getVertices(), 1f);
	}

	@Test
	public void testConcavePolygonArea () {
		float[] concaveVertices = {0, 0, 2, 4, 4, 0, 2, 2};
		Polygon concavePolygon = new Polygon(concaveVertices);
		float expectedArea = 4.0f;
		assertEquals("The area doesn't correspond.", expectedArea, concavePolygon.area(), 1f);
	}

	@Test
	public void testTriangleArea () {
		float[] triangleVertices = {0, 0, 2, 3, 4, 0};
		Polygon triangle = new Polygon(triangleVertices);
		float expectedArea = 6.0f;
		assertEquals("The area doesn't correspond.", expectedArea, triangle.area(), 1f);
	}
}
