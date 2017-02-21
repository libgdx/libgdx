package com.badlogic.gdx.tools.flame;

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
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier.BrownianAcceleration;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier.CentripetalAcceleration;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier.FaceDirection;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier.PolarAcceleration;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier.Rotational3D;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier.TangentialAcceleration;
import com.badlogic.gdx.tools.flame.FlameMain.ControllerType;
import com.badlogic.gdx.utils.Array;

/** @author Inferno */
public class DynamicsInfluencerPanel extends InfluencerPanel<DynamicsInfluencer> {
	
	private static final String 	VEL_TYPE_ROTATIONAL_2D = "Angular Velocity 2D",
															VEL_TYPE_ROTATIONAL_3D = "Angular Velocity 3D",
											VEL_TYPE_CENTRIPETAL = "Centripetal",
											VEL_TYPE_TANGENTIAL = "Tangential",
											VEL_TYPE_POLAR = "Polar",
											VEL_TYPE_BROWNIAN = "Brownian", 
											VEL_TYPE_FACE = "Face";
	
	protected class VelocityWrapper{
		public DynamicsModifier velocityValue;
		public boolean isActive;
		
		public VelocityWrapper(DynamicsModifier value, boolean isActive){
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
	
	public DynamicsInfluencerPanel (FlameMain editor, DynamicsInfluencer influencer) {
		super(editor, influencer, "Dynamics Influencer", 
							"Defines how the particles dynamics (acceleration, angular velocity).");
		velocities = new Array<VelocityWrapper>();
		setValue(value);
		set(influencer);
	}

	private void set (DynamicsInfluencer influencer) {
		//Clear
		for (int i = velocityTableModel.getRowCount() - 1; i >= 0; i--) {
			velocityTableModel.removeRow(i);
		}
		velocities.clear();
		
		//Add
		for(int i=0, c = influencer.velocities.size; i < c; ++i){
			velocities.add(new VelocityWrapper((DynamicsModifier)influencer.velocities.items[i], true));
			velocityTableModel.addRow(new Object[] {"Velocity "+i, true});
		}
		
		DefaultComboBoxModel model = (DefaultComboBoxModel) velocityBox.getModel();
		model.removeAllElements();
		for(Object velocityObject : getAvailableVelocities(editor.getControllerType())){
			model.addElement(velocityObject);
		}
	}

	private Object[] getAvailableVelocities (ControllerType type) {
		if(type == ControllerType.Billboard || type == ControllerType.PointSprite) {
			return new String[]{	VEL_TYPE_ROTATIONAL_2D, VEL_TYPE_CENTRIPETAL, VEL_TYPE_TANGENTIAL,
				VEL_TYPE_POLAR, VEL_TYPE_BROWNIAN};
		}
		else if(type == ControllerType.ModelInstance|| type == ControllerType.ParticleController) {
			return new String[]{	VEL_TYPE_ROTATIONAL_3D, VEL_TYPE_CENTRIPETAL, VEL_TYPE_TANGENTIAL,
				VEL_TYPE_POLAR, VEL_TYPE_BROWNIAN, VEL_TYPE_FACE};
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
		DynamicsInfluencer influencer = (DynamicsInfluencer)controller.findInfluencer(DynamicsInfluencer.class);		
		influencer.velocities.clear();
		velocities.get(index).isActive = isChecked;
		for(VelocityWrapper wrapper : velocities){
			if(wrapper.isActive)
				influencer.velocities.add(wrapper.velocityValue);
		}
		//Restart the effect and reinit the controller
		editor.restart();
	}

	protected void velocitySelected () {
		//Show the velocity value panel
		int index = velocityTable.getSelectedRow();
		if(index == -1) return;
		
		DynamicsModifier velocityValue = velocities.get(index).velocityValue;
		EditorPanel velocityPanel = getVelocityPanel(velocityValue);
		
		//Show the selected velocity
		if(selectedVelocityPanel != null && selectedVelocityPanel != velocityPanel) 
			selectedVelocityPanel.setVisible(false);
		velocityPanel.setVisible(true);
		velocityPanel.showContent(true);
		selectedVelocityPanel = velocityPanel;
	}

	private EditorPanel getVelocityPanel (DynamicsModifier velocityValue) {
		EditorPanel panel = null;
		//Billboards
		if(velocityValue instanceof DynamicsModifier.Rotational2D ){
			strengthVelocityPanel.setValue((DynamicsModifier.Strength) velocityValue);
			strengthVelocityPanel.setName("Angular Velocity");
			strengthVelocityPanel.setDescription("The angular speed around the billboard facing direction, in degrees/sec .");
			panel = strengthVelocityPanel;
		}
		else if(	velocityValue instanceof CentripetalAcceleration){
			strengthVelocityPanel.setValue((DynamicsModifier.CentripetalAcceleration) velocityValue);
			strengthVelocityPanel.setName("Centripetal Acceleration");
			strengthVelocityPanel.setDescription("A directional acceleration, the direction is towards the origin (global), or towards the emitter position (local), in world units/sec2 .");
			panel = strengthVelocityPanel;
		}
		else if(	velocityValue instanceof TangentialAcceleration){
			angularVelocityPanel.setValue((DynamicsModifier.Angular) velocityValue);
			angularVelocityPanel.setName("Tangetial Velocity");
			angularVelocityPanel.setDescription("A directional acceleration (axis and magnitude), the final direction is the cross product between particle position and the axis, in world units/sec2 .");
			panel = angularVelocityPanel;
		}
		else if(	velocityValue instanceof PolarAcceleration){
			angularVelocityPanel.setValue((DynamicsModifier.Angular) velocityValue);
			angularVelocityPanel.setName("Polar Velocity");
			angularVelocityPanel.setDescription("A directional acceleration (axis and magnitude), in world units/sec2 .");
			panel = angularVelocityPanel;
		}
		else if(	velocityValue instanceof BrownianAcceleration){
			strengthVelocityPanel.setValue((DynamicsModifier.Strength) velocityValue);
			strengthVelocityPanel.setName("Brownian Velocity");
			strengthVelocityPanel.setDescription("A directional acceleration which has random direction at each update, in world units/sec2.");
			panel = strengthVelocityPanel;
		}
		else if(velocityValue instanceof Rotational3D ){
			angularVelocityPanel.setValue((DynamicsModifier.Angular) velocityValue);
			angularVelocityPanel.setName("Angular Velocity");
			angularVelocityPanel.setDescription("An angular velocity (axis and magnitude), in degree/sec2.");
			panel = angularVelocityPanel;
		}
		else if(	velocityValue instanceof FaceDirection){ 
			emptyPanel.setName("Face");
			emptyPanel.setDescription("Rotates the model to face its current velocity (Do not add any other angular velocity when using this).");
			panel = emptyPanel;
		}

		return panel;
	}
	
	private DynamicsModifier createVelocityValue (Object selectedItem) {
		DynamicsModifier velocityValue = null;
		if(selectedItem == VEL_TYPE_ROTATIONAL_2D) velocityValue = new DynamicsModifier.Rotational2D();
		else if(selectedItem == VEL_TYPE_ROTATIONAL_3D) velocityValue = new DynamicsModifier.Rotational3D();
		else if(selectedItem == VEL_TYPE_CENTRIPETAL) velocityValue = new DynamicsModifier.CentripetalAcceleration();
		else if(selectedItem == VEL_TYPE_TANGENTIAL) velocityValue = new DynamicsModifier.TangentialAcceleration();
		else if(selectedItem == VEL_TYPE_POLAR) velocityValue = new DynamicsModifier.PolarAcceleration();
		else if(selectedItem == VEL_TYPE_BROWNIAN) velocityValue = new DynamicsModifier.BrownianAcceleration();
		else if(selectedItem == VEL_TYPE_FACE) velocityValue = new DynamicsModifier.FaceDirection();
		return velocityValue;
	}
	

	protected void deleteVelocity () {
		int row = velocityTable.getSelectedRow();
		if (row == -1) return;
		
		//Remove the velocity from the table
		ParticleController controller = editor.getEmitter();
		DynamicsInfluencer influencer = (DynamicsInfluencer)controller.findInfluencer(DynamicsInfluencer.class);
		influencer.velocities.removeValue(velocities.removeIndex(row).velocityValue, true);
		velocityTableModel.removeRow(row);
		
		//Restart the effect and reinit the controller
		editor.restart();

		selectedVelocityPanel.setVisible(false);
		selectedVelocityPanel = null;
	}

	protected void createVelocity (Object selectedItem) {
		//Add the velocity to the table and to the influencer
		ParticleController controller = editor.getEmitter();
		DynamicsInfluencer influencer = (DynamicsInfluencer)controller.findInfluencer(DynamicsInfluencer.class);
		VelocityWrapper wrapper = new VelocityWrapper(createVelocityValue(selectedItem), true);
		velocities.add(wrapper);
		influencer.velocities.add(wrapper.velocityValue);
		int index = velocities.size-1;
		velocityTableModel.addRow(new Object[] {"Velocity "+index, true});
		
		//Reinit
		editor.restart();
		
		//Select new velocity
		velocityTable.getSelectionModel().setSelectionInterval(index, index);
		revalidate();
		repaint();
	}
	
}
