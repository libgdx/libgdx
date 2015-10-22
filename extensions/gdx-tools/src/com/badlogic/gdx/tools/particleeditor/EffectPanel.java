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

package com.badlogic.gdx.tools.particleeditor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URI;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StreamUtils;

class EffectPanel extends JPanel {
	ParticleEditor editor;
	JTable emitterTable;
	DefaultTableModel emitterTableModel;
	int editIndex;
	String lastDir;

	public EffectPanel (ParticleEditor editor) {
		this.editor = editor;
		initializeComponents();
	}

	public ParticleEmitter newEmitter (String name, boolean select) {
		final ParticleEmitter emitter = new ParticleEmitter();

		emitter.getDuration().setLow(1000);
		emitter.getEmission().setHigh(50);
		emitter.getLife().setHigh(500);
		emitter.getScale().setHigh(32, 32);

		emitter.getTint().setColors(new float[] {1, 0.12156863f, 0.047058824f});
		emitter.getTransparency().setHigh(1);

		emitter.setMaxParticleCount(25);
		emitter.setImagePath(ParticleEditor.DEFAULT_PARTICLE);

		addEmitter(name, select, emitter);
		return emitter;
	}

	public ParticleEmitter newExampleEmitter (String name, boolean select) {
		final ParticleEmitter emitter = new ParticleEmitter();

		emitter.getDuration().setLow(3000);

		emitter.getEmission().setHigh(250);

		emitter.getLife().setHigh(500, 1000);
		emitter.getLife().setTimeline(new float[] {0, 0.66f, 1});
		emitter.getLife().setScaling(new float[] {1, 1, 0.3f});

		emitter.getScale().setHigh(32, 32);

		emitter.getRotation().setLow(1, 360);
		emitter.getRotation().setHigh(180, 180);
		emitter.getRotation().setTimeline(new float[] {0, 1});
		emitter.getRotation().setScaling(new float[] {0, 1});
		emitter.getRotation().setRelative(true);

		emitter.getAngle().setHigh(45, 135);
		emitter.getAngle().setLow(90);
		emitter.getAngle().setTimeline(new float[] {0, 0.5f, 1});
		emitter.getAngle().setScaling(new float[] {1, 0, 0});
		emitter.getAngle().setActive(true);

		emitter.getVelocity().setHigh(30, 300);
		emitter.getVelocity().setActive(true);

		emitter.getTint().setColors(new float[] {1, 0.12156863f, 0.047058824f});

		emitter.getTransparency().setHigh(1, 1);
		emitter.getTransparency().setTimeline(new float[] {0, 0.2f, 0.8f, 1});
		emitter.getTransparency().setScaling(new float[] {0, 1, 0.75f, 0});

		emitter.setMaxParticleCount(200);
		emitter.setImagePath(ParticleEditor.DEFAULT_PARTICLE);

		addEmitter(name, select, emitter);
		return emitter;
	}

	private void addEmitter (String name, boolean select, final ParticleEmitter emitter) {
		Array<ParticleEmitter> emitters = editor.effect.getEmitters();
		if (emitters.size == 0)
			emitter.setPosition(editor.worldCamera.viewportWidth / 2, editor.worldCamera.viewportHeight / 2);
		else {
			ParticleEmitter p = emitters.get(0);
			emitter.setPosition(p.getX(), p.getY());
		}
		emitters.add(emitter);

		emitterTableModel.addRow(new Object[] {name, true});
		if (select) {
			editor.reloadRows();
			int row = emitterTableModel.getRowCount() - 1;
			emitterTable.getSelectionModel().setSelectionInterval(row, row);
		}
	}

	void emitterSelected () {
		int row = emitterTable.getSelectedRow();
		if (row == -1) {
			row = editIndex;
			emitterTable.getSelectionModel().setSelectionInterval(row, row);
		}
		if (row == editIndex) return;
		editIndex = row;
		editor.reloadRows();
	}

	void openEffect (boolean mergeIntoCurrent) {
		FileDialog dialog = new FileDialog(editor, "Open Effect", FileDialog.LOAD);
		if (lastDir != null) dialog.setDirectory(lastDir);
		dialog.setVisible(true);
		final String file = dialog.getFile();
		final String dir = dialog.getDirectory();
		if (dir == null || file == null || file.trim().length() == 0) return;
		lastDir = dir;
		ParticleEffect effect = new ParticleEffect();
		try {
			File effectFile = new File(dir, file);
			effect.loadEmitters(Gdx.files.absolute(effectFile.getAbsolutePath()));
			if (mergeIntoCurrent){
				for (ParticleEmitter emitter : effect.getEmitters()){
					addEmitter(emitter.getName(), false, emitter);
				}
			} else {
				editor.effect = effect;
				editor.effectFile = effectFile;
			}
			emitterTableModel.getDataVector().removeAllElements();
			editor.particleData.clear();
		} catch (Exception ex) {
			System.out.println("Error loading effect: " + new File(dir, file).getAbsolutePath());
			ex.printStackTrace();
			JOptionPane.showMessageDialog(editor, "Error opening effect.");
			return;
		}
		for (ParticleEmitter emitter : editor.effect.getEmitters()) {
			emitter.setPosition(editor.worldCamera.viewportWidth / 2, editor.worldCamera.viewportHeight / 2);
			emitterTableModel.addRow(new Object[] {emitter.getName(), true});
		}
		editIndex = 0;
		emitterTable.getSelectionModel().setSelectionInterval(editIndex, editIndex);
		editor.reloadRows();
	}

	void saveEffect () {
		FileDialog dialog = new FileDialog(editor, "Save Effect", FileDialog.SAVE);
		if (lastDir != null) dialog.setDirectory(lastDir);
		dialog.setVisible(true);
		String file = dialog.getFile();
		String dir = dialog.getDirectory();
		if (dir == null || file == null || file.trim().length() == 0) return;
		lastDir = dir;
		int index = 0;
		File effectFile = new File(dir, file);

		// save each image path as relative path to effect file directory
		URI effectDirUri = effectFile.getParentFile().toURI();
		for (ParticleEmitter emitter : editor.effect.getEmitters()) {
			emitter.setName((String)emitterTableModel.getValueAt(index++, 0));
			String imagePath = emitter.getImagePath();
			if ((imagePath.contains("/") || imagePath.contains("\\")) && !imagePath.contains("..")) {
				// it's absolute, make it relative:
				URI imageUri = new File(emitter.getImagePath()).toURI();
				emitter.setImagePath(effectDirUri.relativize(imageUri).getPath());
			}
		}

		File outputFile = new File(dir, file);
		Writer fileWriter = null;
		try {
			fileWriter = new FileWriter(outputFile);
			editor.effect.save(fileWriter);
		} catch (Exception ex) {
			System.out.println("Error saving effect: " + outputFile.getAbsolutePath());
			ex.printStackTrace();
			JOptionPane.showMessageDialog(editor, "Error saving effect.");
		} finally {
			StreamUtils.closeQuietly(fileWriter);
		}
	}

	void duplicateEmitter () {
		int row = emitterTable.getSelectedRow();
		if (row == -1) return;

		String name = (String)emitterTableModel.getValueAt(row, 0);

		addEmitter(name, true, new ParticleEmitter(editor.effect.getEmitters().get(row)));
	}

	void deleteEmitter () {
		if (editor.effect.getEmitters().size == 1) return;
		int row = emitterTable.getSelectedRow();
		if (row == -1) return;
		if (row <= editIndex) {
			int oldEditIndex = editIndex;
			editIndex = Math.max(0, editIndex - 1);
			if (oldEditIndex == row) editor.reloadRows();
		}
		editor.effect.getEmitters().removeIndex(row);
		emitterTableModel.removeRow(row);
		emitterTable.getSelectionModel().setSelectionInterval(editIndex, editIndex);
	}

	void move (int direction) {
		if (direction < 0 && editIndex == 0) return;
		Array<ParticleEmitter> emitters = editor.effect.getEmitters();
		if (direction > 0 && editIndex == emitters.size - 1) return;
		int insertIndex = editIndex + direction;
		Object name = emitterTableModel.getValueAt(editIndex, 0);
		emitterTableModel.removeRow(editIndex);
		ParticleEmitter emitter = emitters.removeIndex(editIndex);
		emitterTableModel.insertRow(insertIndex, new Object[] {name});
		emitters.insert(insertIndex, emitter);
		editIndex = insertIndex;
		emitterTable.getSelectionModel().setSelectionInterval(editIndex, editIndex);
	}

	void emitterChecked (int index, boolean checked) {
		editor.setEnabled(editor.effect.getEmitters().get(index), checked);
		editor.effect.start();
	}

	private void initializeComponents () {
		setLayout(new GridBagLayout());
		{
			JPanel sideButtons = new JPanel(new GridBagLayout());
			add(sideButtons, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
			{
				JButton newButton = new JButton("New");
				sideButtons.add(newButton, new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
				newButton.addActionListener(new ActionListener() {
					public void actionPerformed (ActionEvent event) {
						newEmitter("Untitled", true);
					}
				});
			}
			{
				JButton newButton = new JButton("Duplicate");
				sideButtons.add(newButton, new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
				newButton.addActionListener(new ActionListener() {
					public void actionPerformed (ActionEvent event) {
						duplicateEmitter();
					}
				});
			}
			{
				JButton deleteButton = new JButton("Delete");
				sideButtons.add(deleteButton, new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
				deleteButton.addActionListener(new ActionListener() {
					public void actionPerformed (ActionEvent event) {
						deleteEmitter();
					}
				});
			}
			{
				sideButtons.add(new JSeparator(JSeparator.HORIZONTAL), new GridBagConstraints(0, -1, 1, 1, 0, 0,
					GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
			}
			{
				JButton saveButton = new JButton("Save");
				sideButtons.add(saveButton, new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
				saveButton.addActionListener(new ActionListener() {
					public void actionPerformed (ActionEvent event) {
						saveEffect();
					}
				});
			}
			{
				JButton openButton = new JButton("Open");
				sideButtons.add(openButton, new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
				openButton.addActionListener(new ActionListener() {
					public void actionPerformed (ActionEvent event) {
						openEffect(false);
					}
				});
			}
			{
				JButton mergeButton = new JButton("Merge");
				sideButtons.add(mergeButton, new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
				mergeButton.addActionListener(new ActionListener() {
					public void actionPerformed (ActionEvent event) {
						openEffect(true);
					}
				});
			}
			{
				JButton upButton = new JButton("Up");
				sideButtons.add(upButton, new GridBagConstraints(0, -1, 1, 1, 0, 1, GridBagConstraints.SOUTH,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
				upButton.addActionListener(new ActionListener() {
					public void actionPerformed (ActionEvent event) {
						move(-1);
					}
				});
			}
			{
				JButton downButton = new JButton("Down");
				sideButtons.add(downButton, new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				downButton.addActionListener(new ActionListener() {
					public void actionPerformed (ActionEvent event) {
						move(1);
					}
				});
			}
		}
		{
			JScrollPane scroll = new JScrollPane();
			add(scroll, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,
				0, 0, 6), 0, 0));
			{
				emitterTable = new JTable() {
					public Class getColumnClass (int column) {
						return column == 1 ? Boolean.class : super.getColumnClass(column);
					}
				};
				emitterTable.getTableHeader().setReorderingAllowed(false);
				emitterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				scroll.setViewportView(emitterTable);
				emitterTableModel = new DefaultTableModel(new String[0][0], new String[] {"Emitter", ""});
				emitterTable.setModel(emitterTableModel);
				emitterTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
					public void valueChanged (ListSelectionEvent event) {
						if (event.getValueIsAdjusting()) return;
						emitterSelected();
					}
				});
				emitterTableModel.addTableModelListener(new TableModelListener() {
					public void tableChanged (TableModelEvent event) {
						if (event.getColumn() != 1) return;
						emitterChecked(event.getFirstRow(), (Boolean)emitterTable.getValueAt(event.getFirstRow(), 1));
					}
				});
			}
		}
	}
}
