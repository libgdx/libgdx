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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.values.CylinderSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.EllipseSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.LineSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.PointSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.PrimitiveSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.PrimitiveSpawnShapeValue.SpawnSide;
import com.badlogic.gdx.graphics.g3d.particles.values.RectangleSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.SpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.UnweightedMeshSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.WeightMeshSpawnShapeValue;
import com.badlogic.gdx.utils.Array;

/** @author Inferno */
class SpawnInfluencerPanel extends InfluencerPanel<SpawnInfluencer> implements TemplatePickerPanel.Listener<Model> {
	private static final String SPAWN_SHAPE_POINT = "Point",
		SPAWN_SHAPE_LINE = "Line",
		SPAWN_SHAPE_RECTANGLE = "Rectangle",
		SPAWN_SHAPE_CYLINDER = "Cylinder",
		SPAWN_SHAPE_ELLIPSE = "Ellipse",
		SPAWN_SHAPE_MESH = "Unweighted Mesh",
		SPAWN_SHAPE_WEIGHT_MESH = "Weighted Mesh";
	private static String[] spawnShapes = new String[]{	SPAWN_SHAPE_POINT, SPAWN_SHAPE_LINE, SPAWN_SHAPE_RECTANGLE,
		SPAWN_SHAPE_ELLIPSE, SPAWN_SHAPE_CYLINDER, 
		SPAWN_SHAPE_MESH, SPAWN_SHAPE_WEIGHT_MESH};
	JComboBox shapeCombo;
	JCheckBox edgesCheckbox;
	JLabel edgesLabel;
	JComboBox sideCombo;
	JLabel sideLabel;
	TemplatePickerPanel<Model> meshPanel;
	ScaledNumericPanel widthPanel, heightPanel, depthPanel;
	RangedNumericPanel xPanel, yPanel, zPanel;
	PointSpawnShapeValue pointSpawnShapeValue;
	LineSpawnShapeValue lineSpawnShapeValue;
	RectangleSpawnShapeValue rectangleSpawnShapeValue;
	EllipseSpawnShapeValue ellipseSpawnShapeValue;
	CylinderSpawnShapeValue cylinderSpawnShapeValue;
	UnweightedMeshSpawnShapeValue meshSpawnShapeValue;
	WeightMeshSpawnShapeValue weightMeshSpawnShapeValue;

	public SpawnInfluencerPanel (final FlameMain editor, SpawnInfluencer influencer) {
		super(editor, influencer, "Spawn Influencer", "Define where the particles are spawned.", true, false);
		setValue(influencer);
		setCurrentSpawnData(influencer.spawnShapeValue);
		shapeCombo.setSelectedItem(spawnShapeToString(influencer.spawnShapeValue));
	}

	private void setCurrentSpawnData (SpawnShapeValue spawnShapeValue) {
		SpawnShapeValue local = null;
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
		if(spawnShapeValue instanceof UnweightedMeshSpawnShapeValue)
			local = meshSpawnShapeValue;
		else if(spawnShapeValue instanceof WeightMeshSpawnShapeValue)
			local = weightMeshSpawnShapeValue;
		local.load(spawnShapeValue);
	}
	
	protected void setSpawnShapeValue(SpawnShapeValue spawnShapeValue){
		xPanel.setValue(spawnShapeValue.xOffsetValue);
		yPanel.setValue(spawnShapeValue.yOffsetValue);
		zPanel.setValue(spawnShapeValue.zOffsetValue);
	}

	protected void setPrimitiveSpawnShape (PrimitiveSpawnShapeValue shape, boolean showEdges, SpawnSide side) {
		setSpawnShapeValue(shape);
		SpawnInfluencer influencer = (SpawnInfluencer)editor.getEmitter().findInfluencer(SpawnInfluencer.class);
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
		value.spawnShapeValue = shape;
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
		if(spawnShapeValue instanceof UnweightedMeshSpawnShapeValue){
			return SPAWN_SHAPE_MESH;
		}
		
		return null;
	}

	public void update (FlameMain editor) {
		SpawnInfluencer influencer = (SpawnInfluencer)editor.getEmitter().findInfluencer(SpawnInfluencer.class);
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

	protected void initializeComponents () {
		super.initializeComponents();
		
		pointSpawnShapeValue = new PointSpawnShapeValue();
		lineSpawnShapeValue = new LineSpawnShapeValue();
		rectangleSpawnShapeValue = new RectangleSpawnShapeValue();
		ellipseSpawnShapeValue = new EllipseSpawnShapeValue();
		cylinderSpawnShapeValue = new CylinderSpawnShapeValue();
		meshSpawnShapeValue = new UnweightedMeshSpawnShapeValue();
		weightMeshSpawnShapeValue = new WeightMeshSpawnShapeValue();
		
		lineSpawnShapeValue.setDimensions(6, 6, 6);
		rectangleSpawnShapeValue.setDimensions(6, 6, 6);
		ellipseSpawnShapeValue.setDimensions(6, 6, 6);
		cylinderSpawnShapeValue.setDimensions(6, 6, 6);

		pointSpawnShapeValue.setActive(true);
		lineSpawnShapeValue.setActive(true);
		rectangleSpawnShapeValue.setActive(true);
		ellipseSpawnShapeValue.setActive(true);
		cylinderSpawnShapeValue.setActive(true);
		meshSpawnShapeValue.setActive(true);
		weightMeshSpawnShapeValue.setActive(true);
		
		Model defaultModel = editor.assetManager.get(FlameMain.DEFAULT_MODEL_PARTICLE);
		Array<Model> models = new Array<Model>();
		models.add(defaultModel);
		
		int i=0;
		JPanel panel = new JPanel(new GridBagLayout());
		EditorPanel.addContent(panel, i, 0, new JLabel("Shape"), false, GridBagConstraints.WEST, GridBagConstraints.NONE, 0,0);
		EditorPanel.addContent(panel, i++,1, shapeCombo = new JComboBox(new DefaultComboBoxModel(spawnShapes)), false, GridBagConstraints.WEST, GridBagConstraints.NONE, 1,0);
		EditorPanel.addContent(panel, i, 0, edgesLabel = new JLabel("Edges"), false, GridBagConstraints.WEST, GridBagConstraints.NONE, 0,0);	
		EditorPanel.addContent(panel, i++, 1, edgesCheckbox = new JCheckBox(), false, GridBagConstraints.WEST, GridBagConstraints.NONE, 0,0);		
		EditorPanel.addContent(panel, i, 0, sideLabel = new JLabel("Side"), false, GridBagConstraints.WEST, GridBagConstraints.NONE, 0,0);		
		EditorPanel.addContent(panel, i++, 1, sideCombo = new JComboBox(new DefaultComboBoxModel(SpawnSide.values())), false, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);				
		edgesCheckbox.setHorizontalTextPosition(SwingConstants.LEFT);

		i=0;
		addContent(i++, 0, panel, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
		addContent(i++, 0, meshPanel = new TemplatePickerPanel<Model>(editor, models, this, Model.class, new LoaderButton.ModelLoaderButton(editor), true, false)
																																			, false, GridBagConstraints.WEST, GridBagConstraints.NONE);
		addContent(i++, 0, xPanel = new RangedNumericPanel(editor, pointSpawnShapeValue.xOffsetValue, "X Offset", "Amount to offset a particle's starting X location, in world units.", false));
		addContent(i++, 0, yPanel = new RangedNumericPanel(editor, pointSpawnShapeValue.yOffsetValue, "Y Offset", "Amount to offset a particle's starting Y location, in world units.", false));
		addContent(i++, 0, zPanel = new RangedNumericPanel(editor, pointSpawnShapeValue.zOffsetValue, "Z Offset", "Amount to offset a particle's starting Z location, in world units.", false));
		addContent(i++, 0, widthPanel = new ScaledNumericPanel(editor, pointSpawnShapeValue.getSpawnWidth(), "Duration", "Spawn Width", "Width of the spawn shape, in world units.", true));
		addContent(i++, 0, heightPanel = new ScaledNumericPanel(editor, pointSpawnShapeValue.getSpawnWidth(), "Duration", "Spawn Height", "Height of the spawn shape, in world units.", true));
		addContent(i++, 0, depthPanel = new ScaledNumericPanel(editor, pointSpawnShapeValue.getSpawnWidth(), "Duration", "Spawn Depth", "Depth of the spawn shape, in world units.", true), false);
		
		meshPanel.setIsAlwayShown(true);
		onTemplateChecked(defaultModel, true);
		
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
				editor.restart();
			}
		});

		edgesCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				SpawnInfluencer influencer = (SpawnInfluencer)editor.getEmitter().findInfluencer(SpawnInfluencer.class);
				PrimitiveSpawnShapeValue shapeValue = (PrimitiveSpawnShapeValue)influencer.spawnShapeValue;
				shapeValue.setEdges(edgesCheckbox.isSelected());
				setEdgesVisible(true);
			}
		});

		sideCombo.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				SpawnSide side = (SpawnSide)sideCombo.getSelectedItem();
				SpawnInfluencer influencer = (SpawnInfluencer)editor.getEmitter().findInfluencer(SpawnInfluencer.class);
				EllipseSpawnShapeValue shapeValue = (EllipseSpawnShapeValue)influencer.spawnShapeValue;
				shapeValue.setSide(side);
			}
		});

	}

	@Override
	public void onTemplateChecked (Model model, boolean isChecked) {
		//Update the shapes
		SpawnShapeValue shapeValue = null;
		Mesh mesh = model.meshes.get(0);
		weightMeshSpawnShapeValue.setMesh(mesh, model);
		meshSpawnShapeValue.setMesh(mesh, model);
		if(shapeCombo.getSelectedItem() == SPAWN_SHAPE_WEIGHT_MESH){
			SpawnInfluencer influencer = (SpawnInfluencer)editor.getEmitter().findInfluencer(SpawnInfluencer.class);
			influencer.spawnShapeValue = weightMeshSpawnShapeValue;
		}
		else if(shapeCombo.getSelectedItem() == SPAWN_SHAPE_MESH){
			SpawnInfluencer influencer = (SpawnInfluencer)editor.getEmitter().findInfluencer(SpawnInfluencer.class);
			influencer.spawnShapeValue = meshSpawnShapeValue;
		}
		editor.restart();
	}

}
