
package com.badlogic.gdx.utils;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class ExtendableEnumTest {
	@Test
	public void addFirstAndLastTest () {
		Scaling testCustomScale = new Scaling("testCustomScale") {
			@Override
			public Vector2 apply (float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
				temp.x = 0;
				temp.y = 0;
				return temp;
			}
		};

		ExtendableEnumTestWrapper wrapper = new ExtendableEnumTestWrapper();
		wrapper.scaling = testCustomScale;

		Json json = new Json();
		String str = json.toJson(wrapper);
		ExtendableEnumTestWrapper dec = json.fromJson(ExtendableEnumTestWrapper.class, str);

		assertEquals(wrapper.scaling.getName(), dec.scaling.getName());
	}

	static class ExtendableEnumTestWrapper {
		Scaling scaling;
	}
}
