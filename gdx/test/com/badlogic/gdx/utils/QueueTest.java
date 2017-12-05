
package com.badlogic.gdx.utils;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

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
			} catch (IndexOutOfBoundsException ignore) {
				// Expected
			}
		}
	}

	@Test
	public void removeTest () {
		final Queue<Integer> q = new Queue<Integer>();

		// Test head < tail.
		for (int j = 0; j <= 6; j++)
			q.addLast(j);
		assertValues(q, 0, 1, 2, 3, 4, 5, 6);
		q.removeIndex(0);
		assertValues(q, 1, 2, 3, 4, 5, 6);
		q.removeIndex(1);
		assertValues(q, 1, 3, 4, 5, 6);
		q.removeIndex(4);
		assertValues(q, 1, 3, 4, 5);
		q.removeIndex(2);
		assertValues(q, 1, 3, 5);

		// Test head >= tail and index >= head.
		q.clear();
		for (int j = 2; j >= 0; j--)
			q.addFirst(j);
		for (int j = 3; j <= 6; j++)
			q.addLast(j);
		assertValues(q, 0, 1, 2, 3, 4, 5, 6);
		q.removeIndex(1);
		assertValues(q, 0, 2, 3, 4, 5, 6);
		q.removeIndex(0);
		assertValues(q, 2, 3, 4, 5, 6);

		// Test head >= tail and index < tail.
		q.clear();
		for (int j = 2; j >= 0; j--)
			q.addFirst(j);
		for (int j = 3; j <= 6; j++)
			q.addLast(j);
		assertValues(q, 0, 1, 2, 3, 4, 5, 6);
		q.removeIndex(5);
		assertValues(q, 0, 1, 2, 3, 4, 6);
		q.removeIndex(5);
		assertValues(q, 0, 1, 2, 3, 4);
	}

	@Test
	public void indexOfTest () {
		final Queue<Integer> q = new Queue<Integer>();

		// Test head < tail.
		for (int j = 0; j <= 6; j++)
			q.addLast(j);
		for (int j = 0; j <= 6; j++)
			assertEquals(q.indexOf(j, false), j);

		// Test head >= tail.
		q.clear();
		for (int j = 2; j >= 0; j--)
			q.addFirst(j);
		for (int j = 3; j <= 6; j++)
			q.addLast(j);
		for (int j = 0; j <= 6; j++)
			assertEquals(q.indexOf(j, false), j);
	}

	@Test
	public void iteratorTest () {
		final Queue<Integer> q = new Queue<Integer>();

		// Test head < tail.
		for (int j = 0; j <= 6; j++)
			q.addLast(j);
		Iterator<Integer> iter = q.iterator();
		for (int j = 0; j <= 6; j++)
			assertEquals(iter.next().intValue(), j);
		iter = q.iterator();
		iter.next();
		iter.remove();
		assertValues(q, 1, 2, 3, 4, 5, 6);
		iter.next();
		iter.remove();
		assertValues(q, 2, 3, 4, 5, 6);
		iter.next();
		iter.next();
		iter.remove();
		assertValues(q, 2, 4, 5, 6);
		iter.next();
		iter.next();
		iter.next();
		iter.remove();
		assertValues(q, 2, 4, 5);

		// Test head >= tail.
		q.clear();
		for (int j = 2; j >= 0; j--)
			q.addFirst(j);
		for (int j = 3; j <= 6; j++)
			q.addLast(j);
		iter = q.iterator();
		for (int j = 0; j <= 6; j++)
			assertEquals(iter.next().intValue(), j);
		iter = q.iterator();
		iter.next();
		iter.remove();
		assertValues(q, 1, 2, 3, 4, 5, 6);
		iter.next();
		iter.remove();
		assertValues(q, 2, 3, 4, 5, 6);
		iter.next();
		iter.next();
		iter.remove();
		assertValues(q, 2, 4, 5, 6);
		iter.next();
		iter.next();
		iter.next();
		iter.remove();
		assertValues(q, 2, 4, 5);
	}

	@Test
	public void iteratorRemoveEdgeCaseTest() {//See #4300
		Queue<Integer> queue = new Queue<Integer>();

		//Simulate normal usage
		for(int i = 0; i < 100; i++) {
			queue.addLast(i);
			if(i > 50)
				queue.removeFirst();
		}

		Iterator<Integer> it = queue.iterator();
		while(it.hasNext()) {
			it.next();
			it.remove();
		}

		queue.addLast(1337);

		Integer i = queue.first();
		assertEquals(1337, (int)i);
	}

	@Test
	public void toStringTest () {
		Queue<Integer> q = new Queue<Integer>(1);
		assertTrue(q.toString().equals("[]"));
		q.addLast(4);
		assertTrue(q.toString().equals("[4]"));
		q.addLast(5);
		q.addLast(6);
		q.addLast(7);
		assertTrue(q.toString().equals("[4, 5, 6, 7]"));
	}

	@Test
	public void hashEqualsTest () {
		Queue<Integer> q1 = new Queue<Integer>();
		Queue<Integer> q2 = new Queue<Integer>();

		assertEqualsAndHash(q1, q2);
		q1.addFirst(1);
		assertNotEquals(q1, q2);
		q2.addFirst(1);
		assertEqualsAndHash(q1, q2);

		q1.clear();
		q1.addLast(1);
		q1.addLast(2);
		q2.addLast(2);
		assertEqualsAndHash(q1, q2);

		for (int i = 0; i < 100; i++) {
			q1.addLast(i);
			q1.addLast(i);
			q1.removeFirst();

			assertNotEquals(q1, q2);

			q2.addLast(i);
			q2.addLast(i);
			q2.removeFirst();

			assertEqualsAndHash(q1, q2);
		}
	}

	private void assertEqualsAndHash (Queue<?> q1, Queue<?> q2) {
		assertEquals(q1, q2);
		assertEquals("Hash codes are not equal", q1.hashCode(), q2.hashCode());
	}

	private void assertValues (Queue<Integer> q, Integer... values) {
		for (int i = 0, n = values.length; i < n; i++) {
			Assert.assertEquals(values[i], q.get(i));
		}
	}
}
