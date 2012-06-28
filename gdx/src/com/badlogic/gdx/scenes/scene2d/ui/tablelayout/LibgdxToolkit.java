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

import java.lang.reflect.InvocationTargetException;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.BaseTableLayout.Debug;
import com.esotericsoftware.tablelayout.Toolkit;

/** The libgdx implementation of the table layout functionality.
 * @author Nathan Sweet */
public class LibgdxToolkit extends Toolkit<Actor, Table, TableLayout> {
	static public LibgdxToolkit instance = new LibgdxToolkit();

	static boolean drawDebug;

	public void addChild (Actor parent, Actor child, String layoutString) {
		child.remove();
		try {
			parent.getClass().getMethod("setWidget", Actor.class).invoke(parent, child);
			return;
		} catch (InvocationTargetException ex) {
			throw new RuntimeException("Error calling setWidget.", ex);
		} catch (Exception ignored) {
		}
		((Group)parent).addActor(child);
	}

	public void removeChild (Actor parent, Actor child) {
		((Group)parent).removeActor(child);
	}

	public float getMinWidth (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getMinWidth();
		return (int)actor.getWidth();
	}

	public float getMinHeight (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getMinHeight();
		return (int)actor.getHeight();
	}

	public float getPrefWidth (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getPrefWidth();
		return (int)actor.getWidth();
	}

	public float getPrefHeight (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getPrefHeight();
		return (int)actor.getHeight();
	}

	public float getMaxWidth (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getMaxWidth();
		return 0;
	}

	public float getMaxHeight (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getMaxHeight();
		return 0;
	}

	public float getWidth (Actor widget) {
		return widget.getWidth();
	}

	public float getHeight (Actor widget) {
		return widget.getHeight();
	}

	public void clearDebugRectangles (TableLayout layout) {
		if (layout.debugRects != null) layout.debugRects.clear();
	}

	public void addDebugRectangle (TableLayout layout, Debug type, float x, float y, float w, float h) {
		drawDebug = true;
		if (layout.debugRects == null) layout.debugRects = new Array();
		layout.debugRects.add(new DebugRect(type, x, layout.getTable().getHeight() - y, w, h));
	}

	static class DebugRect extends Rectangle {
		final Debug type;

		public DebugRect (Debug type, float x, float y, float width, float height) {
			super(x, y, width, height);
			this.type = type;
		}
	}
}
