
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

	 /** Test if check before use len() in nor method work */
	 @Test
	 public void testNorCheck () {
		  Vector3 v = new Vector3(34.616817f,97.90621f,19.677162f).nor();
		  assertFalse(v.len2() == 1f);
		  assertTrue(MathUtils.isEqual(v.len2(), 1f));
	 }

	 @Test
	 public void testNorNoArthimeticException () {
		  Vector3 v = new Vector3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
		  Vector3 vNor = v.cpy().nor();
		  assertEquals(v, vNor);
	 }
}
