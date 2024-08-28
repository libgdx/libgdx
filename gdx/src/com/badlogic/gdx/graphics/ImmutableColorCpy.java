
package com.badlogic.gdx.graphics;

/** A color class that prevents mutation to r/g/b/a values. */
public class ImmutableColorCpy extends Color {
	public ImmutableColorCpy() {
	}

	public ImmutableColorCpy(int rgba8888) {
		Color temp = new Color(rgba8888);
		this.r = temp.r;
		this.g = temp.g;
		this.b = temp.b;
		this.a = temp.a;
	}

	public ImmutableColorCpy(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		super.clamp();
	}

	@Override
	public Color setR (float r) {
		return cpy().setR(r);
	}

	@Override
	public Color setG (float g) {
		return cpy().setG(g);
	}

	@Override
	public Color setB (float b) {
		return cpy().setB(b);
	}

	@Override
	public Color setA (float a) {
		return cpy().setA(a);
	}

	@Override
	public Color set (Color color) {
		return cpy().set(color);
	}

	@Override
	public Color set (Color rgb, float alpha) {
		return cpy().set(rgb, alpha);
	}

	@Override
	public Color mul (Color color) {
		return cpy().mul(color);
	}

	@Override
	public Color mul (float value) {
		return cpy().mul(value);
	}

	@Override
	public Color add (Color color) {
		return cpy().add(color);
	}

	@Override
	public Color sub (Color color) {
		return cpy().sub(color);
	}

	@Override
	public Color clamp () {
		return cpy().clamp();
	}

	@Override
	public Color set (float r, float g, float b, float a) {
		return cpy().set(r, g, b, a);
	}

	@Override
	public Color set (int rgba) {
		return cpy().set(rgba);
	}

	@Override
	public Color add (float r, float g, float b, float a) {
		return cpy().add(r, g, b, a);
	}

	@Override
	public Color sub (float r, float g, float b, float a) {
		return cpy().sub(r, g, b, a);
	}

	@Override
	public Color mul (float r, float g, float b, float a) {
		return cpy().mul(r, g, b, a);
	}

	@Override
	public Color lerp (final Color target, final float t) {
		return cpy().lerp(target, t);
	}

	@Override
	public Color lerp (final float r, final float g, final float b, final float a, final float t) {
		return cpy().lerp(r, g, b, a, t);
	}

	@Override
	public Color premultiplyAlpha () {
		return cpy().premultiplyAlpha();
	}

	@Override
	public Color fromHsv (float h, float s, float v) {
		return cpy().fromHsv(h, s, v);
	}
}
