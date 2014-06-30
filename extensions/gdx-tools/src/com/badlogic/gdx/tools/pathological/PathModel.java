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
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.math.Vector2;

class PathModel implements Serializable {
	private static int numPaths = 0;

	private static final Color STROKE_COLOR = Color.BLUE;
	private static final Color FILL_COLOR = Color.RED;
	private static final Color SELECTED_STROKE = Color.CYAN;
	private static final Color SELECTED_FILL = Color.ORANGE;

	private List<PathNode> nodes;
	private transient Path<Vector2> path;

	private PathType type;
	private String name;
	private float radius;
	private boolean continuous;

	private transient boolean changed;

	public PathModel () {
		this.nodes = new ArrayList<>();
		this.radius = 8;
		this.type = PathType.BSPLINE;
		this.name = "path" + (numPaths++);
	}

	public PathModel (String name) {
		this();
		this.name = name;
	}

	public void addNode (float x, float y, int snap) {
		this.nodes.add(new PathNode(x, y));
		this.path = null;
		this.changed = true;
	}

	public void unsetChanged () {
		this.changed = false;
	}

	public boolean hasChanged () {
		return this.changed;
	}

	public void addNode (float x, float y) {
		this.addNode(x, y, 0);
	}

	public PathNode getNode (int x, int y) {
		for (PathNode node : this.nodes) {
			if (node.contains(x, y)) {
				return node;
			}
		}

		return null;
	}

	public void setName (String name) {
		if (!Objects.equals(this.name, name)) {
			this.name = name;
			this.changed = true;
		}
	}

	public String getName () {
		return this.name;
	}

	public boolean isContinuous () {
		return this.continuous;
	}

	public void setContinuous (boolean continuous) {
		if (this.continuous != continuous) {
			this.continuous = continuous;
			this.path = null;
			this.changed = true;
		}
	}

	public void setType (PathType type) {
		if (this.type != type) {
			this.type = type;
			this.path = null;
			this.changed = true;
		}
	}

	public PathType getType () {
		return this.type;
	}

	public void select (PathNode node) {
		this.select(node, !node.selected);
	}

	public void select (PathNode node, boolean selected) {
		node.selected = selected;
	}

	public void unselectAll () {
		for (PathNode node : this.nodes) {
			node.selected = false;
		}
	}

	public void deleteSelected () {
		Collection<PathNode> toDelete = new HashSet<>(this.getNumberOfNodes());
		for (PathNode node : this.nodes) {
			if (node.selected) {
				toDelete.add(node);
				this.path = null;
				this.changed = true;
			}
		}

		if (!toDelete.isEmpty()) {
			this.nodes.removeAll(toDelete);
		}
	}

	public boolean anySelected () {
		for (PathNode node : this.nodes) {
			if (node.selected) return true;
		}

		return false;
	}

	public PathNode getNode (int index) {
		return this.nodes.get(index);
	}

	public int getNumberOfNodes () {
		return this.nodes.size();
	}

	public Path<Vector2> getPath () {
		if (this.path == null) {
			this.path = this.type.toPath(this, this.continuous);
		}

		return this.path;
	}

	public void draw (Graphics2D g) {
		if (this.nodes.size() <= 0) return;

		try {
			this.type.drawPath(this, g, Color.yellow);

			for (PathNode node : this.nodes) {
				node.draw(g);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void moveSelectedNodesBy (int x, int y) {
		this.moveSelectedNodesBy(x, y, 0);
	}

	public void moveSelectedNodesBy (int x, int y, int snap) {
		for (PathNode node : this.nodes) {
			if (node.selected) {
				this.path = null;

				node.ellipse.x += x;
				node.ellipse.y += y;

				if (snap > 0) {
					node.ellipse.setFrame(Math.round(node.ellipse.getX() / snap) * (float)snap - radius,
						Math.round(node.ellipse.getY() / snap) * (float)snap - radius, radius * 2, radius * 2);
				}

				this.changed = true;
			}
		}
	}

	@Override
	public String toString () {
		return this.name;
	}

	public class PathNode implements Serializable {
		private boolean selected;
		private Ellipse2D.Float ellipse;

		private PathNode () {
			this(0, 0);
			this.selected = false;
		}

		private PathNode (float x, float y) {
			super();
			this.ellipse = new Ellipse2D.Float(x - radius, y - radius, radius * 2, radius * 2);
		}

		private void draw (Graphics2D g) {
			Color c = g.getColor();
			g.setColor(this.selected ? SELECTED_FILL : FILL_COLOR);

			g.fill(this.ellipse);

			g.setColor(this.selected ? SELECTED_STROKE : STROKE_COLOR);
			g.draw(this.ellipse);
			g.setColor(c);
		}

		public float getX () {
			return (float)this.ellipse.getCenterX();
		}

		public float getY () {
			return (float)this.ellipse.getCenterY();
		}

		public boolean contains (int x, int y) {
			return this.ellipse.contains(x, y);
		}
	}
}
