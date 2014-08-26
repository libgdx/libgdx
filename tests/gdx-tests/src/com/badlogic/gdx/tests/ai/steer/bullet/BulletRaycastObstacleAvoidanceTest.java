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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance.Ray;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance.RaycastCollisionDetector;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.steer.limiters.FullLimiter;
import com.badlogic.gdx.ai.steer.rays.CentralRayWithWhiskersConfiguration;
import com.badlogic.gdx.ai.steer.rays.ParallelSideRayConfiguration;
import com.badlogic.gdx.ai.steer.rays.RayConfigurationBase;
import com.badlogic.gdx.ai.steer.rays.SingleRayConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.SteeringBehaviorTest;
import com.badlogic.gdx.tests.bullet.BulletEntity;

/** @author Daniel Holderbaum */
public class BulletRaycastObstacleAvoidanceTest extends BulletSteeringTest {

	SteeringBulletEntity character;
	int rayConfigurationIndex;
	RayConfigurationBase<Vector3>[] rayConfigurations;
	RaycastObstacleAvoidance<Vector3> raycastObstacleAvoidanceSB;

	boolean drawDebug;
	ShapeRenderer shapeRenderer;

	private Vector3 tmp = new Vector3();

	public BulletRaycastObstacleAvoidanceTest (SteeringBehaviorTest container) {
		super(container, "Bullet Raycast Obstacle Avoidance");
	}

	@Override
	public void create (Table table) {
		super.create(table);
		drawDebug = true;

		shapeRenderer = new ShapeRenderer();

		world.add("ground", 0f, 0f, 0f).setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);

		BulletEntity wall1 = world.add("staticwall", -10f, 0f, 0f);
		wall1.setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);
		wall1.transform.rotate(Vector3.Y, 90);
		wall1.body.setWorldTransform(wall1.transform);

		BulletEntity wall2 = world.add("staticwall", 0f, 0f, -10f);
		wall2.setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);

		BulletEntity wall3 = world.add("staticwall", 10f, 0f, 0f);
		wall3.setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);
		wall3.transform.rotate(Vector3.Y, 90);
		wall3.body.setWorldTransform(wall3.transform);

		BulletEntity wall4 = world.add("staticwall", 0f, 0f, 10f);
		wall4.setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);

		BulletEntity characterBase = world.add("capsule", new Matrix4());

		character = new SteeringBulletEntity(characterBase);
		character.setMaxLinearAcceleration(1500);
		character.setMaxLinearSpeed(250);

		rayConfigurations = new RayConfigurationBase[] {new SingleRayConfiguration(character, 2),
			new ParallelSideRayConfiguration<Vector3>(character, 2, character.getBoundingRadius()),
			new CentralRayWithWhiskersConfiguration<Vector3>(character, 2, 1, 35 * MathUtils.degreesToRadians)};
		rayConfigurationIndex = 0;
		RaycastCollisionDetector<Vector3> raycastCollisionDetector = new BulletRaycastCollisionDetector(world.collisionWorld,
			character.body);
		raycastObstacleAvoidanceSB = new RaycastObstacleAvoidance<Vector3>(character, rayConfigurations[rayConfigurationIndex],
			raycastCollisionDetector, 2);

		Wander<Vector3> wanderSB = new Wander<Vector3>(character) //
			// Notice that:
			// 1. setting maxLinearSpeed to -1 has no effect; we actually take it from the character, see the overridden getter
			// 2. maxAngularAcceleration is set to 0 because independent facing is disabled
			.setLimiter(new FullLimiter(1500, -1, 0, 5) {
				@Override
				public float getMaxLinearSpeed () {
					return character.getMaxLinearSpeed();
				}
			}) //
			.setAlignTolerance(0.001f) //
			.setDecelerationRadius(5) //
			.setTimeToTarget(0.1f) //
			.setWanderOffset(2) //
			.setWanderOrientation(0) //
			.setWanderRadius(1) //
			.setWanderRate(MathUtils.PI / 5);

		PrioritySteering<Vector3> prioritySteeringSB = new PrioritySteering<Vector3>(character, 0.0001f) //
			.add(raycastObstacleAvoidanceSB) //
			.add(wanderSB);

		character.setSteeringBehavior(wanderSB);

		Table detailTable = new Table(container.skin);

		detailTable.row();
		addMaxLinearAccelerationController(detailTable, character, 0, 20000, 100);

		detailTable.row();
		final Label labelDistFromBoundary = new Label("Distance from Boundary ["
			+ raycastObstacleAvoidanceSB.getDistanceFromBoundary() + "]", container.skin);
		detailTable.add(labelDistFromBoundary);
		detailTable.row();
		Slider distFromBoundary = new Slider(0, 10, 0.1f, false, container.skin);
		distFromBoundary.setValue(raycastObstacleAvoidanceSB.getDistanceFromBoundary());
		distFromBoundary.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				raycastObstacleAvoidanceSB.setDistanceFromBoundary(slider.getValue());
				labelDistFromBoundary.setText("Distance from Boundary [" + slider.getValue() + "]");
			}
		});
		detailTable.add(distFromBoundary);

		detailTable.row();
		final Label labelRayConfig = new Label("Ray Configuration", container.skin);
		detailTable.add(labelRayConfig);
		detailTable.row();
		SelectBox<String> rayConfig = new SelectBox<String>(container.skin);
		rayConfig.setItems(new String[] {"Single Ray", "Parallel Side Rays", "Central Ray with Whiskers"});
		rayConfig.setSelectedIndex(0);
		rayConfig.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				SelectBox<String> selectBox = (SelectBox<String>)actor;
				rayConfigurationIndex = selectBox.getSelectedIndex();
				raycastObstacleAvoidanceSB.setRayConfiguration(rayConfigurations[rayConfigurationIndex]);
			}
		});
		detailTable.add(rayConfig);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		CheckBox debug = new CheckBox("Draw Rays", container.skin);
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

	@Override
	public void render () {
		character.update();

		super.render(true);

		if (drawDebug) {
			Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
			Ray<Vector3>[] rays = rayConfigurations[rayConfigurationIndex].getRays();
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(1, 1, 0, 1);
			shapeRenderer.setProjectionMatrix(camera.combined);
			for (int i = 0; i < rays.length; i++) {
				Ray<Vector3> ray = rays[i];
				shapeRenderer.line(ray.origin, tmp.set(ray.origin).add(ray.direction));
			}
			shapeRenderer.end();
			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		}
	}

	@Override
	public void dispose () {
		super.dispose();
		shapeRenderer.dispose();
	}

}
