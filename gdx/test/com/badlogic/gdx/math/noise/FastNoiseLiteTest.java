
package com.badlogic.gdx.math.noise;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FastNoiseLiteTest {
	private final FastNoiseLite fastNoiseLite = new FastNoiseLite();

	@Test
	public void testGetNoise1D () {
		for (int i = -256; i < 256; i++) {
			float n = fastNoiseLite.getNoise1D(i);
			assertTrue(n >= -1f && n <= 1f);
		}
	}

	@Test
	public void testGetNoise2D () {
		for (int i = -256; i < 256; i++) {
			for (int j = -256; j < 256; j++) {
				float n = fastNoiseLite.getNoise2D(i, j);
				assertTrue(n >= -1f && n <= 1f);
			}
		}
	}

	@Test
	public void testGetNoise3D () {
		for (int i = -256; i < 256; i++) {
			for (int j = -256; j < 256; j++) {
				for (int k = -256; k < 256; k++) {
					float n = fastNoiseLite.getNoise3D(i, j, k);
					assertTrue(n >= -1f && n <= 1f);
				}
			}
		}
	}
}
