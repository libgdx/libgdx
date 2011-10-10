
package com.badlogic.gdx.math;

/** Function that takes a linear value in the range of 0-1 and outputs a (usually) non-linear interpolated value. */
public abstract class Interpolation {
	abstract public float apply (float a);

	static public final Interpolation linear = new Interpolation() {
		public float apply (float a) {
			return a;
		}
	};

	static public final Interpolation fade = new Interpolation() {
		public float apply (float a) {
			return a * a * a * (a * (a * 6 - 15) + 10);
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

	static public final Interpolation exp = new Interpolation() {
		public float apply (float a) {
			if (a <= 0.5f) return (float)Math.pow(2, 10 * (a * 2 - 1)) / 2;
			return (2 - (float)Math.pow(2, -10 * (a * 2 - 1))) / 2;
		}
	};

	static public final Interpolation expIn = new Interpolation() {
		public float apply (float a) {
			return (float)Math.pow(2, 10 * (a - 1));
		}
	};

	static public final Interpolation expOut = new Interpolation() {
		public float apply (float a) {
			return 1 - (float)Math.pow(2, -10 * a);
		}
	};

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

	static public final Interpolation elastic = new Interpolation() {
		public float apply (float a) {
			if (a <= 0.5f) {
				a *= 2;
				return (float)Math.pow(2, 10 * (a - 1)) * MathUtils.sin(a * 3.14f / 0.157f) * 1.1f / 2;
			}
			a = 1 - a;
			a *= 2;
			return 1 - (float)Math.pow(2, 10 * (a - 1)) * MathUtils.sin((a) * 3.14f / 0.157f) * 1.1f / 2;
		}
	};

	static public final Interpolation elasticIn = new Interpolation() {
		public float apply (float a) {
			return (float)Math.pow(2, 10 * (a - 1)) * MathUtils.sin(a * 3.14f / 0.157f) * 1.1f;
		}
	};

	static public final Interpolation elasticOut = new Interpolation() {
		public float apply (float a) {
			a = 1 - a;
			return 1 - (float)Math.pow(2, 10 * (a - 1)) * MathUtils.sin((a) * 3.14f / 0.157f) * 1.1f;
		}
	};

	static public final Interpolation back = new Interpolation() {
		public float apply (float a) {
			if (a <= 0.5f) {
				a *= 2;
				return ((1.70158f + 1) * a * a * a - 1.70158f * a * a) / 2;
			}
			a = 1 - a;
			a *= 2;
			return 1 - ((1.70158f + 1) * a * a * a - 1.70158f * a * a) / 2;
		}
	};

	static public final Interpolation backIn = new Interpolation() {
		public float apply (float a) {
			return (1.70158f + 1) * a * a * a - 1.70158f * a * a;
		}
	};

	static public final Interpolation backOut = new Interpolation() {
		public float apply (float a) {
			a = 1 - a;
			return 1 - ((1.70158f + 1) * a * a * a - 1.70158f * a * a);
		}
	};

	static public final Interpolation bounce = new Interpolation() {
		public float apply (float a) {
			if (a <= 0.5f) return (1 - bounceOut.apply(1 - a * 2)) / 2;
			return bounceOut.apply(a * 2 - 1) / 2 + 0.5f;
		}
	};

	static public final Interpolation bounceIn = new Interpolation() {
		public float apply (float a) {
			return 1 - bounceOut.apply(1 - a);
		}
	};

	static public final Interpolation bounceOut = new Interpolation() {
		public float apply (float a) {
			if (a < 1 / 2.75f) {
				return 7.5625f * a * a;
			}
			if (a < 2 / 2.75f) {
				a -= (1.5 / 2.75);
				return 7.5625f * a * a + 0.75f;
			} else if (a < 2.5 / 2.75) {
				a -= 2.25 / 2.75;
				return 7.5625f * a * a + 0.9375f;
			}
			a -= 2.625 / 2.75;
			return 7.5625f * a * a + 0.984375f;
		}
	};

	//

	static public class Pow extends Interpolation {
		private final int power;

		public Pow (int power) {
			this.power = power;
		}

		public float apply (float a) {
			if (a <= 0.5f) return (float)Math.pow(a * 2, power) / 2;
			return (float)Math.pow((a - 1) * 2, power) / (power % 2 == 0 ? -2 : 2) + 1;
		}
	}

	static public class PowIn extends Interpolation {
		private final int power;

		public PowIn (int power) {
			this.power = power;
		}

		public float apply (float a) {
			return (float)Math.pow(a, power);
		}
	}

	static public class PowOut extends Interpolation {
		private final int power;

		public PowOut (int power) {
			this.power = power;
		}

		public float apply (float a) {
			return (float)Math.pow(a - 1, power) * (power % 2 == 0 ? -1 : 1) + 1;
		}
	}

}
