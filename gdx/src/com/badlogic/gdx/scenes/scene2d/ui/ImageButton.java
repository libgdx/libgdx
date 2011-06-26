package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class ImageButton extends Widget {
	final ImageButtonStyle style;
	TextureRegion image;
	final Rectangle bounds = new Rectangle();	
	boolean isPressed = false;	
	ClickListener listener = null;
	
	public ImageButton(String name, TextureRegion image, ImageButtonStyle style) {
		super(name, 0, 0);		
		this.style = style;
		this.image = image;
		layout();
		this.width = prefWidth;
		this.height = prefHeight;
	}		
	
	@Override
	public void layout() {			
		final NinePatch downPatch = style.down;		
		
		bounds.set(0, 0, image.getRegionWidth(), image.getRegionHeight());
		
		prefHeight = downPatch.getBottomHeight() + downPatch.getTopHeight() + bounds.height;
		prefWidth = downPatch.getLeftWidth() + downPatch.getRightWidth() + bounds.width;
		invalidated = false;
	}
	
	@Override
	protected void draw(SpriteBatch batch, float parentAlpha) {		
		final NinePatch downPatch = style.down;		
		final NinePatch upPatch = style.up;
		
		if(invalidated) layout();
		if(isPressed) downPatch.draw(batch, x, y, width, height, parentAlpha);
		else upPatch.draw(batch, x, y, width, height, parentAlpha);
		
		float imageX = (width - bounds.width) * 0.5f;
		float imageY = (height - bounds.height) * 0.5f;
		
		batch.draw(image, x + imageX, y + imageY);
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
	
	public static class ImageButtonStyle {
		public final NinePatch down;
		public final NinePatch up;
				
		public ImageButtonStyle(NinePatch down, NinePatch up) {			
			this.down = down;
			this.up = up;
		}
	}
	
	public interface ClickListener {
		public void click(ImageButton button);
	}
	
	public TextureRegion getImage() {
		return image;
	}
	
	public void setImage(TextureRegion image) {
		this.image = image;
		invalidateHierarchy();
	}
	
	public ImageButton setClickListener(ClickListener listener) {
		this.listener = listener;
		return this;
	}
}
