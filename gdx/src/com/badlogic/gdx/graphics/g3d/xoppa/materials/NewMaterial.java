package com.badlogic.gdx.graphics.g3d.xoppa.materials;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class NewMaterial {
	private static long currentID = 0;
	protected final static long newMask() { 
		return 1L << currentID++; 
	}
	
	public final static long Blending = newMask();
	public final static long DiffuseColor = newMask();
	public final static long SpecularColor = newMask();
	public final static long EmmisiveColor = newMask();
	public final static long DiffuseTexture = newMask();
	
	protected long mask;
	
	protected final void enable(final long mask) {
		this.mask |= mask; 
	}
	protected final void disable(final long mask) {
		this.mask &= -1 ^ mask;
	}
	protected final void toggle(final long mask, boolean enabled) {
		if (enabled) enable(mask);
		else disable(mask);
	}	
	
	public final long getMask() {
		return mask;
	}
	/** @return True if this material has the specified property */
	public final boolean has(final long mask){
		return (this.mask & mask) == mask;
	}
	
	// Blending
	protected int blendSourceFunction;
	protected int blendDestFunction;
	public void setBlending(boolean enabled, int sFunc, int dFunc) {
		toggle(Blending, enabled);
		blendSourceFunction = sFunc;
		blendDestFunction = dFunc;
	}
	/** Only applicable if this material {@link #has(long)} {@link #Blending} */
	public final int getBlendSourceFunction() {
		return blendSourceFunction;
	}
	/** Only applicable if this material {@link #has(long)} {@link #Blending} */
	public final int getBlendDestFunction() {
		return blendDestFunction;
	}
	
	// DiffuseColor
	protected Color diffuseColor = null;
	public void setDiffuseColor(Color color) {
		if (color != null) {
			enable(DiffuseColor);
			if (diffuseColor == null)
				diffuseColor = new Color();
			diffuseColor.set(color);
		} else
			disable(DiffuseColor);
	}
	/** Only applicable if this material {@link #has(long)} {@link #DiffuseColor} */
	public final Color getDiffuseColor() {
		return diffuseColor;
	}
	
	// DiffuseTexture
	// TODO add wrap etc.
	protected Texture diffuseTexture = null;
	public void setDiffuseTexture(Texture texture) {
		toggle(DiffuseTexture, texture != null);
		diffuseTexture = texture;
	}
	/** Only applicable if this material {@link #has(long)} {@link #DiffuseTexture} */
	public final Texture getDiffuseTexture() {
		return diffuseTexture;
	}
	
	// Etc...
}
