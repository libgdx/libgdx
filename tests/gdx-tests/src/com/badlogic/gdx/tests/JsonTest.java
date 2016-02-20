
package com.badlogic.gdx.tests;

import java.util.ArrayList;

import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ArrayReflection;

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
		test.someEnum = SomeEnum.b;
		roundTrip(test);

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
		public SomeEnum someEnum;

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
