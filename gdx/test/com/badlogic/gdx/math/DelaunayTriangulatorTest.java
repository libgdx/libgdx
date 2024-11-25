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

import com.badlogic.gdx.utils.ShortArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;

import static java.lang.Math.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public abstract class DelaunayTriangulatorTest {
	private static final float MAX = 10000;
	private static final float MAX_X = MAX;
	private static final float MAX_Y = MAX;
	private static final int NUM_OF_TESTS = 1000;
	private static final Random rnd = new Random();

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

	@Parameter // first data value (0) is default
	public float[] inputPolygon;

	protected abstract int expectedTrianglesCount ();

	@Test
	public void testMany () {
		float[] polygon = inputPolygon;
		ShortArray triangles = new DelaunayTriangulator().computeTriangles(polygon, false);
		assertTrue("full triangles", triangles.size % 3 == 0);
		assertEquals("number of triangles, polygon=" + pointsToString(polygon), expectedTrianglesCount(), triangles.size / 3);
		assertDelaunay(polygon, triangles);
	}

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

		@Override
		protected int expectedTrianglesCount () {
			return 2;
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

		@Override
		protected int expectedTrianglesCount () {
			return 3;
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

		@Override
		protected int expectedTrianglesCount () {
			return 4;
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

		@Override
		protected int expectedTrianglesCount () {
			return 4;
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

		@Override
		protected int expectedTrianglesCount () {
			return 5;
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

		@Override
		protected int expectedTrianglesCount () {
			return 6;
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

		@Override
		protected int expectedTrianglesCount () {
			return 2;
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

		@Override
		protected int expectedTrianglesCount () {
			return 3;
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

		@Override
		protected int expectedTrianglesCount () {
			return 4;
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

		@Override
		protected int expectedTrianglesCount () {
			return 4;
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

		@Override
		protected int expectedTrianglesCount () {
			return 5;
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

		@Override
		protected int expectedTrianglesCount () {
			return 6;
		}
	}

	public static class NearlyColinearTest extends DelaunayTriangulatorTest {
		@Parameters
		public static Object[] nearlyColinear () {
			return new Object[] {new float[] {1, 1, 1, 100, 10f, 200}, new float[] {1, 1, 1, 100, 1.1f, 200},
				new float[] {0, 0, 0, 1000, 1f, 2000}, new float[] {-50, -100, -50, 100, -49, 200}};
		}

		@Override
		protected int expectedTrianglesCount () {
			return 1;
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

		@Override
		protected int expectedTrianglesCount () {
			return 8; // 2*(3-1)*(3-1)
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

		@Override
		protected int expectedTrianglesCount () {
			return 18; // 2*(4-1)*(4-1)
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

		@Override
		protected int expectedTrianglesCount () {
			return 32; // 2*(5-1)*(5-1)
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

		@Override
		protected int expectedTrianglesCount () {
			return 50; // 2*(6-1)*(6-1)
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

		@Override
		protected int expectedTrianglesCount () {
			return 30; // 2*(4-1)*(6-1)
		}
	}
}
