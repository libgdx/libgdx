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

import com.badlogic.gdx.ai.steer.behaviors.Seek;
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

/** A class to test and experiment with the {@link Seek} behavior.
 * 
 * @autor davebaol */
public class SeekTest extends SteeringTest {

	SteeringActor character;
	SteeringActor target;

	public SeekTest (SteeringBehaviorTest container) {
		super(container, "Seek");
	}

	@Override
	public void create (Table table) {
		character = new SteeringActor(container.badlogicSmall, false);
		target = new SteeringActor(container.target);
		inputProcessor = new TargetInputProcessor(target);

		character.setMaxLinearSpeed(250);
		character.setMaxLinearAcceleration(2000);

		final Seek<Vector2> seekSB = new Seek<Vector2>(character, target);
		character.setSteeringBehavior(seekSB);

		table.addActor(character);
		table.addActor(target);

		character.setCenterPosition(container.stageWidth / 2, container.stageHeight / 2);
		target.setCenterPosition(MathUtils.random(container.stageWidth), MathUtils.random(container.stageHeight));

		Table detailTable = new Table(container.skin);

		detailTable.row();
		addMaxLinearAccelerationController(detailTable, character, 0, 10000, 20);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		addMaxSpeedController(detailTable, character);

		detailWindow = createDetailWindow(detailTable);
	}

	@Override
	public void render () {
	}

	@Override
	public void dispose () {
	}

}
