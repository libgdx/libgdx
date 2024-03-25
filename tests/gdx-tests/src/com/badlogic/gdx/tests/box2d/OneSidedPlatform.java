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
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.ContactFilter;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class OneSidedPlatform extends Box2DTest {
	enum State {
		Unknown, Above, Below
	}

	Fixture m_platform;
	Fixture m_character;
	float m_bottom;
	float m_top;
	float m_radius;
	State m_state;

	@Override
	protected void createWorld (World world) {
		{
			BodyDef bd = new BodyDef();
			Body ground = world.createBody(bd);

			EdgeShape shape = new EdgeShape();
			shape.set(new Vector2(-20.0f, 0), new Vector2(20.0f, 0f));
			ground.createFixture(shape, 0);
			shape.dispose();
		}

		{
			BodyDef bd = new BodyDef();
			bd.position.set(0, 10);
			Body body = world.createBody(bd);

			PolygonShape shape = new PolygonShape();
			shape.setAsBox(3, 0.5f);
			m_platform = body.createFixture(shape, 0);
			m_bottom = 10.0f - 0.5f;
			m_top = 10.0f + 0.5f;
		}

		{
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DynamicBody;
			bd.position.set(0, 12);
			Body body = world.createBody(bd);

			m_radius = 0.5f;
			CircleShape shape = new CircleShape();
			shape.setRadius(m_radius);
			m_character = body.createFixture(shape, 20.0f);
			shape.dispose();

			m_state = State.Unknown;
		}

		world.setContactFilter(new ContactFilter() {

			@Override
			public boolean shouldCollide (Fixture fixtureA, Fixture fixtureB) {
				if ((fixtureA == m_platform && fixtureB == m_character) || (fixtureB == m_platform && fixtureA == m_character)) {
					Vector2 position = m_character.getBody().getPosition();
					if (position.y < m_top + m_radius - 3.0f * 0.005f)
						return false;
					else
						return true;
				} else
					return true;
			}

		});
	}

}
