
package com.badlogic.gdx.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class JsonTest {

	@Test
	public void testFromJsonObject () {
		Json json = new Json();
		JsonValue value = json.fromJson(null, JsonValue.class, "{\"key\":\"value\"}");
		assertEquals("value", value.getString("key"));
	}

	@Test
	public void testFromJsonArray () {
		Json json = new Json();
		Array<String> value = json.fromJson(null, "[\"value1\",\"value2\"]");
		assertEquals("value1", value.get(0));
		assertEquals("value2", value.get(1));
	}

	@Test
	public void testCharFromNumber () {
		Json json = new Json();
		char value = json.fromJson(char.class, "90");
		assertEquals('Z', value);
	}

	@Test
	public void testReuseReader () {
		Json json = new Json();
		JsonValue value = json.fromJson(null, JsonValue.class, "{\"key\":\"value\"}");
		assertEquals("value", value.getString("key"));
		value = json.fromJson(null, JsonValue.class, "{\"key2\":\"value2\"}");
		assertEquals("value2", value.getString("key2"));
	}
}
