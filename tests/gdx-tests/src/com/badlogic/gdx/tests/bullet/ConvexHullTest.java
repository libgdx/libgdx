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

package com.badlogic.gdx.tests.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.badlogic.gdx.physics.bullet.collision.btShapeHull;

/** @author xoppa */
public class ConvexHullTest extends BaseBulletTest {

	@Override
	public void create () {
		super.create();

		final Model carModel = objLoader.loadModel(Gdx.files.internal("data/car.obj"));
		disposables.add(carModel);
		carModel.materials.get(0).clear();
		carModel.materials.get(0).set(ColorAttribute.createDiffuse(Color.WHITE), ColorAttribute.createSpecular(Color.WHITE));
		world.addConstructor("car", new BulletConstructor(carModel, 5f, createConvexHullShape(carModel, true)));

		// Create the entities
		world.add("ground", 0f, 0f, 0f).setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);

		for (float y = 10f; y < 50f; y += 5f)
			world.add("car", -2f + (float)Math.random() * 4f, y, -2f + (float)Math.random() * 4f).setColor(
				0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);
	}

	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y);
		return true;
	}

	public static btConvexHullShape createConvexHullShape (final Model model, boolean optimize) {
		final Mesh mesh = model.meshes.get(0);
		final btConvexHullShape shape = new btConvexHullShape(mesh.getVerticesBuffer(), mesh.getNumVertices(), mesh.getVertexSize());
		if (!optimize) return shape;
		// now optimize the shape
		final btShapeHull hull = new btShapeHull(shape);
		hull.buildHull(shape.getMargin());
		final btConvexHullShape result = new btConvexHullShape(hull);
		// delete the temporary shape
		shape.dispose();
		hull.dispose();
		return result;
	}
}
