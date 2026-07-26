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

package com.badlogic.gdx.math;

import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.ShortArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.*;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public abstract class DelaunayTriangulatorTest {
	private static final float MAX = 10000;
	private static final float MAX_X = MAX;
	private static final float MAX_Y = MAX;
	private static final int NUM_OF_TESTS = 1000;
	private static final RandomXS128 rnd = new RandomXS128(123456789);

	private static double nextBigRadius () {
		final float min = MAX / 100;
		final float max = MAX;
		return min + nextPositiveDouble(max - min);
	}

	private static double nextSmallRadius () {
		final float min = (float)(30 * ((double)Math.nextUp(MAX) - (double)MAX));
		final float max = 10 * min;
		return min + nextPositiveDouble(max - min);
	}

	private static float[] nextPolygon (int numPoints, double radius) {
		float[] points = new float[2 * numPoints];
		float x = nextFloat(MAX_X);
		float y = nextFloat(MAX_Y);
		double stepAngle = 2 * PI / numPoints;
		double angle = 2 * PI * rnd.nextFloat();
		for (int i = 0; i < numPoints; i++) {
			points[2 * i] = (float)(x + radius * cos(angle + i * stepAngle));
			points[2 * i + 1] = (float)(y + radius * sin(angle + i * stepAngle));
		}
		return points;
	}

	private static float[] nextPolygonWithCenter (int numTips, double radius) {
		float[] points = new float[2 * (numTips + 1)];
		float x = nextFloat(MAX_X);
		float y = nextFloat(MAX_Y);
		double stepAngle = 2 * PI / numTips;
		double angle = 2 * PI * rnd.nextFloat();
		for (int i = 0; i < numTips; i++) {
			points[2 * i] = (float)(x + radius * cos(angle + i * stepAngle));
			points[2 * i + 1] = (float)(y + radius * sin(angle + i * stepAngle));
		}
		points[2 * numTips] = x;
		points[2 * numTips + 1] = y;
		return points;
	}

	private static float[] nextGrid (int w, int h) {
		int scale = 1 + rnd.nextInt(8);
		int ox = (int)nextFloat(MAX_X);
		int oy = (int)nextFloat(MAX_Y);
		float[] points = new float[2 * w * h];
		int k = 0;
		for (int gy = 0; gy < h; gy++) {
			for (int gx = 0; gx < w; gx++) {
				points[k++] = (float)((ox + gx) * scale);
				points[k++] = (float)((oy + gy) * scale);
			}
		}
		return points;
	}

	private static int nextSign () {
		return rnd.nextBoolean() ? 1 : -1;
	}

	private static double nextPositiveDouble (double max) {
		return rnd.nextDouble() * max;
	}

	private static float nextFloat (float max) {
		return nextSign() * nextPositiveFloat(max);
	}

	private static float nextPositiveFloat (float max) {
		return rnd.nextFloat() * max;
	}

	private static float nextFloatBits () {
		return NumberUtils.intBitsToFloat(rnd.nextInt() & 0x3FFFFFFF);
	}

	private static float nextSignedFloatBits () {
		return NumberUtils.intBitsToFloat(rnd.nextInt() & 0xBFFFFFFF);
	}

	private static float[] nextTinyPolygon (int numPoints) {
		float[] points = new float[2 * numPoints];
		float x = nextSignedFloatBits();
		float y = nextSignedFloatBits();
		float radius = nextFloatBits();
		double stepAngle = 2 * PI / numPoints;
		double angle = 2 * PI * rnd.nextFloat();
		for (int i = 0; i < numPoints; i++) {
			points[2 * i] = (float)(x + radius * cos(angle + i * stepAngle));
			points[2 * i + 1] = (float)(y + radius * sin(angle + i * stepAngle));
		}
		return points;
	}

	private static float[] nextTinyPolygonWithCenter (int numTips) {
		float[] points = new float[2 * (numTips + 1)];
		float x = nextSignedFloatBits();
		float y = nextSignedFloatBits();
		float radius = nextFloatBits();
		double stepAngle = 2 * PI / numTips;
		double angle = 2 * PI * rnd.nextFloat();
		for (int i = 0; i < numTips; i++) {
			points[2 * i] = (float)(x + radius * cos(angle + i * stepAngle));
			points[2 * i + 1] = (float)(y + radius * sin(angle + i * stepAngle));
		}
		points[2 * numTips] = x;
		points[2 * numTips + 1] = y;
		return points;
	}

	private static boolean hasDuplicates (float[] points) {
		for (int i = 0; i < points.length; i += 2) {
			for (int j = i + 2; j < points.length; j += 2) {
				if (points[i] == points[j] && points[i + 1] == points[j + 1]) return true;
			}
		}
		return false;
	}

	private static boolean allCollinear (float[] points) {
		ShewchukExactPredicates pred = new ShewchukExactPredicates();
		double ax = points[0], ay = points[1];
		double bx = points[2], by = points[3];
		for (int i = 4; i < points.length; i += 2) {
			if (pred.orient2d(ax, ay, bx, by, points[i], points[i + 1]) != 0) return false;
		}
		return true;
	}

	private static boolean isValidTinyPolygon (float[] points) {
		return !hasDuplicates(points) && !allCollinear(points);
	}

	private static float[] nextDistinctTinyPolygon (int numPoints) {
		for (int t = 0; t < 1000; t++) {
			float[] p = nextTinyPolygon(numPoints);
			if (isValidTinyPolygon(p)) return p;
		}
		throw new IllegalStateException("could not generate non-degenerate tiny polygon after 1000 tries");
	}

	private static float[] nextDistinctTinyPolygonWithCenter (int numTips) {
		for (int t = 0; t < 1000; t++) {
			float[] p = nextTinyPolygonWithCenter(numTips);
			if (isValidTinyPolygon(p)) return p;
		}
		throw new IllegalStateException("could not generate non-degenerate tiny polygon with center after 1000 tries");
	}

	@Parameter // first data value (0) is default
	public float[] inputPolygon;

	@Test
	public void testMany () {
		float[] polygon = inputPolygon;
		assertTrue("input is invalid: " + pointsToString(polygon), isValidTinyPolygon(polygon));
		ShortArray triangles = new DelaunayTriangulator().computeTriangles(polygon, false);
		assertTrue("full triangles", triangles.size % 3 == 0);
		assertDelaunay(polygon, triangles);
		assertClockwise(polygon, triangles);
		assertTilesConvexHull(polygon, triangles);
	}

	public static void assertClockwise (float[] points, ShortArray triangles) {
		ShewchukExactPredicates pred = new ShewchukExactPredicates();
		for (int t = 0; t < triangles.size; t += 3) {
			int a = triangles.get(t);
			int b = triangles.get(t + 1);
			int c = triangles.get(t + 2);

			double ax = points[2 * a], ay = points[2 * a + 1];
			double bx = points[2 * b], by = points[2 * b + 1];
			double cx = points[2 * c], cy = points[2 * c + 1];
			double o = pred.orient2d(ax, ay, bx, by, cx, cy);
			assertTrue("triangle " + a + "," + b + "," + c + " is not clockwise", o < 0);
		}
	}

	/** Checks that every input vertex appears in some triangle, no triangle is degenerate, and the empty-circumcircle Delaunay
	 * property holds for every triangle. See {@link #assertTilesConvexHull} for triangulation correctness. */
	public static void assertDelaunay (float[] points, ShortArray triangles) {
		int n = points.length / 2;
		boolean[] used = new boolean[n];
		for (int i = 0; i < triangles.size; i++)
			used[triangles.get(i)] = true;
		for (int i = 0; i < n; i++)
			assertTrue("vertex " + i + " missing from triangulation (point dropped)", used[i]);
		ShewchukExactPredicates pred = new ShewchukExactPredicates();
		for (int t = 0; t < triangles.size; t += 3) {
			int a = triangles.get(t), b = triangles.get(t + 1), c = triangles.get(t + 2);
			double ax = points[2 * a], ay = points[2 * a + 1];
			double bx = points[2 * b], by = points[2 * b + 1];
			double cx = points[2 * c], cy = points[2 * c + 1];
			double o = pred.orient2d(ax, ay, bx, by, cx, cy);
			assertTrue("degenerate (collinear) triangle " + a + "," + b + "," + c, o != 0);
			for (int q = 0; q < n; q++) {
				if (q == a || q == b || q == c) continue;
				double inc = pred.incircle(ax, ay, bx, by, cx, cy, points[2 * q], points[2 * q + 1]);
				assertTrue("non-Delaunay: point " + q + " strictly inside circumcircle of triangle " + a + "," + b + "," + c,
					inc == 0 || (inc < 0) != (o < 0));
			}
		}
	}

	/** Checks that the triangles form a triangulation of the convex hull of the input points: every interior edge is shared by
	 * exactly two triangles, every boundary edge appears in exactly one triangle, and every boundary edge lies on the convex
	 * hull. */
	public static void assertTilesConvexHull (float[] points, ShortArray triangles) {
		int n = points.length / 2;
		ShewchukExactPredicates pred = new ShewchukExactPredicates();
		HashMap<Long, Integer> edgeCount = new HashMap<>();
		for (int t = 0; t < triangles.size; t += 3) {
			int a = triangles.get(t);
			int b = triangles.get(t + 1);
			int c = triangles.get(t + 2);
			bumpEdge(edgeCount, a, b);
			bumpEdge(edgeCount, b, c);
			bumpEdge(edgeCount, c, a);
		}

		for (Map.Entry<Long, Integer> entry : edgeCount.entrySet()) {
			int count = entry.getValue();
			long key = entry.getKey();
			int a = (int)(key >>> 32);
			int b = (int)(key & 0xFFFFFFFFL);
			assertTrue("edge " + a + "-" + b + " appears " + count + " times (overlapping triangles)", count <= 2);
			if (count == 1) {
				double ax = points[2 * a], ay = points[2 * a + 1];
				double bx = points[2 * b], by = points[2 * b + 1];
				int side = 0;
				for (int q = 0; q < n; q++) {
					if (q == a || q == b) continue;
					double o = pred.orient2d(ax, ay, bx, by, points[2 * q], points[2 * q + 1]);
					if (o == 0) continue;
					int s = o > 0 ? 1 : -1;
					if (side == 0)
						side = s;
					else
						assertTrue("boundary edge " + a + "-" + b + " has points on both sides (missing triangle or hole)", s == side);
				}
			}
		}
	}

	private static void bumpEdge (HashMap<Long, Integer> map, int a, int b) {
		long key = a < b ? ((long)a << 32) | (b & 0xFFFFFFFFL) : ((long)b << 32) | (a & 0xFFFFFFFFL);
		map.compute(key, (k, cur) -> cur == null ? 1 : cur + 1);
	}

	public String trianglesToString (float[] points, ShortArray triangles) {
		StringBuilder s = new StringBuilder("[");
		for (int t = 0; t < triangles.size; t += 3) {
			int i1 = triangles.get(t);
			int i2 = triangles.get(t + 1);
			int i3 = triangles.get(t + 2);
			s.append("polygon(");
			s.append(pointToString(points, i1)).append(", ");
			s.append(pointToString(points, i2)).append(", ");
			s.append(pointToString(points, i3));
			s.append(")");
			if (t < triangles.size - 3) s.append(", ");
		}
		s.append("]");
		return s.toString();
	}

	public String pointsToString (float[] points) {
		StringBuilder s = new StringBuilder("[");
		for (int i = 0; i < points.length; i += 2) {
			s.append(pointToString(points[i], points[i + 1]));
			if (i < points.length - 2) s.append(", ");
		}
		s.append("]");
		return s.toString();
	}

	private static String pointToString (float[] points, int i) {
		return pointToString(points[2 * i], points[2 * i + 1]);
	}

	private static String pointToString (float x, float y) {
		return "(" + x + ", " + y + ")";
	}

	public static class SmallSquaresTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] squares () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextPolygon(4, nextSmallRadius());
			return hs;
		}
	}

	public static class SmallPentagonsTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] pentagons () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextPolygon(5, nextSmallRadius());
			return hs;
		}
	}

	public static class SmallHexagonsTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] hexagons () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextPolygon(6, nextSmallRadius());
			return hs;
		}
	}

	public static class SmallSquaresWithCenterTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] squares () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextPolygonWithCenter(4, nextSmallRadius());
			return hs;
		}
	}

	public static class SmallPentagonsWithCenterTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] pentagons () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextPolygonWithCenter(5, nextSmallRadius());
			return hs;
		}
	}

	public static class SmallHexagonsWithCenterTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] hexagons () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextPolygonWithCenter(6, nextSmallRadius());
			return hs;
		}
	}

	public static class BigSquaresTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] squares () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextPolygon(4, nextBigRadius());
			return hs;
		}
	}

	public static class BigPentagonsTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] pentagons () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextPolygon(5, nextBigRadius());
			return hs;
		}
	}

	public static class BigHexagonsTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] hexagons () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextPolygon(6, nextBigRadius());
			return hs;
		}
	}

	public static class BigSquaresWithCenterTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] squares () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextPolygonWithCenter(4, nextBigRadius());
			return hs;
		}
	}

	public static class BigPentagonsWithCenterTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] pentagons () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextPolygonWithCenter(5, nextBigRadius());
			return hs;
		}
	}

	public static class BigHexagonsWithCenterTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] hexagons () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextPolygonWithCenter(6, nextBigRadius());
			return hs;
		}
	}

	public static class NearlyColinearTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] nearlyColinear () {
			return new Object[] {new float[] {1, 1, 1, 100, 10f, 200}, new float[] {1, 1, 1, 100, 1.1f, 200},
				new float[] {0, 0, 0, 1000, 1f, 2000}, new float[] {-50, -100, -50, 100, -49, 200}};
		}
	}

	public static class Grid3x3Test extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] grids () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextGrid(3, 3);
			return hs;
		}
	}

	public static class Grid4x4Test extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] grids () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextGrid(4, 4);
			return hs;
		}
	}

	public static class Grid5x5Test extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] grids () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextGrid(5, 5);
			return hs;
		}
	}

	public static class Grid6x6Test extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] grids () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextGrid(6, 6);
			return hs;
		}
	}

	public static class Grid4x6Test extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] grids () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextGrid(4, 6);
			return hs;
		}
	}

	public static class TinySquaresTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] squares () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextDistinctTinyPolygon(4);
			return hs;
		}
	}

	public static class TinyPentagonsTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] pentagons () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextDistinctTinyPolygon(5);
			return hs;
		}
	}

	public static class TinyHexagonsTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] hexagons () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextDistinctTinyPolygon(6);
			return hs;
		}
	}

	public static class TinySquaresWithCenterTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] squares () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextDistinctTinyPolygonWithCenter(4);
			return hs;
		}
	}

	public static class TinyPentagonsWithCenterTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] pentagons () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextDistinctTinyPolygonWithCenter(5);
			return hs;
		}
	}

	public static class TinyHexagonsWithCenterTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] hexagons () {
			Object[] hs = new Object[NUM_OF_TESTS];
			for (int i = 0; i < NUM_OF_TESTS; i++)
				hs[i] = nextDistinctTinyPolygonWithCenter(6);
			return hs;
		}
	}
}
