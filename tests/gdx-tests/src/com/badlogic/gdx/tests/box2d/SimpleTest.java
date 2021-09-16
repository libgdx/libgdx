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
/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.tests.box2d;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class SimpleTest extends Box2DTest {
	@Override
	protected void createWorld (World world) {
		// next we create a static ground platform. This platform
		// is not moveable and will not react to any influences from
		// outside. It will however influence other bodies. First we
		// create a PolygonShape that holds the form of the platform.
		// it will be 100 meters wide and 2 meters high, centered
		// around the origin
		PolygonShape groundPoly = new PolygonShape();
		groundPoly.setAsBox(50, 1);

		// next we create the body for the ground platform. It's
		// simply a static body.
		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.type = BodyType.StaticBody;
		groundBody = world.createBody(groundBodyDef);

		// finally we add a fixture to the body using the polygon
		// defined above. Note that we have to dispose PolygonShapes
		// and CircleShapes once they are no longer used. This is the
		// only time you have to care explicitly for memomry managment.
		groundBody.createFixture(groundPoly, 10);
		groundPoly.dispose();

		// next we create 50 boxes at random locations above the ground
		// body. First we create a nice polygon representing a box 2 meters
		// wide and high.
		PolygonShape boxPoly = new PolygonShape();
		boxPoly.setAsBox(1, 1);

		// next we create the 50 box bodies using the PolygonShape we just
		// defined. This process is similar to the one we used for the ground
		// body. Note that we reuse the polygon for each body fixture.
		for (int i = 0; i < 20; i++) {
			// Create the BodyDef, set a random position above the
			// ground and create a new body
			BodyDef boxBodyDef = new BodyDef();
			boxBodyDef.type = BodyType.DynamicBody;
			boxBodyDef.position.x = -24 + (float)(Math.random() * 48);
			boxBodyDef.position.y = 10 + (float)(Math.random() * 100);
			Body boxBody = world.createBody(boxBodyDef);

			// add the boxPoly shape as a fixture
			boxBody.createFixture(boxPoly, 10);
		}

		// we are done, all that's left is disposing the boxPoly
		boxPoly.dispose();

		// next we add a few more circles
		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(1);

		for (int i = 0; i < 10; i++) {
			BodyDef circleBodyDef = new BodyDef();
			circleBodyDef.type = BodyType.DynamicBody;
			circleBodyDef.position.x = -24 + (float)(Math.random() * 48);
			circleBodyDef.position.y = 10 + (float)(Math.random() * 100);
			Body circleBody = world.createBody(circleBodyDef);

			// add the boxPoly shape as a fixture
			circleBody.createFixture(circleShape, 10);
		}
		circleShape.dispose();
	}
}
