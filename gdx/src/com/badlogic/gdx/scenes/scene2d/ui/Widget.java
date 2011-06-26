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

public abstract class Widget extends Actor implements Layout {
	public float prefWidth;
	public float prefHeight;
	protected boolean invalidated = false;
	
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
	
	public void invalidate() {
		this.invalidated = true;
	}	
	
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
	
	public void setPrefSize (int prefWidth, int prefHeight) {
		this.prefWidth = width = prefWidth;
		this.prefHeight = height = prefHeight;		
		invalidateHierarchy();
	}
}
