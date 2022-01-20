
package com.badlogic.gdx.math.collision;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.math.MathUtils;
import org.junit.Test;

import com.badlogic.gdx.math.Vector3;

public class CollisionTest {

	@Test
	public void testBoundingBox () {
		BoundingBox b1 = new BoundingBox(Vector3.Zero, new Vector3(1, 1, 1));
		BoundingBox b2 = new BoundingBox(new Vector3(1, 1, 1), new Vector3(2, 2, 2));
		assertTrue(b1.contains(Vector3.Zero));
		assertTrue(b1.contains(b1));
		assertFalse(b1.contains(b2));
		// Note, in stage the bottom and left sides are inclusive while the right and top sides are exclusive.
	}

	@Test
	public void testOrientedBoundingBox () {
		OrientedBoundingBox b1 = new OrientedBoundingBox(new BoundingBox(Vector3.Zero, new Vector3(1, 1, 1)));
		OrientedBoundingBox b2 = new OrientedBoundingBox(new BoundingBox(new Vector3(1, 1, 1), new Vector3(2, 2, 2)));
		assertTrue(b1.contains(Vector3.Zero));
		assertTrue(b1.contains(b1));
		assertFalse(b1.contains(b2));
		// Note, in stage the bottom and left sides are inclusive while the right and top sides are exclusive.
	}

	@Test
	public void testOrientedBoundingBoxCollision () {
		OrientedBoundingBox b1 = new OrientedBoundingBox(new BoundingBox(Vector3.Zero, new Vector3(1, 1, 1)));
		OrientedBoundingBox b2 = new OrientedBoundingBox(
			new BoundingBox(new Vector3(1 + MathUtils.FLOAT_ROUNDING_ERROR, 1, 1), new Vector3(2, 2, 2)));

		assertFalse(b1.intersects(b2));

		b1 = new OrientedBoundingBox(new BoundingBox(Vector3.Zero, new Vector3(1, 1, 1)));
		b2 = new OrientedBoundingBox(new BoundingBox(new Vector3(0.5f, 0.5f, 0.5f), new Vector3(2, 2, 2)));

		assertTrue(b1.intersects(b2));
	}

}
