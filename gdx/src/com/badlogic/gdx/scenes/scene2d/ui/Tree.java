
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

/** A simple tree widget where each node is an actor.
 * <p>
 * The preferred size of the tree is determined by the preferred size of the actors for the expanded nodes.
 * <p>
 * {@link ChangeEvent} is fired when the selected node changes. Cancelling the event will select the previously selected node.
 * @author Nathan Sweet */
public class Tree extends WidgetGroup {
	TreeStyle style;
	Array<Node> rootNodes = new Array();
	Node selectedNode;
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
				hit(rootNodes, x, y);
			}
		});
	}

	void hit (Array<Node> nodes, float x, float y) {
		for (int i = 0, n = nodes.size; i < n; i++) {
			Node node = nodes.get(i);
			if (y >= node.actor.getY() && y <= node.actor.getY() + node.actor.getHeight()) {
				if (x < node.actor.getX()) node.setExpanded(!node.expanded);
				Node oldNode = selectedNode;
				selectedNode = node;
				ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
				if (fire(changeEvent)) selectedNode = oldNode;
				Pools.free(changeEvent);
				return;
			}
			if (node.expanded) hit(node.children, x, y);
		}
	}

	public void setStyle (TreeStyle style) {
		this.style = style;
		indentSpacing = Math.max(style.plus.getMinWidth(), style.minus.getMinWidth()) + iconSpacing * 2;
	}

	public void add (Node node) {
		node.parent = null;
		rootNodes.add(node);
		node.addToTree(this);
		invalidateHierarchy();
	}

	public void remove (Node node) {
		if (node.parent != null) {
			node.parent.remove(node);
			return;
		}
		rootNodes.removeValue(node, true);
		node.removeFromTree(this);
		invalidateHierarchy();
	}

	public Array<Node> getNodes () {
		return rootNodes;
	}

	public void layout () {
		prefWidth = style.plus.getMinWidth();
		prefWidth = Math.max(prefWidth, style.minus.getMinWidth());
		prefHeight = getHeight();
		layout(rootNodes, indentSpacing);
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
			if (node.expanded) layout(node.children, indent + indentSpacing);
		}
	}

	public void draw (SpriteBatch batch, float parentAlpha) {
		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		if (style.background != null) style.background.draw(batch, getX(), getY(), getWidth(), getHeight());
		draw(batch, rootNodes, iconSpacing);
		super.draw(batch, parentAlpha);
	}

	private void draw (SpriteBatch batch, Array<Node> nodes, float indent) {
		Drawable plus = style.plus, minus = style.minus;
		for (int i = 0, n = nodes.size; i < n; i++) {
			Node node = nodes.get(i);
			Actor actor = node.actor;

			if (selectedNode == node && style.selection != null) {
				float rowHeight = actor instanceof Widget ? ((Widget)actor).getPrefHeight() : actor.getHeight();
				style.selection.draw(batch, 0, actor.getY(), getWidth(), rowHeight);
			}

			if (node.children == null || node.children.size == 0) continue;

			Drawable icon = node.expanded ? minus : plus;
			float iconY = actor.getY() + actor.getHeight() / 2 - icon.getMinHeight() / 2;
			icon.draw(batch, indent, iconY, icon.getMinWidth(), icon.getMinHeight());
			if (node.expanded) draw(batch, node.children, indent + indentSpacing);
		}
	}

	/** @return May be null. */
	public Node getSelection () {
		return selectedNode;
	}

	/** @param node May be null. */
	public void setSelection (Node node) {
		selectedNode = node;
	}

	public float getPrefWidth () {
		return prefWidth;
	}

	public float getPrefHeight () {
		return prefHeight;
	}

	static public class Node {
		final Actor actor;
		Node parent;
		Array<Node> children;
		boolean expanded;

		public Node (Actor actor) {
			this.actor = actor;
		}

		void setExpanded (boolean expanded) {
			if (expanded == this.expanded || children == null || children.size == 0) return;
			this.expanded = expanded;
			Group parent = actor.getParent();
			if (!(parent instanceof Tree)) return;
			Tree tree = (Tree)parent;
			if (expanded) {
				for (int i = 0, n = children.size; i < n; i++)
					children.get(i).addToTree(tree);
			} else {
				for (int i = 0, n = children.size; i < n; i++)
					children.get(i).removeFromTree(tree);
			}
			tree.invalidateHierarchy();
		}

		void addToTree (Tree tree) {
			tree.addActor(actor);
			if (!expanded) return;
			for (int i = 0, n = children.size; i < n; i++)
				children.get(i).addToTree(tree);
		}

		void removeFromTree (Tree tree) {
			tree.removeActor(actor);
			if (!expanded) return;
			for (int i = 0, n = children.size; i < n; i++)
				children.get(i).removeFromTree(tree);
		}

		public void add (Node node) {
			node.parent = this;
			if (children == null) children = new Array(2);
			children.add(node);
			if (!expanded) return;
			Group parent = actor.getParent();
			if (!(parent instanceof Tree)) return;
			Tree tree = (Tree)parent;
			for (int i = 0, n = children.size; i < n; i++)
				children.get(i).addToTree(tree);
		}

		public void remove (Node node) {
			if (children == null) return;
			children.removeValue(node, true);
			if (!expanded) return;
			Group parent = actor.getParent();
			if (!(parent instanceof Tree)) return;
			Tree tree = (Tree)parent;
			node.removeFromTree(tree);
			if (children.size == 0) expanded = false;
		}

		public Actor getActor () {
			return actor;
		}

		public boolean isExpanded () {
			return expanded;
		}

		public Array<Node> getChildren () {
			return children;
		}

		public Node getParent () {
			return parent;
		}
	}

	/** The style for a {@link Tree}.
	 * @author Nathan Sweet */
	static public class TreeStyle {
		public Drawable plus, minus;
		/** Optional. */
		public Drawable selection, background;

		public TreeStyle () {
		}

		public TreeStyle (Drawable plus, Drawable minus, Drawable selection) {
			this.plus = plus;
			this.minus = minus;
			this.selection = selection;
		}

		public TreeStyle (TreeStyle style) {
			this.plus = style.plus;
			this.minus = style.minus;
			this.selection = style.selection;
		}
	}
}
