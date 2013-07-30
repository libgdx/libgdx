package com.badlogic.gdx.math;

/**
 * A constant Vector3 that throws a RuntimeException if a mutator is called.
 * @author Natman64
 *
 */
public class ConstVector3 extends Vector3 {

	private static final String ERROR_MESSAGE = "Tried to modify a ConstVector3.";
	
	/** Constructs a vector at (0,0,0) */
	public ConstVector3 () {
		super();
	}

	/** Creates a vector with the given components
	 * @param x The x-component
	 * @param y The y-component
	 * @param z The z-component */
	public ConstVector3 (float x, float y, float z) {
		super(x, y, z);
	}

	/** Creates a vector from the given array. The array must have at least 3 elements.
	 * @param values The array */
	public ConstVector3 (float[] values) {
		super(values);
	}

	/** Creates a vector from the given vector
	 * @param vector The vector */
	public ConstVector3 (Vector3 vector) {
		super(vector);
	}

	@Override
	public Vector3 set (float x, float y, float z) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 set (Vector3 vector) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 set (float[] values) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 add (Vector3 vector) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 add (float x, float y, float z) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 add (float values) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 sub (Vector3 a_vec) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 sub (float x, float y, float z) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 sub (float value) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 scl (float value) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 mul (float value) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 scl (Vector3 other) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 mul (Vector3 other) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 scl (float vx, float vy, float vz) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 mul (float vx, float vy, float vz) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 scale (float scalarX, float scalarY, float scalarZ) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 div (float value) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 div (float vx, float vy, float vz) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 div (Vector3 other) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 nor () {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 crs (Vector3 vector) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 crs (float x, float y, float z) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 mul (Matrix4 matrix) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 mul (Quaternion quat) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 prj (Matrix4 matrix) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 rot (Matrix4 matrix) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 rotate (float angle, float axisX, float axisY, float axisZ) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 rotate (Vector3 axis, float angle) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 lerp (Vector3 target, float alpha) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 slerp (Vector3 target, float alpha) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 limit (float limit) {
		throw new RuntimeException("ERROR_MESSAGE");
	}

	@Override
	public Vector3 clamp (float min, float max) {
		throw new RuntimeException("ERROR_MESSAGE");
	}
	
}
