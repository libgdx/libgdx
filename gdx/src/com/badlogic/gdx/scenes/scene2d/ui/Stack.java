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

package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.LibgdxToolkit;

/** A stack is a container that sizes its children to its size and positions them at 0,0 on top of each other.
 * <p>
 * The preferred and min size of the stack is the largest preferred and min size of any children. The max size of the stack is the
 * smallest max size of any children.
 * @author Nathan Sweet */
public class Stack extends WidgetGroup {
	public Stack () {
		this(null);
	}

	public Stack (String name) {
		super(name);
		transform = false;
		width = 150;
		height = 150;
	}

	public void layout () {
		for (int i = 0, n = children.size(); i < n; i++) {
			Actor actor = children.get(i);
			actor.x = 0;
			actor.y = 0;
			actor.width = width;
			actor.height = height;
			if (actor instanceof Layout) ((Layout)actor).invalidate();
		}
	}

	public float getPrefWidth () {
		float width = 0;
		for (int i = 0, n = children.size(); i < n; i++)
			width = Math.max(width, LibgdxToolkit.instance.getPrefWidth(children.get(i)));
		return width * scaleX;
	}

	public float getPrefHeight () {
		float height = 0;
		for (int i = 0, n = children.size(); i < n; i++)
			height = Math.max(height, LibgdxToolkit.instance.getPrefHeight(children.get(i)));
		return height * scaleY;
	}

	public float getMaxWidth () {
		if (children.isEmpty()) return 0;
		float width = 0;
		for (int i = 0, n = children.size(); i < n; i++) {
			int maxWidth = LibgdxToolkit.instance.getMaxHeight(children.get(i));
			if (maxWidth > 0) width = width == 0 ? maxWidth : Math.min(width, maxWidth);
		}
		return width * scaleX;
	}

	public float getMaxHeight () {
		if (children.isEmpty()) return 0;
		float height = 0;
		for (int i = 0, n = children.size(); i < n; i++) {
			int maxHeight = LibgdxToolkit.instance.getMaxHeight(children.get(i));
			if (maxHeight > 0) height = height == 0 ? maxHeight : Math.min(height, maxHeight);
		}
		return height * scaleY;
	}

	public float getMinWidth () {
		float width = 0;
		for (int i = 0, n = children.size(); i < n; i++)
			width = Math.max(width, LibgdxToolkit.instance.getMinWidth(children.get(i)));
		return width * scaleX;
	}

	public float getMinHeight () {
		float height = 0;
		for (int i = 0, n = children.size(); i < n; i++)
			height = Math.max(height, LibgdxToolkit.instance.getMinHeight(children.get(i)));
		return height * scaleY;
	}
}
