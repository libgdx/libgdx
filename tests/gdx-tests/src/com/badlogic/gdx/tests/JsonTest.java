
package com.badlogic.gdx.tests;

import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.reflect.ArrayReflection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JsonTest extends GdxTest {
	Json json;

	public void create () {
		json = new Json();

// json.fromJson(Test1.class, //
// "{byteArrayField:[-1\n,-2]}"
// );
// if (true) return;

		Test1 test = new Test1();
		test.booleanField = true;
		test.byteField = 123;
		test.charField = 'Z';
		test.shortField = 12345;
		test.intField = 123456;
		test.longField = 123456789;
		test.floatField = 123.456f;
		test.doubleField = 1.23456d;
		test.BooleanField = true;
		test.ByteField = -12;
		test.CharacterField = 'X';
		test.ShortField = -12345;
		test.IntegerField = -123456;
		test.LongField = -123456789l;
		test.FloatField = -123.3f;
		test.DoubleField = -0.121231d;
		test.stringField = "stringvalue";
		test.byteArrayField = new byte[] {2, 1, 0, -1, -2};
		test.map = new ObjectMap();
		test.map.put("one", 1);
		test.map.put("two", 2);
		test.map.put("nine", 9);
		test.stringArray = new Array();
		test.stringArray.add("meow");
		test.stringArray.add("moo");
		test.objectArray = new Array();
		test.objectArray.add("meow");
		test.objectArray.add(new Test1());
		test.longMap = new LongMap<String>(4);
		test.longMap.put(42L, "The Answer");
		test.longMap.put(0x9E3779B97F4A7C15L, "Golden Ratio");
		test.stringFloatMap = new ObjectFloatMap<String>(4);
		test.stringFloatMap.put("point one", 0.1f);
		test.stringFloatMap.put("point double oh seven", 0.007f);
		test.someEnum = SomeEnum.b;

		// IntIntMap can be written, but only as a normal object, not as a kind of map.
		test.intsToIntsUnboxed = new IntIntMap();
		test.intsToIntsUnboxed.put(102, 14);
		test.intsToIntsUnboxed.put(107, 1);
		test.intsToIntsUnboxed.put(10, 2);
		test.intsToIntsUnboxed.put(2, 1);
		test.intsToIntsUnboxed.put(7, 3);
		test.intsToIntsUnboxed.put(101, 63);
		test.intsToIntsUnboxed.put(4, 2);
		test.intsToIntsUnboxed.put(106, 4);
		test.intsToIntsUnboxed.put(1, 1);
		test.intsToIntsUnboxed.put(103, 2);
		test.intsToIntsUnboxed.put(6, 2);
		test.intsToIntsUnboxed.put(3, 1);
		test.intsToIntsUnboxed.put(105, 6);
		test.intsToIntsUnboxed.put(8, 2);
		// The above "should" print like this:
		// {size:14,keyTable:[0,0,102,0,0,0,0,0,107,0,0,10,0,0,0,2,0,0,0,0,7,0,0,0,0,0,101,0,0,0,4,0,106,0,0,0,0,0,0,1,0,0,103,0,0,6,0,0,0,0,0,0,0,0,3,0,0,105,0,0,8,0,0,0],valueTable:[0,0,14,0,0,0,0,0,1,0,0,2,0,0,0,1,0,0,0,0,3,0,0,0,0,0,63,0,0,0,2,0,4,0,0,0,0,0,0,1,0,0,2,0,0,2,0,0,0,0,0,0,0,0,1,0,0,6,0,0,2,0,0,0]}
		// This is potentially correct, but also quite large considering the contents.
		// It would be nice to have IntIntMap look like IntMap<Integer> does, below.

		// IntMap gets special treatment and is written as a kind of map.
		test.intsToIntsBoxed = new IntMap<Integer>();
		test.intsToIntsBoxed.put(102, 14);
		test.intsToIntsBoxed.put(107, 1);
		test.intsToIntsBoxed.put(10, 2);
		test.intsToIntsBoxed.put(2, 1);
		test.intsToIntsBoxed.put(7, 3);
		test.intsToIntsBoxed.put(101, 63);
		test.intsToIntsBoxed.put(4, 2);
		test.intsToIntsBoxed.put(106, 4);
		test.intsToIntsBoxed.put(1, 1);
		test.intsToIntsBoxed.put(103, 2);
		test.intsToIntsBoxed.put(6, 2);
		test.intsToIntsBoxed.put(3, 1);
		test.intsToIntsBoxed.put(105, 6);
		test.intsToIntsBoxed.put(8, 2);
		// The above should print like this:
		// {102:14,107:1,10:2,2:1,7:3,101:63,4:2,106:4,1:1,103:2,6:2,3:1,105:6,8:2}

		roundTrip(test);
		int sum = 0;
		// iterate over an IntIntMap so one of its Entries is instantiated
		for (IntIntMap.Entry e : test.intsToIntsUnboxed) {
			sum += e.value + 1;
		}
		// also iterate over an Array, which does not have any problems
		String concat = "";
		for (String s : test.stringArray) {
			concat += s;
		}
		// by round-tripping again, we verify that the Entries is correctly skipped
		roundTrip(test);
		int sum2 = 0;
		// check and make sure that no entries are skipped over or incorrectly added
		for (IntIntMap.Entry e : test.intsToIntsUnboxed) {
			sum2 += e.value + 1;
		}
		String concat2 = "";
		// also check the Array again
		for (String s : test.stringArray) {
			concat2 += s;
		}

		System.out.println("before: " + sum + ", after: " + sum2);
		System.out.println("before: " + concat + ", after: " + concat2);

		test.someEnum = null;
		roundTrip(test);

		test = new Test1();
		roundTrip(test);

		test.stringArray = new Array();
		roundTrip(test);

		test.stringArray.add("meow");
		roundTrip(test);

		test.stringArray.add("moo");
		roundTrip(test);

		TestMapGraph objectGraph = new TestMapGraph();
		testObjectGraph(objectGraph, "exoticTypeName");

		test = new Test1();
		test.map = new ObjectMap();
		roundTrip(test);

		test.map.put("one", 1);
		roundTrip(test);

		test.map.put("two", 2);
		test.map.put("nine", 9);
		roundTrip(test);

		test.map.put("\nst\nuff\n", 9);
		test.map.put("\r\nst\r\nuff\r\n", 9);
		roundTrip(test);

		equals(json.toJson("meow"), "meow");
		equals(json.toJson("meow "), "\"meow \"");
		equals(json.toJson(" meow"), "\" meow\"");
		equals(json.toJson(" meow "), "\" meow \"");
		equals(json.toJson("\nmeow\n"), "\\nmeow\\n");
		equals(json.toJson(Array.with(1, 2, 3), null, int.class), "[1,2,3]");
		equals(json.toJson(Array.with("1", "2", "3"), null, String.class), "[1,2,3]");
		equals(json.toJson(Array.with(" 1", "2 ", " 3 "), null, String.class), "[\" 1\",\"2 \",\" 3 \"]");
		equals(json.toJson(Array.with("1", "", "3"), null, String.class), "[1,\"\",3]");

		System.out.println();
		System.out.println("Success!");
	}

	private String roundTrip (Object object) {
		String text = json.toJson(object);
		System.out.println(text);
		test(text, object);

		text = json.prettyPrint(object, 130);
		test(text, object);

		return text;
	}

	private void testObjectGraph (TestMapGraph object, String typeName) {
		Json json = new Json();
		json.setTypeName(typeName);
		json.setUsePrototypes(false);
		json.setIgnoreUnknownFields(true);
		json.setOutputType(OutputType.json);
		String text = json.prettyPrint(object);

		TestMapGraph object2 = json.fromJson(TestMapGraph.class, text);

		if (object2.map.size() != object.map.size()) {
			throw new RuntimeException("Too many items in deserialized json map.");
		}

		if (object2.objectMap.size != object.objectMap.size) {
			throw new RuntimeException("Too many items in deserialized json object map.");
		}

		if (object2.arrayMap.size != object.arrayMap.size) {
			throw new RuntimeException("Too many items in deserialized json map.");
		}
	}

	private void test (String text, Object object) {
		check(text, object);

		text = text.replace("{", "/*moo*/{/*moo*/");
		check(text, object);

		text = text.replace("}", "/*moo*/}/*moo*/");
		text = text.replace("[", "/*moo*/[/*moo*/");
		text = text.replace("]", "/*moo*/]/*moo*/");
		text = text.replace(":", "/*moo*/:/*moo*/");
		text = text.replace(",", "/*moo*/,/*moo*/");
		check(text, object);

		text = text.replace("/*moo*/", " /*moo*/ ");
		check(text, object);

		text = text.replace("/*moo*/", "// moo\n");
		check(text, object);

		text = text.replace("\n", "\r\n");
		check(text, object);

		text = text.replace(",", "\n");
		check(text, object);

		text = text.replace("\n", "\r\n");
		check(text, object);

		text = text.replace("\r\n", "\r\n\r\n");
		check(text, object);
	}

	private void check (String text, Object object) {
		Object object2 = json.fromJson(object.getClass(), text);
		equals(object, object2);
	}

	private void equals (Object a, Object b) {
		if (!a.equals(b)) throw new RuntimeException("Fail!\n" + a + "\n!=\n" + b);
	}

	static public class Test1 {
		// Primitives.
		public boolean booleanField;
		public byte byteField;
		public char charField;
		public short shortField;
		public int intField;
		public long longField;
		public float floatField;
		public double doubleField;
		// Primitive wrappers.
		public Boolean BooleanField;
		public Byte ByteField;
		public Character CharacterField;
		public Short ShortField;
		public Integer IntegerField;
		public Long LongField;
		public Float FloatField;
		public Double DoubleField;
		// Other.
		public String stringField;
		public byte[] byteArrayField;
		public Object object;
		public ObjectMap<String, Integer> map;
		public Array<String> stringArray;
		public Array objectArray;
		public LongMap<String> longMap;
		public ObjectFloatMap<String> stringFloatMap;
		public SomeEnum someEnum;
		public IntMap<Integer> intsToIntsBoxed;
		public IntIntMap intsToIntsUnboxed;

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Test1 other = (Test1)obj;
			if (BooleanField == null) {
				if (other.BooleanField != null) return false;
			} else if (!BooleanField.equals(other.BooleanField)) return false;
			if (ByteField == null) {
				if (other.ByteField != null) return false;
			} else if (!ByteField.equals(other.ByteField)) return false;
			if (CharacterField == null) {
				if (other.CharacterField != null) return false;
			} else if (!CharacterField.equals(other.CharacterField)) return false;
			if (DoubleField == null) {
				if (other.DoubleField != null) return false;
			} else if (!DoubleField.equals(other.DoubleField)) return false;
			if (FloatField == null) {
				if (other.FloatField != null) return false;
			} else if (!FloatField.equals(other.FloatField)) return false;
			if (IntegerField == null) {
				if (other.IntegerField != null) return false;
			} else if (!IntegerField.equals(other.IntegerField)) return false;
			if (LongField == null) {
				if (other.LongField != null) return false;
			} else if (!LongField.equals(other.LongField)) return false;
			if (ShortField == null) {
				if (other.ShortField != null) return false;
			} else if (!ShortField.equals(other.ShortField)) return false;
			if (stringField == null) {
				if (other.stringField != null) return false;
			} else if (!stringField.equals(other.stringField)) return false;
			if (booleanField != other.booleanField) return false;

			Object list1 = arrayToList(byteArrayField);
			Object list2 = arrayToList(other.byteArrayField);
			if (list1 != list2) {
				if (list1 == null || list2 == null) return false;
				if (!list1.equals(list2)) return false;
			}

			if (object != other.object) {
				if (object == null || other.object == null) return false;
				if (object != this && !object.equals(other.object)) return false;
			}

			if (map != other.map) {
				if (map == null || other.map == null) return false;
				if (!map.keys().toArray().equals(other.map.keys().toArray())) return false;
				if (!map.values().toArray().equals(other.map.values().toArray())) return false;
			}

			if (stringArray != other.stringArray) {
				if (stringArray == null || other.stringArray == null) return false;
				if (!stringArray.equals(other.stringArray)) return false;
			}

			if (objectArray != other.objectArray) {
				if (objectArray == null || other.objectArray == null) return false;
				if (!objectArray.equals(other.objectArray)) return false;
			}

			if (longMap != other.longMap) {
				if (longMap == null || other.longMap == null) return false;
				if (!longMap.equals(other.longMap)) return false;
			}

			if (stringFloatMap != other.stringFloatMap) {
				if (stringFloatMap == null || other.stringFloatMap == null) return false;
				if (!stringFloatMap.equals(other.stringFloatMap)) return false;
			}

			if (intsToIntsBoxed != other.intsToIntsBoxed) {
				if (intsToIntsBoxed == null || other.intsToIntsBoxed == null) return false;
				if (!intsToIntsBoxed.equals(other.intsToIntsBoxed)) return false;
			}

			if (intsToIntsUnboxed != other.intsToIntsUnboxed) {
				if (intsToIntsUnboxed == null || other.intsToIntsUnboxed == null) return false;
				if (!intsToIntsUnboxed.equals(other.intsToIntsUnboxed)) return false;
			}

			if (byteField != other.byteField) return false;
			if (charField != other.charField) return false;
			if (Double.doubleToLongBits(doubleField) != Double.doubleToLongBits(other.doubleField)) return false;
			if (Float.floatToIntBits(floatField) != Float.floatToIntBits(other.floatField)) return false;
			if (intField != other.intField) return false;
			if (longField != other.longField) return false;
			if (shortField != other.shortField) return false;
			return true;
		}
	}

	public static class TestMapGraph {
		public Map<String, String> map = new HashMap<String, String>();
		public ObjectMap<String, String> objectMap = new ObjectMap<String, String>();
		public ArrayMap<String, String> arrayMap = new ArrayMap<String, String>();

		public TestMapGraph () {
			map.put("a", "b");
			map.put("c", "d");
			objectMap.put("a", "b");
			objectMap.put("c", "d");
			arrayMap.put("a", "b");
			arrayMap.put("c", "d");
		}
	}

	public enum SomeEnum {
		a, b, c;
	}

	static Object arrayToList (Object array) {
		if (array == null || !array.getClass().isArray()) return array;
		ArrayList list = new ArrayList(ArrayReflection.getLength(array));
		for (int i = 0, n = ArrayReflection.getLength(array); i < n; i++)
			list.add(arrayToList(ArrayReflection.get(array, i)));
		return list;
	}
}
