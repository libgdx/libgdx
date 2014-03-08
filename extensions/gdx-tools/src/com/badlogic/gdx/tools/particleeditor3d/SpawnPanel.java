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
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.newparticles.WeigthMesh;
import com.badlogic.gdx.graphics.g3d.newparticles.values.CylinderSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.newparticles.values.EllipseSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.newparticles.values.LineSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.newparticles.values.MeshSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.newparticles.values.PointSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.newparticles.values.PrimitiveSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.newparticles.values.RectangleSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.newparticles.values.ScaledNumericValue;
import com.badlogic.gdx.graphics.g3d.newparticles.values.SpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.newparticles.values.WeightMeshSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.newparticles.values.PrimitiveSpawnShapeValue.SpawnSide;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEmitter;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.UBJsonReader;

class SpawnPanel extends EditorPanel {
	private static final String SPAWN_SHAPE_POINT = "Point",
		SPAWN_SHAPE_LINE = "Line",
		SPAWN_SHAPE_RECTANGLE = "Rectangle",
		SPAWN_SHAPE_CYLINDER = "Cylinder",
		SPAWN_SHAPE_ELLIPSE = "Ellipse",
		SPAWN_SHAPE_MESH = "Mesh",
		SPAWN_SHAPE_WEIGHT_MESH = "Weight Mesh";
	JComboBox shapeCombo;
	JCheckBox edgesCheckbox;
	JLabel edgesLabel;
	JComboBox sideCombo;
	JLabel sideLabel;
	JPanel meshPanel;
	ScaledNumericPanel widthPanel, heightPanel, depthPanel;
	PointSpawnShapeValue pointSpawnShapeValue;
	LineSpawnShapeValue lineSpawnShapeValue;
	RectangleSpawnShapeValue rectangleSpawnShapeValue;
	EllipseSpawnShapeValue ellipseSpawnShapeValue;
	CylinderSpawnShapeValue cylinderSpawnShapeValue;
	MeshSpawnShapeValue meshSpawnShapeValue;
	WeightMeshSpawnShapeValue weightMeshSpawnShapeValue;
	ParticleEditor3D editor;

	public SpawnPanel (final ParticleEditor3D editor, SpawnShapeValue spawnShapeValue, String charTitle,  String name, String description) {
		super(null, name, description);
		this.editor = editor;
		pointSpawnShapeValue = new PointSpawnShapeValue();
		lineSpawnShapeValue = new LineSpawnShapeValue();
		rectangleSpawnShapeValue = new RectangleSpawnShapeValue();
		ellipseSpawnShapeValue = new EllipseSpawnShapeValue();
		cylinderSpawnShapeValue = new CylinderSpawnShapeValue();
		meshSpawnShapeValue = new MeshSpawnShapeValue();
		weightMeshSpawnShapeValue = new WeightMeshSpawnShapeValue();
		
		pointSpawnShapeValue.setActive(true);
		lineSpawnShapeValue.setActive(true);
		rectangleSpawnShapeValue.setActive(true);
		ellipseSpawnShapeValue.setActive(true);
		cylinderSpawnShapeValue.setActive(true);
		meshSpawnShapeValue.setActive(true);
		weightMeshSpawnShapeValue.setActive(true);
		
		initializeComponents(charTitle);

		shapeCombo.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				String shape = (String)shapeCombo.getSelectedItem();
				if(shape == SPAWN_SHAPE_POINT){
					setPrimitiveSpawnShape(pointSpawnShapeValue, false, null);
				}
				else if(shape == SPAWN_SHAPE_LINE){
					setPrimitiveSpawnShape(lineSpawnShapeValue, false, null);
				}
				else if(shape == SPAWN_SHAPE_RECTANGLE){
					setPrimitiveSpawnShape(rectangleSpawnShapeValue, true, null);
				}
				else if(shape == SPAWN_SHAPE_ELLIPSE){
					setPrimitiveSpawnShape(ellipseSpawnShapeValue, true, ellipseSpawnShapeValue.getSide());
				}
				else if(shape == SPAWN_SHAPE_CYLINDER){
					setPrimitiveSpawnShape(cylinderSpawnShapeValue, true, null);
				}
				else if(shape == SPAWN_SHAPE_MESH){
					setMeshSpawnShape(meshSpawnShapeValue);
				}
				else if(shape == SPAWN_SHAPE_WEIGHT_MESH){
					setMeshSpawnShape(weightMeshSpawnShapeValue);
				}
			}
		});

		edgesCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				PrimitiveSpawnShapeValue shapeValue = (PrimitiveSpawnShapeValue)editor.getEmitter().getSpawnShape();
				shapeValue.setEdges(edgesCheckbox.isSelected());
				setEdgesVisible(true);
			}
		});

		sideCombo.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				SpawnSide side = (SpawnSide)sideCombo.getSelectedItem();
				EllipseSpawnShapeValue shapeValue = (EllipseSpawnShapeValue)editor.getEmitter().getSpawnShape();
				shapeValue.setSide(side);
			}
		});

		setCurrentSpawnData(spawnShapeValue); //Copy the spawn shape data to the local editing version
		shapeCombo.setSelectedItem(spawnShapeToString(spawnShapeValue));
	}

	private void setCurrentSpawnData (SpawnShapeValue spawnShapeValue) {
		if(spawnShapeValue instanceof PrimitiveSpawnShapeValue){
			PrimitiveSpawnShapeValue local = null;
			if(spawnShapeValue instanceof PointSpawnShapeValue) 
				local = pointSpawnShapeValue;
			else if(spawnShapeValue instanceof LineSpawnShapeValue) 
				local = lineSpawnShapeValue;
			else if(spawnShapeValue instanceof RectangleSpawnShapeValue) 
				local = rectangleSpawnShapeValue;
			else if(spawnShapeValue instanceof EllipseSpawnShapeValue)
				local = ellipseSpawnShapeValue;
			else if(spawnShapeValue instanceof CylinderSpawnShapeValue)
				local = cylinderSpawnShapeValue;
			local.load(spawnShapeValue);
		}
	}

	protected void setPrimitiveSpawnShape (PrimitiveSpawnShapeValue shape, boolean showEdges, SpawnSide side) {
		editor.getEmitter().setSpawnShape(shape);
		widthPanel.setValue(shape.getSpawnWidth());
		heightPanel.setValue(shape.getSpawnHeight());
		depthPanel.setValue(shape.getSpawnDepth());
		setEdgesVisible(showEdges);
		if(showEdges) 
			edgesCheckbox.setSelected(shape.isEdges());
		if(side != null){
			setSidesVisible(true);
			sideCombo.setSelectedItem(side);
		}
		else {
			setSidesVisible(false);
		}

		widthPanel.setVisible(true);
		heightPanel.setVisible(true);
		depthPanel.setVisible(true);
		meshPanel.setVisible(false);
	}
	
	protected void setMeshSpawnShape (SpawnShapeValue shape) {
		if(lastModel != null) editor.getEmitter().setSpawnShape(shape);
		setEdgesVisible(false);
		setSidesVisible(false);
		widthPanel.setVisible(false);
		heightPanel.setVisible(false);
		depthPanel.setVisible(false);
		meshPanel.setVisible(true);
	}

	private Object spawnShapeToString (SpawnShapeValue spawnShapeValue) {
		if(spawnShapeValue instanceof PrimitiveSpawnShapeValue){
			if(spawnShapeValue instanceof PointSpawnShapeValue) return SPAWN_SHAPE_POINT;
			else if(spawnShapeValue instanceof LineSpawnShapeValue) return SPAWN_SHAPE_LINE;
			else if(spawnShapeValue instanceof RectangleSpawnShapeValue) return SPAWN_SHAPE_RECTANGLE;
			else if(spawnShapeValue instanceof EllipseSpawnShapeValue) return SPAWN_SHAPE_ELLIPSE;
			else if(spawnShapeValue instanceof CylinderSpawnShapeValue) return SPAWN_SHAPE_CYLINDER;
		}
		if(spawnShapeValue instanceof WeightMeshSpawnShapeValue){
			return SPAWN_SHAPE_WEIGHT_MESH;
		}
		return null;
	}

	public void update (ParticleEditor3D editor) {
		shapeCombo.setSelectedItem( spawnShapeToString(editor.getEmitter().getSpawnShape()));
	}

	void setEdgesVisible (boolean visible)
	{
		edgesCheckbox.setVisible(visible);
		edgesLabel.setVisible(visible);
	}
	
	void setSidesVisible(boolean visible)
	{
		sideCombo.setVisible(visible);
		sideLabel.setVisible(visible);
	}

	private void initializeComponents (String charTitle) {
		JPanel contentPanel = getContentPanel();
		{
			JLabel label = new JLabel("Shape:");
			contentPanel.add(label, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			shapeCombo = new JComboBox();
			shapeCombo.setModel(new DefaultComboBoxModel(new String[]{SPAWN_SHAPE_POINT, SPAWN_SHAPE_LINE, SPAWN_SHAPE_RECTANGLE,
				SPAWN_SHAPE_ELLIPSE, SPAWN_SHAPE_CYLINDER, SPAWN_SHAPE_MESH, SPAWN_SHAPE_WEIGHT_MESH}));
			contentPanel.add(shapeCombo, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			edgesLabel = new JLabel("Edges:");
			contentPanel.add(edgesLabel, new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 12, 0, 6), 0, 0));
		}
		{
			edgesCheckbox = new JCheckBox();
			contentPanel.add(edgesCheckbox, new GridBagConstraints(3, 1, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			sideLabel = new JLabel("Side:");
			contentPanel.add(sideLabel, new GridBagConstraints(4, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 12, 0, 6), 0, 0));
		}
		{
			sideCombo = new JComboBox();
			sideCombo.setModel(new DefaultComboBoxModel(SpawnSide.values()));
			contentPanel.add(sideCombo, new GridBagConstraints(5, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			contentPanel.add( widthPanel = new ScaledNumericPanel(pointSpawnShapeValue.getSpawnWidth(), charTitle, "Spawn Width", "Width of the spawn shape, in world units.", true), 
					new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
							new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			contentPanel.add(heightPanel = new ScaledNumericPanel(pointSpawnShapeValue.getSpawnWidth(), charTitle, "Spawn Height", "Height of the spawn shape, in world units.", true),
					new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
							new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			contentPanel.add(depthPanel = new ScaledNumericPanel(pointSpawnShapeValue.getSpawnWidth(), charTitle, "Spawn Depth", "Depth of the spawn shape, in world units.", true),
					new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
							new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			meshPanel = new JPanel();
			JLabel label = new JLabel("Mesh:");
			meshPanel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 6), 0, 0));
			JButton loadButton = new JButton("Open");
			meshPanel.add(loadButton, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 6), 0, 0));
			loadButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					loadMesh();
				}
			});
			contentPanel.add(meshPanel,
				new GridBagConstraints(0, 5, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			JPanel spacer = new JPanel();
			spacer.setPreferredSize(new Dimension());
			contentPanel.add(spacer, new GridBagConstraints(6, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
	}

	private String lastDir;
	private Model lastModel;
	protected void loadMesh () {
		FileDialog dialog = new FileDialog(editor, "Open Mesh", FileDialog.LOAD);
		if (lastDir != null) dialog.setDirectory(lastDir);
		dialog.setVisible(true);
		final String file = dialog.getFile();
		final String dir = dialog.getDirectory();
		if (dir == null || file == null || file.trim().length() == 0) return;
		lastDir = dir; 
		ParticleEffect effect = new ParticleEffect();
		try {
			Model model = null;
			FileHandle handle = Gdx.files.absolute(new File(dir, file).getAbsolutePath());
			if(file.endsWith(".obj")){
				ObjLoader loader = new ObjLoader();
				model = loader.loadModel(handle);
			}
			else if(file.endsWith(".g3dj")){
				G3dModelLoader loader = new G3dModelLoader(new JsonReader());
				model = loader.loadModel(handle);
			}
			else if(file.endsWith(".g3db")){
				G3dModelLoader loader = new G3dModelLoader(new UBJsonReader());
				model = loader.loadModel(handle);
			}
			else throw new Exception();
			SpawnShapeValue shapeValue = null;
			//Update the shapes
			weightMeshSpawnShapeValue.mesh = new WeigthMesh(model.meshes.get(0));
			weightMeshSpawnShapeValue.mesh.calculateWeights();
			meshSpawnShapeValue.setMesh(model.meshes.get(0));
			if(shapeCombo.getSelectedItem() == SPAWN_SHAPE_WEIGHT_MESH){
				editor.getEmitter().setSpawnShape(weightMeshSpawnShapeValue);
			}
			else if(shapeCombo.getSelectedItem() == SPAWN_SHAPE_MESH){
				editor.getEmitter().setSpawnShape(meshSpawnShapeValue);
			}
			if(lastModel != null)lastModel.dispose();
			lastModel = model;
		} catch (Exception ex) {
			System.out.println("Error loading effect: " + new File(dir, file).getAbsolutePath());
			ex.printStackTrace();
			JOptionPane.showMessageDialog(editor, "Error opening effect.");
			return;
		}
		
	}
}
