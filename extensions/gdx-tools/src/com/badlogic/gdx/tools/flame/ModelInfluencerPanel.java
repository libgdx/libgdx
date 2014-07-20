package com.badlogic.gdx.tools.flame;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ModelInfluencer;

/** @author Inferno */
public class ModelInfluencerPanel extends InfluencerPanel<ModelInfluencer> implements TemplatePickerPanel.Listener<Model>, EventManager.Listener {

	TemplatePickerPanel<Model> pickerPanel;
	
	public ModelInfluencerPanel (FlameMain editor, ModelInfluencer influencer, boolean single, String name, String desc) {
		super(editor, influencer, name, desc, true, false);
		pickerPanel.setMultipleSelectionAllowed(!single);
		EventManager.get().attach(FlameMain.EVT_ASSET_RELOADED, this);
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
		editor.restart();
	}

	@Override
	public void handle (int aEventType, Object aEventData) {
		if(aEventType == FlameMain.EVT_ASSET_RELOADED){
			Object[] data = (Object[])aEventData;
			if(data[0] instanceof Model){
				if(value.models.removeValue((Model)data[0], true)){
					value.models.add((Model)data[1]);
					pickerPanel.reloadTemplates();
					pickerPanel.setValue(value.models);
					editor.restart();
				}
			}
		}
	}

}
