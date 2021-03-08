package com.badlogic.gdx.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class AtomicQueueTest {

    @Test
    public void PutTest () {
        AtomicQueue<Integer> atomicQueue = new AtomicQueue<>(2);
        // We still have enough space to write another element.
        assertTrue(atomicQueue.put(1));

        // We do not have enougt space to write another element.
        assertFalse(atomicQueue.put(2));
    }

    @Test
    public void PullTest () {
        AtomicQueue<Integer> atomicQueue = new AtomicQueue<>(3);

        // Add elements, not that the last element can not fit on the array.
        atomicQueue.put(1);
        atomicQueue.put(2);
        atomicQueue.put(3);

        // Poll the two elements on the array.
        assertEquals(1, (int) atomicQueue.poll());
        assertEquals(2, (int) atomicQueue.poll());

        // Last element did not fit, so it is null.
        assertNull(atomicQueue.poll());
    }
}
