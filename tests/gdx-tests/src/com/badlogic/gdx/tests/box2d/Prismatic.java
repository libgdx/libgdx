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
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;

public class Prismatic extends Box2DTest {
	PrismaticJoint m_joint;

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
			shape.setAsBox(2, 5);

			BodyDef bd = new BodyDef();
			bd.type = BodyType.DynamicBody;
			bd.position.set(-10, 10);
			bd.angle = 0.5f * (float)Math.PI;
			bd.allowSleep = false;

			Body body = world.createBody(bd);
			body.createFixture(shape, 5.0f);

			PrismaticJointDef pjd = new PrismaticJointDef();

			Vector2 axis = new Vector2(2, 1);
			axis.nor();
			pjd.initialize(ground, body, new Vector2(0, 0), axis);

			pjd.motorSpeed = 10.0f;
			pjd.maxMotorForce = 10000.0f;
			pjd.enableMotor = true;
			pjd.lowerTranslation = 0;
			pjd.upperTranslation = 20.0f;
			pjd.enableLimit = true;

			m_joint = (PrismaticJoint)world.createJoint(pjd);
		}
	}

	public boolean keyDown (int keyCode) {
		if (keyCode == Keys.L) m_joint.enableLimit(!m_joint.isLimitEnabled());
		if (keyCode == Keys.M) m_joint.enableMotor(!m_joint.isMotorEnabled());
		if (keyCode == Keys.S) m_joint.setMotorSpeed(-m_joint.getMotorSpeed());

		return false;

	}

	public void render () {
		super.render();

		// if (renderer.batch != null) {
		// renderer.batch.begin();
		// // renderer.batch.drawText(renderer.font, "Keys: (l) limits, (m) motors, (s) speed", 0,
// Gdx.app.getGraphics().getHeight(),
		// // Color.WHITE);
		// // renderer.batch.drawText(renderer.font, "Motor Force = " + m_joint.getMotorForce(), 0,
		// // Gdx.app.getGraphics().getHeight() - 15, Color.WHITE);
		// renderer.batch.end();
		// }
	}
}
