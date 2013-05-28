/**
 * 
 */
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.utils.SimplePool;
import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.Cell.CellFactory;

/**
 * @author princemanfred
 *
 */
public class TableCellFactory extends CellFactory {
	private static TableCellFactory instance;
	private static SimplePool<Cell> cellPool = new SimplePool<Cell>();
	
	protected TableCellFactory() {}
	
	@Override
	public Cell obtain (BaseTableLayout layout) {
		Cell cell = cellPool.obtain();
		if (cell == null) {
			cell = super.obtain(layout);
		} else {
			setLayout(cell, layout);
		}
			
		return cell;
	}

	@Override
	public void free (Cell cell) {
		reset(cell);
		cellPool.free(cell);
	}
	
	public static TableCellFactory getInstance() {
		if(instance == null) instance = new TableCellFactory();
		return instance;
	}
}
