package com.badlogic.gdx.graphics.g3d.particles.values;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Random;

import com.badlogic.gdx.graphics.g3d.particles.Utils;
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
		load(spawnShapeValue);
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
	public void save (Writer output) throws IOException {
		super.save(output);
		/*
		if(!active) return;
		output.write("edges: " + edges + "\n");
		output.write("- Spawn Width - \n");
		spawnWidthValue.save(output);
		output.write("- Spawn Height - \n");
		spawnHeightValue.save(output);
		output.write("- Spawn Depth - \n");
		spawnDepthValue.save(output);
		*/
	}
	
	@Override
	public void load (BufferedReader reader) throws IOException {
		super.load(reader);
		if (!active) return;
		/*
		edges = Utils.readBoolean(reader, "edges");
		reader.readLine();
		spawnWidthValue.load(reader);
		reader.readLine();
		spawnHeightValue.load(reader);
		reader.readLine();
		spawnDepthValue.load(reader);
		*/
	}
	
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