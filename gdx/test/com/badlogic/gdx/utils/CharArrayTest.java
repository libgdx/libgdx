
package com.badlogic.gdx.utils;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

public class CharArrayTest {
	/** Test constructors */
	@Test
	public void constructorTest () {
		// Default constructor
		CharArray array1 = new CharArray();
		Assert.assertEquals(0, array1.size);
		Assert.assertTrue(array1.ordered);

		// Capacity constructor
		CharArray array2 = new CharArray(100);
		Assert.assertEquals(0, array2.size);
		Assert.assertEquals(100, array2.capacity());
		Assert.assertTrue(array2.ordered);

		// Ordered and capacity constructor
		CharArray array3 = new CharArray(false, 50);
		Assert.assertEquals(0, array3.size);
		Assert.assertEquals(50, array3.capacity());
		Assert.assertFalse(array3.ordered);

		// Copy constructor
		CharArray array4 = new CharArray();
		array4.add('a');
		array4.add('b');
		CharArray array5 = new CharArray(array4);
		Assert.assertEquals(2, array5.size);
		Assert.assertEquals('a', array5.get(0));
		Assert.assertEquals('b', array5.get(1));

		// Array constructor
		char[] chars = {'x', 'y', 'z'};
		CharArray array6 = new CharArray(chars);
		Assert.assertEquals(3, array6.size);
		Assert.assertEquals('x', array6.get(0));
		Assert.assertEquals('z', array6.get(2));

		// Array with offset and count
		CharArray array7 = new CharArray(true, chars, 1, 2);
		Assert.assertEquals(2, array7.size);
		Assert.assertEquals('y', array7.get(0));
		Assert.assertEquals('z', array7.get(1));

		// CharSequence constructor
		CharArray array8 = new CharArray("hello");
		Assert.assertEquals(5, array8.size);
		Assert.assertEquals("hello", array8.toString());

		// StringBuilder constructor
		StringBuilder sb = new StringBuilder("world");
		CharArray array9 = new CharArray(sb);
		Assert.assertEquals(5, array9.size);
		Assert.assertEquals("world", array9.toString());
	}

	/** Test add methods */
	@Test
	public void addTest () {
		CharArray array = new CharArray();

		// Single add
		array.add('a');
		Assert.assertEquals(1, array.size);
		Assert.assertEquals('a', array.get(0));

		// Multiple adds
		array.add('b', 'c');
		Assert.assertEquals(3, array.size);
		Assert.assertEquals('b', array.get(1));
		Assert.assertEquals('c', array.get(2));

		array.add('d', 'e', 'f');
		Assert.assertEquals(6, array.size);
		Assert.assertEquals('f', array.get(5));

		array.add('g', 'h', 'i', 'j');
		Assert.assertEquals(10, array.size);
		Assert.assertEquals('j', array.get(9));

		// AddAll with CharArray
		CharArray array2 = new CharArray();
		array2.add('k');
		array2.add('l');
		array.addAll(array2);
		Assert.assertEquals(12, array.size);
		Assert.assertEquals('k', array.get(10));
		Assert.assertEquals('l', array.get(11));

		// AddAll with array
		array.addAll('m', 'n', 'o');
		Assert.assertEquals(15, array.size);
		Assert.assertEquals('o', array.get(14));

		// AddAll with offset and length
		char[] chars = {'p', 'q', 'r', 's', 't'};
		array.addAll(chars, 1, 3);
		Assert.assertEquals(18, array.size);
		Assert.assertEquals('q', array.get(15));
		Assert.assertEquals('r', array.get(16));
		Assert.assertEquals('s', array.get(17));
	}

	/** Test get and set methods */
	@Test
	public void getSetTest () {
		CharArray array = new CharArray();
		array.addAll('a', 'b', 'c', 'd', 'e');

		// Get
		Assert.assertEquals('a', array.get(0));
		Assert.assertEquals('e', array.get(4));

		// Set
		array.set(2, 'Z');
		Assert.assertEquals('Z', array.get(2));

		// Incr
		array.set(0, (char)65); // 'A'
		array.incr(0, (char)1);
		Assert.assertEquals((char)66, array.get(0)); // 'B'

		// Incr all
		CharArray array2 = new CharArray();
		array2.addAll((char)1, (char)2, (char)3);
		array2.incr((char)10);
		Assert.assertEquals((char)11, array2.get(0));
		Assert.assertEquals((char)12, array2.get(1));
		Assert.assertEquals((char)13, array2.get(2));

		// Mul
		array2.set(0, (char)5);
		array2.mul(0, (char)3);
		Assert.assertEquals((char)15, array2.get(0));

		// Mul all
		CharArray array3 = new CharArray();
		array3.addAll((char)2, (char)3, (char)4);
		array3.mul((char)2);
		Assert.assertEquals((char)4, array3.get(0));
		Assert.assertEquals((char)6, array3.get(1));
		Assert.assertEquals((char)8, array3.get(2));
	}

	/** Test remove methods */
	@Test
	public void removeTest () {
		// Test ordered removal
		CharArray array = new CharArray(true, 10);
		array.addAll('a', 'b', 'c', 'd', 'e');

		// RemoveValue
		Assert.assertTrue(array.removeValue('c'));
		Assert.assertEquals(4, array.size);
		Assert.assertEquals('a', array.get(0));
		Assert.assertEquals('b', array.get(1));
		Assert.assertEquals('d', array.get(2));
		Assert.assertEquals('e', array.get(3));
		Assert.assertFalse(array.removeValue('z'));

		// RemoveIndex
		char removed = array.removeIndex(1);
		Assert.assertEquals('b', removed);
		Assert.assertEquals(3, array.size);
		Assert.assertEquals('a', array.get(0));
		Assert.assertEquals('d', array.get(1));
		Assert.assertEquals('e', array.get(2));

		// RemoveRange
		array.addAll('f', 'g', 'h', 'i');
		array.removeRange(1, 4);
		Assert.assertEquals(3, array.size);
		Assert.assertEquals('a', array.get(0));
		Assert.assertEquals('h', array.get(1));
		Assert.assertEquals('i', array.get(2));

		// Test unordered removal
		CharArray unordered = new CharArray(false, 10);
		unordered.addAll('a', 'b', 'c', 'd', 'e');

		Assert.assertTrue(unordered.removeValue('b'));
		Assert.assertEquals(4, unordered.size);
		// In unordered removal, last element is moved to removed position
		Assert.assertEquals('a', unordered.get(0));
		Assert.assertEquals('e', unordered.get(1));
		Assert.assertEquals('c', unordered.get(2));
		Assert.assertEquals('d', unordered.get(3));

		// RemoveAll
		CharArray toRemove = new CharArray();
		toRemove.addAll('a', 'd');
		Assert.assertTrue(unordered.removeAll(toRemove));
		Assert.assertEquals(2, unordered.size);
		Assert.assertEquals('c', unordered.get(0));
		Assert.assertEquals('e', unordered.get(1));
	}

	/** Test search methods */
	@Test
	public void searchTest () {
		CharArray array = new CharArray();
		array.addAll('h', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd');

		// Contains
		Assert.assertTrue(array.contains('l'));
		Assert.assertFalse(array.contains('z'));

		// IndexOf
		Assert.assertEquals(0, array.indexOf('h'));
		Assert.assertEquals(2, array.indexOf('l'));
		Assert.assertEquals(-1, array.indexOf('z'));

		// LastIndexOf
		Assert.assertEquals(9, array.lastIndexOf('l'));
		Assert.assertEquals(7, array.lastIndexOf('o'));
		Assert.assertEquals(-1, array.lastIndexOf('z'));

		// String contains
		Assert.assertTrue(array.contains("hello"));
		Assert.assertTrue(array.contains("world"));
		Assert.assertFalse(array.contains("xyz"));

		// String indexOf
		Assert.assertEquals(0, array.indexOf("hello"));
		Assert.assertEquals(6, array.indexOf("world"));
		Assert.assertEquals(-1, array.indexOf("xyz"));

		// LastIndexOf string
		Assert.assertEquals(0, array.lastIndexOf("hello"));
		Assert.assertEquals(2, array.lastIndexOf("ll"));
	}

	/** Test stack operations */
	@Test
	public void stackTest () {
		CharArray array = new CharArray();

		// Push (add)
		array.add('a');
		array.add('b');
		array.add('c');

		// Pop
		Assert.assertEquals('c', array.pop());
		Assert.assertEquals(2, array.size);

		// Peek
		Assert.assertEquals('b', array.peek());
		Assert.assertEquals(2, array.size); // Size shouldn't change

		// First
		Assert.assertEquals('a', array.first());

		// Empty checks
		Assert.assertTrue(array.notEmpty());
		Assert.assertFalse(array.isEmpty());

		array.clear();
		Assert.assertFalse(array.notEmpty());
		Assert.assertTrue(array.isEmpty());
	}

	/** Test array operations */
	@Test
	public void arrayOperationsTest () {
		CharArray array = new CharArray();
		array.addAll('d', 'b', 'e', 'a', 'c');

		// Sort
		array.sort();
		Assert.assertEquals('a', array.get(0));
		Assert.assertEquals('b', array.get(1));
		Assert.assertEquals('c', array.get(2));
		Assert.assertEquals('d', array.get(3));
		Assert.assertEquals('e', array.get(4));

		// Swap
		array.swap(0, 4);
		Assert.assertEquals('e', array.get(0));
		Assert.assertEquals('a', array.get(4));

		// Reverse
		CharArray array2 = new CharArray();
		array2.addAll('1', '2', '3', '4', '5');
		array2.reverse();
		Assert.assertEquals('5', array2.get(0));
		Assert.assertEquals('4', array2.get(1));
		Assert.assertEquals('3', array2.get(2));
		Assert.assertEquals('2', array2.get(3));
		Assert.assertEquals('1', array2.get(4));

		// Truncate
		array2.truncate(3);
		Assert.assertEquals(3, array2.size);
		Assert.assertEquals('5', array2.get(0));
		Assert.assertEquals('4', array2.get(1));
		Assert.assertEquals('3', array2.get(2));

		// Clear
		array2.clear();
		Assert.assertEquals(0, array2.size);
	}

	/** Test append methods */
	@Test
	public void appendTest () {
		CharArray array = new CharArray();

		// Append boolean
		array.append(true);
		Assert.assertEquals("true", array.toString());
		array.clear();

		array.append(false);
		Assert.assertEquals("false", array.toString());
		array.clear();

		// Append char
		array.append('X');
		Assert.assertEquals("X", array.toString());
		array.clear();

		// Append int
		array.append(123);
		Assert.assertEquals("123", array.toString());
		array.clear();

		// Append int with padding
		array.append(42, 5, '0');
		Assert.assertEquals("00042", array.toString());
		array.clear();

		// Append long
		array.append(9876543210L);
		Assert.assertEquals("9876543210", array.toString());
		array.clear();

		// Append float/double
		array.append(3.14f);
		Assert.assertEquals("3.14", array.toString());
		array.clear();

		array.append(2.71828);
		Assert.assertEquals("2.71828", array.toString());
		array.clear();

		// Append String
		array.append("Hello");
		Assert.assertEquals("Hello", array.toString());

		array.append(" World");
		Assert.assertEquals("Hello World", array.toString());
		array.clear();

		// Append null
		array.append((String)null);
		Assert.assertEquals("null", array.toString());
		array.clear();

		// Append with separator
		array.append("one");
		array.appendSeparator(',');
		array.append("two");
		array.appendSeparator(',');
		array.append("three");
		Assert.assertEquals("one,two,three", array.toString());
		array.clear();

		// Append CharArray
		CharArray other = new CharArray("test");
		array.append(other);
		Assert.assertEquals("test", array.toString());
		array.clear();

		// Append StringBuilder
		StringBuilder sb = new StringBuilder("builder");
		array.append(sb);
		Assert.assertEquals("builder", array.toString());
		array.clear();

		// Append StringBuffer
		StringBuffer sbuf = new StringBuffer("buffer");
		array.append(sbuf);
		Assert.assertEquals("buffer", array.toString());
	}

	/** Test appendln methods */
	@Test
	public void appendlnTest () {
		CharArray array = new CharArray();

		array.appendln("Line 1");
		array.appendln("Line 2");
		array.append("Line 3");

		String result = array.toString();
		Assert.assertEquals("Line 1\nLine 2\nLine 3", result);

		array.clear();
		array.appendln();
		Assert.assertEquals("\n", array.toString());
	}

	/** Test padding and fixed width methods */
	@Test
	public void paddingTest () {
		CharArray array = new CharArray();

		// Append padding
		array.append("Hello");
		array.appendPadding(5, '*');
		Assert.assertEquals("Hello*****", array.toString());
		array.clear();

		// Fixed width pad left
		array.appendFixedWidthPadLeft("42", 5, '0');
		Assert.assertEquals("00042", array.toString());
		array.clear();

		array.appendFixedWidthPadLeft("12345", 3, '0');
		Assert.assertEquals("345", array.toString()); // Keeps rightmost chars when too long
		array.clear();

		// Fixed width pad right
		array.appendFixedWidthPadRight("Hi", 5, ' ');
		Assert.assertEquals("Hi   ", array.toString());
		array.clear();
	}

	/** Test delete methods */
	@Test
	public void deleteTest () {
		CharArray array = new CharArray("Hello World!");

		// Delete range
		array.delete(5, 11);
		Assert.assertEquals("Hello!", array.toString());

		// Delete char at
		array.deleteCharAt(5);
		Assert.assertEquals("Hello", array.toString());

		// Delete all occurrences of char
		array = new CharArray("Hello World!");
		array.deleteAll('l');
		Assert.assertEquals("Heo Word!", array.toString());

		// Delete first occurrence of char
		array = new CharArray("Hello World!");
		array.deleteFirst('l');
		Assert.assertEquals("Helo World!", array.toString());

		// Delete all occurrences of string
		array = new CharArray("Hello World! Hello!");
		array.deleteAll("Hello");
		Assert.assertEquals(" World! !", array.toString());

		// Delete first occurrence of string
		array = new CharArray("Hello World! Hello!");
		array.deleteFirst("Hello");
		Assert.assertEquals(" World! Hello!", array.toString());
	}

	/** Test replace methods */
	@Test
	public void replaceTest () {
		CharArray array = new CharArray("Hello World!");

		// Replace first char
		Assert.assertTrue(array.replaceFirst('l', 'L'));
		Assert.assertEquals("HeLlo World!", array.toString());
		Assert.assertFalse(array.replaceFirst('z', 'Z'));

		// Replace all chars
		array = new CharArray("Hello World!");
		int count = array.replaceAll('l', 'L');
		Assert.assertEquals(3, count);
		Assert.assertEquals("HeLLo WorLd!", array.toString());

		// Replace string range
		array = new CharArray("Hello World!");
		array.replace(0, 5, "Hi");
		Assert.assertEquals("Hi World!", array.toString());

		// Replace all strings
		array = new CharArray("Hello World! Hello!");
		array.replaceAll("Hello", "Hi");
		Assert.assertEquals("Hi World! Hi!", array.toString());

		// Replace first string
		array = new CharArray("Hello World! Hello!");
		array.replaceFirst("Hello", "Hi");
		Assert.assertEquals("Hi World! Hello!", array.toString());

		// Replace char with string
		array = new CharArray("a-b-c");
		array.replace('-', " to ");
		Assert.assertEquals("a to b to c", array.toString());
	}

	/** Test insert methods */
	@Test
	public void insertTest () {
		CharArray array = new CharArray("Hello!");

		// Insert char
		array.insert(5, ' ');
		Assert.assertEquals("Hello !", array.toString());

		// Insert string
		array.insert(6, "World");
		Assert.assertEquals("Hello World!", array.toString());

		// Insert at beginning
		array.insert(0, "Say ");
		Assert.assertEquals("Say Hello World!", array.toString());

		// Insert boolean
		array = new CharArray("Value: ");
		array.insert(7, true);
		Assert.assertEquals("Value: true", array.toString());

		// Insert numbers
		array = new CharArray("Number: ");
		array.insert(8, 42);
		Assert.assertEquals("Number: 42", array.toString());

		// Insert char array
		array = new CharArray("AB");
		char[] chars = {'C', 'D', 'E'};
		array.insert(1, chars);
		Assert.assertEquals("ACDEB", array.toString());

		// Insert range
		array = new CharArray("AC");
		array.insertRange(1, 2);
		array.set(1, 'B');
		array.set(2, 'B');
		Assert.assertEquals("ABBC", array.toString());
	}

	/** Test substring methods */
	@Test
	public void substringTest () {
		CharArray array = new CharArray("Hello World!");

		// Substring
		Assert.assertEquals("Hello", array.substring(0, 5));
		Assert.assertEquals("World!", array.substring(6));
		Assert.assertEquals("World", array.substring(6, 11));

		// LeftString
		Assert.assertEquals("Hello", array.leftString(5));
		Assert.assertEquals("", array.leftString(0));
		Assert.assertEquals("Hello World!", array.leftString(20)); // More than length

		// RightString
		Assert.assertEquals("World!", array.rightString(6));
		Assert.assertEquals("", array.rightString(0));
		Assert.assertEquals("Hello World!", array.rightString(20)); // More than length

		// MidString
		Assert.assertEquals("World", array.midString(6, 5));
		Assert.assertEquals("", array.midString(6, 0));
		Assert.assertEquals("World!", array.midString(6, 10)); // More than available
	}

	/** Test string comparison methods */
	@Test
	public void stringComparisonTest () {
		CharArray array = new CharArray("Hello World");

		// StartsWith
		Assert.assertTrue(array.startsWith("Hello"));
		Assert.assertFalse(array.startsWith("World"));
		Assert.assertTrue(array.startsWith(""));

		// EndsWith
		Assert.assertTrue(array.endsWith("World"));
		Assert.assertFalse(array.endsWith("Hello"));
		Assert.assertTrue(array.endsWith(""));

		// Contains
		Assert.assertTrue(array.contains("Hello"));
		Assert.assertTrue(array.contains("World"));
		Assert.assertTrue(array.contains(" "));
		Assert.assertFalse(array.contains("xyz"));

		// ContainsIgnoreCase
		Assert.assertTrue(array.containsIgnoreCase("hello"));
		Assert.assertTrue(array.containsIgnoreCase("WORLD"));
		Assert.assertFalse(array.containsIgnoreCase("xyz"));

		// Equals
		CharArray other = new CharArray("Hello World");
		Assert.assertTrue(array.equals(other));
		Assert.assertTrue(array.equalsString("Hello World"));

		other.append("!");
		Assert.assertFalse(array.equals(other));

		// EqualsIgnoreCase
		CharArray upper = new CharArray("HELLO WORLD");
		Assert.assertTrue(array.equalsIgnoreCase(upper));
		Assert.assertTrue(array.equalsIgnoreCase("hello world"));
	}

	/** Test CharSequence methods */
	@Test
	public void charSequenceTest () {
		CharArray array = new CharArray("Hello World!");

		// Length
		Assert.assertEquals(12, array.length());

		// CharAt
		Assert.assertEquals('H', array.charAt(0));
		Assert.assertEquals('!', array.charAt(11));

		// SubSequence
		CharSequence sub = array.subSequence(0, 5);
		Assert.assertEquals("Hello", sub.toString());
	}

	/** Test trim and capacity methods */
	@Test
	public void trimCapacityTest () {
		CharArray array = new CharArray(100);
		array.append("Hello");

		Assert.assertEquals(100, array.capacity());
		Assert.assertEquals(5, array.size);

		// Trim
		array.trim();
		Assert.assertEquals("Hello", array.toString());
		Assert.assertEquals(5, array.size);

		// TrimToSize
		array = new CharArray(100);
		array.append("Test");
		array.trimToSize();
		Assert.assertEquals(4, array.capacity());

		// SetLength
		array = new CharArray("Hello");
		array.setLength(3);
		Assert.assertEquals("Hel", array.toString());

		array.setLength(5);
		Assert.assertEquals(5, array.length());
		// Extended with null chars
	}

	/** Test hashCode and equals */
	@Test
	public void hashCodeEqualsTest () {
		CharArray array1 = new CharArray("Hello");
		CharArray array2 = new CharArray("Hello");
		CharArray array3 = new CharArray("World");

		// Equals
		Assert.assertTrue(array1.equals(array2));
		Assert.assertFalse(array1.equals(array3));
		Assert.assertFalse(array1.equals(null));
		Assert.assertFalse(array1.equals("Hello")); // Different type

		// HashCode
		Assert.assertEquals(array1.hashCode(), array2.hashCode());
		Assert.assertNotEquals(array1.hashCode(), array3.hashCode());
	}

	/** Test Reader and Writer */
	@Test
	public void readerWriterTest () throws IOException {
		CharArray array = new CharArray("Hello World!");

		// Test Reader
		Reader reader = array.reader();
		char[] buffer = new char[5];
		int read = reader.read(buffer);
		Assert.assertEquals(5, read);
		Assert.assertArrayEquals(new char[] {'H', 'e', 'l', 'l', 'o'}, buffer);

		// Test single char read
		Assert.assertEquals(' ', reader.read());
		Assert.assertEquals('W', reader.read());

		// Test skip
		reader.skip(2);
		Assert.assertEquals('l', reader.read());

		// Test Writer
		CharArray array2 = new CharArray();
		Writer writer = array2.writer();
		writer.write("Test");
		Assert.assertEquals("Test", array2.toString());

		writer.write(' ');
		writer.write(new char[] {'1', '2', '3'});
		Assert.assertEquals("Test 123", array2.toString());
	}

	/** Test Unicode/code point methods */
	@Test
	public void unicodeTest () {
		CharArray array = new CharArray();

		// Append code point (emoji)
		int smiley = 0x1F600; // 😀
		array.appendCodePoint(smiley);
		Assert.assertEquals(2, array.size); // Surrogate pair

		// Code point at
		int cp = array.codePointAt(0);
		Assert.assertEquals(smiley, cp);

		// Code point count
		array.append("Hello");
		int count = array.codePointCount(0, array.size);
		Assert.assertEquals(6, count); // 1 emoji + 5 chars

		// Reverse with code points
		CharArray array2 = new CharArray();
		array2.appendCodePoint(0x1F600); // 😀
		array2.append("Hi");
		array2.reverseCodePoints();
		Assert.assertEquals("iH", array2.substring(0, 2));
	}

	/** Test iterator methods */
	@Test
	public void iteratorTest () {
		CharArray array = new CharArray();

		// AppendAll with Iterable
		ArrayList<String> list = new ArrayList<String>();
		list.add("One");
		list.add("Two");
		list.add("Three");
		array.appendAll(list);
		Assert.assertEquals("OneTwoThree", array.toString());

		// AppendWithSeparators
		array.clear();
		array.appendWithSeparators(list, ", ");
		Assert.assertEquals("One, Two, Three", array.toString());

		// With Iterator
		array.clear();
		Iterator<String> iter = list.iterator();
		array.appendAll(iter);
		Assert.assertEquals("OneTwoThree", array.toString());

		// AppendWithSeparators with array
		array.clear();
		String[] strArray = {"A", "B", "C"};
		array.appendWithSeparators(strArray, "-");
		Assert.assertEquals("A-B-C", array.toString());
	}

	/** Test edge cases and error conditions */
	@Test
	public void edgeCasesTest () {
		CharArray array = new CharArray();

		// Empty array operations
		Assert.assertEquals(-1, array.indexOf('a'));
		Assert.assertEquals(-1, array.lastIndexOf('a'));
		Assert.assertFalse(array.contains('a'));
		Assert.assertFalse(array.removeValue('a'));

		// Append empty/null strings
		array.append("");
		Assert.assertEquals(0, array.size);

		array.append((String)null);
		Assert.assertEquals("null", array.toString());

		// Large capacity
		CharArray large = new CharArray(10000);
		for (int i = 0; i < 10000; i++) {
			large.add((char)('A' + (i % 26)));
		}
		Assert.assertEquals(10000, large.size);

		// Test boundaries
		try {
			array.get(100);
			Assert.fail("Should throw exception");
		} catch (IndexOutOfBoundsException e) {
			// Expected
		}

		try {
			array.set(100, 'x');
			Assert.fail("Should throw exception");
		} catch (IndexOutOfBoundsException e) {
			// Expected
		}
	}

	/** Test toArray conversions */
	@Test
	public void toArrayTest () {
		CharArray array = new CharArray();
		array.addAll('a', 'b', 'c', 'd', 'e');

		// ToCharArray
		char[] chars = array.toCharArray();
		Assert.assertArrayEquals(new char[] {'a', 'b', 'c', 'd', 'e'}, chars);

		// GetChars
		char[] target = new char[10];
		array.getChars(1, 4, target, 2);
		Assert.assertEquals('b', target[2]);
		Assert.assertEquals('c', target[3]);
		Assert.assertEquals('d', target[4]);
	}

	/** Test drain methods */
	@Test
	public void drainTest () {
		CharArray array = new CharArray("Hello World!");

		// DrainChar
		char drained = array.drainChar(6);
		Assert.assertEquals('W', drained);
		Assert.assertEquals("Hello orld!", array.toString());

		// DrainChars
		char[] target = new char[5];
		int count = array.drainChars(0, 5, target, 0);
		Assert.assertEquals(5, count);
		Assert.assertArrayEquals(new char[] {'H', 'e', 'l', 'l', 'o'}, target);
		Assert.assertEquals(" orld!", array.toString());
	}

	/** Test appendSeparator variations */
	@Test
	public void appendSeparatorTest () {
		CharArray array = new CharArray();

		// First append - no separator
		array.appendSeparator(',');
		array.append("first");
		Assert.assertEquals("first", array.toString());

		// Second append - separator added
		array.appendSeparator(',');
		array.append("second");
		Assert.assertEquals("first,second", array.toString());

		// With default for empty
		CharArray array2 = new CharArray();
		array2.appendSeparator(',', ';');
		Assert.assertEquals(";", array2.toString());

		// With loop index
		CharArray array3 = new CharArray();
		for (int i = 0; i < 3; i++) {
			array3.appendSeparator(',', i);
			array3.append("item" + i);
		}
		Assert.assertEquals("item0,item1,item2", array3.toString());

		// String separators
		CharArray array4 = new CharArray();
		array4.append("A");
		array4.appendSeparator(" | ");
		array4.append("B");
		array4.appendSeparator(" | ");
		array4.append("C");
		Assert.assertEquals("A | B | C", array4.toString());
	}

	/** Test random access */
	@Test
	public void randomTest () {
		CharArray array = new CharArray();
		array.addAll('a', 'b', 'c', 'd', 'e');

		// Random should return one of the elements
		char random = array.random();
		Assert.assertTrue(array.contains(random));

		// Shuffle - just verify size is maintained
		array.shuffle();
		Assert.assertEquals(5, array.size);
		// Elements should still be there, just in different order
		Assert.assertTrue(array.contains('a'));
		Assert.assertTrue(array.contains('b'));
		Assert.assertTrue(array.contains('c'));
		Assert.assertTrue(array.contains('d'));
		Assert.assertTrue(array.contains('e'));
	}

	/** Test appendTo method */
	@Test
	public void appendToTest () throws IOException {
		CharArray array = new CharArray("Hello World");

		// Append to StringBuilder
		StringBuilder sb = new StringBuilder("Start: ");
		array.appendTo(sb);
		Assert.assertEquals("Start: Hello World", sb.toString());

		// Append to StringBuffer
		StringBuffer sbuf = new StringBuffer("Start: ");
		array.appendTo(sbuf);
		Assert.assertEquals("Start: Hello World", sbuf.toString());
	}

	/** Test setCharAt */
	@Test
	public void setCharAtTest () {
		CharArray array = new CharArray("Hello");
		array.setCharAt(1, 'a');
		Assert.assertEquals("Hallo", array.toString());

		// Chain calls
		array.setCharAt(2, 'p').setCharAt(3, 'p').setCharAt(4, 'y');
		Assert.assertEquals("Happy", array.toString());
	}

	/** Test toStringAndClear */
	@Test
	public void toStringAndClearTest () {
		CharArray array = new CharArray("Test String");
		String result = array.toStringAndClear();
		Assert.assertEquals("Test String", result);
		Assert.assertEquals(0, array.size);
		Assert.assertTrue(array.isEmpty());
	}

	/** Test toString with separator */
	@Test
	public void toStringWithSeparatorTest () {
		CharArray array = new CharArray();
		array.addAll('a', 'b', 'c', 'd', 'e');

		String result = array.toString(",");
		Assert.assertEquals("a,b,c,d,e", result);

		// Empty array
		CharArray empty = new CharArray();
		Assert.assertEquals("", empty.toString(","));

		// Single element
		CharArray single = new CharArray();
		single.add('x');
		Assert.assertEquals("x", single.toString(","));
	}
}
