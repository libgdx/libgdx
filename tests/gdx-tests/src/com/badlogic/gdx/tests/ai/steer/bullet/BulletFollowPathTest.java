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

import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance.Ray;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance.RaycastCollisionDetector;
import com.badlogic.gdx.ai.steer.paths.LinePath;
import com.badlogic.gdx.ai.steer.paths.LinePath.LinePathParam;
import com.badlogic.gdx.ai.steer.rays.CentralRayWithWhiskersConfiguration;
import com.badlogic.gdx.ai.steer.rays.ParallelSideRayConfiguration;
import com.badlogic.gdx.ai.steer.rays.RayConfigurationBase;
import com.badlogic.gdx.ai.steer.rays.SingleRayConfiguration;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.tests.SteeringBehaviorTest;
import com.badlogic.gdx.tests.bullet.BulletEntity;

/** A class to test and experiment with the {@link FollowPath} behavior.
 * @author Daniel Holderbaum */
public class BulletFollowPathTest extends BulletSteeringTest {

	SteeringBulletEntity character;

	Vector3[] wayPoints;
	FollowPath<Vector3, LinePathParam> followPathSB;

	boolean drawDebug;
	ShapeRenderer shapeRenderer;

	private Vector3 tmp = new Vector3();

	public BulletFollowPathTest (SteeringBehaviorTest container) {
		super(container, "Bullet Follow Path");
	}

	@Override
	public void create (Table table) {
		super.create(table);
		drawDebug = true;

		shapeRenderer = new ShapeRenderer();

		world.add("ground", 0f, 0f, 0f).setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);

		BulletEntity characterBase = world.add("capsule", new Matrix4());

		character = new SteeringBulletEntity(characterBase);
		character.setMaxSpeed(250);

		wayPoints = createRandomPath(MathUtils.random(4, 12), 10, 10, 20, 20, 1.5f);

		LinePath<Vector3> linePath = new LinePath<Vector3>(wayPoints);
		followPathSB = new FollowPath<Vector3, LinePathParam>(character, linePath, 1, 500);

		character.setSteeringBehavior(followPathSB);

		character.transform.setToTranslation(wayPoints[0]);
		character.body.setWorldTransform(character.transform);

		Table detailTable = new Table(container.skin);

		detailTable.row();
		final Label labelPathOffset = new Label("Path Offset [" + followPathSB.getPathOffset() + "]", container.skin);
		detailTable.add(labelPathOffset);
		detailTable.row();
		Slider pathOffset = new Slider(-50, +50, 1, false, container.skin);
		pathOffset.setValue(followPathSB.getPathOffset());
		pathOffset.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				followPathSB.setPathOffset(slider.getValue());
				labelPathOffset.setText("Path Offset [" + slider.getValue() + "]");
			}
		});
		detailTable.add(pathOffset);

		detailTable.row();
		final Label labelMaxLinAcc = new Label("Max.linear.acc.[" + followPathSB.getMaxLinearAcceleration() + "]", container.skin);
		detailTable.add(labelMaxLinAcc);
		detailTable.row();
		Slider maxLinAcc = new Slider(0, 2000, 10, false, container.skin);
		maxLinAcc.setValue(followPathSB.getMaxLinearAcceleration());
		maxLinAcc.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				followPathSB.setMaxLinearAcceleration(slider.getValue());
				labelMaxLinAcc.setText("Max.linear.acc.[" + slider.getValue() + "]");
			}
		});
		detailTable.add(maxLinAcc);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		CheckBox debug = new CheckBox("Draw path", container.skin);
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
			// Draw path
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(0, 1, 0, 1);
			shapeRenderer.setProjectionMatrix(camera.combined);
			for (int i = 0; i < wayPoints.length; i++) {
				int next = (i + 1) % wayPoints.length;
				shapeRenderer.line(wayPoints[i], wayPoints[next]);
			}
			shapeRenderer.end();
		}
	}

	@Override
	public void dispose () {
		super.dispose();
		shapeRenderer.dispose();
	}

	private static final Matrix3 matrix = new Matrix3();

	/** Creates a random path which is bound by rectangle described by the min/max values */
	private static Vector3[] createRandomPath (int numWaypoints, float minX, float minY, float maxX, float maxY, float height) {
		Vector3[] wayPoints = new Vector3[numWaypoints];

		float spacing = MathUtils.PI2 / numWaypoints;
		float midX = (maxX + minX) / 2f;
		float midY = (maxY + minY) / 2f;
		float smaller = Math.min(midX, midY);

		for (int i = 0; i < numWaypoints; i++) {
			float radialDist = MathUtils.random(smaller * 0.2f, smaller);
			Vector2 temp = new Vector2(radialDist, 0.0f);

			// rotates the specified vector angle rads around the origin
			// init and rotate the transformation matrix
			matrix.idt().rotateRad(i * spacing);
			// now transform the object's vertices
			temp.mul(matrix);

			wayPoints[i] = new Vector3(temp.x, height, temp.y);
		}

		return wayPoints;
	}

}
