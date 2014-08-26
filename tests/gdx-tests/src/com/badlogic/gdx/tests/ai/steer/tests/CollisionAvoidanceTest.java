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
import com.badlogic.gdx.ai.steer.behaviors.CollisionAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.steer.limiters.FullLimiter;
import com.badlogic.gdx.ai.steer.proximities.RadiusProximity;
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

/** A class to test and experiment with the {@link CollisionAvoidance} behavior.
 * 
 * @autor davebaol */
public class CollisionAvoidanceTest extends SteeringTest {
	Array<SteeringActor> characters;
	RadiusProximity<Vector2> char0Proximity;
	Array<RadiusProximity<Vector2>> proximities;
	boolean drawDebug;
	ShapeRenderer shapeRenderer;

	public CollisionAvoidanceTest (SteeringBehaviorTest container) {
		super(container, "Collision Avoidance");
	}

	@Override
	public void create (Table table) {
		drawDebug = true;

		shapeRenderer = new ShapeRenderer();

		characters = new Array<SteeringActor>();
		proximities = new Array<RadiusProximity<Vector2>>();

		for (int i = 0; i < 60; i++) {
			final SteeringActor character = new SteeringActor(container.greenFish, false);
			character.setMaxLinearSpeed(50);
			character.setMaxLinearAcceleration(100);

			RadiusProximity<Vector2> proximity = new RadiusProximity<Vector2>(character, characters,
				character.getBoundingRadius() * 4);
			proximities.add(proximity);
			if (i == 0) char0Proximity = proximity;
			CollisionAvoidance<Vector2> collisionAvoidanceSB = new CollisionAvoidance<Vector2>(character, proximity);

			Wander<Vector2> wanderSB = new Wander<Vector2>(character) //
				// Notice that:
				// 1. setting maxLinearSpeed to -1 has no effect; we actually take it from the character, see the overridden getter
				// 2. maxAngularAcceleration is set to 0 because independent facing is disabled
				.setLimiter(new FullLimiter(30, -1, 0, 5) {
					@Override
					public float getMaxLinearSpeed () {
						return character.getMaxLinearSpeed();
					}
				}) //
				.setAlignTolerance(0.001f) //
				.setDecelerationRadius(5) //
				.setTimeToTarget(0.1f) //
				.setWanderOffset(60) //
				.setWanderOrientation(10) //
				.setWanderRadius(40) //
				.setWanderRate(MathUtils.PI / 5);

			PrioritySteering<Vector2> prioritySteeringSB = new PrioritySteering<Vector2>(character, 0.0001f);
			prioritySteeringSB.add(collisionAvoidanceSB);
			prioritySteeringSB.add(wanderSB);

			character.setSteeringBehavior(prioritySteeringSB);

			setRandomNonOverlappingPosition(character, characters, 5);

			table.addActor(character);

			characters.add(character);
		}

		inputProcessor = null;

		Table detailTable = new Table(container.skin);

		detailTable.row();
		final Label labelMaxLinAcc = new Label("Max.Linear Acc.[" + characters.get(0).getMaxLinearAcceleration() + "]",
			container.skin);
		detailTable.add(labelMaxLinAcc);
		detailTable.row();
		Slider maxLinAcc = new Slider(0, 1500, 1, false, container.skin);
		maxLinAcc.setValue(characters.get(0).getMaxLinearAcceleration());
		maxLinAcc.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				for (int i = 0; i < characters.size; i++)
					characters.get(i).setMaxLinearAcceleration(slider.getValue());
				labelMaxLinAcc.setText("Max.Linear Acc.[" + slider.getValue() + "]");
			}
		});
		detailTable.add(maxLinAcc);

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
		addSeparator(detailTable);

		detailTable.row();
		final Label labelMaxLinSpeed = new Label("Max.Linear Speed.[" + characters.get(0).getMaxLinearSpeed() + "]",
			container.skin);
		detailTable.add(labelMaxLinSpeed);
		detailTable.row();
		Slider maxLinSpeed = new Slider(0, 300, 10, false, container.skin);
		maxLinSpeed.setValue(characters.get(0).getMaxLinearSpeed());
		maxLinSpeed.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				for (int i = 0; i < characters.size; i++)
					characters.get(i).setMaxLinearSpeed(slider.getValue());
				labelMaxLinSpeed.setText("Max.Linear Speed.[" + slider.getValue() + "]");
			}
		});
		detailTable.add(maxLinSpeed);

		detailTable.row();
		CheckBox debug = new CheckBox("Draw Proximity", container.skin);
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
			Steerable<Vector2> steerable = characters.get(0);
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(0, 1, 0, 1);
			shapeRenderer.circle(steerable.getPosition().x, steerable.getPosition().y, char0Proximity.getRadius());
			shapeRenderer.end();
		}
	}

	@Override
	public void dispose () {
		shapeRenderer.dispose();
	}

}
