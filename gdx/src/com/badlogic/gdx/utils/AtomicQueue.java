/*
 * Copyright (c) 2008-2010, Matthias Mann
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution. * Neither the name of Matthias Mann nor
 * the names of its contributors may be used to endorse or promote products derived from this software without specific prior
 * written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.badlogic.gdx.utils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

/** A queue that allows one thread to call {@link #put(Object)} and another thread to call {@link #poll()}. Multiple threads must
 * not call these methods.
 * @author Matthias Mann */
public class AtomicQueue<T> {
	private final AtomicInteger writeIndex = new AtomicInteger();
	private final AtomicInteger readIndex = new AtomicInteger();
	private final AtomicReferenceArray<T> queue;

	public AtomicQueue (int capacity) {
		queue = new AtomicReferenceArray(capacity);
	}

	private int next (int idx) {
		return (idx + 1) % queue.length();
	}

	public boolean put (@Null T value) {
		int write = writeIndex.get();
		int read = readIndex.get();
		int next = next(write);
		if (next == read) return false;
		queue.set(write, value);
		writeIndex.set(next);
		return true;
	}

	@Null
	public T poll () {
		int read = readIndex.get();
		int write = writeIndex.get();
		if (read == write) return null;
		T value = queue.get(read);
		readIndex.set(next(read));
		return value;
	}
}
