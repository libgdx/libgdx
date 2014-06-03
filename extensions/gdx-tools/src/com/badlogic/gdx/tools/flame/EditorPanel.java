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

package com.badlogic.gdx.tools.flame;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;


/** @author Inferno */
public abstract class EditorPanel<T> extends JPanel {
	private String name;
	private String description;
	protected  T value;
	private JPanel titlePanel;
	JToggleButton activeButton;
	JPanel contentPanel;
	JToggleButton advancedButton;
	JButton removeButton;
	JPanel advancedPanel;
	private boolean hasAdvanced;
	JLabel nameLabel, descriptionLabel;
	protected boolean isAlwaysActive, isAlwaysShown = false, isRemovable;
	protected FlameMain editor;
	
	public EditorPanel (FlameMain editor, String name, String description, boolean alwaysActive, boolean isRemovable) {
		this.editor = editor;
		this.name = name;
		this.description = description;
		this.isRemovable = isRemovable;
		this.isAlwaysActive = alwaysActive;
		initializeComponents();
		showContent(false);
	}
	
	public EditorPanel (FlameMain editor, String name, String description) {
		this(editor, name, description, true, false);
	}

	protected void activate () {}
	
	public void showContent (boolean visible) {
		contentPanel.setVisible(visible);
		advancedPanel.setVisible(visible && advancedButton.isSelected());
		advancedButton.setVisible(visible && hasAdvanced);
		descriptionLabel.setText(visible ? description : "");
	}
	
	public void setIsAlwayShown(boolean isAlwaysShown){
		showContent(isAlwaysShown);
		this.isAlwaysShown = isAlwaysShown;
		titlePanel.setCursor(null);
	}

	public void update (FlameMain editor) {
	}

	public void setHasAdvanced (boolean hasAdvanced) {
		this.hasAdvanced = hasAdvanced;
		advancedButton.setVisible(hasAdvanced);
	}

	public JPanel getContentPanel () {
		return contentPanel;
	}

	public JPanel getAdvancedPanel () {
		return advancedPanel;
	}

	public String getName () {
		return name;
	}

	public void setEmbedded () {
		GridBagLayout layout = (GridBagLayout)getLayout();
		GridBagConstraints constraints = layout.getConstraints(contentPanel);
		constraints.insets = new Insets(0, 0, 0, 0);
		layout.setConstraints(contentPanel, constraints);

		titlePanel.setVisible(false);
	}

	protected void initializeComponents () {
		setLayout(new GridBagLayout());
		{
			titlePanel = new JPanel(new GridBagLayout());
			add(titlePanel, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(3, 0, 3, 0), 0, 0));
			titlePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			{
				nameLabel = new JLabel(name);
				titlePanel.add(nameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(3, 6, 3, 6), 0, 0));
				nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
			}
			{
				descriptionLabel = new JLabel(description);
				titlePanel.add(descriptionLabel, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(3, 6, 3, 6), 0, 0));
			}
			{
				advancedButton = new JToggleButton("Advanced");
				titlePanel.add(advancedButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 6), 0, 0));
				advancedButton.setVisible(false);
			}
			{
				activeButton = new JToggleButton("Active");
				titlePanel.add(activeButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 6), 0, 0));
			}
			{
				removeButton = new JButton("X");
				titlePanel.add(removeButton, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 6), 0, 0));
			}
		}
		{
			contentPanel = new JPanel(new GridBagLayout());
			add(contentPanel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 6, 6, 6), 0, 0));
			contentPanel.setVisible(false);
		}
		{
			advancedPanel = new JPanel(new GridBagLayout());
			add(advancedPanel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 6, 6, 6), 0, 0));
			advancedPanel.setVisible(false);
		}
		

		titlePanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked (MouseEvent event) {
				if(!isAlwaysShown)
					showContent(!contentPanel.isVisible());
			}
		});
		activeButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				activate();
			}
		});
		advancedButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				advancedPanel.setVisible(advancedButton.isSelected());
			}
		});
		
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				removePanel();
			}
		});
	}
	
	protected void removePanel () {
		Container parent = this.getParent();
		parent.remove(this);
		parent.validate();
		parent.repaint();
	}

	public void setName(String name){
		this.name = name;
		nameLabel.setText(name);
	}
	
	public void setDescription(String desc){
		description = desc;
		descriptionLabel.setText(desc);
	}
	
	
	protected void addContent(int row, int column, JComponent component){
		addContent(row, column, component, true, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
	}
	
	protected void addContent(int row, int column, JComponent component, boolean addBorder){
		addContent(row, column, component, addBorder, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
	}
	
	protected void addContent(int row, int column, JComponent component, int anchor, int fill){
		addContent(row, column, component, true, anchor, fill);
	}
	
	protected void addContent(int row, int column, JComponent component, boolean addBorders, int anchor, int fill, float wx, float wy){
		addContent(contentPanel, row, column, component, addBorders, anchor, fill, wx, wy);
	}
	
	protected void addContent(int row, int column, JComponent component, boolean addBorders, int anchor, int fill){
		addContent(row, column, component, addBorders, anchor, fill, 1, 1);
	}

	public void setValue (T value) {
		this.value = value;
		activeButton.setVisible(value == null ? false : !isAlwaysActive);
		removeButton.setVisible(isRemovable);
	}
	
	public static void addContent( JPanel panel, int row, int column, JComponent component, boolean addBorders, int anchor, int fill, float wx, float wy){
		if(addBorders) component.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, java.awt.Color.black));
		panel.add(component, new GridBagConstraints(column, row, 1, 1, wx, wy, anchor, fill,
			new Insets(0, 0, 0, 0), 0, 0));
	}
	
	protected static <K> void setValue(JSpinner spinner, K object){
		ChangeListener[] listeners = spinner.getChangeListeners();
		ChangeListener listener = null;
		if(listeners != null && listeners.length >0){
			listener = listeners[0];
			spinner.removeChangeListener(listener);
		}
		spinner.setValue(object);
		if(listener != null) spinner.addChangeListener(listener);
	}
	
	protected static void setValue(JCheckBox checkBox, boolean isSelected){
		ActionListener[] listeners = checkBox.getActionListeners();
		ActionListener listener = null;
		if(listeners != null && listeners.length >0){
			listener = listeners[0];
			checkBox.removeActionListener(listener);
		}
		checkBox.setSelected(isSelected);
		if(listener != null) checkBox.addActionListener(listener);
	}
	
	protected static <K> void setValue(Slider slider, float value){
		ChangeListener[] listeners = slider.spinner.getChangeListeners();
		ChangeListener listener = null;
		if(listeners != null && listeners.length >0){
			listener = listeners[0];
			slider.spinner.removeChangeListener(listener);
		}
		slider.setValue(value);
		if(listener != null) slider.spinner.addChangeListener(listener);
	}

	protected static void setValue (DefaultTableModel tableModel, Object value, int row, int column) {
		TableModelListener[] listeners = tableModel.getTableModelListeners();
		TableModelListener listener = null;
		if(listeners != null && listeners.length >0){
			listener = listeners[0];
			tableModel.removeTableModelListener(listener);
		}
		tableModel.setValueAt(value, row, column);
		if(listener != null) tableModel.addTableModelListener(listener);
	}
	
	
}
