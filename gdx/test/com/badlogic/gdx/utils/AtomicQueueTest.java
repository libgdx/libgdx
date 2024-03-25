
package com.badlogic.gdx.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class AtomicQueueTest {

	@Test
	public void PutTest () {
		AtomicQueue<Integer> atomicQueue = new AtomicQueue<>(2);
		// We still have enough space to write another element.
		assertTrue(atomicQueue.put(1));

		// We do not have enough space to write another element as the read pointer is there.
		assertFalse(atomicQueue.put(2));
	}

	@Test
	public void PullTest () {
		AtomicQueue<Integer> atomicQueue = new AtomicQueue<>(3);

		// Add elements, note that the last element can not fit in the array.
		atomicQueue.put(1);
		atomicQueue.put(2);
		atomicQueue.put(3);

		// Poll the two elements in the array.
		assertEquals(1, (int)atomicQueue.poll());
		assertEquals(2, (int)atomicQueue.poll());

		// Last element did not fit, so it is null.
		assertNull(atomicQueue.poll());
	}

	@Test
	public void LoopAroundTest () {
		AtomicQueue<Integer> atomicQueue = new AtomicQueue<>(2);
		// We still have enough space to write another element.
		assertTrue(atomicQueue.put(1));

		// We do not have enough space to write another element as the read pointer is there.
		assertFalse(atomicQueue.put(2));

		// Retrieve the element.
		assertEquals(1, (int)atomicQueue.poll());

		// We can push an element again
		assertTrue(atomicQueue.put(2));
		assertEquals(2, (int)atomicQueue.poll());
	}
}
