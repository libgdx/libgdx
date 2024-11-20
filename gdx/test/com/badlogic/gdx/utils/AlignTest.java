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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.utils;

import static org.junit.Assert.*;
import org.junit.Test;

public class AlignTest {

    @Test
    public void testIsLeft() {
        /*This method tests if the isLeft function correctly identifies the left alignment
        and does not incorrectly identify the right alignment*/
        assertTrue(Align.isLeft(Align.left));
        assertFalse(Align.isLeft(Align.right));
    }

    @Test
    public void testIsRight() {
        /*This method tests if the isRight function correctly identifies the right alignment
        and does not incorrectly identify the left alignment*/
        assertTrue(Align.isRight(Align.right));
        assertFalse(Align.isRight(Align.left));
    }

    @Test
    public void testIsTop() {
        /*This method tests if the isTop function correctly identifies the top alignment
        and does not incorrectly identify the bottom alignment*/
        assertTrue(Align.isTop(Align.top));
        assertFalse(Align.isTop(Align.bottom));
    }

    @Test
    public void testIsBottom() {
        /*This method tests if the isBottom function correctly identifies the bottom alignment
        and does not incorrectly identify the top alignment*/
        assertTrue(Align.isBottom(Align.bottom));
        assertFalse(Align.isBottom(Align.top));
    }

    @Test
    public void testIsCenterVertical() {
        /*This method tests if the isCenterVertical function correctly identifies the center vertical alignment
        and does not incorrectly identify the top alignment*/
        assertTrue(Align.isCenterVertical(Align.center));
        assertFalse(Align.isCenterVertical(Align.top));
    }

    @Test
    public void testIsCenterHorizontal() {
        /*This method tests if the isCenterHorizontal function correctly identifies the center horizontal alignment
        and does not incorrectly identify the left alignment*/
        assertTrue(Align.isCenterHorizontal(Align.center));
        assertFalse(Align.isCenterHorizontal(Align.left));
    }

    @Test
    public void testToString() {
        /*This method tests if the toString function correctly converts the alignment constants
        to their string representations, such as "top,left", "bottom,right", and "center,center"*/
        assertEquals("top,left", Align.toString(Align.topLeft));
        assertEquals("bottom,right", Align.toString(Align.bottomRight));
        assertEquals("center,center", Align.toString(Align.center));
    }
}
