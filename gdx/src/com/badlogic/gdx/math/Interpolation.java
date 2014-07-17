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

package com.badlogic.gdx.math;

import com.badlogic.gdx.utils.ObjectMap;

/** Takes a linear value in the range of 0-1 and outputs a (usually) non-linear, interpolated value.
 * @author Nathan Sweet */
public abstract class Interpolation {
	/** @param a Alpha value between 0 and 1. */
	abstract public float apply (float a);

	/** @param a Alpha value between 0 and 1. */
	public float apply (float start, float end, float a) {
		return start + (end - start) * apply(a);
	}

	static public final Interpolation linear = new Interpolation() {
		public float apply (float a) {
			return a;
		}
	};

	static public final Interpolation fade = new Interpolation() {
		public float apply (float a) {
			return MathUtils.clamp(a * a * a * (a * (a * 6 - 15) + 10), 0, 1);
		}
	};

	static public final Pow pow2 = new Pow(2);
	static public final PowIn pow2In = new PowIn(2);
	static public final PowOut pow2Out = new PowOut(2);

	static public final Pow pow3 = new Pow(3);
	static public final PowIn pow3In = new PowIn(3);
	static public final PowOut pow3Out = new PowOut(3);

	static public final Pow pow4 = new Pow(4);
	static public final PowIn pow4In = new PowIn(4);
	static public final PowOut pow4Out = new PowOut(4);

	static public final Pow pow5 = new Pow(5);
	static public final PowIn pow5In = new PowIn(5);
	static public final PowOut pow5Out = new PowOut(5);

	static public final Interpolation sine = new Interpolation() {
		public float apply (float a) {
			return (1 - MathUtils.cos(a * MathUtils.PI)) / 2;
		}
	};

	static public final Interpolation sineIn = new Interpolation() {
		public float apply (float a) {
			return 1 - MathUtils.cos(a * MathUtils.PI / 2);
		}
	};

	static public final Interpolation sineOut = new Interpolation() {
		public float apply (float a) {
			return MathUtils.sin(a * MathUtils.PI / 2);
		}
	};

	static public final Interpolation exp10 = new Exp(2, 10);
	static public final Interpolation exp10In = new ExpIn(2, 10);
	static public final Interpolation exp10Out = new ExpOut(2, 10);

	static public final Interpolation exp5 = new Exp(2, 5);
	static public final Interpolation exp5In = new ExpIn(2, 5);
	static public final Interpolation exp5Out = new ExpOut(2, 5);

	static public final Interpolation circle = new Interpolation() {
		public float apply (float a) {
			if (a <= 0.5f) {
				a *= 2;
				return (1 - (float)Math.sqrt(1 - a * a)) / 2;
			}
			a--;
			a *= 2;
			return ((float)Math.sqrt(1 - a * a) + 1) / 2;
		}
	};

	static public final Interpolation circleIn = new Interpolation() {
		public float apply (float a) {
			return 1 - (float)Math.sqrt(1 - a * a);
		}
	};

	static public final Interpolation circleOut = new Interpolation() {
		public float apply (float a) {
			a--;
			return (float)Math.sqrt(1 - a * a);
		}
	};

	static public final Elastic elastic = new Elastic(2, 10);
	static public final Elastic elasticIn = new ElasticIn(2, 10);
	static public final Elastic elasticOut = new ElasticOut(2, 10);

	static public final Interpolation swing = new Swing(1.5f);
	static public final Interpolation swingIn = new SwingIn(2f);
	static public final Interpolation swingOut = new SwingOut(2f);

	static public final Interpolation bounce = new Bounce(4);
	static public final Interpolation bounceIn = new BounceIn(4);
	static public final Interpolation bounceOut = new BounceOut(4);

	static private final ObjectMap<String, Interpolation> byName = new ObjectMap<String, Interpolation>();

	/** Clears all custom {@code Interpolation}s and resets the underlying map to the default values. */
	static public void reset () {
		byName.clear();
		byName.put("bounce", bounce);
		byName.put("bouncein", bounceIn);
		byName.put("bounce-in", bounceIn);
		byName.put("bounce_in", bounceIn);
		byName.put("bounce in", bounceIn);
		byName.put("bounceout", bounceOut);
		byName.put("bounce-out", bounceOut);
		byName.put("bounce_out", bounceOut);
		byName.put("bounce out", bounceOut);

		byName.put("circle", circle);
		byName.put("circlein", circleIn);
		byName.put("circle-in", circleIn);
		byName.put("circle_in", circleIn);
		byName.put("circle in", circleIn);
		byName.put("circleout", circleOut);
		byName.put("circle-out", circleOut);
		byName.put("circle_out", circleOut);
		byName.put("circle out", circleOut);

		byName.put("elastic", elastic);
		byName.put("elasticin", elasticIn);
		byName.put("elastic-in", elasticIn);
		byName.put("elastic_in", elasticIn);
		byName.put("elastic in", elasticIn);
		byName.put("elasticout", elasticOut);
		byName.put("elastic-out", elasticOut);
		byName.put("elastic_out", elasticOut);
		byName.put("elastic out", elasticOut);

		byName.put("exp10", exp10);
		byName.put("exp-10", exp10);
		byName.put("exp_10", exp10);
		byName.put("exp 10", exp10);
		byName.put("exp10in", exp10In);
		byName.put("exp-10-in", exp10In);
		byName.put("exp_10_in", exp10In);
		byName.put("exp 10 in", exp10In);
		byName.put("exp10-in", exp10In);
		byName.put("exp10_in", exp10In);
		byName.put("exp10 in", exp10In);
		byName.put("exp10out", exp10Out);
		byName.put("exp-10-out", exp10Out);
		byName.put("exp_10_out", exp10Out);
		byName.put("exp 10 out", exp10Out);
		byName.put("exp10-out", exp10Out);
		byName.put("exp10_out", exp10Out);
		byName.put("exp10 out", exp10Out);

		byName.put("exp5", exp5);
		byName.put("exp-5", exp5);
		byName.put("exp_5", exp5);
		byName.put("exp 5", exp5);
		byName.put("exp5in", exp5In);
		byName.put("exp-5-in", exp5In);
		byName.put("exp_5_in", exp5In);
		byName.put("exp 5 in", exp5In);
		byName.put("exp5-in", exp5In);
		byName.put("exp5_in", exp5In);
		byName.put("exp5 in", exp5In);
		byName.put("exp5out", exp5Out);
		byName.put("exp-5-out", exp5Out);
		byName.put("exp_5_out", exp5Out);
		byName.put("exp 5 out", exp5Out);
		byName.put("exp5-out", exp5Out);
		byName.put("exp5_out", exp5Out);
		byName.put("exp5 out", exp5Out);

		byName.put("fade", fade);

		byName.put("linear", linear);
		byName.put("line", linear);

		byName.put("quadratic", pow2);
		byName.put("quadratic-in", pow2In);
		byName.put("quadratic_in", pow2In);
		byName.put("quadratic in", pow2In);
		byName.put("quadratic-out", pow2Out);
		byName.put("quadratic_out", pow2Out);
		byName.put("quadratic out", pow2Out);
		byName.put("pow2", pow2);
		byName.put("pow-2", pow2);
		byName.put("pow_2", pow2);
		byName.put("pow 2", pow2);
		byName.put("pow2in", pow2In);
		byName.put("pow-2-in", pow2In);
		byName.put("pow_2_in", pow2In);
		byName.put("pow 2 in", pow2In);
		byName.put("pow2-in", pow2In);
		byName.put("pow2_in", pow2In);
		byName.put("pow2 in", pow2In);
		byName.put("pow2out", pow2Out);
		byName.put("pow-2-out", pow2Out);
		byName.put("pow_2_out", pow2Out);
		byName.put("pow 2 out", pow2Out);
		byName.put("pow2-out", pow2Out);
		byName.put("pow2_out", pow2Out);
		byName.put("pow2 out", pow2Out);

		byName.put("cubic", pow3);
		byName.put("cubic-in", pow3In);
		byName.put("cubic_in", pow3In);
		byName.put("cubic in", pow3In);
		byName.put("cubic-out", pow3Out);
		byName.put("cubic_out", pow3Out);
		byName.put("cubic out", pow3Out);
		byName.put("pow3", pow3);
		byName.put("pow-3", pow3);
		byName.put("pow_3", pow3);
		byName.put("pow 3", pow3);
		byName.put("pow3in", pow3In);
		byName.put("pow-3-in", pow3In);
		byName.put("pow_3_in", pow3In);
		byName.put("pow 3 in", pow3In);
		byName.put("pow3-in", pow3In);
		byName.put("pow3_in", pow3In);
		byName.put("pow3 in", pow3In);
		byName.put("pow3out", pow3Out);
		byName.put("pow-3-out", pow3Out);
		byName.put("pow_3_out", pow3Out);
		byName.put("pow 3 out", pow3Out);
		byName.put("pow3-out", pow3Out);
		byName.put("pow3_out", pow3Out);
		byName.put("pow3 out", pow3Out);

		byName.put("quartic", pow4);
		byName.put("quartic-in", pow4In);
		byName.put("quartic_in", pow4In);
		byName.put("quartic in", pow4In);
		byName.put("quartic-out", pow4Out);
		byName.put("quartic_out", pow4Out);
		byName.put("quartic out", pow4Out);
		byName.put("pow4", pow4);
		byName.put("pow-4", pow4);
		byName.put("pow_4", pow4);
		byName.put("pow 4", pow4);
		byName.put("pow4in", pow4In);
		byName.put("pow-4-in", pow4In);
		byName.put("pow_4_in", pow4In);
		byName.put("pow 4 in", pow4In);
		byName.put("pow4-in", pow4In);
		byName.put("pow4_in", pow4In);
		byName.put("pow4 in", pow4In);
		byName.put("pow4out", pow4Out);
		byName.put("pow-4-out", pow4Out);
		byName.put("pow_4_out", pow4Out);
		byName.put("pow 4 out", pow4Out);
		byName.put("pow4-out", pow4Out);
		byName.put("pow4_out", pow4Out);
		byName.put("pow4 out", pow4Out);

		byName.put("quintic", pow5);
		byName.put("quintic-in", pow5In);
		byName.put("quintic_in", pow5In);
		byName.put("quintic in", pow5In);
		byName.put("quintic-out", pow5Out);
		byName.put("quintic_out", pow5Out);
		byName.put("quintic out", pow5Out);
		byName.put("pow5", pow5);
		byName.put("pow-5", pow5);
		byName.put("pow_5", pow5);
		byName.put("pow 5", pow5);
		byName.put("pow5in", pow5In);
		byName.put("pow-5-in", pow5In);
		byName.put("pow_5_in", pow5In);
		byName.put("pow 5 in", pow5In);
		byName.put("pow5-in", pow5In);
		byName.put("pow5_in", pow5In);
		byName.put("pow5 in", pow5In);
		byName.put("pow5out", pow5Out);
		byName.put("pow-5-out", pow5Out);
		byName.put("pow_5_out", pow5Out);
		byName.put("pow 5 out", pow5Out);
		byName.put("pow5-out", pow5Out);
		byName.put("pow5_out", pow5Out);
		byName.put("pow5 out", pow5Out);

		byName.put("sine", sine);
		byName.put("sinusoidal", sine);
		byName.put("sinein", sineIn);
		byName.put("sine-in", sineIn);
		byName.put("sine_in", sineIn);
		byName.put("sine in", sineIn);
		byName.put("sineout", sineOut);
		byName.put("sine-out", sineOut);
		byName.put("sine_out", sineOut);
		byName.put("sine out", sineOut);

		byName.put("swing", swing);
		byName.put("swingin", swingIn);
		byName.put("swing-in", swingIn);
		byName.put("swing_in", swingIn);
		byName.put("swing in", swingIn);
		byName.put("swingout", swingOut);
		byName.put("swing-out", swingOut);
		byName.put("swing_out", swingOut);
		byName.put("swing out", swingOut);

		byName.shrink(byName.size);
	}

	static {
		reset();
	}

	/** Retrieves one of the default (or even custom, see {@link Interpolation#put}) {@code Interpolation} objects by name. This is
	 * mostly for easy specification in configuration files or similar.
	 * 
	 * The name is case-insensitive, and leading/trailing whitespace is stripped. You can obtain the {@code Interpolation}s via the
	 * following conventions, where {@code type} is the main type (e.g. {@link Interpolation#swing}) and {@code sub} is the optional
	 * sub-type (e.g. {@link Interpolation#swingIn}), usually either {@code in} or {@code out}.
	 * <ul>
	 * <li>{@code typesub}
	 * <li>{@code type-sub}
	 * <li>{@code type_sub}
	 * <li>{@code type sub}
	 * </ul>
	 * 
	 * Several synonyms are available, too. The subtype conventions apply.
	 * <ul>
	 * <li>{@code quadratic} refers to {@link Interpolation#pow2}
	 * <li>{@code cubic} refers to {@link Interpolation#pow3}
	 * <li>{@code quartic} refers to {@link Interpolation#pow4}
	 * <li>{@code quintic} refers to {@link Interpolation#pow5}
	 * </ul>
	 * 
	 * @param name The name of the {@code Interpolation} to retrieve. Case-insensitive, and leading and trailing whitespace is
	 *           stripped.
	 * @return The {@code Interpolation} if found, {@code null} if not or if {@code name} is {@code null}. */
	static public Interpolation get (final String name) {
		return (name == null) ? null : byName.get(name.toLowerCase().trim(), null);
	}

	/** @param name The name of the new {@code Interpolation} to add in.
	 * @param value The {@code Interpolation} itself.
	 * @return Whatever {@code Interpolation} was previously referred to by {@code name}, or {@code null} if there wasn't one. */
	static public Interpolation put (String name, Interpolation value) {
		return byName.put(name, value);
	}

	static public class Pow extends Interpolation {
		final int power;

		public Pow (int power) {
			this.power = power;
		}

		public float apply (float a) {
			if (a <= 0.5f) return (float)Math.pow(a * 2, power) / 2;
			return (float)Math.pow((a - 1) * 2, power) / (power % 2 == 0 ? -2 : 2) + 1;
		}
	}

	static public class PowIn extends Pow {
		public PowIn (int power) {
			super(power);
		}

		public float apply (float a) {
			return (float)Math.pow(a, power);
		}
	}

	static public class PowOut extends Pow {
		public PowOut (int power) {
			super(power);
		}

		public float apply (float a) {
			return (float)Math.pow(a - 1, power) * (power % 2 == 0 ? -1 : 1) + 1;
		}
	}

	//

	static public class Exp extends Interpolation {
		final float value, power, min, scale;

		public Exp (float value, float power) {
			this.value = value;
			this.power = power;
			min = (float)Math.pow(value, -power);
			scale = 1 / (1 - min);
		}

		public float apply (float a) {
			if (a <= 0.5f) return ((float)Math.pow(value, power * (a * 2 - 1)) - min) * scale / 2;
			return (2 - ((float)Math.pow(value, -power * (a * 2 - 1)) - min) * scale) / 2;
		}
	};

	static public class ExpIn extends Exp {
		public ExpIn (float value, float power) {
			super(value, power);
		}

		public float apply (float a) {
			return ((float)Math.pow(value, power * (a - 1)) - min) * scale;
		}
	}

	static public class ExpOut extends Exp {
		public ExpOut (float value, float power) {
			super(value, power);
		}

		public float apply (float a) {
			return 1 - ((float)Math.pow(value, -power * a) - min) * scale;
		}
	}

	//

	static public class Elastic extends Interpolation {
		final float value, power;

		public Elastic (float value, float power) {
			this.value = value;
			this.power = power;
		}

		public float apply (float a) {
			if (a <= 0.5f) {
				a *= 2;
				return (float)Math.pow(value, power * (a - 1)) * MathUtils.sin(a * 20) * 1.0955f / 2;
			}
			a = 1 - a;
			a *= 2;
			return 1 - (float)Math.pow(value, power * (a - 1)) * MathUtils.sin((a) * 20) * 1.0955f / 2;
		}
	}

	static public class ElasticIn extends Elastic {
		public ElasticIn (float value, float power) {
			super(value, power);
		}

		public float apply (float a) {
			return (float)Math.pow(value, power * (a - 1)) * MathUtils.sin(a * 20) * 1.0955f;
		}
	}

	static public class ElasticOut extends Elastic {
		public ElasticOut (float value, float power) {
			super(value, power);
		}

		public float apply (float a) {
			a = 1 - a;
			return (1 - (float)Math.pow(value, power * (a - 1)) * MathUtils.sin(a * 20) * 1.0955f);
		}
	}

	//

	static public class Bounce extends BounceOut {
		public Bounce (float[] widths, float[] heights) {
			super(widths, heights);
		}

		public Bounce (int bounces) {
			super(bounces);
		}

		private float out (float a) {
			float test = a + widths[0] / 2;
			if (test < widths[0]) return test / (widths[0] / 2) - 1;
			return super.apply(a);
		}

		public float apply (float a) {
			if (a <= 0.5f) return (1 - out(1 - a * 2)) / 2;
			return out(a * 2 - 1) / 2 + 0.5f;
		}
	}

	static public class BounceOut extends Interpolation {
		final float[] widths, heights;

		public BounceOut (float[] widths, float[] heights) {
			if (widths.length != heights.length)
				throw new IllegalArgumentException("Must be the same number of widths and heights.");
			this.widths = widths;
			this.heights = heights;
		}

		public BounceOut (int bounces) {
			if (bounces < 2 || bounces > 5) throw new IllegalArgumentException("bounces cannot be < 2 or > 5: " + bounces);
			widths = new float[bounces];
			heights = new float[bounces];
			heights[0] = 1;
			switch (bounces) {
			case 2:
				widths[0] = 0.6f;
				widths[1] = 0.4f;
				heights[1] = 0.33f;
				break;
			case 3:
				widths[0] = 0.4f;
				widths[1] = 0.4f;
				widths[2] = 0.2f;
				heights[1] = 0.33f;
				heights[2] = 0.1f;
				break;
			case 4:
				widths[0] = 0.34f;
				widths[1] = 0.34f;
				widths[2] = 0.2f;
				widths[3] = 0.15f;
				heights[1] = 0.26f;
				heights[2] = 0.11f;
				heights[3] = 0.03f;
				break;
			case 5:
				widths[0] = 0.3f;
				widths[1] = 0.3f;
				widths[2] = 0.2f;
				widths[3] = 0.1f;
				widths[4] = 0.1f;
				heights[1] = 0.45f;
				heights[2] = 0.3f;
				heights[3] = 0.15f;
				heights[4] = 0.06f;
				break;
			}
			widths[0] *= 2;
		}

		public float apply (float a) {
			a += widths[0] / 2;
			float width = 0, height = 0;
			for (int i = 0, n = widths.length; i < n; i++) {
				width = widths[i];
				if (a <= width) {
					height = heights[i];
					break;
				}
				a -= width;
			}
			a /= width;
			float z = 4 / width * height * a;
			return 1 - (z - z * a) * width;
		}
	}

	static public class BounceIn extends BounceOut {
		public BounceIn (float[] widths, float[] heights) {
			super(widths, heights);
		}

		public BounceIn (int bounces) {
			super(bounces);
		}

		public float apply (float a) {
			return 1 - super.apply(1 - a);
		}
	}

	//

	static public class Swing extends Interpolation {
		private final float scale;

		public Swing (float scale) {
			this.scale = scale * 2;
		}

		public float apply (float a) {
			if (a <= 0.5f) {
				a *= 2;
				return a * a * ((scale + 1) * a - scale) / 2;
			}
			a--;
			a *= 2;
			return a * a * ((scale + 1) * a + scale) / 2 + 1;
		}
	}

	static public class SwingOut extends Interpolation {
		private final float scale;

		public SwingOut (float scale) {
			this.scale = scale;
		}

		public float apply (float a) {
			a--;
			return a * a * ((scale + 1) * a + scale) + 1;
		}
	}

	static public class SwingIn extends Interpolation {
		private final float scale;

		public SwingIn (float scale) {
			this.scale = scale;
		}

		public float apply (float a) {
			return a * a * ((scale + 1) * a - scale);
		}
	}
}
