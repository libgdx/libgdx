
package com.badlogic.gdx.math;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Shape2DTest {

	@Test
	public void testCircle () {
		Circle c1 = new Circle(0, 0, 1);
		Circle c2 = new Circle(0, 0, 1);
		Circle c3 = new Circle(2, 0, 1);
		Circle c4 = new Circle(0, 0, 2);

		assertTrue(c1.overlaps(c1));
		assertTrue(c1.overlaps(c2));
		assertFalse(c1.overlaps(c3));
		assertTrue(c1.overlaps(c4));
		assertTrue(c4.overlaps(c1));
		assertTrue(c1.contains(0, 1));
		assertFalse(c1.contains(0, 2));
		assertTrue(c1.contains(c1));
		assertFalse(c1.contains(c4));
		assertTrue(c4.contains(c1));
	}

	@Test
	public void testRectangle () {
		Rectangle r1 = new Rectangle(0, 0, 1, 1);
		Rectangle r2 = new Rectangle(1, 0, 2, 1);
		assertTrue(r1.overlaps(r1));
		assertFalse(r1.overlaps(r2));
		assertTrue(r1.contains(0, 0));
	}
}
