
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
		// This fails in 1.14.0 because the chars '0' through '9' get interpreted as the JSON Number type, which
		// means they become the char values from (char)(0), also called NUL, through (char)(9).
		{
			CharArray ascii = new CharArray(128);
			for (char c = ' '; c <= '~'; c++) {
				ascii.add(c);
			}
			String data = json.toJson(ascii);
			CharArray ascii2 = json.fromJson(CharArray.class, data);
			assertEquals(ascii, ascii2);
		}
	}

	@Test
	public void testIntArrray () {
		Json json = new Json();
		IntArray numbers = new IntArray(128);
		for (int c = 32; c <= 126; c++) {
			numbers.add(c);
		}
		String data = json.toJson(numbers);
		// Nothing in an IntArray should be quoted.
		assertFalse(data.contains("\""));
		IntArray numbers2 = json.fromJson(IntArray.class, data);
		assertEquals(numbers, numbers2);
	}

	@Test
	public void testFloatArrray () {
		Json json = new Json();
		FloatArray numbers = new FloatArray(128);
		for (int c = 32; c <= 126; c++) {
			numbers.add(c * 0.1f);
		}
		String data = json.toJson(numbers);
		// Nothing in a FloatArray should be quoted.
		assertFalse(data.contains("\""));
		FloatArray numbers2 = json.fromJson(FloatArray.class, data);
		assertEquals(numbers, numbers2);
	}

	@Test
	public void testLongArrray () {
		Json json = new Json();
		boolean quoting = false;
		for (int i = 0; i < 2; i++) {
			json.setQuoteLongValues(quoting);
			LongArray numbers = new LongArray(128);
			for (int c = 32; c <= 126; c++) {
				numbers.add(c * 1234567890L);
			}
			String data = json.toJson(numbers);
			// A LongArray should only contain quotes if json.setQuoteLongValues(true) was called.
			assertEquals(quoting, data.contains("\""));
			LongArray numbers2 = json.fromJson(LongArray.class, data);
			assertEquals(numbers, numbers2);
			quoting = !quoting;
		}
	}
}
