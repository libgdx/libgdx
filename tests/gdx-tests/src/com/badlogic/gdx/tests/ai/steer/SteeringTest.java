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

package com.badlogic.gdx.tests.ai.steer;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.SteeringBehaviorTest;
import com.badlogic.gdx.tests.ai.steer.bullet.SteeringBulletEntity;
import com.badlogic.gdx.tests.g3d.BaseG3dHudTest.CollapsableWindow;
import com.badlogic.gdx.utils.Array;

public abstract class SteeringTest {
	protected SteeringBehaviorTest container;
	public String name;
	protected InputProcessor inputProcessor;
	protected CollapsableWindow detailWindow;

	public SteeringTest (SteeringBehaviorTest container, String name) {
		this(container, name, null);
	}

	public SteeringTest (SteeringBehaviorTest container, String name, InputProcessor inputProcessor) {
		this.container = container;
		this.name = name;
		this.inputProcessor = inputProcessor;
	}

	public abstract void create (Table table);

	public abstract void render ();

	public abstract void dispose ();

	public InputProcessor getInputProcessor () {
		return inputProcessor;
	}

	public void setInputProcessor (InputProcessor inputProcessor) {
		this.inputProcessor = inputProcessor;
	}

	public CollapsableWindow getDetailWindow () {
		return detailWindow;
	}

	protected CollapsableWindow createDetailWindow (Table table) {
		CollapsableWindow window = new CollapsableWindow(this.name, container.skin);
		window.row();
		window.add(table);
		window.pack();
		window.layout();
		window.collapse();
		return window;
	}

	protected void addMaxSpeedController (Table table, final SteeringActor character) {
		addMaxSpeedController(table, character, 0, 500, 10);
	}

	protected void addMaxSpeedController (Table table, final SteeringActor character, float minValue, float maxValue, float step) {
		final Label labelMaxSpeed = new Label("Max Speed [" + character.getMaxSpeed() + "]", container.skin);
		table.add(labelMaxSpeed);
		table.row();
		Slider maxSpeed = new Slider(minValue, maxValue, step, false, container.skin);
		maxSpeed.setValue(character.getMaxSpeed());
		maxSpeed.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				character.setMaxSpeed(slider.getValue());
				labelMaxSpeed.setText("Max Speed [" + character.getMaxSpeed() + "]");
			}
		});
		table.add(maxSpeed);
	}
	
	protected void addMaxSpeedController (Table table, final SteeringBulletEntity character) {
		addMaxSpeedController(table, character, 0, 50, 0.1f);
	}
	
	protected void addMaxSpeedController (Table table, final SteeringBulletEntity character, float minValue, float maxValue, float step) {
		final Label labelMaxSpeed = new Label("Max Speed [" + character.getMaxSpeed() + "]", container.skin);
		table.add(labelMaxSpeed);
		table.row();
		Slider maxSpeed = new Slider(minValue, maxValue, step, false, container.skin);
		maxSpeed.setValue(character.getMaxSpeed());
		maxSpeed.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				character.setMaxSpeed(slider.getValue());
				labelMaxSpeed.setText("Max Speed [" + character.getMaxSpeed() + "]");
			}
		});
		table.add(maxSpeed);
	}

	protected void addAlignOrientationToLinearVelocityController (Table table, final SteeringActor character) {
		CheckBox alignOrient = new CheckBox("Align orient.to velocity", container.skin);
		alignOrient.setChecked(character.isIndependentFacing());
		alignOrient.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				CheckBox checkBox = (CheckBox)event.getListenerActor();
				character.setIndependentFacing(checkBox.isChecked());
			}
		});
		table.add(alignOrient);
	}

	protected void addSeparator (Table table) {
		Label lbl = new Label("", container.skin);
		lbl.setColor(0.75f, 0.75f, 0.75f, 1);
		lbl.setStyle(new LabelStyle(lbl.getStyle()));
		lbl.getStyle().background = container.skin.newDrawable("white");
		table.add(lbl).colspan(2).height(1).width(220).pad(5, 1, 5, 1);
	}

	protected void setRandomNonOverlappingPosition (SteeringActor character, Array<SteeringActor> others,
		float minDistanceFromBoundary) {
		SET_NEW_POS:
		while (true) {
			character.setCenterPosition(MathUtils.random(container.stageWidth), MathUtils.random(container.stageHeight));
			character.getPosition().set(character.getCenterX(), character.getCenterY());
			for (int i = 0; i < others.size; i++) {
				SteeringActor other = (SteeringActor)others.get(i);
				if (character.getPosition().dst(other.getPosition()) <= character.getBoundingRadius() + other.getBoundingRadius()
					+ minDistanceFromBoundary) continue SET_NEW_POS;
			}
			return;
		}
	}
}
