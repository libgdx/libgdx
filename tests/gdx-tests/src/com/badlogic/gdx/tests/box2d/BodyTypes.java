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

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

public class BodyTypes extends Box2DTest {
	Body m_attachment;
	Body m_platform;
	float m_speed;

	@Override
	protected void createWorld (World world) {
		Body ground;

		{
			BodyDef bd = new BodyDef();
			ground = world.createBody(bd);

			EdgeShape shape = new EdgeShape();
			shape.set(new Vector2(-20, 0), new Vector2(20, 0));

			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			ground.createFixture(fd);
			shape.dispose();
		}

		{
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DynamicBody;
			bd.position.set(0, 3.0f);
			m_attachment = world.createBody(bd);

			PolygonShape shape = new PolygonShape();
			shape.setAsBox(0.5f, 2.0f);
			m_attachment.createFixture(shape, 2.0f);
			shape.dispose();
		}

		{
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DynamicBody;
			bd.position.set(-4.0f, 5.0f);
			m_platform = world.createBody(bd);

			PolygonShape shape = new PolygonShape();
			shape.setAsBox(0.5f, 4.0f, new Vector2(4.0f, 0), 0.5f * (float)Math.PI);

			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.friction = 0.6f;
			fd.density = 2.0f;

			m_platform.createFixture(fd);
			shape.dispose();

			RevoluteJointDef rjd = new RevoluteJointDef();
			rjd.initialize(m_attachment, m_platform, new Vector2(0, 5.0f));
			rjd.maxMotorTorque = 50.0f;
			rjd.enableMotor = true;
			world.createJoint(rjd);

			PrismaticJointDef pjd = new PrismaticJointDef();
			pjd.initialize(ground, m_platform, new Vector2(0, 5.0f), new Vector2(1, 0));

			pjd.maxMotorForce = 1000.0f;
			pjd.enableMotor = true;
			pjd.lowerTranslation = -10f;
			pjd.upperTranslation = 10.0f;
			pjd.enableLimit = true;

			world.createJoint(pjd);

			m_speed = 3.0f;
		}

		{
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DynamicBody;
			bd.position.set(0, 8.0f);
			Body body = world.createBody(bd);

			PolygonShape shape = new PolygonShape();
			shape.setAsBox(0.75f, 0.75f);

			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.friction = 0.6f;
			fd.density = 2.0f;

			body.createFixture(fd);
			shape.dispose();
		}
	}

	private final Vector2 tmp = new Vector2();

	@Override
	public boolean keyDown (int keyCode) {
		if (keyCode == Keys.D) m_platform.setType(BodyType.DynamicBody);
		if (keyCode == Keys.S) m_platform.setType(BodyType.StaticBody);
		if (keyCode == Keys.K) {
			m_platform.setType(BodyType.KinematicBody);
			m_platform.setLinearVelocity(tmp.set(-m_speed, 0));
			m_platform.setAngularVelocity(0);
		}

		return false;
	}

	@Override
	public void render () {
		if (m_platform.getType() == BodyType.KinematicBody) {
			Vector2 p = m_platform.getTransform().getPosition();
			Vector2 v = m_platform.getLinearVelocity();

			if ((p.x < -10 && v.x < 0) || (p.x > 10 && v.x > 0)) {
				v.x = -v.x;
				m_platform.setLinearVelocity(v);
			}
		}

		super.render();

		// if (renderer.batch != null) {
		// renderer.batch.begin();
		// // renderer.batch.drawText(renderer.font, "Keys: (d) dynamic, (s) static, (k) kinematic", 0, Gdx.app.getGraphics()
		// // .getHeight(), Color.WHITE);
		// renderer.batch.end();
		// }
	}
}
