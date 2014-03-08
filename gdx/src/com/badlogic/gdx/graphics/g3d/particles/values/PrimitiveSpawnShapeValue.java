package com.badlogic.gdx.graphics.g3d.particles.values;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import com.badlogic.gdx.graphics.g3d.particles.Utils;
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
	public void save (Writer output) throws IOException {
		super.save(output);
		if(!active) return;
		output.write("edges: " + edges + "\n");
		output.write("- Spawn Width - \n");
		spawnWidthValue.save(output);
		output.write("- Spawn Height - \n");
		spawnHeightValue.save(output);
		output.write("- Spawn Depth - \n");
		spawnDepthValue.save(output);
	}
	
	@Override
	public void load (BufferedReader reader) throws IOException {
		super.load(reader);
		if (!active) return;
		edges = Utils.readBoolean(reader, "edges");
		reader.readLine();
		spawnWidthValue.load(reader);
		reader.readLine();
		spawnHeightValue.load(reader);
		reader.readLine();
		spawnDepthValue.load(reader);
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
