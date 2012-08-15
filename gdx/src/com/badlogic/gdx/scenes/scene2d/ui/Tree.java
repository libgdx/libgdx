
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
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

/** A simple tree widget where each node has a left actor, icon, and a right actor.
 * <p>
 * The preferred size of the tree is determined by the preferred size of the actors for the expanded nodes.
 * <p>
 * {@link ChangeEvent} is fired when the selected node changes. Cancelling the event will select the previously selected node.
 * @author Nathan Sweet */
public class Tree extends WidgetGroup {
	TreeStyle style;
	final Array<Node> rootNodes = new Array();
	final Array<Node> selectedNodes = new Array();
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
				hit(rootNodes, x, y, getHeight());
			}
		});
	}

	float hit (Array<Node> nodes, float x, float y, float rowY) {
		for (int i = 0, n = nodes.size; i < n; i++) {
			Node node = nodes.get(i);
			if (y > rowY - node.height - ySpacing / 2 && y <= rowY + ySpacing / 2) {
				if (x < leftColumnWidth - iconSpacing) return -1;
				float rowX = node.rightActor.getX();
				if (node.icon != null) rowX -= iconSpacing + node.icon.getMinWidth();
				// Toggle expanded.
				if (x < rowX) {
					node.setExpanded(!node.expanded);
					return -1;
				}
				// Select range (shift/ctrl).
				if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) && !Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT) && selectedNodes.size > 0) {
					float low = selectedNodes.first().rightActor.getY();
					float high = node.rightActor.getY();
					if (!Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && !Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT))
						selectedNodes.clear();
					if (low > high)
						selectNodes(rootNodes, high, low);
					else
						selectNodes(rootNodes, low, high);
					return -1;
				}
				// Select single (ctrl).
				if (!Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && !Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) selectedNodes.clear();
				if (!selectedNodes.removeValue(node, true)) selectedNodes.add(node);
				ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
				fire(changeEvent);
				Pools.free(changeEvent);
				return -1;
			}
			rowY -= node.height + ySpacing;
			if (node.expanded) {
				rowY = hit(node.children, x, y, rowY);
				if (rowY == -1) return -1;
			}
		}
		return rowY;
	}

	private void selectNodes (Array<Node> nodes, float low, float high) {
		float ySpacing = this.ySpacing;
		for (int i = 0, n = nodes.size; i < n; i++) {
			Node node = nodes.get(i);
			if (node.rightActor.getY() < low) break;
			if (node.rightActor.getY() <= high) selectedNodes.add(node);
			if (node.expanded) selectNodes(node.children, low, high);
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

	public void invalidate () {
		super.invalidate();
		sizeInvalid = true;
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
			float rowWidth = indent + iconSpacing;
			Actor actor = node.rightActor;
			if (actor instanceof Layout) {
				Layout layout = (Layout)actor;
				rowWidth += layout.getPrefWidth();
				node.height = layout.getPrefHeight();
				layout.pack();
			} else {
				rowWidth += actor.getWidth();
				node.height = actor.getHeight();
			}
			if (node.icon != null) {
				rowWidth += iconSpacing * 2 + node.icon.getMinWidth();
				node.height = Math.max(node.height, node.icon.getMinHeight());
			}
			actor = node.leftActor;
			if (actor instanceof Layout) {
				Layout layout = (Layout)actor;
				leftColumnWidth = Math.max(leftColumnWidth, layout.getPrefWidth());
				node.height = Math.max(node.height, layout.getPrefHeight());
				layout.pack();
			} else if (actor != null) {
				leftColumnWidth = Math.max(leftColumnWidth, actor.getWidth());
				node.height = Math.max(node.height, actor.getHeight());
			}
			prefWidth = Math.max(prefWidth, rowWidth);
			prefHeight -= node.height + ySpacing;
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
			float x = indent;
			if (node.icon != null) x += node.icon.getMinWidth();
			y -= node.height;
			node.rightActor.setPosition(x, y);
			if (node.leftActor != null) node.leftActor.setPosition(0, y);
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
		super.draw(batch, parentAlpha); // Draw left and right actors.
	}

	/** Draws selection, icons, and expand icons. */
	private void draw (SpriteBatch batch, Array<Node> nodes, float indent) {
		Drawable plus = style.plus, minus = style.minus;
		float x = getX(), y = getY();
		for (int i = 0, n = nodes.size; i < n; i++) {
			Node node = nodes.get(i);
			Actor actor = node.rightActor;

			if (selectedNodes.contains(node, true) && style.selection != null)
				style.selection.draw(batch, x, y + actor.getY() - ySpacing / 2, getWidth(), node.height + ySpacing);

			if (node.icon != null) {
				float iconY = actor.getY() + node.height / 2 - node.icon.getMinHeight() / 2;
				node.icon.draw(batch, x + node.rightActor.getX() - iconSpacing - node.icon.getMinWidth(), y + iconY,
					node.icon.getMinWidth(), node.icon.getMinHeight());
			}

			if (node.children == null || node.children.size == 0) continue;

			Drawable expandIcon = node.expanded ? minus : plus;
			float iconY = actor.getY() + +node.height / 2 - expandIcon.getMinHeight() / 2;
			expandIcon.draw(batch, x + indent, y + iconY, expandIcon.getMinWidth(), expandIcon.getMinHeight());
			if (node.expanded) draw(batch, node.children, indent + indentSpacing);
		}
	}

	public Array<Node> getSelection () {
		return selectedNodes;
	}

	public void setSelection (Node node) {
		selectedNodes.clear();
		selectedNodes.add(node);
	}

	public void addSelection (Node node) {
		selectedNodes.add(node);
	}

	public void clearSelection () {
		selectedNodes.clear();
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
		Drawable icon;
		float height;

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

		public void setIcon (Drawable icon) {
			this.icon = icon;
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
