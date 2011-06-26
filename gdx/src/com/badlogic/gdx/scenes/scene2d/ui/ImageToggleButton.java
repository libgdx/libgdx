package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class ImageToggleButton extends Widget {
	final ImageToggleButtonStyle style;
	TextureRegion image;
	final Rectangle bounds = new Rectangle();	
	boolean isPressed = false;	
	ClickListener listener = null;
	
	public ImageToggleButton(String name, TextureRegion image, ImageToggleButtonStyle style) {
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
		if (pointer != 0)
			return false;
		if (hit(x, y) != null) {
			isPressed = !isPressed;			
			if(listener != null) listener.click(this, isPressed);
			return true;
		}
		return false;
	}

	@Override
	protected boolean touchUp(float x, float y, int pointer) {		
		return false;
	}

	@Override
	protected boolean touchDragged(float x, float y, int pointer) {
		return false;
	}	
	
	public static class ImageToggleButtonStyle {
		public final NinePatch down;
		public final NinePatch up;
				
		public ImageToggleButtonStyle(NinePatch down, NinePatch up) {			
			this.down = down;
			this.up = up;
		}
	}
	
	public interface ClickListener {
		public void click(ImageToggleButton button, boolean isPressed);
	}
	
	public TextureRegion getImage() {
		return image;
	}
	
	public void setImage(TextureRegion image) {
		this.image = image;
		invalidateHierarchy();
	}
	
	public ImageToggleButton setClickListener(ClickListener listener) {
		this.listener = listener;
		return this;
	}
	
	public boolean isPressed() {
		return isPressed;
	}
	
	public void setPressed(boolean isPressed) {
		this.isPressed = isPressed;
	}	
}
