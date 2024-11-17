/*******************************************************************************
 * Copyright 2024 Julien Abraul Guilherme.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.utils;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class BinaryHeapTest {

	private BinaryHeap minHeap;
	private BinaryHeap maxHeap;

	@Before
	public void setUp () {
		// Initialize minHeap and maxHeap before each test
		minHeap = new BinaryHeap();
		maxHeap = new BinaryHeap(16, true);
	}

	@Test
	public void testAddAndPeekMinHeap () {
		// Test adding nodes to minHeap and peeking the minimum value
		BinaryHeap.Node node1 = new BinaryHeap.Node(1);
		BinaryHeap.Node node2 = new BinaryHeap.Node(2);
		minHeap.add(node2);
		minHeap.add(node1);
		assertEquals(node1, minHeap.peek());
	}

	@Test
	public void testAddAndPeekMaxHeap () {
		// Test adding nodes to maxHeap and peeking the maximum value
		BinaryHeap.Node node1 = new BinaryHeap.Node(1);
		BinaryHeap.Node node2 = new BinaryHeap.Node(2);
		maxHeap.add(node1);
		maxHeap.add(node2);
		assertEquals(node2, maxHeap.peek());
	}

	@Test
	public void testPopMinHeap () {
		// Test popping the minimum value from minHeap
		BinaryHeap.Node node1 = new BinaryHeap.Node(1);
		BinaryHeap.Node node2 = new BinaryHeap.Node(2);
		minHeap.add(node2);
		minHeap.add(node1);
		assertEquals(node1, minHeap.pop());
		assertEquals(node2, minHeap.peek());
	}

	@Test
	public void testPopMaxHeap () {
		// Test popping the maximum value from maxHeap
		BinaryHeap.Node node1 = new BinaryHeap.Node(1);
		BinaryHeap.Node node2 = new BinaryHeap.Node(2);
		maxHeap.add(node1);
		maxHeap.add(node2);
		assertEquals(node2, maxHeap.pop());
		assertEquals(node1, maxHeap.peek());
	}

	@Test
	public void testContains () {
		// Test if minHeap contains specific nodes
		BinaryHeap.Node node1 = new BinaryHeap.Node(1);
		BinaryHeap.Node node2 = new BinaryHeap.Node(2);
		minHeap.add(node1);
		assertTrue(minHeap.contains(node1, true));
		assertFalse(minHeap.contains(node2, true));
	}

	@Test
	public void testClear () {
		// Test clearing all nodes from minHeap
		BinaryHeap.Node node1 = new BinaryHeap.Node(1);
		minHeap.add(node1);
		minHeap.clear();
		assertTrue(minHeap.isEmpty());
	}

	@Test
	public void testSetValue () {
		// Test setting a new value for a node in minHeap
		BinaryHeap.Node node1 = new BinaryHeap.Node(1);
		minHeap.add(node1);
		minHeap.setValue(node1, 0);
		assertEquals(0, node1.getValue(), 0.01);
		assertEquals(node1, minHeap.peek());
	}
}
