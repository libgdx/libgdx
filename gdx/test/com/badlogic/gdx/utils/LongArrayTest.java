/*
 * Copyright 2024 See AUTHORS file
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.badlogic.gdx.utils;

import org.junit.Assert;
import org.junit.Test;

public class LongArrayTest {
	/** Test of the different adding methods */
	@Test
	public void addTest () {
		/*
		 * Test of the classic add(long value) method, it should be adding a number at the first available place.
		 */
		LongArray longArray1 = new LongArray(3);
		longArray1.add(3);
		Assert.assertArrayEquals(new long[] {3}, longArray1.toArray());

		/*
		 * Test of the add(long value1,long value2) method, it should be adding these numbers in the array, when it is possible.
		 */
		LongArray longArray2 = new LongArray();
		longArray2.add(1, 2);
		Assert.assertArrayEquals(new long[] {1, 2}, longArray2.toArray());

		/*
		 * Some test with the addAll(LongArray array) and addAll (long... array) methods. The second call should resize longArray3
		 * to a size of 17.
		 */
		LongArray longArray3 = new LongArray();
		longArray3.addAll(longArray2);
		Assert.assertArrayEquals(longArray2.toArray(), longArray3.toArray());
		longArray3.addAll(longArray1);
		Assert.assertArrayEquals(new long[] {1, 2, 3}, longArray3.toArray());
		longArray3.addAll(new long[] {4, 5, 6, 2, 8, 10, 1, 6, 2, 3, 30, 31, 25, 20});
		Assert.assertEquals(17, longArray3.size);
		Assert.assertArrayEquals(new long[] {1, 2, 3, 4, 5, 6, 2, 8, 10, 1, 6, 2, 3, 30, 31, 25, 20}, longArray3.toArray());

		/*
		 * Test of the method addAll(long[] array, int offset, int length) that adds the numbers contained between index offset to
		 * offset+length in array to longArray4.
		 */
		LongArray longArray4 = new LongArray();
		longArray4.addAll(new long[] {4, 5, 6, 2, 21, 45, 78}, 3, 3);
		Assert.assertArrayEquals(new long[] {2, 21, 45}, longArray4.toArray());
	}

	/** Test of the get() method */
	@Test
	public void getTest () {
		LongArray longArray = new LongArray();
		longArray.add(3, 4, 5, 1);
		Assert.assertEquals(3, longArray.get(0));
		try {
			longArray.get(9);
			Assert.fail();
		} catch (IndexOutOfBoundsException e) {
			// We should get here as we want to get an index that's out of bounds.
		}
	}

	/** Test of the set() method */
	@Test
	public void setTest () {
		LongArray longArray = new LongArray(new long[] {3, 4, 5, 7});
		longArray.set(1, 51);
		Assert.assertEquals(51, longArray.get(1));
		try {
			longArray.set(5, 8);
			Assert.fail();
		} catch (IndexOutOfBoundsException e) {
			// We should get here as we are trying to set the value of an index that's out of bounds.
		}
	}

	/** Test of the incr() method */
	@Test
	public void incrTest () {
		LongArray longArray = new LongArray(new long[] {3, 4, 5, 1, 56, 32});
		longArray.incr(3, 45);
		Assert.assertEquals(46, longArray.get(3));
		longArray.incr(3);
		Assert.assertArrayEquals(new long[] {6, 7, 8, 49, 59, 35}, longArray.toArray());
		try {
			longArray.incr(28, 4);
			Assert.fail();
		} catch (IndexOutOfBoundsException e) {
			// This should throw an exception as we are trying to increase at an index out of bounds
		}
	}

	/** Test of the mul() method */
	@Test
	public void mulTest () {
		LongArray longArray = new LongArray(new long[] {3, 4, 5, 1, 56, 32});
		longArray.mul(1, 3);
		Assert.assertEquals(12, longArray.get(1));
		longArray.mul(2);
		Assert.assertArrayEquals(new long[] {6, 24, 10, 2, 112, 64}, longArray.toArray());
		try {
			longArray.mul(17, 8);
			Assert.fail();
		} catch (IndexOutOfBoundsException e) {
			// This should throw an exception as we are trying to multiply at an index out of bounds
		}
	}

	/** Test of the insert() method */
	@Test
	public void insertTest () {
		// With an ordered array
		LongArray longArray1 = new LongArray();
		longArray1.addAll(new long[] {1, 3, 4, 5, 6});
		longArray1.insert(1, 2);
		Assert.assertArrayEquals(new long[] {1, 2, 3, 4, 5, 6}, longArray1.toArray());
		longArray1.insertRange(2, 3);
		Assert.assertArrayEquals(new long[] {1, 2, 3, 4, 5, 3, 4, 5, 6}, longArray1.toArray());
		try {
			longArray1.insertRange(400, 4);
			Assert.fail();
		} catch (IndexOutOfBoundsException e) {
			// This should throw an exception as we are trying to insert at an index out of bounds
		}

		// With an unordered array
		LongArray longArray2 = new LongArray(false, 16);
		longArray2.addAll(new long[] {1, 3, 4, 5, 6});
		longArray2.insert(1, 2);
		Assert.assertArrayEquals(new long[] {1, 2, 4, 5, 6, 3}, longArray2.toArray());
		try {
			longArray2.insert(2783, 3);
			Assert.fail();
		} catch (IndexOutOfBoundsException e) {
			// This should throw an exception as we are trying to insert at an index out of bounds
		}
	}

	/** Test of the swap() method */
	@Test
	public void swapTest () {
		LongArray longArray1 = new LongArray(new long[] {1, 3, 4, 5, 6});
		longArray1.swap(1, 4);
		Assert.assertArrayEquals(new long[] {1, 6, 4, 5, 3}, longArray1.toArray());
		try {
			longArray1.swap(100, 3);
			Assert.fail();
		} catch (IndexOutOfBoundsException e) {
			// This should throw an exception as we are trying to swap at an index out of bounds
		}
		try {
			longArray1.swap(3, 100);
			Assert.fail();
		} catch (IndexOutOfBoundsException e) {
			// This should throw an exception as we are trying to swap at an index out of bounds
		}
	}

	@Test
	public void containsTest () {
		LongArray longArray1 = new LongArray(new long[] {1, 3, 4, 5, 6});
		Assert.assertTrue(longArray1.contains(3));
		Assert.assertFalse(longArray1.contains(100));
	}

	/** Test of the indexOf() method */
	@Test
	public void indexOfTest () {
		LongArray longArray1 = new LongArray(new long[] {1, 3, 4, 5, 6, 6, 3, 9, 68000, 68000});
		Assert.assertEquals(-1, longArray1.indexOf(100));
		Assert.assertEquals(1, longArray1.indexOf(3));
		Assert.assertEquals(9, longArray1.lastIndexOf(68000));
		Assert.assertEquals(-1, longArray1.lastIndexOf(100));
	}

	/** Test of all remove methods (removeValue, removeIndex, removeRange, removeAll) */
	@Test
	public void removeTest () {
		// removeValue test
		LongArray longArray1 = LongArray.with(1, 3, 4, 5, 6, 6, 3, 9);
		Assert.assertTrue(longArray1.removeValue(3));
		Assert.assertArrayEquals(new long[] {1, 4, 5, 6, 6, 3, 9}, longArray1.toArray());
		Assert.assertEquals(7, longArray1.size);
		Assert.assertFalse(longArray1.removeValue(99));
		// removeIndex test
		Assert.assertEquals(4, longArray1.removeIndex(1));
		Assert.assertArrayEquals(new long[] {1, 5, 6, 6, 3, 9}, longArray1.toArray());
		Assert.assertEquals(6, longArray1.size);
		try {
			longArray1.removeIndex(56);
			Assert.fail();
		} catch (IndexOutOfBoundsException e) {
			// This should throw an exception as we are trying to remove at an index that is out of bounds
		}

		// removeRange test
		LongArray longArray2 = new LongArray();
		longArray2.addAll(new long[] {1, 10, 25, 2, 23, 345});
		longArray2.removeRange(2, 5);
		Assert.assertArrayEquals(new long[] {1, 10}, longArray2.toArray());
		try {
			longArray2.removeRange(3, 4);
			Assert.fail();
		} catch (IndexOutOfBoundsException e) {
			// This should throw an exception as we are trying to remove at a range that is out of bounds
		}
		try {
			longArray2.removeRange(1, 0);
			Assert.fail();
		} catch (IndexOutOfBoundsException e) {
			// This should throw an exception as the starting index is > than ending index
		}

		// removeAll test
		LongArray longArray3 = new LongArray();
		longArray3.addAll(new long[] {1, 10, 25, 35, 50, 40});
		LongArray toBeRemoved = new LongArray(new long[] {1, 25, 35});
		Assert.assertTrue(longArray3.removeAll(toBeRemoved));
		Assert.assertArrayEquals(new long[] {10, 50, 40}, longArray3.toArray());
		Assert.assertFalse(longArray3.removeAll(toBeRemoved));
		toBeRemoved = new LongArray(new long[] {10, 30, 22});
		Assert.assertTrue(longArray3.removeAll(toBeRemoved));
		Assert.assertArrayEquals(new long[] {50, 40}, longArray3.toArray());
	}

	/** Test of the pop(), peek() and first() methods */
	@Test
	public void popPeekFirstTest () {
		LongArray longArray = LongArray.with(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		LongArray emptyLongArray = new LongArray();
		Assert.assertEquals(1, longArray.first());
		Assert.assertEquals(10, longArray.peek());
		Assert.assertEquals(10, longArray.pop());
		Assert.assertArrayEquals(new long[] {1, 2, 3, 4, 5, 6, 7, 8, 9}, longArray.toArray());
		try {
			long first = emptyLongArray.first();
			Assert.fail();
		} catch (IllegalStateException e) {
			// This should throw an exception as we can't take the first item of an empty array
		}
		try {
			long last = emptyLongArray.pop();
			Assert.fail();
		} catch (IllegalStateException e) {
			// This should throw an exception as we can't take the last item of an empty array
		}
		try {
			long last = emptyLongArray.peek();
			Assert.fail();
		} catch (IllegalStateException e) {
			// This should throw an exception as we can't take the last item of an empty array
		}
	}

	/** test of the isEmpty() and notEmpty() methods */
	@Test
	public void emptyTest () {
		Assert.assertTrue((new LongArray()).isEmpty());
		Assert.assertFalse((new LongArray(new long[] {1})).isEmpty());
		Assert.assertFalse((new LongArray()).notEmpty());
		Assert.assertTrue((new LongArray(new long[] {1})).notEmpty());
	}

	/** Test of the clear() method */
	@Test
	public void clearTest () {
		LongArray longArray = new LongArray(new long[] {1});
		longArray.clear();
		Assert.assertTrue(longArray.isEmpty());
	}

	/** Test of the shrink() method */
	@Test
	public void shrinkTest () {
		LongArray longArray = new LongArray(); // This LongArray will have an "items" attribute of length 16 by default
		longArray.add(1, 2, 3);
		Assert.assertArrayEquals(new long[] {1, 2, 3}, longArray.shrink());
		Assert.assertEquals(3, longArray.items.length);
	}

	/** Test of the ensureCapacity method */
	@Test
	public void ensureCapacityTest () {
		LongArray longArray1 = new LongArray(
			new long[] {1, 2, 4, 6, 32, 53, 564, 53, 2, 1, 89, 0, 10, 389, 8, 392, 4, 27346, 2, 234, 12});
		LongArray longArray2 = new LongArray(new long[] {1, 2, 3});
		Assert.assertArrayEquals(new long[] {1, 2, 3, 0, 0, 0, 0, 0}, longArray2.ensureCapacity(2));
		Assert.assertArrayEquals(new long[] {1, 2, 4, 6, 32, 53, 564, 53, 2, 1, 89, 0, 10, 389, 8, 392, 4, 27346, 2, 234, 12, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, longArray1.ensureCapacity(18));
		try {
			longArray1.ensureCapacity(-6);
			Assert.fail();
		} catch (IllegalArgumentException e) {
			// This should throw an exception as it is not allowed to have a negative integer as ensureCapacity argument
		}
	}

	/** Test of the setSize() method */
	@Test
	public void setSizeTest () {
		LongArray longArray1 = new LongArray(
			new long[] {1, 2, 4, 6, 32, 53, 564, 53, 2, 1, 89, 90, 10, 389, 8, 392, 4, 27346, 2, 234, 12});
		longArray1.setSize(23);
		Assert.assertEquals(23, longArray1.size);
		longArray1.setSize(10);
		Assert.assertArrayEquals(new long[] {1, 2, 4, 6, 32, 53, 564, 53, 2, 1}, longArray1.toArray());
		LongArray longArray2 = new LongArray(new long[] {1, 2, 3});
		Assert.assertArrayEquals(new long[] {1, 2, 3, 0, 0, 0, 0, 0}, longArray2.setSize(5));
		try {
			longArray1.setSize(-3);
			Assert.fail();
		} catch (IllegalArgumentException e) {
			// This should throw an exception as it is not allowed to have a negative integer as setSize argument
		}
	}

	/** Test of resize() method */
	@Test
	public void resizeTest () {
		LongArray longArray1 = new LongArray(
			new long[] {1, 2, 4, 6, 32, 53, 564, 53, 2, 1, 89, 90, 10, 389, 8, 392, 4, 27346, 2, 234, 12});
		Assert.assertArrayEquals(
			new long[] {1, 2, 4, 6, 32, 53, 564, 53, 2, 1, 89, 90, 10, 389, 8, 392, 4, 27346, 2, 234, 12, 0, 0},
			longArray1.resize(23));
	}

	/** Test of sort() and reverse() methods */
	@Test
	public void sortAndReverseTest () {
		LongArray longArray1 = LongArray.with(1, 2, 4, 6, 32, 53, 564, 53, 2, 1, 89, 90, 10, 389, 8, 392, 4, 27346, 2, 234, 12);
		longArray1.sort();
		Assert.assertArrayEquals(new long[] {1, 1, 2, 2, 2, 4, 4, 6, 8, 10, 12, 32, 53, 53, 89, 90, 234, 389, 392, 564, 27346},
			longArray1.toArray());
		longArray1.reverse();
		Assert.assertArrayEquals(new long[] {27346, 564, 392, 389, 234, 90, 89, 53, 53, 32, 12, 10, 8, 6, 4, 4, 2, 2, 2, 1, 1},
			longArray1.toArray());
	}

	/** Test of equals() method */
	@Test
	public void equalsTest () {
		LongArray longArray1 = new LongArray();
		LongArray longArray2 = new LongArray();
		longArray1.add(1, 2);
		longArray2.add(1, 2);

		Assert.assertTrue(longArray1.equals(longArray2));

		// Verifying that an object of instance =/= LongArray cannot be equal
		ArrayMap<Integer, Integer> o = new ArrayMap<Integer, Integer>(); // Random object of a different class
		Assert.assertFalse(longArray1.equals(o));

		// An unordered array, even if the content is the same, cannot be equal to an ordered array
		LongArray longArray3 = new LongArray(false, 16);
		longArray3.add(1, 2);
		Assert.assertFalse(longArray1.equals(longArray3));

		// The capacity isn't something that makes two arrays not equal
		LongArray longArray4 = new LongArray(true, 12);
		longArray4.add(1, 2);
		Assert.assertTrue(longArray1.equals(longArray4));

		// Standard verification that two arrays with different content are not equal
		longArray1.add(3);
		Assert.assertFalse(longArray1.equals(longArray2));
	}
}
