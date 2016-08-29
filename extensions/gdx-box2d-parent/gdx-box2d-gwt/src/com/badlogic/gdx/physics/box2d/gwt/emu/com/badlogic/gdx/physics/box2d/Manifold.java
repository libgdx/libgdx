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

package com.badlogic.gdx.physics.box2d;

import com.badlogic.gdx.math.Vector2;

public class Manifold {
	org.jbox2d.collision.Manifold manifold;
	final ManifoldPoint[] points = new ManifoldPoint[] {new ManifoldPoint(), new ManifoldPoint()};
	final Vector2 localNormal = new Vector2();
	final Vector2 localPoint = new Vector2();

	final int[] tmpInt = new int[2];
	final float[] tmpFloat = new float[4];

	Manifold () {
	}

	protected Manifold (org.jbox2d.collision.Manifold manifold) {
		this.manifold = manifold;
	}

	public ManifoldType getType () {
		org.jbox2d.collision.Manifold.ManifoldType type = manifold.type;
		if (type == org.jbox2d.collision.Manifold.ManifoldType.CIRCLES) return ManifoldType.Circle;
		if (type == org.jbox2d.collision.Manifold.ManifoldType.FACE_A) return ManifoldType.FaceA;
		if (type == org.jbox2d.collision.Manifold.ManifoldType.FACE_B) return ManifoldType.FaceB;
		return ManifoldType.Circle;
	}

	public int getPointCount () {
		return manifold.pointCount;
	}

	public Vector2 getLocalNormal () {
		return localNormal.set(manifold.localNormal.x, manifold.localNormal.y);
	}

	public Vector2 getLocalPoint () {
		return localPoint.set(manifold.localPoint.x, manifold.localPoint.y);
	}

	public ManifoldPoint[] getPoints () {
		for (int i = 0; i < manifold.points.length; i++) {
			points[i].contactID = manifold.points[i].id.getKey();
			points[i].localPoint.set(manifold.points[i].localPoint.x, manifold.points[i].localPoint.y);
			points[i].normalImpulse = manifold.points[i].normalImpulse;
			points[i].tangentImpulse = manifold.points[i].tangentImpulse;
		}
		return points;
	}

	public class ManifoldPoint {
		public final Vector2 localPoint = new Vector2();
		public float normalImpulse;
		public float tangentImpulse;
		public int contactID = 0;
	}

	public enum ManifoldType {
		Circle, FaceA, FaceB
	}
}
