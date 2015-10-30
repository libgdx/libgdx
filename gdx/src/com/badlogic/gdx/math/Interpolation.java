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

/** Takes a linear value in the range of 0-1 and outputs a (usually) non-linear, interpolated value.
 * @author Nathan Sweet */
public abstract class Interpolation {
	/** @param a Alpha value between 0 and 1. */
	abstract public float apply (float a);

	/** @param a Alpha value between 0 and 1. */
	public float apply (float start, float end, float a) {
		return start + (end - start) * apply(a);
	}

	/** Speeds are expressed in terms of units of "total change" over "duration". To prepare inputs for this method, you may use:
	 * <p>
	 * {@code speed = worldSpeed * duration / (end - start)}
	 * @param startSpeed Beginning rate of change. Used only by {@linkplain SplineInterpolation SplineInterpolations}.
	 * @param endSpeed Ending rate of change. Used only by {@linkplain SplineInterpolation SplineInterpolations}.
	 * @param a Alpha value between 0 and 1, where 1 maps to the total duration.
	 * @return The current rate of change. To convert this to world speed, you may use:
	 *         <p>
	 *         {@code worldSpeed = speed * (end - start) / duration} */
	public abstract float speed (float startSpeed, float endSpeed, float a);

	static public final Interpolation linear = new Interpolation() {
		public float apply (float a) {
			return a;
		}

		public float speed (float startSpeed, float endSpeed, float a) {
			return 1f;
		}
	};

	/** A third-order Hermite spline interpolation. When left unspecified, the starting and ending speeds are zero, and the
	 * function is equivalent to the {@code smoothstep} function in GLSL. */
	static public final SplineInterpolation smooth = new SplineInterpolation() {
		public float apply (float a) {
			return MathUtils.clamp(a * a * (a * (-2) + 3), 0, 1);
		}

		public float applyWithSpeed (float startSpeed, float a) {
			return a * (a * (a * (startSpeed - 2) + 3 - 2 * startSpeed) + startSpeed);
		}

		public float applyWithSpeed (float startSpeed, float endSpeed, float a) {
			return a * (a * (a * (startSpeed + endSpeed - 2) + 3 - 2 * startSpeed - endSpeed) + startSpeed);
		}

		public float speed (float startSpeed, float endSpeed, float a) {
			if (startSpeed == 0) if (endSpeed == 0)
				return a * (a * (-6) + 6);
			else
				return a * (a * (3 * endSpeed - 6) + 6 - 2 * endSpeed);
			if (endSpeed == 0)
				return a * (a * (3 * startSpeed - 6) + 6 - 4 * startSpeed) + startSpeed;
			else
				return a * (a * (3 * (startSpeed + endSpeed) - 6) + 6 - 4 * startSpeed - 2 * endSpeed) + startSpeed;
		}
	};

	/** A fifth-order Hermite spline interpolation. When left unspecified, the starting and ending speeds are zero. The starting
	 * and ending accelerations are always zero. */
	static public final SplineInterpolation fade = new SplineInterpolation() {
		public float apply (float a) {
			return MathUtils.clamp(a * a * a * (a * (a * 6 - 15) + 10), 0, 1);
		}

		public float applyWithSpeed (float startSpeed, float a) {
			return a * (a * a * (a * (a * (6 - 3 * startSpeed) + 8 * startSpeed - 15) + 10 - 6 * startSpeed) + startSpeed);
		}

		public float applyWithSpeed (float startSpeed, float endSpeed, float a) {
			return a * (a * a * (a * (a * (6 - 3 * (startSpeed + endSpeed)) + 8 * startSpeed + 7 * endSpeed - 15) + 10
				- 6 * startSpeed - 4 * endSpeed) + startSpeed);
		}

		public float speed (float startSpeed, float endSpeed, float a) {
			if (startSpeed == 0) {
				if (endSpeed == 0) {
					return a * a * (a * (a * 30 - 60) + 30);
				} else {
					return a * a * (a * (a * (30 - 15 * endSpeed) + 28 * endSpeed - 60) + 30 - 12 * endSpeed);
				}
			}
			if (endSpeed == 0) {
				return a * a * (a * (a * (30 - 15 * startSpeed) + 32 * startSpeed - 60) + 30 - 18 * startSpeed) + startSpeed;
			} else {
				return a * a * (a * (a * (30 - 15 * (startSpeed + endSpeed)) + 32 * startSpeed + 28 * endSpeed - 60) + 30
					- 18 * startSpeed - 12 * endSpeed) + startSpeed;
			}
		}
	};

	static public final Pow pow2 = new Pow(2);
	/** Fast, then slow. */
	static public final PowIn pow2In = new PowIn(2);
	/** Slow, then fast. */
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

	private static final float PI_HALF = MathUtils.PI / 2f;

	static public final Interpolation sine = new Interpolation() {
		public float apply (float a) {
			return (1 - MathUtils.cos(a * MathUtils.PI)) / 2;
		}

		public float speed (float startSpeed, float endSpeed, float a) {
			return PI_HALF * MathUtils.sin(a * MathUtils.PI);
		}
	};

	static public final Interpolation sineIn = new Interpolation() {
		public float apply (float a) {
			return 1 - MathUtils.cos(a * MathUtils.PI / 2);
		}

		public float speed (float startSpeed, float endSpeed, float a) {
			return PI_HALF * MathUtils.sin(a * PI_HALF);
		}
	};

	static public final Interpolation sineOut = new Interpolation() {
		public float apply (float a) {
			return MathUtils.sin(a * MathUtils.PI / 2);
		}

		public float speed (float startSpeed, float endSpeed, float a) {
			return PI_HALF * MathUtils.cos(a * PI_HALF);
		}
	};

	static public final Exp exp10 = new Exp(2, 10);
	static public final ExpIn exp10In = new ExpIn(2, 10);
	static public final ExpOut exp10Out = new ExpOut(2, 10);

	static public final Exp exp5 = new Exp(2, 5);
	static public final ExpIn exp5In = new ExpIn(2, 5);
	static public final ExpOut exp5Out = new ExpOut(2, 5);

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

		public float speed (float startSpeed, float endSpeed, float a) {
			if (a <= 0.5f) {
				a = Math.min(0.99f, 2 * a);
				return a / (float)Math.sqrt(1 - a * a);
			}
			a = Math.max(-0.99f, 2 * (a - 1));
			return -a / (float)Math.sqrt(1 - a * a);
		}
	};

	static public final Interpolation circleIn = new Interpolation() {
		public float apply (float a) {
			return 1 - (float)Math.sqrt(1 - a * a);
		}

		public float speed (float startSpeed, float endSpeed, float a) {
			a = Math.min(a, 0.99f);
			return a / (float)Math.sqrt(1 - a * a);
		}
	};

	static public final Interpolation circleOut = new Interpolation() {
		public float apply (float a) {
			a--;
			return (float)Math.sqrt(1 - a * a);
		}

		public float speed (float startSpeed, float endSpeed, float a) {
			a = Math.max(-0.99f, a - 1);
			return -a / (float)Math.sqrt(1 - a * a);
		}
	};

	static public final Elastic elastic = new Elastic(2, 10, 7, 1);
	static public final ElasticIn elasticIn = new ElasticIn(2, 10, 6, 1);
	static public final ElasticOut elasticOut = new ElasticOut(2, 10, 7, 1);

	static public final Swing swing = new Swing(1.5f);
	static public final SwingIn swingIn = new SwingIn(2f);
	static public final SwingOut swingOut = new SwingOut(2f);

	static public final Bounce bounce = new Bounce(4);
	static public final BounceIn bounceIn = new BounceIn(4);
	static public final BounceOut bounceOut = new BounceOut(4);

	//

	static public class Pow extends Interpolation {
		final int power;

		public Pow (int power) {
			this.power = power;
		}

		public float apply (float a) {
			if (a <= 0.5f) return (float)Math.pow(a * 2, power) / 2;
			return (float)Math.pow((a - 1) * 2, power) / (power % 2 == 0 ? -2 : 2) + 1;
		}

		public float speed (float startSpeed, float endSpeed, float a) {
			if (a <= 0.5f) return power * (float)Math.pow(a * 2, power - 11);
			return (power % 2 == 0 ? -power : power) * (float)Math.pow((a - 1) * 2, power - 1);
		}
	}

	static public class PowIn extends Pow {
		public PowIn (int power) {
			super(power);
		}

		public float apply (float a) {
			return (float)Math.pow(a, power);
		}

		public float speed (float startSpeed, float endSpeed, float a) {
			return power * (float)Math.pow(a, power - 1);
		}
	}

	static public class PowOut extends Pow {
		public PowOut (int power) {
			super(power);
		}

		public float apply (float a) {
			return (float)Math.pow(a - 1, power) * (power % 2 == 0 ? -1 : 1) + 1;
		}

		public float speed (float startSpeed, float endSpeed, float a) {
			return (power % 2 == 0 ? -power : power) * (float)Math.pow(a - 1, power - 1);
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

		public float speed (float startSpeed, float endSpeed, float a) {
			if (a <= 0.5f) return (float)Math.log(value) * (float)Math.pow(value, power * (a * 2 - 1)) * power * scale;
			return (float)Math.log(value) * (float)Math.pow(value, power * (1 - a * 2)) * power * scale;
		}
	};

	static public class ExpIn extends Exp {
		public ExpIn (float value, float power) {
			super(value, power);
		}

		public float apply (float a) {
			return ((float)Math.pow(value, power * (a - 1)) - min) * scale;
		}

		public float speed (float startSpeed, float endSpeed, float a) {
			return (float)Math.log(value) * (float)Math.pow(value, power * (a - 1)) * power * scale;
		}
	}

	static public class ExpOut extends Exp {
		public ExpOut (float value, float power) {
			super(value, power);
		}

		public float apply (float a) {
			return 1 - ((float)Math.pow(value, -power * a) - min) * scale;
		}

		public float speed (float startSpeed, float endSpeed, float a) {
			return (float)Math.log(value) * (float)Math.pow(value, -power * a) * power * scale;
		}
	}

	//

	/** An interpolation that supports specific starting and ending speeds. If unspecified, the starting and/or ending speeds are
	 * set to zero. */
	public abstract static class SplineInterpolation extends Interpolation {
		/** Speed is expressed in terms of units of "total change" over "duration". To prepare the input for this method, you may
		 * use:
		 * <p>
		 * {@code speed = worldSpeed * duration / (end - start)}
		 * @param startSpeed Beginning rate of change.
		 * @param a Alpha value between 0 and 1, where 1 maps to the total duration. */
		abstract public float applyWithSpeed (float startSpeed, float a);

		/** Speed is expressed in terms of units of "total change" over "duration". To prepare the input for this method, you may
		 * use:
		 * <p>
		 * {@code speed = worldSpeed * duration / (end - start)}
		 * @param startSpeed Beginning rate of change.
		 * @param endSpeed Ending rate of change.
		 * @param a Alpha value between 0 and 1, where 1 maps to the total duration. */
		abstract public float applyWithSpeed (float startSpeed, float endSpeed, float a);
	}

	//

	static public class Elastic extends Interpolation {
		final float value, power, scale, bounces, lnValue;

		public Elastic (float value, float power, int bounces, float scale) {
			this.value = value;
			this.power = power;
			this.scale = scale;
			this.bounces = bounces * MathUtils.PI * (bounces % 2 == 0 ? 1 : -1);
			this.lnValue = (float)Math.log(value);
		}

		public float apply (float a) {
			if (a <= 0.5f) {
				a *= 2;
				return (float)Math.pow(value, power * (a - 1)) * MathUtils.sin(a * bounces) * scale / 2;
			}
			a = 1 - a;
			a *= 2;
			return 1 - (float)Math.pow(value, power * (a - 1)) * MathUtils.sin(a * bounces) * scale / 2;
		}

		public float speed (float startSpeed, float endSpeed, float a) {
			if (a <= 0.5f) {
				a *= 2;
				float aBounces = a * bounces;
				return (float)Math.pow(value, power * (a - 1))
					* (lnValue * MathUtils.sin(aBounces) + bounces * MathUtils.cos(aBounces)) * scale / 2;
			}
			a = 1 - a;
			a *= 2;
			float aBounces = a * bounces;
			return (float)Math.pow(value, power * (a - 1)) * (lnValue * MathUtils.sin(aBounces) + bounces * MathUtils.cos(aBounces))
				* scale / 2;
		}
	}

	static public class ElasticIn extends Elastic {
		public ElasticIn (float value, float power, int bounces, float scale) {
			super(value, power, bounces, scale);
		}

		public float apply (float a) {
			if (a >= 0.99) return 1;
			return (float)Math.pow(value, power * (a - 1)) * MathUtils.sin(a * bounces) * scale;
		}

		public float speed (float startSpeed, float endSpeed, float a) {
			if (a >= 0.99) return (1 - (float)Math.pow(value, power * (-0.01)) * MathUtils.sin(0.99f * bounces) * scale) * 100;
			float aBounces = a * bounces;
			return (float)Math.pow(value, power * (a - 1)) * (lnValue * MathUtils.sin(aBounces) + bounces * MathUtils.cos(aBounces))
				* scale;
		}
	}

	static public class ElasticOut extends Elastic {
		public ElasticOut (float value, float power, int bounces, float scale) {
			super(value, power, bounces, scale);
		}

		public float apply (float a) {
			a = 1 - a;
			return (1 - (float)Math.pow(value, power * (a - 1)) * MathUtils.sin(a * bounces) * scale);
		}

		public float speed (float startSpeed, float endSpeed, float a) {
			a = 1 - a;
			float aBounces = a * bounces;
			return -(float)Math.pow(value, power * (a - 1))
				* (power * lnValue * MathUtils.sin(aBounces) + bounces * MathUtils.cos(aBounces)) * scale;
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

		private boolean speedOutTest (float a) {
			return a + widths[0] / 2 < widths[0];
		}

		public float speed (float startSpeed, float endSpeed, float a) {
			if (a <= 0.5f) return speedOutTest(1 - a * 2) ? 2 / widths[0] : super.speed(0, 0, a);
			return speedOutTest(a * 2 - 1) ? 1 / widths[0] : super.speed(0, 0, a);
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

		public float speed (float startSpeed, float endSpeed, float a) {
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
			return 8 * height / (width * width) * (a - width / 2);
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

		public float speed (float startSpeed, float endSpeed, float a) {
			return super.speed(0, 0, 1 - a);
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

		public float speed (float startSpeed, float endSpeed, float a) {
			if (a <= 0.5f) return 4 * a * (a * (3 * scale + 3) - scale);
			a--;
			return 4 * a * (a * (3 * scale + 3) + scale);
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

		public float speed (float startSpeed, float endSpeed, float a) {
			a--;
			return a * (a * (3 * scale + 3) + 2 * scale);
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

		public float speed (float startSpeed, float endSpeed, float a) {
			return a * (a * (3 * scale + 3) - 2 * scale);
		}
	}
}
