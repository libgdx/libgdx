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

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class VerticalStack extends Box2DTest {
	static final int e_columnCount = 5;
	static final int e_rowCount = 16;

	Body m_bullet;
	Body[] m_bodies = new Body[e_rowCount * e_columnCount];
	int[] m_indices = new int[e_rowCount * e_columnCount];

	@Override
	protected void createWorld (World world) {
		{
			BodyDef bd = new BodyDef();
			Body ground = world.createBody(bd);

			EdgeShape shape = new EdgeShape();
			shape.set(new Vector2(-40, 0), new Vector2(40, 0));
			ground.createFixture(shape, 0.0f);

			shape.set(new Vector2(20, 0), new Vector2(20, 20));
			ground.createFixture(shape, 0);
			shape.dispose();
		}

		float xs[] = {0, -10, -5, 5, 10};

		for (int j = 0; j < e_columnCount; j++) {
			PolygonShape shape = new PolygonShape();
			shape.setAsBox(0.5f, 0.5f);

			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.density = 1.0f;
			fd.friction = 0.3f;

			for (int i = 0; i < e_rowCount; i++) {
				BodyDef bd = new BodyDef();
				bd.type = BodyType.DynamicBody;

				int n = j * e_rowCount + i;
				m_indices[n] = n;

				float x = 0;
				bd.position.set(xs[j] + x, 0.752f + 1.54f * i);
				Body body = world.createBody(bd);
				body.setUserData(n);

				m_bodies[n] = body;
				body.createFixture(fd);
			}

			shape.dispose();
		}

		m_bullet = null;
	}

	@Override
	public boolean keyDown (int keyCode) {
		if (keyCode == Input.Keys.COMMA) {
			if (m_bullet != null) {
				world.destroyBody(m_bullet);
				m_bullet = null;
			}

			{
				CircleShape shape = new CircleShape();
				shape.setRadius(0.25f);

				FixtureDef fd = new FixtureDef();
				fd.shape = shape;
				fd.density = 20.0f;
				fd.restitution = 0.05f;

				BodyDef bd = new BodyDef();
				bd.type = BodyType.DynamicBody;
				bd.bullet = true;
				bd.position.set(-31, 5);

				m_bullet = world.createBody(bd);
				m_bullet.createFixture(fd);

				m_bullet.setLinearVelocity(new Vector2(400, 0));
			}
		}

		return false;
	}

	public void render () {
		super.render();

		// if (renderer.batch != null) {
		// renderer.batch.begin();
		// // renderer.batch.drawText(renderer.font, "Press: (,) to launch a bullet", 0, Gdx.app.getGraphics().getHeight(),
		// // Color.WHITE);
		// renderer.batch.end();
		// }
	}
}
