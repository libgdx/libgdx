
package com.badlogic.gdx.math;

public enum Interpolation {
	linear, fade;

	public float apply (float alpha) {
		switch (this) {
		case linear:
			return alpha;
		case fade:
			return alpha * alpha * alpha * (alpha * (alpha * 6 - 15) + 10);
		}
		return 0; // Can't happen.
	}
}
