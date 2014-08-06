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

import com.badlogic.gdx.ai.steer.behaviors.Interpose;
import com.badlogic.gdx.ai.steer.behaviors.Pursue;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
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

/** A class to test and experiment with the {@link Pursue} behavior.
 * 
 * @autor davebaol */
public class InterposeTest extends SteeringTest {
	boolean drawDebug;
	ShapeRenderer shapeRenderer;

	SteeringActor character;
	SteeringActor c1;
	SteeringActor c2;

	Interpose<Vector2> interposeSB;

	public InterposeTest (SteeringBehaviorTest container) {
		super(container, "Interpose");
	}

	@Override
	public void create (Table table) {
		drawDebug = true;

		shapeRenderer = new ShapeRenderer();

		character = new SteeringActor(container.target, false);
		c1 = new SteeringActor(container.greenFish, false);
		c2 = new SteeringActor(container.badlogicSmall, false);

		interposeSB = new Interpose<Vector2>(character, c1, c2, .5f) //
			.setMaxLinearAcceleration(700) //
			.setMaxSpeed(300) //
			.setTimeToTarget(0.1f) //
			.setArrivalTolerance(0.001f) //
			.setDecelerationRadius(20);
		character.setSteeringBehavior(interposeSB);

		Wander<Vector2> wanderSB1 = new Wander<Vector2>(c1) //
			.setMaxLinearAcceleration(250) //
			.setMaxAngularAcceleration(0) // set to 0 because independent facing is disabled
			.setAlignTolerance(0.001f) //
			.setDecelerationRadius(5) //
			.setMaxRotation(5) //
			.setTimeToTarget(0.1f) //
			.setWanderOffset(110) //
			.setWanderOrientation(MathUtils.random(MathUtils.PI2)) //
			.setWanderRadius(64) //
			.setWanderRate(MathUtils.PI / 6);
		c1.setSteeringBehavior(wanderSB1);

		Wander<Vector2> wanderSB2 = new Wander<Vector2>(c1) //
			.setMaxLinearAcceleration(450) //
			.setMaxAngularAcceleration(0) // set to 0 because independent facing is disabled
			.setAlignTolerance(0.001f) //
			.setDecelerationRadius(5) //
			.setMaxRotation(5) //
			.setTimeToTarget(0.1f) //
			.setWanderOffset(70) //
			.setWanderOrientation(MathUtils.random(MathUtils.PI2)) //
			.setWanderRadius(94).setWanderRate(MathUtils.PI / 4);
		c2.setSteeringBehavior(wanderSB2);

		table.addActor(character);
		table.addActor(c1);
		table.addActor(c2);

		character.setCenterPosition(MathUtils.random(container.stageWidth), MathUtils.random(container.stageHeight));
		character.setMaxSpeed(200);
		c1.setCenterPosition(MathUtils.random(container.stageWidth), MathUtils.random(container.stageHeight));
		c1.setMaxSpeed(100);
		c2.setCenterPosition(MathUtils.random(container.stageWidth), MathUtils.random(container.stageHeight));
		c2.setMaxSpeed(100);

		Table detailTable = new Table(container.skin);

		detailTable.row();
		final Label labelMaxLinAcc = new Label("Max.linear.acc.[" + interposeSB.getMaxLinearAcceleration() + "]", container.skin);
		detailTable.add(labelMaxLinAcc);
		detailTable.row();
		Slider maxLinAcc = new Slider(0, 10000, 20, false, container.skin);
		maxLinAcc.setValue(interposeSB.getMaxLinearAcceleration());
		maxLinAcc.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				interposeSB.setMaxLinearAcceleration(slider.getValue());
				labelMaxLinAcc.setText("Max.linear.acc.[" + slider.getValue() + "]");
			}
		});
		detailTable.add(maxLinAcc);

		detailTable.row();
		final Label labelInterpositionRatio = new Label("Interposition Ratio [" + interposeSB.getInterpositionRatio() + "]",
			container.skin);
		detailTable.add(labelInterpositionRatio);
		detailTable.row();
		Slider interpositionRatio = new Slider(0, 1, 0.1f, false, container.skin);
		interpositionRatio.setValue(interposeSB.getInterpositionRatio());
		interpositionRatio.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				interposeSB.setInterpositionRatio(slider.getValue());
				labelInterpositionRatio.setText("Interposition Ratio [" + slider.getValue() + "]");
			}
		});
		detailTable.add(interpositionRatio);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		CheckBox debug = new CheckBox("Draw debug info", container.skin);
		debug.setChecked(drawDebug);
		debug.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				CheckBox checkBox = (CheckBox)event.getListenerActor();
				drawDebug = checkBox.isChecked();
			}
		});
		detailTable.add(debug);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		addMaxSpeedController(detailTable, character);

		detailWindow = createDetailWindow(detailTable);
	}

	Vector2 point = new Vector2();

	@Override
	public void render () {
		if (drawDebug) {
			Vector2 posA = interposeSB.getAgentA().getPosition();
			Vector2 posB = interposeSB.getAgentB().getPosition();

			// Draw line between agents
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(1, 1, 0, 1);
			shapeRenderer.line(posA.x, posA.y, posB.x, posB.y);
			shapeRenderer.end();

			// Draw real target along the line between agents
			point.set(posB).sub(posA).scl(interposeSB.getInterpositionRatio()).add(posA);
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(1, 1, 0, 1);
			shapeRenderer.circle(point.x, point.y, 4);
			shapeRenderer.end();

			// Draw estimated target
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(1, 0, 0, 1);
			shapeRenderer.circle(interposeSB.getTargetPosition().x, interposeSB.getTargetPosition().y, 4);
			shapeRenderer.end();
		}
	}

	@Override
	public void dispose () {
		shapeRenderer.dispose();
	}

}
