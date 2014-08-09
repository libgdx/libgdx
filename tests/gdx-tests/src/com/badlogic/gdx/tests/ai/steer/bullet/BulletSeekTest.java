/* ######################################
 * Copyright 2014 (c) Pixel Scientists
 * All rights reserved.
 * Unauthorized copying of this file, via
 * any medium is strictly prohibited.
 * Proprietary and confidential.
 * ###################################### */

package com.badlogic.gdx.tests.ai.steer.bullet;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.tests.SteeringBehaviorTest;
import com.badlogic.gdx.tests.ai.steer.SteeringActor;
import com.badlogic.gdx.tests.ai.steer.TargetInputProcessor;
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
		character.setMaxSpeed(250);

		BulletEntity targetBase = world.add("staticbox", new Matrix4().setToTranslation(new Vector3(5f, 1.5f, 5f)));
		targetBase.body.setCollisionFlags(targetBase.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
		target = new SteeringBulletEntity(targetBase);

		BulletTargetInputProcessor bulletTargetInputProcessor = new BulletTargetInputProcessor(target, new Vector3(0, 1.5f, 0), viewport, world.collisionWorld);
		InputMultiplexer multiplexer = new InputMultiplexer(bulletTargetInputProcessor, cameraController);
		inputProcessor = multiplexer;

		final Seek<Vector3> seekSB = new Seek<Vector3>(character, target, 2500);
		character.setSteeringBehavior(seekSB);

		Table detailTable = new Table(container.skin);

		detailTable.row();
		final Label labelMaxLinAcc = new Label("Max.linear.acc.[" + seekSB.getMaxLinearAcceleration() + "]", container.skin);
		detailTable.add(labelMaxLinAcc);
		detailTable.row();
		Slider maxLinAcc = new Slider(0, 10000, 20, false, container.skin);
		maxLinAcc.setValue(seekSB.getMaxLinearAcceleration());
		maxLinAcc.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				seekSB.setMaxLinearAcceleration(slider.getValue());
				labelMaxLinAcc.setText("Max.linear.acc.[" + slider.getValue() + "]");
			}
		});
		detailTable.add(maxLinAcc);

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
