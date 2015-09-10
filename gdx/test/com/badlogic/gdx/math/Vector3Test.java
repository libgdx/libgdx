
package com.badlogic.gdx.math;

import static org.junit.Assert.*;

import org.junit.Test;

public class Vector3Test {
	@Test
	public void testToString () {
		assertEquals("(-5.0,42.00055,44444.32)", new Vector3(-5f, 42.00055f, 44444.32f).toString());
	}

	@Test
	public void testFromString () {
		assertEquals(new Vector3(-5f, 42.00055f, 44444.32f), new Vector3().fromString("(-5,42.00055,44444.32)"));
	}
}
