package com.badlogic.gdx.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class AlignTest {

	@Test
	public void isLeftTest() {
		assertFalse(Align.isLeft(Align.top));
		assertFalse(Align.isLeft(Align.topRight));
		assertFalse(Align.isLeft(Align.right));
		assertFalse(Align.isLeft(Align.bottomRight));
		assertFalse(Align.isLeft(Align.bottom));
		assertTrue(Align.isLeft(Align.bottomLeft));
		assertTrue(Align.isLeft(Align.left));
		assertTrue(Align.isLeft(Align.topLeft));
		assertFalse(Align.isLeft(Align.center));
	}

	@Test
	public void isRightTest() {
		assertFalse(Align.isRight(Align.top));
        assertTrue(Align.isRight(Align.topRight));
        assertTrue(Align.isRight(Align.right));
        assertTrue(Align.isRight(Align.bottomRight));
		assertFalse(Align.isRight(Align.bottom));
        assertFalse(Align.isRight(Align.bottomLeft));
        assertFalse(Align.isRight(Align.left));
        assertFalse(Align.isRight(Align.topLeft));
		assertFalse(Align.isRight(Align.center));
	}

    @Test
    public void isTopTest() {
        assertTrue(Align.isTop(Align.top));
        assertTrue(Align.isTop(Align.topRight));
        assertFalse(Align.isTop(Align.right));
        assertFalse(Align.isTop(Align.bottomRight));
        assertFalse(Align.isTop(Align.bottom));
        assertFalse(Align.isTop(Align.bottomLeft));
        assertFalse(Align.isTop(Align.left));
        assertTrue(Align.isTop(Align.topLeft));
        assertFalse(Align.isTop(Align.center));
    }

    @Test
    public void isBottomTest() {
        assertFalse(Align.isBottom(Align.top));
        assertFalse(Align.isBottom(Align.topRight));
        assertFalse(Align.isBottom(Align.right));
        assertTrue(Align.isBottom(Align.bottomRight));
        assertTrue(Align.isBottom(Align.bottom));
        assertTrue(Align.isBottom(Align.bottomLeft));
        assertFalse(Align.isBottom(Align.left));
        assertFalse(Align.isBottom(Align.topLeft));
        assertFalse(Align.isBottom(Align.center));
    }

    @Test
    public void isCenterVerticalTest() {
        assertFalse(Align.isCenterVertical(Align.top));
        assertFalse(Align.isCenterVertical(Align.topRight));
        assertTrue(Align.isCenterVertical(Align.right));
        assertFalse(Align.isCenterVertical(Align.bottomRight));
        assertFalse(Align.isCenterVertical(Align.bottom));
        assertFalse(Align.isCenterVertical(Align.bottomLeft));
        assertTrue(Align.isCenterVertical(Align.left));
        assertFalse(Align.isCenterVertical(Align.topLeft));
        assertTrue(Align.isCenterVertical(Align.center));
    }

    @Test
    public void isCenterHorizontalTest() {
        assertTrue(Align.isCenterHorizontal(Align.top));
        assertFalse(Align.isCenterHorizontal(Align.topRight));
        assertFalse(Align.isCenterHorizontal(Align.right));
        assertFalse(Align.isCenterHorizontal(Align.bottomRight));
        assertTrue(Align.isCenterHorizontal(Align.bottom));
        assertFalse(Align.isCenterHorizontal(Align.bottomLeft));
        assertFalse(Align.isCenterHorizontal(Align.left));
        assertFalse(Align.isCenterHorizontal(Align.topLeft));
        assertTrue(Align.isCenterHorizontal(Align.center));
    }

    @Test
    public void toStringTest() {
        assertEquals("top,center", Align.toString(Align.top));
        assertEquals("top,right", Align.toString(Align.topRight));
        assertEquals("center,right", Align.toString(Align.right));
        assertEquals("bottom,right", Align.toString(Align.bottomRight));
        assertEquals("bottom,center", Align.toString(Align.bottom));
        assertEquals("bottom,left", Align.toString(Align.bottomLeft));
        assertEquals("center,left", Align.toString(Align.left));
        assertEquals("top,left", Align.toString(Align.topLeft));
        assertEquals("center,center", Align.toString(Align.center));
    }
}
