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
import com.badlogic.gdx.utils.Null;

/** A listener that shows a tooltip actor when another actor is hovered over with the mouse.
 * @author Nathan Sweet */
public class Tooltip<T extends Actor> extends InputListener {
	static Vector2 tmp = new Vector2();

	private final TooltipManager manager;
	final Container<T> container;
	boolean instant, always;
	Actor targetActor;

	/** @param contents May be null. */
	public Tooltip (@Null T contents) {
		this(contents, TooltipManager.getInstance());
	}

	/** @param contents May be null. */
	public Tooltip (@Null T contents, TooltipManager manager) {
		this.manager = manager;

		container = new Container(contents) {
			public void act (float delta) {
				super.act(delta);
				if (targetActor != null && targetActor.getStage() == null) remove();
			}
		};
		container.setTouchable(Touchable.disabled);
	}

	public TooltipManager getManager () {
		return manager;
	}

	public Container<T> getContainer () {
		return container;
	}

	public void setActor (@Null T contents) {
		container.setActor(contents);
	}

	@Null
	public T getActor () {
		return container.getActor();
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
			container.toFront();
			return false;
		}
		manager.touchDown(this);
		return false;
	}

	public boolean mouseMoved (InputEvent event, float x, float y) {
		if (container.hasParent()) return false;
		setContainerPosition(event.getListenerActor(), x, y);
		return true;
	}

	private void setContainerPosition (Actor actor, float x, float y) {
		this.targetActor = actor;
		Stage stage = actor.getStage();
		if (stage == null) return;

		container.pack();
		float offsetX = manager.offsetX, offsetY = manager.offsetY, dist = manager.edgeDistance;
		Vector2 point = actor.localToStageCoordinates(tmp.set(x + offsetX, y - offsetY - container.getHeight()));
		if (point.y < dist) point = actor.localToStageCoordinates(tmp.set(x + offsetX, y + offsetY));
		if (point.x < dist) point.x = dist;
		if (point.x + container.getWidth() > stage.getWidth() - dist) point.x = stage.getWidth() - dist - container.getWidth();
		if (point.y + container.getHeight() > stage.getHeight() - dist) point.y = stage.getHeight() - dist - container.getHeight();
		container.setPosition(point.x, point.y);

		point = actor.localToStageCoordinates(tmp.set(actor.getWidth() / 2, actor.getHeight() / 2));
		point.sub(container.getX(), container.getY());
		container.setOrigin(point.x, point.y);
	}

	public void enter (InputEvent event, float x, float y, int pointer, @Null Actor fromActor) {
		if (pointer != -1) return;
		if (Gdx.input.isTouched()) return;
		Actor actor = event.getListenerActor();
		if (fromActor != null && fromActor.isDescendantOf(actor)) return;
		setContainerPosition(actor, x, y);
		manager.enter(this);
	}

	public void exit (InputEvent event, float x, float y, int pointer, @Null Actor toActor) {
		if (toActor != null && toActor.isDescendantOf(event.getListenerActor())) return;
		hide();
	}

	public void hide () {
		manager.hide(this);
	}
}
