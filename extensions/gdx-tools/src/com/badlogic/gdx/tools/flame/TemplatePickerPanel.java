package com.badlogic.gdx.tools.flame;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.badlogic.gdx.utils.Array;

/** @author Inferno */
public class TemplatePickerPanel<T> extends EditorPanel<Array<T>> implements LoaderButton.Listener<T> {

	public interface Listener<T> {
		void onTemplateChecked(T template, boolean isChecked);
	}

	Array<T> loadedTemplates;
	Array<T> excludedTemplates;
	Class<T> type;
	JTable templatesTable;
	DefaultTableModel templatesTableModel;
	boolean 	isOneModelSelectedRequired = true, 
				isMultipleSelectionAllowed = true;
	Listener listener;
	int lastSelected = -1;
	
	public TemplatePickerPanel (FlameMain editor, Array<T> value, Listener listener, Class<T> type){
		this(editor, value, listener, type, null, true, true);
	}
	
	public TemplatePickerPanel (FlameMain editor, Array<T> value, Listener listener, Class<T> type, LoaderButton<T> loaderButton){
		this(editor, value, listener, type, loaderButton, true, true);
	}

	public TemplatePickerPanel (FlameMain editor, Array<T> value, Listener listener, Class<T> type, 
									LoaderButton<T> loaderButton, boolean isOneModelSelectedRequired, boolean isMultipleSelectionAllowed) {
		super(editor, "", "");
		this.type = type;
		this.listener = listener;
		this.isOneModelSelectedRequired = isOneModelSelectedRequired;
		this.isMultipleSelectionAllowed = isMultipleSelectionAllowed;
		loadedTemplates = new Array<T>();
		excludedTemplates = new Array<T>();
		initializeComponents(type, loaderButton);
		setValue(value);
	}
	
	@Override
	public void setValue (Array<T> value) {
		super.setValue(value);
		if(value == null) return;
		if(!isMultipleSelectionAllowed && value.size >1)
			throw new RuntimeException("Multiple selection must be enabled to ensure consistency between picked and available models.");
		for(int i=0; i < value.size;++i ){
			T model  = value.get(i);
			int index = loadedTemplates.indexOf(model, true);
			if(index >-1){
				EditorPanel.setValue(templatesTableModel, true, index, 1);
				lastSelected = index;
			}
		}
	}
	
	public void setOneModelSelectionRequired(boolean isOneModelSelectionRequired){
		this.isOneModelSelectedRequired = isOneModelSelectionRequired;
	}
	
	public void setMultipleSelectionAllowed(boolean isMultipleSelectionAllowed){
		this.isMultipleSelectionAllowed = isMultipleSelectionAllowed;
	}
	
	public void setExcludedTemplates(Array<T> excludedTemplates){
		this.excludedTemplates.clear();
		this.excludedTemplates.addAll(excludedTemplates);
	}
	
	public void setLoadedTemplates(Array<T> templates){
		loadedTemplates.clear();
		loadedTemplates.addAll(templates);
		loadedTemplates.removeAll(excludedTemplates, true);
		templatesTableModel.getDataVector().removeAllElements();
		int i=0;
		for(T template : templates){
			templatesTableModel.addRow(new Object[] {getTemplateName(template, i), false});
			i++;
		}
		lastSelected = -1;
		setValue(value);
	}
	
	protected String getTemplateName(T template, int index){
		String name = editor.assetManager.getAssetFileName(template);
		return name == null ? "template "+index:name; 
	}
	

	public void reloadTemplates () {
		setLoadedTemplates(editor.assetManager.getAll(type,  new Array<T>()));
	}
	
	protected void initializeComponents (Class<T> type, LoaderButton<T> loaderButton) {
		int i=0;
		if(loaderButton != null){
			loaderButton.setListener(this);
			contentPanel.add(loaderButton, new GridBagConstraints(0, i++, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,
				0, 0, 6), 0, 0));
		}
		
		JScrollPane scroll = new JScrollPane();
		contentPanel.add(scroll, new GridBagConstraints(0, i, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,
			0, 0, 6), 0, 0));
		{
			templatesTable = new JTable() {
				public Class getColumnClass (int column) {
					return column == 1 ? Boolean.class : super.getColumnClass(column);
				}
				@Override
				public Dimension getPreferredScrollableViewportSize () {
					Dimension dim = super.getPreferredScrollableViewportSize();
					dim.height = getPreferredSize().height;
					return dim;
				}
			};
			templatesTable.getTableHeader().setReorderingAllowed(false);
			templatesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			scroll.setViewportView(templatesTable);
			templatesTableModel = new DefaultTableModel(new String[0][0], new String[] {"Template", "Selected"});
			templatesTable.setModel(templatesTableModel);
			reloadTemplates();
			
			templatesTableModel.addTableModelListener(new TableModelListener() {
				public void tableChanged (TableModelEvent event) {
					if (event.getColumn() != 1) return;
					int row = event.getFirstRow();
					boolean checked = (Boolean)templatesTable.getValueAt(row, 1);
					if( isOneModelSelectedRequired && (value.size == 1 && !checked) ){
						EditorPanel.setValue(templatesTableModel, true, row, 1);
						return;
					}
						
					templateChecked(row, checked);
				}
			});
		}
	}

	protected void templateChecked (int index, Boolean isChecked) {
		T template = loadedTemplates.get(index);
		if(isChecked){
			if(!isMultipleSelectionAllowed){
				if(lastSelected >-1){
					value.removeValue(loadedTemplates.get(lastSelected), true);
					EditorPanel.setValue(templatesTableModel, false, lastSelected, 1);
				}
			}
			value.add(template);
			lastSelected = index;
		}
		else {
			value.removeValue(template, true);
		}
		listener.onTemplateChecked(template, isChecked);
	}

	@Override
	public void onResourceLoaded (T model) {
		reloadTemplates();
		if(lastSelected == -1 && isOneModelSelectedRequired){
			templateChecked(loadedTemplates.size-1, true);
		}
		else {
			setValue(value);
		}

		revalidate();
		repaint();
	}

}
