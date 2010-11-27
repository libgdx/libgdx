/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.badlogic.gdx.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.utils.Pool.PoolObjectFactory;

/**
 * A specialized lockless thread queue class template
 * for doing single direction message passing from one
 * thread to another.
 * 
 * This queue class does NOT use the Synchronized method. However
 * it is designed with atomic queue passing which makes it
 * very nice for non blocking event passing.
 * 
 * This queue class keeps a fixed sized queue buffer which must be
 * defined during initialization. If the queue buffer gets filled
 * up, any new queue request will get dropped.
 *  
 * This queue class also handles object pooling such that no extra allocation
 * will be used. This prevents bad GC trashing.
 */
public class LocklessThreadQueue<T> {
	/**
	 * Interface for an Object Factory for the initial buffer creation.
	 * @param <T> the type
	 */
	public interface ObjectFactory<T> {
		public T createObject ();
	}

	protected List<T> queueBuffer;
	protected AtomicInteger head;
	protected AtomicInteger tail;
	protected int queueSize;

	/**
	 * Constructor.
	 * @param factory Object factory for pre-buffered queue pool allocation.
	 * @param size Number of queue objects allowed before dropping queue request.
	 */
	public LocklessThreadQueue (ObjectFactory<T> factory, int size) {
		queueSize = size + 2;
		queueBuffer = new ArrayList<T>(queueSize);
		head = new AtomicInteger(0);
		tail = new AtomicInteger(size + 1);

		for (int i = 0; i < queueSize; ++i)
		{
			queueBuffer.add(factory.createObject());
		}
	}

	/** Returns the next available preallocated object for preparation. */
	public T prepare () {
		int currHead = head.get();
		int nextHead = (currHead + 1) % queueSize;
		if (nextHead == tail.get()) return null;
		return queueBuffer.get(currHead);
	}

	/**
	 * Push the next available object to the queue.
	 * This must be called after prepare is called for preparation.
	 * If it prepare() was not called, this will simply push the object unchanged from it's last queued state.
	 */
	public void push () {
		int nextHead = (head.get() + 1) % queueSize;
		if (nextHead == tail.get()) return;
		head.set(nextHead);
	}

	/** Returns null or the next queued object in the queue list */
	public T pop() {
		int nextTail = (tail.get() + 1) % queueSize; 
		if (head.get() == nextTail) return null;
		T object = queueBuffer.get(nextTail);
		tail.set(nextTail);
		return object;
	}
}
