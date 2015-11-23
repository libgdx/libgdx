
package com.badlogic.gdx.utils;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class QueueTest {

	@Test
	public void resizableQueueTest () {
		final Queue<Integer> q = new Queue<Integer>(8);

		assertTrue("New queue is not empty!", q.size == 0);

		for (int i = 0; i < 100; i++) {

			for (int j = 0; j < i; j++) {
				try {
					q.addLast(j);
				} catch (IllegalStateException e) {
					fail("Failed to add element " + j + " (" + i + ")");
				}
				final Integer peeked = q.last();
				assertTrue("peekLast shows " + peeked + ", should be " + j + " (" + i + ")", peeked.equals(j));
				final int size = q.size;
				assertTrue("Size should be " + (j + 1) + " but is " + size + " (" + i + ")", size == j + 1);
			}

			if (i != 0) {
				final Integer peek = q.first();
				assertTrue("First thing is not zero but " + peek + " (" + i + ")", peek == 0);
			}

			for (int j = 0; j < i; j++) {
				final Integer pop = q.removeFirst();
				assertTrue("Popped should be " + j + " but is " + pop + " (" + i + ")", pop == j);

				final int size = q.size;
				assertTrue("Size should be " + (i - 1 - j) + " but is " + size + " (" + i + ")", size == i - 1 - j);
			}

			assertTrue("Not empty after cycle " + i, q.size == 0);
		}

		for (int i = 0; i < 56; i++) {
			q.addLast(42);
		}
		q.clear();
		assertTrue("Clear did not clear properly", q.size == 0);
	}

	/** Same as resizableQueueTest, but in reverse */
	@Test
	public void resizableDequeTest () {
		final Queue<Integer> q = new Queue<Integer>(8);

		assertTrue("New deque is not empty!", q.size == 0);

		for (int i = 0; i < 100; i++) {

			for (int j = 0; j < i; j++) {
				try {
					q.addFirst(j);
				} catch (IllegalStateException e) {
					fail("Failed to add element " + j + " (" + i + ")");
				}
				final Integer peeked = q.first();
				assertTrue("peek shows " + peeked + ", should be " + j + " (" + i + ")", peeked.equals(j));
				final int size = q.size;
				assertTrue("Size should be " + (j + 1) + " but is " + size + " (" + i + ")", size == j + 1);
			}

			if (i != 0) {
				final Integer peek = q.last();
				assertTrue("Last thing is not zero but " + peek + " (" + i + ")", peek == 0);
			}

			for (int j = 0; j < i; j++) {
				final Integer pop = q.removeLast();
				assertTrue("Popped should be " + j + " but is " + pop + " (" + i + ")", pop == j);

				final int size = q.size;
				assertTrue("Size should be " + (i - 1 - j) + " but is " + size + " (" + i + ")", size == i - 1 - j);
			}

			assertTrue("Not empty after cycle " + i, q.size == 0);
		}

		for (int i = 0; i < 56; i++) {
			q.addFirst(42);
		}
		q.clear();
		assertTrue("Clear did not clear properly", q.size == 0);
	}

	@Test
	public void getTest () {
		final Queue<Integer> q = new Queue<Integer>(7);
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 4; j++) {
				q.addLast(j);
			}
			assertEquals("get(0) is not equal to peek (" + i + ")", q.get(0), q.first());
			assertEquals("get(size-1) is not equal to peekLast (" + i + ")", q.get(q.size - 1), q.last());
			for (int j = 0; j < 4; j++) {
				assertTrue(q.get(j) == j);
			}
			for (int j = 0; j < 4 - 1; j++) {
				q.removeFirst();
				assertEquals("get(0) is not equal to peek (" + i + ")", q.get(0), q.first());
			}
			q.removeFirst();
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
			Queue<Integer> q = new Queue<Integer>(1);
			assertTrue(q.toString().equals("[]"));
			q.addLast(4);
			assertTrue(q.toString().equals("[4]"));
			q.addLast(5);
			q.addLast(6);
			q.addLast(7);
			assertTrue(q.toString().equals("[4, 5, 6, 7]"));
		}
	}

}
