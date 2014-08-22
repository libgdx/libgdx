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

import com.badlogic.gdx.ai.steer.behaviors.Face;
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

/** A class to test and experiment with the {@link Face} behavior.
 * 
 * @autor davebaol */
public class FaceTest extends SteeringTest {
	SteeringActor character;
	SteeringActor target;

	public FaceTest (SteeringBehaviorTest container) {
		super(container, "Face");
	}

	@Override
	public void create (Table table) {
		character = new SteeringActor(container.badlogicSmall, true);
		target = new SteeringActor(container.target);
		inputProcessor = new TargetInputProcessor(target);

		final Face<Vector2> faceSB = new Face<Vector2>(character, target) //
			.setMaxAngularAcceleration(100) //
			.setMaxAngularSpeed(5) //
			.setTimeToTarget(0.1f) //
			.setAlignTolerance(0.001f) //
			.setDecelerationRadius(MathUtils.degreesToRadians * 7);
		character.setSteeringBehavior(faceSB);

		table.addActor(character);
		table.addActor(target);

		character.setCenterPosition(container.stageWidth / 2, container.stageHeight / 2);
		target.setCenterPosition(MathUtils.random(container.stageWidth), MathUtils.random(container.stageHeight));

		Table detailTable = new Table(container.skin);

		detailTable.row();
		final Label labelMaxRotation = new Label("Max.Rotation [" + faceSB.getMaxAngularSpeed() + "]", container.skin);
		detailTable.add(labelMaxRotation);
		detailTable.row();
		Slider maxRotation = new Slider(0, 20, 1, false, container.skin);
		maxRotation.setValue(faceSB.getMaxAngularSpeed());
		maxRotation.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				faceSB.setMaxAngularSpeed(slider.getValue());
// character.setMaxSpeed(slider.getValue());
				labelMaxRotation.setText("Max.Rotation [" + slider.getValue() + "]");
			}
		});
		detailTable.add(maxRotation);

		detailTable.row();
		final Label labelDecelerationRadius = new Label("Deceleration Radius [" + faceSB.getDecelerationRadius() + "]",
			container.skin);
		detailTable.add(labelDecelerationRadius);
		detailTable.row();
		Slider decelerationRadius = new Slider(0, MathUtils.PI2, MathUtils.degreesToRadians, false, container.skin);
		decelerationRadius.setValue(faceSB.getDecelerationRadius());
		decelerationRadius.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				System.out.println("Deceleration radius changed!!!");
				Slider slider = (Slider)actor;
				faceSB.setDecelerationRadius(slider.getValue());
				labelDecelerationRadius.setText("Deceleration Radius [" + slider.getValue() + "]");
			}
		});
		detailTable.add(decelerationRadius);

		detailTable.row();
		final Label labelAlignTolerance = new Label("Align tolerance [" + faceSB.getAlignTolerance() + "]", container.skin);
		detailTable.add(labelAlignTolerance);
		detailTable.row();
		Slider alignTolerance = new Slider(0, 1, 0.0001f, false, container.skin);
		alignTolerance.setValue(faceSB.getAlignTolerance());
		alignTolerance.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				faceSB.setAlignTolerance(slider.getValue());
				labelAlignTolerance.setText("Align tolerance [" + slider.getValue() + "]");
			}
		});
		detailTable.add(alignTolerance);

		detailTable.row();
		final Label labelTimeToTarget = new Label("Time to Target [" + faceSB.getTimeToTarget() + " sec.]", container.skin);
		detailTable.add(labelTimeToTarget);
		detailTable.row();
		Slider timeToTarget = new Slider(0, 3, 0.1f, false, container.skin);
		timeToTarget.setValue(faceSB.getTimeToTarget());
		timeToTarget.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				faceSB.setTimeToTarget(slider.getValue());
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
