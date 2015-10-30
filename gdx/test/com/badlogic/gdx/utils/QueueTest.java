
package com.badlogic.gdx.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class QueueTest {

	@Test
	public void resizableQueueTest () {
		final Queue<Integer> q = new Queue<Integer>(8, true);

		assertTrue("New queue is not empty!", q.size() == 0);

		for (int i = 0; i < 100; i++) {

			for (int j = 0; j < i; j++) {
				assertTrue("Failed to add element " + j + " (" + i + ")", q.push(j));
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
				assertTrue("Failed to add element " + j + " (" + i + ")", q.pushFront(j));
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
			assertTrue("Failed to add element " + i, q.push(i));
			assertTrue("Wrong size after " + i, q.size() == i + 1);
		}
		assertTrue("Not full when should be full", q.isFull());
		for (int i = 5; i < 10; i++) {
			assertFalse("Added element " + i, q.push(i));
			assertTrue("Wrong size after " + i, q.size() == 5);
			assertTrue("Not full when should be full (" + i + ")", q.isFull());
		}

		q.clear();
		assertTrue("Wrong size after clear", q.size() == 0);
		assertTrue("Not empty when should be empty", q.size() == 0);
		assertFalse("Full when should be empty", q.isFull());
	}

	@Test
	public void toStringTest () {
		{// Resizable
			Queue<Integer> q = new Queue<Integer>(1, true);
			assertTrue(q.toString().equals("Queue []"));
			q.push(4);
			assertTrue(q.toString().equals("Queue [4]"));
			q.push(5);
			q.push(6);
			q.push(7);
			assertTrue(q.toString().equals("Queue [4, 5, 6, 7]"));
		}

		{// Non-resizable
			Queue<Integer> nonResQ = new Queue<Integer>(4, false);
			nonResQ.push(1);
			nonResQ.push(2);
			nonResQ.push(3);
			nonResQ.push(4);
			nonResQ.push(5);
			assertTrue(nonResQ.toString().equals("Queue [1, 2, 3, 4]"));
		}
	}

}
