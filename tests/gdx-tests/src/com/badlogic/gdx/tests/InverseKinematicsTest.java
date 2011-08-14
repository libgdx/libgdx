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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;

public class InverseKinematicsTest extends GdxTest {

	@Override
	public boolean needsGL20 () {
		return false;
	}

	static class Bone {
		final float len;
		final Vector3 position = new Vector3();
		final Vector3 inertia = new Vector3();

		public String name;

		public Bone (String name, float x, float y, float len) {
			this.name = name;
			this.position.set(x, y, 0);
			this.len = len;
		}

		public String toString () {
			return "bone " + name + ": " + position + ", " + len;
		}
	}

	static final float GRAVITY = 0;
	OrthographicCamera camera;
	ImmediateModeRenderer10 renderer;
	Bone[] bones;
	Vector3 globalCoords = new Vector3();
	Vector3 endPoint = new Vector3();
	Vector2 diff = new Vector2();

	@Override
	public void create () {
		float aspect = Gdx.graphics.getWidth() / (float)Gdx.graphics.getHeight();
		camera = new OrthographicCamera(15 * aspect, 15);
		renderer = new ImmediateModeRenderer10();

		bones = new Bone[] {new Bone("bone0", 0, 0, 0), new Bone("bone1", 0, 2, 2), new Bone("bone2", 0, 4, 2),
			new Bone("bone3", 0, 6, 2), new Bone("end", 0, 8, 2)};
		globalCoords.set(bones[0].position);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		camera.update();
		camera.apply(Gdx.gl10);

		if (Gdx.input.isTouched()) camera.unproject(globalCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0));
		solveFakeIK(globalCoords);
		renderBones();
	}

	private void renderBones () {
		renderer.begin(GL10.GL_LINES);
		for (int i = 0; i < bones.length - 1; i++) {
			renderer.color(0, 1, 0, 1);
			renderer.vertex(bones[i].position);
			renderer.color(0, 1, 0, 1);
			renderer.vertex(bones[i + 1].position);
		}
		renderer.end();

		Gdx.gl10.glPointSize(5);

		renderer.begin(GL10.GL_POINTS);
		for (int i = 0; i < bones.length; i++) {
			renderer.color(1, 0, 0, 1);
			renderer.vertex(bones[i].position);
		}
		renderer.end();
		Gdx.gl10.glPointSize(1);
	}

	public void solveFakeIK (Vector3 target) {
		float gravity = Gdx.graphics.getDeltaTime() * GRAVITY;

		endPoint.set(target);
		bones[0].position.set(endPoint);

		for (int i = 0; i < bones.length - 1; i++) {
			Bone bone = bones[i];
			endPoint.set(bone.position);

			diff.set(endPoint.x, endPoint.y).sub(bones[i + 1].position.x, bones[i + 1].position.y);
			diff.add(0, gravity);
			diff.add(bones[i + 1].inertia.x, bones[i + 1].inertia.y);
			diff.nor().mul(bones[i + 1].len);

			float x = endPoint.x - diff.x;
			float y = endPoint.y - diff.y;
			float delta = Gdx.graphics.getDeltaTime();
			bones[i + 1].inertia.add((bones[i + 1].position.x - x) * delta, (bones[i + 1].position.y - y) * delta, 0).mul(0.99f);
			bones[i + 1].position.set(x, y, 0);
		}
	}
}