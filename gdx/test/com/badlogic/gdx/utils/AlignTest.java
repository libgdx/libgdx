/*******************************************************************************
 * Copyright 2024 see AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 ******************************************************************************/

package com.badlogic.gdx.utils;

import static org.junit.Assert.*;
import org.junit.Test;

/*
 * This test class verifies the correctness of the alignment functions in the Align class.
 * Each test ensures that the corresponding alignment function correctly identifies its
 * respective alignment.
 */

public class AlignTest {

    @Test
    public void testIsLeft () {
        assertTrue(Align.isLeft(Align.left));
        assertFalse(Align.isLeft(Align.right));
        assertTrue(Align.isLeft(Align.topLeft));
        assertFalse(Align.isLeft(Align.topRight));
    }

    @Test
    public void testIsRight () {
        assertTrue(Align.isRight(Align.right));
        assertFalse(Align.isRight(Align.left));
        assertTrue(Align.isRight(Align.topRight));
        assertFalse(Align.isRight(Align.topLeft));
    }

    @Test
    public void testIsTop () {
        assertTrue(Align.isTop(Align.top));
        assertFalse(Align.isTop(Align.bottom));
        assertTrue(Align.isTop(Align.topLeft));
        assertFalse(Align.isTop(Align.bottomLeft));
        assertTrue(Align.isTop(Align.topRight));
        assertFalse(Align.isTop(Align.bottomRight));
    }

    @Test
    public void testIsBottom () {
        assertTrue(Align.isBottom(Align.bottom));
        assertFalse(Align.isBottom(Align.top));
        assertTrue(Align.isBottom(Align.bottomLeft));
        assertFalse(Align.isBottom(Align.topLeft));
        assertTrue(Align.isBottom(Align.bottomRight));
        assertFalse(Align.isBottom(Align.topRight));
    }

    @Test public void testIsCenterVertical() {
        assertTrue(Align.isCenterVertical(Align.center));
        assertFalse(Align.isCenterVertical(Align.top));
        assertFalse(Align.isCenterVertical(Align.bottom));
        assertFalse(Align.isCenterVertical(Align.topLeft));
        assertFalse(Align.isCenterVertical(Align.topRight));
        assertFalse(Align.isCenterVertical(Align.bottomLeft));
        assertFalse(Align.isCenterVertical(Align.bottomRight));
    }

    @Test public void testIsCenterHorizontal() {
        assertTrue(Align.isCenterHorizontal(Align.center));
        assertFalse(Align.isCenterHorizontal(Align.left));
        assertFalse(Align.isCenterHorizontal(Align.right));
        assertFalse(Align.isCenterHorizontal(Align.topLeft));
        assertFalse(Align.isCenterHorizontal(Align.topRight));
        assertFalse(Align.isCenterHorizontal(Align.bottomLeft));
        assertFalse(Align.isCenterHorizontal(Align.bottomRight));
    }

    @Test
    public void testCombinations () {
        assertTrue(Align.isLeft(Align.bottomLeft));
        assertTrue(Align.isRight(Align.bottomRight));
        assertTrue(Align.isTop(Align.topRight));
        assertTrue(Align.isBottom(Align.bottomRight));

        assertFalse(Align.isLeft(Align.topRight));
        assertFalse(Align.isRight(Align.topLeft));
        assertFalse(Align.isTop(Align.bottomLeft));
        assertFalse(Align.isBottom(Align.topLeft));
    }
}
