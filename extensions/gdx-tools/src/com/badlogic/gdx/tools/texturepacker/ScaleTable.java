package com.badlogic.gdx.tools.texturepacker;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class ScaleTable extends JTable {
	private ScaleTableModel model;
	
	public ScaleTable () {
		super();
		model = new ScaleTableModel();
		setModel(model);
		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		((DefaultTableCellRenderer) getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	public void addData() {
		model.addData();
	}
	
	public void setDatas(float[] scale, String[] scaleSuffix) {
		model.setDatas(scale, scaleSuffix);
	}
	
	public float[] getScale() {
		return model.getScale();
	}
	
	public String[] getScaleSuffix() {
		return model.getScaleSuffix();
	}
	
	public void deleteData(int rowIndexView) {
		int rowIndexModel = convertRowIndexToModel(rowIndexView);
		model.deleteData(rowIndexModel);
	}
	
	public void clear() {
		model.clear();
	}
	
	class ScaleTableModel extends AbstractTableModel {
		private final String[] TABLE_COLUMN = {"Scale", "Scale Suffix"};
		private List<Float> scale;
		private List<String> scaleSuffix;
		
		public ScaleTableModel () {
			scale = new ArrayList<Float>();
			scaleSuffix = new ArrayList<String>();
		}
		
		public void addData() {
			scale.add(1f);
			scaleSuffix.add("");
			fireTableRowsInserted(scale.size() - 1, scale.size() - 1);
		}
		
		public void setDatas(float[] scale, String[] scaleSuffix) {
			this.scale.clear();
			this.scaleSuffix.clear();
			for (float s: scale) {
				this.scale.add(s);
			}
			for (String s: scaleSuffix) {
				this.scaleSuffix.add(s);
			}
			fireTableDataChanged();
		}
		
		public float[] getScale() {
			float[] scale = new float[this.scale.size()];
			for (int i = 0; i < this.scale.size(); i++) {
				scale[i] = this.scale.get(i).floatValue();
			}
			return scale;
		}
		
		public String[] getScaleSuffix() {
			return scaleSuffix.toArray(new String[scaleSuffix.size()]);
		}
		
		public void deleteData(int rowIndexModel) {
			scale.remove(rowIndexModel);
			scaleSuffix.remove(rowIndexModel);
			fireTableRowsDeleted(rowIndexModel, rowIndexModel);
		}
		
		public void clear() {
			scale.clear();
			scaleSuffix.clear();
			fireTableDataChanged();
		}

		@Override
		public int getRowCount () {
			return scale.size();
		}

		@Override
		public int getColumnCount () {
			return TABLE_COLUMN.length;
		}

		@Override
		public String getColumnName (int column) {
			return TABLE_COLUMN[column];
		}

		@Override
		public Class<?> getColumnClass (int columnIndex) {
			if (columnIndex == 0) {
				return Float.class;
			} else if (columnIndex == 1) {
				return String.class;
			}
			return null;
		}

		@Override
		public boolean isCellEditable (int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		public void setValueAt (Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 0 && aValue instanceof Float) {
				scale.set(rowIndex, (Float) aValue);
			} else if (columnIndex == 1 && aValue instanceof String) {
				scaleSuffix.set(rowIndex, (String) aValue);
			}
		}
		
		@Override
		public Object getValueAt (int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return scale.get(rowIndex);
			} else if (columnIndex == 1) {
				return scaleSuffix.get(rowIndex);
			}
			return null;
		}
		
	}
}

