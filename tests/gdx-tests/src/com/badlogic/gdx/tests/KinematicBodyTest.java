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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.tests.utils.GdxTest;

public class KinematicBodyTest extends GdxTest {

	OrthographicCamera cam;
	World world;
	Box2DDebugRenderer renderer;

	public void create () {
		cam = new OrthographicCamera(48, 32);
		cam.position.set(0, 15, 0);
		renderer = new Box2DDebugRenderer();

		world = new World(new Vector2(0, -10), true);
		Body body = world.createBody(new BodyDef());
		CircleShape shape = new CircleShape();
		shape.setRadius(1f);
		MassData mass = new MassData();
		mass.mass = 1f;
		body.setMassData(mass);
		body.setFixedRotation(true);
		body.setType(BodyType.KinematicBody);
		body.createFixture(shape, 1);
		body.setBullet(true);
		body.setTransform(new Vector2(0, 0), body.getAngle());
		body.setLinearVelocity(new Vector2(50f, 0));
	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		world.step(Math.min(0.032f, Gdx.graphics.getDeltaTime()), 3, 4);
		cam.update();
		renderer.render(world, cam.combined);
	}

	@Override
	public void dispose () {
		world.dispose();
		renderer.dispose();
	}
}
