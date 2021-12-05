
package com.badlogic.gdx.math;

import static org.junit.Assert.*;

import org.junit.Test;

public class Vector2Test {
	@Test
	public void testToString () {
		assertEquals("(-5.0,42.00055)", new Vector2(-5f, 42.00055f).toString());
	}

	@Test
	public void testFromString () {
		assertEquals(new Vector2(-5f, 42.00055f), new Vector2().fromString("(-5,42.00055)"));
	}

	@Test
	public void testAngle () {
		assertEquals(270f, new Vector2(0, -1f).angleDeg(), MathUtils.FLOAT_ROUNDING_ERROR);
	}

	@Test
	public void testAngleRelative () {
		assertEquals(270f, new Vector2(0, -1f).angleDeg(Vector2.X), MathUtils.FLOAT_ROUNDING_ERROR);
	}

	@Test
	public void testAngleRad () {
		assertEquals(-MathUtils.HALF_PI, new Vector2(0, -1f).angleRad(), MathUtils.FLOAT_ROUNDING_ERROR);
	}

	@Test
	public void testAngleRadRelative () {
		assertEquals(-MathUtils.HALF_PI, new Vector2(0, -1f).angleRad(Vector2.X), MathUtils.FLOAT_ROUNDING_ERROR);
	}
}
