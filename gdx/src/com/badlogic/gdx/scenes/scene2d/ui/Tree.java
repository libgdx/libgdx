
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;

/** A simple tree widget where each node is an actor.
 * <p>
 * The preferred size of the tree is determined by the preferred size of the actors for the expanded nodes.
 * @author Nathan Sweet */
public class Tree extends WidgetGroup {
	TreeStyle style;
	Array<Node> nodes = new Array();
	float ySpacing = 2, iconSpacing = 5, indentSpacing;
	private float prefWidth, prefHeight;

	public Tree (Skin skin) {
		this(skin.get(TreeStyle.class));
	}

	public Tree (Skin skin, String styleName) {
		this(skin.get(styleName, TreeStyle.class));
	}

	public Tree (TreeStyle style) {
		setStyle(style);
		initialize();
	}

	private void initialize () {
		addListener(new ClickListener() {
			public void clicked (InputEvent event, float x, float y) {
				if (event.getTarget() != Tree.this) return;
				expand(nodes, x, y);
			}
		});
	}

	void expand (Array<Node> nodes, float x, float y) {
		for (int i = 0, n = nodes.size; i < n; i++) {
			Node node = nodes.get(i);
			if (y >= node.actor.getY() && y <= node.actor.getY() + node.actor.getHeight()) {
				node.setExpanded(!node.expanded);
				return;
			}
			if (node.expanded) expand(node.nodes, x, y);
		}
	}

	public void setStyle (TreeStyle style) {
		this.style = style;
		indentSpacing = Math.max(style.plus.getMinWidth(), style.minus.getMinWidth()) + iconSpacing * 2;
	}

	public void add (Node node) {
		nodes.add(node);
		addActors(node);
	}

	private void addActors (Node node) {
		if (node.actor.getParent() != this) addActor(node.actor);
		if (node.nodes == null) return;
		for (int i = 0, n = node.nodes.size; i < n; i++)
			addActors(node.nodes.get(i));
	}

	public Array<Node> getNodes () {
		return nodes;
	}

	public void layout () {
		prefWidth = style.plus.getMinWidth();
		prefWidth = Math.max(prefWidth, style.minus.getMinWidth());
		prefHeight = getHeight();
		layout(nodes, indentSpacing);
		prefHeight = getHeight() - (prefHeight + ySpacing);
	}

	private void layout (Array<Node> nodes, float indent) {
		Drawable plus = style.plus, minus = style.minus;
		float ySpacing = this.ySpacing;

		for (int i = 0, n = nodes.size; i < n; i++) {
			Node node = nodes.get(i);
			Actor actor = node.actor;
			Drawable icon = node.expanded ? minus : plus;
			float rowWidth = indent + iconSpacing, rowHeight;
			if (actor instanceof Widget) {
				Widget widget = (Widget)actor;
				rowWidth += widget.getPrefWidth();
				rowHeight = widget.getPrefHeight();
				widget.pack();
			} else {
				rowWidth += actor.getWidth();
				rowHeight = actor.getHeight();
			}
			prefWidth = Math.max(prefWidth, rowWidth);
			rowHeight = Math.max(rowHeight, icon.getMinHeight());
			prefHeight -= rowHeight;
			node.actor.setPosition(indent, prefHeight);
			prefHeight -= ySpacing;
			if (node.expanded) layout(node.nodes, indent + indentSpacing);
		}
	}

	public void draw (SpriteBatch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		batch.setColor(getColor());
		draw(batch, nodes, iconSpacing);
	}

	private void draw (SpriteBatch batch, Array<Node> nodes, float indent) {
		Drawable plus = style.plus, minus = style.minus;
		for (int i = 0, n = nodes.size; i < n; i++) {
			Node node = nodes.get(i);
			if (node.nodes == null) continue;
			Actor actor = node.actor;
			Drawable icon = node.expanded ? minus : plus;
			float iconY = actor.getY() + actor.getHeight() / 2 - icon.getMinHeight() / 2;
			icon.draw(batch, indent, iconY, icon.getMinWidth(), icon.getMinHeight());
			if (node.expanded) draw(batch, node.nodes, indent + indentSpacing);
		}
	}

	public float getPrefWidth () {
		return prefWidth;
	}

	public float getPrefHeight () {
		return prefHeight;
	}

	static public class Node {
		final Actor actor;
		Array<Node> nodes;
		boolean expanded;

		public Node (Actor actor) {
			this.actor = actor;
		}

		void setExpanded (boolean expanded) {
			if (expanded == this.expanded || nodes == null) return;
			this.expanded = expanded;
			Group parent = actor.getParent();
			if (!(parent instanceof Tree)) return;
			Tree tree = (Tree)parent;
			if (expanded) {
				for (int i = 0, n = nodes.size; i < n; i++)
					nodes.get(i).addToTree(tree);
			} else {
				for (int i = 0, n = nodes.size; i < n; i++)
					nodes.get(i).removeFromTree(tree);
			}
			tree.invalidateHierarchy();
		}

		private void addToTree (Tree tree) {
			tree.addActor(actor);
			if (!expanded) return;
			for (int i = 0, n = nodes.size; i < n; i++)
				nodes.get(i).addToTree(tree);
		}

		private void removeFromTree (Tree tree) {
			tree.removeActor(actor);
			if (!expanded) return;
			for (int i = 0, n = nodes.size; i < n; i++)
				nodes.get(i).removeFromTree(tree);
		}

		public void add (Node node) {
			if (nodes == null) nodes = new Array(2);
			nodes.add(node);
		}
	}

	/** The style for a {@link Tree}.
	 * @author Nathan Sweet */
	static public class TreeStyle {
		public Drawable plus, minus;
	}
}
