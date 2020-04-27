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

/** A sorted double linked list which uses ints for indexing
 * 
 * @param <E> */
public class SortedIntList<E> implements Iterable<SortedIntList.Node<E>> {
	private NodePool<E> nodePool = new NodePool<E>(); // avoid allocating nodes
	private Iterator iterator;
	int size = 0;

	Node<E> first;

	/** Creates an ascending list */
	public SortedIntList () {
	}

	/** Inserts an element into the list at the given index
	 * 
	 * @param index Index of the element
	 * @param value Element to insert
	 * @return Element replaced by newly inserted element, null if nothing was replaced */
	@Null
	public E insert (int index, E value) {
		if (first != null) {
			Node<E> c = first;
			// iterate to the right until we can't move any further because the next number is bigger than index
			while (c.n != null && c.n.index <= index) {
				c = c.n;
			}
			// add one to the right
			if (index > c.index) {
				c.n = nodePool.obtain(c, c.n, value, index);
				if (c.n.n != null) {
					c.n.n.p = c.n;
				}
				size++;
			}
			// the new element is smaller than every other element
			else if (index < c.index) {
				Node<E> newFirst = nodePool.obtain(null, first, value, index);
				first.p = newFirst;
				first = newFirst;
				size++;
			}
			// that element already exists so swap the value
			else {
				c.value = value;
			}
		} else {
			first = nodePool.obtain(null, null, value, index);
			size++;
		}
		return null;
	}

	/** Retrieves an element at a given index
	 * 
	 * @param index Index of the element to retrieve
	 * @return Matching element, null otherwise */
	public E get (int index) {
		E match = null;
		if (first != null) {
			Node<E> c = first;
			while (c.n != null && c.index < index) {
				c = c.n;
			}
			if (c.index == index) {
				match = c.value;
			}
		}
		return match;
	}

	/** Clears list */
	public void clear () {
		for (; first != null; first = first.n) {
			nodePool.free(first);
		}
		size = 0;
	}

	/** @return size of list equal to elements contained in it */
	public int size () {
		return size;
	}

	/** Returns true if the list has one or more items. */
	public boolean notEmpty () {
		return size > 0;
	}

	/** Returns true if the list is empty. */
	public boolean isEmpty () {
		return size == 0;
	}

	/** Returns an iterator to traverse the list.
	 * <p>
	 * If {@link Collections#allocateIterators} is false, the same iterator instance is returned each time this method is called.
	 * Use the {@link Iterator} constructor for nested or multithreaded iteration. */
	public java.util.Iterator<Node<E>> iterator () {
		if (Collections.allocateIterators) return new Iterator();
		if (iterator == null) iterator = new Iterator();
		return iterator.reset();
	}

	public class Iterator implements java.util.Iterator<Node<E>> {
		private Node<E> position;
		private Node<E> previousPosition;

		@Override
		public boolean hasNext () {
			return position != null;
		}

		@Override
		public Node<E> next () {
			previousPosition = position;
			position = position.n;
			return previousPosition;
		}

		@Override
		public void remove () {
			// the contract specifies to remove the last returned element, if nothing was returned yet assumably do nothing
			if (previousPosition != null) {
				// if we are at the second element set it as the first element
				if (previousPosition == first) {
					first = position;
				}
				// else remove last returned element by changing the chain
				else {
					previousPosition.p.n = position;
					if (position != null) {
						position.p = previousPosition.p;
					}
				}
				size--;
			}
		}

		public Iterator reset () {
			position = first;
			previousPosition = null;
			return this;
		}
	}

	public static class Node<E> {
		/** Node previous to this */
		protected Node<E> p;
		/** Node next to this */
		protected Node<E> n;
		/** Value held */
		public E value;
		/** Index value in list */
		public int index;
	}

	static class NodePool<E> extends Pool<Node<E>> {
		@Override
		protected Node<E> newObject () {
			return new Node<E>();
		}

		public Node<E> obtain (Node<E> p, Node<E> n, E value, int index) {
			Node<E> newNode = super.obtain();
			newNode.p = p;
			newNode.n = n;
			newNode.value = value;
			newNode.index = index;
			return newNode;
		}
	}
}
