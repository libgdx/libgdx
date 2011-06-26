package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Button extends Widget {
	final ButtonStyle style;
	String text;	
	final TextBounds bounds = new TextBounds();	
	boolean isPressed = false;	
	ClickListener listener = null;
	
	public Button(String name, String label, ButtonStyle style) {
		super(name, 0, 0);		
		this.style = style;
		this.text = label;
		layout();
		this.width = prefWidth;
		this.height = prefHeight;
	}		
	
	@Override
	public void layout() {	
		final BitmapFont font = style.font;
		final NinePatch downPatch = style.down;
		bounds.set(font.getMultiLineBounds(text));
		bounds.height -= font.getDescent();
		
		prefHeight = downPatch.getBottomHeight() + downPatch.getTopHeight() + bounds.height;
		prefWidth = downPatch.getLeftWidth() + downPatch.getRightWidth() + bounds.width;
		invalidated = false;
	}
	
	@Override
	protected void draw(SpriteBatch batch, float parentAlpha) {
		final BitmapFont font = style.font;
		final Color fontColor = style.fontColor;
		final NinePatch downPatch = style.down;		
		final NinePatch upPatch = style.up;
		
		if(invalidated) layout();
		if(isPressed) downPatch.draw(batch, x, y, width, height, parentAlpha);
		else upPatch.draw(batch, x, y, width, height, parentAlpha);
				
		float textY = (int)(height * 0.5f) + (int)(bounds.height * 0.5f);
		font.setColor(fontColor);
		font.drawMultiLine(batch, text, x + (int)(width * 0.5f), y + textY, 0, HAlignment.CENTER);
	}

	@Override
	protected boolean touchDown(float x, float y, int pointer) {
		if(pointer != 0) return false;
		if(hit(x, y) != null) {
			isPressed = true;
			parent.focus(this, pointer);	
			return true;
		}
		return false;
	}

	@Override
	protected boolean touchUp(float x, float y, int pointer) {
		if(pointer != 0) return false;
		if(hit(x, y) != null) {			
			if(listener != null) listener.click(this);				
			parent.focus(null, pointer);
			isPressed = false;
			return true;
		}		
		isPressed = false;
		parent.focus(null, pointer);
		return false;
	}

	@Override
	protected boolean touchDragged(float x, float y, int pointer) {
		if(pointer != 0) return false;		
		return isPressed;
	}	
	
	public static class ButtonStyle {
		public final NinePatch down;
		public final NinePatch up;
		public final BitmapFont font;
		public final Color fontColor;	
		
		public ButtonStyle(BitmapFont font, Color fontColor, NinePatch down, NinePatch up) {
			this.font = font;
			this.fontColor = fontColor;
			this.down = down;
			this.up = up;
		}
	}
	
	public interface ClickListener {
		public void click(Button button);
	}
	
	public void setText(String text) {
		this.text = text;
		invalidateHierarchy();
	}
	
	public String getText() {
		return text;
	}
	
	public Button setClickListener(ClickListener listener) {
		this.listener = listener;
		return this;
	}
}
