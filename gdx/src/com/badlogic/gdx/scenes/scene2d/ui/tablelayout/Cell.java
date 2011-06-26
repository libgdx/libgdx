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
package com.badlogic.gdx.scenes.scene2d.ui.tablelayout;

import static com.badlogic.gdx.scenes.scene2d.ui.tablelayout.BaseTableLayout.*;

// BOZO - Methods for convenience, setPadding?

public class Cell {
	public String minWidth, minHeight;
	public String prefWidth, prefHeight;
	public String maxWidth, maxHeight;
	public String spaceTop, spaceLeft, spaceBottom, spaceRight;
	public String padTop, padLeft, padBottom, padRight;
	public Float fillWidth, fillHeight;
	public Integer align;
	public Integer expandWidth, expandHeight;
	public Boolean ignore;
	public Integer colspan;
	public Boolean uniformWidth, uniformHeight;
	public String name;

	public Object widget;
	public int widgetX, widgetY;
	public int widgetWidth, widgetHeight;

	boolean endRow;
	int column, row;
	int cellAboveIndex = -1;
	int padTopTemp, padLeftTemp, padBottomTemp, padRightTemp;

	Cell () {
	}

	void set (Cell defaults) {
		minWidth = defaults.minWidth;
		minHeight = defaults.minHeight;
		prefWidth = defaults.prefWidth;
		prefHeight = defaults.prefHeight;
		maxWidth = defaults.maxWidth;
		maxHeight = defaults.maxHeight;
		spaceTop = defaults.spaceTop;
		spaceLeft = defaults.spaceLeft;
		spaceBottom = defaults.spaceBottom;
		spaceRight = defaults.spaceRight;
		padTop = defaults.padTop;
		padLeft = defaults.padLeft;
		padBottom = defaults.padBottom;
		padRight = defaults.padRight;
		fillWidth = defaults.fillWidth;
		fillHeight = defaults.fillHeight;
		align = defaults.align;
		expandWidth = defaults.expandWidth;
		expandHeight = defaults.expandHeight;
		ignore = defaults.ignore;
		colspan = defaults.colspan;
		uniformWidth = defaults.uniformWidth;
		uniformHeight = defaults.uniformHeight;
	}

	void merge (Cell cell) {
		if (cell == null) return;
		if (cell.minWidth != null) minWidth = cell.minWidth;
		if (cell.minHeight != null) minHeight = cell.minHeight;
		if (cell.prefWidth != null) prefWidth = cell.prefWidth;
		if (cell.prefHeight != null) prefHeight = cell.prefHeight;
		if (cell.maxWidth != null) maxWidth = cell.maxWidth;
		if (cell.maxHeight != null) maxHeight = cell.maxHeight;
		if (cell.spaceTop != null) spaceTop = cell.spaceTop;
		if (cell.spaceLeft != null) spaceLeft = cell.spaceLeft;
		if (cell.spaceBottom != null) spaceBottom = cell.spaceBottom;
		if (cell.spaceRight != null) spaceRight = cell.spaceRight;
		if (cell.padTop != null) padTop = cell.padTop;
		if (cell.padLeft != null) padLeft = cell.padLeft;
		if (cell.padBottom != null) padBottom = cell.padBottom;
		if (cell.padRight != null) padRight = cell.padRight;
		if (cell.fillWidth != null) fillWidth = cell.fillWidth;
		if (cell.fillHeight != null) fillHeight = cell.fillHeight;
		if (cell.align != null) align = cell.align;
		if (cell.expandWidth != null) expandWidth = cell.expandWidth;
		if (cell.expandHeight != null) expandHeight = cell.expandHeight;
		if (cell.ignore != null) ignore = cell.ignore;
		if (cell.colspan != null) colspan = cell.colspan;
		if (cell.uniformWidth != null) uniformWidth = cell.uniformWidth;
		if (cell.uniformHeight != null) uniformHeight = cell.uniformHeight;
	}

	static Cell defaults () {
		Cell defaults = new Cell();
		defaults.minWidth = MIN;
		defaults.minHeight = MIN;
		defaults.prefWidth = PREF;
		defaults.prefHeight = PREF;
		defaults.maxWidth = MAX;
		defaults.maxHeight = MAX;
		defaults.spaceTop = null;
		defaults.spaceLeft = null;
		defaults.spaceBottom = null;
		defaults.spaceRight = null;
		defaults.padTop = null;
		defaults.padLeft = null;
		defaults.padBottom = null;
		defaults.padRight = null;
		defaults.fillWidth = 0f;
		defaults.fillHeight = 0f;
		defaults.align = CENTER;
		defaults.expandWidth = 0;
		defaults.expandHeight = 0;
		defaults.ignore = false;
		defaults.colspan = 1;
		return defaults;
	}
}
