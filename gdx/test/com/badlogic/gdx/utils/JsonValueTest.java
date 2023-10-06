
package com.badlogic.gdx.utils;

import org.junit.Assert;
import org.junit.Test;

public class JsonValueTest {

	@Test
	public void testAddingRemovedValue () {
		// Prepare two JSON objects
		JsonValue firstObject = new JsonValue(JsonValue.ValueType.object);
		JsonValue secondObject = new JsonValue(JsonValue.ValueType.object);

		firstObject.addChild("a", new JsonValue("A"));
		secondObject.addChild("b", new JsonValue("B"));
		secondObject.addChild("c", new JsonValue("C"));

		// Remove an item from one object and add it to the other
		JsonValue b = secondObject.remove("b");
		firstObject.addChild(b);

		// Check if both objects have the expected children
		Assert.assertNotNull(firstObject.get("a"));
		Assert.assertNotNull(firstObject.get("b"));
		Assert.assertNull(firstObject.get("c"));

		Assert.assertNull(secondObject.get("a"));
		Assert.assertNull(secondObject.get("b"));
		Assert.assertNotNull(secondObject.get("c"));
	}
}
