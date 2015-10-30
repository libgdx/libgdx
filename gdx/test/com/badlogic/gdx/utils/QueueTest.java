
package com.badlogic.gdx.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class QueueTest {

	@Test
	public void resizableQueueTest () {
		final Queue<Integer> q = new Queue<Integer>(8, true);

		assertTrue("New queue is not empty!", q.isEmpty());

		for (int i = 0; i < 100; i++) {

			for (int j = 0; j < i; j++) {
				assertTrue("Failed to add element " + j + " (" + i + ")", q.add(j));
				final int size = q.size();
				assertTrue("Size should be " + (j + 1) + " but is " + size + " (" + i + ")", size == j + 1);
			}

			if (i != 0) {
				final Integer peek = q.peek();
				assertTrue("First thing is not zero but " + peek + " (" + i + ")", peek == 0);
			}

			for (int j = 0; j < i; j++) {
				final Integer pop = q.remove();
				assertTrue("Popped should be " + j + " but is " + pop + " (" + i + ")", pop == j);

				final int size = q.size();
				assertTrue("Size should be " + (i - 1 - j) + " but is " + size + " (" + i + ")", size == i - 1 - j);
			}

			assertTrue("Not empty after cycle " + i, q.isEmpty());
		}

		for (int i = 0; i < 56; i++) {
			q.add(42);
		}
		q.clear();
		assertTrue("Clear did not clear properly", q.isEmpty());
	}

	@Test
	public void nonResizableQueueTest () {
		final Queue<Integer> q = new Queue<Integer>(5, false);
		for (int i = 0; i < 5; i++) {
			assertTrue("Failed to add element " + i, q.add(i));
			assertTrue("Wrong size after " + i, q.size() == i + 1);
		}
		assertTrue("Not full when should be full", q.isFull());
		for (int i = 5; i < 10; i++) {
			assertFalse("Added element " + i, q.add(i));
			assertTrue("Wrong size after " + i, q.size() == 5);
			assertTrue("Not full when should be full (" + i + ")", q.isFull());
		}

		q.clear();
		assertTrue("Wrong size after clear", q.size() == 0);
		assertTrue("Not empty when should be empty", q.isEmpty());
		assertFalse("Full when should be empty", q.isFull());
	}

	@Test
	public void toStringTest () {
		{// Resizable
			Queue<Integer> q = new Queue<Integer>(1, true);
			assertTrue(q.toString().equals("Queue []"));
			q.add(4);
			assertTrue(q.toString().equals("Queue [4]"));
			q.add(5);
			q.add(6);
			q.add(7);
			assertTrue(q.toString().equals("Queue [4, 5, 6, 7]"));
		}

		{// Non-resizable
			Queue<Integer> nonResQ = new Queue<Integer>(4, false);
			nonResQ.add(1);
			nonResQ.add(2);
			nonResQ.add(3);
			nonResQ.add(4);
			nonResQ.add(5);
			assertTrue(nonResQ.toString().equals("Queue [1, 2, 3, 4]"));
		}
	}

}
