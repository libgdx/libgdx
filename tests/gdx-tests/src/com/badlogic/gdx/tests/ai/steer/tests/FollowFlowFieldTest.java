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

import com.badlogic.gdx.ai.steer.behaviors.FollowFlowField;
import com.badlogic.gdx.ai.steer.behaviors.FollowFlowField.FlowField;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.tests.SteeringBehaviorTest;
import com.badlogic.gdx.tests.ai.steer.SteeringActor;
import com.badlogic.gdx.tests.ai.steer.SteeringTest;
import com.badlogic.gdx.utils.Array;

/** A class to test and experiment with the {@link FollowFlowField} behavior.
 * 
 * @autor davebaol */
public class FollowFlowFieldTest extends SteeringTest {
	boolean drawDebug;
	ShapeRenderer shapeRenderer;

	SteeringActor character;

	RandomFlowField2DWithRepulsors flowField;

	public FollowFlowFieldTest (SteeringBehaviorTest container) {
		super(container, "Follow Flow Field");
	}

	@Override
	public void create (Table table) {
		drawDebug = true;

		shapeRenderer = new ShapeRenderer();

		// Create obstacles
		Array<SteeringActor> obstacles = new Array<SteeringActor>();
		for (int i = 0; i < 4; i++) {
			SteeringActor obstacle = new SteeringActor(container.cloud, false);
			setRandomNonOverlappingPosition(obstacle, obstacles, 100);
			obstacles.add(obstacle);
			table.addActor(obstacle);
		}

		character = new SteeringActor(container.badlogicSmall, false);
		character.setMaxLinearSpeed(300);
		character.setMaxLinearAcceleration(400);

		flowField = new RandomFlowField2DWithRepulsors(container.stageWidth, container.stageHeight, container.badlogicSmall.getRegionWidth(), obstacles);
		final FollowFlowField<Vector2> followFlowFieldSB = new FollowFlowField<Vector2>(character, flowField);
		character.setSteeringBehavior(followFlowFieldSB);

		table.addActor(character);

		character.setCenterPosition(container.stageWidth / 2, container.stageHeight / 2);

		Table detailTable = new Table(container.skin);

		detailTable.row();
		addMaxLinearAccelerationController(detailTable, character, 0, 10000, 20);

		detailTable.row();
		addMaxSpeedController(detailTable, character);

		detailTable.row();
		final Label labelPredictionTime = new Label("Prediction Time [" + followFlowFieldSB.getPredictionTime() + " sec.]", container.skin);
		detailTable.add(labelPredictionTime);
		detailTable.row();
		Slider predictionTime = new Slider(0, 3, .1f, false, container.skin);
		predictionTime.setValue(followFlowFieldSB.getPredictionTime());
		predictionTime.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				followFlowFieldSB.setPredictionTime(slider.getValue());
				labelPredictionTime.setText("Prediction Time [" + slider.getValue() + " sec.]");
			}
		});
		detailTable.add(predictionTime);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		CheckBox debug = new CheckBox("Draw Flow Field", container.skin);
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

	Vector2 tmp1 = new Vector2();
	Vector2 tmp2 = new Vector2();

	@Override
	public void render () {
		if (drawDebug) {
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(0, 1, 0, 1);
			Vector2[][] field = flowField.field;
			float resolution = flowField.resolution;
			float scaledResolution = resolution * .7f;
			float halfResolution = resolution * .5f;
			int columns = flowField.columns;
			int rows = flowField.rows;
			for (int i = 0; i < columns; i++) {
				for (int j = 0; j < rows; j++) {
					Vector2 flow = field[i][j];
					tmp2.set(i * resolution + halfResolution, j * resolution + halfResolution); // midpoint
					tmp2.mulAdd(flow, scaledResolution / 2); // end point
					tmp1.set(tmp2).mulAdd(flow, -scaledResolution); // start point
					shapeRenderer.line(tmp1.x, tmp1.y, tmp2.x, tmp2.y);
					shapeRenderer.circle(tmp2.x, tmp2.y, 1.5f);
				}
			}
			shapeRenderer.end();
		}
	}

	@Override
	public void dispose () {
		shapeRenderer.dispose();
	}

	static class RandomFlowField2DWithRepulsors implements FlowField<Vector2> {

		Vector2[][] field;
		int rows, columns;
		int resolution;

		public RandomFlowField2DWithRepulsors (float width, float height, int resolution, Array<SteeringActor> obstacles) {
			this.resolution = resolution;
			this.columns = MathUtils.ceil(width / resolution);
			this.rows = MathUtils.ceil(height / resolution);
			this.field = new Vector2[columns][rows];

			for (int i = 0; i < columns; i++) {
				ROWS:
				for (int j = 0; j < rows; j++) {
					for (int k = 0; k < obstacles.size; k++) {
						SteeringActor obstacle = obstacles.get(k); 
						if (obstacle.getPosition().dst(resolution * (i + .5f), resolution * (j + .5f)) < obstacle.getBoundingRadius() + 40) {
							field[i][j] = new Vector2(resolution * (i + .5f), resolution * (j + .5f)).sub(obstacle.getPosition()).nor();
							continue ROWS;
						}
					}
					field[i][j] = new Vector2(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
				}
			}
		}

		@Override
		public Vector2 lookup (Vector2 position) {
			int column = (int)MathUtils.clamp(position.x / resolution, 0, columns - 1);
			int row = (int)MathUtils.clamp(position.y / resolution, 0, rows - 1);
			return field[column][row];
		}
	}

}
