
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

	@Test
	public void testReplaceValue () {
		JsonValue object = new JsonValue(JsonValue.ValueType.object);

		object.addChild("a", new JsonValue("A"));
		object.addChild("b", new JsonValue("B"));
		object.addChild("c", new JsonValue("C"));
		object.addChild("d", new JsonValue("D"));

		object.setChild("b", new JsonValue("X"));
		object.setChild("c", new JsonValue("Y"));

		Assert.assertEquals(object.get("a").asString(), "A");
		Assert.assertEquals(object.get("b").asString(), "X");
		Assert.assertEquals(object.get("c").asString(), "Y");
		Assert.assertEquals(object.get("d").asString(), "D");

		object.setChild("a", new JsonValue("W"));
		JsonValue z = new JsonValue("Z");
		z.name = "d";
		object.setChild(z);

		Assert.assertEquals(object.get("a").parent(), object);
		Assert.assertEquals(object.get("a").asString(), "W");
		Assert.assertEquals(object.get("b").asString(), "X");
		Assert.assertEquals(object.get("c").asString(), "Y");
		Assert.assertEquals(object.get("d").asString(), "Z");
	}

	@Test
	public void testCopyConstructor () {
		JsonValue b = new JsonValue(JsonValue.ValueType.object);
		b.addChild("c", new JsonValue("C"));
		b.addChild("d", new JsonValue("D"));

		JsonValue object = new JsonValue(JsonValue.ValueType.object);
		object.addChild("a", new JsonValue("A"));
		object.addChild("b", b);
		object.addChild("e", new JsonValue(123));

		JsonValue copy = new JsonValue(object);

		Assert.assertEquals(object.get("a").asString(), "A");
		Assert.assertEquals(object.get("b"), b);
		Assert.assertEquals(object.get("b").get("c").asString(), "C");
		Assert.assertEquals(object.get("b").get("d").asString(), "D");
		Assert.assertEquals(object.get("b").parent(), object);
		Assert.assertEquals(object.get("E").asInt(), 123);

		Assert.assertEquals(copy.get("a").asString(), "A");
		Assert.assertNotEquals(copy.get("b"), b);
		Assert.assertEquals(copy.get("b").get("c").asString(), "C");
		Assert.assertEquals(copy.get("b").get("d").asString(), "D");
		Assert.assertEquals(copy.get("b").parent(), copy);
		Assert.assertEquals(copy.get("E").asInt(), 123);

		JsonValue bCopy = new JsonValue(copy.get("b"));
		Assert.assertNotEquals(b, bCopy);
		Assert.assertNotEquals(copy.get("b"), bCopy);
		Assert.assertEquals(bCopy.get("c").asString(), "C");
		Assert.assertEquals(bCopy.get("d").asString(), "D");
		Assert.assertEquals(bCopy.get("d").parent(), bCopy);
	}
}
