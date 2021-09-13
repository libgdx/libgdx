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
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;

public class CollisionFiltering extends Box2DTest {
	private final static short k_smallGroup = 1;
	private final static short k_largeGroup = -1;

	private final static short k_defaultCategory = 0x0001;
	private final static short k_triangleCategory = 0x0002;
	private final static short k_boxCategory = 0x0004;
	private final static short k_circleCategory = 0x0008;

	private final static short k_triangleMask = -1;
	private final static short k_boxMask = -1 ^ k_triangleCategory;
	private final static short k_circleMask = -1;

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

		Vector2[] vertices = new Vector2[3];
		vertices[0] = new Vector2(-1, 0);
		vertices[1] = new Vector2(1, 0);
		vertices[2] = new Vector2(0, 2);
		PolygonShape polygon = new PolygonShape();
		polygon.set(vertices);

		FixtureDef triangleShapeDef = new FixtureDef();
		triangleShapeDef.shape = polygon;
		triangleShapeDef.density = 1.0f;

		triangleShapeDef.filter.groupIndex = k_smallGroup;
		triangleShapeDef.filter.categoryBits = k_triangleCategory;
		triangleShapeDef.filter.maskBits = k_triangleMask;

		BodyDef triangleBodyDef = new BodyDef();
		triangleBodyDef.type = BodyType.DynamicBody;
		triangleBodyDef.position.set(-5, 2);

		Body body1 = world.createBody(triangleBodyDef);
		body1.createFixture(triangleShapeDef);

		vertices[0].scl(2);
		vertices[1].scl(2);
		vertices[2].scl(2);

		polygon.set(vertices);
		triangleShapeDef.filter.groupIndex = k_largeGroup;
		triangleBodyDef.position.set(-5, 6);
		triangleBodyDef.fixedRotation = true;

		Body body2 = world.createBody(triangleBodyDef);
		body2.createFixture(triangleShapeDef);

		{
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DynamicBody;
			bd.position.set(-5, 10);
			Body body = world.createBody(bd);

			PolygonShape p = new PolygonShape();
			p.setAsBox(0.5f, 1.0f);
			body.createFixture(p, 1);

			PrismaticJointDef jd = new PrismaticJointDef();
			jd.bodyA = body2;
			jd.bodyB = body;
			jd.enableLimit = true;
			jd.localAnchorA.set(0, 4);
			jd.localAnchorB.set(0, 0);
			jd.localAxisA.set(0, 1);
			jd.lowerTranslation = -1;
			jd.upperTranslation = 1;

			world.createJoint(jd);

			p.dispose();
		}

		polygon.setAsBox(1, 0.5f);
		FixtureDef boxShapeDef = new FixtureDef();
		boxShapeDef.shape = polygon;
		boxShapeDef.density = 1;
		boxShapeDef.restitution = 0.1f;

		boxShapeDef.filter.groupIndex = k_smallGroup;
		boxShapeDef.filter.categoryBits = k_boxCategory;
		boxShapeDef.filter.maskBits = k_boxMask;

		BodyDef boxBodyDef = new BodyDef();
		boxBodyDef.type = BodyType.DynamicBody;
		boxBodyDef.position.set(0, 2);

		Body body3 = world.createBody(boxBodyDef);
		body3.createFixture(boxShapeDef);

		polygon.setAsBox(2, 1);
		boxShapeDef.filter.groupIndex = k_largeGroup;
		boxBodyDef.position.set(0, 6);

		Body body4 = world.createBody(boxBodyDef);
		body4.createFixture(boxShapeDef);

		CircleShape circle = new CircleShape();
		circle.setRadius(1);

		FixtureDef circleShapeDef = new FixtureDef();
		circleShapeDef.shape = circle;
		circleShapeDef.density = 1.0f;

		circleShapeDef.filter.groupIndex = k_smallGroup;
		circleShapeDef.filter.categoryBits = k_circleCategory;
		circleShapeDef.filter.maskBits = k_circleMask;

		BodyDef circleBodyDef = new BodyDef();
		circleBodyDef.type = BodyType.DynamicBody;
		circleBodyDef.position.set(5, 2);

		Body body5 = world.createBody(circleBodyDef);
		body5.createFixture(circleShapeDef);

		circle.setRadius(2);
		circleShapeDef.filter.groupIndex = k_largeGroup;
		circleBodyDef.position.set(5, 6);

		Body body6 = world.createBody(circleBodyDef);
		body6.createFixture(circleShapeDef);
	}

}
