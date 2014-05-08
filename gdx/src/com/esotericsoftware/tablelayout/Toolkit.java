/*******************************************************************************
 * Copyright (c) 2011, Nathan Sweet <nathan.sweet@gmail.com>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package com.esotericsoftware.tablelayout;

import com.esotericsoftware.tablelayout.BaseTableLayout.Debug;

/** Base class for UI toolkit.
 * @author Nathan Sweet */
public abstract class Toolkit<C, T extends C, L extends BaseTableLayout> {
	static public Toolkit instance;

	abstract public Cell<C> obtainCell (L layout);

	abstract public void freeCell (Cell cell);

	abstract public void addChild (C parent, C child);

	abstract public void removeChild (C parent, C child);

	abstract public float getMinWidth (C widget);

	abstract public float getMinHeight (C widget);

	abstract public float getPrefWidth (C widget);

	abstract public float getPrefHeight (C widget);

	abstract public float getMaxWidth (C widget);

	abstract public float getMaxHeight (C widget);

	abstract public float getWidth (C widget);

	abstract public float getHeight (C widget);

	/** Clears all debugging rectangles. */
	abstract public void clearDebugRectangles (L layout);

	/** Adds a rectangle that should be drawn for debugging. */
	abstract public void addDebugRectangle (L layout, Debug type, float x, float y, float w, float h);

	/** @param widget May be null. */
	public void setWidget (L layout, Cell cell, C widget) {
		if (cell.widget == widget) return;
		removeChild((T)layout.table, (C)cell.widget);
		cell.widget = widget;
		if (widget != null) addChild((T)layout.table, widget);
	}

	/** Interprets the specified value as a size. This can be used to scale all sizes applied to a table. The default implementation
	 * returns the value unmodified.
	 * @see Value#width(Object)
	 * @see Value#width(Cell) */
	public float width (float value) {
		return value;
	}

	/** Interprets the specified value as a size. This can be used to scale all sizes applied to a table. The default implementation
	 * returns the value unmodified.
	 * @see Value#height(Object)
	 * @see Value#height(Cell) */
	public float height (float value) {
		return value;
	}
}
