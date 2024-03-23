
package com.badlogic.gdx.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Vector4Test {
	@Test
	public void testToString () {
		assertEquals("(-5.0,42.00055,44444.32,-1.975)", new Vector4(-5f, 42.00055f, 44444.32f, -1.975f).toString());
	}

	@Test
	public void testFromString () {
		assertEquals(new Vector4(-5f, 42.00055f, 44444.32f, -1.975f), new Vector4().fromString("(-5,42.00055,44444.32,-1.9750)"));
	}
}
