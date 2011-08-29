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
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.TableLayout;
import com.badlogic.gdx.scenes.scene2d.ui.utils.ScissorStack;

/** A container acting as a dialog or window.
 * 
 * <h2>Functionality</h2> A Window is a {@link Table} that can be moved around by touching and dragging its titlebar.It can house
 * multiple {@link Actor} instances in a table-layout. The difference to a pure Container is that the Window will automatically
 * set the padding of the layout to respect the width and height of the border patches of its background NinePatch. See
 * {@link Table} for more information on how Actor instances are laid out when using this class.</p>
 * 
 * A Window can also be set to be modal via a call to {@link #setModal(boolean)}, in which case all touch input will go to that
 * window no matter where the user touched the screen.
 * 
 * <h2>Layout</h2> The (preferred) width and height are determined by the values given in the constructor of this class. Please
 * consult the {@link Table} documentation on how the width and height will be manipulated if the Window is contained in another
 * Container, a not so common use case. Additionally you can set the (preferred) width and height via a call to
 * {@link TableLayout#size(int, int)}.
 * 
 * <h2>Style</h2> A Window is a {@link Table} displaying a background {@link NinePatch} and its child Actors, clipped to the
 * Window's area, taking into account the padding as described in the functionality section. Additionally the window will render a
 * title string in its top border patches. The style is defined via an instance of {@link WindowStyle}, which can be either done
 * programmatically or via a {@link Skin}.</p>
 * 
 * A Pane's style definition in a skin XML file should look like this:
 * 
 * <pre>
 * {@code 
 * <window name="name"
 *         titleFont="fontName" 
 *         titleFontColor="fontColor" 
 *         background="backgroundPatch"/>
 * }
 * </pre>
 * 
 * <ul>
 * <li>The <code>name</code> attribute defines the name of the style which you can later use with
 * {@link Skin#newWindow(String, Stage, String, int, int, String)}.</li>
 * <li>The <code>titleFont</code> attribute references a {@link BitmapFont} by name, to be used to render the title string.</li>
 * *
 * <li>The <code>titleFontColor</code> attribute references a {@link Color} by name, to be used to render the title string.</li>
 * <li>The <code>background</code> attribute references a {@link NinePatch} by name, to be used as the Window's background.</li> *
 * </ul>
 * 
 * @author mzechner */
public class Window extends Table {
	final WindowStyle style;
	String title;
	final Stage stage;
	final Rectangle widgetBounds = new Rectangle();
	final Rectangle titleBounds = new Rectangle();
	final TextBounds textBounds = new TextBounds();
	final Rectangle scissors = new Rectangle();
	boolean move = false;
	boolean isMovable = true;
	final Vector2 initial = new Vector2();
	boolean isModal = false;

	/** Creates a new Window. The width and height are determined by the given parameters.
	 * @param name the name
	 * @param stage the {@link Stage}, used for clipping
	 * @param title the title
	 * @param prefWidth the (preferred) width
	 * @param prefHeight the (preferred) height
	 * @param style the {@link WindowStyle} */
	public Window (String name, Stage stage, String title, int prefWidth, int prefHeight, WindowStyle style) {
		super(name);
		this.stage = stage;
		this.title = title;
		width = prefWidth;
		height = prefHeight;
		this.style = style;

		transform = true;

		final NinePatch background = style.background;
		TableLayout layout = getTableLayout();
		layout.padBottom(Integer.toString((int)(background.getBottomHeight()) + 1));
		layout.padTop(Integer.toString((int)(background.getTopHeight()) + 1));
		layout.padLeft(Integer.toString((int)(background.getLeftWidth()) + 1));
		layout.padRight(Integer.toString((int)(background.getRightWidth()) + 1));
	}

	private void calculateBoundsAndScissors (Matrix4 transform) {
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
	public void draw (SpriteBatch batch, float parentAlpha) {
		final NinePatch backgroundPatch = style.background;
		final BitmapFont titleFont = style.titleFont;
		final Color titleFontColor = style.titleFontColor;

		layout();
		applyTransform(batch);
		calculateBoundsAndScissors(batch.getTransformMatrix());

		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		backgroundPatch.draw(batch, 0, 0, width, height);
		float textY = height - (int)(backgroundPatch.getTopHeight() / 2) + (int)(textBounds.height / 2);
		titleFont.setColor(titleFontColor.r, titleFontColor.g, titleFontColor.b, titleFontColor.a * parentAlpha);
		titleFont.drawMultiLine(batch, title, (int)(width / 2), textY, 0, HAlignment.CENTER);
		batch.flush();

		ScissorStack.pushScissors(scissors);
		super.drawChildren(batch, parentAlpha);
		ScissorStack.popScissors();

		resetTransform(batch);

	}

	/** Defines the style of a window, see {@link Window}
	 * @author mzechner */
	public static class WindowStyle {
		public NinePatch background;
		public BitmapFont titleFont;
		public Color titleFontColor = new Color(1, 1, 1, 1);

		public WindowStyle () {
		}

		public WindowStyle (BitmapFont titleFont, Color titleFontColor, NinePatch backgroundPatch) {
			this.background = backgroundPatch;
			this.titleFont = titleFont;
			this.titleFontColor.set(titleFontColor);
		}
	}

	@Override
	public boolean touchDown (float x, float y, int pointer) {
		if (pointer != 0) return false;
		if (parent.getActors().size() > 1) parent.swapActor(this, parent.getActors().get(parent.getActors().size() - 1));
		if (titleBounds.contains(x, y)) {
			if (isMovable) move = true;
			initial.set(x, y);
		} else
			super.touchDown(x, y, pointer);
		return true;
	}

	@Override
	public void touchUp (float x, float y, int pointer) {
		move = false;
		if (parent.focusedActor[0] != this) super.touchUp(x, y, pointer);
	}

	@Override
	public void touchDragged (float x, float y, int pointer) {
		if (move) {
			this.x += (x - initial.x);
			this.y += (y - initial.y);
			return;
		}
		if (parent.focusedActor[0] != this) super.touchDragged(x, y, pointer);
	}

	@Override
	public Actor hit (float x, float y) {
		return (x > 0 && x < width && y > 0 && y < height) || isModal ? this : null;
	}

	/** Sets the title of the Window
	 * @param title the title */
	public void setTitle (String title) {
		this.title = title;
	}

	/** @return the title of the window */
	public String getTitle () {
		return title;
	}

	/** Sets whether this Window is movable by touch or not. In case it is the user will be able to grab and move the window by its
	 * title bar
	 * @param isMovable whether the window is movable or not */
	public void setMovable (boolean isMovable) {
		this.isMovable = isMovable;
	}

	/** @return whether the window is movable */
	public boolean isMovable () {
		return isMovable;
	}

	/** Sets whether this Window is modal or not. In case it is it will receive all touch events, no matter where the user touched
	 * the screen.
	 * @param isModal whether the window is modal or not */
	public void setModal (boolean isModal) {
		this.isModal = isModal;
	}

	/** @return whether the window is modal */
	public boolean isModal () {
		return isModal;
	}
}
