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

import com.badlogic.gdx.ai.steer.behaviors.Arrive;
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
import com.badlogic.gdx.tests.ai.steer.TargetInputProcessor;

/** A class to test and experiment with the {@link Arrive} behavior.
 * 
 * @autor davebaol */
public class ArriveTest extends SteeringTest {
	SteeringActor character;
	SteeringActor target;

	public ArriveTest (SteeringBehaviorTest container) {
		super(container, "Arrive");
	}

	@Override
	public void create (Table table) {
		character = new SteeringActor(container.badlogicSmall, false);
		target = new SteeringActor(container.target);
		inputProcessor = new TargetInputProcessor(target);

		final Arrive<Vector2> arriveSB = new Arrive<Vector2>(character, target, 100);
		arriveSB.setMaxSpeed(100);
		arriveSB.setTimeToTarget(0.1f);
		arriveSB.setArrivalTolerance(0.001f);
		arriveSB.setDecelerationRadius(80);
		character.setSteeringBehavior(arriveSB);

		table.addActor(character);
		table.addActor(target);

		character.setCenterPosition(container.stageWidth / 2, container.stageHeight / 2);
		character.setMaxSpeed(arriveSB.getMaxSpeed());
		target.setCenterPosition(MathUtils.random(container.stageWidth), MathUtils.random(container.stageHeight));

		Table detailTable = new Table(container.skin);

		detailTable.row();
		final Label labelMaxLinAcc = new Label("Max.linear.acc.[" + arriveSB.getMaxLinearAcceleration() + "]", container.skin);
		detailTable.add(labelMaxLinAcc);
		detailTable.row();
		Slider maxLinAcc = new Slider(0, 2000, 20, false, container.skin);
		maxLinAcc.setValue(arriveSB.getMaxLinearAcceleration());
		maxLinAcc.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				arriveSB.setMaxLinearAcceleration(slider.getValue());
				labelMaxLinAcc.setText("Max.linear.acc.[" + slider.getValue() + "]");
			}
		});
		detailTable.add(maxLinAcc);

		detailTable.row();
		final Label labelMaxSpeed = new Label("Max.Speed [" + arriveSB.getMaxSpeed() + "]", container.skin);
		detailTable.add(labelMaxSpeed);
		detailTable.row();
		Slider maxSpeed = new Slider(0, 300, 10, false, container.skin);
		maxSpeed.setValue(arriveSB.getMaxSpeed());
		maxSpeed.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				arriveSB.setMaxSpeed(slider.getValue());
				character.setMaxSpeed(slider.getValue());
				labelMaxSpeed.setText("Max.Speed [" + slider.getValue() + "]");
			}
		});
		detailTable.add(maxSpeed);

		detailTable.row();
		final Label labelDecelerationRadius = new Label("Deceleration Radius [" + arriveSB.getDecelerationRadius() + "]", container.skin);
		detailTable.add(labelDecelerationRadius);
		detailTable.row();
		Slider decelerationRadius = new Slider(0, 150, 1, false, container.skin);
		decelerationRadius.setValue(arriveSB.getDecelerationRadius());
		decelerationRadius.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				arriveSB.setDecelerationRadius(slider.getValue());
				labelDecelerationRadius.setText("Deceleration Radius [" + slider.getValue() + "]");
			}
		});
		detailTable.add(decelerationRadius);

		detailTable.row();
		final Label labelArrivalTolerance = new Label("Arrival tolerance [" + arriveSB.getArrivalTolerance() + "]", container.skin);
		detailTable.add(labelArrivalTolerance);
		detailTable.row();
		Slider arrivalTolerance = new Slider(0, 1, 0.0001f, false, container.skin);
		arrivalTolerance.setValue(arriveSB.getArrivalTolerance());
		arrivalTolerance.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				arriveSB.setArrivalTolerance(slider.getValue());
				labelArrivalTolerance.setText("Arrival tolerance [" + slider.getValue() + "]");
			}
		});
		detailTable.add(arrivalTolerance);

		detailTable.row();
		final Label labelTimeToTarget = new Label("Time to Target [" + arriveSB.getTimeToTarget() + " sec.]", container.skin);
		detailTable.add(labelTimeToTarget);
		detailTable.row();
		Slider timeToTarget = new Slider(0, 3, 0.1f, false, container.skin);
		timeToTarget.setValue(arriveSB.getTimeToTarget());
		timeToTarget.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				arriveSB.setTimeToTarget(slider.getValue());
				labelTimeToTarget.setText("Time to Target [" + slider.getValue() + " sec.]");
			}
		});
		detailTable.add(timeToTarget);

		detailWindow = createDetailWindow(detailTable);
	}

	@Override
	public void render () {
	}

	@Override
	public void dispose () {
	}

}
