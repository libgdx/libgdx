package com.badlogic.gdx.math;

public enum Interpolation {
	bounce, other;

	public float apply (float alpha) {
		switch (this) {
		case bounce:
			return alpha * alpha;
		case other:
			return alpha * alpha * alpha;
		default:
			return 0; // Can't happen.
		}
	}
}
