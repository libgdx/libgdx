
package com.badlogic.gdx.math;

import com.badlogic.gdx.math.Intersector.SplitTriangle;
import org.junit.Test;

import static org.junit.Assert.*;

public class IntersectorTest {

	/** Compares two triangles for equality. Triangles must have the same winding, but may begin with different vertex. Values are
	 * epsilon compared, with default tolerance. Triangles are assumed to be valid triangles - no duplicate vertices. */
	private static boolean triangleEquals (float[] base, int baseOffset, int stride, float[] comp) {
		assertTrue(stride >= 3);
		assertTrue(base.length - baseOffset >= 9);
		assertTrue(comp.length == 9);

		int offset = -1;
		// Find first comp vertex in base triangle
		for (int i = 0; i < 3; i++) {
			int b = baseOffset + i * stride;
			if (MathUtils.isEqual(base[b], comp[0]) && MathUtils.isEqual(base[b + 1], comp[1])
				&& MathUtils.isEqual(base[b + 2], comp[2])) {
				offset = i;
				break;
			}
		}
		assertTrue("Triangles do not have common first vertex.", offset != -1);
		// Compare vertices
		for (int i = 0; i < 3; i++) {
			int b = baseOffset + ((offset + i) * stride) % (3 * stride);
			int c = i * stride;
			if (!MathUtils.isEqual(base[b], comp[c]) || !MathUtils.isEqual(base[b + 1], comp[c + 1])
				|| !MathUtils.isEqual(base[b + 2], comp[c + 2])) {
				return false;
			}
		}
		return true;
	}

	@Test
	public void testSplitTriangle () {
		Plane plane = new Plane(new Vector3(1, 0, 0), 0);
		SplitTriangle split = new SplitTriangle(3);

		{// All back
			float[] fTriangle = {-10, 0, 10, -1, 0, 0, -12, 0, 10}; // Whole triangle on the back side
			Intersector.splitTriangle(fTriangle, plane, split);
			assertTrue(split.numBack == 1);
			assertTrue(split.numFront == 0);
			assertTrue(split.total == 1);
			assertTrue(triangleEquals(split.back, 0, 3, fTriangle));

			fTriangle[4] = 5f;
			assertFalse("Test is broken", triangleEquals(split.back, 0, 3, fTriangle));
		}

		{// All front
			float[] fTriangle = {10, 0, 10, 1, 0, 0, 12, 0, 10}; // Whole triangle on the front side
			Intersector.splitTriangle(fTriangle, plane, split);
			assertTrue(split.numBack == 0);
			assertTrue(split.numFront == 1);
			assertTrue(split.total == 1);
			assertTrue(triangleEquals(split.front, 0, 3, fTriangle));
		}

		{// Two back, one front
			float[] triangle = {-10, 0, 10, 10, 0, 0, -10, 0, -10}; // ABC One vertex in front, two in back
			Intersector.splitTriangle(triangle, plane, split); // Split points are D (0,0,5) and E (0,0,-5)
			assertTrue(split.numBack == 2);
			assertTrue(split.numFront == 1);
			assertTrue(split.total == 3);
			// There is only one way to triangulate front
			assertTrue(triangleEquals(split.front, 0, 3, new float[] {0, 0, 5, 10, 0, 0, 0, 0, -5}));

			// There are two ways to triangulate back
			float[][] firstWay = {{-10, 0, 10, 0, 0, 5, 0, 0, -5}, {-10, 0, 10, 0, 0, -5, -10, 0, -10}};// ADE AEC
			float[][] secondWay = {{-10, 0, 10, 0, 0, 5, -10, 0, -10}, {0, 0, 5, 0, 0, -5, -10, 0, -10}};// ADC DEC
			float[] base = split.back;
			boolean first = (triangleEquals(base, 0, 3, firstWay[0]) && triangleEquals(base, 9, 3, firstWay[1]))
				|| (triangleEquals(base, 0, 3, firstWay[1]) && triangleEquals(base, 9, 3, firstWay[0]));
			boolean second = (triangleEquals(base, 0, 3, secondWay[0]) && triangleEquals(base, 9, 3, secondWay[1]))
				|| (triangleEquals(base, 0, 3, secondWay[1]) && triangleEquals(base, 9, 3, secondWay[0]));
			assertTrue("Either first or second way must be right (first: " + first + ", second: " + second + ")", first ^ second);
		}

		{// Two front, one back
			float[] triangle = {10, 0, 10, -10, 0, 0, 10, 0, -10}; // ABC One vertex in back, two in front
			Intersector.splitTriangle(triangle, plane, split); // Split points are D (0,0,5) and E (0,0,-5)
			assertTrue(split.numBack == 1);
			assertTrue(split.numFront == 2);
			assertTrue(split.total == 3);
			// There is only one way to triangulate back
			assertTrue(triangleEquals(split.back, 0, 3, new float[] {0, 0, 5, -10, 0, 0, 0, 0, -5}));

			// There are two ways to triangulate front
			float[][] firstWay = {{10, 0, 10, 0, 0, 5, 0, 0, -5}, {10, 0, 10, 0, 0, -5, 10, 0, -10}};// ADE AEC
			float[][] secondWay = {{10, 0, 10, 0, 0, 5, 10, 0, -10}, {0, 0, 5, 0, 0, -5, 10, 0, -10}};// ADC DEC
			float[] base = split.front;
			boolean first = (triangleEquals(base, 0, 3, firstWay[0]) && triangleEquals(base, 9, 3, firstWay[1]))
				|| (triangleEquals(base, 0, 3, firstWay[1]) && triangleEquals(base, 9, 3, firstWay[0]));
			boolean second = (triangleEquals(base, 0, 3, secondWay[0]) && triangleEquals(base, 9, 3, secondWay[1]))
				|| (triangleEquals(base, 0, 3, secondWay[1]) && triangleEquals(base, 9, 3, secondWay[0]));
			assertTrue("Either first or second way must be right (first: " + first + ", second: " + second + ")", first ^ second);
		}
	}

	@Test
	public void intersectSegmentCircle () {
		Circle circle = new Circle(5f, 5f, 4f);
		// Segment intersects, both segment points outside circle
		boolean intersects = Intersector.intersectSegmentCircle(new Vector2(0, 1f), new Vector2(12f, 3f), circle, null);
		assertTrue(intersects);
		// Segment intersects, only one of the points inside circle (and is aligned with center)
		intersects = Intersector.intersectSegmentCircle(new Vector2(0, 5f), new Vector2(2f, 5f), circle, null);
		assertTrue(intersects);
		// Segment intersects, no points outside circle
		intersects = Intersector.intersectSegmentCircle(new Vector2(5.5f, 6f), new Vector2(7f, 5.5f), circle, null);
		assertTrue(intersects);
		// Segment doesn't intersect
		intersects = Intersector.intersectSegmentCircle(new Vector2(0f, 6f), new Vector2(0.5f, 2f), circle, null);
		assertFalse(intersects);
		// Segment is parallel to Y axis left of circle's center
		Intersector.MinimumTranslationVector mtv = new Intersector.MinimumTranslationVector();
		intersects = Intersector.intersectSegmentCircle(new Vector2(1.5f, 6f), new Vector2(1.5f, 3f), circle, mtv);
		assertTrue(intersects);
		assertTrue(mtv.normal.equals(new Vector2(-1f, 0)));
		assertTrue(mtv.depth == 0.5f);
		// Segment contains circle center point
		intersects = Intersector.intersectSegmentCircle(new Vector2(4f, 5f), new Vector2(6f, 5f), circle, mtv);
		assertTrue(intersects);
		assertTrue(mtv.normal.equals(new Vector2(0, 1f)) || mtv.normal.equals(new Vector2(0f, -1f)));
		assertTrue(mtv.depth == 4f);
		// Segment contains circle center point which is the same as the end point
		intersects = Intersector.intersectSegmentCircle(new Vector2(4f, 5f), new Vector2(5f, 5f), circle, mtv);
		assertTrue(intersects);
		assertTrue(mtv.normal.equals(new Vector2(0, 1f)) || mtv.normal.equals(new Vector2(0f, -1f)));
		assertTrue(mtv.depth == 4f);
	}

	@Test
	public void testIntersectPlanes () {
		final int NEAR = 0;
		final int FAR = 1;
		final int LEFT = 2;
		final int RIGHT = 3;
		final int TOP = 4;
		final int BOTTOM = 5;

		/*
		 * camera = new PerspectiveCamera(60, 1280, 720); camera.direction.set(0, 0, 1); camera.near = 0.1f; camera.far = 100f;
		 * camera.update(); Plane[] planes = camera.frustum.planes;
		 */
		Plane[] planes = new Plane[6];
		planes[NEAR] = new Plane(new Vector3(0.0f, 0.0f, 1.0f), -0.1f);
		planes[FAR] = new Plane(new Vector3(0.0f, -0.0f, -1.0f), 99.99771f);
		planes[LEFT] = new Plane(new Vector3(-0.69783056f, 0.0f, 0.71626294f), -9.3877316E-7f);
		planes[RIGHT] = new Plane(new Vector3(0.6978352f, 0.0f, 0.71625835f), -0.0f);
		planes[TOP] = new Plane(new Vector3(0.0f, -0.86602545f, 0.5f), -0.0f);
		planes[BOTTOM] = new Plane(new Vector3(-0.0f, 0.86602545f, 0.5f), -0.0f);

		Vector3 intersection = new Vector3();
		Intersector.intersectPlanes(planes[TOP], planes[FAR], planes[LEFT], intersection);
		assertEquals(102.63903f, intersection.x, 0.1f);
		assertEquals(57.7337f, intersection.y, 0.1f);
		assertEquals(100, intersection.z, 0.1f);

		Intersector.intersectPlanes(planes[TOP], planes[FAR], planes[RIGHT], intersection);
		assertEquals(-102.63903f, intersection.x, 0.1f);
		assertEquals(57.7337f, intersection.y, 0.1f);
		assertEquals(100, intersection.z, 0.1f);

		Intersector.intersectPlanes(planes[BOTTOM], planes[FAR], planes[LEFT], intersection);
		assertEquals(102.63903f, intersection.x, 0.1f);
		assertEquals(-57.7337f, intersection.y, 0.1f);
		assertEquals(100, intersection.z, 0.1f);

		Intersector.intersectPlanes(planes[BOTTOM], planes[FAR], planes[RIGHT], intersection);
		assertEquals(-102.63903f, intersection.x, 0.1f);
		assertEquals(-57.7337f, intersection.y, 0.1f);
		assertEquals(100, intersection.z, 0.1f);
	}

	@Test
	public void testIsPointInTriangle2D () {
		assertFalse(Intersector.isPointInTriangle(new Vector2(0.1f, 0), new Vector2(0, 0), new Vector2(1, 1), new Vector2(-1, -1)));

		assertTrue(Intersector.isPointInTriangle(new Vector2(0, 0.1f), new Vector2(-1, 1), new Vector2(1, 1), new Vector2(-1, -2)));
	}

	@Test
	public void testIsPointInTriangle3D () {
		// 2D ---
		assertFalse(Intersector.isPointInTriangle(new Vector3(0.1f, 0, 0), new Vector3(0, 0, 0), new Vector3(1, 1, 0),
			new Vector3(-1, -1, 0)));

		assertTrue(Intersector.isPointInTriangle(new Vector3(0, 0.1f, 0), new Vector3(-1, 1, 0), new Vector3(1, 1, 0),
			new Vector3(-1, -2, 0)));

		// 3D ---
		assertTrue(Intersector.isPointInTriangle(new Vector3(0.2f, 0, 1.25f), new Vector3(-1, 1, 0), new Vector3(1.4f, 0.99f, 2.5f),
			new Vector3(-1, -2, 0)));
		// 1.2f away.
		assertFalse(Intersector.isPointInTriangle(new Vector3(2.6f, 0, 3.75f), new Vector3(-1, 1, 0),
			new Vector3(1.4f, 0.99f, 2.5f), new Vector3(-1, -2, 0)));
		// In an edge.
		assertTrue(Intersector.isPointInTriangle(new Vector3(0, -0.5f, 0.5f), new Vector3(-1, 1, 0), new Vector3(1, 1, 1),
			new Vector3(-1, -2, 0)));
		// Really close to the edge.
		float epsilon = 0.0000001f; // One more 0 will fail.
		float almost1 = 1 - epsilon;
		assertFalse(Intersector.isPointInTriangle(new Vector3(0, -0.5f, 0.5f), new Vector3(-1, 1, 0), new Vector3(almost1, 1, 1),
			new Vector3(-1, -2, 0)));

		// A really long distance away.
		assertFalse(Intersector.isPointInTriangle(new Vector3(199f, 1f, 500f), new Vector3(-1, 1, 0), new Vector3(1, 1, 5f),
			new Vector3(-1, -2, 0)));

		assertFalse(Intersector.isPointInTriangle(new Vector3(-5120.8345f, 8946.126f, -3270.5813f),
			new Vector3(50.008057f, 22.20586f, 124.62208f), new Vector3(62.282288f, 22.205864f, 109.665924f),
			new Vector3(70.92052f, 7.205861f, 115.437805f)));
	}

	@Test
	public void testIntersectPolygons () {
		// Corner case with extremely small overlap polygon
		Polygon intersectionPolygon = new Polygon();
		assertFalse(
			Intersector.intersectPolygons(new Polygon(new float[] {3200.1453f, 88.00839f, 3233.9087f, 190.34174f, 3266.2905f, 0.0f}),
				new Polygon(new float[] {3213.0f, 131.0f, 3214.0f, 131.0f, 3214.0f, 130.0f, 3213.0f, 130.0f}), intersectionPolygon));
		assertEquals(0, intersectionPolygon.getVertexCount());
	}

	@Test
	public void testIntersectPolygonsWithVertexLyingOnEdge () {
		Polygon p1 = new Polygon(new float[] {1, -1, 2, -1, 2, -2, 1, -2});
		Polygon p2 = new Polygon(new float[] {0.5f, -1.5f, 1.5f, -1.5f, 1.5f, -2.5f});

		Polygon intersectionPolygon = new Polygon();
		boolean checkResult = Intersector.intersectPolygons(p1, p2, intersectionPolygon);

		assertTrue(checkResult);
		assertEquals(4, intersectionPolygon.getVertexCount());
		assertEquals(new Vector2(1.0f, -2.0f), intersectionPolygon.getVertex(0, new Vector2()));
		assertEquals(new Vector2(1.0f, -1.5f), intersectionPolygon.getVertex(1, new Vector2()));
		assertEquals(new Vector2(1.5f, -1.5f), intersectionPolygon.getVertex(2, new Vector2()));
		assertEquals(new Vector2(1.5f, -2.0f), intersectionPolygon.getVertex(3, new Vector2()));
	}

	@Test
	public void testIntersectPolygonsWithTransformationsOnProvidedResultPolygon () {
		Polygon p1 = new Polygon(new float[] {1, -1, 2, -1, 2, -2, 1, -2});
		Polygon p2 = new Polygon(new float[] {0.5f, -1.5f, 1.5f, -1.5f, 1.5f, -2.5f});
		Polygon intersectionPolygon = new Polygon(new float[8]);
		intersectionPolygon.setScale(5, 5);
		intersectionPolygon.setOrigin(10, 20);
		intersectionPolygon.setPosition(-33, -33);
		intersectionPolygon.setRotation(48);

		boolean checkResult = Intersector.intersectPolygons(p1, p2, intersectionPolygon);

		assertTrue(checkResult);
		assertArrayEquals(new float[] {1, -2, 1, -1.5f, 1.5f, -1.5f, 1.5f, -2}, intersectionPolygon.getVertices(), 0);
		assertArrayEquals(new float[] {1, -2, 1, -1.5f, 1.5f, -1.5f, 1.5f, -2}, intersectionPolygon.getTransformedVertices(), 0);
		// verify that the origin has also been reset
		intersectionPolygon.setScale(2, 2);
		assertArrayEquals(new float[] {2 * 1, 2 * -2, 2 * 1, 2 * -1.5f, 2 * 1.5f, 2 * -1.5f, 2 * 1.5f, 2 * -2},
			intersectionPolygon.getTransformedVertices(), 0);
	}
}
