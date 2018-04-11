package com.badlogic.gdx.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class StackTest {

    @Test
    public void testIsEmpty() {
        final Stack<String> stack = new Stack<String>();

        assertTrue("Stack is empty", stack.isEmpty());

        stack.push("An element");
        assertFalse("Stack is not empty", stack.isEmpty());
    }

    @Test
    public void testUnderflow() {
        final Stack<String> stack = new Stack<String>();

        try {
            stack.pop();
        } catch (Exception e) {
            assertTrue("Underflow is indicated", e.getMessage().equals("Stack underflow"));
        }
    }

    @Test
    public void testOverflow() {
        final Stack<String> stack = new Stack<String>(3);

        try {
            stack.push("first");
            stack.push("second");
            stack.push("third");
            stack.push("fourth, should not fit!");
        } catch (Exception e) {
            assertTrue("Overflow is indicated", e.getMessage().equals("Stack overflow"));
        }
    }

    @Test
    public void testMethodClear() {
        final Stack<String> stack = new Stack<String>();

        stack.push("1");
        stack.push("2");
        stack.push("3");
        stack.push("4");

        assertFalse("Stack is filled", stack.isEmpty());

        stack.clear();

        assertTrue("Stack is cleared properly", stack.isEmpty());

    }



}
