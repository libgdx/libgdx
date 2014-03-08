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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.particles.Particle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.WeigthMesh;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.values.CylinderSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.EllipseSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.LineSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.MeshSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.PointSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.PrimitiveSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.RectangleSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;
import com.badlogic.gdx.graphics.g3d.particles.values.SpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.WeightMeshSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.PrimitiveSpawnShapeValue.SpawnSide;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.UBJsonReader;

class SpawnInfluencerPanel extends EditorPanel {
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
	RangedNumericPanel xPanel, yPanel, zPanel;
	PointSpawnShapeValue pointSpawnShapeValue;
	LineSpawnShapeValue lineSpawnShapeValue;
	RectangleSpawnShapeValue rectangleSpawnShapeValue;
	EllipseSpawnShapeValue ellipseSpawnShapeValue;
	CylinderSpawnShapeValue cylinderSpawnShapeValue;
	MeshSpawnShapeValue meshSpawnShapeValue;
	WeightMeshSpawnShapeValue weightMeshSpawnShapeValue;

	public SpawnInfluencerPanel (final ParticleEditor3D editor, SpawnShapeValue spawnShapeValue) {
		super(editor, null, "Spawn Influencer", "Define where the particles are spawned.");
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
		
		initializeComponents();

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
				SpawnShapeInfluencer influencer = (SpawnShapeInfluencer)editor.getEmitter().findInfluencer(SpawnShapeInfluencer.class);
				PrimitiveSpawnShapeValue shapeValue = (PrimitiveSpawnShapeValue)influencer.spawnShapeValue;
				shapeValue.setEdges(edgesCheckbox.isSelected());
				setEdgesVisible(true);
			}
		});

		sideCombo.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				SpawnSide side = (SpawnSide)sideCombo.getSelectedItem();
				SpawnShapeInfluencer influencer = (SpawnShapeInfluencer)editor.getEmitter().findInfluencer(SpawnShapeInfluencer.class);
				EllipseSpawnShapeValue shapeValue = (EllipseSpawnShapeValue)influencer.spawnShapeValue;
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
	
	protected void setSpawnShapeValue(SpawnShapeValue spawnShapeValue){
		xPanel.setValue(spawnShapeValue.xOffsetValue);
		yPanel.setValue(spawnShapeValue.yOffsetValue);
		zPanel.setValue(spawnShapeValue.zOffsetValue);
	}

	protected void setPrimitiveSpawnShape (PrimitiveSpawnShapeValue shape, boolean showEdges, SpawnSide side) {
		setSpawnShapeValue(shape);
		SpawnShapeInfluencer influencer = (SpawnShapeInfluencer)editor.getEmitter().findInfluencer(SpawnShapeInfluencer.class);
		influencer.spawnShapeValue = shape;
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
		setSpawnShapeValue(shape);
		if(lastModel != null) {
			SpawnShapeInfluencer influencer = (SpawnShapeInfluencer)editor.getEmitter().findInfluencer(SpawnShapeInfluencer.class);
			influencer.spawnShapeValue = shape;
		}
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
		if(spawnShapeValue instanceof MeshSpawnShapeValue){
			return SPAWN_SHAPE_MESH;
		}
		
		return null;
	}

	public void update (ParticleEditor3D editor) {
		SpawnShapeInfluencer influencer = (SpawnShapeInfluencer)editor.getEmitter().findInfluencer(SpawnShapeInfluencer.class);
		shapeCombo.setSelectedItem( spawnShapeToString(influencer.spawnShapeValue));
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

	private void initializeComponents () {
		
		int i=0;
		OptionsPanel panel = new OptionsPanel();
		panel.addOption(i++, 0, "Shape:", shapeCombo = new JComboBox(new DefaultComboBoxModel(new String[]{	SPAWN_SHAPE_POINT, SPAWN_SHAPE_LINE, SPAWN_SHAPE_RECTANGLE,
																															SPAWN_SHAPE_ELLIPSE, SPAWN_SHAPE_CYLINDER, SPAWN_SHAPE_MESH, SPAWN_SHAPE_WEIGHT_MESH})), 
																															GridBagConstraints.WEST, GridBagConstraints.NONE);
		panel.addOption(i++, 0, edgesLabel = new JLabel("Edges:"), edgesCheckbox = new JCheckBox());
		panel.addOption(i++, 0, sideLabel = new JLabel("Side:"), sideCombo = new JComboBox(new DefaultComboBoxModel(SpawnSide.values())));
		i=0;
		addContent(i++, 0, panel, GridBagConstraints.WEST, GridBagConstraints.BOTH);
		addContent(i++, 0, meshPanel = new ModelPanel(editor, "", "") {
			@Override
			protected void onModelLoaded (Model model) {
				SpawnInfluencerPanel.this.onModelLoaded(model);
			}
		}, false, GridBagConstraints.WEST, GridBagConstraints.NONE);
		addContent(i++, 0, xPanel = new RangedNumericPanel(editor, pointSpawnShapeValue.xOffsetValue, "X Offset", "Amount to offset a particle's starting X location, in world units.", false));
		addContent(i++, 0, yPanel = new RangedNumericPanel(editor, pointSpawnShapeValue.yOffsetValue, "Y Offset", "Amount to offset a particle's starting Y location, in world units.", false));
		addContent(i++, 0, zPanel = new RangedNumericPanel(editor, pointSpawnShapeValue.zOffsetValue, "Z Offset", "Amount to offset a particle's starting Z location, in world units.", false));
		addContent(i++, 0, widthPanel = new ScaledNumericPanel(editor, pointSpawnShapeValue.getSpawnWidth(), "Duration", "Spawn Width", "Width of the spawn shape, in world units.", true));
		addContent(i++, 0, heightPanel = new ScaledNumericPanel(editor, pointSpawnShapeValue.getSpawnWidth(), "Duration", "Spawn Height", "Height of the spawn shape, in world units.", true));
		addContent(i++, 0, depthPanel = new ScaledNumericPanel(editor, pointSpawnShapeValue.getSpawnWidth(), "Duration", "Spawn Depth", "Depth of the spawn shape, in world units.", true), false);
	}

	private Model lastModel;
	protected void onModelLoaded (Model model) {
		//Update the shapes
		SpawnShapeValue shapeValue = null;
		weightMeshSpawnShapeValue.mesh = new WeigthMesh(model.meshes.get(0));
		weightMeshSpawnShapeValue.mesh.calculateWeights();
		meshSpawnShapeValue.setMesh(model.meshes.get(0));
		if(shapeCombo.getSelectedItem() == SPAWN_SHAPE_WEIGHT_MESH){
			SpawnShapeInfluencer influencer = (SpawnShapeInfluencer)editor.getEmitter().findInfluencer(SpawnShapeInfluencer.class);
			influencer.spawnShapeValue = weightMeshSpawnShapeValue;
		}
		else if(shapeCombo.getSelectedItem() == SPAWN_SHAPE_MESH){
			SpawnShapeInfluencer influencer = (SpawnShapeInfluencer)editor.getEmitter().findInfluencer(SpawnShapeInfluencer.class);
			influencer.spawnShapeValue = meshSpawnShapeValue;
		}
		if(lastModel != null){
			String modelPath = editor.assetManager.getAssetFileName(lastModel);
			editor.assetManager.setReferenceCount(modelPath, editor.assetManager.getReferenceCount(modelPath)-1);
		}
		lastModel = model;
	}
}
