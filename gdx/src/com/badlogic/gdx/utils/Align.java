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
 * Provides bit flag constants for alignment.
 *
 * @author Nathan Sweet
 */
public class Align {
    static public final int center = 0x1;
    static public final int top = 0x10;
    static public final int bottom = 0x100;
    static public final int left = 0x1000;
    static public final int right = 0x10000;

    static public final int topLeft = top | left;
    static public final int topRight = top | right;
    static public final int bottomLeft = bottom | left;
    static public final int bottomRight = bottom | right;

    /**
     * Return true if the Align is left.
     *
     * @param align The Align you want to check.
     * @return true if Align is left, else false.
     */
    static public final boolean isLeft (int align) {
        return (align & left) != 0;
    }

    /**
     * Return true if the Align is right.
     *
     * @param align The Align you want to check.
     * @return true if Align is right, else false.
     */
    static public final boolean isRight (int align) {
        return (align & right) != 0;
    }

    /**
     * Return true if the Align is top.
     *
     * @param align The Align you want to check.
     * @return true if Align is top, else false.
     */
    static public final boolean isTop (int align) {
        return (align & top) != 0;
    }

    /**
     * Return true if the Align is bottom.
     *
     * @param align The Align you want to check.
     * @return true if Align is bottom, else false.
     */
    static public final boolean isBottom (int align) {
        return (align & bottom) != 0;
    }

    /**
     * Return true if the Align is left, center, right.
     *
     * @param align The Align you want to check.
     * @return true if Align is left, center, or right, else false.
     */
    static public final boolean isCenterVertical (int align) {
        return (align & top) == 0 && (align & bottom) == 0;
    }

    /**
     * Return true if the Align is top, center, bottom.
     *
     * @param align The Align you want to check.
     * @return true if Align is top, center, or bottom, else false.
     */
    static public final boolean isCenterHorizontal (int align) {
        return (align & left) == 0 && (align & right) == 0;
    }

    /**
     * Transform an align to a fully written alignment string.
     * The return is in the way  of (y,x), where
     * y can be either (top, bottom, center) and
     * x can be either (top, bottom, center)
     *
     * @param align The align you want to transform to a written representation.
     * @return a string representation in written words of the align.
     */
    static public String toString (int align) {
        // Create string buffer of maximum return size.
        StringBuilder buffer = new StringBuilder(13);

        // Write the vertical representation.
        if ((align & top) != 0)
            buffer.append("top,");
        else if ((align & bottom) != 0)
            buffer.append("bottom,");
        else
            buffer.append("center,");

        // Write the horizontal representation.
        if ((align & left) != 0)
            buffer.append("left");
        else if ((align & right) != 0)
            buffer.append("right");
        else
            buffer.append("center");

        // Return the written representation.
        return buffer.toString();
    }
}
