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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Pyramid extends Box2DTest {
	@Override
	protected void createWorld (World world) {
		{
			BodyDef bd = new BodyDef();
			Body ground = world.createBody(bd);

			EdgeShape shape = new EdgeShape();
			shape.set(new Vector2(-40, 0), new Vector2(40, 0));
			ground.createFixture(shape, 0.0f);
			shape.dispose();
		}

		{
			float a = 0.5f;
			PolygonShape shape = new PolygonShape();
			shape.setAsBox(a, a);

			Vector2 x = new Vector2(-7.0f, 0.75f);
			Vector2 y = new Vector2();
			Vector2 deltaX = new Vector2(0.5625f, 1.25f);
			Vector2 deltaY = new Vector2(1.125f, 0.0f);

			for (int i = 0; i < 20; i++) {
				y.set(x);

				for (int j = i; j < 20; j++) {
					BodyDef bd = new BodyDef();
					bd.type = BodyType.DynamicBody;
					bd.position.set(y);
					Body body = world.createBody(bd);
					body.createFixture(shape, 5.0f);

					y.add(deltaY);
				}

				x.add(deltaX);
			}

		}
	}
}
