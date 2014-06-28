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

package com.badlogic.gdx.tools.pathological;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import com.badlogic.gdx.math.BSpline;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tools.pathological.PathModel.PathNode;
import com.badlogic.gdx.tools.pathological.util.PathSerializer;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

enum PathType {
	BSPLINE() {
		@Override
		public String toString () {
			return "Basis Spline";
		}

		@Override
		public Path<Vector2> toPath (PathModel model, boolean continuous) {
			Vector2[] vecs = new Vector2[model.getNumberOfNodes()];

			for (int i = 0; i < vecs.length; ++i) {
				PathNode node = model.getNode(i);
				vecs[i] = new Vector2(node.getX(), node.getY());
			}

			BSpline<Vector2> crs = new BSpline<>(vecs, 3, continuous);
			// only 3rd-degree bsplines are supported
			return crs;
		}

		@Override
		public boolean acceptModel (PathModel model) {
			return model.getNumberOfNodes() >= 3;
		}

		@Override
		public String getInvalidPathMessage () {
			return "Basis splines must have at least 3 nodes.";
		}

		@Override
		public boolean supportsClosedPaths () {
			return true;
		}

	},
	BEZIER() {
		@Override
		public String toString () {
			return "Bézier";
		}

		@Override
		public Path<Vector2> toPath (PathModel model, boolean continuous) {
			Vector2[] vecs = new Vector2[model.getNumberOfNodes()];

			for (int i = 0; i < vecs.length; ++i) {
				PathNode node = model.getNode(i);
				vecs[i] = new Vector2(node.getX(), node.getY());
			}
			Bezier<Vector2> bezier = new Bezier<>(vecs);

			return bezier;
		}

		@Override
		public boolean acceptModel (PathModel model) {
			int num = model.getNumberOfNodes();
			return (num >= 2 && num <= 4);
		}

		@Override
		public String getInvalidPathMessage () {
			return "Bézier curves must have exactly 2, 3, or 4 nodes.";
		}

		@Override
		public boolean supportsClosedPaths () {
			return false;
		}
	},
	CATMULLROM() {
		@Override
		public String toString () {
			return "Catmull-Rom";
		}

		@Override
		public Path<Vector2> toPath (PathModel model, boolean continuous) {
			Vector2[] vecs = new Vector2[model.getNumberOfNodes()];

			for (int i = 0; i < vecs.length; ++i) {
				PathNode node = model.getNode(i);
				vecs[i] = new Vector2(node.getX(), node.getY());
			}

			CatmullRomSpline<Vector2> crs = new CatmullRomSpline<>(vecs, continuous);
			return crs;
		}

		@Override
		public boolean acceptModel (PathModel model) {
			return model.getNumberOfNodes() >= 4;
		}

		@Override
		public String getInvalidPathMessage () {
			return "Catmull-Rom splines must have at least 4 nodes.";
		}

		@Override
		public boolean supportsClosedPaths () {
			return true;
		}
	};

	public static final Json json;

	static {
		json = new Json();
		PathSerializer ps = new PathSerializer();
		json.setSerializer(Bezier.class, ps);
		json.setSerializer(BSpline.class, ps);
		json.setSerializer(CatmullRomSpline.class, ps);
		json.setSerializer(Path.class, ps);
	}

	public Path<Vector2> toPath (PathModel model, boolean continuous) {
		return null;
	}

	public boolean acceptModel (PathModel model) {
		return false;
	}

	public void drawPath (PathModel model, Graphics2D g, Color color) {
		Color c = g.getColor();
		Stroke s = g.getStroke();

		g.setColor(Color.WHITE);
		for (int i = 0; i < model.getNumberOfNodes() - 1; ++i) {
			PathNode a = model.getNode(i);
			PathNode b = model.getNode(i + 1);
			g.drawLine((int)a.getX(), (int)a.getY(), (int)b.getX(), (int)b.getY());
		}

		if (this.acceptModel(model)) {

			g.setColor(color);
			g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

			Path<Vector2> path = model.getPath();
			int num = model.getNumberOfNodes();
			float interval = 1.0f / (num * 25);
			Vector2 current = new Vector2();
			Vector2 prev = new Vector2();
			path.valueAt(prev, 0);
			current.set(prev);
			for (float i = 0; i <= 1; i += interval, prev.set(current), current = path.valueAt(current, i)) {
				g.drawLine(Math.round(prev.x), Math.round(prev.y), Math.round(current.x), Math.round(current.y));
			}

			g.setColor(c);
			g.setStroke(s);
		}
	}

	public JsonValue toJson (PathModel model) {
		Path<Vector2> path = model.getPath();

		return new JsonValue(json.toJson(path));
	}

	public String getInvalidPathMessage () {
		return "This path is not valid";
	}

	public abstract boolean supportsClosedPaths ();
}
