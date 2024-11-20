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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.utils;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.Comparator;

public class SortTest {

	private Sort sortInstance;

	@Before
	public void setUp () {
		// Initialize the Sort instance before each test
		sortInstance = Sort.instance();
	}

	@Test
	public void testSortArrayComparable () {
		// Test sorting an array of Comparable elements
		Integer[] array = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
		sortInstance.sort(array);
		assertArrayEquals(new Integer[] {1, 1, 2, 3, 3, 4, 5, 5, 5, 6, 9}, array);
	}

	@Test
	public void testSortArrayWithComparator () {
		// Test sorting an array using a custom Comparator
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
		// Test sorting a subarray using a custom Comparator
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
		// Test sorting a subarray of Comparable elements
		Integer[] array = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
		sortInstance.sort(array, 2, 7);
		assertArrayEquals(new Integer[] {3, 1, 1, 2, 4, 5, 9, 6, 5, 3, 5}, array);
	}

	@Test
	public void testSortArray () {
		// Test sorting an Array object of Comparable elements
		Array<Integer> array = new Array<>(new Integer[] {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5});
		sortInstance.sort(array);
		assertArrayEquals(new Integer[] {1, 1, 2, 3, 3, 4, 5, 5, 5, 6, 9}, array.items);
	}

	@Test
	public void testSortArrayWithComparatorClass () {
		// Test sorting an Array object using a custom Comparator
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
}
