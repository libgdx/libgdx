/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests;

import java.util.Iterator;
import java.util.Random;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.BooleanArray;
import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.CharArray;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IdentityMap;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntFloatMap;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.LongArray;
import com.badlogic.gdx.utils.LongMap;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.OrderedSet;
import com.badlogic.gdx.utils.ShortArray;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Constructor;
import com.badlogic.gdx.utils.reflect.Method;

/** Tests for the collection classes. Currently, only equals() and hashCode() methods are tested. */
public class CollectionsTest extends GdxTest {
	// Objects to use for test keys/values; no duplicates may exist. All arrays are 10 elements.
	private Object[] values = {"just", "some", "random", "values", true, false, 50, "nope", "yeah", 53};
	private Object[] valuesWithNulls = {"just", "some", null, "values", true, false, 50, "nope", "yeah", 53};
	private Integer[] intValues = {42, 13, 0, -44, 56, 561, 61, -532, -1, 32};
	private Float[] floatValues = {4f, 3.14f, 0f, 5f, 2f, -5f, 43f, 643f, 3525f, 32f};
	private Long[] longValues = {5L, 3L, 41432L, 0L, -4312L, -532L, 1L, 4L, 1362L};
	private Byte[] byteValues = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
	private Short[] shortValues = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
	private Character[] charValues = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};

	/** Checks that the two values are equal, and that their hashcodes are equal. */
	private void assertEquals (Object a, Object b) {
		if (!a.equals(b)) throw new GdxRuntimeException("equals() failed: " + a + " != " + b);
		if (!b.equals(a)) throw new GdxRuntimeException("equals() failed (not symmetric): " + b + " != " + a);
		if (a.hashCode() != b.hashCode()) throw new GdxRuntimeException("hashCode() failed: " + a + " != " + b);
	}

	/** Checks that the two values are not equal, and emits a warning if their hashcodes are equal. */
	private void assertNotEquals (Object a, Object b) {
		if (a.equals(b)) throw new GdxRuntimeException("!equals() failed: " + a + " == " + b);
		if (b.equals(a)) throw new GdxRuntimeException("!equals() failed (not symmetric): " + b + " == " + a);
		if (a.hashCode() == b.hashCode()) System.out.println("Warning: hashCode() may be incorrect: " + a + " == " + b);
	}

	/** Uses reflection to create a new instance of the given type. */
	private Object newInstance (Class<?> clazz) {
		try {
			return ClassReflection.newInstance(clazz);
		} catch (Throwable ex) {
			throw new GdxRuntimeException(ex);
		}
	}

	private void invoke (String methodName, Object object, Object... args) {
		try {
			Method theMethod = null;
			for (Method method : ClassReflection.getMethods(object.getClass())) {
				if (methodName.equals(method.getName()) && method.getParameterTypes().length == args.length) {
					theMethod = method;
					break;
				}
			}
			theMethod.invoke(object, args);
		} catch (Throwable ex) {
			throw new GdxRuntimeException(ex);
		}
	}

	private void set (String fieldName, Object object, Object value) {
		try {
			ClassReflection.getField(object.getClass(), fieldName).set(object, value);
		} catch (Throwable ex) {
			throw new GdxRuntimeException(ex);
		}
	}

	private Object copy(Object object) {
		try {
			Constructor theConstructor = null;
			for (Constructor constructor : ClassReflection.getConstructors(object.getClass())) {
				if (constructor.getParameterTypes().length == 1
						&& ClassReflection.isAssignableFrom(constructor.getParameterTypes()[0], object.getClass())) {
					theConstructor = constructor;
					break;
				}
			}
			return theConstructor.newInstance(object);
		} catch (Throwable ex) {
			throw new GdxRuntimeException(ex);
		}
	}

	private void testMap (Class<?> mapClass, Object[] keys, Object[] values) {
		System.out.println(mapClass);
		Object map = newInstance(mapClass);
		Object otherMap = newInstance(mapClass);
		assertEquals(map, map);
		for (int i = 0, n = keys.length; i < n; ++i) {
			Object anotherMap = copy(map);
			assertEquals(map, anotherMap);
			invoke("put", map, keys[i], values[i]);
			invoke("put", otherMap, keys[i], values[i]);
			assertEquals(map, otherMap);
			assertNotEquals(map, anotherMap);
			invoke("put", anotherMap, keys[(i + 1) % keys.length], values[i]);
			assertNotEquals(map, anotherMap);
		}

		// perform an iteration test
		Object anotherMap = copy(map);
		Iterator it = ((Iterable) anotherMap).iterator();
		int iterationCount = 0;
		while (it.hasNext()) {
			Object entry = it.next();
			iterationCount++;
		}
		assertEquals(iterationCount, keys.length);

		// perform an iteration and remove test for every index
		for (int i = 0, n = keys.length; i < n; ++i) {
			anotherMap = copy(map);
			it = ((Iterable) anotherMap).iterator();
			iterationCount = 0;
			while (it.hasNext()) {
				Object entry = it.next();
				if (iterationCount == i) {
					it.remove();
				}
				iterationCount++;
			}
			assertEquals(iterationCount, keys.length);
		}
	}

	private void testArray (Class<?> arrayClass, Object[] values) {
		System.out.println(arrayClass);
		Object array = newInstance(arrayClass);
		for (int i = 0; i < values.length; i++)
			invoke("add", array, values[i]);
		Object otherArray = newInstance(arrayClass);
		for (int i = 0; i < values.length; i++)
			invoke("add", otherArray, values[i]);
		assertEquals(array, otherArray);
		Object unorderedArray = newInstance(arrayClass);
		set("ordered", unorderedArray, false);
		Object otherUnorderedArray = newInstance(arrayClass);
		set("ordered", otherUnorderedArray, false);
		assertEquals(unorderedArray, unorderedArray);
		assertNotEquals(unorderedArray, otherUnorderedArray);
	}

	private void testSet (Class<?> setClass, Object[] values) {
		System.out.println(setClass);
		Object set = newInstance(setClass);
		for (int i = 0, n = values.length; i < n; ++i)
			invoke("add", set, values[i]);
		Object otherSet = newInstance(setClass);
		for (int i = 0, n = values.length; i < n; ++i)
			invoke("add", otherSet, values[i]);
		Object thirdSet = newInstance(setClass);
		for (int i = 0, n = values.length; i < n; i++)
			invoke("add", thirdSet, values[n - i - 1]);
		assertEquals(set, set);
		assertEquals(set, otherSet);
		assertEquals(set, thirdSet);
		assertEquals(otherSet, set);
		assertEquals(otherSet, otherSet);
		assertEquals(otherSet, thirdSet);
		assertEquals(thirdSet, set);
		assertEquals(thirdSet, otherSet);
		assertEquals(thirdSet, thirdSet);
	}

	public void create () {
		testMap(ArrayMap.class, values, valuesWithNulls);
		testMap(IdentityMap.class, values, valuesWithNulls);
		testMap(IntFloatMap.class, intValues, floatValues);
		testMap(IntIntMap.class, intValues, intValues);
		testMap(IntMap.class, intValues, valuesWithNulls);
		testMap(LongMap.class, longValues, valuesWithNulls);
		testMap(ObjectFloatMap.class, values, floatValues);
		testMap(ObjectIntMap.class, values, intValues);
		testMap(ObjectMap.class, values, valuesWithNulls);
		testMap(OrderedMap.class, values, valuesWithNulls);

		testArray(Array.class, valuesWithNulls);
		testArray(BooleanArray.class, new Boolean[] {true, false});
		testArray(ByteArray.class, byteValues);
		testArray(CharArray.class, charValues);
		testArray(FloatArray.class, floatValues);
		testArray(IntArray.class, intValues);
		testArray(LongArray.class, longValues);
		testArray(ShortArray.class, shortValues);
		testArray(SnapshotArray.class, values);

		testSet(IntSet.class, intValues);
		testSet(ObjectSet.class, values);
		testSet(OrderedSet.class, values);

		System.out.println("Success!");
	}
}
