
package com.badlogic.gdx.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import com.badlogic.gdx.utils.FloatArray;

public class ConvexHullTest {
	@Test
	public void testComputePolygon () {
		ConvexHull convexHull = new ConvexHull();
		float[] rawPolygon = new float[] {0, 0, 0, 1, 1, 1, 1, 0};
		float[] polygonCounterclockwise = new float[] {1, 0, 1, 1, 0, 1, 0, 0};
		assertArraySimilar(convexHull.computePolygon(rawPolygon, 0, 8, false), polygonCounterclockwise, 0, 8);
		assertArraySimilar(convexHull.computePolygon(rawPolygon, false), polygonCounterclockwise, 0, 8);
		assertArraySimilar(convexHull.computePolygon(rawPolygon, 2, 6, false), polygonCounterclockwise, 0, 6);
		assertArraySimilar(convexHull.computePolygon(rawPolygon, 0, 6, false), polygonCounterclockwise, 2, 6);

		assertArraySimilar(new ConvexHull().computePolygon(rawPolygon, 0, 8, false), polygonCounterclockwise, 0, 8);
		assertArraySimilar(new ConvexHull().computePolygon(rawPolygon, false), polygonCounterclockwise, 0, 8);
		assertArraySimilar(new ConvexHull().computePolygon(rawPolygon, 2, 6, false), polygonCounterclockwise, 0, 6);
		assertArraySimilar(new ConvexHull().computePolygon(rawPolygon, 0, 6, false), polygonCounterclockwise, 2, 6);
	}

	private void assertArraySimilar (FloatArray array, float[] witness, int witnessOffset, int witnessCount) {

		// under this method the same contents of the arrays must be the in the same order (and have the same values),
		// but they are not required to start with the same element
		// The first two elements of the output array should correspond to the last two elements,
		// otherwise the last two element is ignored.

		int witnessLength = witnessCount + witnessOffset;
		assertTrue(witnessCount + witnessOffset <= witness.length);
		assertEquals(witnessCount + 2, array.size);
		assertEquals(array.items[0], array.items[array.size - 2], 0);
		assertEquals(array.items[1], array.items[array.size - 1], 0);

		for (int offset = 0; offset < witnessLength; offset++) {
			boolean contentMatches = true;
			for (int i = 0; i < witnessLength; i++) {
				int j = ((offset + i) % witnessCount) + witnessOffset;
				if (array.get(i) != witness[j]) {
					contentMatches = false;
					break;
				}
			}
			if (contentMatches) {
				return;
			}
		}

		fail("Array items " + array.toString() + " does not match witness array " + Arrays.toString(witness));
	}
}
