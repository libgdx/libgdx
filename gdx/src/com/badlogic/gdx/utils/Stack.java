package com.badlogic.gdx.utils;

public class Stack<T> {

    private T[] items;
    private int size;
    private int top;

    public Stack() {
        this(16);
    }

    @SuppressWarnings("unchecked")
    public Stack(int initialSize) {
        this.items = (T[])new Object[initialSize];
        this.size = initialSize;
        this.top = -1;
    }

    public void push(T element) throws IndexOutOfBoundsException {
        /* `top + 2` because top is a pointer to an array cell.
         * Thus, `top + 1` to make it even with `size`, `top + 2` to check for additional space. */
        if (top + 2 > size) {
            throw new IndexOutOfBoundsException("Stack overflow");
        }
        this.top += 1;
        this.items[top] = element;
    }

    public T pop() throws IndexOutOfBoundsException {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("Stack underflow");
        }
        T element = items[top];
        this.items[top] = null;
        this.top -= 1;
        return element;
    }

    public T peek() throws Exception {
        if (isEmpty()) {
            throw new Exception("Stack is empty");
        }
        return this.items[top];
    }

    public boolean isEmpty() {
        return top == -1;
    }

    public void clear() {
        if (!isEmpty()) {
            for (int i = 0; i <= top; i++) {
                this.items[i] = null;
            }
            top = -1;
        }
    }

}
