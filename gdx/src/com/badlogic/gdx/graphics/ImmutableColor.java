
package com.badlogic.gdx.graphics;

/** A color class that prevents mutation to r/g/b/a values. */
public class ImmutableColor extends Color {
	public ImmutableColor () {
	}

	public ImmutableColor (int rgba8888) {
		Color temp = new Color(rgba8888);
		this.r = temp.r;
		this.g = temp.g;
		this.b = temp.b;
		this.a = temp.a;
	}

	public ImmutableColor (float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		super.clamp();
	}

	@Override
	public Color setR (float r) {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color setG (float g) {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color setB (float b) {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color setA (float a) {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color set (Color color) {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color set (Color rgb, float alpha) {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color mul (Color color) {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color mul (float value) {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color add (Color color) {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color sub (Color color) {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color clamp () {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color set (float r, float g, float b, float a) {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color set (int rgba) {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color add (float r, float g, float b, float a) {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color sub (float r, float g, float b, float a) {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color mul (float r, float g, float b, float a) {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color lerp (final Color target, final float t) {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color lerp (final float r, final float g, final float b, final float a, final float t) {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color premultiplyAlpha () {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}

	@Override
	public Color fromHsv (float h, float s, float v) {
		throw new UnsupportedOperationException("Attempt to modify ImmutableColor");
	}
}
