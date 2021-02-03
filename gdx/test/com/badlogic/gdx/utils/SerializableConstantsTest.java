
package com.badlogic.gdx.utils;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;

public class SerializableConstantsTest {
	@Test
	public void testConstantSeralization () {
		TestWrapper wrapper = new TestWrapper();
		wrapper.scaling = Scaling.fit;

		Json json = new Json();
		String str = json.toJson(wrapper);

		assertEquals("{scaling:fit}", str);

		TestWrapper dec = json.fromJson(TestWrapper.class, str);
		// This should match the instance, not just class
		assertEquals(Scaling.fit, dec.scaling);
	}

	@Test
	public void testCustomSeralization () {
		TestWrapper wrapper = new TestWrapper();
		wrapper.scaling = new CustomScaling();

		Json json = new Json();
		String str = json.toJson(wrapper);

		TestWrapper dec = json.fromJson(TestWrapper.class, str);

		// This is a different instance, but same class
		assertEquals(wrapper.scaling.getClass(), dec.scaling.getClass());
	}

	@Test
	public void testNullSeralization () {
		TestWrapper wrapper = new TestWrapper();
		wrapper.scaling = null;

		Json json = new Json();
		String str = json.toJson(wrapper);

		assertEquals("{}", str);

		TestWrapper dec = json.fromJson(TestWrapper.class, str);
		assertEquals(null, dec.scaling);
	}

	/** Tests null serialize where null is specified in the string. */
	@Test
	public void testNullSeralization2 () {
		TestNullWrapper wrapper = new TestNullWrapper();
		wrapper.scaling = null;

		Json json = new Json();
		String str = json.toJson(wrapper);

		assertEquals("{scaling:null}", str);

		TestNullWrapper dec = json.fromJson(TestNullWrapper.class, str);
		assertEquals(null, dec.scaling);
	}

	static class TestWrapper {
		Scaling scaling;
	}

	static class TestNullWrapper {
		Scaling scaling = Scaling.fit;
	}

	static class CustomScaling extends Scaling {
		@Override
		public Vector2 apply (float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
			return temp.setZero();
		}
	}
}
