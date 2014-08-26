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

import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.paths.LinePath;
import com.badlogic.gdx.ai.steer.paths.LinePath.LinePathParam;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
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

/** A class to test and experiment with the {@link FollowPath} behavior.
 * 
 * @autor davebaol */
public class FollowPathTest extends SteeringTest {
	ShapeRenderer shapeRenderer;
	boolean drawDebug;

	SteeringActor character;

	Vector2[] wayPoints;
	LinePath<Vector2> linePath;
	FollowPath<Vector2, LinePathParam> followPathSB;

	final boolean openPath;
	Slider pathOffset;

	public FollowPathTest (SteeringBehaviorTest container, boolean openPath) {
		super(container, "Follow " + (openPath ? "Open" : "Closed") + " Path");
		this.openPath = openPath;
	}

	@Override
	public void create (Table table) {
		drawDebug = true;
		shapeRenderer = new ShapeRenderer();

		character = new SteeringActor(container.badlogicSmall, false) {
			@Override
			public void act (float delta) {
				super.act(delta);
				if (openPath) {
					// Once arrived at an extremity of the path we want to go the other way around
					Vector2 extremity = followPathSB.getPathOffset() >= 0 ? linePath.getEndPoint() : linePath.getStartPoint();
					float tolerance = followPathSB.getArrivalTolerance();
					if (getPosition().dst2(extremity) < tolerance * tolerance) {
						followPathSB.setPathOffset(-followPathSB.getPathOffset());
						pathOffset.setValue(followPathSB.getPathOffset());
					}
				}
			}
		};
		
		// Set character's limiter
		character.setMaxLinearSpeed(100);
		character.setMaxLinearAcceleration(300);

		wayPoints = createRandomPath(MathUtils.random(4, 16), 50, 50, container.stageWidth - 50, container.stageHeight - 50);

		linePath = new LinePath<Vector2>(wayPoints, openPath);
		followPathSB = new FollowPath<Vector2, LinePathParam>(character, linePath, 30) //
			// Setters below are only useful to arrive at the end of an open path
			.setTimeToTarget(0.1f) //
			.setArrivalTolerance(0.001f) //
			.setDecelerationRadius(80);

		character.setSteeringBehavior(followPathSB);

		table.addActor(character);

		character.setCenterPosition(wayPoints[0].x, wayPoints[0].y);

		Table detailTable = new Table(container.skin);

		detailTable.row();
		final Label labelPathOffset = new Label("Path Offset [" + followPathSB.getPathOffset() + "]", container.skin);
		detailTable.add(labelPathOffset);
		detailTable.row();
		pathOffset = new Slider(-150, +150, 5, false, container.skin);
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
		addMaxLinearAccelerationController(detailTable, character, 0, 5000, 10);

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
			Slider decelerationRadius = new Slider(0, 150, 1, false, container.skin);
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
			Slider arrivalTolerance = new Slider(0, 1, 0.0001f, false, container.skin);
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
		CheckBox debug = new CheckBox("Draw target", container.skin);
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
		// Draw path
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(0, 1, 0, 1);
		for (int i = 0; i < wayPoints.length; i++) {
			int next = (i + 1) % wayPoints.length;
			if (next != 0 || !linePath.isOpen()) shapeRenderer.line(wayPoints[i], wayPoints[next]);
			shapeRenderer.circle(wayPoints[i].x, wayPoints[i].y, 2f);
		}
		shapeRenderer.end();

		if (drawDebug) {
			// Draw target
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(1, 1, 0, 1);
			shapeRenderer.circle(followPathSB.getInternalTargetPosition().x, followPathSB.getInternalTargetPosition().y, 5);
			shapeRenderer.end();
		}
	}

	@Override
	public void dispose () {
		shapeRenderer.dispose();
	}

	/** Creates a random path which is bound by rectangle described by the min/max values */
	private Vector2[] createRandomPath (int numWaypoints, float minX, float minY, float maxX, float maxY) {
		Vector2[] wayPoints = new Vector2[numWaypoints];

		float midX = (maxX + minX) / 2f;
		float midY = (maxY + minY) / 2f;

		float smaller = Math.min(midX, midY);

		float spacing = MathUtils.PI2 / numWaypoints;

		for (int i = 0; i < numWaypoints; i++) {
			float radialDist = MathUtils.random(smaller * 0.2f, smaller);

			Vector2 temp = new Vector2(radialDist, 0.0f);

			rotateVectorAroundOrigin(temp, i * spacing);

			temp.x += midX;
			temp.y += midY;

			wayPoints[i] = temp;
		}

		return wayPoints;
	}

	private static final Matrix3 tmpMatrix3 = new Matrix3();

	/** Rotates the specified vector angle rads around the origin */
	private static Vector2 rotateVectorAroundOrigin (Vector2 vector, float radians) {
		// Init and rotate the transformation matrix
		tmpMatrix3.idt().rotateRad(radians);

		// Now transform the object's vertices
		return vector.mul(tmpMatrix3);
	}

}
