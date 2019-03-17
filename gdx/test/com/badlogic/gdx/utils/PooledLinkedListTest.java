package com.badlogic.gdx.utils;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PooledLinkedListTest {

	private PooledLinkedList<Integer> list;

	@Before
	public void setUp () {
		list = new PooledLinkedList<Integer>(10);
		list.add(1);
		list.add(2);
		list.add(3);
	}

	@Test
	public void size () {
		assertEquals(3, list.size());
		list.iter();
		list.next();
		list.remove();
		assertEquals(2, list.size());
	}

	@Test
	public void iteration () {
		list.iter();
		assertEquals(Integer.valueOf(1), list.next());
		assertEquals(Integer.valueOf(2), list.next());
		assertEquals(Integer.valueOf(3), list.next());
		assertNull(list.next());
	}

	@Test
	public void reverseIteration () {
		list.iterReverse();
		assertEquals(Integer.valueOf(3), list.previous());
		assertEquals(Integer.valueOf(2), list.previous());
		assertEquals(Integer.valueOf(1), list.previous());
		assertNull(list.previous());
	}

	@Test
	public void remove () {
		list.iter();
		list.next(); // 1
		list.remove();
		list.next(); // 2
		list.next(); // 3
		list.remove();
		list.iter();
		assertEquals(Integer.valueOf(2), list.next());
		assertNull(list.next());
	}

	@Test
	public void removeLast () {
		list.iter();
		assertEquals(Integer.valueOf(1), list.next());
		list.removeLast();
		assertEquals(Integer.valueOf(2), list.next());
		assertNull(list.next());
	}

	@Test
	public void clear () {
		list.clear();
		assertEquals(0, list.size());
		list.iter();
		assertNull(list.next());
	}

}
