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

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.utils.ScissorStack;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class SplitPane extends Group implements Layout {
	final SplitPaneStyle style;
	float prefWidth;
	float prefHeight;
	
	boolean invalidated = false;
		
	boolean vertical;
	float splitAmount = 0.5f;
	float minAmount = 0;
	float maxAmount = 1;
	float oldSplitAmount = 0;
	Stage stage;
	Actor firstWidget;
	Actor secondWidget;
	Rectangle firstWidgetBounds = new Rectangle();
	Rectangle secondWidgetBounds = new Rectangle();
	Rectangle handleBounds = new Rectangle();
	Rectangle[] scissors = new Rectangle[] { new Rectangle(), new Rectangle() };	
	boolean touchDrag = false;

	public SplitPane(String name, Stage stage, Actor firstWidget, Actor secondWidget, boolean vertical, int prefWidth, int prefHeight, SplitPaneStyle style) {
		super(name);
		this.stage = stage;
		this.style = style;
		this.firstWidget = firstWidget;
		this.secondWidget = secondWidget;
		this.prefWidth = this.width = prefWidth;
		this.prefHeight = this.height = prefHeight;
		this.vertical = vertical;		
		
		this.addActor(firstWidget);
		this.addActor(secondWidget);		
		layout();
	}	

	@Override	
	public void layout() {
		if(firstWidget instanceof Layout) {
			Layout layout = (Layout)firstWidget;
			layout.layout();
			firstWidget.width = layout.getPrefWidth();
			firstWidget.height = layout.getPrefHeight();		
		}
		
		if(secondWidget instanceof Layout) {
			Layout layout = (Layout)secondWidget;
			layout.layout();			
			secondWidget.width = layout.getPrefWidth();
			secondWidget.height = layout.getPrefHeight();		
		}
		
		invalidated = false;
	}	

	@Override
	public void invalidate() {
		if(firstWidget instanceof Layout) ((Layout)firstWidget).invalidate();
		if(secondWidget instanceof Layout) ((Layout)secondWidget).invalidate();
		invalidated = true;
	}

	@Override
	public float getPrefWidth() {
		return prefHeight;
	}

	@Override
	public float getPrefHeight() {
		return prefWidth;
	}
	
	private void calculateBoundsAndPositions(Matrix4 transform) {
		if(oldSplitAmount != splitAmount) {
			oldSplitAmount = splitAmount;
			invalidate();			
		}
		
		if(!vertical) calculateHorizBoundsAndPositions();
		else calculateVertBoundsAndPositions();			
		
		boolean layoutFirst = false;
		boolean layoutSecond = false;		
		if(firstWidget.width != firstWidgetBounds.width || firstWidget.height != firstWidgetBounds.height) {
			layoutFirst = true;
		}		
		if(secondWidget.width != secondWidgetBounds.width || secondWidget.height != secondWidgetBounds.height) {
			layoutSecond = true;
		}
		
		firstWidget.x = firstWidgetBounds.x;
		firstWidget.y = firstWidgetBounds.y;
		firstWidget.width = firstWidgetBounds.width;
		firstWidget.height = firstWidgetBounds.height;
		
		secondWidget.x = secondWidgetBounds.x;
		secondWidget.y = secondWidgetBounds.y;
		secondWidget.width = secondWidgetBounds.width;
		secondWidget.height = secondWidgetBounds.height;		
		
		if(layoutFirst && firstWidget instanceof Layout) {
			((Layout)firstWidget).invalidate();			
		}
		
		if(layoutSecond && secondWidget instanceof Layout) {
			((Layout)secondWidget).invalidate();			
		}
		ScissorStack.calculateScissors(stage.getCamera(), transform, firstWidgetBounds, scissors[0]);
		ScissorStack.calculateScissors(stage.getCamera(), transform, secondWidgetBounds, scissors[1]);
	}
	
	private void calculateHorizBoundsAndPositions() {
		NinePatch handle = style.handle;
		
		float availWidth = width - handle.getTotalWidth();
		float leftAreaWidth = (int)(availWidth * splitAmount);
		float rightAreaWidth = (availWidth - leftAreaWidth);
		float handleWidth = (handle.getTotalWidth());
		
		firstWidgetBounds.set(0, 0, leftAreaWidth, height);
		secondWidgetBounds.set(leftAreaWidth + handleWidth, 0, rightAreaWidth, height);
		handleBounds.set(leftAreaWidth, 0, handleWidth, height);			
	}
	
	private void calculateVertBoundsAndPositions() {
		NinePatch handle = style.handle;
		
		float availHeight = height - handle.getTotalHeight();
		float topAreaHeight = (int)(availHeight * splitAmount);
		float bottomAreaHeight = (availHeight - topAreaHeight);
		float handleHeight = handle.getTotalHeight();
		
		firstWidgetBounds.set(0, height - topAreaHeight, width, topAreaHeight);
		secondWidgetBounds.set(0, 0, width, bottomAreaHeight);
		handleBounds.set(0, bottomAreaHeight, width, handleHeight);			
	}
	
	@Override
	protected void draw(SpriteBatch batch, float parentAlpha) {
		NinePatch handle = style.handle;
		
		setupTransform(batch);			
		calculateBoundsAndPositions(batch.getTransformMatrix());									
		for(int i = 0; i < children.size(); i++) {
			ScissorStack.pushScissors(scissors[i]);			
			drawChild(children.get(i), batch, parentAlpha);
			ScissorStack.popScissors();
		}
		handle.draw(batch, handleBounds.x, handleBounds.y, handleBounds.width, handleBounds.height, parentAlpha);
		if(invalidated) layout();
		resetTransform(batch);
	}
	
	Vector2 lastPoint = new Vector2();
	Vector2 handlePos = new Vector2();
	@Override
	protected boolean touchDown (float x, float y, int pointer) {
		if(pointer != 0) return false;		
		if(handleBounds.contains(x, y)) {
			touchDrag = true;
			lastPoint.set(x, y);
			handlePos.set(handleBounds.x, handleBounds.y);
			focus(this, 0);
			return true;
		}
		return super.touchDown(x, y, pointer);		
	}

	@Override
	protected boolean touchUp (float x, float y, int pointer) {
		if(pointer != 0) return false;
		if(touchDrag) {
			focus(null, 0);
			touchDrag = false;
			return true;
		}
		return super.touchUp(x, y, pointer);
	}

	@Override
	protected boolean touchDragged (float x, float y, int pointer) {
		NinePatch handle = style.handle;
		
		if(pointer != 0) return false;
		if(touchDrag) {						
			if(!vertical) {
				float delta = x - lastPoint.x;	
				float availWidth = width - handle.getTotalWidth();
				float dragX = handlePos.x + delta;
				handlePos.x = dragX;
				dragX = Math.max(0, dragX);
				dragX = Math.min(availWidth, dragX);				
				splitAmount = dragX / availWidth;
				if(splitAmount < minAmount) splitAmount = minAmount;
				if(splitAmount > maxAmount) splitAmount = maxAmount;
				invalidate();
				lastPoint.set(x, y);						
			} else {
				float delta = y - lastPoint.y;
				float availHeight = height - handle.getTotalHeight();
				float dragY = handlePos.y + delta;
				handlePos.y = dragY;
				dragY = Math.max(0, dragY);
				dragY = Math.min(availHeight, dragY);				
				splitAmount = 1 - (dragY / availHeight);				
				if(splitAmount < minAmount) splitAmount = minAmount;
				if(splitAmount > maxAmount) splitAmount = maxAmount;
				invalidate();
				lastPoint.set(x, y);
			}
			return true;
		} else return super.touchDragged(x, y, pointer);
	}
	
	@Override
	public Actor hit(float x, float y) {
		return x > 0 && x < width && y > 0 && y < height?this: null;
	}
	
	public static class SplitPaneStyle {
		public final NinePatch handle;
		
		public SplitPaneStyle(NinePatch handle) {
			this.handle = handle;
		}
	}
	
	public void setSplitAmount(float split) {
		this.splitAmount = Math.max(Math.min(maxAmount, split), minAmount);
		invalidate();
	}
	
	public float getSplit() {
		return splitAmount;
	}
	
	public void setMinSplitAmount(float minAmount) {
		if(minAmount < 0) throw new GdxRuntimeException("minAmount has to be >= 0");
		if(minAmount >= maxAmount) throw new GdxRuntimeException("minAmount has to be < maxAmount");
		this.minAmount = minAmount;		
	}
	
	public void setMaxSplitAmount(float maxAmount) {
		if(maxAmount > 1) throw new GdxRuntimeException("maxAmount has to be >= 0");
		if(maxAmount <= minAmount) throw new GdxRuntimeException("maxAmount has to be > minAmount");
		this.maxAmount = maxAmount;
	}
}
