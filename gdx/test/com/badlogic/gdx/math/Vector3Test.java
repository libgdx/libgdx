
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

	@Test
	public void testSlerpOppositeVectors () {
		Vector3 start = new Vector3(0, 0.6f, 0.8f);
		Vector3 end = new Vector3(start).scl(-1);
		for (float alpha = 0.25f; alpha <= 1f; alpha += 0.25f) {
			Vector3 result = new Vector3(start).slerp(end, alpha);
			assertEquals(1f, result.len(), 1e-5f);
			// The angle to the start vector grows linearly with alpha, from 0 to 180 degrees.
			assertEquals(180f * alpha, (float)Math.toDegrees(Math.acos(MathUtils.clamp(result.dot(start), -1f, 1f))), 0.01f);
		}
		assertTrue(new Vector3(start).slerp(end, 1f).epsilonEquals(end, 1e-5f));
	}

	@Test
	public void testSlerpNearlyOppositeVectors () {
		Vector3 start = new Vector3(1, 0, 0);
		Vector3 end = new Vector3(-1, 0.02f, 0).nor(); // about 178.85 degrees apart
		float angle = (float)Math.toDegrees(Math.acos(start.dot(end)));

		Vector3 half = new Vector3(start).slerp(end, 0.5f);
		assertEquals(1f, half.len(), 1e-5f);
		assertEquals(angle / 2, (float)Math.toDegrees(Math.acos(half.dot(start))), 0.01f);
		assertEquals(angle / 2, (float)Math.toDegrees(Math.acos(half.dot(end))), 0.01f);
		assertTrue(new Vector3(start).slerp(end, 1f).epsilonEquals(end, 1e-5f));
	}
}
