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
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.BaseTableLayout;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.TableLayout;

/**
 * <h2>Functionality</h2>
 * A Container is a {@link Group} capable of layouting inserted {@link Actor} instances in a table layout. See 
 * <a href="http://code.google.com/p/table-layout/">http://code.google.com/p/table-layout/</a></p>
 * 
 * <h2>Layout</h2>
 * Actors added to the Container will be layouted in rows and columns. To start a new row call the {@link #row()} method.
 * To insert an Actor into the current row call the {@link #add(Actor)} method. Actors are added from left to right to 
 * the current row. <b>Do not use the {@link #addActor(Actor)}, {@link #addActorAfter(Actor, Actor)}, 
 * {@link #addActorAt(int, Actor)}, {@link #addActorBefore(Actor, Actor)} methods!</b></p>
 * 
 * <h3>CellProperties</h3>
 * Each added Actor is contained within a cell, which has {@link CellProperties} defining the layout of that Actor within
 * the cell as well as the layout of the cell within the table.</p>
 * 
 * To specify how an Actor should be layouted within the cell, one can manipulate the {@link CellProperties} instance
 * returned by the {@link #add(Actor)} method. It allows to define the alignment, padding, minimum and maximum size
 * of the Actor within the cell. By default an Actor's width and height are taken. If the Actor is a {@link Widget} its
 * preferred width and height are taken (see {@link Widget#getPrefWidth()} and {@link Widget#getPrefWidth()}. To let an Actor
 * fill the entire cell on either or both the x- and y-axis one can use the {@link CellProperties#fill(boolean, boolean)} method. 
 * The layouting process may resize the Actor according to the {@link CellProperties} as well as the available space 
 * of the cell within the table.</p>
 * 
 * To specify how a cell should be layouted within the table the {@link CellProperties} class offers the methods 
 * {@link CellProperties#expand(boolean, boolean)}, {@link CellProperties#spacing(int, int, int, int)} and 
 * {@link CellProperties#colspan(int)} method. </p>
 * 
 * The {@link CellProperties#expand(boolean, boolean)} method will make the cell take up
 * any remaining space in the layout. If multiple cells are configured to expand, the sizes of each cell determined 
 * by its contained Actor are used as weightings. Additionally one can use the {@link CellProperties#expand(int, int)} 
 * method to directly specify a weight.</p>
 * 
 * The {@link CellProperties#spacing(int, int, int, int)} method allows to define how much space should be left empty 
 * between adjacent cells.</p>
 * 
 * The {@link CellProperties#colspan(int)} method allows to define how many columns a cell should span in the table. 
 * Note that a table does not necessarily have to have the same amount of columns in every row.</p> 
 * 
 * To set the {@link CellProperties} for all cells in an entire row, one can manipulate the {@link CellProperties} instance 
 * returned by the {@link #row()} method.
 * 
 * <h3>Container Size</h3>
 * A Container's size is usually determined by the dimensions given in its constructor. Unless the Container is nested 
 * inside another Container, its width and height will stay constant. To programmatically change the size of a Container 
 * call the {@link #setPrefSize(float, float)} method.</p>
 * 
 * <h3>Layout Invalidation</h3>
 * When a Container is resized, its layout has to be recomputed. For this process to work the Container has to be invalidated, 
 * which will trigger a relayouting in the next call to either {@link Container#layout()} or 
 * {@link Container#draw(com.badlogic.gdx.graphics.g2d.SpriteBatch, float)}. Consequently, if a Container is 
 * nested inside another Container, the parent Container also has to be invalidated.</p>
 * 
 * To perform the invalidation of a Container call the {@link Container#invalidate()} method. To also invalidate a Container's 
 * parent Container call the {@link Container#invalidateHierarchically()} method, which will invalidate the Container and 
 * all its parents.</p>
 * 
 * An Actor added to a container can also change its (preferred) size. In this case the Container has to be informed of this circumstance by a 
 * explicitely calling either {@link Container#invalidate()} or {@link Container#invalidateHierarchically()}.</p>
 * 
 * <h3>Restrictions</h3>
 * Once an Actor is added to a Container, it's width and height controlled by the Container (unless you chose to use the method described in the
 * last paragraph). For this reason, applying an {@link Action} to an Actor contained in a Container will not work. It is advised to not change
 * the size of contained Actors during runtime, as layouting every frame might be a costly operation.</p>
 * 
 * Actors should also not be scaled or rotated when contained inside a Container. The layouting algorithm will simply ignore these properties!</p>
 * 
 * <h2>Style</h2>
 * A Container has no graphical representation. See {@link Window} and {@link Pane} for styled
 * Containers.
 * 
 * @author mzechner
 *
 */
public class Container extends Table {
	
	/**
	 * Class specifying the layout of an Actor within a cell as well as the
	 * behaviour of th cell within the entire layout. See <a href="http://code.google.com/p/table-layout/">http://code.google.com/p/table-layout/</a> as
	 * well as the class documentation of {@link Container}.
	 * @author mzechner
	 *
	 */
	public static class CellProperties {
		final Cell cell;
		
		public CellProperties(Cell cell) {
			this.cell = cell;
		}
		
		/**
		 * Sets whether the cell should take up the remaining space on the x- and
		 * y-axis. If multiple cells have expansion enabled on one or both axis, the
		 * size distribution will be weighted by the cells size determined from its
		 * {@link Actor}.
		 * 
		 * @param x if true, all horizontal space is taken up.
		 * @param y if true, all vertical space is taken up.
		 * @return this {@link CellProperties} for further manipulation.
		 */
		public CellProperties expand(boolean x, boolean y) {
			cell.expandWidth = x?1:0;
			cell.expandHeight = y?1:0;
			return this;
		}
		
		/**
		 * Lets the cell take up all remaining space, specifying the weighting of the cell for expansion,
		 * relative to the weights of the other cells in the layout. Note: The {@link #expand(boolean, boolean)} method will
		 * set the weights to 1.
		 * 
		 * @param x the weight on x for expansion.
		 * @param y the weight on y for expansion.
		 * @return this {@link CellProperties} for further manipulation.
		 */
		public CellProperties expand(int x, int y) {
			cell.expandWidth = x;
			cell.expandHeight = y;
			return this;
		}
		
		/**
		 * Sets whether to let the contained {@link Actor} fill the entire cell on the x-
		 * and y-axis.
		 * 
		 * @param x if true, the Actor takes up all horizontal space in the cell.
		 * @param y if true, the Actor takes up all vertical space int he cell.
		 * @return this {@link CellProperties} for further manipulation.
		 */
		public CellProperties fill(boolean x, boolean y) {
			cell.fillWidth = x?1.0f:0.0f;
			cell.fillHeight = y?1.0f:0.0f;
			return this;
		}
		
		/**
		 * Sets the percentage of space taken up by the contained {@link Actor} within the cell. Values
		 * have to be in the range 0.0f to 1.0f. A value of 0.0f will make the Actor use it's (preferred)
		 * size.
		 * 
		 * @param x actor size between 0.0f to 1.0f on the x-axis (0-100% of the cell width)
		 * @param y actor size between 0.0f to 1.0f on the y-axis (0-100% of the cell height)
		 * @return this {@link CellProperties} for further manipulation.
		 */
		public CellProperties fill(float x, float y) {
			cell.fillWidth = x;
			cell.fillHeight = y;
			return this;
		}
		
		/**
		 * Sets the alignment of the {@link Actor} within the cell. Use binary OR with the constants
		 * {@link BaseTableLayout#TOP}, {@link BaseTableLayout#BOTTOM}, {@link BaseTableLayout#LEFT},
		 * {@link BaseTableLayout#RIGHT}, {@link BaseTableLayout#CENTER}.
		 * @param align the alignment.
		 * @return this {@link CellProperties} for further manipulation.
		 */
		public CellProperties align(int align) {
			cell.align = align;
			return this;
		}
		
		/**
		 * Sets how many columns this cell should span in the layout.
		 * @param span number of columns to span.
		 * @return this {@link CellProperties} for further manipulation.
		 */
		public CellProperties colspan(int span) {
			cell.colspan = span;
			return this;
		}
		
		/**
		 * Cells marked as uniform on an axis will be the same size on that
		 * axis.
		 * @param x if true, this cell will be uniform on the x-axis
		 * @param y if true, this cell will be uniform on the y-axis
		 * @return this {@link CellProperties} for further manipulation.
		 */
		public CellProperties uniform(boolean x, boolean y) {
			cell.uniformWidth = x;
			cell.uniformHeight = y;
			return this;
		}
		
		/**
		 * Sets the padding on the top, left, bottom and right edge. The padding is an
		 * inset inside the cell and takes away space available for the contained Actor.
		 * @param top the top padding
		 * @param left the left padding
		 * @param bottom the bottom padding
		 * @param right the right padding
		 * @return this {@link CellProperties} for further manipulation.
		 */
		public CellProperties pad(int top, int left, int bottom, int right) {
			cell.padTop = Integer.toString(top);
			return this;
		}

		/**
		 * @param top the top padding 
		 * @return this {@link CellProperties} for further manipulation.
		 */
		public CellProperties padTop(int top) {
			cell.padTop = Integer.toString(top);
			return this;
		}
		
		/** 
		 * @param left the left padding
		 * @return this {@link CellProperties} for further manipulation.
		 */
		public CellProperties padLeft(int left) {
			cell.padLeft = Integer.toString(left);
			return this;
		}
		
		/**
		 * @param bottom the bottom padding
		 * @return this {@link CellProperties} for further manipulation.
		 */
		public CellProperties padBottom(int bottom) {
			cell.padBottom = Integer.toString(bottom);
			return this;
		}
		
		/**
		 * @param right the right padding
		 * @return this {@link CellProperties} for further manipulation.
		 */
		public CellProperties padRight(int right) {
			cell.padRight = Integer.toString(right);
			return this;
		}
		
		/**
		 * Sets the spacing for the top, left, bottom and right edge of this cell.
		 * The spacing is space between this cell and adjacent cells. It will not
		 * take away space from the inside of the cell.
		 * @param top the top spacing
		 * @param left the left spacing
		 * @param bottom the bottom spacing
		 * @param right the right spacing
		 * @return this {@link CellProperties} for further manipulation.
		 */
		public CellProperties spacing(int top, int left, int bottom, int right) {
			cell.spaceTop = Integer.toString(top);
			cell.spaceLeft = Integer.toString(left);
			cell.spaceBottom = Integer.toString(bottom);
			cell.spaceRight = Integer.toString(right);
			return this;
		}
		
		/** 
		 * @param top the top spacing
		 * @return this {@link CellProperties} for further manipulation. 
		 */
		public CellProperties spacingTop(int top) {
			cell.spaceTop = Integer.toString(top);
			return this;
		}
		
		/** 
		 * @param left the left spacing
		 * @return this {@link CellProperties} for further manipulation.
		 */
		public CellProperties spacingLeft(int left) {
			cell.spaceLeft = Integer.toString(left);
			return this;
		}
		
		/**		 
		 * @param bottom the bottom spacing
		 * @return this {@link CellProperties} for further manipulation.
		 */
		public CellProperties spacingBottom(int bottom) {
			cell.spaceBottom = Integer.toString(bottom);
			return this;
		}
		
		/**
		 * @param right the right spacing
		 * @return this {@link CellProperties} for further manipulation.
		 */
		public CellProperties spacingRight(int right) {
			cell.spaceRight = Integer.toString(right);
			return this;
		}
		
		/**
		 * Sets the minimum size of the cell.
		 * @param width the width
		 * @param height the height
		 * @return this {@link CellProperties} for further manipulation.
		 */
		public CellProperties minSize(int width, int height) {
			cell.minWidth = Integer.toString(width);
			cell.minHeight = Integer.toString(height);
			return this;
		}
		
		/**
		 * Sets the maximum size of the cell.
		 * @param width the width
		 * @param height the height
		 * @return this {@link CellProperties} for further manipulation.
		 */
		public CellProperties maxSize(int width, int height) {
			cell.maxWidth = Integer.toString(width);
			cell.maxHeight = Integer.toString(height);
			return this;
		}
	}	
	
	/**
	 * Creates a new container with the given name and (preferred) width. See the 
	 * class documentation for more information ({@link Container}).
	 * @param name the name of the container.
	 * @param prefWidth the (preferred) width
	 * @param prefHeight the (preferred) height
	 */
	public Container(String name, int prefWidth, int prefHeight) {	
		super(name);		
		this.width = prefWidth; 
		this.height = prefHeight;		
		this.layout.align = TableLayout.TOP | TableLayout.LEFT;
	}	
	
	/**
	 * Adds a new {@link Actor} to this Container. See the class documentation for
	 * more information ({@link Container}).
	 * @param actor the Actor
	 * @return the {@link CellProperties} of the cell containing the Actor.
	 */
	public CellProperties add(Actor actor) {		
		return new CellProperties(layout.addCell(actor));
	}		

	/**
	 * Adds a new row to the table layout of this Container. All subsequent calls to {@link #add(Actor)} 
	 * will add the Actors to this row. See the class documentation for more information ({@link Container}).
	 * @return the {@link CellProperties} for all cells within this row.
	 */
	public CellProperties row() {
		return new CellProperties(layout.startRow());
	}	
	
	/**
	 * Sets the padding of this container. The padding will take away
	 * space from the cells container in the Container.
	 * @param top the top padding
	 * @param left the left padding
	 * @param bottom the bottom padding
	 * @param right the right padding
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
	 * Sets the alignment of this container's content within the container.
	 * @param align combination of TableLayout.TOP, TableLayout.LEFT, etc.
	 * @return this container
	 */
	public Container align(int align) {
		layout.align = align;
		return this;
	}
	
	/**
	 * Invalidates this layout and all parents that implement the {@link Layout} interface.
	 * See the class documentation for more information ({@link Container}).
	 */
	public void invalidateHierarchically() {
		invalidate();
		
		Group parent = this.parent;
		while(parent != null) {
			if(parent instanceof Layout) ((Layout) parent).invalidate();
			parent = parent.parent;
		}
	}	
	
	/**
	 * Invalidates this layout. See the class documentation for more information ({@link Container})
	 */
	public void invalidate() {
		super.invalidate();
		for(Actor child: children) {
			if(child instanceof Layout) {
				((Layout)child).invalidate();
			}
		}
	}
	
	/**
	 * Sets the (preferred) size of this Container. Calls {@link #invalidateHierarchically()} implicitely.
	 * @param width the width
	 * @param height the height
	 */
	public void setPrefSize(float width, float height) {
		this.width = width;
		this.height = height;
		invalidateHierarchically();
	}
}