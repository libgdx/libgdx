package com.badlogic.gdx.tools.particleeditor3d;


/** @author Inferno */
public class EmptyPanel extends EditorPanel {

	public EmptyPanel (ParticleEditor3D particleEditor3D, String name , String desc) {
		super(particleEditor3D, name, desc);
		setValue(null);
	}
}
