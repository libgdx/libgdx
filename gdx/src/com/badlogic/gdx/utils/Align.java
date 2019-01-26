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

/** Provides bit flag constants for alignment.
 * @author Nathan Sweet */
public class Align {

    static public final int center = 1 << 0;
    static public final int top = 1 << 1;
    static public final int bottom = 1 << 2;
    static public final int left = 1 << 3;
    static public final int right = 1 << 4;

    static public final int topLeft = top | left;
    static public final int topCenter = top | left | right;
    static public final int topRight = top | right;
    static public final int centerLeft = top | bottom | left;
    static public final int centerRight = top | bottom | right;
    static public final int bottomLeft = bottom | left;
    static public final int bottomCenter = bottom | left | right;
    static public final int bottomRight = bottom | right;

    static public boolean isLeft(int align) {
        return (align & left) != 0;
    }

    static public boolean isRight(int align) {
        return (align & right) != 0;
    }

    static public boolean isTop(int align) {
        return (align & top) != 0;
    }

    static public boolean isBottom(int align) {
        return (align & bottom) != 0;
    }

    static public boolean isCenterVertical(int align) {
        return isTop(align) && isBottom(align) || align == center;
    }

    static public boolean isCenterHorizontal(int align) {
        return isLeft(align) && isRight(align) || align == center;
    }

    static public String toString(int align) {
        if (align == center) return "center";

        StringBuilder buffer = new StringBuilder(11);

        if (isCenterVertical(align)) {
            buffer.append("center ");
        } else if (isTop(align)) {
            buffer.append("top ");
        } else if (isBottom(align)) {
            buffer.append("bottom ");
        }

        if (isCenterHorizontal(align)) {
            buffer.append("center ");
        } else if (isLeft(align)) {
            buffer.append("left ");
        } else if (isRight(align)) {
            buffer.append("right ");
        }

        buffer.deleteCharAt(buffer.length - 1);
        return buffer.toString();
    }
}
