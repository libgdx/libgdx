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

import java.util.HashMap;
import java.util.Iterator;

import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.BinaryHeap;
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

	// 49 String keys that all have the same two hashCode() results
	// It is extremely easy to generate String keys that have colliding hashCode()s, so we check to make
	// sure ObjectSet and OrderedSet can tolerate them in case of low-complexity malicious use.
	// If they can tolerate these problem values, then ObjectMap and others should too.
	private String[] problemValues = ("21oo 0oq1 0opP 0ooo 0pPo 21pP 21q1 1Poo 1Pq1 1PpP 0q31 0pR1 0q2P 0q1o 232P 231o 2331 0pQP 22QP"
		+ " 22Po 22R1 1QQP 1R1o 1QR1 1R2P 1R31 1QPo 1Qup 1S7p 0r8Q 0r7p 0r92 23X2 2492 248Q 247p 22vQ"
		+ " 22up 1S92 1S8Q 23WQ 23Vp 22w2 1QvQ 1Qw2 1RVp 1RWQ 1RX2 0qX2").split(" ");

	/** Checks that the two values are equal, and that their hashcodes are equal. */
	private void assertEquals (Object a, Object b) {
		if (!a.equals(b)) throw new GdxRuntimeException(a.getClass().getSimpleName() + " equals() failed:\n" + a + "\n!=\n" + b);
		if (!b.equals(a))
			throw new GdxRuntimeException(a.getClass().getSimpleName() + " equals() failed (not symmetric):\n" + b + "\n!=\n" + a);
		if (a.hashCode() != b.hashCode())
			throw new GdxRuntimeException(a.getClass().getSimpleName() + " hashCode() failed:\n" + a + "\n!=\n" + b);
	}

	/** Checks that the two values are not equal, and emits a warning if their hashcodes are equal. */
	private void assertNotEquals (Object a, Object b) {
		if (a.equals(b)) throw new GdxRuntimeException(a.getClass().getSimpleName() + " !equals() failed:\n" + a + "\n==\n" + b);
		if (b.equals(a))
			throw new GdxRuntimeException(a.getClass().getSimpleName() + " !equals() failed (not symmetric):\n" + b + "\n==\n" + a);
		if (a.hashCode() == b.hashCode())
			System.out.println("Warning: " + a.getClass().getSimpleName() + " hashCode() may be incorrect:\n" + a + "\n==\n" + b);
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

	private Object copy (Object object) {
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
		for (int i = 0, n = keys.length; i < n; i++) {
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
		Iterator it = ((Iterable)anotherMap).iterator();
		int iterationCount = 0;
		while (it.hasNext()) {
			Object entry = it.next();
			iterationCount++;
		}
		assertEquals(iterationCount, keys.length);

		// perform an iteration and remove test for every index
		for (int i = 0, n = keys.length; i < n; i++) {
			anotherMap = copy(map);
			it = ((Iterable)anotherMap).iterator();
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

		invoke("clear", map);
		otherMap = newInstance(mapClass);
		assertEquals(map, otherMap);

		int[] clear = {0, 1, 2, 3, keys.length - 1, keys.length, keys.length + 1, 10, 1000};
		for (int i = 0, n = clear.length; i < n; i++) {
			for (int ii = 0, nn = keys.length; ii < nn; ii++) {
				invoke("put", map, keys[ii], values[ii]);
				invoke("put", otherMap, keys[ii], values[ii]);
			}
			assertEquals(map, otherMap);

			invoke("clear", map, clear[i]);
			otherMap = newInstance(mapClass);
			assertEquals(map, otherMap);
		}
	}

	private void testEmptyMaps () {
		{
			System.out.println(IntIntMap.class);
			Object map = new IntIntMap(0);
			Integer[] keys = intValues, values = intValues;
			Object otherMap = new IntIntMap(0);
			assertEquals(map, map);
			for (int i = 0, n = keys.length; i < n; i++) {
				Object anotherMap = copy(map);
				assertEquals(map, anotherMap);
				assertEquals(((IntIntMap)map).get(keys[n - 1], 0), 0);
				((IntIntMap)map).put(keys[i], values[i]);
				((IntIntMap)otherMap).put(keys[i], values[i]);
				assertEquals(map, otherMap);
				assertNotEquals(map, anotherMap);
				((IntIntMap)anotherMap).put(keys[(i + 1) % n], values[i]);
				assertNotEquals(map, anotherMap);
			}

			// perform an iteration test
			Object anotherMap = copy(map);
			Iterator it = ((Iterable)anotherMap).iterator();
			int iterationCount = 0;
			while (it.hasNext()) {
				it.next();
				iterationCount++;
			}
			assertEquals(iterationCount, keys.length);

			// perform an iteration and remove test for every index
			for (int i = 0, n = keys.length; i < n; i++) {
				anotherMap = copy(map);
				it = ((Iterable)anotherMap).iterator();
				iterationCount = 0;
				while (it.hasNext()) {
					it.next();
					if (iterationCount == i) {
						it.remove();
					}
					iterationCount++;
				}
				assertEquals(iterationCount, keys.length);
			}
		}
		{
			System.out.println(IntMap.class);
			Object map = new IntMap(0);
			Integer[] keys = intValues, values = intValues;
			Object otherMap = new IntMap(0);
			assertEquals(map, map);
			for (int i = 0, n = keys.length; i < n; i++) {
				Object anotherMap = copy(map);
				assertEquals(map, anotherMap);
				if (((IntMap)map).get(keys[n - 1]) != null)
					throw new GdxRuntimeException("get() on an impossible key returned non-null");
				((IntMap)map).put(keys[i], values[i]);
				((IntMap)otherMap).put(keys[i], values[i]);
				assertEquals(map, otherMap);
				assertNotEquals(map, anotherMap);
				((IntMap)anotherMap).put(keys[(i + 1) % n], values[i]);
				assertNotEquals(map, anotherMap);
			}

			// perform an iteration test
			Object anotherMap = copy(map);
			Iterator it = ((Iterable)anotherMap).iterator();
			int iterationCount = 0;
			while (it.hasNext()) {
				it.next();
				iterationCount++;
			}
			assertEquals(iterationCount, keys.length);

			// perform an iteration and remove test for every index
			for (int i = 0, n = keys.length; i < n; i++) {
				anotherMap = copy(map);
				it = ((Iterable)anotherMap).iterator();
				iterationCount = 0;
				while (it.hasNext()) {
					it.next();
					if (iterationCount == i) {
						it.remove();
					}
					iterationCount++;
				}
				assertEquals(iterationCount, keys.length);
			}
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
		for (int i = 0, n = values.length; i < n; i++)
			invoke("add", set, values[i]);
		Object otherSet = newInstance(setClass);
		for (int i = 0, n = values.length; i < n; i++)
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

	public void testEntrySet () {
		int hmSize = 1000;
		Object[] objArray = new Object[hmSize];
		Object[] objArray2 = new Object[hmSize];
		for (int i = 0; i < objArray.length; i++) {
			objArray[i] = i;
			objArray2[i] = objArray[i].toString();
		}
		ObjectMap hm = new ObjectMap();
		for (int i = 0; i < objArray.length; i++)
			hm.put(objArray2[i], objArray[i]);
		hm.put("test", null);

		ObjectMap.Entries s = hm.entries();
		Iterator i = s.iterator();
		while (i.hasNext()) {
			ObjectMap.Entry m = (ObjectMap.Entry)i.next();
			assertEquals(hm.containsKey(m.key), true);
			assertEquals(hm.containsValue(m.value, false), true);
		}

		ObjectMap.Entries iter = s.iterator();
		iter.reset();
		hm.remove(iter.next());
		assertEquals(1001, hm.size);
	}

	public void testBinaryHeap () {
		class Node extends BinaryHeap.Node {
			public Node (float value) {
				super(value);
			}

			public boolean equals (Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				return ((Node)o).getValue() == getValue();
			}
		}

		Object[] values = { // @off
			44.683983f, new Node(44.683983f),
			160.47682f, new Node(160.47682f),
			95.038086f, new Node(95.038086f),
			396.49918f, new Node(396.49918f),
			835.0006f, new Node(835.0006f),
			439.67096f, new Node(439.67096f),
			377.55692f, new Node(377.55692f),
			373.29028f, new Node(373.29028f),
			926.524f, new Node(926.524f),
			189.30789f, new Node(189.30789f),
			926.524f, new Node(926.524f),
			924.88995f, new Node(924.88995f),
			700.856f, new Node(700.856f),
			342.5846f, new Node(342.5846f),
			313.3819f, new Node(313.3819f),
			407.9829f, new Node(407.9829f),
			1482.5394f, new Node(1482.5394f),
			1135.7894f, new Node(1135.7894f),
			362.44937f, new Node(362.44937f),
			725.86615f, new Node(725.86615f),
			1656.2006f, new Node(1656.2006f),
			490.8201f, new Node(490.8201f),
			725.86615f, new Node(725.86615f),
			723.18396f, new Node(723.18396f),
			716.36115f, new Node(716.36115f),
			490.8201f, new Node(490.8201f),
			474.9852f, new Node(474.9852f),
			379.61304f, new Node(379.61304f),
			465.81775f, new Node(465.81775f),
			440.83838f, new Node(440.83838f),
			1690.9901f, new Node(1690.9901f),
			1711.5605f, new Node(1711.5605f),
			1795.7483f, new Node(1795.7483f),
			388.60376f, new Node(388.60376f),
			2119.6921f, new Node(2119.6921f),
			1040.5143f, new Node(1040.5143f),
			1018.3097f, new Node(1018.3097f),
			1039.8417f, new Node(1039.8417f),
			1142.326f, new Node(1142.326f),
			1045.692f, new Node(1045.692f),
			820.3383f, new Node(820.3383f),
			474.9852f, new Node(474.9852f),
			430.27383f, new Node(430.27383f),
			506.89728f, new Node(506.89728f),
			973.9379f, new Node(973.9379f),
			723.18396f, new Node(723.18396f),
			619.83624f, new Node(619.83624f),
			1656.2006f, new Node(1656.2006f),
			1547.9089f, new Node(1547.9089f),
			1018.3097f, new Node(1018.3097f),
			930.3666f, new Node(930.3666f),
			1039.8417f, new Node(1039.8417f),
			950.749f, new Node(950.749f),
			1142.326f, new Node(1142.326f),
			1055.636f, new Node(1055.636f),
			1045.692f, new Node(1045.692f),
			958.5852f, new Node(958.5852f),
			820.3383f, new Node(820.3383f),
			771.37115f, new Node(771.37115f),
			506.89728f, new Node(506.89728f),
			417.02042f, new Node(417.02042f),
			930.3666f, new Node(930.3666f),
			864.04517f, new Node(864.04517f),
			950.749f, new Node(950.749f),
			879.2704f, new Node(879.2704f),
			958.5852f, new Node(958.5852f),
			894.9335f, new Node(894.9335f),
			1534.2864f, new Node(1534.2864f),
			619.83624f, new Node(619.83624f),
			548.92786f, new Node(548.92786f),
			924.88995f, new Node(924.88995f),
			905.3478f, new Node(905.3478f),
			440.83838f, new Node(440.83838f),
			436.48087f, new Node(436.48087f),
			1040.5143f, new Node(1040.5143f),
			950.6953f, new Node(950.6953f),
			992.51624f, new Node(992.51624f),
			808.5153f, new Node(808.5153f),
			876.47845f, new Node(876.47845f),
			472.963f, new Node(472.963f),
			465.81775f, new Node(465.81775f),
			461.85135f, new Node(461.85135f),
			1552.4479f, new Node(1552.4479f),
			950.6953f, new Node(950.6953f),
			862.6192f, new Node(862.6192f),
			992.51624f, new Node(992.51624f),
			900.9059f, new Node(900.9059f),
			808.5153f, new Node(808.5153f),
			716.3565f, new Node(716.3565f),
			876.47845f, new Node(876.47845f),
			610.04565f, new Node(610.04565f),
			598.95935f, new Node(598.95935f),
			487.93192f, new Node(487.93192f),
			864.04517f, new Node(864.04517f),
			852.66907f, new Node(852.66907f),
			879.2704f, new Node(879.2704f),
			867.3523f, new Node(867.3523f),
			894.9335f, new Node(894.9335f),
			884.0505f, new Node(884.0505f),
			548.7671f, new Node(548.7671f),
			1437.1154f, new Node(1437.1154f),
			1934.038f, new Node(1934.038f),
			2401.7002f, new Node(2401.7002f),
			973.9379f, new Node(973.9379f),
			903.2409f, new Node(903.2409f),
			1547.9089f, new Node(1547.9089f),
			1481.2589f, new Node(1481.2589f),
			1430.7216f, new Node(1430.7216f)
		}; // @on
		HashMap<Float, Node> m = new HashMap(values.length);
		for (int i = 0, n = values.length; i < n; i += 2)
			m.put((Float)values[i], (Node)values[i + 1]);

		BinaryHeap<Node> h = new BinaryHeap<Node>();

		h.add(m.get(44.683983f));
		if (h.pop().getValue() != 44.683983f) throw new RuntimeException("Should be 44.683983");
		h.add(m.get(160.47682f));
		h.add(m.get(95.038086f));
		h.add(m.get(396.49918f));
		h.add(m.get(835.0006f));
		h.add(m.get(439.67096f));
		h.add(m.get(377.55692f));
		h.add(m.get(373.29028f));
		if (h.pop().getValue() != 95.038086f) throw new RuntimeException("Should be 95.038086");
		h.add(m.get(926.524f));
		if (h.pop().getValue() != 160.47682f) throw new RuntimeException("Should be 160.47682");
		h.add(m.get(189.30789f));
		h.remove(m.get(926.524f));
		h.add(m.get(924.88995f));
		h.add(m.get(700.856f));
		h.add(m.get(342.5846f));
		h.add(m.get(313.3819f));
		if (h.pop().getValue() != 189.30789f) throw new RuntimeException("Should be 189.30789");
		h.add(m.get(407.9829f));
		h.add(m.get(1482.5394f));
		h.add(m.get(1135.7894f));
		h.add(m.get(362.44937f));
		if (h.pop().getValue() != 313.3819f) throw new RuntimeException("Should be 313.3819");
		h.add(m.get(725.86615f));
		h.add(m.get(1656.2006f));
		h.add(m.get(490.8201f));
		if (h.pop().getValue() != 342.5846f) throw new RuntimeException("Should be 342.5846");
		h.remove(m.get(725.86615f));
		h.add(m.get(723.18396f));
		h.add(m.get(716.36115f));
		h.remove(m.get(490.8201f));
		h.add(m.get(474.9852f));
		h.add(m.get(379.61304f));
		if (h.pop().getValue() != 362.44937f) throw new RuntimeException("Should be 362.44937");
		h.add(m.get(465.81775f));
		h.add(m.get(440.83838f));
		h.add(m.get(1690.9901f));
		h.add(m.get(1711.5605f));
		h.add(m.get(1795.7483f));
		h.add(m.get(388.60376f));
		h.add(m.get(2119.6921f));
		if (h.pop().getValue() != 373.29028f) throw new RuntimeException("Should be 373.29028");
		h.add(m.get(1040.5143f));
		h.add(m.get(1018.3097f));
		h.add(m.get(1039.8417f));
		h.add(m.get(1142.326f));
		h.add(m.get(1045.692f));
		h.add(m.get(820.3383f));
		h.remove(m.get(474.9852f));
		h.add(m.get(430.27383f));
		h.add(m.get(506.89728f));
		if (h.pop().getValue() != 377.55692f) throw new RuntimeException("Should be 377.55692");
		h.add(m.get(973.9379f));
		h.remove(m.get(723.18396f));
		h.add(m.get(619.83624f));
		h.remove(m.get(1656.2006f));
		h.add(m.get(1547.9089f));
		if (h.pop().getValue() != 379.61304f) throw new RuntimeException("Should be 379.61304");
		h.remove(m.get(1018.3097f));
		h.add(m.get(930.3666f));
		h.remove(m.get(1039.8417f));
		h.add(m.get(950.749f));
		h.remove(m.get(1142.326f));
		h.add(m.get(1055.636f));
		h.remove(m.get(1045.692f));
		h.add(m.get(958.5852f));
		h.remove(m.get(820.3383f));
		h.add(m.get(771.37115f));
		h.remove(m.get(506.89728f));
		h.add(m.get(417.02042f));
		if (h.pop().getValue() != 388.60376f) throw new RuntimeException("Should be 388.60376");
		h.remove(m.get(930.3666f));
		h.add(m.get(864.04517f));
		h.remove(m.get(950.749f));
		h.add(m.get(879.2704f));
		h.remove(m.get(958.5852f));
		h.add(m.get(894.9335f));
		h.add(m.get(1534.2864f));
		if (h.pop().getValue() != 396.49918f) throw new RuntimeException("Should be 396.49918");
		h.remove(m.get(619.83624f));
		h.add(m.get(548.92786f));
		h.remove(m.get(924.88995f));
		h.add(m.get(905.3478f));
		if (h.pop().getValue() != 407.9829f) throw new RuntimeException("Should be 407.9829");
		h.remove(m.get(440.83838f));
		h.add(m.get(436.48087f));
		if (h.pop().getValue() != 417.02042f) throw new RuntimeException("Should be 417.02042");
		h.remove(m.get(1040.5143f));
		h.add(m.get(950.6953f));
		h.add(m.get(992.51624f));
		h.add(m.get(808.5153f));
		if (h.pop().getValue() != 430.27383f) throw new RuntimeException("Should be 430.27383");
		h.add(m.get(876.47845f));
		h.add(m.get(472.963f));
		if (h.pop().getValue() != 436.48087f) throw new RuntimeException("Should be 436.48087");
		h.remove(m.get(465.81775f));
		h.add(m.get(461.85135f));
		h.add(m.get(1552.4479f));
		if (h.pop().getValue() != 439.67096f) throw new RuntimeException("Should be 439.67096");
		if (h.pop().getValue() != 461.85135f) throw new RuntimeException("Should be 461.85135");
		h.remove(m.get(950.6953f));
		h.add(m.get(862.6192f));
		h.remove(m.get(992.51624f));
		h.add(m.get(900.9059f));
		h.remove(m.get(808.5153f));
		h.add(m.get(716.3565f));
		h.remove(m.get(876.47845f));
		h.add(m.get(610.04565f));
		h.add(m.get(598.95935f));
		h.add(m.get(487.93192f));
		h.remove(m.get(864.04517f));
		h.add(m.get(852.66907f));
		h.remove(m.get(879.2704f));
		h.add(m.get(867.3523f));
		h.remove(m.get(894.9335f));
		h.add(m.get(884.0505f));
		if (h.pop().getValue() != 472.963f) throw new RuntimeException("Should be 472.963");
		if (h.pop().getValue() != 487.93192f) throw new RuntimeException("Should be 487.93192");
		h.add(m.get(548.7671f));
		if (h.pop().getValue() != 548.7671f) throw new RuntimeException("Should be 548.7671");
		h.add(m.get(1437.1154f));
		h.add(m.get(1934.038f));
		h.add(m.get(2401.7002f));
		if (h.pop().getValue() != 548.92786f) throw new RuntimeException("Should be 548.92786");
		h.remove(m.get(973.9379f));
		h.add(m.get(903.2409f));
		h.remove(m.get(1547.9089f));
		h.add(m.get(1481.2589f));
		if (h.pop().getValue() != 598.95935f) throw new RuntimeException("Should be 598.95935");
		h.add(m.get(1430.7216f));

		// at this point in a debugger, you can tell that 610.04565 is in position 1, while 700.856 is in position 0.
		// this is incorrect, but I'm not sure at what point in the test it became incorrect.
		float popped = h.pop().getValue();
		if (popped != 610.04565f) throw new RuntimeException("Should be 610.04565, but is " + popped);
	}

	public void create () {
		testMap(ObjectMap.class, values, valuesWithNulls);
		testMap(OrderedMap.class, values, valuesWithNulls);
		testMap(IdentityMap.class, values, valuesWithNulls);
		testMap(ArrayMap.class, values, valuesWithNulls);
		testMap(ObjectFloatMap.class, values, floatValues);
		testMap(ObjectIntMap.class, values, intValues);
		testMap(IntFloatMap.class, intValues, floatValues);
		testMap(IntIntMap.class, intValues, intValues);
		testMap(IntMap.class, intValues, valuesWithNulls);
		testMap(LongMap.class, longValues, valuesWithNulls);

		testEmptyMaps();

		testArray(Array.class, valuesWithNulls);
		testArray(BooleanArray.class, new Boolean[] {true, false});
		testArray(ByteArray.class, byteValues);
		testArray(CharArray.class, charValues);
		testArray(FloatArray.class, floatValues);
		testArray(IntArray.class, intValues);
		testArray(LongArray.class, longValues);
		testArray(ShortArray.class, shortValues);
		testArray(SnapshotArray.class, values);

		testSet(ObjectSet.class, values);
		testSet(OrderedSet.class, values);
		testSet(IntSet.class, intValues);

		testSet(ObjectSet.class, problemValues);
		testSet(OrderedSet.class, problemValues);

		testEntrySet();

		testBinaryHeap();

		System.out.println("Success!");
	}
}
