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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/** A toggle button with an image on it.
 * 
 * <h2>Functionality</h2> A toggle button can be either in a pressed or unpressed state. A {@link ClickListener} can be registered
 * with the ImageToggleButton which will be called in case the button changed its state.
 * 
 * <h2>Layout</h2> The (preferred) width and height of an ImageToggleButton are derrived from the border patches in the background
 * {@link NinePatch} as well as the width and height of the TextureRegion of the image displayed inside the ImageToggleButton. Use
 * {@link #setPrefSize(int, int)} to programmatically change the size to your liking. In case the width and height you set are to
 * small for the contained image you will see artifacts.
 * 
 * <h2>Style</h2> An ImageToggleButton is a {@link Widget} displaying a background {@link NinePatch} as well as image in form of a
 * {@link TextureRegion}. The style is defined via an instance of {@link ImageToggleButtonStyle}, which can be either done
 * programmatically or via a {@link Skin}.</p>
 * 
 * A Button's style definition in a skin XML file should look like this:
 * 
 * <pre>
 * {@code 
 * <imagetogglebutton name="styleName" 
 *                    down="downNinePatch" 
 *                    up="upNinePatch" 
 *                    />
 * }
 * </pre>
 * 
 * <ul>
 * <li>The <code>name</code> attribute defines the name of the style which you can later use with
 * {@link Skin#newImageToggleButton(String, TextureRegion, String)}.</li>
 * <li>The <code>down</code> attribute references a {@link NinePatch} by name, to be used as the button's background when it is
 * pressed</li>
 * <li>The <code>up</code> attribute references a {@link NinePatch} by name, to be used as the button's background when it is not
 * pressed</li>
 * </ul>
 * 
 * Note that the image's TextureRegion is defined at construction time or via {@link #setImageSize(float, float)}
 * 
 * @author mzechner */
public class ImageToggleButton extends Widget {
	final ImageToggleButtonStyle style;
	TextureRegion image;
	float imageWidth;
	float imageHeight;
	final Rectangle bounds = new Rectangle();
	boolean isPressed = false;
	ClickListener listener = null;

	/** Creates a new image toggle button. The size is determined by the image's size as well as the style.
	 * @param name the name
	 * @param image the image' {@link TextureRegion}
	 * @param style the {@link ImageToggleButtonStyle} */
	public ImageToggleButton (String name, TextureRegion image, ImageToggleButtonStyle style) {
		super(name, 0, 0);
		this.style = style;
		this.image = image;
		this.imageWidth = image.getRegionWidth();
		this.imageHeight = image.getRegionHeight();
		layout();
		this.width = prefWidth;
		this.height = prefHeight;
	}

	/** Creates a new image toggle button. The size is determined by the image's size as well as the style.
	 * @param name the name
	 * @param image the image' {@link TextureRegion}
	 * @param imageWidth the image's width
	 * @param imageHeight the image's height
	 * @param style the {@link ImageToggleButtonStyle} */
	public ImageToggleButton (String name, TextureRegion image, float imageWidth, float imageHeight, ImageToggleButtonStyle style) {
		super(name, 0, 0);
		this.style = style;
		this.image = image;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		layout();
		this.width = prefWidth;
		this.height = prefHeight;
	}

	@Override
	public void layout () {
		final NinePatch downPatch = style.down;

		bounds.set(0, 0, imageWidth, imageHeight);

		prefHeight = downPatch.getBottomHeight() + downPatch.getTopHeight() + bounds.height;
		prefWidth = downPatch.getLeftWidth() + downPatch.getRightWidth() + bounds.width;
		invalidated = false;
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		final NinePatch downPatch = style.down;
		final NinePatch upPatch = style.up;

		if (invalidated) layout();

		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		if (isPressed)
			downPatch.draw(batch, x, y, width, height);
		else
			upPatch.draw(batch, x, y, width, height);

		float imageX = (width - bounds.width) * 0.5f;
		float imageY = (height - bounds.height) * 0.5f;

		batch.draw(image, x + imageX, y + imageY, imageWidth, imageHeight);
	}

	@Override
	public boolean touchDown (float x, float y, int pointer) {
		if (pointer != 0) return false;
		isPressed = !isPressed;
		if (listener != null) listener.click(this, isPressed);
		return true;
	}

	@Override
	public void touchUp (float x, float y, int pointer) {
	}

	@Override
	public void touchDragged (float x, float y, int pointer) {
	}

	/** Defines an image toggle button style, see {@link ImageToggleButton}
	 * @author mzechner */
	public static class ImageToggleButtonStyle {
		public NinePatch down;
		public NinePatch up;

		public ImageToggleButtonStyle () {
		}

		public ImageToggleButtonStyle (NinePatch down, NinePatch up) {
			this.down = down;
			this.up = up;
		}
	}

	/** Interface for listening to click events.
	 * @author mzechner */
	public interface ClickListener {
		public void click (ImageToggleButton button, boolean isPressed);
	}

	/** @return the image's {@link TextureRegion} */
	public TextureRegion getImage () {
		return image;
	}

	/** Sets the image's TextureRegion. Invalidates all parents. Keeps the previously set image size.
	 * @param image the image's {@link TextureRegion} */
	public void setImage (TextureRegion image) {
		this.image = image;
		invalidateHierarchy();
	}

	/** Sets the image's size. Invalidates all parents
	 * @param width the image's width
	 * @param height the image's height */
	public void setImageSize (float width, float height) {
		this.imageWidth = width;
		this.imageHeight = height;
		invalidateHierarchy();
	}

	/** Sets the {@link ClickListener}
	 * @param listener the listener or null
	 * @return this {@link ImageToggleButton} for chaining */
	public ImageToggleButton setClickListener (ClickListener listener) {
		this.listener = listener;
		return this;
	}

	/** @return whether this {@link ImageToggleButton} is down or not */
	public boolean isPressed () {
		return isPressed;
	}

	/** Sets whether this {@link ImageToggleButton} is down.
	 * @param isPressed whether this button is down */
	public void setPressed (boolean isPressed) {
		this.isPressed = isPressed;
	}
}
