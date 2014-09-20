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

/** An unordered set where the keys are objects. This implementation uses cuckoo hashing using 3 hashes, random walking, and a
 * small stash for problematic keys. Null keys are not allowed. No allocation is done except when growing the table size. <br>
 * <br>
 * This set performs very fast contains and remove (typically O(1), worst case O(log(n))). Add may be a bit slower, depending on
 * hash collisions. Load factors greater than 0.91 greatly increase the chances the set will have to rehash to the next higher POT
 * size.
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
		if (!contains(key)) items.add(key);
		return super.add(key);
	}

	public boolean remove (T key) {
		items.removeValue(key, false);
		return super.remove(key);
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
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('{');
		Array<T> keys = this.items;
		for (int i = 0, n = keys.size; i < n; i++) {
			T key = keys.get(i);
			if (i > 0) buffer.append(", ");
			buffer.append(key);
		}
		buffer.append('}');
		return buffer.toString();
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
