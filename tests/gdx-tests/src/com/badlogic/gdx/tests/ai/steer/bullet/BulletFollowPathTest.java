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
import com.badlogic.gdx.ai.steer.paths.LinePath;
import com.badlogic.gdx.ai.steer.paths.LinePath.LinePathParam;
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
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.SteeringBehaviorTest;
import com.badlogic.gdx.tests.bullet.BulletEntity;
import com.badlogic.gdx.utils.Array;

/** A class to test and experiment with the {@link FollowPath} behavior.
 * @author Daniel Holderbaum */
public class BulletFollowPathTest extends BulletSteeringTest {
	boolean drawDebug;
	ShapeRenderer shapeRenderer;

	SteeringBulletEntity character;

	Array<Vector3> wayPoints;
	LinePath<Vector3> linePath;
	FollowPath<Vector3, LinePathParam> followPathSB;

	final boolean openPath;
	Slider pathOffset;

	private Vector3 tmp = new Vector3();

	public BulletFollowPathTest (SteeringBehaviorTest container, boolean openPath) {
		super(container, "Bullet Follow " + (openPath ? "Open" : "Closed") + " Path");
		this.openPath = openPath;
	}

	@Override
	public void create (Table table) {
		super.create(table);
		drawDebug = true;

		shapeRenderer = new ShapeRenderer();

		world.add("ground", 0f, 0f, 0f).setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);

		BulletEntity characterBase = world.add("capsule", new Matrix4());

		character = new SteeringBulletEntity(characterBase) {
			@Override
			public void update () {
				super.update();
				if (openPath) {
					// Once arrived at an extremity of the path we want to go the other way around
					Vector3 extremity = followPathSB.getPathOffset() >= 0 ? linePath.getEndPoint() : linePath.getStartPoint();
					float tolerance = followPathSB.getArrivalTolerance();
					if (getPosition().dst2(extremity) < tolerance * tolerance) {
						followPathSB.setPathOffset(-followPathSB.getPathOffset());
						pathOffset.setValue(followPathSB.getPathOffset());
					}
				}
			}
		};
		character.setMaxLinearAcceleration(2000);
		character.setMaxLinearSpeed(15);

		wayPoints = createRandomPath(MathUtils.random(4, 16), 20, 20, 30, 30, 1.5f);

		linePath = new LinePath<Vector3>(wayPoints, openPath);
		followPathSB = new FollowPath<Vector3, LinePathParam>(character, linePath, 3) //
			// Setters below are only useful to arrive at the end of an open path
			.setTimeToTarget(0.1f) //
			.setArrivalTolerance(0.5f) //
			.setDecelerationRadius(3);

		character.setSteeringBehavior(followPathSB);

		character.transform.setToTranslation(wayPoints.first());
		character.body.setWorldTransform(character.transform);

		Table detailTable = new Table(container.skin);

		detailTable.row();
		final Label labelPathOffset = new Label("Path Offset [" + followPathSB.getPathOffset() + "]", container.skin);
		detailTable.add(labelPathOffset);
		detailTable.row();
		pathOffset = new Slider(-15, +15, 0.5f, false, container.skin);
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
		addMaxSpeedController(detailTable, character);

		detailTable.row();
		addMaxLinearAccelerationController(detailTable, character, 0, 20000, 100);

		detailTable.row();
		final Label labelPredictionTime = new Label("Prediction Time [" + followPathSB.getPredictionTime() + " sec.]",
			container.skin);
		detailTable.add(labelPredictionTime);
		detailTable.row();
		Slider predictionTime = new Slider(0, 3, .1f, false, container.skin);
		predictionTime.setValue(followPathSB.getPredictionTime());
		predictionTime.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				followPathSB.setPredictionTime(slider.getValue());
				labelPredictionTime.setText("Prediction Time [" + slider.getValue() + " sec.]");
			}
		});
		detailTable.add(predictionTime);

		// Add controls to arrive at the end of an open path
		if (openPath) {

			detailTable.row();
			final Label labelDecelerationRadius = new Label("Deceleration Radius [" + followPathSB.getDecelerationRadius() + "]",
				container.skin);
			detailTable.add(labelDecelerationRadius);
			detailTable.row();
			Slider decelerationRadius = new Slider(0, 15, .5f, false, container.skin);
			decelerationRadius.setValue(followPathSB.getDecelerationRadius());
			decelerationRadius.addListener(new ChangeListener() {
				@Override
				public void changed (ChangeEvent event, Actor actor) {
					Slider slider = (Slider)actor;
					followPathSB.setDecelerationRadius(slider.getValue());
					labelDecelerationRadius.setText("Deceleration Radius [" + slider.getValue() + "]");
				}
			});
			detailTable.add(decelerationRadius);

			detailTable.row();
			final Label labelArrivalTolerance = new Label("Arrival tolerance [" + followPathSB.getArrivalTolerance() + "]",
				container.skin);
			detailTable.add(labelArrivalTolerance);
			detailTable.row();
			Slider arrivalTolerance = new Slider(0, 1, 0.01f, false, container.skin);
			arrivalTolerance.setValue(followPathSB.getArrivalTolerance());
			arrivalTolerance.addListener(new ChangeListener() {
				@Override
				public void changed (ChangeEvent event, Actor actor) {
					Slider slider = (Slider)actor;
					followPathSB.setArrivalTolerance(slider.getValue());
					labelArrivalTolerance.setText("Arrival tolerance [" + slider.getValue() + "]");
				}
			});
			detailTable.add(arrivalTolerance);
		}

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
			for (int i = 0; i < wayPoints.size; i++) {
				int next = (i + 1) % wayPoints.size;
				if (next != 0 || !linePath.isOpen()) shapeRenderer.line(wayPoints.get(i), wayPoints.get(next));
			}
			shapeRenderer.end();
		}
	}

	@Override
	public void dispose () {
		super.dispose();
		shapeRenderer.dispose();
	}

	private static final Matrix3 tmpMatrix3 = new Matrix3();
	private static final Vector2 tmpVector2 = new Vector2();

	/** Creates a random path which is bound by rectangle described by the min/max values */
	private static Array<Vector3> createRandomPath (int numWaypoints, float minX, float minY, float maxX, float maxY, float height) {
		Array<Vector3> wayPoints = new Array<Vector3>();

		float midX = (maxX + minX) / 2f;
		float midY = (maxY + minY) / 2f;

		float smaller = Math.min(midX, midY);

		float spacing = MathUtils.PI2 / numWaypoints;

		for (int i = 0; i < numWaypoints; i++) {
			float radialDist = MathUtils.random(smaller * 0.2f, smaller);
			tmpVector2.set(radialDist, 0.0f);

			// rotates the specified vector angle rads around the origin
			// init and rotate the transformation matrix
			tmpMatrix3.idt().rotateRad(i * spacing);
			// now transform the object's vertices
			tmpVector2.mul(tmpMatrix3);

			wayPoints.add(new Vector3(tmpVector2.x, height, tmpVector2.y));
		}

		return wayPoints;
	}

}
