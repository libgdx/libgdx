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

package com.badlogic.gdx.tests.ai.steer.bullet;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.tests.SteeringBehaviorTest;
import com.badlogic.gdx.tests.bullet.BulletEntity;

/** @author Daniel Holderbaum */
public class BulletSeekTest extends BulletSteeringTest {

	SteeringBulletEntity character;
	SteeringBulletEntity target;

	public BulletSeekTest (SteeringBehaviorTest container) {
		super(container, "Bullet Seek");
	}

	@Override
	public void create (Table table) {
		super.create(table);

		BulletEntity ground = world.add("ground", 0f, 0f, 0f);
		ground.setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);
		ground.body.userData = "ground";

		BulletEntity characterBase = world.add("capsule", new Matrix4());

		character = new SteeringBulletEntity(characterBase);
		character.setMaxLinearSpeed(250);
		character.setMaxLinearAcceleration(2500);

		BulletEntity targetBase = world.add("staticbox", new Matrix4().setToTranslation(new Vector3(5f, 1.5f, 5f)));
		targetBase.body.setCollisionFlags(targetBase.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
		target = new SteeringBulletEntity(targetBase);

		BulletTargetInputProcessor bulletTargetInputProcessor = new BulletTargetInputProcessor(target, new Vector3(0, 1.5f, 0), viewport, world.collisionWorld);
		InputMultiplexer multiplexer = new InputMultiplexer(bulletTargetInputProcessor, cameraController);
		inputProcessor = multiplexer;

		final Seek<Vector3> seekSB = new Seek<Vector3>(character, target);
		character.setSteeringBehavior(seekSB);

		Table detailTable = new Table(container.skin);

		detailTable.row();
		addMaxLinearAccelerationController(detailTable, character, 0, 20000, 100);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		addMaxSpeedController(detailTable, character);

		detailWindow = createDetailWindow(detailTable);
	}
	
	@Override
	public void render () {
		character.update();

		super.render(true);
	}

	@Override
	public void dispose () {
		super.dispose();
	}

}
