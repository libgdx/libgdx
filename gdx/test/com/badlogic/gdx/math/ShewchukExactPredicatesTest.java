
package com.badlogic.gdx.math;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Java port of mourner/robust-predicates test/test.js.
 *
 * <p>
 * The npm {@code nextafter} dependency is replaced by {@link Math#nextAfter}, and the {@code robust-orientation} reference used
 * in the orient2d near-collinear sweep is replaced by an exact {@link BigDecimal} determinant. The 4×1000 hard fixtures carry
 * their own expected signs. */
public class ShewchukExactPredicatesTest {

	private final Random rnd = new Random(1234);
	private final ShewchukExactPredicates p = new ShewchukExactPredicates();

	private static void assertNegative (double v, String msg) {
		assertTrue(msg + " — expected < 0 but was " + v, v < 0);
	}

	private static void assertPositive (double v, String msg) {
		assertTrue(msg + " — expected > 0 but was " + v, v > 0);
	}

	private static void assertZero (double v, String msg) {
		assertEquals(msg, 0.0, v, 0.0);
	}

	/** Exact sign of the 2D orientation determinant (ax-cx)(by-cy) - (ay-cy)(bx-cx). */
	private static int exactOrient2dSign (double ax, double ay, double bx, double by, double cx, double cy) {
		BigDecimal bax = new BigDecimal(ax), bay = new BigDecimal(ay);
		BigDecimal bbx = new BigDecimal(bx), bby = new BigDecimal(by);
		BigDecimal bcx = new BigDecimal(cx), bcy = new BigDecimal(cy);
		BigDecimal left = bax.subtract(bcx).multiply(bby.subtract(bcy));
		BigDecimal right = bay.subtract(bcy).multiply(bbx.subtract(bcx));
		return left.subtract(right).signum();
	}

	/** tok[0] is the line index; the next {@code numbersPerLine} values are coords then sign. */
	private static List<double[]> readFixture (String name, int numbersPerLine) throws Exception {
		List<double[]> rows = new ArrayList<>();
		InputStream is = ShewchukExactPredicatesTest.class.getResourceAsStream("/fixtures/" + name);
		assert is != null;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) continue;
				String[] tok = line.split("\\s+");
				double[] vals = new double[numbersPerLine];
				for (int i = 0; i < numbersPerLine; i++) {
					vals[i] = Double.parseDouble(tok[i + 1]);
				}
				rows.add(vals);
			}
		}
		return rows;
	}

	@Test
	public void testOrient2d () throws Exception {
		assertPositive(p.orient2d(0, 0, 1, 1, 0, 1), "counterclockwise");
		assertNegative(p.orient2d(0, 0, 0, 1, 1, 1), "clockwise");
		assertZero(p.orient2d(0, 0, 0.5, 0.5, 1, 1), "collinear");

		double r = 0.95;
		double q = 18;
		double pp = 16.8;
		double w = Math.pow(2, -43);

		for (int i = 0; i < 128; i++) {
			for (int j = 0; j < 128; j++) {
				double x = r + w * i / 128;
				double y = r + w * j / 128;

				double o = p.orient2d(x, y, q, q, pp, pp);
				int expected = exactOrient2dSign(x, y, q, q, pp, pp);

				assertEquals(x + "," + y + ", " + q + "," + q + ", " + pp + "," + pp + ": " + o, expected, (int)Math.signum(o));
			}
		}
		// 128x128 near-collinear

		List<double[]> lines = readFixture("orient2d.txt", 7);
		for (double[] v : lines) {
			double result = p.orient2d(v[0], v[1], v[2], v[3], v[4], v[5]);
			int sign = (int)v[6];
			assertEquals("orient2d fixture: " + result, sign, (int)Math.signum(result));
		}
		// 1000 hard fixtures
	}

	@Test
	public void testOrient2dfast () {
		assertPositive(p.orient2dfast(0, 0, 1, 1, 0, 1), "counterclockwise");
		assertNegative(p.orient2dfast(0, 0, 0, 1, 1, 1), "clockwise");
		assertZero(p.orient2dfast(0, 0, 0.5, 0.5, 1, 1), "collinear");
	}

	@Test
	public void testIncircle () throws Exception {
		assertNegative(p.incircle(0, -1, 0, 1, 1, 0, -0.5, 0), "inside");
		assertZero(p.incircle(0, -1, 1, 0, 0, 1, -1, 0), "on circle");
		assertPositive(p.incircle(0, -1, 0, 1, 1, 0, -1.5, 0), "outside");

		double a = Math.nextAfter(-1.0, 0.0);
		double b = Math.nextAfter(-1.0, -2.0);

		assertNegative(p.incircle(1, 0, -1, 0, 0, 1, 0, a), "near inside");
		assertPositive(p.incircle(1, 0, -1, 0, 0, 1, 0, b), "near outside");

		double x = 1e-64;
		for (int i = 0; i < 128; i++) {
			assertPositive(p.incircle(0, x, -x, -x, x, -x, 0, 0), "incircle test " + x + ", outside");
			assertNegative(p.incircle(0, x, -x, -x, x, -x, 0, 2 * x), "incircle test " + x + ", inside");
			assertZero(p.incircle(0, x, -x, -x, x, -x, 0, x), "incircle test " + x + ", cocircular");
			x *= 10;
		}
		// 384 incircle tests

		List<double[]> lines = readFixture("incircle.txt", 9);
		for (double[] v : lines) {
			double result = p.incircle(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7]);
			int sign = (int)v[8];
			assertEquals("incircle fixture: " + result, sign, (int)Math.signum(result));
		}
		// 1000 hard fixtures
	}

	@Test
	public void testIncirclefast () {
		assertNegative(p.incirclefast(0, -1, 0, 1, 1, 0, -0.5, 0), "inside");
		assertZero(p.incirclefast(0, -1, 0, 1, 1, 0, -1, 0), "on circle");
		assertPositive(p.incirclefast(0, -1, 0, 1, 1, 0, -1.5, 0), "outside");
	}

	@Test
	public void testOrient3d () throws Exception {
		assertPositive(p.orient3d(0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1), "above");

		assertNegative(p.orient3d(0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, -1), "below");

		assertZero(p.orient3d(0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0), "coplanar");

		double a = Math.nextAfter(0.0, 1.0);
		double b = Math.nextAfter(0.0, -1.0);

		assertPositive(p.orient3d(0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, a), "near above");

		assertNegative(p.orient3d(0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, b), "near below");

		List<double[]> lines = readFixture("orient3d.txt", 13);
		for (double[] v : lines) {
			double ax = v[0], ay = v[1], az = v[2];
			double bx = v[3], by = v[4], bz = v[5];
			double cx = v[6], cy = v[7], cz = v[8];
			double dx = v[9], dy = v[10], dz = v[11];
			int sign = (int)v[12];
			double result = p.orient3d(ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz);
			assertEquals("orient3d fixture: " + result, -sign, (int)Math.signum(result));
			assertEquals("symmetry", (int)Math.signum(result),
				(int)Math.signum(p.orient3d(dx, dy, dz, bx, by, bz, ax, ay, az, cx, cy, cz)));
		}
		// 1000 hard fixtures

		double tol = 5.0e-14;
		for (int i = 0; i < 1000; i++) {
			double ax = 0.5 + tol * rnd.nextDouble();
			double ay = 0.5 + tol * rnd.nextDouble();
			double az = 0.5 + tol * rnd.nextDouble();
			double bb = 12, cc = 24, dd = 48;
			assertZero(p.orient3d(bb, bb, bb, cc, cc, cc, dd, dd, dd, ax, ay, az), "degenerate");
			assertZero(p.orient3d(cc, cc, cc, dd, dd, dd, ax, ay, az, bb, bb, bb), "degenerate");
		}
		// 1000 degenerate cases
	}

	@Test
	public void testOrient3dfast () {
		assertPositive(p.orient3dfast(0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1), "above");

		assertNegative(p.orient3dfast(0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, -1), "below");

		assertZero(p.orient3dfast(0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0), "coplanar");
	}

	@Test
	public void testInsphere () throws Exception {
		assertNegative(p.insphere(1, 0, 0, 0, -1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0), "inside");

		assertPositive(p.insphere(1, 0, 0, 0, -1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 2), "outside");

		assertZero(p.insphere(1, 0, 0, 0, -1, 0, 0, 1, 0, 0, 0, 1, 0, 0, -1), "cospherical");

		double a = Math.nextAfter(-1.0, 0.0);
		double b = Math.nextAfter(-1.0, -2.0);

		assertNegative(p.insphere(1, 0, 0, 0, -1, 0, 0, 1, 0, 0, 0, 1, 0, 0, a), "near inside");

		assertPositive(p.insphere(1, 0, 0, 0, -1, 0, 0, 1, 0, 0, 0, 1, 0, 0, b), "near outside");

		List<double[]> lines = readFixture("insphere.txt", 16);
		for (double[] v : lines) {
			double result = p.insphere(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9], v[10], v[11], v[12], v[13],
				v[14]);
			int sign = (int)v[15];
			assertEquals("insphere fixture: " + result, -sign, (int)Math.signum(result));
		}
		// 1000 hard fixtures
	}

	@Test
	public void testInspherefast () {
		assertNegative(p.inspherefast(1, 0, 0, 0, -1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0), "inside");

		assertPositive(p.inspherefast(1, 0, 0, 0, -1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 2), "outside");

		assertZero(p.inspherefast(1, 0, 0, 0, -1, 0, 0, 1, 0, 0, 0, 1, 0, 0, -1), "cospherical");
	}
}
