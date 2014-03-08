package com.badlogic.gdx.graphics.g3d.particles.values;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import com.badlogic.gdx.graphics.g3d.particles.Utils;

public class VelocityValue extends ParticleValue{
	public enum VelocityType {
		centripetal, tangential, polar
	}
	public VelocityType type = VelocityType.polar;
	public ScaledNumericValue strength;
	public ScaledNumericValue theta;
	public ScaledNumericValue phi;
	public boolean isGlobal;

	public VelocityValue(){
		strength = new ScaledNumericValue();
		theta = new ScaledNumericValue();
		phi = new ScaledNumericValue();
		isGlobal = false;
	}
	

	public VelocityType getType(){
		return type;
	}

	public void setType(VelocityType aType){
		type = aType;
	}

	public ScaledNumericValue getStrength(){
		return strength;
	}

	public ScaledNumericValue getTheta(){
		return theta;
	}

	public ScaledNumericValue getPhi(){
		return phi;
	}

	public boolean isGlobal(){
		return isGlobal;
	}

	public void setGlobal(boolean isGlobal){
		this.isGlobal = isGlobal;
	}

	public void save (Writer output) throws IOException {
		super.save(output);
		if (!active) return;
		output.write("type: " + type+ "\n");
		output.write("global: " + isGlobal+ "\n");
		output.write("strength:\n");
		strength.save(output);
		output.write("theta:\n");
		theta.save(output);
		output.write("phi:\n");
		phi.save(output);
	}

	public void load (BufferedReader reader) throws IOException {
		super.load(reader);
		if (!active) return;
		type = VelocityType.valueOf(Utils.readString(reader, "type"));
		isGlobal = Utils.readBoolean(reader, "global");
		reader.readLine();
		strength.load(reader);
		reader.readLine();
		theta.load(reader);
		reader.readLine();
		phi.load(reader);
	}

	public void load (VelocityValue value) {
		super.load(value);
		type = value.type;
		strength.load(value.strength);
		theta.load(value.theta);
		phi.load(value.phi);
		isGlobal = value.isGlobal;
	}

}

