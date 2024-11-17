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
