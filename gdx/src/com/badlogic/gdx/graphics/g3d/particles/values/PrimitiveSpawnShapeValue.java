package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.math.Vector3;

public abstract class PrimitiveSpawnShapeValue extends SpawnShapeValue {
	protected static final Vector3 TMP_V1 = new Vector3();
	static public enum SpawnSide {
		both, top, bottom
	}
	public ScaledNumericValue spawnWidthValue,
							 spawnHeightValue,
							 spawnDepthValue;
	protected float spawnWidth, spawnWidthDiff;
	protected float spawnHeight, spawnHeightDiff;
	protected float spawnDepth, spawnDepthDiff;
	boolean edges = false;
	
	public PrimitiveSpawnShapeValue(){
		spawnWidthValue = new ScaledNumericValue();
		spawnHeightValue = new ScaledNumericValue();
		spawnDepthValue = new ScaledNumericValue();
	}
	
	public PrimitiveSpawnShapeValue(PrimitiveSpawnShapeValue value){
		super(value);
		spawnWidthValue = new ScaledNumericValue();
		spawnHeightValue = new ScaledNumericValue();
		spawnDepthValue = new ScaledNumericValue();
	}
	
	@Override
	public void setActive (boolean active) {
		super.setActive(active);
		spawnWidthValue.setActive(true);
		spawnHeightValue.setActive(true);
		spawnDepthValue.setActive(true);
	}
	
	public boolean isEdges () {
		return edges;
	}

	public void setEdges (boolean edges) {
		this.edges = edges;
	}
	
	public ScaledNumericValue getSpawnWidth () {
		return spawnWidthValue;
	}

	public ScaledNumericValue getSpawnHeight () {
		return spawnHeightValue;
	}
	
	public ScaledNumericValue getSpawnDepth () 
	{
		return spawnDepthValue;
	}
	
	@Override
	public void start () {
		super.start();
		spawnWidth = spawnWidthValue.newLowValue();
		spawnWidthDiff = spawnWidthValue.newHighValue();
		if (!spawnWidthValue.isRelative()) spawnWidthDiff -= spawnWidth;

		spawnHeight = spawnHeightValue.newLowValue();
		spawnHeightDiff = spawnHeightValue.newHighValue();
		if (!spawnHeightValue.isRelative()) spawnHeightDiff -= spawnHeight;
		
		spawnDepth = spawnDepthValue.newLowValue();
		spawnDepthDiff = spawnDepthValue.newHighValue();
		if (!spawnDepthValue.isRelative()) spawnDepthDiff -= spawnDepth;
	}
	
	@Override
	public void load (ParticleValue value) {
		super.load(value);
		PrimitiveSpawnShapeValue shape = (PrimitiveSpawnShapeValue) value;
		edges = shape.edges;
		spawnWidthValue.load(shape.spawnWidthValue);
		spawnHeightValue.load(shape.spawnHeightValue);
		spawnDepthValue.load(shape.spawnDepthValue);
	}
}
