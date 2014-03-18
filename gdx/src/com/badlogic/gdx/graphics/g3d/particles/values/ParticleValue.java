package com.badlogic.gdx.graphics.g3d.particles.values;


public abstract class ParticleValue {
	public boolean active;

	public ParticleValue(){
		
	}
	
	public ParticleValue (ParticleValue value) {
		this.active =value.active;
	}
	
	public boolean isActive () 
	{
		return active;
	}

	public void setActive (boolean active) {
		this.active = active;
	}

	public void load (ParticleValue value) 
	{
		active = value.active;
	}
}