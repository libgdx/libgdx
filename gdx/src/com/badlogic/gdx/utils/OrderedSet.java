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

import java.util.NoSuchElementException;

/** An {@link ObjectSet} that also stores keys in an {@link Array} using the insertion order. {@link #iterator() Iteration} is
 * ordered and faster than an unordered set. Keys can also be accessed and the order changed using {@link #orderedItems()}. There
 * is some additional overhead for put and remove. When used for faster iteration versus ObjectSet and the order does not actually
 * matter, copying during remove can be greatly reduced by setting {@link Array#ordered} to false for
 * {@link OrderedSet#orderedItems()}.
 * @author Nathan Sweet */
public class OrderedSet<T> extends ObjectSet<T> {
	final Array<T> items;
	OrderedSetIterator iterator1, iterator2;

	public OrderedSet () {
		items = new Array();
	}

	public OrderedSet (int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		items = new Array(capacity);
	}

	public OrderedSet (int initialCapacity) {
		super(initialCapacity);
		items = new Array(capacity);
	}

	public OrderedSet (OrderedSet set) {
		super(set);
		items = new Array(capacity);
		items.addAll(set.items);
	}

	public boolean add (T key) {
		if (!super.add(key)) return false;
		items.add(key);
		return true;
	}

	public boolean add (T key, int index) {
		if (!super.add(key)) {
			items.removeValue(key, true);
			items.insert(index, key);
			return false;
		}
		items.insert(index, key);
		return true;
	}

	public boolean remove (T key) {
		if (!super.remove(key)) return false;
		items.removeValue(key, false);
		return true;
	}

	public void clear (int maximumCapacity) {
		items.clear();
		super.clear(maximumCapacity);
	}

	public void clear () {
		items.clear();
		super.clear();
	}

	public Array<T> orderedItems () {
		return items;
	}

	public OrderedSetIterator<T> iterator () {
		if (iterator1 == null) {
			iterator1 = new OrderedSetIterator(this);
			iterator2 = new OrderedSetIterator(this);
		}
		if (!iterator1.valid) {
			iterator1.reset();
			iterator1.valid = true;
			iterator2.valid = false;
			return iterator1;
		}
		iterator2.reset();
		iterator2.valid = true;
		iterator1.valid = false;
		return iterator2;
	}

	public String toString () {
		if (size == 0) return "{}";
		T[] items = this.items.items;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('{');
		buffer.append(items[0]);
		for (int i = 1; i < size; i++) {
			buffer.append(", ");
			buffer.append(items[i]);
		}
		buffer.append('}');
		return buffer.toString();
	}

	public String toString (String separator) {
		return items.toString(separator);
	}

	static public class OrderedSetIterator<T> extends ObjectSetIterator<T> {
		private Array<T> items;

		public OrderedSetIterator (OrderedSet<T> set) {
			super(set);
			items = set.items;
		}

		public void reset () {
			nextIndex = 0;
			hasNext = set.size > 0;
		}

		public T next () {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
			T key = items.get(nextIndex);
			nextIndex++;
			hasNext = nextIndex < set.size;
			return key;
		}

		public void remove () {
			if (nextIndex < 0) throw new IllegalStateException("next must be called before remove.");
			nextIndex--;
			set.remove(items.get(nextIndex));
		}
	}
}
