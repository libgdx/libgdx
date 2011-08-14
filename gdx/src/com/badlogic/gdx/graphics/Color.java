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
	public static final Color WHITE = new Color(1, 1, 1, 1);
	public static final Color BLACK = new Color(0, 0, 0, 1);
	public static final Color RED = new Color(1, 0, 0, 1);
	public static final Color GREEN = new Color(0, 1, 0, 1);
	public static final Color BLUE = new Color(0, 0, 1, 1);

	/** the red, green, blue and alpha components **/
	public float r, g, b, a;

	/** Constructs a new Color with all components set to 0. */
	public Color () {

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
		clamp();
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
		clamp();
		return this;
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
		clamp();
		return this;
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
		clamp();
		return this;
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
		clamp();
		return this;
	}

	public void clamp () {
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
	}

	public void set (float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	@Override
	public boolean equals (Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Color color = (Color)o;

		if (Float.compare(color.a, a) != 0) return false;
		if (Float.compare(color.b, b) != 0) return false;
		if (Float.compare(color.g, g) != 0) return false;
		if (Float.compare(color.r, r) != 0) return false;

		return true;
	}

	@Override
	public int hashCode () {
		int result = (r != +0.0f ? NumberUtils.floatToIntBits(r) : 0);
		result = 31 * result + (g != +0.0f ? NumberUtils.floatToIntBits(g) : 0);
		result = 31 * result + (b != +0.0f ? NumberUtils.floatToIntBits(b) : 0);
		result = 31 * result + (a != +0.0f ? NumberUtils.floatToIntBits(a) : 0);
		return result;
	}

	public String toString () {
		return Integer.toHexString(toIntBits());
	}

	/** Packs the four color components which should be in the range 0-255 into a 32-bit integer and then converts it to a float.
	 * Note that no range checking is performed for higher performance.
	 * 
	 * @param r the red component, 0 - 255
	 * @param g the green component, 0 - 255
	 * @param b the blue component, 0 - 255
	 * @param a the alpha component, 0 - 255
	 * @return the packed color as a float */
	public static float toFloatBits (int r, int g, int b, int a) {
		int color = (a << 24) | (b << 16) | (g << 8) | r;
		float floatColor = NumberUtils.intBitsToFloat(color & 0xfeffffff);
		return floatColor;
	}

	/** Packs the four color components which should be in the range 0-255 into a 32-bit. Note that no range checking is performed
	 * for higher performance.
	 * 
	 * @param r the red component, 0 - 255
	 * @param g the green component, 0 - 255
	 * @param b the blue component, 0 - 255
	 * @param a the alpha component, 0 - 255
	 * @return the packed color as a 32-bit int */
	public static int toIntBits (int r, int g, int b, int a) {
		return (a << 24) | (b << 16) | (g << 8) | r;
	}

	/** Packs the 4 components of this color into a 32-bit int and returns it as a float.
	 * 
	 * @return the packed color as a 32-bit float */
	public float toFloatBits () {
		int color = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		return NumberUtils.intBitsToFloat(color & 0xfeffffff);
	}

	/** Packs the 4 components of this color into a 32-bit int.
	 * 
	 * @return the packed color as a 32-bit int. */
	public int toIntBits () {
		int color = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		return color;
	}

	/** Packs the 4 components of this color into a 32-bit int and returns it as a float.
	 * 
	 * @return the packed color as a 32-bit float */
	public static float toFloatBits (float r, float g, float b, float a) {
		int color = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		return NumberUtils.intBitsToFloat(color & 0xfeffffff);
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
}
