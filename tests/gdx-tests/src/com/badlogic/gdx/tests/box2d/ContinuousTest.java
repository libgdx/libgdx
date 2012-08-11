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

public class ContinuousTest extends Box2DTest {
	int m_stepCount = 0;
	Body m_body;
	float m_angularVelocity;

	@Override
	protected void createWorld (World world) {
		{
			BodyDef bd = new BodyDef();
			bd.position.set(0, 0);
			Body body = world.createBody(bd);

			EdgeShape shape = new EdgeShape();
			shape.set(new Vector2(-10, 0), new Vector2(10, 0));
			body.createFixture(shape, 0);
			shape.dispose();

			PolygonShape poly = new PolygonShape();
			poly.setAsBox(0.2f, 1.0f, new Vector2(0.5f, 1.0f), 0);
			body.createFixture(poly, 0);
			poly.dispose();
		}

		{
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DynamicBody;
			bd.position.set(0, 20);

			PolygonShape shape = new PolygonShape();
			shape.setAsBox(2, 0.1f);

			m_body = world.createBody(bd);
			m_body.createFixture(shape, 1);

			m_angularVelocity = 33.468121f;
			m_body.setLinearVelocity(new Vector2(0, -100));
			m_body.setAngularVelocity(m_angularVelocity);
			shape.dispose();
		}
	}

	private void launch () {
		m_body.setTransform(new Vector2(0, 20), 0);
		m_angularVelocity = (float)Math.random() * 100 - 50;
		m_body.setLinearVelocity(new Vector2(0, -100));
		m_body.setAngularVelocity(m_angularVelocity);
	}

	public void render () {
		super.render();

		m_stepCount++;
		if (m_stepCount % 60 == 0) launch();
	}
}
