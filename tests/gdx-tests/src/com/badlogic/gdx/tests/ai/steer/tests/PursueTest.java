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

package com.badlogic.gdx.tests.ai.steer.tests;

import com.badlogic.gdx.ai.steer.behaviors.Pursue;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.tests.SteeringBehaviorTest;
import com.badlogic.gdx.tests.ai.steer.SteeringActor;
import com.badlogic.gdx.tests.ai.steer.SteeringTest;

/** A class to test and experiment with the {@link Pursue} behavior.
 * 
 * @autor davebaol */
public class PursueTest extends SteeringTest {
	SteeringActor character;
	SteeringActor prey;

	public PursueTest (SteeringBehaviorTest container) {
		super(container, "Pursue");
	}

	@Override
	public void create (Table table) {
		character = new SteeringActor(container.badlogicSmall, false);
		prey = new SteeringActor(container.target, false);

		final Pursue<Vector2> pursueSB = new Pursue<Vector2>(character, prey, 600, 0.3f);
		character.setSteeringBehavior(pursueSB);

		Wander<Vector2> wanderSB = new Wander<Vector2>(prey) //
			.setMaxLinearAcceleration(250) //
			.setMaxAngularAcceleration(0) // set to 0 because independent facing is disabled
			.setAlignTolerance(0.001f) //
			.setDecelerationRadius(5) //
			.setMaxAngularSpeed(5) //
			.setTimeToTarget(0.1f) //
			.setWanderOffset(110) //
			.setWanderOrientation(10) //
			.setWanderRadius(64) //
			.setWanderRate(MathUtils.PI / 6);
		prey.setSteeringBehavior(wanderSB);

		table.addActor(character);
		table.addActor(prey);

		character.setCenterPosition(MathUtils.random(container.stageWidth), MathUtils.random(container.stageHeight));
		character.setMaxSpeed(100);
		prey.setCenterPosition(MathUtils.random(container.stageWidth), MathUtils.random(container.stageHeight));
		prey.setMaxSpeed(100);

		Table detailTable = new Table(container.skin);

		detailTable.row();
		final Label labelMaxLinAcc = new Label("Max.linear.acc.[" + pursueSB.getMaxLinearAcceleration() + "]", container.skin);
		detailTable.add(labelMaxLinAcc);
		detailTable.row();
		Slider maxLinAcc = new Slider(0, 2000, 20, false, container.skin);
		maxLinAcc.setValue(pursueSB.getMaxLinearAcceleration());
		maxLinAcc.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				pursueSB.setMaxLinearAcceleration(slider.getValue());
				labelMaxLinAcc.setText("Max.linear.acc.[" + slider.getValue() + "]");
			}
		});
		detailTable.add(maxLinAcc);

		detailTable.row();
		final Label labelMaxPredictionTime = new Label("Max.Prediction Time[" + pursueSB.getMaxPredictionTime() + "] sec.",
			container.skin);
		detailTable.add(labelMaxPredictionTime);
		detailTable.row();
		Slider maxPredictionTime = new Slider(0, 5, .1f, false, container.skin);
		maxPredictionTime.setValue(pursueSB.getMaxPredictionTime());
		maxPredictionTime.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				pursueSB.setMaxPredictionTime(slider.getValue());
				labelMaxPredictionTime.setText("Max.Prediction Time [" + slider.getValue() + "] sec.");
			}
		});
		detailTable.add(maxPredictionTime);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		final Label labelMaxSpeed = new Label("Max.Speed [" + character.getMaxSpeed() + "]", container.skin);
		detailTable.add(labelMaxSpeed);
		detailTable.row();
		Slider maxSpeed = new Slider(0, 300, 10, false, container.skin);
		maxSpeed.setValue(character.getMaxSpeed());
		maxSpeed.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				character.setMaxSpeed(slider.getValue());
				labelMaxSpeed.setText("Max.Speed [" + slider.getValue() + "]");
			}
		});
		detailTable.add(maxSpeed);

		detailWindow = createDetailWindow(detailTable);
	}

	@Override
	public void render () {
	}

	@Override
	public void dispose () {
	}

}
