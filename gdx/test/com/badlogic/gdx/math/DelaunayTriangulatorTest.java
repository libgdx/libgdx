
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
    private static final float MIN_RADIUS = 0.00001f;
    private static final float MAX_RADIUS = 10000;
    private static final float MAX_X = 10000;
    private static final float MAX_Y = 10000;
    private static final int NUM_OF_TESTS = 1000;
    private static final Random rnd = new Random();

    private static float[] nextPolygon(int numPoints) {
        float[] points = new float[2 * numPoints];
        float x = nextFloat(MAX_X);
        float y = nextFloat(MAX_Y);
        double size = MIN_RADIUS + nextPositiveDouble(MAX_RADIUS - MIN_RADIUS);
        double stepAngle = 2 * PI / numPoints;
        double angle = 2 * PI * rnd.nextFloat();
        for (int i = 0; i < numPoints; i++) {
            points[2 * i] = (float) (x + size * cos(angle + i * stepAngle));
            points[2 * i + 1] = (float) (y + size * sin(angle + i * stepAngle));
        }
        Collections.shuffle(Arrays.asList(points));
        return points;
    }

    private static float[] nextPolygonWithCenter(int numTips) {
        float[] points = new float[2 * (numTips + 1)];
        float x = nextFloat(MAX_X);
        float y = nextFloat(MAX_Y);
        double size = MIN_RADIUS + nextPositiveDouble(MAX_RADIUS - MIN_RADIUS);
        double stepAngle = 2 * PI / numTips;
        double angle = 2 * PI * rnd.nextFloat();
        for (int i = 0; i < numTips; i++) {
            points[2 * i] = (float) (x + size * cos(angle + i * stepAngle));
            points[2 * i + 1] = (float) (y + size * sin(angle + i * stepAngle));
        }
        points[2 * numTips] = x;
        points[2 * numTips + 1] = y;
        Collections.shuffle(Arrays.asList(points));
        return points;
    }

    private static int nextSign() {
        return rnd.nextBoolean() ? 1 : -1;
    }

    private static double nextDouble(double max) {
        return nextSign() * nextPositiveDouble(max);
    }

    private static double nextPositiveDouble(double max) {
        return rnd.nextDouble() * max;
    }

    private static float nextFloat(float max) {
        return nextSign() * nextPositiveFloat(max);
    }

    private static float nextPositiveFloat(float max) {
        return rnd.nextFloat() * max;
    }

    @Parameter // first data value (0) is default
    public float[] inputPolygon;

    protected abstract int expectedTrianglesCount();

    @Test
    public void testMany() {
        float[] polygon = inputPolygon;
//        System.out.println(pointsToString(polygon));

        ShortArray triangles = new DelaunayTriangulator().computeTriangles(polygon, false);
//        System.out.println(trianglesToString(polygon, triangles));

        assertTrue("full triangles", triangles.size % 3 == 0);
        assertEquals("number of triangles", expectedTrianglesCount(), triangles.size / 3);
    }

    public String trianglesToString(float[] points, ShortArray triangles) {
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

    public String pointsToString(float[] points) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < points.length; i += 2) {
            s.append(pointToString(points[i], points[i + 1]));
            if (i < points.length - 2) s.append(", ");
        }
        s.append("]");
        return s.toString();
    }

    private static String pointToString(float[] points, int i) {
        return pointToString(points[2 * i], points[2 * i + 1]);
    }

    private static String pointToString(float x, float y) {
        return "(" + x + ", " + y + ")";
    }

    public static class SquaresTest extends DelaunayTriangulatorTest {
        @Parameters
        public static Object[] squares() {
            Object[] hs = new Object[NUM_OF_TESTS];
            for (int i = 0; i < NUM_OF_TESTS; i++)
                hs[i] = nextPolygon(4);
            return hs;
        }

        @Override
        protected int expectedTrianglesCount() {
            return 2;
        }
    }

    public static class PentagonsTest extends DelaunayTriangulatorTest {
        @Parameters
        public static Object[] squares() {
            Object[] hs = new Object[NUM_OF_TESTS];
            for (int i = 0; i < NUM_OF_TESTS; i++)
                hs[i] = nextPolygon(5);
            return hs;
        }

        @Override
        protected int expectedTrianglesCount() {
            return 3;
        }
    }

    public static class HexagonsTest extends DelaunayTriangulatorTest {
        @Parameters
        public static Object[] hexagons() {
            Object[] hs = new Object[NUM_OF_TESTS];
            for (int i = 0; i < NUM_OF_TESTS; i++)
                hs[i] = nextPolygon(6);
            return hs;
        }

        @Override
        protected int expectedTrianglesCount() {
            return 4;
        }
    }

    public static class SquaresWithCenterTest extends DelaunayTriangulatorTest {
        @Parameters
        public static Object[] squares() {
            Object[] hs = new Object[NUM_OF_TESTS];
            for (int i = 0; i < NUM_OF_TESTS; i++)
                hs[i] = nextPolygonWithCenter(4);
            return hs;
        }

        @Override
        protected int expectedTrianglesCount() {
            return 4;
        }
    }

    public static class PentagonsWithCenterTest extends DelaunayTriangulatorTest {
        @Parameters
        public static Object[] squares() {
            Object[] hs = new Object[NUM_OF_TESTS];
            for (int i = 0; i < NUM_OF_TESTS; i++)
                hs[i] = nextPolygonWithCenter(5);
            return hs;
        }

        @Override
        protected int expectedTrianglesCount() {
            return 5;
        }
    }

    public static class HexagonsWithCenterTest extends DelaunayTriangulatorTest {
        @Parameters
        public static Object[] hexagons() {
            Object[] hs = new Object[NUM_OF_TESTS];
            for (int i = 0; i < NUM_OF_TESTS; i++)
                hs[i] = nextPolygonWithCenter(6);
            return hs;
        }

        @Override
        protected int expectedTrianglesCount() {
            return 6;
        }
    }
}
