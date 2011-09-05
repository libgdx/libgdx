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
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;

public class Cantilever extends Box2DTest {
	Body m_middle;
	final int e_count = 8;

	@Override
	protected void createWorld (World world) {
		Body ground;
		{
			BodyDef bd = new BodyDef();
			ground = world.createBody(bd);

			EdgeShape shape = new EdgeShape();
			shape.set(new Vector2(-40, 0), new Vector2(40, 0));
			ground.createFixture(shape, 0);
			shape.dispose();
		}

		{
			PolygonShape shape = new PolygonShape();
			shape.setAsBox(0.5f, 0.125f);

			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.density = 20.0f;

			WeldJointDef jd = new WeldJointDef();

			Body prevBody = ground;
			for (int i = 0; i < e_count; i++) {
				BodyDef bd = new BodyDef();
				bd.type = BodyType.DynamicBody;
				bd.position.set(-14.5f + 1.0f * i, 5.0f);
				Body body = world.createBody(bd);
				body.createFixture(fd);

				Vector2 anchor = new Vector2(-15.0f + 1 * i, 5.0f);
				jd.initialize(prevBody, body, anchor);
				world.createJoint(jd);
				prevBody = body;
			}

			shape.dispose();
		}

		{
			PolygonShape shape = new PolygonShape();
			shape.setAsBox(0.5f, 0.125f);

			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.density = 20.0f;

			WeldJointDef jd = new WeldJointDef();

			Body prevBody = ground;
			for (int i = 0; i < e_count; i++) {
				BodyDef bd = new BodyDef();
				bd.type = BodyType.DynamicBody;
				bd.position.set(-14.5f + 1.0f * i, 15.0f);
				bd.gravityScale = 10.0f;
				Body body = world.createBody(bd);
				body.createFixture(fd);

				Vector2 anchor = new Vector2(-15.0f + 1.0f * i, 15.0f);
				jd.initialize(prevBody, body, anchor);
				world.createJoint(jd);

				prevBody = body;
			}

			shape.dispose();
		}

		{
			PolygonShape shape = new PolygonShape();
			shape.setAsBox(0.5f, 0.125f);

			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.density = 20.0f;

			WeldJointDef jd = new WeldJointDef();

			Body prevBody = ground;
			for (int i = 0; i < e_count; i++) {
				BodyDef bd = new BodyDef();
				bd.type = BodyType.DynamicBody;
				bd.position.set(-4.5f + 1.0f * i, 5.0f);
				Body body = world.createBody(bd);
				body.createFixture(fd);

				if (i > 0) {
					Vector2 anchor = new Vector2(-5.0f + 1.0f * i, 5.0f);
					jd.initialize(prevBody, body, anchor);
					world.createJoint(jd);
				}

				prevBody = body;
			}

			shape.dispose();
		}

		{
			PolygonShape shape = new PolygonShape();
			shape.setAsBox(0.5f, 0.125f);

			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.density = 20.0f;

			WeldJointDef jd = new WeldJointDef();

			Body prevBody = ground;
			for (int i = 0; i < e_count; i++) {
				BodyDef bd = new BodyDef();
				bd.type = BodyType.DynamicBody;
				bd.position.set(5.5f + 1.0f * i, 10.0f);
				bd.gravityScale = 10.0f;
				Body body = world.createBody(bd);
				body.createFixture(fd);

				if (i > 0) {
					Vector2 anchor = new Vector2(5.0f + 1.0f * i, 10.0f);
					jd.initialize(prevBody, body, anchor);
					world.createJoint(jd);
				}

				prevBody = body;
			}

			shape.dispose();
		}

		for (int i = 0; i < 2; i++) {
			Vector2[] vertices = new Vector2[3];
			vertices[0] = new Vector2(-0.5f, 0);
			vertices[1] = new Vector2(0.5f, 0);
			vertices[2] = new Vector2(0, 1.5f);

			PolygonShape shape = new PolygonShape();
			shape.set(vertices);

			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.density = 1.0f;

			BodyDef bd = new BodyDef();
			bd.type = BodyType.DynamicBody;
			bd.position.set(-8.0f + 8.0f * i, 12.0f);
			Body body = world.createBody(bd);
			body.createFixture(fd);

			shape.dispose();
		}

		for (int i = 0; i < 2; i++) {
			CircleShape shape = new CircleShape();
			shape.setRadius(0.5f);

			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.density = 1.0f;

			BodyDef bd = new BodyDef();
			bd.type = BodyType.DynamicBody;
			bd.position.set(-6.0f + 6.0f * i, 10.0f);
			Body body = world.createBody(bd);
			body.createFixture(fd);
			shape.dispose();
		}
	}

}
