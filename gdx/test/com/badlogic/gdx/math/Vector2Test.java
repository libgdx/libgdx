
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
}
