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

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.behaviors.Alignment;
import com.badlogic.gdx.ai.steer.behaviors.Cohesion;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.Separation;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.steer.behaviors.BlendedSteering;
import com.badlogic.gdx.ai.steer.proximities.FieldOfViewProximity;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.SteeringBehaviorTest;
import com.badlogic.gdx.tests.ai.steer.SteeringActor;
import com.badlogic.gdx.tests.ai.steer.SteeringTest;
import com.badlogic.gdx.utils.Array;

/** A class to test and experiment with flocking behavior which consists of {@link Separation}, {@link Cohesion} and
 * {@link Alignment}.
 * 
 * @autor davebaol */
public class FlockingTest extends SteeringTest {
	Array<SteeringActor> characters;
	boolean drawDebug;
	ShapeRenderer shapeRenderer;
	Array<BlendedSteering<Vector2>> blendedSteerings;
	FieldOfViewProximity<Vector2> char0Proximity;
	Array<FieldOfViewProximity<Vector2>> proximities;

	public FlockingTest (SteeringBehaviorTest container) {
		super(container, "Flocking");
	}

	@Override
	public void create (Table table) {
		drawDebug = true;

		shapeRenderer = new ShapeRenderer();

		characters = new Array<SteeringActor>();
		blendedSteerings = new Array<BlendedSteering<Vector2>>();
		proximities = new Array<FieldOfViewProximity<Vector2>>();

		for (int i = 0; i < 60; i++) {
			SteeringActor character = new SteeringActor(container.greenFish, false);

			FieldOfViewProximity<Vector2> proximity = new FieldOfViewProximity<Vector2>(character, characters, 150,
				270 * MathUtils.degreesToRadians);
			proximities.add(proximity);
			if (i == 0) char0Proximity = proximity;
			Alignment<Vector2> groupAlignmentSB = new Alignment<Vector2>(character, proximity);
			Separation<Vector2> groupSeparationSB = new Separation<Vector2>(character, proximity);
			Cohesion<Vector2> groupCohesionSB = new Cohesion<Vector2>(character, proximity);

			BlendedSteering<Vector2> blendedSteering = new BlendedSteering<Vector2>(character, 500, 500) //
				.add(groupAlignmentSB, 2f) //
				.add(groupCohesionSB, 45f) //
				.add(groupSeparationSB, 350f);
			blendedSteerings.add(blendedSteering);

			Wander<Vector2> wanderSB = new Wander<Vector2>(character) //
				.setMaxLinearAcceleration(30) //
				.setMaxAngularAcceleration(0) // set to 0 because independent facing is disabled
				.setAlignTolerance(0.001f) //
				.setDecelerationRadius(5) //
				.setMaxRotation(5) //
				.setTimeToTarget(0.1f) //
				.setWanderOffset(60) //
				.setWanderOrientation(10) //
				.setWanderRadius(40) //
				.setWanderRate(MathUtils.PI / 5);

			PrioritySteering<Vector2> prioritySteeringSB = new PrioritySteering<Vector2>(character, 0.0001f) //
				.add(blendedSteering) //
				.add(wanderSB);

			character.setSteeringBehavior(prioritySteeringSB);

			character.setCenterPosition(MathUtils.random(container.stageWidth), MathUtils.random(container.stageHeight));
			character.setMaxSpeed(50);

			table.addActor(character);

			characters.add(character);
		}

		inputProcessor = null;

		Table detailTable = new Table(container.skin);

		detailTable.row();
		final Label alignmentWeightLabel = new Label("Alignment Weight ["
			+ blendedSteerings.get(0).get(0).getWeight() + "]", container.skin);
		detailTable.add(alignmentWeightLabel);
		detailTable.row();
		Slider alignmentWeight = new Slider(0, 500, 1, false, container.skin);
		alignmentWeight.setValue(blendedSteerings.get(0).get(0).getWeight());
		alignmentWeight.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				for (int i = 0; i < blendedSteerings.size; i++)
					blendedSteerings.get(i).get(0).setWeight(slider.getValue());
				alignmentWeightLabel
					.setText("Alignment Weight [" + blendedSteerings.get(0).get(0).getWeight() + "]");
			}
		});
		detailTable.add(alignmentWeight);

		detailTable.row();
		final Label cohesionWeightLabel = new Label("Cohesion Weight ["
			+ blendedSteerings.get(0).get(1).getWeight() + "]", container.skin);
		detailTable.add(cohesionWeightLabel);
		detailTable.row();
		Slider cohesionWeight = new Slider(0, 500, 1, false, container.skin);
		cohesionWeight.setValue(blendedSteerings.get(0).get(1).getWeight());
		cohesionWeight.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				for (int i = 0; i < blendedSteerings.size; i++)
					blendedSteerings.get(i).get(1).setWeight(slider.getValue());
				cohesionWeightLabel.setText("Cohesion Weight [" + blendedSteerings.get(0).get(1).getWeight() + "]");
			}
		});
		detailTable.add(cohesionWeight);

		detailTable.row();
		final Label separationWeightLabel = new Label("Separation Weight ["
			+ blendedSteerings.get(0).get(2).getWeight() + "]", container.skin);
		detailTable.add(separationWeightLabel);
		detailTable.row();
		Slider separationWeight = new Slider(0, 500, 1, false, container.skin);
		separationWeight.setValue(blendedSteerings.get(0).get(2).getWeight());
		separationWeight.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				for (int i = 0; i < blendedSteerings.size; i++)
					blendedSteerings.get(i).get(2).setWeight(slider.getValue());
				separationWeightLabel.setText("Separation Weight [" + blendedSteerings.get(0).get(2).getWeight()
					+ "]");
			}
		});
		detailTable.add(separationWeight);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		final Label labelProximityRadius = new Label("Proximity Radius [" + proximities.get(0).getRadius() + "]", container.skin);
		detailTable.add(labelProximityRadius);
		detailTable.row();
		Slider proximityRadius = new Slider(0, 500, 1, false, container.skin);
		proximityRadius.setValue(proximities.get(0).getRadius());
		proximityRadius.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				for (int i = 0; i < proximities.size; i++)
					proximities.get(i).setRadius(slider.getValue());
				labelProximityRadius.setText("Proximity Radius [" + slider.getValue() + "]");
			}
		});
		detailTable.add(proximityRadius);

		detailTable.row();
		final Label labelProximityAngle = new Label("Proximity Angle [" + proximities.get(0).getAngle() + "]", container.skin);
		detailTable.add(labelProximityAngle);
		detailTable.row();
		Slider proximityAngle = new Slider(0, 360, 1, false, container.skin);
		proximityAngle.setValue(proximities.get(0).getAngle() * MathUtils.degreesToRadians);
		proximityAngle.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				for (int i = 0; i < proximities.size; i++)
					proximities.get(i).setAngle(slider.getValue() * MathUtils.degreesToRadians);
				labelProximityAngle.setText("Proximity Angle [" + slider.getValue() + "]");
			}
		});
		detailTable.add(proximityAngle);

		detailTable.row();
		final Label labelMaxLinAcc = new Label("Max linear.acc.[" + blendedSteerings.get(0).getMaxLinearAcceleration() + "]",
			container.skin);
		detailTable.add(labelMaxLinAcc);
		detailTable.row();
		Slider maxLinAcc = new Slider(0, 500, 1, false, container.skin);
		maxLinAcc.setValue(blendedSteerings.get(0).getMaxLinearAcceleration());
		maxLinAcc.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				for (int i = 0; i < blendedSteerings.size; i++)
					blendedSteerings.get(i).setMaxLinearAcceleration(slider.getValue());
				labelMaxLinAcc.setText("Max linear.acc.[" + slider.getValue() + "]");
			}
		});
		detailTable.add(maxLinAcc);

		detailTable.row();
		final Label labelMaxAngAcc = new Label("Max ang.acc.[" + blendedSteerings.get(0).getMaxAngularAcceleration() + "]",
			container.skin);
		detailTable.add(labelMaxAngAcc);
		detailTable.row();
		Slider maxAngAcc = new Slider(0, 500, 1, false, container.skin);
		maxAngAcc.setValue(blendedSteerings.get(0).getMaxAngularAcceleration());
		maxAngAcc.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				for (int i = 0; i < blendedSteerings.size; i++)
					blendedSteerings.get(i).setMaxAngularAcceleration(slider.getValue());
				labelMaxAngAcc.setText("Max ang.acc.[" + slider.getValue() + "]");
			}
		});
		detailTable.add(maxAngAcc);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		CheckBox debug = new CheckBox("Draw circle", container.skin);
		debug.setChecked(drawDebug);
		debug.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				CheckBox checkBox = (CheckBox)event.getListenerActor();
				drawDebug = checkBox.isChecked();
			}
		});
		detailTable.add(debug);

		detailWindow = createDetailWindow(detailTable);
	}

	@Override
	public void render () {
		if (drawDebug) {
// SteeringActor character = characters.get(0);
// shapeRenderer.begin(ShapeType.Line);
// shapeRenderer.setColor(0, 1, 0, 1);
// shapeRenderer.circle(character.getPosition().x, character.getPosition().y, char0Proximity.getRadius());
// shapeRenderer.end();
			Steerable<Vector2> steerable = characters.get(0);
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(0, 1, 0, 1);
// shapeRenderer.circle(steerable.getPosition().x, steerable.getPosition().y, char0Proximity.getRadius());
			float angle = char0Proximity.getAngle() * MathUtils.radiansToDegrees;
			shapeRenderer.arc(steerable.getPosition().x, steerable.getPosition().y, char0Proximity.getRadius(),
				steerable.getOrientation() * MathUtils.radiansToDegrees - angle / 2f + 90f, angle);
			shapeRenderer.end();
		}
	}

	@Override
	public void dispose () {
		shapeRenderer.dispose();
	}

}
