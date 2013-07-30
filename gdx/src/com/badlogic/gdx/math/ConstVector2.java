package com.badlogic.gdx.math;

/**
 * A constant Vector2 that throws a RuntimeException if a mutator is called.
 * @author Natman64
 *
 */
public class ConstVector2 extends Vector2 {

	private static final String ERROR_MESSAGE = "Tried to modify a ConstVector2.";
	
	/** Constructs a new vector at (0,0) */
	public ConstVector2 () {
		super();
	}

	/** Constructs a vector with the given components
	 * @param x The x-component
	 * @param y The y-component */
	public ConstVector2 (float x, float y) {
		super(x, y);
	}

	/** Constructs a vector from the given vector
	 * @param v The vector */
	public ConstVector2 (Vector2 v) {
		super(v);
	}

	@Override
	public Vector2 set (Vector2 v) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 set (float x, float y) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 sub (Vector2 v) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 nor () {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 add (Vector2 v) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 add (float x, float y) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 scl (float scalar) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 mul (float scalar) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 scl (float x, float y) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 mul (float x, float y) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 scl (Vector2 v) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 mul (Vector2 v) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 div (float value) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 div (float vx, float vy) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 div (Vector2 other) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 limit (float limit) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 clamp (float min, float max) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 sub (float x, float y) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 mul (Matrix3 mat) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public void setAngle (float angle) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 rotate (float degrees) {
		throw new RuntimeException(ERROR_MESSAGE);
	}

	@Override
	public Vector2 lerp (Vector2 target, float alpha) {
		throw new RuntimeException(ERROR_MESSAGE);
	}
	
}