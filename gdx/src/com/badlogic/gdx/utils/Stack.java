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

/**
 * A lightweight, non-resizable, ordered array, with strict rules of reading and writing, implementing LIFO principle.
 * Is supposed to replace a semantic excess of {@link com.badlogic.gdx.utils.Queue} Deque functionality with pure Stack data structure.
 * Allows writing and reading to and from the beginning in O(1).
 * Take care before using it, it is not resizable! Once defined, it holds the exact amount of values passed via {@link #Stack(int)} or 16, if no parameters were passed to constructor.
 */
public class Stack<T> {

    /** Contains items inside this Stack */
    private T[] items;
    /** Indicates the maximal capacity of the Stack. Is used instead of items.length for the sake of readability */
    private int size;
    /** Points at the top of the stack. If -1, then the stack is empty */
    private int top;

    /** Creates a Stack which can hold 16 values */
    public Stack() {
        this(16);
    }

    /** Creates a Stack which can hold the specified number of values */
    @SuppressWarnings("unchecked")
    public Stack(int initialSize) {
        this.items = (T[])new Object[initialSize];
        this.size = initialSize;
        this.top = -1;
    }

    /**
     * Puts an element on top of the stack.
     * @throws IndexOutOfBoundsException if the stack cannot hold any more elements.
     */
    public void push(T element) throws IndexOutOfBoundsException {
        /* `top + 2` because top is a pointer to an array cell.
         * Thus, `top + 1` to make it even with `size`, `top + 2` to check for additional space. */
        if (top + 2 > size) {
            throw new IndexOutOfBoundsException("Stack overflow");
        }
        this.top += 1;
        this.items[top] = element;
    }

    /**
     * Removes the top element of the stack and returns it.
     * @throws IndexOutOfBoundsException if the stack is empty.
     */
    public T pop() throws IndexOutOfBoundsException {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("Stack underflow");
        }
        T element = items[top];
        this.items[top] = null;
        this.top -= 1;
        return element;
    }

    /**
     * Returns the top element of the stack without removing it.
     * @throws IndexOutOfBoundsException if the stack is empty.
     */
    public T peek() throws IndexOutOfBoundsException {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("Stack is empty");
        }
        return this.items[top];
    }

    public boolean isEmpty() {
        return top == -1;
    }

    /**
     * Replaces all values with nulls and sets `top` to -1
     */
    public void clear() {
        if (!isEmpty()) {
            for (int i = 0; i <= top; i++) {
                this.items[i] = null;
            }
            top = -1;
        }
    }

}
