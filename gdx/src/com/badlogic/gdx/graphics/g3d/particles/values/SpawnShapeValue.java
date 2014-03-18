package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.math.Vector3;

public abstract class SpawnShapeValue extends ParticleValue {
	
	public RangedNumericValue xOffsetValue, yOffsetValue, zOffsetValue;
	
	public SpawnShapeValue(){
		xOffsetValue = new RangedNumericValue();
		yOffsetValue = new RangedNumericValue();
		zOffsetValue = new RangedNumericValue();
	}
	
	public SpawnShapeValue(SpawnShapeValue spawnShapeValue){
		this();
	}
	
	public abstract void spawnAux(Vector3 vector, float percent);
	
	public final Vector3 spawn(Vector3 vector, float percent){
		spawnAux(vector, percent);
		if (xOffsetValue.active) vector.x += xOffsetValue.newLowValue();
		if (yOffsetValue.active) vector.y += yOffsetValue.newLowValue();
		if (zOffsetValue.active) vector.z += zOffsetValue.newLowValue();
		return vector;
	}
	
	public void start(){}
	
	@Override
	public void load (ParticleValue value) {
		super.load(value);
		SpawnShapeValue shape = (SpawnShapeValue) value;
		xOffsetValue.load(shape.xOffsetValue);
		yOffsetValue.load(shape.yOffsetValue);
		zOffsetValue.load(shape.zOffsetValue);
	}
	
	public abstract SpawnShapeValue copy ();
}