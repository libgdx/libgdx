
package com.badlogic.gdx.utils;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class QueueTest {

	@Test
	public void resizableQueueTest () {
		final Queue<Integer> q = new Queue<Integer>(8, true);

		assertTrue("New queue is not empty!", q.size() == 0);

		for (int i = 0; i < 100; i++) {

			for (int j = 0; j < i; j++) {
				try {
					q.push(j);
				} catch (IllegalStateException e) {
					fail("Failed to add element " + j + " (" + i + ")");
				}
				final Integer peeked = q.peekLast();
				assertTrue("peekLast shows " + peeked + ", should be " + j + " (" + i + ")", peeked.equals(j));
				final int size = q.size();
				assertTrue("Size should be " + (j + 1) + " but is " + size + " (" + i + ")", size == j + 1);
			}

			if (i != 0) {
				final Integer peek = q.peek();
				assertTrue("First thing is not zero but " + peek + " (" + i + ")", peek == 0);
			}

			for (int j = 0; j < i; j++) {
				final Integer pop = q.pop();
				assertTrue("Popped should be " + j + " but is " + pop + " (" + i + ")", pop == j);

				final int size = q.size();
				assertTrue("Size should be " + (i - 1 - j) + " but is " + size + " (" + i + ")", size == i - 1 - j);
			}

			assertTrue("Not empty after cycle " + i, q.size() == 0);
		}

		for (int i = 0; i < 56; i++) {
			q.push(42);
		}
		q.clear();
		assertTrue("Clear did not clear properly", q.size() == 0);
	}

	/** Same as resizableQueueTest, but in reverse */
	@Test
	public void resizableDequeTest () {
		final Queue<Integer> q = new Queue<Integer>(8, true);

		assertTrue("New deque is not empty!", q.size() == 0);

		for (int i = 0; i < 100; i++) {

			for (int j = 0; j < i; j++) {
				try {
					q.pushFront(j);
				} catch (IllegalStateException e) {
					fail("Failed to add element " + j + " (" + i + ")");
				}
				final Integer peeked = q.peek();
				assertTrue("peek shows " + peeked + ", should be " + j + " (" + i + ")", peeked.equals(j));
				final int size = q.size();
				assertTrue("Size should be " + (j + 1) + " but is " + size + " (" + i + ")", size == j + 1);
			}

			if (i != 0) {
				final Integer peek = q.peekLast();
				assertTrue("Last thing is not zero but " + peek + " (" + i + ")", peek == 0);
			}

			for (int j = 0; j < i; j++) {
				final Integer pop = q.popLast();
				assertTrue("Popped should be " + j + " but is " + pop + " (" + i + ")", pop == j);

				final int size = q.size();
				assertTrue("Size should be " + (i - 1 - j) + " but is " + size + " (" + i + ")", size == i - 1 - j);
			}

			assertTrue("Not empty after cycle " + i, q.size() == 0);
		}

		for (int i = 0; i < 56; i++) {
			q.pushFront(42);
		}
		q.clear();
		assertTrue("Clear did not clear properly", q.size() == 0);
	}

	@Test
	public void nonResizableQueueTest () {
		final Queue<Integer> q = new Queue<Integer>(5, false);
		for (int i = 0; i < 5; i++) {
			try {
				q.push(i);
			} catch (IllegalStateException ise) {
				fail("Failed to add element " + i);
			}
			assertTrue("Wrong size after " + i, q.size() == i + 1);
		}
		assertTrue("Not full when should be full", q.isFull());
		for (int i = 5; i < 10; i++) {
			try {
				q.push(i);
				fail("Added element " + i);
			} catch (IllegalStateException e) {
				// This is expected
			}
			assertTrue("Wrong size after " + i, q.size() == 5);
			assertTrue("Not full when should be full (" + i + ")", q.isFull());
		}

		q.clear();
		assertTrue("Wrong size after clear", q.size() == 0);
		assertTrue("Not empty when should be empty", q.size() == 0);
		assertFalse("Full when should be empty", q.isFull());
	}

	@Test
	public void getTest () {
		final Queue<Integer> q = new Queue<Integer>(7, true);
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 4; j++) {
				q.push(j);
			}
			assertEquals("get(0) is not equal to peek (" + i + ")", q.get(0), q.peek());
			assertEquals("get(size-1) is not equal to peekLast (" + i + ")", q.get(q.size - 1), q.peekLast());
			for (int j = 0; j < 4; j++) {
				assertTrue(q.get(j) == j);
			}
			for (int j = 0; j < 4 - 1; j++) {
				q.pop();
				assertEquals("get(0) is not equal to peek (" + i + ")", q.get(0), q.peek());
			}
			q.pop();
			assert q.size == 0; // Failing this means broken test
			try {
				q.get(0);
				fail("get() on empty queue did not throw");
			} catch (NoSuchElementException ignore) {
				// Expected
			}
		}
	}

	@Test
	public void toStringTest () {
		{// Resizable
			Queue<Integer> q = new Queue<Integer>(1, true);
			assertTrue(q.toString().equals("[]"));
			q.push(4);
			assertTrue(q.toString().equals("[4]"));
			q.push(5);
			q.push(6);
			q.push(7);
			assertTrue(q.toString().equals("[4, 5, 6, 7]"));
		}

		{// Non-resizable
			Queue<Integer> nonResQ = new Queue<Integer>(4, false);
			nonResQ.push(1);
			nonResQ.push(2);
			nonResQ.push(3);
			nonResQ.push(4);
			try {
				nonResQ.push(5);
			} catch (IllegalStateException e) {
				// Expected
			}
			assertTrue(nonResQ.toString().equals("[1, 2, 3, 4]"));
		}
	}

}
