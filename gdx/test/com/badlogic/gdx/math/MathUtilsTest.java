
package com.badlogic.gdx.math;

import static com.badlogic.gdx.math.MathUtils.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.badlogic.gdx.math.MathUtils;

public class MathUtilsTest {

	@Test
	public void lerpAngleDeg () {
		assertEquals(10, MathUtils.lerpAngleDeg(10, 30, 0.0f), 0.01f);
		assertEquals(20, MathUtils.lerpAngleDeg(10, 30, 0.5f), 0.01f);
		assertEquals(30, MathUtils.lerpAngleDeg(10, 30, 1.0f), 0.01f);
	}

	@Test
	public void lerpAngleDegCrossingZero () {
		assertEquals(350, MathUtils.lerpAngleDeg(350, 10, 0.0f), 0.01f);
		assertEquals(0, MathUtils.lerpAngleDeg(350, 10, 0.5f), 0.01f);
		assertEquals(10, MathUtils.lerpAngleDeg(350, 10, 1.0f), 0.01f);
	}

	@Test
	public void lerpAngleDegCrossingZeroBackwards () {
		assertEquals(10, MathUtils.lerpAngleDeg(10, 350, 0.0f), 0.01f);
		assertEquals(0, MathUtils.lerpAngleDeg(10, 350, 0.5f), 0.01f);
		assertEquals(350, MathUtils.lerpAngleDeg(10, 350, 1.0f), 0.01f);
	}

	@Test
	public void testNorm(){
		assertEquals(-1.0f, MathUtils.norm(10f, 20f, 0f), 0.01f);
		assertEquals(0.0f,  MathUtils.norm(10f, 20f, 10f), 0.01f);
		assertEquals(0.5f,  MathUtils.norm(10f, 20f, 15f), 0.01f);
		assertEquals(1.0f,  MathUtils.norm(10f, 20f, 20f), 0.01f);
		assertEquals(2.0f,  MathUtils.norm(10f, 20f, 30f), 0.01f);
	}
	
	@Test
	public void testMap(){
		assertEquals(0f, MathUtils.map(10f, 20f, 100f, 200f, 0f), 0.01f);
		assertEquals(100f, MathUtils.map(10f, 20f, 100f, 200f, 10f), 0.01f);
		assertEquals(150f, MathUtils.map(10f, 20f, 100f, 200f, 15f), 0.01f);
		assertEquals(200f, MathUtils.map(10f, 20f, 100f, 200f, 20f), 0.01f);
		assertEquals(300f, MathUtils.map(10f, 20f, 100f, 200f, 30f), 0.01f);
	}
}
