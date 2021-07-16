package com.badlogic.gdx.utils;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class QueueTest {

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void initializeWithType () {
		Queue<String> queue = new Queue<String>(1, String.class);
		queue.addFirst("a");
		assertEquals("a", queue.get(0));
	}

	@Test
	public void addFirstAndLastTest () {
		Queue<Integer> queue = new Queue<Integer>();
		queue.addFirst(1);
		queue.addLast(2);
		queue.addFirst(3);
		queue.addLast(4);

		assertEquals(0, queue.indexOf(3, true));
		assertEquals(1, queue.indexOf(1, true));
		assertEquals(2, queue.indexOf(2, true));
		assertEquals(3, queue.indexOf(4, true));
	}

	@Test
	public void removeLastTest () {
		Queue<Integer> queue = new Queue<Integer>();
		queue.addLast(1);
		queue.addLast(2);
		queue.addLast(3);
		queue.addLast(4);

		assertEquals(4, queue.size);
		assertEquals(3, queue.indexOf(4, true));
		assertEquals(4, (Object)queue.removeLast());

		assertEquals(3, queue.size);
		assertEquals(2, queue.indexOf(3, true));
		assertEquals(3, (Object)queue.removeLast());

		assertEquals(2, queue.size);
		assertEquals(1, queue.indexOf(2, true));
		assertEquals(2, (Object)queue.removeLast());

		assertEquals(1, queue.size);
		assertEquals(0, queue.indexOf(1, true));
		assertEquals(1, (Object)queue.removeLast());

		assertEquals(0, queue.size);
	}

	@Test
	public void removeFirstTest () {
		Queue<Integer> queue = new Queue<Integer>();
		queue.addLast(1);
		queue.addLast(2);
		queue.addLast(3);
		queue.addLast(4);

		assertEquals(4, queue.size);
		assertEquals(0, queue.indexOf(1, true));
		assertEquals(1, (Object)queue.removeFirst());

		assertEquals(3, queue.size);
		assertEquals(0, queue.indexOf(2, true));
		assertEquals(2, (Object)queue.removeFirst());

		assertEquals(2, queue.size);
		assertEquals(0, queue.indexOf(3, true));
		assertEquals(3, (Object)queue.removeFirst());

		assertEquals(1, queue.size);
		assertEquals(0, queue.indexOf(4, true));
		assertEquals(4, (Object)queue.removeFirst());

		assertEquals(0, queue.size);
	}

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

	/**
	 * Same as resizableQueueTest, but in reverse
	 */
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
	public void iteratorRemoveEdgeCaseTest () {//See #4300
		Queue<Integer> queue = new Queue<Integer>();

		//Simulate normal usage
		for (int i = 0; i < 100; i++) {
			queue.addLast(i);
			if (i > 50)
				queue.removeFirst();
		}

		Iterator<Integer> it = queue.iterator();
		while (it.hasNext()) {
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
	public void toStringTestWithSeperator () {
		String separator = ";";
		Queue<Integer> q = new Queue<Integer>(1);
		assertEquals(q.toString(separator), "");
		q.addLast(4);
		assertEquals(q.toString(separator), "4");
		q.addLast(5);
		q.addLast(6);
		q.addLast(7);
		assertEquals(q.toString(separator), "4;5;6;7");
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

	@Test
	public void emptyQueueLast () {
		Queue<Integer> queue = new Queue<>();

		// Queue is empty and an error should be thrown.
		exception.expect(NoSuchElementException.class);
		queue.last();
	}

	@Test
	public void emptyQueueRemoveFirst () {
		Queue<Integer> queue = new Queue<>();

		// Queue is empty and an error should be thrown.
		exception.expect(NoSuchElementException.class);
		queue.removeFirst();
	}

	@Test
	public void emptyQueueRemoveLast () {
		Queue<Integer> queue = new Queue<>();

		// Queue is empty and an error should be thrown.
		exception.expect(NoSuchElementException.class);
		queue.removeLast();
	}

	@Test
	public void emptyQueueFirst () {
		Queue<Integer> queue = new Queue<>();

		// Queue is empty and an error should be thrown.
		exception.expect(NoSuchElementException.class);
		queue.first();
	}

	@Test
	public void indexOfNotFound () {
		Queue<Integer> queue = new Queue<>();

		// Check an existing element
		queue.addFirst(1);
		assertEquals(0, queue.indexOf(1, true));

		// Check a non existing element.
		assertEquals(-1, queue.indexOf(42, true));
	}

	@Test
	public void removeValueFound () {
		Queue<Integer> queue = new Queue<>();

		// Check an existing element
		queue.addFirst(1);
		assertEquals(0, queue.indexOf(1, true));

		// Remove the element and check again for existence.
		assertTrue(queue.removeValue(1, true));
		assertEquals(-1, queue.indexOf(1, true));
	}

	@Test
	public void removeNotFound () {
		Queue<Integer> queue = new Queue<>();

		// Check an existing element
		assertEquals(-1, queue.indexOf(1, true));

		// Remove the element and check again for existence.
		assertFalse(queue.removeValue(1, true));
	}

	@Test
	public void ensureCapacityFit () {
		// Create the queue.
		Queue<String> queue = new Queue<String>(2, String.class);

		// Enough space, does not change.
		assertEquals(2, queue.values.length);
		queue.ensureCapacity(2);
		assertEquals(2, queue.values.length);
	}

	@Test
	public void ensureCapacityNotFit () {
		// Create the queue.
		Queue<String> queue = new Queue<>(2, String.class);

		// Add an element.
		queue.addFirst("first");

		// Enough space, does not change.
		assertEquals(2, queue.values.length);
		queue.ensureCapacity(2);
		assertEquals(3, queue.values.length);
	}

	@Test
	public void isEmpty () {
		// Create the queue
		Queue<String> queue = new Queue<>();

		assertTrue(queue.isEmpty());
		queue.addFirst("element");
		assertFalse(queue.isEmpty());
	}

	@Test
	public void notEmpty () {
		// Create the queue
		Queue<String> queue = new Queue<>();

		assertFalse(queue.notEmpty());
		queue.addFirst("element");
		assertTrue(queue.notEmpty());
	}

	@Test
	public void equalsIdentitySameObject () {
		Queue<String> queue = new Queue<>();
		assertTrue(queue.equalsIdentity(queue));
	}

	@Test
	public void equalsIdentityDifferentObject () {
		Queue<String> queue = new Queue<>();

		assertFalse(queue.equalsIdentity(null));
		assertFalse(queue.equalsIdentity("different"));
	}

	@Test
	public void equalsIdentityDifferentQueueSameObjects () {
		// Initialize queues.
		Queue<String> queue1 = new Queue<>();
		Queue<String> queue2 = new Queue<>();

		// Check empty equality
		assertTrue(queue1.equalsIdentity(queue2));
		assertTrue(queue2.equalsIdentity(queue1));

		// Check inequality.
		queue1.addLast("1");
		assertFalse(queue1.equalsIdentity(queue2));
		assertFalse(queue2.equalsIdentity(queue1));

		// Check equality.
		queue2.addLast("1");
		assertTrue(queue1.equalsIdentity(queue2));
		assertTrue(queue2.equalsIdentity(queue1));
	}

	@Test
	public void initializeQueueIterator () {
		// Initialize queue and iterator.
		Queue<String> queue = new Queue<>();
		queue.addLast("remove");
		Queue.QueueIterator<String> iterator = new Queue.QueueIterator<>(queue);

		// Allow remove is set to true, thus test a remove case.
		assertEquals(1, queue.size);
		iterator.next();
		iterator.remove();
		assertEquals(0, queue.size);
	}

	@Test
	public void queueIteratorInvalidHasNext () {
		// Initialize queue and iterator.
		Queue<Integer> queue = new Queue<>();
		Queue.QueueIterator<Integer> iterator = new Queue.QueueIterator<>(queue);
		iterator.valid = false;

		// GdxRuntimeException will be thrown by check hasNext of invalid iterator.
		exception.expect(GdxRuntimeException.class);
		boolean hasNext = iterator.hasNext();
	}

	@Test
	public void queueIteratorInvalidNext () {
		// Initialize queue and iterator.
		Queue<String> queue = new Queue<>();
		Queue.QueueIterator<String> iterator = new Queue.QueueIterator<>(queue);
		iterator.valid = false;
		queue.addLast("1");

		// GdxRuntimeException will be thrown by check hasNext of invalid iterator.
		exception.expect(GdxRuntimeException.class);
		String next = iterator.next();
	}

	@Test
	public void queueIteratorReset () {
		// Initialize queue and iterator.
		Queue<String> queue = new Queue<>();
		queue.addLast("element");
		Queue.QueueIterator<String> iterator = new Queue.QueueIterator<>(queue);

		assertEquals(0, iterator.index);
		assertEquals("element", iterator.next());
		assertEquals(1, iterator.index);
		iterator.reset();
		assertEquals(0, iterator.index);
	}

	@Test
	public void queueIteratorGetIterator () {
		// Initialize queue and iterator.
		Queue<String> queue = new Queue<>();
		Queue.QueueIterator<String> iterator = new Queue.QueueIterator<>(queue);

		assertEquals(iterator, iterator.iterator());
	}

	@Test
	public void resizeLargerTailThanHead () {
		Queue<String> queue = new Queue<>(2);
		queue.addLast("1");
		queue.addLast("2");
		queue.head = 1;
		queue.tail = 2;

		assertEquals("1", queue.get(1));
		queue.resize(1);
		assertEquals("2", queue.get(0));
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
