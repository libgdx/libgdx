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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.tests.utils.GdxTest;

public class Box2DInitialOverlapTest extends GdxTest {
	World world = null; // Box2D world
	int i = 0;
	Body b1 = null, b2 = null;

	Body makeBody () {
		CircleShape cshape = new CircleShape();
		cshape.setRadius(5);

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = cshape;
		fixtureDef.density = 0.05f;
		fixtureDef.friction = 0.3f;
		fixtureDef.filter.categoryBits = 2;
		fixtureDef.filter.maskBits = 4;

		Body body = world.createBody(bodyDef);
		body.createFixture(fixtureDef);

		cshape.dispose();
		return body;
	}

	@Override
	public void create () {
		// World with no gravity
		world = new World(new Vector2(0, 0), true);

		// Do one step before we create the bodies. If we don't do this step first, we don't see the bug.
		world.step(1.0f / 75.0f, 3, 3);

		b1 = makeBody();
		b2 = makeBody();

		/************************************************* From my testing, this line seems to trigger the weird behavior. If I comment this line out (the setTransform call), then
		 * filtering seems to work, and I don't see the bug anymore. Keep in mind I am running on a Ubuntu 10.10 x86_64 box, so I
		 * don't know if all platforms will exhibit this behavior in the same way. ************************************************/
		b1.setTransform(new Vector2(0.1f, 0f), 0);
	}

	@Override
	public void dispose () {
	}

	@Override
	public void pause () {
	}

	@Override
	public void render () {
		if (i < 30) { // don't bother continuing after 30 time steps...
			world.step(1.0f / 75.0f, 3, 3);
			System.out.println("b1 (" + b1.getWorldCenter().x + "," + b1.getWorldCenter().y + ")" + " b2 (" + b2.getWorldCenter().x
				+ "," + b2.getWorldCenter().y + ")");
			++i;
		}
	}

	@Override
	public void resize (int arg0, int arg1) {
	}

	@Override
	public void resume () {
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}
}
