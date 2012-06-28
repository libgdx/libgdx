/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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

/** @author Nathan Sweet */
public class BinaryHeap<T> {
	public int size = 0;

	private Node<T>[] nodes;
	private final boolean isMaxHeap;

	public BinaryHeap () {
		this(16, false);
	}

	public BinaryHeap (int capacity, boolean isMaxHeap) {
		this.isMaxHeap = isMaxHeap;
		nodes = new Node[capacity];
	}

	public Node add (Node node) {
		// Expand if necessary.
		if (size == nodes.length) {
			Node[] newNodes = new Node[size << 1];
			System.arraycopy(nodes, 0, newNodes, 0, size);
			nodes = newNodes;
		}
		// Insert at end and bubble up.
		node.index = size;
		nodes[size] = node;
		up(size++);
		return node;
	}

	public Node pop () {
		Node[] nodes = this.nodes;
		Node popped = nodes[0];
		nodes[0] = nodes[--size];
		nodes[size] = null;
		if (size > 0) down(0);
		return popped;
	}

	public void setValue (Node node, float value) {
		float oldValue = node.value;
		node.value = value;
		if (value < oldValue ^ isMaxHeap)
			up(node.index);
		else
			down(node.index);
	}

	private void up (int index) {
		Node[] nodes = this.nodes;
		Node node = nodes[index];
		float value = node.value;
		while (index > 0) {
			int parentIndex = (index - 1) >> 1;
			Node parent = nodes[parentIndex];
			if (value < parent.value ^ isMaxHeap) {
				nodes[index] = parent;
				parent.index = index;
				index = parentIndex;
			} else
				break;
		}
		nodes[index] = node;
		node.index = index;
	}

	private void down (int index) {
		Node[] nodes = this.nodes;
		int size = this.size;

		Node node = nodes[index];
		float value = node.value;

		while (true) {
			int leftIndex = 1 + (index << 1);
			if (leftIndex >= size) break;
			int rightIndex = leftIndex + 1;

			// Always have a left child.
			Node leftNode = nodes[leftIndex];
			float leftValue = leftNode.value;

			// May have a right child.
			Node rightNode;
			float rightValue;
			if (rightIndex >= size) {
				rightNode = null;
				rightValue = isMaxHeap ? Float.MIN_VALUE : Float.MAX_VALUE;
			} else {
				rightNode = nodes[rightIndex];
				rightValue = rightNode.value;
			}

			// The smallest of the three values is the parent.
			if (leftValue < rightValue ^ isMaxHeap) {
				if (leftValue == value || (leftValue > value ^ isMaxHeap)) break;
				nodes[index] = leftNode;
				leftNode.index = index;
				index = leftIndex;
			} else {
				if (rightValue == value || (rightValue > value ^ isMaxHeap)) break;
				nodes[index] = rightNode;
				rightNode.index = index;
				index = rightIndex;
			}
		}

		nodes[index] = node;
		node.index = index;
	}

	public String toString () {
		if (size == 0) return "[]";
		Object[] nodes = this.nodes;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('[');
		buffer.append(nodes[0]);
		for (int i = 1; i < size; i++) {
			buffer.append(", ");
			buffer.append(nodes[i]);
		}
		buffer.append(']');
		return buffer.toString();
	}

	/** @author Nathan Sweet */
	static public class Node<T> {
		float value;
		int index;

		public Node (float value) {
			this.value = value;
		}
	}
}