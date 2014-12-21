/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics;

import com.badlogic.gdx.utils.NumberUtils;

/** A color class, holding the r, g, b and alpha component as floats in the range [0,1]. All methods perform clamping on the
 * internal values after execution.
 * 
 * @author mzechner */
public class Color {
	public static final Color CLEAR = new Color(0, 0, 0, 0);
	public static final Color WHITE = new Color(1, 1, 1, 1);
	public static final Color BLACK = new Color(0, 0, 0, 1);
	public static final Color RED = new Color(1, 0, 0, 1);
	public static final Color GREEN = new Color(0, 1, 0, 1);
	public static final Color BLUE = new Color(0, 0, 1, 1);
	public static final Color LIGHT_GRAY = new Color(0.75f, 0.75f, 0.75f, 1);
	public static final Color GRAY = new Color(0.5f, 0.5f, 0.5f, 1);
	public static final Color DARK_GRAY = new Color(0.25f, 0.25f, 0.25f, 1);
	public static final Color PINK = new Color(1, 0.68f, 0.68f, 1);
	public static final Color ORANGE = new Color(1, 0.78f, 0, 1);
	public static final Color YELLOW = new Color(1, 1, 0, 1);
	public static final Color MAGENTA = new Color(1, 0, 1, 1);
	public static final Color CYAN = new Color(0, 1, 1, 1);
	public static final Color OLIVE = new Color(0.5f, 0.5f, 0, 1);
	public static final Color PURPLE = new Color(0.5f, 0, 0.5f, 1);
	public static final Color MAROON = new Color(0.5f, 0, 0, 1);
	public static final Color TEAL = new Color(0, 0.5f, 0.5f, 1);
	public static final Color NAVY = new Color(0, 0, 0.5f, 1);

	@Deprecated public static Color tmp = new Color();

	/** the red, green, blue and alpha components **/
	public float r, g, b, a;

	/** Constructs a new Color with all components set to 0. */
	public Color () {
	}

	/** @see #rgba8888ToColor(Color, int) */
	public Color (int rgba8888) {
		rgba8888ToColor(this, rgba8888);
	}

	/** Constructor, sets the components of the color
	 * 
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 * @param a the alpha component */
	public Color (float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		clamp();
	}

	/** Constructs a new color using the given color
	 * 
	 * @param color the color */
	public Color (Color color) {
		set(color);
	}

	/** Sets this color to the given color.
	 * 
	 * @param color the Color */
	public Color set (Color color) {
		this.r = color.r;
		this.g = color.g;
		this.b = color.b;
		this.a = color.a;
		return this;
	}

	/** Multiplies the this color and the given color
	 * 
	 * @param color the color
	 * @return this color. */
	public Color mul (Color color) {
		this.r *= color.r;
		this.g *= color.g;
		this.b *= color.b;
		this.a *= color.a;
		return clamp();
	}

	/** Multiplies all components of this Color with the given value.
	 * 
	 * @param value the value
	 * @return this color */
	public Color mul (float value) {
		this.r *= value;
		this.g *= value;
		this.b *= value;
		this.a *= value;
		return clamp();
	}

	/** Adds the given color to this color.
	 * 
	 * @param color the color
	 * @return this color */
	public Color add (Color color) {
		this.r += color.r;
		this.g += color.g;
		this.b += color.b;
		this.a += color.a;
		return clamp();
	}

	/** Subtracts the given color from this color
	 * 
	 * @param color the color
	 * @return this color */
	public Color sub (Color color) {
		this.r -= color.r;
		this.g -= color.g;
		this.b -= color.b;
		this.a -= color.a;
		return clamp();
	}

	/** Clamps this Color's components to a valid range [0 - 1]
	 * @return this Color for chaining */
	public Color clamp () {
		if (r < 0)
			r = 0;
		else if (r > 1) r = 1;

		if (g < 0)
			g = 0;
		else if (g > 1) g = 1;

		if (b < 0)
			b = 0;
		else if (b > 1) b = 1;

		if (a < 0)
			a = 0;
		else if (a > 1) a = 1;
		return this;
	}

	/** Sets this Color's component values.
	 * 
	 * @param r Red component
	 * @param g Green component
	 * @param b Blue component
	 * @param a Alpha component
	 * 
	 * @return this Color for chaining */
	public Color set (float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		return clamp();
	}

	/** Sets this color's component values through an integer representation.
	 * 
	 * @return this Color for chaining
	 * @see #rgba8888ToColor(Color, int) */
	public Color set (int rgba) {
		rgba8888ToColor(this, rgba);
		return this;
	}

	/** Adds the given color component values to this Color's values.
	 * 
	 * @param r Red component
	 * @param g Green component
	 * @param b Blue component
	 * @param a Alpha component
	 * 
	 * @return this Color for chaining */
	public Color add (float r, float g, float b, float a) {
		this.r += r;
		this.g += g;
		this.b += b;
		this.a += a;
		return clamp();
	}

	/** Subtracts the given values from this Color's component values.
	 * 
	 * @param r Red component
	 * @param g Green component
	 * @param b Blue component
	 * @param a Alpha component
	 * 
	 * @return this Color for chaining */
	public Color sub (float r, float g, float b, float a) {
		this.r -= r;
		this.g -= g;
		this.b -= b;
		this.a -= a;
		return clamp();
	}

	/** Multiplies this Color's color components by the given ones.
	 * 
	 * @param r Red component
	 * @param g Green component
	 * @param b Blue component
	 * @param a Alpha component
	 * 
	 * @return this Color for chaining */
	public Color mul (float r, float g, float b, float a) {
		this.r *= r;
		this.g *= g;
		this.b *= b;
		this.a *= a;
		return clamp();
	}

	/** Linearly interpolates between this color and the target color by t which is in the range [0,1]. The result is stored in this
	 * color.
	 * @param target The target color
	 * @param t The interpolation coefficient
	 * @return This color for chaining. */
	public Color lerp (final Color target, final float t) {
		this.r += t * (target.r - this.r);
		this.g += t * (target.g - this.g);
		this.b += t * (target.b - this.b);
		this.a += t * (target.a - this.a);
		return clamp();
	}

	/** Linearly interpolates between this color and the target color by t which is in the range [0,1]. The result is stored in this
	 * color.
	 * @param r The red component of the target color
	 * @param g The green component of the target color
	 * @param b The blue component of the target color
	 * @param a The alpha component of the target color
	 * @param t The interpolation coefficient
	 * @return This color for chaining. */
	public Color lerp (final float r, final float g, final float b, final float a, final float t) {
		this.r += t * (r - this.r);
		this.g += t * (g - this.g);
		this.b += t * (b - this.b);
		this.a += t * (a - this.a);
		return clamp();
	}

	/** Multiplies the RGB values by the alpha. */
	public Color premultiplyAlpha () {
		r *= a;
		g *= a;
		b *= a;
		return this;
	}

	@Override
	public boolean equals (Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Color color = (Color)o;
		return toIntBits() == color.toIntBits();
	}

	@Override
	public int hashCode () {
		int result = (r != +0.0f ? NumberUtils.floatToIntBits(r) : 0);
		result = 31 * result + (g != +0.0f ? NumberUtils.floatToIntBits(g) : 0);
		result = 31 * result + (b != +0.0f ? NumberUtils.floatToIntBits(b) : 0);
		result = 31 * result + (a != +0.0f ? NumberUtils.floatToIntBits(a) : 0);
		return result;
	}

	/** Packs the color components into a 32-bit integer with the format ABGR and then converts it to a float.
	 * @return the packed color as a 32-bit float
	 * @see NumberUtils#intToFloatColor(int) */
	public float toFloatBits () {
		int color = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		return NumberUtils.intToFloatColor(color);
	}

	/** Packs the color components into a 32-bit integer with the format ABGR.
	 * @return the packed color as a 32-bit int. */
	public int toIntBits () {
		int color = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		return color;
	}

	/** Returns the color encoded as hex string with the format RRGGBBAA. */
	public String toString () {
		String value = Integer.toHexString(((int)(255 * r) << 24) | ((int)(255 * g) << 16) | ((int)(255 * b) << 8)
			| ((int)(255 * a)));
		while (value.length() < 8)
			value = "0" + value;
		return value;
	}

	/** Returns a new color from a hex string with the format RRGGBBAA.
	 * @see #toString() */
	public static Color valueOf (String hex) {
		int r = Integer.valueOf(hex.substring(0, 2), 16);
		int g = Integer.valueOf(hex.substring(2, 4), 16);
		int b = Integer.valueOf(hex.substring(4, 6), 16);
		int a = hex.length() != 8 ? 255 : Integer.valueOf(hex.substring(6, 8), 16);
		return new Color(r / 255f, g / 255f, b / 255f, a / 255f);
	}

	/** Packs the color components into a 32-bit integer with the format ABGR and then converts it to a float. Note that no range
	 * checking is performed for higher performance.
	 * @param r the red component, 0 - 255
	 * @param g the green component, 0 - 255
	 * @param b the blue component, 0 - 255
	 * @param a the alpha component, 0 - 255
	 * @return the packed color as a float
	 * @see NumberUtils#intToFloatColor(int) */
	public static float toFloatBits (int r, int g, int b, int a) {
		int color = (a << 24) | (b << 16) | (g << 8) | r;
		float floatColor = NumberUtils.intToFloatColor(color);
		return floatColor;
	}

	/** Packs the color components into a 32-bit integer with the format ABGR and then converts it to a float.
	 * @return the packed color as a 32-bit float
	 * @see NumberUtils#intToFloatColor(int) */
	public static float toFloatBits (float r, float g, float b, float a) {
		int color = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		return NumberUtils.intToFloatColor(color);
	}

	/** Packs the color components into a 32-bit integer with the format ABGR. Note that no range checking is performed for higher
	 * performance.
	 * @param r the red component, 0 - 255
	 * @param g the green component, 0 - 255
	 * @param b the blue component, 0 - 255
	 * @param a the alpha component, 0 - 255
	 * @return the packed color as a 32-bit int */
	public static int toIntBits (int r, int g, int b, int a) {
		return (a << 24) | (b << 16) | (g << 8) | r;
	}

	public static int alpha (float alpha) {
		return (int)(alpha * 255.0f);
	}

	public static int luminanceAlpha (float luminance, float alpha) {
		return ((int)(luminance * 255.0f) << 8) | (int)(alpha * 255);
	}

	public static int rgb565 (float r, float g, float b) {
		return ((int)(r * 31) << 11) | ((int)(g * 63) << 5) | (int)(b * 31);
	}

	public static int rgba4444 (float r, float g, float b, float a) {
		return ((int)(r * 15) << 12) | ((int)(g * 15) << 8) | ((int)(b * 15) << 4) | (int)(a * 15);
	}

	public static int rgb888 (float r, float g, float b) {
		return ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
	}

	public static int rgba8888 (float r, float g, float b, float a) {
		return ((int)(r * 255) << 24) | ((int)(g * 255) << 16) | ((int)(b * 255) << 8) | (int)(a * 255);
	}

	public static int argb8888 (float a, float r, float g, float b) {
		return ((int)(a * 255) << 24) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
	}

	public static int rgb565 (Color color) {
		return ((int)(color.r * 31) << 11) | ((int)(color.g * 63) << 5) | (int)(color.b * 31);
	}

	public static int rgba4444 (Color color) {
		return ((int)(color.r * 15) << 12) | ((int)(color.g * 15) << 8) | ((int)(color.b * 15) << 4) | (int)(color.a * 15);
	}

	public static int rgb888 (Color color) {
		return ((int)(color.r * 255) << 16) | ((int)(color.g * 255) << 8) | (int)(color.b * 255);
	}

	public static int rgba8888 (Color color) {
		return ((int)(color.r * 255) << 24) | ((int)(color.g * 255) << 16) | ((int)(color.b * 255) << 8) | (int)(color.a * 255);
	}

	public static int argb8888 (Color color) {
		return ((int)(color.a * 255) << 24) | ((int)(color.r * 255) << 16) | ((int)(color.g * 255) << 8) | (int)(color.b * 255);
	}

	/** Sets the Color components using the specified integer value in the format RGB565. This is inverse to the rgb565(r, g, b)
	 * method.
	 * 
	 * @param color The Color to be modified.
	 * @param value An integer color value in RGB565 format. */
	public static void rgb565ToColor (Color color, int value) {
		color.r = ((value & 0x0000F800) >>> 11) / 31f;
		color.g = ((value & 0x000007E0) >>> 5) / 63f;
		color.b = ((value & 0x0000001F) >>> 0) / 31f;
	}

	/** Sets the Color components using the specified integer value in the format RGBA4444. This is inverse to the rgba4444(r, g, b,
	 * a) method.
	 * 
	 * @param color The Color to be modified.
	 * @param value An integer color value in RGBA4444 format. */
	public static void rgba4444ToColor (Color color, int value) {
		color.r = ((value & 0x0000f000) >>> 12) / 15f;
		color.g = ((value & 0x00000f00) >>> 8) / 15f;
		color.b = ((value & 0x000000f0) >>> 4) / 15f;
		color.a = ((value & 0x0000000f)) / 15f;
	}

	/** Sets the Color components using the specified integer value in the format RGB888. This is inverse to the rgb888(r, g, b)
	 * method.
	 * 
	 * @param color The Color to be modified.
	 * @param value An integer color value in RGB888 format. */
	public static void rgb888ToColor (Color color, int value) {
		color.r = ((value & 0x00ff0000) >>> 16) / 255f;
		color.g = ((value & 0x0000ff00) >>> 8) / 255f;
		color.b = ((value & 0x000000ff)) / 255f;
	}

	/** Sets the Color components using the specified integer value in the format RGBA8888. This is inverse to the rgba8888(r, g, b,
	 * a) method.
	 * 
	 * @param color The Color to be modified.
	 * @param value An integer color value in RGBA8888 format. */
	public static void rgba8888ToColor (Color color, int value) {
		color.r = ((value & 0xff000000) >>> 24) / 255f;
		color.g = ((value & 0x00ff0000) >>> 16) / 255f;
		color.b = ((value & 0x0000ff00) >>> 8) / 255f;
		color.a = ((value & 0x000000ff)) / 255f;
	}

	/** Sets the Color components using the specified integer value in the format ARGB8888. This is the inverse to the argb8888(a, r,
     * g, b) method
	 *
	 * @param color The Color to be modified.
	 * @param value An integer color value in ARGB8888 format. */
	public static void argb8888ToColor(Color color, int value) {
		color.a = ((value & 0xff000000) >>> 24) / 255f;
		color.r = ((value & 0x00ff0000) >>> 16) / 255f;
		color.g = ((value & 0x0000ff00) >>> 8) / 255f;
		color.b = ((value & 0x000000ff)) / 255f;
	}

	/** Returns a temporary copy of this color. This is not thread safe, do not save a reference to this instance.
	 * 
	 * @return a temporary copy of this color */
	public Color tmp () {
		return tmp.set(this);
	}

	/** @return a copy of this color */
	public Color cpy () {
		return new Color(this);
	}
}
