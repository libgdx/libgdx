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
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.utils.ScissorStack;

/**
 * <h2>Functionality</h2>
 * A Pane is a {@link Container} displaying a background {@link NinePatch}. It can house
 * multiple {@link Actor} instances in a table-layout. The difference to a pure Container is
 * that the Pane will automatically set the padding of the layout to respect the width and height
 * of the border patches of its background NinePatch. See {@link Container} for more information on
 * how Actor instances are laid out when using this class. </p>
 * 
 * In addition to the basic functionality provided by the Container super class, a Pane will also
 * employ scissoring (clipping) to ensure that no contained Actor can render outside of its bounds.
 * 
 * <h2>Layout</h2>
 * The (preferred) width and height are determined by the values given in the constructor of this class. 
 * Please consult the {@link Container} documentation on how the width and height will be manipulated
 * if the Pane is contained in another Container. Additionally you can set the (preferred) width and height
 * via a call to {@link #setPrefSize(float, float)}.
 * 
 * <h2>Style</h2>
 * A Pane is a {@link Container} displaying a background {@link NinePatch} and its child Actors, clipped to the
 * Pane's area, taking into account the padding as described in the functionality section. The style is defined via
 * an instance of {@link PaneStyle}, which can be either done programmatically or via a {@link Skin}.</p>
 * 
 * A Pane's style definition in a skin XML file should look like this:
 * 
 * <pre>
 * {@code 
 *    <pane name="name" 
 *          background="backgroundPatch"/>
 * }
 * </pre>
 * 
 * <ul>
 * <li>The <code>styleName</code> attribute defines the name of the style which you can later use with {@link Skin#newPane(String, Stage, int, int, String)}.</li>
 * <li>The <code>backgroundPatch</code> attribute references a {@link NinePatch} by name, to be used as the Pane's background.</li> * 
 * </ul> 
 * @author mzechner
 *
 */
public class Pane extends Container {	
	final PaneStyle style;
	final Stage stage;
	final Rectangle widgetBounds = new Rectangle();
	final Rectangle scissors = new Rectangle();
	
	/**
	 * Creates a new Pane. The width and height are determined by the 
	 * arguments passed to the constructor.
	 * @param name the name
	 * @param stage the stage used for clipping
	 * @param prefWidth the width 
	 * @param prefHeight the height
	 * @param style the {@link PaneStyle}
	 */
	public Pane(String name, Stage stage, int prefWidth, int prefHeight, PaneStyle style) {
		super(name, prefWidth, prefHeight);
		this.style = style;
		this.stage = stage;
		
		final NinePatch background = style.background;
		layout.padBottom = Integer.toString((int)(background.getBottomHeight()) + 1);
		layout.padTop = Integer.toString((int)(background.getTopHeight()) + 1);
		layout.padLeft = Integer.toString((int)(background.getLeftWidth()) + 1);
		layout.padRight = Integer.toString((int)(background.getRightWidth()) + 1);
	}	
	
	private void calculateScissors(Matrix4 transform) {
		final NinePatch background = style.background;
		
		widgetBounds.x = background.getLeftWidth();
		widgetBounds.y = background.getBottomHeight();
		widgetBounds.width = width - background.getLeftWidth() - background.getRightWidth();
		widgetBounds.height = height - background.getTopHeight() - background.getBottomHeight();
		ScissorStack.calculateScissors(stage.getCamera(), transform, widgetBounds, scissors);
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		final NinePatch background = style.background;
		
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		background.draw(batch, x, y, width, height);		
		setupTransform(batch);
		applyLayout();
		calculateScissors(batch.getTransformMatrix());
		ScissorStack.pushScissors(scissors);
		super.drawChildren(batch, parentAlpha);
		resetTransform(batch);
		ScissorStack.popScissors();		
	}
	
	/**
	 * Defines the style of a pane, see {@link Pane}
	 * @author mzechner
	 *
	 */
	public static class PaneStyle {
		public final NinePatch background;
		
		public PaneStyle(NinePatch backgroundPatch) {
			this.background = backgroundPatch;
		}
	}
}
