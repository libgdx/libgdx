package com.badlogic.gdx.math;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PolygonTest {

	@Test
	public void testZeroRotation() {
		float[] vertices = {0, 0, 3, 0, 3, 4};
		Polygon polygon = new Polygon(vertices);
		polygon.rotate(0);
		assertArrayEquals("The polygons vertices don't correspond.", polygon.getTransformedVertices(), polygon.getVertices(), 1f);
	}
	
	@Test
	public void test360Rotation() {
		float[] vertices = {0, 0, 3, 0, 3, 4};
		Polygon polygon = new Polygon(vertices);
		polygon.rotate(360);
		assertArrayEquals("The polygons vertices don't correspond.", polygon.getTransformedVertices(), polygon.getVertices(), 1f);
	}

	@Test
	public void testConcavePolygonArea() {
		float[] concaveVertices = {0, 0, 2, 4, 4, 0, 2, 2};
		Polygon concavePolygon = new Polygon(concaveVertices);
		float expectedArea = 4.0f;
		assertEquals("The area doesn't correspond.", expectedArea, concavePolygon.area(), 1f);
	}
	
	@Test
	public void testTriangleArea() {
		float[] triangleVertices = {0,0,2,3,4,0};
		Polygon triangle = new Polygon(triangleVertices);
		float expectedArea = 6.0f;
		assertEquals("The area doesn't correspond.", expectedArea, triangle.area(), 1f);
		
	}
}
