package com.badlogic.gdx.tools.texturepacker;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.SpinnerUI;

public class PotSpinner extends JSpinner {
	private Object valueBefore;

	public PotSpinner () {
		super();
	}

	public PotSpinner (SpinnerModel model) {
		super(model);
	}

	public Object getValueBefore () {
		return valueBefore;
	}

	@Override
	public void setValue (Object value) {
		valueBefore = getValue();
//		System.out.println(valueBefore + " -> " + value);
		super.setValue(value);
	}
}
