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

/**
 * A queue that allows one thread to call {@link #put(Object)} and another thread to call {@link #poll()}. Multiple threads must
 * not call these methods.
 *
 * @author Matthias Mann
 */
public class AtomicQueue<T> {
    private final AtomicInteger writeIndex = new AtomicInteger();
    private final AtomicInteger readIndex = new AtomicInteger();
    private final AtomicReferenceArray<T> queue;

    /**
     * Constructor of AtomicQueue, set the size of the queue to a specific capacity.
     *
     * @param capacity The maximum capacity of the queue.
     */
    public AtomicQueue (int capacity) {
        queue = new AtomicReferenceArray<>(capacity);
    }

    /**
     * Method to retrieve the next id of the queue, will wrap around.
     *
     * @param idx The previous id you want to get the next of.
     * @return the next id given idx. The value will wraparound if larger than queue capacity.
     */
    private int next (int idx) {
        return (idx + 1) % queue.length();
    }

    /**
     * Method to put an element inside the atomicQueue.
     * If the next put element will be placed at the current {@link #readIndex},
     * then it is skipped and false is returned.
     *
     * @param value The value you want to put to the AtomicQueue.
     * @return true if it was put on the AtomicQueue, else false.
     */
    public boolean put (@Null T value) {
        int write = writeIndex.get();
        int read = readIndex.get();
        int next = next(write);

        // It is not possible to overwrite the element that needs to be polled first.
        if (next == read) {
            return false;
        }

        // There was space to put the element.
        queue.set(write, value);
        writeIndex.set(next);
        return true;
    }

    /**
     * Method to poll an element inside the atomicQueue.
     * If the {@link #readIndex} is at the {@link #writeIndex}, then no element
     * could be polled, as there are no elements in the queue left.
     *
     * @return The first element in the queue that has not been polled yet,
     * if there are no elements left in the queue, then null is returned.
     */
    public @Null T poll () {
        int read = readIndex.get();
        int write = writeIndex.get();

        // There are no elements to poll yet, as the read pointer equals the write pointer.
        if (read == write) {
            return null;
        }

        // There was an element that could be read from the queue.
        T value = queue.get(read);
        readIndex.set(next(read));
        return value;
    }
}
