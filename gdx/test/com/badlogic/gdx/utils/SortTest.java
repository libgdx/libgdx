/*******************************************************************************
 * Copyright 2024 see AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 ******************************************************************************/

package com.badlogic.gdx.utils;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Comparator;

/*
 * This test class verifies the correctness of the sort functions in the Sort class.
 * Each test ensures that the corresponding sort function correctly sorts arrays
 * and Array objects according to the specified criteria.
 */

public class SortTest {

	private Sort sortInstance;

	@Before
	public void setUp () {
		sortInstance = Sort.instance();
	}

	private class NullsFirstComparator implements Comparator<Integer> {
		@Override
		public int compare (Integer o1, Integer o2) {
			if (o1 == null && o2 == null) {
				return 0;
			} else if (o1 == null) {
				return -1;
			} else if (o2 == null) {
				return 1;
			} else {
				return o1.compareTo(o2);
			}
		}
	}

	@Test
	public void testSortArrayComparable () {
		Integer[] array = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
		sortInstance.sort(array);
		assertArrayEquals(new Integer[] {1, 1, 2, 3, 3, 4, 5, 5, 5, 6, 9}, array);
	}

	@Test
	public void testSortArrayWithComparator () {
		Integer[] array = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
		Comparator<Integer> comparator = new Comparator<Integer>() {
			@Override
			public int compare (Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		};
		sortInstance.sort(array, comparator);
		assertArrayEquals(new Integer[] {1, 1, 2, 3, 3, 4, 5, 5, 5, 6, 9}, array);
	}

	@Test
	public void testSortArrayWithComparatorAndRange () {
		Integer[] array = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
		Comparator<Integer> comparator = new Comparator<Integer>() {
			@Override
			public int compare (Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		};
		sortInstance.sort(array, comparator, 2, 7);
		assertArrayEquals(new Integer[] {3, 1, 1, 2, 4, 5, 9, 6, 5, 3, 5}, array);
	}

	@Test
	public void testSortArrayRange () {
		Integer[] array = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
		sortInstance.sort(array, 2, 7);
		assertArrayEquals(new Integer[] {3, 1, 1, 2, 4, 5, 9, 6, 5, 3, 5}, array);
	}

	@Test
	public void testSortArray () {
		Array<Integer> array = new Array<>(new Integer[] {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5});
		sortInstance.sort(array);
		assertArrayEquals(new Integer[] {1, 1, 2, 3, 3, 4, 5, 5, 5, 6, 9}, array.items);
	}

	@Test
	public void testSortArrayComparableWithPreExistingComparableTimSort () throws Exception {
		Field comparableTimSortField = Sort.class.getDeclaredField("comparableTimSort");
		comparableTimSortField.setAccessible(true);
		comparableTimSortField.set(sortInstance, new ComparableTimSort());

		Array<Integer> array = new Array<>(new Integer[] {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5});
		sortInstance.sort(array);
		assertArrayEquals(new Integer[] {1, 1, 2, 3, 3, 4, 5, 5, 5, 6, 9}, array.items);
	}

	@Test
	public void testSortArrayComparableWithNullComparableTimSort () throws Exception {
		Field comparableTimSortField = Sort.class.getDeclaredField("comparableTimSort");
		comparableTimSortField.setAccessible(true);
		comparableTimSortField.set(sortInstance, null);
		Integer[] array = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
		sortInstance.sort(array);
		assertArrayEquals(new Integer[] {1, 1, 2, 3, 3, 4, 5, 5, 5, 6, 9}, array);
	}

	@Test
	public void testSortArrayWithRangeWithNullComparableTimSort () throws Exception {
		Field comparableTimSortField = Sort.class.getDeclaredField("comparableTimSort");
		comparableTimSortField.setAccessible(true);
		comparableTimSortField.set(sortInstance, null);
		Integer[] array = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
		sortInstance.sort(array, 2, 7);
		assertArrayEquals(new Integer[] {3, 1, 1, 2, 4, 5, 9, 6, 5, 3, 5}, array);
	}

	@Test
	public void testSortArrayWithNullTimSort () throws Exception {
		Field timSortField = Sort.class.getDeclaredField("timSort");
		timSortField.setAccessible(true);
		timSortField.set(sortInstance, null);
		Integer[] array = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
		Comparator<Integer> comparator = new Comparator<Integer>() {
			@Override
			public int compare (Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		};
		sortInstance.sort(array, comparator);
		assertArrayEquals(new Integer[] {1, 1, 2, 3, 3, 4, 5, 5, 5, 6, 9}, array);
	}

	@Test
	public void testSortArrayWithNullTimSortArray () throws Exception {
		Field timSortField = Sort.class.getDeclaredField("timSort");
		timSortField.setAccessible(true);
		timSortField.set(sortInstance, null);
		Array<Integer> array = new Array<>(new Integer[] {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5});
		Comparator<Integer> comparator = new Comparator<Integer>() {
			@Override
			public int compare (Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		};
		sortInstance.sort(array, comparator);
		assertArrayEquals(new Integer[] {1, 1, 2, 3, 3, 4, 5, 5, 5, 6, 9}, array.items);
	}

	@Test
	public void testSortArrayWithComparatorAndRangeWithNullTimSort () throws Exception {
		Field timSortField = Sort.class.getDeclaredField("timSort");
		timSortField.setAccessible(true);
		timSortField.set(sortInstance, null);
		Integer[] array = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
		Comparator<Integer> comparator = new Comparator<Integer>() {
			@Override
			public int compare (Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		};
		sortInstance.sort(array, comparator, 2, 7);
		assertArrayEquals(new Integer[] {3, 1, 1, 2, 4, 5, 9, 6, 5, 3, 5}, array);
	}

	@Test
	public void testSortArrayWithCustomComparator () {
		Array<Integer> array = new Array<>(new Integer[] {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5});
		Comparator<Integer> customComparator = new Comparator<Integer>() {
			@Override
			public int compare (Integer o1, Integer o2) {
				return o2.compareTo(o1);
			}
		};
		sortInstance.sort(array, customComparator);
		assertArrayEquals(new Integer[] {9, 6, 5, 5, 5, 4, 3, 3, 2, 1, 1}, array.items);
	}

	@Test
	public void testSortEmptyArray () {
		Integer[] emptyArray = {};
		sortInstance.sort(emptyArray);
		assertArrayEquals(new Integer[] {}, emptyArray);
	}

	@Test
	public void testSortSingleElementArray () {
		Integer[] singleElementArray = {1};
		sortInstance.sort(singleElementArray);
		assertArrayEquals(new Integer[] {1}, singleElementArray);
	}

	@Test
	public void testSortArrayWithNulls () {
		Integer[] arrayWithNulls = {3, null, 1, 4, null, 2};
		Comparator<Integer> comparator = new NullsFirstComparator();
		sortInstance.sort(arrayWithNulls, comparator);
		assertArrayEquals(new Integer[] {null, null, 1, 2, 3, 4}, arrayWithNulls);
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void testSortArrayRangeWithInvalidIndices () {
		Integer[] array = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
		sortInstance.sort(array, -1, 15);
	}

	@Test
	public void testSortAlreadySortedArrayComparable () {
		Array<Integer> sortedArray = new Array<>(new Integer[] {1, 2, 3, 4, 5});
		sortInstance.sort(sortedArray);
		assertArrayEquals(new Integer[] {1, 2, 3, 4, 5}, sortedArray.items);
	}

	@Test
	public void testSortArrayWithEqualElements () {
		Array<Integer> equalElementsArray = new Array<>(new Integer[] {2, 2, 2, 2, 2});
		sortInstance.sort(equalElementsArray);
		assertArrayEquals(new Integer[] {2, 2, 2, 2, 2}, equalElementsArray.items);
	}

	@Test
	public void testSortSingleElementArrayComparable () {
		Array<Integer> singleElementArray = new Array<>(new Integer[] {1});
		sortInstance.sort(singleElementArray);
		assertArrayEquals(new Integer[] {1}, singleElementArray.items);
	}

	@Test
	public void testSortEmptyArrayComparable () {
		Array<Integer> emptyArray = new Array<>(new Integer[] {});
		sortInstance.sort(emptyArray);
		assertArrayEquals(new Integer[] {}, emptyArray.items);
	}
}
