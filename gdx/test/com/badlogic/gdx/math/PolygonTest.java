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
import org.junit.Before;
import org.junit.Test;

public class PolygonTest {

	private Polygon polygon;

	@Before
	public void setUp () {
		// Create a simple triangle for the tests
		float[] vertices = {0, 0, 3, 0, 3, 4};
		polygon = new Polygon(vertices);
	}

	@Test
	public void testGetVertices () {
		float[] expectedVertices = {0, 0, 3, 0, 3, 4};
		assertArrayEquals("The polygons vertices don't correspond.", expectedVertices, polygon.getVertices(), 0.001f);
	}

	@Test
	public void testSetPosition () {
		polygon.setPosition(2, 3);
		assertEquals("The X position of the polygon is incorrect.", 2, polygon.getX(), 0.001f);
		assertEquals("The Y position of the polygon is incorrect.", 3, polygon.getY(), 0.001f);
	}

	@Test
	public void testSetOrigin () {
		polygon.setOrigin(1, 2);
		assertEquals("The X position of the polygon is incorrect.", 1, polygon.getOriginX(), 0.001f);
		assertEquals("The Y position of the polygon is incorrect.", 2, polygon.getOriginY(), 0.001f);
	}

	@Test
	public void testSetVertices () {
		float[] newVertices = {1, 1, 4, 1, 4, 5};
		polygon.setVertices(newVertices);
		assertArrayEquals("The new vertices are not correctly defined.", newVertices, polygon.getVertices(), 0.001f);
	}

	@Test
	public void testGetTransformedVertices () {
		polygon.setPosition(1, 1);
		polygon.setRotation(90);
		polygon.setScale(2, 2);
		float[] transformedVertices = polygon.getTransformedVertices();
		assertNotNull("Transformed vertices shouldn't be null.", transformedVertices);
		assertTrue("Transformed vertices should contain values.", transformedVertices.length > 0);
	}

	@Test
	public void testContainsPoint () {
		assertTrue("The point (1, 1) should be in the polygon.", polygon.contains(1, 1));
		assertFalse("The point (5, 5) shouldn't be in the polygon.", polygon.contains(5, 5));
	}

	@Test
	public void testSetRotation () {
		polygon.setRotation(45);
		assertEquals("The polygon is incorrect.", 45, polygon.getRotation(), 0.001f);
	}

	@Test
	public void testTranslate () {
		polygon.setPosition(0, 0);
		polygon.translate(5, 5);
		assertEquals("The X translation of the polygon is incorrect.", 5, polygon.getX(), 0.001f);
		assertEquals("The Y translation of the polygon is incorrect.", 5, polygon.getY(), 0.001f);
	}

	@Test
	public void testArea () {
		float expectedArea = 6.0f;
		assertEquals("The polygon's area is incorrect.", expectedArea, polygon.area(), 0.01f);
	}

	@Test
	public void testGetBoundingRectangle () {
		polygon.setPosition(1, 1);
		Rectangle bounds = polygon.getBoundingRectangle();
		assertNotNull("The bounding rectangle shouldn't be null.", bounds);
		assertEquals("The x coordinate of the bounding rectangle is incorrect.", 1, bounds.x, 0.001f);
		assertEquals("The y coordinate of the bounding rectangle is incorrect.", 1, bounds.y, 0.001f);
	}

	@Test
	public void testInvalidSetVertices () {
		float[] invalidVertices = {0, 0, 1, 1}; // Less than 3 vertices
		Exception exception = null;
		try {
			polygon.setVertices(invalidVertices);
		} catch (IllegalArgumentException e) {
			exception = e;
		}
		assertNotNull("An exception should be raised for invalid vertices.", exception);
		assertEquals("The error message is incorrect.", "polygons must contain at least 3 points.", exception.getMessage());
	}

}
