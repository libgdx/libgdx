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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ModelInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ParticleControllerFinalizerInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ParticleControllerInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ModelInstanceRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ParticleControllerControllerRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.PointSpriteRenderer;
import com.badlogic.gdx.graphics.g3d.particles.values.EllipseSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.PointSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.PrimitiveSpawnShapeValue.SpawnSide;
import com.badlogic.gdx.tools.flame.FlameMain.ControllerData;
import com.badlogic.gdx.tools.flame.FlameMain.ControllerType;

/** @author Inferno */
class EffectPanel extends JPanel {
	FlameMain editor;
	JTable emitterTable;
	DefaultTableModel emitterTableModel;
	int editIndex = -1;
	String lastDir;
	JComboBox controllerTypeCombo;
	
	
	public EffectPanel (FlameMain editor) {
		this.editor = editor;
		initializeComponents();
	}

	public <T extends ParticleController> T createDefaultEmitter (ControllerType type, boolean select, boolean add) {

		T controller = null;
		if(type == ControllerType.Billboard){
			controller = (T)createDefaultBillboardController();
		}
		else if(type == ControllerType.ModelInstance){
			controller = (T) createDefaultModelInstanceController();
		}
		else if(type == ControllerType.PointSprite){
			controller = (T) createDefaultPointController();
		}
		else if(type == ControllerType.ParticleController){
			controller = (T) createDefaultParticleController();
		}
		
		if(add){
			controller.init();
			addEmitter(controller, select);
		}
		return controller;
	}

	private ParticleController createDefaultModelInstanceController () {
		//Emission
		RegularEmitter emitter = new RegularEmitter();
		emitter.getDuration().setLow(3000);
		emitter.getEmission().setHigh(80);
		emitter.getLife().setHigh(500, 1000);
		emitter.getLife().setTimeline(new float[] {0, 0.66f, 1});
		emitter.getLife().setScaling(new float[] {1, 1, 0.3f});
		emitter.setMaxParticleCount(100);

		//Color
		ColorInfluencer.Random colorInfluencer = new ColorInfluencer.Random();

		//Spawn
		EllipseSpawnShapeValue spawnShapeValue = new EllipseSpawnShapeValue();
		spawnShapeValue.setDimensions(1, 1, 1);
		SpawnInfluencer spawnSource = new SpawnInfluencer(spawnShapeValue);

		//Velocity
		DynamicsInfluencer velocityInfluencer = new DynamicsInfluencer();

		//Directional
		DynamicsModifier.CentripetalAcceleration velocityValue = new DynamicsModifier.CentripetalAcceleration();
		velocityValue.strengthValue.setHigh(5, 11);
		velocityValue.strengthValue.setActive(true);
		//velocityValue.setActive(true);
		velocityInfluencer.velocities.add(velocityValue);
		//VelocityModifier.FaceDirection faceVelocityValue = new VelocityModifier.FaceDirection();
		//velocityInfluencer.velocities.add(faceVelocityValue);
		
		return new ParticleController("ModelInstance Controller", emitter, new ModelInstanceRenderer(editor.getModelInstanceParticleBatch()), 
			new ModelInfluencer.Single((Model) editor.assetManager.get(FlameMain.DEFAULT_MODEL_PARTICLE) ),
			spawnSource,
			colorInfluencer,
			velocityInfluencer
			);
	}

	private ParticleController createDefaultBillboardController () {
		//Emission
		RegularEmitter emitter = new RegularEmitter();
		emitter.getDuration().setLow(3000);
		emitter.getEmission().setHigh(250);
		emitter.getLife().setHigh(500, 1000);
		emitter.getLife().setTimeline(new float[] {0, 0.66f, 1});
		emitter.getLife().setScaling(new float[] {1, 1, 0.3f});
		emitter.setMaxParticleCount(200);

		//Spawn
		PointSpawnShapeValue pointSpawnShapeValue = new PointSpawnShapeValue();
		SpawnInfluencer spawnSource = new SpawnInfluencer(pointSpawnShapeValue);

		//Color
		ColorInfluencer.Single colorInfluencer = new ColorInfluencer.Single();
		colorInfluencer.colorValue.setColors(new float[] {1, 0.12156863f, 0.047058824f, 0,0,0});
		colorInfluencer.colorValue.setTimeline(new float[] {0, 1});
		colorInfluencer.alphaValue.setHigh(1);
		colorInfluencer.alphaValue.setTimeline(new float[] {0, 0.5f, 0.8f, 1});
		colorInfluencer.alphaValue.setScaling(new float[] {0, 0.15f, 0.5f, 0});
		
		//Velocity
		DynamicsInfluencer velocityInfluencer = new DynamicsInfluencer();

		//Directional
		DynamicsModifier.PolarAcceleration velocityValue = new DynamicsModifier.PolarAcceleration();
		velocityValue.phiValue.setHigh(-35, 35);
		velocityValue.phiValue.setActive(true);
		velocityValue.phiValue.setTimeline(new float[] {0, 0.5f, 1});
		velocityValue.phiValue.setScaling(new float[] {1, 0, 0});
		velocityValue.thetaValue.setHigh(0, 360);
		velocityValue.strengthValue.setHigh(5, 10);
		velocityInfluencer.velocities.add(velocityValue);

		return new ParticleController("Billboard Controller", emitter, new BillboardRenderer(editor.getBillboardBatch()), 
			new RegionInfluencer.Single(editor.getTexture()),
			spawnSource,
			colorInfluencer,
			velocityInfluencer
			);
	}
	
	private ParticleController createDefaultPointController () {
		//Emission
		RegularEmitter emitter = new RegularEmitter();
		emitter.getDuration().setLow(3000);
		emitter.getEmission().setHigh(250);
		emitter.getLife().setHigh(500, 1000);
		emitter.getLife().setTimeline(new float[] {0, 0.66f, 1});
		emitter.getLife().setScaling(new float[] {1, 1, 0.3f});
		emitter.setMaxParticleCount(200);
		
		//Scale
		ScaleInfluencer scaleInfluencer = new ScaleInfluencer();
		scaleInfluencer.value.setHigh(1);

		//Color
		ColorInfluencer.Single colorInfluencer = new ColorInfluencer.Single();
		colorInfluencer.colorValue.setColors(new float[] {0.12156863f, 0.047058824f, 1, 0,0,0});
		colorInfluencer.colorValue.setTimeline(new float[] {0, 1});
		colorInfluencer.alphaValue.setHigh(1);
		colorInfluencer.alphaValue.setTimeline(new float[] {0, 0.5f, 0.8f, 1});
		colorInfluencer.alphaValue.setScaling(new float[] {0, 0.15f, 0.5f, 0});

		//Spawn
		PointSpawnShapeValue pointSpawnShapeValue = new PointSpawnShapeValue();
		SpawnInfluencer spawnSource = new SpawnInfluencer(pointSpawnShapeValue);

		//Velocity
		DynamicsInfluencer velocityInfluencer = new DynamicsInfluencer();

		//Directional
		DynamicsModifier.PolarAcceleration velocityValue = new DynamicsModifier.PolarAcceleration();
		velocityValue.phiValue.setHigh(-35, 35);
		velocityValue.phiValue.setActive(true);
		velocityValue.phiValue.setTimeline(new float[] {0, 0.5f, 1});
		velocityValue.phiValue.setScaling(new float[] {1, 0, 0});
		velocityValue.thetaValue.setHigh(0, 360);
		velocityValue.strengthValue.setHigh(5, 10);

		return new ParticleController("PointSprite Controller", emitter, new PointSpriteRenderer(editor.getPointSpriteBatch()),
			new RegionInfluencer.Single((Texture) editor.assetManager.get(FlameMain.DEFAULT_BILLBOARD_PARTICLE) ),
			spawnSource,
			scaleInfluencer,
			colorInfluencer,
			velocityInfluencer
			);
	}
	
	private ParticleController createDefaultParticleController () {
		//Emission
		RegularEmitter emitter = new RegularEmitter();
		emitter.getDuration().setLow(3000);
		emitter.getEmission().setHigh(90);
		emitter.getLife().setHigh(3000);
		emitter.setMaxParticleCount(100);

		//Spawn
		EllipseSpawnShapeValue pointSpawnShapeValue = new EllipseSpawnShapeValue();
		pointSpawnShapeValue.setDimensions(1, 1, 1);
		pointSpawnShapeValue.setSide(SpawnSide.top);
		SpawnInfluencer spawnSource = new SpawnInfluencer(pointSpawnShapeValue);

		//Scale
		ScaleInfluencer scaleInfluencer = new ScaleInfluencer();
		scaleInfluencer.value.setHigh(1);
		scaleInfluencer.value.setLow(0);
		scaleInfluencer.value.setTimeline(new float[]{0,1});
		scaleInfluencer.value.setScaling(new float[]{1, 0});
		
		//Velocity
		DynamicsInfluencer velocityInfluencer = new DynamicsInfluencer();

		//Directional
		DynamicsModifier.CentripetalAcceleration velocityValue = new DynamicsModifier.CentripetalAcceleration();
		velocityValue.strengthValue.setHigh(5, 10);
		velocityValue.strengthValue.setActive(true);
		velocityInfluencer.velocities.add(velocityValue);

		return new ParticleController("ParticleController Controller", emitter, new ParticleControllerControllerRenderer(),
			new ParticleControllerInfluencer.Single(editor.assetManager.get(FlameMain.DEFAULT_TEMPLATE_PFX, ParticleEffect.class).getControllers().get(0)),
			spawnSource,
			scaleInfluencer,
			velocityInfluencer,
			new ParticleControllerFinalizerInfluencer()
			);
	}
	
	public ParticleController createDefaultTemplateController(){
		//Emission
		RegularEmitter emitter = new RegularEmitter();
		emitter.getDuration().setLow(3000);
		emitter.getEmission().setHigh(90);
		emitter.getLife().setHigh(1000);
		emitter.getLife().setTimeline(new float[] {0, 0.66f, 1});
		emitter.getLife().setScaling(new float[] {1, 1, 0.3f});
		emitter.setMaxParticleCount(100);

		//Spawn
		PointSpawnShapeValue pointSpawnShapeValue = new PointSpawnShapeValue();		
		pointSpawnShapeValue.xOffsetValue.setLow(0, 1f);
		pointSpawnShapeValue.xOffsetValue.setActive(true);
		pointSpawnShapeValue.yOffsetValue.setLow(0, 1f);
		pointSpawnShapeValue.yOffsetValue.setActive(true);
		pointSpawnShapeValue.zOffsetValue.setLow(0, 1f);
		pointSpawnShapeValue.zOffsetValue.setActive(true);
		SpawnInfluencer spawnSource = new SpawnInfluencer(pointSpawnShapeValue);

		ScaleInfluencer scaleInfluencer = new ScaleInfluencer();
		scaleInfluencer.value.setHigh(1f);
		
		//Color
		ColorInfluencer.Single colorInfluencer = new ColorInfluencer.Single();
		colorInfluencer.colorValue.setColors(new float[] {1, 0.12156863f, 0.047058824f, 0,0,0});
		colorInfluencer.colorValue.setTimeline(new float[] {0, 1});
		colorInfluencer.alphaValue.setHigh(1);
		colorInfluencer.alphaValue.setTimeline(new float[] {0, 0.5f, 0.8f, 1});
		colorInfluencer.alphaValue.setScaling(new float[] {0, 0.15f, 0.5f, 0});

		return new ParticleController("Billboard Controller", emitter, new BillboardRenderer(editor.getBillboardBatch()), 
			new RegionInfluencer.Single(editor.getTexture()),
			spawnSource,
			scaleInfluencer,
			colorInfluencer
			);
	}

	private void addEmitter (final ParticleController emitter, boolean select) {
		editor.addEmitter(emitter);
		emitterTableModel.addRow(new Object[] {emitter.name, true});
		
		int row = emitterTableModel.getRowCount() - 1;
		emitterChecked (row, true); 
		
		if (select) {
			emitterTable.getSelectionModel().setSelectionInterval(row, row);
		}
	}

	void emitterSelected () {
		int row = emitterTable.getSelectedRow();
		if (row == editIndex) 
			return;
		
		editIndex = row;
		editor.reloadRows();
	}

	void emitterChecked (int index, boolean checked) {
		editor.setEnabled(index, checked);
	}
	
	void openEffect () {
		File file = editor.showFileLoadDialog();
		if(file != null){
			if(editor.openEffect(file, true) != null){
				emitterTableModel.getDataVector().removeAllElements();
				for (ControllerData data : editor.controllersData) {
					emitterTableModel.addRow(new Object[] {data.controller.name, true});
				}
				editIndex = 0;
				emitterTable.getSelectionModel().setSelectionInterval(editIndex, editIndex);
			}
		}
	}
	
	protected void importEffect () {
		File file = editor.showFileLoadDialog();
		if(file != null){
			ParticleEffect effect;
			if( (effect = editor.openEffect(file, false)) != null){
				for(ParticleController controller : effect.getControllers())
					addEmitter(controller, false);
				editIndex = 0;
				emitterTable.getSelectionModel().setSelectionInterval(editIndex, editIndex);
			}
		}
	}

	void saveEffect () {
		File file = editor.showFileSaveDialog();
		if(file != null){
			int index = 0;
			for (ControllerData data : editor.controllersData)
				data.controller.name = ((String)emitterTableModel.getValueAt(index++, 0));
			editor.saveEffect(file);
		}
	}

	void deleteEmitter () {
		int row = emitterTable.getSelectedRow();
		if (row == -1) return;
		
		int newIndex = Math.min(editIndex, emitterTableModel.getRowCount()-2);
		
		editor.removeEmitter(row);
		emitterTableModel.removeRow(row);

		//Reload data check
		emitterTable.getSelectionModel().setSelectionInterval(newIndex, newIndex);
	}

	protected void cloneEmitter () {
		int row = emitterTable.getSelectedRow();
		if (row == -1) return;
		ParticleController controller = editor.controllersData.get(row).controller.copy();
		controller.init();
		controller.name +=" Clone";
		addEmitter(controller, true);
	}

	void move (int direction) {
		/*
		Array<ParticleController> emitters = editor.effect.getControllers();
		if ( (direction < 0 && editIndex == 0) || (direction > 0 && editIndex == emitters.size - 1)) return;
		int insertIndex = editIndex + direction;
		Object name = emitterTableModel.getValueAt(editIndex, 0);
		boolean isEnabled = editor.isEnabled(editIndex);
		ParticleController emitter = emitters.removeIndex(editIndex);
		emitterTableModel.removeRow(editIndex);
		emitterTableModel.insertRow(insertIndex, new Object[] {name, isEnabled});
		emitters.insert(insertIndex, emitter);
		editIndex = insertIndex;
		emitterTable.getSelectionModel().setSelectionInterval(editIndex, editIndex);
		*/
	}

	private void initializeComponents () {
		setLayout(new GridBagLayout());
		{
			JScrollPane scroll = new JScrollPane();
			add(scroll, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,
				0, 0, 6), 0, 0));
			{
				emitterTable = new JTable() {
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
		{
			JPanel sideButtons = new JPanel(new GridBagLayout());
			add(sideButtons, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
			{
				controllerTypeCombo = new JComboBox();
				controllerTypeCombo.setModel(new DefaultComboBoxModel(ControllerType.values()));
				sideButtons.add(controllerTypeCombo, new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
			}
			{
				JButton newButton = new JButton("New");
				sideButtons.add(newButton, new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
				newButton.addActionListener(new ActionListener() {
					public void actionPerformed (ActionEvent event) {
						ControllerType item = (ControllerType)controllerTypeCombo.getSelectedItem();
						createDefaultEmitter(item, true, true);
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
				JButton cloneButton = new JButton("Clone");
				sideButtons.add(cloneButton, new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
				cloneButton.addActionListener(new ActionListener() {
					public void actionPerformed (ActionEvent event) {
						cloneEmitter();
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
						openEffect();
					}
				});
			}
			{
				JButton importButton = new JButton("Import");
				sideButtons.add(importButton, new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
				importButton.addActionListener(new ActionListener() {
					public void actionPerformed (ActionEvent event) {
						importEffect();
					}
				});
			}
			/*
			{
				JButton importButton = new JButton("Export");
				sideButtons.add(importButton, new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
				importButton.addActionListener(new ActionListener() {
					public void actionPerformed (ActionEvent event) {
						exportEffect();
					}
				});
			}
			*/
		}
	}

}
