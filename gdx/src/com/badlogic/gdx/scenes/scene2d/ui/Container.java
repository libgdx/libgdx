package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.TableLayout;

/**
 * Convenience class for programmatically manipulating a 
 * Table-Layout {@link Table} instance. Used as the base
 * class for all containers that should allow containment
 * of multiple children, like {@link Pane}. The children
 * will be layouted according to <a href="http://code.google.com/p/table-layout/">http://code.google.com/p/table-layout/</a>
 * 
 * @author mzechner
 *
 */
public class Container extends Table {		
	public static class CellProperties {
		final Cell cell;
		
		public CellProperties(Cell cell) {
			this.cell = cell;
		}
		
		public CellProperties expand(boolean x, boolean y) {
			cell.expandWidth = x?1:0;
			cell.expandHeight = y?1:0;
			return this;
		}
		
		public CellProperties expand(int x, int y) {
			cell.expandWidth = x;
			cell.expandHeight = y;
			return this;
		}
		
		public CellProperties fill(boolean x, boolean y) {
			cell.fillWidth = x?1.0f:0.0f;
			cell.fillHeight = y?1.0f:0.0f;
			return this;
		}
		
		public CellProperties fill(float x, float y) {
			cell.fillWidth = x;
			cell.fillHeight = y;
			return this;
		}
		
		public CellProperties align(int align) {
			cell.align = align;
			return this;
		}
		
		public CellProperties colspan(int span) {
			cell.colspan = span;
			return this;
		}
		
		public CellProperties uniform(boolean x, boolean y) {
			cell.uniformWidth = x;
			cell.uniformHeight = y;
			return this;
		}
		
		public CellProperties pad(int top, int left, int bottom, int right) {
			cell.padTop = Integer.toString(top);
			return this;
		}

		public CellProperties padTop(int top) {
			cell.padTop = Integer.toString(top);
			return this;
		}
		
		public CellProperties padLeft(int left) {
			cell.padLeft = Integer.toString(left);
			return this;
		}
		
		public CellProperties padBottom(int bottom) {
			cell.padBottom = Integer.toString(bottom);
			return this;
		}
		
		public CellProperties padRight(int right) {
			cell.padRight = Integer.toString(right);
			return this;
		}
		
		public CellProperties spacing(int top, int left, int bottom, int right) {
			cell.spaceTop = Integer.toString(top);
			cell.spaceLeft = Integer.toString(left);
			cell.spaceBottom = Integer.toString(bottom);
			cell.spaceRight = Integer.toString(right);
			return this;
		}
		
		public CellProperties spacingTop(int top) {
			cell.spaceTop = Integer.toString(top);
			return this;
		}
		
		public CellProperties spacingLeft(int left) {
			cell.spaceLeft = Integer.toString(left);
			return this;
		}
		
		public CellProperties spacingBottom(int bottom) {
			cell.spaceBottom = Integer.toString(bottom);
			return this;
		}
		
		public CellProperties spacingRight(int right) {
			cell.spaceRight = Integer.toString(right);
			return this;
		}
		
		public CellProperties minSize(int width, int height) {
			cell.minWidth = Integer.toString(width);
			cell.minHeight = Integer.toString(height);
			return this;
		}
		
		public CellProperties maxSize(int width, int height) {
			cell.maxWidth = Integer.toString(width);
			cell.maxHeight = Integer.toString(height);
			return this;
		}
	}	
	
	public Container(String name, int prefWidth, int prefHeight) {	
		super(name);		
		this.width = prefWidth; 
		this.height = prefHeight;		
		this.layout.align = TableLayout.TOP | TableLayout.LEFT;
	}	
	
	public CellProperties add(Actor actor) {		
		return new CellProperties(layout.addCell(actor));
	}		

	public CellProperties row() {
		return new CellProperties(layout.startRow());
	}	
	
	/**
	 * Sets the padding of this container.
	 * @param top
	 * @param left
	 * @param bottom
	 * @param right
	 * @return this container
	 */
	public Container padding(int top, int left, int bottom, int right) {
		layout.padTop = Integer.toString(top);
		layout.padLeft = Integer.toString(left);
		layout.padBottom = Integer.toString(bottom);
		layout.padRight = Integer.toString(right);
		return this;
	}
	
	/**
	 * Sets the alignment of this container
	 * @param align combination of TableLayout.TOP, TableLayout.LEFT, etc.
	 * @return this container
	 */
	public Container align(int align) {
		layout.align = align;
		return this;
	}
	
	/**
	 * Invalidates this layout and all parents that implement the {@link Layout} interface.
	 */
	public void invalidateHierarchically() {
		if(this instanceof Layout) invalidate();
		
		Group parent = this.parent;
		while(parent != null) {
			if(parent instanceof Layout) ((Layout) parent).invalidate();
			parent = parent.parent;
		}
	}	
	
	public void invalidate() {
		super.invalidate();
		for(Actor child: children) {
			if(child instanceof Layout) {
				((Layout)child).invalidate();
			}
		}
	}
}