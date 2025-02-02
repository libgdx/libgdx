
package com.badlogic.gdx.math;

import static com.badlogic.gdx.math.MathUtils.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class MathUtilsTest {

	@Test
	public void lerpAngle () {
		assertEquals(PI / 18f, MathUtils.lerpAngle(PI / 18f, PI / 6f, 0.0f), 0.01f);
		assertEquals(PI / 9f, MathUtils.lerpAngle(PI / 18f, PI / 6f, 0.5f), 0.01f);
		assertEquals(PI / 6f, MathUtils.lerpAngle(PI / 18f, PI / 6f, 1.0f), 0.01f);

		// checks both negative c, which should produce a result close to HALF_PI, and
		// positive c, which should be close to PI + HALF_PI.
		// intentionally skips where c == 0, because there are two equally-valid results for that case.
		for (float c = -1f; c <= 1f; c += 0.003f) {
			assertEquals(PI + Math.copySign(HALF_PI, c) + c, MathUtils.lerpAngle(0, PI2 + PI + c + c, 0.5f), 0.01f);
			assertEquals(PI + Math.copySign(HALF_PI, c) + c, MathUtils.lerpAngle(PI2 + PI + c + c, 0, 0.5f), 0.01f);
		}
	}

	@Test
	public void lerpAngleDeg () {
		assertEquals(10, MathUtils.lerpAngleDeg(10, 30, 0.0f), 0.01f);
		assertEquals(20, MathUtils.lerpAngleDeg(10, 30, 0.5f), 0.01f);
		assertEquals(30, MathUtils.lerpAngleDeg(10, 30, 1.0f), 0.01f);

		// checks both negative c, which should produce a result close to 90, and
		// positive c, which should be close to 270.
		// intentionally skips where c == 0, because there are two equally-valid results for that case.
		for (float c = -80f; c <= 80f; c += 0.3f) {
			assertEquals(180f + Math.copySign(90f, c) + c, MathUtils.lerpAngleDeg(0, 540 + c + c, 0.5f), 0.01f);
			assertEquals(180f + Math.copySign(90f, c) + c, MathUtils.lerpAngleDeg(540 + c + c, 0, 0.5f), 0.01f);
		}
	}

	@Test
	public void lerpAngleDegCrossingZero () {
		assertEquals(350, MathUtils.lerpAngleDeg(350, 10, 0.0f), 0.01f);
		assertEquals(0, MathUtils.lerpAngleDeg(350, 10, 0.5f), 0.01f);
		assertEquals(10, MathUtils.lerpAngleDeg(350, 10, 1.0f), 0.01f);
	}

	@Test
	public void lerpAngleDegCrossingZeroBackwards () {
		assertEquals(10, MathUtils.lerpAngleDeg(10, 350, 0.0f), 0.01f);
		assertEquals(0, MathUtils.lerpAngleDeg(10, 350, 0.5f), 0.01f);
		assertEquals(350, MathUtils.lerpAngleDeg(10, 350, 1.0f), 0.01f);
	}

	@Test
	public void testNorm () {
		assertEquals(-1.0f, MathUtils.norm(10f, 20f, 0f), 0.01f);
		assertEquals(0.0f, MathUtils.norm(10f, 20f, 10f), 0.01f);
		assertEquals(0.5f, MathUtils.norm(10f, 20f, 15f), 0.01f);
		assertEquals(1.0f, MathUtils.norm(10f, 20f, 20f), 0.01f);
		assertEquals(2.0f, MathUtils.norm(10f, 20f, 30f), 0.01f);
	}

	@Test
	public void testMap () {
		assertEquals(0f, MathUtils.map(10f, 20f, 100f, 200f, 0f), 0.01f);
		assertEquals(100f, MathUtils.map(10f, 20f, 100f, 200f, 10f), 0.01f);
		assertEquals(150f, MathUtils.map(10f, 20f, 100f, 200f, 15f), 0.01f);
		assertEquals(200f, MathUtils.map(10f, 20f, 100f, 200f, 20f), 0.01f);
		assertEquals(300f, MathUtils.map(10f, 20f, 100f, 200f, 30f), 0.01f);
	}

	@Test
	public void testRandomLong () {
		long r;
		for (int i = 0; i < 512; i++) {
			assertTrue((r = MathUtils.random(1L, 5L)) >= 1L && r <= 5L);
			assertTrue((r = MathUtils.random(6L, 1L)) >= 1L && r <= 6L);
			assertTrue((r = MathUtils.random(-1L, -7L)) <= -1L && r >= -7L);
			assertTrue((r = MathUtils.random(-8L, -1L)) <= -1L && r >= -8L);
		}
	}

	@Test
	public void testSinDeg () {
		assertEquals(0f, MathUtils.sinDeg(0f), 0f);
		assertEquals(1f, MathUtils.sinDeg(90f), 0f);
		assertEquals(0f, MathUtils.sinDeg(180f), 0f);
		assertEquals(-1f, MathUtils.sinDeg(270f), 0f);
	}

	@Test
	public void testCosDeg () {
		assertEquals(1f, MathUtils.cosDeg(0f), 0f);
		assertEquals(0f, MathUtils.cosDeg(90f), 0f);
		assertEquals(-1f, MathUtils.cosDeg(180f), 0f);
		assertEquals(0f, MathUtils.cosDeg(270f), 0f);
	}

	@Test
	public void testTanDeg () {
		assertEquals(0f, MathUtils.tanDeg(0f), FLOAT_ROUNDING_ERROR);
		assertEquals(Math.tan(Math.toRadians(45f)), MathUtils.tanDeg(45f), FLOAT_ROUNDING_ERROR);
// assertEquals(Float.POSITIVE_INFINITY, MathUtils.tanDeg(90f), 0f); // near infinite, maximum error here
		assertEquals(Math.tan(Math.toRadians(135f)), MathUtils.tanDeg(135f), FLOAT_ROUNDING_ERROR);
		assertEquals(0f, MathUtils.tanDeg(180f), FLOAT_ROUNDING_ERROR);
	}

	@Test
	public void testAtan2Deg360 () {
		assertEquals(0f, MathUtils.atan2Deg360(0f, 1f), FLOAT_ROUNDING_ERROR);
		assertEquals(45f, MathUtils.atan2Deg360(1f, 1f), FLOAT_ROUNDING_ERROR);
		assertEquals(90f, MathUtils.atan2Deg360(1f, 0f), FLOAT_ROUNDING_ERROR);
		assertEquals(135f, MathUtils.atan2Deg360(1f, -1f), FLOAT_ROUNDING_ERROR);
		assertEquals(180f, MathUtils.atan2Deg360(0f, -1f), FLOAT_ROUNDING_ERROR);
		assertEquals(225f, MathUtils.atan2Deg360(-1f, -1f), FLOAT_ROUNDING_ERROR);
		assertEquals(270f, MathUtils.atan2Deg360(-1f, 0f), FLOAT_ROUNDING_ERROR);
		assertEquals(315f, MathUtils.atan2Deg360(-1f, 1f), FLOAT_ROUNDING_ERROR);
	}
}
