/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/** A listener that shows a tooltip table when an actor is hovered over with the mouse.
 * @author Nathan Sweet */
public class Tooltip extends InputListener {
	static Vector2 tmp = new Vector2();

	private final TooltipManager manager;
	private TooltipStyle style;
	final Table table;
	final Label label;
	boolean instant, always;
	Actor targetActor;

	public Tooltip (String text, Skin skin) {
		this(text, TooltipManager.getInstance(), skin.get(TooltipStyle.class));
	}

	public Tooltip (String text, Skin skin, String styleName) {
		this(text, TooltipManager.getInstance(), skin.get(styleName, TooltipStyle.class));
	}

	public Tooltip (String text, TooltipStyle style) {
		this(text, TooltipManager.getInstance(), style);
	}

	public Tooltip (String text, TooltipManager manager, Skin skin) {
		this(text, manager, skin.get(TooltipStyle.class));
	}

	public Tooltip (String text, TooltipManager manager, Skin skin, String styleName) {
		this(text, manager, skin.get(styleName, TooltipStyle.class));
	}

	public Tooltip (String text, final TooltipManager manager, TooltipStyle style) {
		this.manager = manager;
		label = new Label(text, style.label);
		label.setWrap(true);
		label.setWidth(300);
		label.validate();

		table = new Table() {
			public void act (float delta) {
				super.act(delta);
				if (targetActor != null && targetActor.getStage() == null) remove();
			}
		};
		table.setBackground(style.background);
		table.setTransform(true);
		table.setTouchable(Touchable.disabled);
		table.defaults().left().pad(-4, 0, -1, 0);
		table.add(label).width(new Value() {
			public float get (Actor context) {
				return Math.min(manager.maxWidth, label.getGlyphLayout().width);
			}
		});
		table.pack();
		table.pack();
	}

	public void setStyle (TooltipStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		if (style.label == null) throw new IllegalArgumentException("Missing TooltipStyle label.");
		this.style = style;

		label.setStyle(style.label);
		table.setBackground(style.background);
		table.pack();
		table.pack();
	}

	public TooltipManager getManager () {
		return manager;
	}

	public Table getTable () {
		return table;
	}

	public Label getLabel () {
		return label;
	}

	/** If true, this tooltip is shown without delay when hovered. */
	public void setInstant (boolean instant) {
		this.instant = instant;
	}

	/** If true, this tooltip is shown even when tooltips are not {@link TooltipManager#enabled}. */
	public void setAlways (boolean always) {
		this.always = always;
	}

	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
		if (instant) {
			table.toFront();
			return false;
		}
		manager.touchDown(this);
		return false;
	}

	public boolean mouseMoved (InputEvent event, float x, float y) {
		if (table.hasParent()) return false;
		setTablePosition(event.getListenerActor(), x, y);
		return true;
	}

	private void setTablePosition (Actor actor, float x, float y) {
		this.targetActor = actor;
		Stage stage = actor.getStage();
		if (stage == null) return;

		Vector2 point = actor.localToStageCoordinates(tmp.set(x + 15, y - 19 - table.getHeight()));
		if (point.y < 7) point = actor.localToStageCoordinates(tmp.set(x + 15, y + 19));
		if (point.x < 7) point.x = 7;
		if (point.x + table.getWidth() > stage.getWidth() - 7) point.x = stage.getWidth() - 7 - table.getWidth();
		if (point.y + table.getHeight() > stage.getHeight() - 7) point.y = stage.getHeight() - 7 - table.getHeight();
		table.setPosition(point.x, point.y);

		point = actor.localToStageCoordinates(tmp.set(actor.getWidth() / 2, actor.getHeight() / 2));
		point.sub(table.getX(), table.getY());
		table.setOrigin(point.x, point.y);
	}

	public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
		if (pointer != -1) return;
		if (Gdx.input.isTouched()) return;
		Actor actor = event.getListenerActor();
		if (fromActor != null && fromActor.isDescendantOf(actor)) return;
		setTablePosition(actor, x, y);
		table.setScale(2);
		manager.enter(this);
	}

	public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
		if (toActor != null && toActor.isDescendantOf(event.getListenerActor())) return;
		hide();
	}

	public void hide () {
		manager.hide(this);
	}

	/** The style for a label, see {@link Label}.
	 * @author Nathan Sweet */
	static public class TooltipStyle {
		public LabelStyle label;
		/** Optional. */
		public Drawable background;

		public TooltipStyle () {
		}

		public TooltipStyle (LabelStyle label, Drawable background) {
			this.label = label;
			this.background = background;
		}

		public TooltipStyle (TooltipStyle style) {
			this.label = new LabelStyle(style.label);
			background = style.background;
		}
	}
}
