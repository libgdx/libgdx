package com.badlogic.gdx.tools.particleeditor3d;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.table.DefaultTableModel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ModelInfluencer;
import com.badlogic.gdx.tools.particleeditor3d.TemplatePickerPanel.Listener;
import com.badlogic.gdx.utils.Array;

/** @author Inferno */
public class ModelInfluencerPanel extends InfluencerPanel<ModelInfluencer> implements Listener<Model>, 
																	com.badlogic.gdx.tools.particleeditor3d.EventManager.Listener {

	TemplatePickerPanel<Model> pickerPanel;
	
	public ModelInfluencerPanel (ParticleEditor3D editor, ModelInfluencer influencer, boolean single, String name, String desc) {
		super(editor, influencer, name, desc, true, false);
		pickerPanel.setMultipleSelectionAllowed(!single);
		EventManager.get().attach(ParticleEditor3D.EVT_ASSET_RELOADED, this);
	}
	
	@Override
	public void setValue (ModelInfluencer value) {
		super.setValue(value);
		if(value == null) return;
		pickerPanel.setValue(value.models);
	}
	
	protected void initializeComponents () {
		super.initializeComponents();
		pickerPanel = new TemplatePickerPanel<Model>(editor, null, this, Model.class, new LoaderButton.ModelLoaderButton(editor));
		pickerPanel.setIsAlwayShown(true);
		contentPanel.add(pickerPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,
			0, 0, 6), 0, 0));
	}

	@Override
	public void onTemplateChecked (Model model, boolean isChecked) {
		editor.getEmitter().init();
		editor.effect.start();
	}

	@Override
	public void handle (int aEventType, Object aEventData) {
		if(aEventType == ParticleEditor3D.EVT_ASSET_RELOADED){
			Object[] data = (Object[])aEventData;
			if(data[0] instanceof Model){
				if(value.models.removeValue((Model)data[0], true)){
					value.models.add((Model)data[1]);
					pickerPanel.reloadTemplates();
					pickerPanel.setValue(value.models);
					editor.getEmitter().init();
					editor.effect.start();
				}
			}
		}
	}

}
