
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

	@Test
	public void testCharArrayCurlyBrace () {
		Json json = new Json();
		// Each of these will get quoted correctly in any libGDX version
		{
			CharArray workingBrackets = CharArray.with('[', ']', '(', ')', '{');
			String data = json.toJson(workingBrackets);
			CharArray workingBrackets2 = json.fromJson(CharArray.class, data);
			assertEquals(workingBrackets, workingBrackets2);
		}
		// Closing curly brace does not get quoted by libGDX 1.14.0, though only with minimal output type
		{
			CharArray curlyBrace = CharArray.with('}');
			String data = json.toJson(curlyBrace);
			CharArray curlyBrace2 = json.fromJson(CharArray.class, data);
			assertEquals(curlyBrace, curlyBrace2);
		}
	}

}
