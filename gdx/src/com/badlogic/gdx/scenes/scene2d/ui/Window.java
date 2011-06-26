package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.utils.ScissorStack;

public class Window extends Container {
	final WindowStyle style;
	String title;
	final Stage stage;
	final Rectangle widgetBounds = new Rectangle();
	final Rectangle titleBounds = new Rectangle();
	final TextBounds textBounds = new TextBounds();
	final Rectangle scissors = new Rectangle();
	boolean move = false;	
	final Vector2 initial = new Vector2();
	boolean isModal = false;
	
	public Window(String name, Stage stage, String title, int prefWidth, int prefHeight, WindowStyle style) {
		super(name, prefWidth, prefHeight);
		this.style = style;
		this.title = title;
		this.stage = stage;
		
		final NinePatch background = style.background;
		layout.padBottom = Integer.toString((int)(background.getBottomHeight()) + 1);
		layout.padTop = Integer.toString((int)(background.getTopHeight()) + 1);
		layout.padLeft = Integer.toString((int)(background.getLeftWidth()) + 1);
		layout.padRight = Integer.toString((int)(background.getRightWidth()) + 1);
	}
	
	
	private void calculateBoundsAndScissors(Matrix4 transform) {
		final NinePatch background = style.background;
		final BitmapFont titleFont = style.titleFont;
		
		widgetBounds.x = background.getLeftWidth();
		widgetBounds.y = background.getBottomHeight();
		widgetBounds.width = width - background.getLeftWidth() - background.getRightWidth();
		widgetBounds.height = height - background.getTopHeight() - background.getBottomHeight();
		ScissorStack.calculateScissors(stage.getCamera(), transform, widgetBounds, scissors);
		titleBounds.x = 0;
		titleBounds.y = height - background.getTopHeight();
		titleBounds.width = width;
		titleBounds.height = background.getTopHeight();
		textBounds.set(titleFont.getBounds(title));
		textBounds.height -= titleFont.getDescent();
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		final NinePatch backgroundPatch = style.background;
		final BitmapFont titleFont = style.titleFont;
		final Color titleFontColor = style.titleFontColor;
		
//		invalidate(); // HACK, invalidate is not properly distributed it seems! Comment, then start qpre and resize window, see options pane!
//		layout(); // HACK, since we can't access the invalidated flag of the Table we derrive from.
					
		setupTransform(batch);
		applyLayout();
		calculateBoundsAndScissors(batch.getTransformMatrix());
		
		backgroundPatch.draw(batch, 0, 0, width, height);
		float textY = height - (int)(backgroundPatch.getTopHeight() / 2) + (int)(textBounds.height / 2);
		titleFont.setColor(titleFontColor);
		titleFont.drawMultiLine(batch, title, (int)(width / 2), textY, 0, HAlignment.CENTER);
		batch.flush();
		
		ScissorStack.pushScissors(scissors);
		super.drawChildren(batch, parentAlpha);
		ScissorStack.popScissors();
		
		resetTransform(batch);
			
	}
	
	public static class WindowStyle {
		public final NinePatch background;
		public final BitmapFont titleFont;
		public final Color titleFontColor = new Color(1, 1, 1, 1);
		
		public WindowStyle(BitmapFont titleFont, Color titleFontColor, NinePatch backgroundPatch) {
			this.background = backgroundPatch;
			this.titleFont = titleFont;
			this.titleFontColor.set(titleFontColor);		
		}
	}
	
	@Override public boolean touchDown(float x, float y, int pointer) {
		if(pointer != 0) return false;
		if(hit(x, y) != null) {
			if(parent.getActors().size() > 1) parent.swapActor(this, parent.getActors().get(parent.getActors().size()-1));
			if(titleBounds.contains(x, y)) {
				focus(this, 0);
				move = true;
				initial.set(x,y);				
			} else if(!super.touchDown(x, y, pointer)) {
				focus(this, 0);
			}
			return true;
		} else {
			if(isModal && parent.getActors().size() > 1) parent.swapActor(this, parent.getActors().get(parent.getActors().size()-1));
			return isModal;
		}
	}
	
	@Override public boolean touchUp(float x, float y, int pointer) {
		if(pointer != 0) return false;
		if(parent.focusedActor[0] == this) focus(null, 0);
		move = false;
		return super.touchUp(x, y, pointer) || isModal;
	}
	
	@Override public boolean touchDragged(float x, float y, int pointer) {
		if(move) {			
			this.x += (x - initial.x);
			this.y += (y - initial.y);						
			return true;
		}
		if(parent.focusedActor[0] == this) return true;
		return super.touchDragged(x, y, pointer) || isModal;
	}
	
	@Override
	public Actor hit(float x, float y) {
		return (x > 0 && x < width && y > 0 && y < height)||isModal?this: null;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setModal(boolean isModal) {
		this.isModal = isModal;
	}
}
