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
package com.badlogic.gdx.tools.particleeditor3d;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
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
import com.badlogic.gdx.graphics.g3d.particles.controllers.BillboardParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.ModelInstanceParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.ParticleControllerParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.PointSpriteParticleController;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer.BillboardColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer.ModelInstanceColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer.PointSpriteColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ModelInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ParticleControllerInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer.BillboardScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer.ModelInstanceScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer.ParticleControllerScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer.PointSpriteScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer.BillboardSpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer.ModelInstanceSpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer.ParticleControllerSpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer.PointSpriteSpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.BillboardVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.ModelInstanceVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.ParticleControllerVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.PointSpriteVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.values.PointSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.BillboardPolarVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ModelInstancePolarVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ModelInstanceRotationVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ParticleControllerPolarVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.PointPolarVelocityValue;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.StreamUtils;

class EffectPanel extends JPanel {
	
	private enum ControllerType{
		Billboard(BillboardParticleController.class, "Billboard"),
		ModelInstance(ModelInstanceParticleController.class, "Model Instance"),
		ParticleController(ParticleControllerParticleController.class, "Particle Controller"),
		Point(PointSpriteParticleController.class, "Point");
		
		public Class type;
		public String desc;
		ControllerType(Class type, String desc){
			this.type = type;
			this.desc = desc;
		}
		
		@Override
		public String toString () {
			return desc;
		}
	}
	
	ParticleEditor3D editor;
	JTable emitterTable;
	DefaultTableModel emitterTableModel;
	int editIndex = -1;
	String lastDir;
	JComboBox<ControllerType> controllerTypeCombo;
	
	
	public EffectPanel (ParticleEditor3D editor) {
		this.editor = editor;
		initializeComponents();
	}

	public <T extends ParticleController> T createDefaultEmitter (Class<T> type, boolean select, boolean add) {

		T controller = null;
		if(type == BillboardParticleController.class){
			controller = (T)createDefaultBillboardController();
		}
		else if(type == ModelInstanceParticleController.class){
			controller = (T) createDefaultModelInstanceController();
		}
		else if(type == PointSpriteParticleController.class){
			controller = (T) createDefaultPointController();
		}
		else if(type == ParticleControllerParticleController.class){
			controller = (T) createDefaultParticleController();
		}
		
		if(add){
			controller.init();
			
			addEmitter(controller, select);
		}
		return controller;
	}

	private ModelInstanceParticleController createDefaultModelInstanceController () {
		//Emission
		RegularEmitter emitter = new RegularEmitter();
		emitter.getDuration().setLow(3000);
		emitter.getEmission().setHigh(80);
		emitter.getLife().setHigh(500, 1000);
		emitter.getLife().setTimeline(new float[] {0, 0.66f, 1});
		emitter.getLife().setScaling(new float[] {1, 1, 0.3f});
		emitter.setMaxParticleCount(100);
		
		//Scale
		ModelInstanceScaleInfluencer scaleInfluencer = new ModelInstanceScaleInfluencer();
		scaleInfluencer.scaleValue.setHigh(1);

		//Color
		ModelInstanceColorInfluencer colorInfluencer = new ModelInstanceColorInfluencer();
		colorInfluencer.colorValue.setColors(new float[] {1, 0.12156863f, 0.047058824f});
		colorInfluencer.alphaValue.setHigh(1);
		colorInfluencer.alphaValue.setTimeline(new float[] {0, 0.2f, 0.8f, 1});
		colorInfluencer.alphaValue.setScaling(new float[] {0, 1, 0.75f, 0});

		//Spawn
		PointSpawnShapeValue pointSpawnShapeValue = new PointSpawnShapeValue();
		ModelInstanceSpawnInfluencer spawnSource = new ModelInstanceSpawnInfluencer(pointSpawnShapeValue);

		//Velocity
		ModelInstanceVelocityInfluencer velocityInfluencer = new ModelInstanceVelocityInfluencer();

		//Directional
		ModelInstancePolarVelocityValue velocityValue = new ModelInstancePolarVelocityValue();
		ScaledNumericValue thetaValue = velocityValue.getTheta();
		thetaValue.setHigh(0, 359);
		thetaValue.setActive(true);
		ScaledNumericValue phiValue = velocityValue.getPhi();
		phiValue.setHigh(45, 135);
		phiValue.setLow(90);
		phiValue.setTimeline(new float[] {0, 0.5f, 1});
		phiValue.setScaling(new float[] {1, 0, 0});
		phiValue.setActive(true);
		velocityValue.getStrength().setHigh(5, 10);
		velocityValue.getStrength().setActive(true);
		velocityValue.setActive(true);
		velocityInfluencer.velocities.add(velocityValue);

		//Rotational
		ModelInstanceRotationVelocityValue rotationVelocityValue = new ModelInstanceRotationVelocityValue();
		rotationVelocityValue.strengthValue.setLow(0, 360);
		rotationVelocityValue.strengthValue.setHigh(180);
		rotationVelocityValue.strengthValue.setTimeline(new float[] {0, 1});
		rotationVelocityValue.strengthValue.setScaling(new float[] {0, 1});
		rotationVelocityValue.strengthValue.setRelative(true);
		velocityInfluencer.velocities.add(rotationVelocityValue);
		
		return new ModelInstanceParticleController("Default ModelInstance", emitter, editor.getModelInstanceParticleBatch(), 
			new ModelInfluencer.ModelInstanceRandomInfluencer((Model) editor.assetManager.get(ParticleEditor3D.DEFAULT_MODEL_PARTICLE) ),
			spawnSource,
			scaleInfluencer,
			colorInfluencer,
			velocityInfluencer
			);
	}

	private BillboardParticleController createDefaultBillboardController () {
		//Emission
		RegularEmitter emitter = new RegularEmitter();
		emitter.getDuration().setLow(3000);
		emitter.getEmission().setHigh(250);
		//emitter.getLife().setHigh(1000);
		emitter.getLife().setHigh(500, 1000);
		emitter.getLife().setTimeline(new float[] {0, 0.66f, 1});
		emitter.getLife().setScaling(new float[] {1, 1, 0.3f});
		emitter.setMaxParticleCount(200);

		//Spawn
		PointSpawnShapeValue pointSpawnShapeValue = new PointSpawnShapeValue();
		BillboardSpawnInfluencer spawnSource = new BillboardSpawnInfluencer(pointSpawnShapeValue);

		//Scale
		BillboardScaleInfluencer scaleInfluencer = new BillboardScaleInfluencer();
		scaleInfluencer.scaleValue.setHigh(1);

		//Color
		BillboardColorInfluencer colorInfluencer = new BillboardColorInfluencer();
		colorInfluencer.colorValue.setColors(new float[] {1, 0.12156863f, 0.047058824f});
		colorInfluencer.alphaValue.setHigh(1);
		colorInfluencer.alphaValue.setTimeline(new float[] {0, 0.2f, 0.8f, 1});
		colorInfluencer.alphaValue.setScaling(new float[] {0, 1, 0.75f, 0});
		
		//Velocity
		BillboardVelocityInfluencer velocityInfluencer = new BillboardVelocityInfluencer();

		//Directional
		BillboardPolarVelocityValue velocityValue = new BillboardPolarVelocityValue();
		ScaledNumericValue phiValue = velocityValue.getPhi();
		phiValue.setHigh(0, 359);
		phiValue.setActive(true);
		ScaledNumericValue thetaValue = velocityValue.getTheta();
		thetaValue.setHigh(45, -45);
		thetaValue.setLow(0);
		thetaValue.setTimeline(new float[] {0, 0.5f, 1});
		thetaValue.setScaling(new float[] {1, 0, 0});
		thetaValue.setActive(true);
		velocityValue.getStrength().setHigh(5, 10);
		velocityValue.getStrength().setActive(true);
		velocityValue.setActive(true);
		velocityInfluencer.velocities.add(velocityValue);

		return new BillboardParticleController("Default Billboard", emitter, editor.getBillboardBatch(), 
			new RegionInfluencer.BillboardSingleRegionInfluencer((Texture) editor.assetManager.get(ParticleEditor3D.DEFAULT_BILLBOARD_PARTICLE) ),
			//new BillboardRegionInfluencer.Animated(new TextureRegion((Texture)editor.assetManager.get(ParticleEditor3D.DEFAULT_BILLBOARD_PARTICLE))),
			spawnSource,
			scaleInfluencer,
			colorInfluencer,
			velocityInfluencer
			);
	}
	
	private PointSpriteParticleController createDefaultPointController () {
		//Emission
		RegularEmitter emitter = new RegularEmitter();
		emitter.getDuration().setLow(3000);
		emitter.getEmission().setHigh(250);
		emitter.getLife().setHigh(500, 1000);
		emitter.getLife().setTimeline(new float[] {0, 0.66f, 1});
		emitter.getLife().setScaling(new float[] {1, 1, 0.3f});
		emitter.setMaxParticleCount(200);
		
		//Scale
		PointSpriteScaleInfluencer scaleInfluencer = new PointSpriteScaleInfluencer();
		scaleInfluencer.scaleValue.setHigh(1);

		//Color
		PointSpriteColorInfluencer colorInfluencer = new PointSpriteColorInfluencer();
		colorInfluencer.colorValue.setColors(new float[] {1, 0.12156863f, 0.047058824f});
		colorInfluencer.alphaValue.setHigh(1);
		colorInfluencer.alphaValue.setTimeline(new float[] {0, 0.2f, 0.8f, 1});
		colorInfluencer.alphaValue.setScaling(new float[] {0, 1, 0.75f, 0});

		//Spawn
		PointSpawnShapeValue pointSpawnShapeValue = new PointSpawnShapeValue();
		PointSpriteSpawnInfluencer spawnSource = new PointSpriteSpawnInfluencer(pointSpawnShapeValue);

		//Velocity
		PointSpriteVelocityInfluencer velocityInfluencer = new PointSpriteVelocityInfluencer();

		//Directional
		PointPolarVelocityValue velocityValue = new PointPolarVelocityValue();
		ScaledNumericValue phiValue = velocityValue.getPhi();
		phiValue.setHigh(0, 359);
		phiValue.setActive(true);
		ScaledNumericValue thetaValue = velocityValue.getTheta();
		thetaValue.setHigh(45, -45);
		thetaValue.setLow(0);
		thetaValue.setTimeline(new float[] {0, 0.5f, 1});
		thetaValue.setScaling(new float[] {1, 0, 0});
		thetaValue.setActive(true);
		velocityValue.getStrength().setHigh(5, 10);
		velocityValue.getStrength().setActive(true);
		velocityValue.setActive(true);
		velocityInfluencer.velocities.add(velocityValue);

		//Rotational
		/*
		PointRotationVelocityValue rotationVelocityValue = new PointRotationVelocityValue();
		rotationVelocityValue.strengthValue.setLow(0, 360);
		rotationVelocityValue.strengthValue.setHigh(180);
		rotationVelocityValue.strengthValue.setTimeline(new float[] {0, 1});
		rotationVelocityValue.strengthValue.setScaling(new float[] {0, 1});
		rotationVelocityValue.strengthValue.setRelative(true);
		velocityInfluencer.velocities.add(rotationVelocityValue);
		*/
		
		return new PointSpriteParticleController("Default PointSprite", emitter, editor.getPointSpriteBatch(),
			new RegionInfluencer.PointSpriteAnimatedRegionInfluencer((Texture) editor.assetManager.get(ParticleEditor3D.DEFAULT_BILLBOARD_PARTICLE) ),
			spawnSource,
			scaleInfluencer,
			colorInfluencer,
			velocityInfluencer
			);
	}
	
	private ParticleControllerParticleController createDefaultParticleController () {
		//Emission
		RegularEmitter emitter = new RegularEmitter();
		emitter.getDuration().setLow(5000);
		emitter.getEmission().setHigh(9);
		emitter.getLife().setHigh(5000);
		emitter.setMaxParticleCount(10);

		//Scale
		ParticleControllerScaleInfluencer scaleInfluencer = new ParticleControllerScaleInfluencer();
		scaleInfluencer.scaleValue.setHigh(1);

		//Spawn
		PointSpawnShapeValue pointSpawnShapeValue = new PointSpawnShapeValue();
		ParticleControllerSpawnInfluencer spawnSource = new ParticleControllerSpawnInfluencer(pointSpawnShapeValue);

		//Velocity
		ParticleControllerVelocityInfluencer velocityInfluencer = new ParticleControllerVelocityInfluencer();

		//Directional
		ParticleControllerPolarVelocityValue velocityValue = new ParticleControllerPolarVelocityValue();
		ScaledNumericValue phiValue = velocityValue.getPhi();
		phiValue.setHigh(0, 359);
		phiValue.setActive(true);
		ScaledNumericValue thetaValue = velocityValue.getTheta();
		thetaValue.setHigh(45, -45);
		thetaValue.setLow(0);
		thetaValue.setTimeline(new float[] {0, 0.5f, 1});
		thetaValue.setScaling(new float[] {1, 0, 0});
		thetaValue.setActive(true);
		velocityValue.getStrength().setHigh(5, 10);
		velocityValue.getStrength().setActive(true);
		velocityValue.setActive(true);
		velocityInfluencer.velocities.add(velocityValue);

		//Rotational
		/*
		PointRotationVelocityValue rotationVelocityValue = new PointRotationVelocityValue();
		rotationVelocityValue.strengthValue.setLow(0, 360);
		rotationVelocityValue.strengthValue.setHigh(180);
		rotationVelocityValue.strengthValue.setTimeline(new float[] {0, 1});
		rotationVelocityValue.strengthValue.setScaling(new float[] {0, 1});
		rotationVelocityValue.strengthValue.setRelative(true);
		velocityInfluencer.velocities.add(rotationVelocityValue);
		*/
		
		return new ParticleControllerParticleController("Default ParticleController", emitter,
			new ParticleControllerInfluencer.ParticleControllerSingleInfluencer(
					editor.assetManager.get(ParticleEditor3D.DEFAULT_PFX, ParticleEffect.class).getControllers().get(0)),
			spawnSource,
			scaleInfluencer,
			velocityInfluencer
			);
	}
	

	private void addEmitter (final ParticleController emitter, boolean select) {
		Array<ParticleController> emitters = editor.effect.getControllers();
		emitters.add(emitter);
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
		editor.setEnabled(editor.effect.getControllers().get(index), checked);
		editor.effect.start();
	}
	
	void openEffect () {
		File file = editor.showFileLoadDialog();
		if(file != null){
			if(editor.openEffect(file, true) != null){
				emitterTableModel.getDataVector().removeAllElements();
				for (ParticleController emitter : editor.effect.getControllers()) {
					emitterTableModel.addRow(new Object[] {emitter.name, true});
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
			for (ParticleController emitter : editor.effect.getControllers())
				emitter.name = ((String)emitterTableModel.getValueAt(index++, 0));
			editor.saveEffect(file);
		}
	}

	void deleteEmitter () {
		int row = emitterTable.getSelectedRow();
		if (row == -1) return;
		
		int newIndex = Math.min(editIndex, emitterTableModel.getRowCount()-2);
		
		editor.effect.getControllers().removeIndex(row).dispose();
		emitterTableModel.removeRow(row);

		//Reload data check
		emitterTable.getSelectionModel().setSelectionInterval(newIndex, newIndex);
	}
	

	protected void cloneEmitter () {
		int row = emitterTable.getSelectedRow();
		if (row == -1) return;
		ParticleController controller = editor.effect.getControllers().get(row).copy();
		controller.init();
		controller.name +=" Clone";
		addEmitter(controller, true);
	}

	void move (int direction) {
		Array<ParticleController> emitters = editor.effect.getControllers();
		if ( (direction < 0 && editIndex == 0) || (direction > 0 && editIndex == emitters.size - 1)) return;
		int insertIndex = editIndex + direction;
		Object name = emitterTableModel.getValueAt(editIndex, 0);
		ParticleController emitter = emitters.removeIndex(editIndex);
		emitterTableModel.removeRow(editIndex);
		emitterTableModel.insertRow(insertIndex, new Object[] {name, editor.isEnabled(emitter)});
		emitters.insert(insertIndex, emitter);
		editIndex = insertIndex;
		emitterTable.getSelectionModel().setSelectionInterval(editIndex, editIndex);
	}

	private void initializeComponents () {
		setLayout(new GridBagLayout());
		{
			JPanel sideButtons = new JPanel(new GridBagLayout());
			add(sideButtons, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
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
						createDefaultEmitter(item.type, true, true);
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
