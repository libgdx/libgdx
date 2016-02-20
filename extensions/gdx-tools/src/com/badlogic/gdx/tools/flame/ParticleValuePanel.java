package com.badlogic.gdx.tools.flame;

import com.badlogic.gdx.graphics.g3d.particles.values.ParticleValue;

/** @author Inferno */
public class ParticleValuePanel<T extends ParticleValue> extends EditorPanel<T> {

	public ParticleValuePanel (FlameMain editor, String name, String description) {
		this(editor, name, description, true);
	}
	
	public ParticleValuePanel (FlameMain editor, String name, String description, boolean isAlwaysActive) {
		this(editor, name, description, isAlwaysActive, false);
	}
	
	public ParticleValuePanel (FlameMain editor, String name, String description, boolean isAlwaysActive, boolean isRemovable) {
		super(editor, name, description, isAlwaysActive, isRemovable);
	}


	public void setHasAdvanced (boolean hasAdvanced) {
		super.setHasAdvanced(hasAdvanced);
		advancedButton.setVisible(hasAdvanced && (value.isActive() || isAlwaysActive));
	}
	
	@Override
	public void setValue (T value) {
		super.setValue(value);
		if(value != null){
			activeButton.setSelected(value.isActive());
		}
	}
	
	@Override
	protected void activate () {
		super.activate();
		if (value != null) value.setActive(activeButton.isSelected());
	}
}
