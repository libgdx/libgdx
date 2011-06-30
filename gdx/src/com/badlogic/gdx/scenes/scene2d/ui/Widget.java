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
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;

/**
 * Base class for all UI widgets. A widget implements the {@link Layout} interface which
 * has a couple of features.</p>
 * 
 * A widget has a preferred width and height which it will use if possible, e.g. if it is not in a {@link Container} ({@link Pane}, {@link Window}) or a {@link SplitPane} or
 * a {@link ScrollPane}. In case it is contained in one of the aforementioned containers, the
 * preferred width and height will be used to guide the layouting mechanism employed by those containers.</p>
 * 
 * A widget can be invalidated, e.g. by a Container changing its available space in the layout, in which case it will layout itself at the next oportunity to do so.</p>
 * 
 * Invalidation can also be triggered manually via a call to {@link #invalidate()} or {@link #invalidateHierarchy()}. The former
 * will tell the Widget to only invalidate itself. The later will also invalidate all the widget's parents. The later mechanism is 
 * used in case the widget was modified and the container it is contained in must relayout itself due to this modification as well.
 * @author mzechner
 *
 */
public abstract class Widget extends Actor implements Layout {
	public float prefWidth;
	public float prefHeight;
	protected boolean invalidated = false;

	/**
	 * Creates a new widget with the preferred width and height
	 * @param name the name
	 * @param prefWidth the preferred width
	 * @param prefHeight the preferred height
	 */
	public Widget(String name, float prefWidth, float prefHeight) {
		super(name);
		this.prefWidth = prefWidth;
		this.prefHeight = prefHeight;
	}		
		
	@Override
	public float getPrefWidth() {
		return prefWidth;
	}

	@Override
	public float getPrefHeight() {
		return prefHeight;
	}
	/**
	 * Invalidates this widget, causing it to relayout itself
	 * at the next oportunity.
	 */
	public void invalidate() {
		this.invalidated = true;
	}	
	
	/**
	 * Invalidates this widget and all its parents, causing all involved
	 * widgets to relayout themselves at the next oportunity.
	 */
	public void invalidateHierarchy() {
		invalidate();		
		Group parent = this.parent;
		while(parent != null) {
			if(parent instanceof Layout) ((Layout) parent).invalidate();
			parent = parent.parent;
		}
	}
	
	@Override
	public Actor hit(float x, float y) {
		return x > 0 && x < width && y > 0 && y < height?this: null;
	}
	
	/**
	 * Sets the preferred width and height of this widget. Invalidates
	 * all parents.
	 * @param prefWidth the preferred width
	 * @param prefHeight the preferred height
	 */
	public void setPrefSize (int prefWidth, int prefHeight) {
		this.prefWidth = width = prefWidth;
		this.prefHeight = height = prefHeight;		
		invalidateHierarchy();
	}
}
