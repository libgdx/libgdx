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
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class DebugRendererTest extends Box2DTest {

	@Override
	protected void createWorld (World world) {
		{
			EdgeShape shape = new EdgeShape();
			shape.set(new Vector2(-40.0f, 0), new Vector2(40, 0));

			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.friction = 0.3f;

			BodyDef bd = new BodyDef();
			Body ground = world.createBody(bd);
			ground.createFixture(fd);
			shape.dispose();
		}

		{
			PolygonShape shape = new PolygonShape();
			shape.setAsBox(1, 2f);

			BodyDef def = new BodyDef();
			def.position.y = 10;
			def.angle = (float)Math.toRadians(90);
			def.type = BodyType.DynamicBody;

			Body body = world.createBody(def);
			body.createFixture(shape, 1);

			def = new BodyDef();
			def.position.x = 10;
			def.position.y = 10;
			def.angle = 0;
			def.type = BodyType.DynamicBody;

			body = world.createBody(def);
			body.createFixture(shape, 1);

			shape.dispose();
		}

	}
}
