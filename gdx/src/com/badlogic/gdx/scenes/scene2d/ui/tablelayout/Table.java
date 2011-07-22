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

import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Table extends Group implements Layout {
	public final TableLayout layout;

	public Table () {
		this(new TableLayout());
	}

	public Table (TableLayout layout) {
		this.layout = layout;
		layout.table = this;
	}

	public Table (String name) {
		super(name);
		layout = new TableLayout();
		layout.table = this;
	}

	public Table (String name, TableLayout layout) {
		super(name);
		this.layout = layout;
		layout.table = this;
	}

	protected void draw (SpriteBatch batch, float parentAlpha) {
		if (layout.needsLayout) layout.layout();
		super.draw(batch, parentAlpha);
	}
	
	protected void applyLayout() {
		if (layout.needsLayout) layout.layout();
	}

	public void layout () {
		layout.layout();
	}

	public void invalidate () {
		layout.invalidate();
	}

	public float getPrefWidth () {
		layout.layout();
		return layout.tablePrefWidth;
	}

	public float getPrefHeight () {
		layout.layout();
		return layout.tablePrefHeight;
	}

	/**
	 * Draws the debug lines for all TableLayouts in the stage. If this method is not called each frame, no debug lines will be
	 * drawn.
	 */
	static public void drawDebug (Stage stage) {
		drawDebug(stage.getActors());
	}

	static private void drawDebug (List<Actor> actors) {
		for (int i = 0, n = actors.size(); i < n; i++) {
			Actor actor = actors.get(i);
			if (actor instanceof Table) ((Table)actor).layout.drawDebug();
			if (actor instanceof Group) drawDebug(((Group)actor).getActors());
		}
	}
}
