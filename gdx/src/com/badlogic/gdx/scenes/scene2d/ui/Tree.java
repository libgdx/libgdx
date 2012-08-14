
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
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
	private float leftColumnWidth, prefWidth, prefHeight;
	private boolean sizeInvalid = true;

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
			if (y >= node.rightActor.getY() && y <= node.rightActor.getY() + node.rightActor.getHeight()) {
				if (x < leftColumnWidth - iconSpacing) return;
				if (x < node.rightActor.getX()) {
					node.setExpanded(!node.expanded);
					return;
				}
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

	private void computeSize () {
		sizeInvalid = false;
		prefWidth = style.plus.getMinWidth();
		prefWidth = Math.max(prefWidth, style.minus.getMinWidth());
		prefHeight = getHeight();
		leftColumnWidth = 0;
		computeSize(rootNodes, indentSpacing);
		leftColumnWidth += iconSpacing;
		prefWidth += leftColumnWidth;
		prefHeight = getHeight() - (prefHeight + ySpacing);
	}

	private void computeSize (Array<Node> nodes, float indent) {
		float ySpacing = this.ySpacing;
		for (int i = 0, n = nodes.size; i < n; i++) {
			Node node = nodes.get(i);
			float rowWidth = indent + iconSpacing, rowHeight;
			Actor actor = node.rightActor;
			if (actor instanceof Layout) {
				Layout layout = (Layout)actor;
				rowWidth += layout.getPrefWidth();
				rowHeight = layout.getPrefHeight();
				layout.pack();
			} else {
				rowWidth += actor.getWidth();
				rowHeight = actor.getHeight();
			}
			actor = node.leftActor;
			if (actor instanceof Layout) {
				Layout layout = (Layout)actor;
				leftColumnWidth = Math.max(leftColumnWidth, layout.getPrefWidth());
				rowHeight = Math.max(rowHeight, layout.getPrefHeight());
				layout.pack();
			} else if (actor != null) {
				leftColumnWidth = Math.max(leftColumnWidth, actor.getWidth());
				rowHeight = Math.max(rowHeight, actor.getHeight());
			}
			prefWidth = Math.max(prefWidth, rowWidth);
			prefHeight -= rowHeight + ySpacing;
			if (node.expanded) computeSize(node.children, indent + indentSpacing);
		}
	}

	public void layout () {
		if (sizeInvalid) computeSize();
		layout(rootNodes, leftColumnWidth + indentSpacing, getHeight());
	}

	private float layout (Array<Node> nodes, float indent, float y) {
		float ySpacing = this.ySpacing;
		Drawable plus = style.plus, minus = style.minus;
		for (int i = 0, n = nodes.size; i < n; i++) {
			Node node = nodes.get(i);
			Actor actor = node.rightActor;
			Drawable icon = node.expanded ? minus : plus;
			y -= actor instanceof Layout ? ((Layout)actor).getPrefHeight() : actor.getHeight();
			if (node.leftActor != null) node.leftActor.setPosition(0, y);
			node.rightActor.setPosition(indent, y);
			y -= ySpacing;
			if (node.expanded) y = layout(node.children, indent + indentSpacing, y);
		}
		return y;
	}

	public void draw (SpriteBatch batch, float parentAlpha) {
		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		if (style.background != null) style.background.draw(batch, getX(), getY(), getWidth(), getHeight());
		draw(batch, rootNodes, leftColumnWidth);
		super.draw(batch, parentAlpha);
	}

	private void draw (SpriteBatch batch, Array<Node> nodes, float indent) {
		Drawable plus = style.plus, minus = style.minus;
		float x = getX(), y = getY();
		for (int i = 0, n = nodes.size; i < n; i++) {
			Node node = nodes.get(i);
			Actor actor = node.rightActor;

			if (selectedNode == node && style.selection != null) {
				float rowHeight = actor instanceof Layout ? ((Layout)actor).getPrefHeight() : actor.getHeight();
				style.selection.draw(batch, x, y + actor.getY(), getWidth(), rowHeight);
			}

			if (node.children == null || node.children.size == 0) continue;

			Drawable icon = node.expanded ? minus : plus;
			float iconY = actor.getY() + actor.getHeight() / 2 - icon.getMinHeight() / 2;
			icon.draw(batch, x + indent, y + iconY, icon.getMinWidth(), icon.getMinHeight());
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
		if (sizeInvalid) computeSize();
		return prefWidth;
	}

	public float getPrefHeight () {
		if (sizeInvalid) computeSize();
		return prefHeight;
	}

	static public class Node {
		Actor leftActor, rightActor;
		Node parent;
		Array<Node> children;
		boolean expanded;

		public Node (Actor rightActor) {
			if (rightActor == null) throw new IllegalArgumentException("rightActor cannot be null.");
			this.rightActor = rightActor;
		}

		/** @@param leftActor May be null. */
		public Node (Actor leftActor, Actor rightActor) {
			if (rightActor == null) throw new IllegalArgumentException("rightActor cannot be null.");
			this.rightActor = rightActor;
			this.leftActor = leftActor;
		}

		public void setExpanded (boolean expanded) {
			if (expanded == this.expanded || children == null || children.size == 0) return;
			this.expanded = expanded;
			Group parent = rightActor.getParent();
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
			if (leftActor != null) tree.addActor(leftActor);
			tree.addActor(rightActor);
			if (!expanded) return;
			for (int i = 0, n = children.size; i < n; i++)
				children.get(i).addToTree(tree);
		}

		void removeFromTree (Tree tree) {
			if (leftActor != null) tree.removeActor(leftActor);
			tree.removeActor(rightActor);
			if (!expanded) return;
			for (int i = 0, n = children.size; i < n; i++)
				children.get(i).removeFromTree(tree);
		}

		public void add (Node node) {
			node.parent = this;
			if (children == null) children = new Array(2);
			children.add(node);
			if (!expanded) return;
			Group parent = rightActor.getParent();
			if (!(parent instanceof Tree)) return;
			Tree tree = (Tree)parent;
			for (int i = 0, n = children.size; i < n; i++)
				children.get(i).addToTree(tree);
		}

		public void remove (Node node) {
			if (children == null) return;
			children.removeValue(node, true);
			if (!expanded) return;
			Group parent = rightActor.getParent();
			if (!(parent instanceof Tree)) return;
			Tree tree = (Tree)parent;
			node.removeFromTree(tree);
			if (children.size == 0) expanded = false;
		}

		public Actor getActor () {
			return rightActor;
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
