
package com.badlogic.gdx.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class QueueTest {

	@Test
	public void queueStressTest () {
		Queue<Integer> q = new Queue<Integer>(8);

		assertTrue("New queue is not empty!", q.isEmpty());

		for (int i = 0; i < 100; i++) {

			for (int j = 0; j < i; j++) {
				q.add(j);
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
    public void toStringTest(){
        Queue<Integer> q = new Queue<Integer>();
        assertTrue(q.toString().equals("Queue []"));
        q.add(4);
        assertTrue(q.toString().equals("Queue [4]"));
        q.add(5);
        q.add(6);
        q.add(7);
        assertTrue(q.toString().equals("Queue [4, 5, 6, 7]"));
    }

}
