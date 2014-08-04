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
import com.badlogic.gdx.ai.steer.paths.LinePath.LinePathParam;
import com.badlogic.gdx.ai.steer.paths.LinePath2D;
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

	FollowPath<Vector2, LinePathParam> followPathSB;

	public FollowPathTest (SteeringBehaviorTest container) {
		super(container, "Follow Path");
	}

	@Override
	public void create (Table table) {
		drawDebug = true;
		shapeRenderer = new ShapeRenderer();

		character = new SteeringActor(container.badlogicSmall, false);
		character.setMaxSpeed(100);
		
		wayPoints = createRandomPath(MathUtils.random(4, 12), 50, 50, container.stageWidth - 50, container.stageHeight - 50);

		LinePath2D linePath2D = new LinePath2D(wayPoints);
		followPathSB = new FollowPath<Vector2, LinePathParam>(character, linePath2D, 30, 300);

		character.setSteeringBehavior(followPathSB);

		table.addActor(character);

		character.setPosition(wayPoints[0].x, wayPoints[0].y);
		
		Table detailTable = new Table(container.skin);

		detailTable.row();
		final Label labelPathOffset= new Label("Path Offset ["+followPathSB.getPathOffset()+"]", container.skin);
		detailTable.add(labelPathOffset);
		detailTable.row();
		Slider pathOffset = new Slider(-150, +150, 5, false, container.skin);
		pathOffset.setValue(followPathSB.getPathOffset());
		pathOffset.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				followPathSB.setPathOffset(slider.getValue());
				labelPathOffset.setText("Path Offset ["+slider.getValue()+"]");
			}
		});
		detailTable.add(pathOffset);

		detailTable.row();
		final Label labelMaxLinAcc = new Label("Max.linear.acc.["+followPathSB.getMaxLinearAcceleration()+"]", container.skin);
		detailTable.add(labelMaxLinAcc);
		detailTable.row();
		Slider maxLinAcc = new Slider(0, 5000, 10, false, container.skin);
		maxLinAcc.setValue(followPathSB.getMaxLinearAcceleration());
		maxLinAcc.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				followPathSB.setMaxLinearAcceleration(slider.getValue());
				labelMaxLinAcc.setText("Max.linear.acc.["+slider.getValue()+"]");
			}
		});
		detailTable.add(maxLinAcc);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		CheckBox debug = new CheckBox("Draw target", container.skin);
		debug.setChecked(drawDebug);
		debug.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				CheckBox checkBox = (CheckBox)event.getListenerActor();
				drawDebug = checkBox.isChecked();
			}
		});
		detailTable.add(debug);

		detailTable.row();
		addSeparator(detailTable);
		
		detailTable.row();
		addMaxSpeedController(detailTable, character);

//		detailWindow.row();
//		addAlignOrientationToLinearVelocityController(detailWindow, character);

		detailWindow = createDetailWindow(detailTable);
	}

	@Override
	public void render () {
		// Draw path
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(0, 1, 0, 1);
		for (int i = 0; i < wayPoints.length; i++) {
			int next = (i + 1) % wayPoints.length;
			shapeRenderer.line(wayPoints[i], wayPoints[next]);
		}		
		shapeRenderer.end();

		if (drawDebug) {
			// Draw target
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(1, 1, 0, 1);
			shapeRenderer.circle(followPathSB.targetPos.x, followPathSB.targetPos.y, 5);
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

	private static final Matrix3 matrix = new Matrix3();

	/** Rotates the specified vector angle rads around the origin */
	private static Vector2 rotateVectorAroundOrigin (Vector2 vector, float radians) {
		// Init and rotate the transformation matrix
		matrix.idt().rotateRad(radians);

		// Now transform the object's vertices
		return vector.mul(matrix);
	}

}
