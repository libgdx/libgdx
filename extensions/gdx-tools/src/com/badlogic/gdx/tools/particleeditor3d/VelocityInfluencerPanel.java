package com.badlogic.gdx.tools.particleeditor3d;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.BillboardVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.ModelInstanceVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.ParticleControllerVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.VelocityInfluencer.PointSpriteVelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.values.AngularVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.StrengthVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.BillboardBrownianVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.BillboardCentripetalVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.BillboardPolarVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.BillboardRotationVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.BillboardTangetialVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.BillboardWeightVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ModelInstanceBrownianVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ModelInstanceCentripetalVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ModelInstanceFaceVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ModelInstancePolarVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ModelInstanceRotationVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ModelInstanceTangetialVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ModelInstanceWeightVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ParticleControllerBrownianVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ParticleControllerCentripetalVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ParticleControllerFaceVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ParticleControllerPolarVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ParticleControllerRotationVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ParticleControllerTangetialVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.ParticleControllerWeightVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.PointBrownianVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.PointCentripetalVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.PointPolarVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.PointRotationVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.PointTangetialVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityValues.PointWeightVelocityValue;
import com.badlogic.gdx.utils.Array;

public class VelocityInfluencerPanel extends InfluencerPanel<VelocityInfluencer> {
	
	private static final String 	VEL_TYPE_ROTATIONAL = "Rotational",
											VEL_TYPE_CENTRIPETAL = "Centripetal",
											VEL_TYPE_TANGENTIAL = "Tangetial",
											VEL_TYPE_POLAR = "Polar",
											VEL_TYPE_WEIGHT = "Weight",
											VEL_TYPE_BROWNIAN = "Brownian", 
											VEL_TYPE_FACE = "Face";
	
	protected class VelocityWrapper{
		public VelocityValue velocityValue;
		public boolean isActive;
		
		public VelocityWrapper(VelocityValue value, boolean isActive){
			this.velocityValue =  value;
			this.isActive = isActive;
		}
	}
	
	JComboBox velocityBox;
	JTable velocityTable;
	DefaultTableModel velocityTableModel;
	JPanel selectedVelocityPanel;
	AngularVelocityPanel angularVelocityPanel;
	StrengthVelocityPanel strengthVelocityPanel;
	ParticleValuePanel emptyPanel;
	Array<VelocityWrapper> velocities;
	
	public VelocityInfluencerPanel (ParticleEditor3D editor, VelocityInfluencer influencer) {
		super(editor, influencer, "Velocity Influencer", 
							"Defines how the particles move in the space, in world units per second (ie. gravity, wind, etc...).");
		velocities = new Array<VelocityWrapper>();
		setValue(value);
		set(influencer);
	}

	private void set (VelocityInfluencer influencer) {
		//Clear
		for (int i = velocityTableModel.getRowCount() - 1; i >= 0; i--) {
			velocityTableModel.removeRow(i);
		}
		velocities.clear();
		
		//Add
		for(int i=0, c = influencer.velocities.size; i < c; ++i){
			velocities.add(new VelocityWrapper((VelocityValue)influencer.velocities.items[i], true));
			velocityTableModel.addRow(new Object[] {"Velocity "+i, true});
		}
		
		DefaultComboBoxModel model = (DefaultComboBoxModel) velocityBox.getModel();
		model.removeAllElements();
		for(Object velocityObject : getAvailableVelocities(influencer)){
			model.addElement(velocityObject);
		}
	}

	private Object[] getAvailableVelocities (VelocityInfluencer influencer) {
		if(influencer instanceof BillboardVelocityInfluencer || 
			influencer instanceof PointSpriteVelocityInfluencer){
			return new String[]{	VEL_TYPE_ROTATIONAL, VEL_TYPE_CENTRIPETAL, VEL_TYPE_TANGENTIAL,
				VEL_TYPE_POLAR, VEL_TYPE_WEIGHT, VEL_TYPE_BROWNIAN};
		}
		else if(	influencer instanceof ParticleControllerVelocityInfluencer || 
			influencer instanceof ModelInstanceVelocityInfluencer){
			return new String[]{	VEL_TYPE_ROTATIONAL, VEL_TYPE_CENTRIPETAL, VEL_TYPE_TANGENTIAL,
				VEL_TYPE_POLAR, VEL_TYPE_WEIGHT, VEL_TYPE_BROWNIAN, VEL_TYPE_FACE};
		}
		return null;
	}

	protected void initializeComponents () {
		super.initializeComponents();
		JPanel velocitiesPanel = new JPanel();
		velocitiesPanel.setLayout(new GridBagLayout());
		{
			JPanel sideButtons = new JPanel(new GridBagLayout());
			velocitiesPanel.add(sideButtons, new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
			{
				sideButtons.add(velocityBox = new JComboBox(new DefaultComboBoxModel()), 
					new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
			}
			{
				JButton newButton = new JButton("New");
				sideButtons.add(newButton, new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
				newButton.addActionListener(new ActionListener() {
					public void actionPerformed (ActionEvent event) {
						createVelocity(velocityBox.getSelectedItem());
					}
				});
			}
			{
				JButton deleteButton = new JButton("Delete");
				sideButtons.add(deleteButton, new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
				deleteButton.addActionListener(new ActionListener() {
					public void actionPerformed (ActionEvent event) {
						deleteVelocity();
					}
				});
			}
		}
		JScrollPane scroll = new JScrollPane();
		velocitiesPanel.add(scroll, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
			0, 0, 6), 0, 0));
		velocityTable = new JTable() {
			public Class getColumnClass (int column) {
				return column == 1 ? Boolean.class : super.getColumnClass(column);
			}
			
			@Override
			public Dimension getPreferredScrollableViewportSize () {
				Dimension dim = super.getPreferredScrollableViewportSize();
				// here we return the pref height
				dim.height = getPreferredSize().height;
				return dim;
			}
		};
		velocityTable.getTableHeader().setReorderingAllowed(false);
		velocityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scroll.setViewportView(velocityTable);
		velocityTableModel = new DefaultTableModel(new String[0][0], new String[] {"Velocity", "Active"});
		velocityTable.setModel(velocityTableModel);
		velocityTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged (ListSelectionEvent event) {
				if (event.getValueIsAdjusting()) return;
				velocitySelected();
			}
		});
		velocityTableModel.addTableModelListener(new TableModelListener() {
			public void tableChanged (TableModelEvent event) {
				if (event.getColumn() != 1) return;
				velocityChecked(event.getFirstRow(), (Boolean)velocityTable.getValueAt(event.getFirstRow(), 1));
			}
		});
				
		//Velocity values
		emptyPanel = new ParticleValuePanel(editor, "", "",true, false);
		strengthVelocityPanel = new StrengthVelocityPanel(editor, null, "Life", "", "");
		angularVelocityPanel = new AngularVelocityPanel(editor, null, "Life", "", "");
		strengthVelocityPanel.setVisible(false);
		angularVelocityPanel.setVisible(false);
		emptyPanel.setVisible(false);
		strengthVelocityPanel.setIsAlwayShown(true);
		angularVelocityPanel.setIsAlwayShown(true);
		emptyPanel.setIsAlwayShown(true);
		emptyPanel.setValue(null);
		
		//Assemble
		int i=0;
		addContent(i++, 0, velocitiesPanel);
		addContent(i++, 0, strengthVelocityPanel);
		addContent(i++, 0, angularVelocityPanel);
		addContent(i++, 0, emptyPanel);
	}

	protected void velocityChecked (int index, boolean isChecked) {
		ParticleController controller = editor.getEmitter();
		VelocityInfluencer influencer = (VelocityInfluencer)controller.findInfluencer(VelocityInfluencer.class);		
		influencer.velocities.clear();
		velocities.get(index).isActive = isChecked;
		for(VelocityWrapper wrapper : velocities){
			if(wrapper.isActive)
				influencer.velocities.add(wrapper.velocityValue);
		}
		//Restart the effect and reinit the controller
		controller.init();
		editor.effect.start();
	}

	protected void velocitySelected () {
		//Show the velocity value panel
		int index = velocityTable.getSelectedRow();
		if(index == -1) return;
		
		//Gdx.app.log("INFERNO", "Selected velocity at "+index);
		
		//VelocityInfluencer influencer = (VelocityInfluencer)editor.getEmitter().findInfluencer(VelocityInfluencer.class);		
		VelocityValue velocityValue = velocities.get(index).velocityValue;
		ParticleValuePanel velocityPanel = getVelocityPanel(velocityValue);
		
		//Show the selected velocity
		if(selectedVelocityPanel != null && selectedVelocityPanel != velocityPanel) 
			selectedVelocityPanel.setVisible(false);
		velocityPanel.setVisible(true);
		velocityPanel.showContent(true);
		selectedVelocityPanel = velocityPanel;
	}

	private ParticleValuePanel getVelocityPanel (VelocityValue velocityValue) {
		ParticleValuePanel panel = null;
		//Billboards
		if(velocityValue instanceof BillboardRotationVelocityValue || 
			velocityValue instanceof PointRotationVelocityValue ){
			strengthVelocityPanel.setValue((StrengthVelocityValue) velocityValue);
			strengthVelocityPanel.setName("Rotational Velocity");
			strengthVelocityPanel.setDescription("The angular speed around the billboard facing direction, in degrees/sec .");
			panel = strengthVelocityPanel;
		}
		else if(	velocityValue instanceof BillboardCentripetalVelocityValue || 
					velocityValue instanceof PointCentripetalVelocityValue ||
					velocityValue instanceof ModelInstanceCentripetalVelocityValue || 
					velocityValue instanceof ParticleControllerCentripetalVelocityValue){
			strengthVelocityPanel.setValue((StrengthVelocityValue) velocityValue);
			strengthVelocityPanel.setName("Centripetal Velocity");
			strengthVelocityPanel.setDescription("A directional velocity, the direction is toward the origin if global, or towards the emitter position if local, in world units/sec .");
			panel = strengthVelocityPanel;
		}
		else if(	velocityValue instanceof BillboardTangetialVelocityValue || 
					velocityValue instanceof PointTangetialVelocityValue || 
					velocityValue instanceof ModelInstanceTangetialVelocityValue || 
					velocityValue instanceof ParticleControllerTangetialVelocityValue){
			angularVelocityPanel.setValue((AngularVelocityValue) velocityValue);
			angularVelocityPanel.setName("Tangetial Velocity");
			angularVelocityPanel.setDescription("A directional velocity (axis and magnitude), the final direction is the cross product between particle position and the axis, in world units/sec .");
			panel = angularVelocityPanel;
		}
		else if(	velocityValue instanceof BillboardPolarVelocityValue ||  
					velocityValue instanceof PointPolarVelocityValue ||
					velocityValue instanceof ModelInstancePolarVelocityValue || 
					velocityValue instanceof ParticleControllerPolarVelocityValue){
			angularVelocityPanel.setValue((AngularVelocityValue) velocityValue);
			angularVelocityPanel.setName("Polar Velocity");
			angularVelocityPanel.setDescription("A directional velocity (axis and magnitude), in world units/sec .");
			panel = angularVelocityPanel;
		}
		else if(	velocityValue instanceof BillboardWeightVelocityValue || 
					velocityValue instanceof PointWeightVelocityValue || 
					velocityValue instanceof ModelInstanceWeightVelocityValue || 
					velocityValue instanceof ParticleControllerWeightVelocityValue){
			strengthVelocityPanel.setValue((StrengthVelocityValue) velocityValue);
			strengthVelocityPanel.setName("Weight Velocity");
			strengthVelocityPanel.setDescription("A directional velocity which is equal to emitter velocity times the weight, in world units/sec.");
			panel = strengthVelocityPanel;
		}
		else if(	velocityValue instanceof BillboardBrownianVelocityValue || 
					velocityValue instanceof PointBrownianVelocityValue ||
					velocityValue instanceof ModelInstanceBrownianVelocityValue ||
					velocityValue instanceof ParticleControllerBrownianVelocityValue){
			strengthVelocityPanel.setValue((StrengthVelocityValue) velocityValue);
			strengthVelocityPanel.setName("Brownian Velocity");
			strengthVelocityPanel.setDescription("A directional velocity which has random direction at each update, in world units/sec.");
			panel = strengthVelocityPanel;
		}
		else if(velocityValue instanceof ModelInstanceRotationVelocityValue || 
			velocityValue instanceof ParticleControllerRotationVelocityValue ){
			angularVelocityPanel.setValue((AngularVelocityValue) velocityValue);
			angularVelocityPanel.setName("Rotation Velocity");
			angularVelocityPanel.setDescription("A rotational velocity (axis and magnitude), in degree/sec .");
			panel = angularVelocityPanel;
		}
		else if(	velocityValue instanceof ModelInstanceFaceVelocityValue  ||
					velocityValue instanceof ParticleControllerFaceVelocityValue){ 
			emptyPanel.setName("Face Velocity");
			emptyPanel.setDescription("Rotates the model to face its current velocity (Do not add any other rotation velocities when using this and ensure this is the last velocity in the list).");
			panel = emptyPanel;
		}

		return panel;
	}
	
	private VelocityValue createVelocityValue (VelocityInfluencer influencer, Object selectedItem) {
		VelocityValue velocityValue = null;
		if(influencer instanceof BillboardVelocityInfluencer){
			if(selectedItem == VEL_TYPE_ROTATIONAL) velocityValue = new BillboardRotationVelocityValue();
			else if(selectedItem == VEL_TYPE_CENTRIPETAL) velocityValue = new BillboardCentripetalVelocityValue();
			else if(selectedItem == VEL_TYPE_TANGENTIAL) velocityValue = new BillboardTangetialVelocityValue();
			else if(selectedItem == VEL_TYPE_POLAR) velocityValue = new BillboardPolarVelocityValue();
			else if(selectedItem == VEL_TYPE_WEIGHT) velocityValue = new BillboardWeightVelocityValue();
			else if(selectedItem == VEL_TYPE_BROWNIAN) velocityValue = new BillboardBrownianVelocityValue();
		}
		else if(influencer instanceof ModelInstanceVelocityInfluencer){
			if(selectedItem == VEL_TYPE_ROTATIONAL) velocityValue = new ModelInstanceRotationVelocityValue();
			else if(selectedItem == VEL_TYPE_CENTRIPETAL) velocityValue = new ModelInstanceCentripetalVelocityValue();
			else if(selectedItem == VEL_TYPE_TANGENTIAL) velocityValue = new ModelInstanceTangetialVelocityValue();
			else if(selectedItem == VEL_TYPE_POLAR) velocityValue = new ModelInstancePolarVelocityValue();
			else if(selectedItem == VEL_TYPE_WEIGHT) velocityValue = new ModelInstanceWeightVelocityValue();
			else if(selectedItem == VEL_TYPE_BROWNIAN) velocityValue = new ModelInstanceBrownianVelocityValue();
			else if(selectedItem == VEL_TYPE_FACE) velocityValue = new ModelInstanceFaceVelocityValue();
		}
		else if(influencer instanceof PointSpriteVelocityInfluencer){
			if(selectedItem == VEL_TYPE_ROTATIONAL) velocityValue = new PointRotationVelocityValue();
			else if(selectedItem == VEL_TYPE_CENTRIPETAL) velocityValue = new PointCentripetalVelocityValue();
			else if(selectedItem == VEL_TYPE_TANGENTIAL) velocityValue = new PointTangetialVelocityValue();
			else if(selectedItem == VEL_TYPE_POLAR) velocityValue = new PointPolarVelocityValue();
			else if(selectedItem == VEL_TYPE_WEIGHT) velocityValue = new PointWeightVelocityValue();
			else if(selectedItem == VEL_TYPE_BROWNIAN) velocityValue = new PointBrownianVelocityValue();
		}
		else if(influencer instanceof ParticleControllerVelocityInfluencer){
			if(selectedItem == VEL_TYPE_ROTATIONAL) velocityValue = new ParticleControllerRotationVelocityValue();
			else if(selectedItem == VEL_TYPE_CENTRIPETAL) velocityValue = new ParticleControllerCentripetalVelocityValue();
			else if(selectedItem == VEL_TYPE_TANGENTIAL) velocityValue = new ParticleControllerTangetialVelocityValue();
			else if(selectedItem == VEL_TYPE_POLAR) velocityValue = new ParticleControllerPolarVelocityValue();
			else if(selectedItem == VEL_TYPE_WEIGHT) velocityValue = new ParticleControllerWeightVelocityValue();
			else if(selectedItem == VEL_TYPE_BROWNIAN) velocityValue = new ParticleControllerBrownianVelocityValue();
			else if(selectedItem == VEL_TYPE_FACE) velocityValue = new ParticleControllerFaceVelocityValue();
		}
		
		return velocityValue;
	}
	

	protected void deleteVelocity () {
		int row = velocityTable.getSelectedRow();
		if (row == -1) return;
		
		//Remove the velocity from the table
		ParticleController controller = editor.getEmitter();
		VelocityInfluencer influencer = (VelocityInfluencer)controller.findInfluencer(VelocityInfluencer.class);
		influencer.velocities.removeValue(velocities.removeIndex(row).velocityValue, true);
		velocityTableModel.removeRow(row);
		
		//Restart the effect and reinit the controller
		controller.init();
		editor.effect.start();

		selectedVelocityPanel.setVisible(false);
		selectedVelocityPanel = null;
	}

	protected void createVelocity (Object selectedItem) {
		//Add the velocity to the table and to the influencer
		ParticleController controller = editor.getEmitter();
		VelocityInfluencer influencer = (VelocityInfluencer)controller.findInfluencer(VelocityInfluencer.class);
		VelocityWrapper wrapper = new VelocityWrapper(createVelocityValue(influencer, selectedItem), true);
		velocities.add(wrapper);
		influencer.velocities.add(wrapper.velocityValue);
		int index = velocities.size-1;
		velocityTableModel.addRow(new Object[] {"Velocity "+index, true});
		
		//Reinit
		controller.init();
		editor.effect.start();
		
		//Select new velocity
		velocityTable.getSelectionModel().setSelectionInterval(index, index);
		revalidate();
		repaint();
	}
	
}
