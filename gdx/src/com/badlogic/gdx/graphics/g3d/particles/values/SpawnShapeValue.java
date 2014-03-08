package com.badlogic.gdx.graphics.g3d.particles.values;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import com.badlogic.gdx.graphics.g3d.particles.Utils;
import com.badlogic.gdx.math.Vector3;

public abstract class SpawnShapeValue extends ParticleValue {
	public abstract Vector3 spawn(Vector3 vector, float percent);
	public void start(){}
	public abstract SpawnShapeValue copy ();
}