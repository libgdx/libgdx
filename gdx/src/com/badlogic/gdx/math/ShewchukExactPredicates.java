
package com.badlogic.gdx.math;

/** Based on Jonathan Richard Shewchuk, "Adaptive Precision Floating-Point Arithmetic and Fast Robust Geometric Predicates"
 * (Discrete &amp; Computational Geometry 18(3):305-363, 1997). Java port of mourner/robust-predicates v3.0.3.
 *
 * <p>
 * {@link #orient2d}, {@link #orient3d}, {@link #incircle} and {@link #insphere} return a {@code double} whose sign is exact; the
 * {@code *fast} variants are faster non-robust approximations.
 *
 * <p>
 * <strong>Not thread-safe.</strong> Each instance lazily allocates and reuses internal scratch buffers; once every predicate has
 * been exercised an instance retains roughly 284&nbsp;KiB of them (dominated by insphere). */
public class ShewchukExactPredicates {

	// ---- constants --------------------------------------------------------

	private static final double epsilon = 1.1102230246251565e-16;
	private static final double splitter = 134217729;
	private static final double resulterrbound = (3 + 8 * epsilon) * epsilon;

	private static final double ccwerrboundA = (3 + 16 * epsilon) * epsilon;
	private static final double ccwerrboundB = (2 + 12 * epsilon) * epsilon;
	private static final double ccwerrboundC = (9 + 64 * epsilon) * epsilon * epsilon;

	private static final double o3derrboundA = (7 + 56 * epsilon) * epsilon;
	private static final double o3derrboundB = (3 + 28 * epsilon) * epsilon;
	private static final double o3derrboundC = (26 + 288 * epsilon) * epsilon * epsilon;

	private static final double iccerrboundA = (10 + 96 * epsilon) * epsilon;
	private static final double iccerrboundB = (4 + 48 * epsilon) * epsilon;
	private static final double iccerrboundC = (44 + 576 * epsilon) * epsilon * epsilon;

	private static final double isperrboundA = (16 + 224 * epsilon) * epsilon;
	private static final double isperrboundB = (5 + 72 * epsilon) * epsilon;
	private static final double isperrboundC = (71 + 1408 * epsilon) * epsilon * epsilon;

	// Per-predicate scratch holders, lazily allocated on first use and reused
	// across calls (never cleared in between).

	private O2Scratch o2;
	private O3Scratch o3;
	private IncircleScratch ic;
	private SumThreeScratch s3;
	private LiftExactScratch lex;
	private InsphereExactScratch iex;
	private LiftAdaptScratch lad;
	private InsphereAdaptScratch iad;

	// ---- expansion arithmetic --------------------------------------------

	private static int sum (int elen, double[] e, int flen, double[] f, double[] h) {
		double Q = 0, Qnew = 0, hh = 0, bvirt = 0;
		double enow = e[0];
		double fnow = f[0];
		int eindex = 0;
		int findex = 0;
		if ((fnow > enow) == (fnow > -enow)) {
			Q = enow;
			enow = ++eindex < elen ? e[eindex] : 0;
		} else {
			Q = fnow;
			fnow = ++findex < flen ? f[findex] : 0;
		}
		int hindex = 0;
		if (eindex < elen && findex < flen) {
			if ((fnow > enow) == (fnow > -enow)) {
				Qnew = enow + Q;
				hh = Q - (Qnew - enow);
				enow = ++eindex < elen ? e[eindex] : 0;
			} else {
				Qnew = fnow + Q;
				hh = Q - (Qnew - fnow);
				fnow = ++findex < flen ? f[findex] : 0;
			}
			Q = Qnew;
			if (hh != 0) {
				h[hindex++] = hh;
			}
			while (eindex < elen && findex < flen) {
				if ((fnow > enow) == (fnow > -enow)) {
					Qnew = Q + enow;
					bvirt = Qnew - Q;
					hh = Q - (Qnew - bvirt) + (enow - bvirt);
					enow = ++eindex < elen ? e[eindex] : 0;
				} else {
					Qnew = Q + fnow;
					bvirt = Qnew - Q;
					hh = Q - (Qnew - bvirt) + (fnow - bvirt);
					fnow = ++findex < flen ? f[findex] : 0;
				}
				Q = Qnew;
				if (hh != 0) {
					h[hindex++] = hh;
				}
			}
		}
		while (eindex < elen) {
			Qnew = Q + enow;
			bvirt = Qnew - Q;
			hh = Q - (Qnew - bvirt) + (enow - bvirt);
			enow = ++eindex < elen ? e[eindex] : 0;
			Q = Qnew;
			if (hh != 0) {
				h[hindex++] = hh;
			}
		}
		while (findex < flen) {
			Qnew = Q + fnow;
			bvirt = Qnew - Q;
			hh = Q - (Qnew - bvirt) + (fnow - bvirt);
			fnow = ++findex < flen ? f[findex] : 0;
			Q = Qnew;
			if (hh != 0) {
				h[hindex++] = hh;
			}
		}
		if (Q != 0 || hindex == 0) {
			h[hindex++] = Q;
		}
		return hindex;
	}

	private static int sum_three (int alen, double[] a, int blen, double[] b, int clen, double[] c, double[] tmp, double[] out) {
		return sum(sum(alen, a, blen, b, tmp), tmp, clen, c, out);
	}

	private static int scale (int elen, double[] e, double b, double[] h) {
		double Q = 0, sm = 0, hh = 0, product1 = 0, product0 = 0;
		double bvirt = 0, c = 0, ahi = 0, alo = 0, bhi = 0, blo = 0;

		c = splitter * b;
		bhi = c - (c - b);
		blo = b - bhi;
		double enow = e[0];
		Q = enow * b;
		c = splitter * enow;
		ahi = c - (c - enow);
		alo = enow - ahi;
		hh = alo * blo - (Q - ahi * bhi - alo * bhi - ahi * blo);
		int hindex = 0;
		if (hh != 0) {
			h[hindex++] = hh;
		}
		for (int i = 1; i < elen; i++) {
			enow = e[i];
			product1 = enow * b;
			c = splitter * enow;
			ahi = c - (c - enow);
			alo = enow - ahi;
			product0 = alo * blo - (product1 - ahi * bhi - alo * bhi - ahi * blo);
			sm = Q + product0;
			bvirt = sm - Q;
			hh = Q - (sm - bvirt) + (product0 - bvirt);
			if (hh != 0) {
				h[hindex++] = hh;
			}
			Q = product1 + sm;
			hh = sm - (Q - product1);
			if (hh != 0) {
				h[hindex++] = hh;
			}
		}
		if (Q != 0 || hindex == 0) {
			h[hindex++] = Q;
		}
		return hindex;
	}

	private static int negate (int elen, double[] e) {
		for (int i = 0; i < elen; i++)
			e[i] = -e[i];
		return elen;
	}

	private static double estimate (int elen, double[] e) {
		double Q = e[0];
		for (int i = 1; i < elen; i++)
			Q += e[i];
		return Q;
	}

	// ---- error-free transforms -------------------------------------------

	private static double splitHi (double a) {
		double c = splitter * a;
		return c - (c - a);
	}

	private static double twoSumLo (double a, double b, double x) {
		double bvirt = x - a;
		return a - (x - bvirt) + (b - bvirt);
	}

	private static double twoDiffTail (double a, double b, double x) {
		double bvirt = a - x;
		return a - (x + bvirt) + (bvirt - b);
	}

	private static double twoProductLo (double a, double b, double x) {
		double ahi = splitHi(a), alo = a - ahi;
		double bhi = splitHi(b), blo = b - bhi;
		return alo * blo - (x - ahi * bhi - alo * bhi - ahi * blo);
	}

	private static double twoProductPresplitLo (double a, double bhi, double blo, double x) {
		double ahi = splitHi(a), alo = a - ahi;
		return alo * blo - (x - ahi * bhi - alo * bhi - ahi * blo);
	}

	private static double squareLo (double a, double x) {
		double ahi = splitHi(a), alo = a - ahi;
		return alo * alo - (x - ahi * ahi - (ahi + ahi) * alo);
	}

	private static double twoProduct (double a, double b, double[] lo, int loIdx) {
		double x = a * b;
		lo[loIdx] = twoProductLo(a, b, x);
		return x;
	}

	private static double crossProduct (double a, double b, double c, double d, double[] D) {
		double s1 = a * d;
		double s0 = twoProductLo(a, d, s1);
		double t1 = c * b;
		double t0 = twoProductLo(c, b, t1);
		double _i = s0 - t0;
		D[0] = twoDiffTail(s0, t0, _i);
		double _j = s1 + _i;
		double _0 = twoSumLo(s1, _i, _j);
		_i = _0 - t1;
		D[1] = twoDiffTail(_0, t1, _i);
		double u3 = _j + _i;
		D[2] = twoSumLo(_j, _i, u3);
		D[3] = u3;
		return u3;
	}

	private static void twoProductSum (double a, double b, double c, double d, double[] D) {
		double s1 = a * b;
		double s0 = twoProductLo(a, b, s1);
		double t1 = c * d;
		double t0 = twoProductLo(c, d, t1);
		double _i = s0 + t0;
		D[0] = twoSumLo(s0, t0, _i);
		double _j = s1 + _i;
		double _0 = twoSumLo(s1, _i, _j);
		_i = _0 + t1;
		D[1] = twoSumLo(_0, t1, _i);
		double u3 = _j + _i;
		D[2] = twoSumLo(_j, _i, u3);
		D[3] = u3;
	}

	private static void squareSum (double a, double b, double[] D) {
		double s1 = a * a;
		double s0 = squareLo(a, s1);
		double t1 = b * b;
		double t0 = squareLo(b, t1);
		double _i = s0 + t0;
		D[0] = twoSumLo(s0, t0, _i);
		double _j = s1 + _i;
		double _0 = twoSumLo(s1, _i, _j);
		_i = _0 + t1;
		D[1] = twoSumLo(_0, t1, _i);
		double u3 = _j + _i;
		D[2] = twoSumLo(_j, _i, u3);
		D[3] = u3;
	}

	private static void twoOneProduct (double a1, double a0, double b, double[] D) {
		double bhi = splitHi(b), blo = b - bhi;
		double _i = a0 * b;
		D[0] = twoProductPresplitLo(a0, bhi, blo, _i);
		double _j = a1 * b;
		double _0 = twoProductPresplitLo(a1, bhi, blo, _j);
		double _k = _i + _0;
		D[1] = twoSumLo(_i, _0, _k);
		double u3 = _j + _k;
		D[2] = _k - (u3 - _j);
		D[3] = u3;
	}

	private static final class Acc {
		double[] fin;
		double[] fin2;
		int len;

		Acc (int cap) {
			fin = new double[cap];
			fin2 = new double[cap];
		}

		void add (int alen, double[] a) {
			len = sum(len, fin, alen, a, fin2);
			double[] t = fin;
			fin = fin2;
			fin2 = t;
		}
	}

	private static final class O2Scratch {
		final double[] B = new double[4], C1 = new double[8], C2 = new double[12], D = new double[16], u = new double[4];
	}

	private static final class O3Scratch {
		final double[] bc = new double[4], ca = new double[4], ab = new double[4], at_b = new double[4], at_c = new double[4],
			bt_c = new double[4], bt_a = new double[4], ct_a = new double[4], ct_b = new double[4], bct = new double[8],
			cat = new double[8], abt = new double[8], u = new double[4], _8 = new double[8], _8b = new double[8],
			_16 = new double[16], _12 = new double[12];
		final Acc acc = new Acc(192);
	}

	private static final class IncircleScratch {
		final double[] bc = new double[4], ca = new double[4], ab = new double[4], aa = new double[4], bb = new double[4],
			cc = new double[4], u = new double[4], v = new double[4], axtbc = new double[8], aytbc = new double[8],
			bxtca = new double[8], bytca = new double[8], cxtab = new double[8], cytab = new double[8], abt = new double[8],
			bct = new double[8], cat = new double[8], abtt = new double[4], bctt = new double[4], catt = new double[4],
			_8 = new double[8], _16 = new double[16], _16b = new double[16], _16c = new double[16], _32 = new double[32],
			_32b = new double[32], _48 = new double[48], _64 = new double[64];
		final Acc acc = new Acc(1152);
	}

	private static final class SumThreeScratch {
		final double[] _8 = new double[8], _8b = new double[8], _8c = new double[8], _16 = new double[16];
	}

	private static final class LiftExactScratch {
		final double[] _48 = new double[48], _48b = new double[48], _96 = new double[96], _192 = new double[192],
			_384x = new double[384], _384y = new double[384], _384z = new double[384], _768 = new double[768];
	}

	private static final class InsphereExactScratch {
		final double[] ab = new double[4], bc = new double[4], cd = new double[4], de = new double[4], ea = new double[4],
			ac = new double[4], bd = new double[4], ce = new double[4], da = new double[4], eb = new double[4], abc = new double[24],
			bcd = new double[24], cde = new double[24], dea = new double[24], eab = new double[24], abd = new double[24],
			bce = new double[24], cda = new double[24], deb = new double[24], eac = new double[24], adet = new double[1152],
			bdet = new double[1152], cdet = new double[1152], ddet = new double[1152], edet = new double[1152],
			abdet = new double[2304], cddet = new double[2304], cdedet = new double[3456], deter = new double[5760];
	}

	private static final class LiftAdaptScratch {
		final double[] _24 = new double[24], _48 = new double[48], xdet = new double[96], ydet = new double[96],
			zdet = new double[96], _192 = new double[192];
	}

	private static final class InsphereAdaptScratch {
		final double[] ab = new double[4], bc = new double[4], cd = new double[4], da = new double[4], ac = new double[4],
			bd = new double[4], adet = new double[1152], bdet = new double[1152], cdet = new double[1152], ddet = new double[1152],
			abdet = new double[2304], cddet = new double[2304], fin = new double[1152];
	}

	// ===== orient2d ========================================================

	private double orient2dadapt (double ax, double ay, double bx, double by, double cx, double cy, double detsum) {
		if (o2 == null) o2 = new O2Scratch();

		double acx = ax - cx;
		double bcx = bx - cx;
		double acy = ay - cy;
		double bcy = by - cy;

		crossProduct(acx, bcx, acy, bcy, o2.B);

		double det = estimate(4, o2.B);
		double errbound = ccwerrboundB * detsum;
		if (Math.abs(det) >= errbound) {
			return det;
		}

		double acxtail = twoDiffTail(ax, cx, acx);
		double bcxtail = twoDiffTail(bx, cx, bcx);
		double acytail = twoDiffTail(ay, cy, acy);
		double bcytail = twoDiffTail(by, cy, bcy);

		if (acxtail == 0 && acytail == 0 && bcxtail == 0 && bcytail == 0) {
			return det;
		}

		errbound = ccwerrboundC * detsum + resulterrbound * Math.abs(det);
		det += (acx * bcytail + bcy * acxtail) - (acy * bcxtail + bcx * acytail);
		if (Math.abs(det) >= errbound) return det;

		crossProduct(acxtail, bcx, acytail, bcy, o2.u);
		int C1len = sum(4, o2.B, 4, o2.u, o2.C1);

		crossProduct(acx, bcxtail, acy, bcytail, o2.u);
		int C2len = sum(C1len, o2.C1, 4, o2.u, o2.C2);

		crossProduct(acxtail, bcxtail, acytail, bcytail, o2.u);
		int Dlen = sum(C2len, o2.C2, 4, o2.u, o2.D);

		return o2.D[Dlen - 1];
	}

	/** 2D orientation test. Returns the signed area of triangle {@code abc} times two: positive when {@code a}, {@code b},
	 * {@code c} are in counter-clockwise order, negative when clockwise, zero when collinear (assuming y-up). Equivalently, the
	 * sign tells which side of the directed line from {@code a} to {@code b} the point {@code c} lies on. Only the sign is
	 * guaranteed exact. */
	public double orient2d (double ax, double ay, double bx, double by, double cx, double cy) {
		double detleft = (ax - cx) * (by - cy);
		double detright = (ay - cy) * (bx - cx);
		double det = detleft - detright;

		double detsum = Math.abs(detleft + detright);
		if (Math.abs(det) >= ccwerrboundA * detsum) return det;

		return orient2dadapt(ax, ay, bx, by, cx, cy, detsum);
	}

	/** Non-robust counterpart of {@link #orient2d}; the sign may be wrong on near-degenerate inputs. */
	public double orient2dfast (double ax, double ay, double bx, double by, double cx, double cy) {
		return (ax - cx) * (by - cy) - (ay - cy) * (bx - cx);
	}

	// ===== orient3d ========================================================

	private static int tailinit (double xtail, double ytail, double ax, double ay, double bx, double by, double[] a, double[] b) {
		if (xtail == 0) {
			if (ytail == 0) {
				a[0] = 0;
				b[0] = 0;
				return 1;
			}
			double negate = -ytail;
			a[1] = twoProduct(negate, ax, a, 0);
			b[1] = twoProduct(ytail, bx, b, 0);
			return 2;
		}
		if (ytail == 0) {
			a[1] = twoProduct(xtail, ay, a, 0);
			double negate = -xtail;
			b[1] = twoProduct(negate, by, b, 0);
			return 2;
		}
		crossProduct(xtail, ax, ytail, ay, a);
		crossProduct(ytail, by, xtail, bx, b);
		return 4;
	}

	private static void tailadd (Acc acc, double[] u, double a, double b, double k, double z) {
		double s1 = a * b;
		double s0 = twoProductLo(a, b, s1);
		twoOneProduct(s1, s0, k, u);
		acc.add(4, u);
		if (z != 0) {
			twoOneProduct(s1, s0, z, u);
			acc.add(4, u);
		}
	}

	private double orient3dadapt (double ax, double ay, double az, double bx, double by, double bz, double cx, double cy,
		double cz, double dx, double dy, double dz, double permanent) {
		if (o3 == null) o3 = new O3Scratch();

		double adx = ax - dx;
		double bdx = bx - dx;
		double cdx = cx - dx;
		double ady = ay - dy;
		double bdy = by - dy;
		double cdy = cy - dy;
		double adz = az - dz;
		double bdz = bz - dz;
		double cdz = cz - dz;

		crossProduct(bdx, bdy, cdx, cdy, o3.bc);
		crossProduct(cdx, cdy, adx, ady, o3.ca);
		crossProduct(adx, ady, bdx, bdy, o3.ab);

		o3.acc.len = sum(sum(scale(4, o3.bc, adz, o3._8), o3._8, scale(4, o3.ca, bdz, o3._8b), o3._8b, o3._16), o3._16,
			scale(4, o3.ab, cdz, o3._8), o3._8, o3.acc.fin);

		double det = estimate(o3.acc.len, o3.acc.fin);
		double errbound = o3derrboundB * permanent;
		if (Math.abs(det) >= errbound) {
			return det;
		}

		double adxtail = twoDiffTail(ax, dx, adx);
		double bdxtail = twoDiffTail(bx, dx, bdx);
		double cdxtail = twoDiffTail(cx, dx, cdx);
		double adytail = twoDiffTail(ay, dy, ady);
		double bdytail = twoDiffTail(by, dy, bdy);
		double cdytail = twoDiffTail(cy, dy, cdy);
		double adztail = twoDiffTail(az, dz, adz);
		double bdztail = twoDiffTail(bz, dz, bdz);
		double cdztail = twoDiffTail(cz, dz, cdz);

		if (adxtail == 0 && bdxtail == 0 && cdxtail == 0 && adytail == 0 && bdytail == 0 && cdytail == 0 && adztail == 0
			&& bdztail == 0 && cdztail == 0) {
			return det;
		}

		errbound = o3derrboundC * permanent + resulterrbound * Math.abs(det);
		det += adz * (bdx * cdytail + cdy * bdxtail - (bdy * cdxtail + cdx * bdytail)) + adztail * (bdx * cdy - bdy * cdx)
			+ bdz * (cdx * adytail + ady * cdxtail - (cdy * adxtail + adx * cdytail)) + bdztail * (cdx * ady - cdy * adx)
			+ cdz * (adx * bdytail + bdy * adxtail - (ady * bdxtail + bdx * adytail)) + cdztail * (adx * bdy - ady * bdx);
		if (Math.abs(det) >= errbound) {
			return det;
		}

		int at_len = tailinit(adxtail, adytail, bdx, bdy, cdx, cdy, o3.at_b, o3.at_c);
		int bt_len = tailinit(bdxtail, bdytail, cdx, cdy, adx, ady, o3.bt_c, o3.bt_a);
		int ct_len = tailinit(cdxtail, cdytail, adx, ady, bdx, bdy, o3.ct_a, o3.ct_b);

		int bctlen = sum(bt_len, o3.bt_c, ct_len, o3.ct_b, o3.bct);
		o3.acc.add(scale(bctlen, o3.bct, adz, o3._16), o3._16);

		int catlen = sum(ct_len, o3.ct_a, at_len, o3.at_c, o3.cat);
		o3.acc.add(scale(catlen, o3.cat, bdz, o3._16), o3._16);

		int abtlen = sum(at_len, o3.at_b, bt_len, o3.bt_a, o3.abt);
		o3.acc.add(scale(abtlen, o3.abt, cdz, o3._16), o3._16);

		if (adztail != 0) {
			o3.acc.add(scale(4, o3.bc, adztail, o3._12), o3._12);
			o3.acc.add(scale(bctlen, o3.bct, adztail, o3._16), o3._16);
		}
		if (bdztail != 0) {
			o3.acc.add(scale(4, o3.ca, bdztail, o3._12), o3._12);
			o3.acc.add(scale(catlen, o3.cat, bdztail, o3._16), o3._16);
		}
		if (cdztail != 0) {
			o3.acc.add(scale(4, o3.ab, cdztail, o3._12), o3._12);
			o3.acc.add(scale(abtlen, o3.abt, cdztail, o3._16), o3._16);
		}

		if (adxtail != 0) {
			if (bdytail != 0) {
				tailadd(o3.acc, o3.u, adxtail, bdytail, cdz, cdztail);
			}
			if (cdytail != 0) {
				tailadd(o3.acc, o3.u, -adxtail, cdytail, bdz, bdztail);
			}
		}
		if (bdxtail != 0) {
			if (cdytail != 0) {
				tailadd(o3.acc, o3.u, bdxtail, cdytail, adz, adztail);
			}
			if (adytail != 0) {
				tailadd(o3.acc, o3.u, -bdxtail, adytail, cdz, cdztail);
			}
		}
		if (cdxtail != 0) {
			if (adytail != 0) {
				tailadd(o3.acc, o3.u, cdxtail, adytail, bdz, bdztail);
			}
			if (bdytail != 0) {
				tailadd(o3.acc, o3.u, -cdxtail, bdytail, adz, adztail);
			}
		}

		return o3.acc.fin[o3.acc.len - 1];
	}

	/** 3D orientation test. Returns six times the signed volume of tetrahedron {@code abcd}: positive when {@code a}, {@code b},
	 * {@code c} appear in counter-clockwise order as viewed from {@code d}, negative when clockwise, zero when the four points are
	 * coplanar (assuming a right-handed coordinate system). Equivalently, the sign tells which side of the plane through
	 * {@code a}, {@code b}, {@code c} the point {@code d} lies on. Only the sign is guaranteed exact. */
	public double orient3d (double ax, double ay, double az, double bx, double by, double bz, double cx, double cy, double cz,
		double dx, double dy, double dz) {
		double adx = ax - dx;
		double bdx = bx - dx;
		double cdx = cx - dx;
		double ady = ay - dy;
		double bdy = by - dy;
		double cdy = cy - dy;
		double adz = az - dz;
		double bdz = bz - dz;
		double cdz = cz - dz;

		double bdxcdy = bdx * cdy;
		double cdxbdy = cdx * bdy;

		double cdxady = cdx * ady;
		double adxcdy = adx * cdy;

		double adxbdy = adx * bdy;
		double bdxady = bdx * ady;

		double det = adz * (cdxbdy - bdxcdy) + bdz * (adxcdy - cdxady) + cdz * (bdxady - adxbdy);

		double permanent = (Math.abs(bdxcdy) + Math.abs(cdxbdy)) * Math.abs(adz)
			+ (Math.abs(cdxady) + Math.abs(adxcdy)) * Math.abs(bdz) + (Math.abs(adxbdy) + Math.abs(bdxady)) * Math.abs(cdz);

		double errbound = o3derrboundA * permanent;
		if (det > errbound || -det > errbound) {
			return det;
		}

		return -orient3dadapt(ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz, permanent);
	}

	/** Non-robust counterpart of {@link #orient3d}; the sign may be wrong on near-degenerate inputs. */
	public double orient3dfast (double ax, double ay, double az, double bx, double by, double bz, double cx, double cy, double cz,
		double dx, double dy, double dz) {
		double adx = ax - dx;
		double bdx = bx - dx;
		double cdx = cx - dx;
		double ady = ay - dy;
		double bdy = by - dy;
		double cdy = cy - dy;
		double adz = az - dz;
		double bdz = bz - dz;
		double cdz = cz - dz;

		return adx * (bdz * cdy - bdy * cdz) + bdx * (cdz * ady - cdy * adz) + cdx * (adz * bdy - ady * bdz);
	}

	// ===== incircle ========================================================

	private double incircleadapt (double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy,
		double permanent) {
		int axtbclen = 0, aytbclen = 0, bxtcalen = 0, bytcalen = 0, cxtablen = 0, cytablen = 0;
		int abtlen, bctlen, catlen;
		int abttlen, bcttlen, cattlen;

		if (ic == null) ic = new IncircleScratch();

		double adx = ax - dx;
		double bdx = bx - dx;
		double cdx = cx - dx;
		double ady = ay - dy;
		double bdy = by - dy;
		double cdy = cy - dy;

		crossProduct(bdx, bdy, cdx, cdy, ic.bc);
		crossProduct(cdx, cdy, adx, ady, ic.ca);
		crossProduct(adx, ady, bdx, bdy, ic.ab);

		ic.acc.len = sum(
			sum(
				sum(scale(scale(4, ic.bc, adx, ic._8), ic._8, adx, ic._16), ic._16,
					scale(scale(4, ic.bc, ady, ic._8), ic._8, ady, ic._16b), ic._16b, ic._32),
				ic._32,
				sum(scale(scale(4, ic.ca, bdx, ic._8), ic._8, bdx, ic._16), ic._16,
					scale(scale(4, ic.ca, bdy, ic._8), ic._8, bdy, ic._16b), ic._16b, ic._32b),
				ic._32b, ic._64),
			ic._64, sum(scale(scale(4, ic.ab, cdx, ic._8), ic._8, cdx, ic._16), ic._16,
				scale(scale(4, ic.ab, cdy, ic._8), ic._8, cdy, ic._16b), ic._16b, ic._32),
			ic._32, ic.acc.fin);

		double det = estimate(ic.acc.len, ic.acc.fin);
		double errbound = iccerrboundB * permanent;
		if (Math.abs(det) >= errbound) {
			return det;
		}

		double adxtail = twoDiffTail(ax, dx, adx);
		double adytail = twoDiffTail(ay, dy, ady);
		double bdxtail = twoDiffTail(bx, dx, bdx);
		double bdytail = twoDiffTail(by, dy, bdy);
		double cdxtail = twoDiffTail(cx, dx, cdx);
		double cdytail = twoDiffTail(cy, dy, cdy);
		if (adxtail == 0 && bdxtail == 0 && cdxtail == 0 && adytail == 0 && bdytail == 0 && cdytail == 0) {
			return det;
		}

		errbound = iccerrboundC * permanent + resulterrbound * Math.abs(det);
		det += ((adx * adx + ady * ady) * ((bdx * cdytail + cdy * bdxtail) - (bdy * cdxtail + cdx * bdytail))
			+ 2 * (adx * adxtail + ady * adytail) * (bdx * cdy - bdy * cdx))
			+ ((bdx * bdx + bdy * bdy) * ((cdx * adytail + ady * cdxtail) - (cdy * adxtail + adx * cdytail))
				+ 2 * (bdx * bdxtail + bdy * bdytail) * (cdx * ady - cdy * adx))
			+ ((cdx * cdx + cdy * cdy) * ((adx * bdytail + bdy * adxtail) - (ady * bdxtail + bdx * adytail))
				+ 2 * (cdx * cdxtail + cdy * cdytail) * (adx * bdy - ady * bdx));

		if (Math.abs(det) >= errbound) {
			return det;
		}

		if (bdxtail != 0 || bdytail != 0 || cdxtail != 0 || cdytail != 0) {
			squareSum(adx, ady, ic.aa);
		}
		if (cdxtail != 0 || cdytail != 0 || adxtail != 0 || adytail != 0) {
			squareSum(bdx, bdy, ic.bb);
		}
		if (adxtail != 0 || adytail != 0 || bdxtail != 0 || bdytail != 0) {
			squareSum(cdx, cdy, ic.cc);
		}

		if (adxtail != 0) {
			axtbclen = scale(4, ic.bc, adxtail, ic.axtbc);
			ic.acc.add(sum_three(scale(axtbclen, ic.axtbc, 2 * adx, ic._16), ic._16,
				scale(scale(4, ic.cc, adxtail, ic._8), ic._8, bdy, ic._16b), ic._16b,
				scale(scale(4, ic.bb, adxtail, ic._8), ic._8, -cdy, ic._16c), ic._16c, ic._32, ic._48), ic._48);
		}
		if (adytail != 0) {
			aytbclen = scale(4, ic.bc, adytail, ic.aytbc);
			ic.acc.add(sum_three(scale(aytbclen, ic.aytbc, 2 * ady, ic._16), ic._16,
				scale(scale(4, ic.bb, adytail, ic._8), ic._8, cdx, ic._16b), ic._16b,
				scale(scale(4, ic.cc, adytail, ic._8), ic._8, -bdx, ic._16c), ic._16c, ic._32, ic._48), ic._48);
		}
		if (bdxtail != 0) {
			bxtcalen = scale(4, ic.ca, bdxtail, ic.bxtca);
			ic.acc.add(sum_three(scale(bxtcalen, ic.bxtca, 2 * bdx, ic._16), ic._16,
				scale(scale(4, ic.aa, bdxtail, ic._8), ic._8, cdy, ic._16b), ic._16b,
				scale(scale(4, ic.cc, bdxtail, ic._8), ic._8, -ady, ic._16c), ic._16c, ic._32, ic._48), ic._48);
		}
		if (bdytail != 0) {
			bytcalen = scale(4, ic.ca, bdytail, ic.bytca);
			ic.acc.add(sum_three(scale(bytcalen, ic.bytca, 2 * bdy, ic._16), ic._16,
				scale(scale(4, ic.cc, bdytail, ic._8), ic._8, adx, ic._16b), ic._16b,
				scale(scale(4, ic.aa, bdytail, ic._8), ic._8, -cdx, ic._16c), ic._16c, ic._32, ic._48), ic._48);
		}
		if (cdxtail != 0) {
			cxtablen = scale(4, ic.ab, cdxtail, ic.cxtab);
			ic.acc.add(sum_three(scale(cxtablen, ic.cxtab, 2 * cdx, ic._16), ic._16,
				scale(scale(4, ic.bb, cdxtail, ic._8), ic._8, ady, ic._16b), ic._16b,
				scale(scale(4, ic.aa, cdxtail, ic._8), ic._8, -bdy, ic._16c), ic._16c, ic._32, ic._48), ic._48);
		}
		if (cdytail != 0) {
			cytablen = scale(4, ic.ab, cdytail, ic.cytab);
			ic.acc.add(sum_three(scale(cytablen, ic.cytab, 2 * cdy, ic._16), ic._16,
				scale(scale(4, ic.aa, cdytail, ic._8), ic._8, bdx, ic._16b), ic._16b,
				scale(scale(4, ic.bb, cdytail, ic._8), ic._8, -adx, ic._16c), ic._16c, ic._32, ic._48), ic._48);
		}

		if (adxtail != 0 || adytail != 0) {
			if (bdxtail != 0 || bdytail != 0 || cdxtail != 0 || cdytail != 0) {
				twoProductSum(bdxtail, cdy, bdx, cdytail, ic.u);
				twoProductSum(cdxtail, -bdy, cdx, -bdytail, ic.v);
				bctlen = sum(4, ic.u, 4, ic.v, ic.bct);
				crossProduct(bdxtail, bdytail, cdxtail, cdytail, ic.bctt);
				bcttlen = 4;
			} else {
				ic.bct[0] = 0;
				bctlen = 1;
				ic.bctt[0] = 0;
				bcttlen = 1;
			}
			if (adxtail != 0) {
				int len = scale(bctlen, ic.bct, adxtail, ic._16c);
				ic.acc.add(
					sum(scale(axtbclen, ic.axtbc, adxtail, ic._16), ic._16, scale(len, ic._16c, 2 * adx, ic._32), ic._32, ic._48),
					ic._48);

				int len2 = scale(bcttlen, ic.bctt, adxtail, ic._8);
				ic.acc.add(sum_three(scale(len2, ic._8, 2 * adx, ic._16), ic._16, scale(len2, ic._8, adxtail, ic._16b), ic._16b,
					scale(len, ic._16c, adxtail, ic._32), ic._32, ic._32b, ic._64), ic._64);

				if (bdytail != 0) {
					ic.acc.add(scale(scale(4, ic.cc, adxtail, ic._8), ic._8, bdytail, ic._16), ic._16);
				}
				if (cdytail != 0) {
					ic.acc.add(scale(scale(4, ic.bb, -adxtail, ic._8), ic._8, cdytail, ic._16), ic._16);
				}
			}
			if (adytail != 0) {
				int len = scale(bctlen, ic.bct, adytail, ic._16c);
				ic.acc.add(
					sum(scale(aytbclen, ic.aytbc, adytail, ic._16), ic._16, scale(len, ic._16c, 2 * ady, ic._32), ic._32, ic._48),
					ic._48);

				int len2 = scale(bcttlen, ic.bctt, adytail, ic._8);
				ic.acc.add(sum_three(scale(len2, ic._8, 2 * ady, ic._16), ic._16, scale(len2, ic._8, adytail, ic._16b), ic._16b,
					scale(len, ic._16c, adytail, ic._32), ic._32, ic._32b, ic._64), ic._64);
			}
		}
		if (bdxtail != 0 || bdytail != 0) {
			if (cdxtail != 0 || cdytail != 0 || adxtail != 0 || adytail != 0) {
				twoProductSum(cdxtail, ady, cdx, adytail, ic.u);
				twoProductSum(adxtail, -cdy, adx, -cdytail, ic.v);
				catlen = sum(4, ic.u, 4, ic.v, ic.cat);
				crossProduct(cdxtail, cdytail, adxtail, adytail, ic.catt);
				cattlen = 4;
			} else {
				ic.cat[0] = 0;
				catlen = 1;
				ic.catt[0] = 0;
				cattlen = 1;
			}
			if (bdxtail != 0) {
				int len = scale(catlen, ic.cat, bdxtail, ic._16c);
				ic.acc.add(
					sum(scale(bxtcalen, ic.bxtca, bdxtail, ic._16), ic._16, scale(len, ic._16c, 2 * bdx, ic._32), ic._32, ic._48),
					ic._48);

				int len2 = scale(cattlen, ic.catt, bdxtail, ic._8);
				ic.acc.add(sum_three(scale(len2, ic._8, 2 * bdx, ic._16), ic._16, scale(len2, ic._8, bdxtail, ic._16b), ic._16b,
					scale(len, ic._16c, bdxtail, ic._32), ic._32, ic._32b, ic._64), ic._64);

				if (cdytail != 0) {
					ic.acc.add(scale(scale(4, ic.aa, bdxtail, ic._8), ic._8, cdytail, ic._16), ic._16);
				}
				if (adytail != 0) {
					ic.acc.add(scale(scale(4, ic.cc, -bdxtail, ic._8), ic._8, adytail, ic._16), ic._16);
				}
			}
			if (bdytail != 0) {
				int len = scale(catlen, ic.cat, bdytail, ic._16c);
				ic.acc.add(
					sum(scale(bytcalen, ic.bytca, bdytail, ic._16), ic._16, scale(len, ic._16c, 2 * bdy, ic._32), ic._32, ic._48),
					ic._48);

				int len2 = scale(cattlen, ic.catt, bdytail, ic._8);
				ic.acc.add(sum_three(scale(len2, ic._8, 2 * bdy, ic._16), ic._16, scale(len2, ic._8, bdytail, ic._16b), ic._16b,
					scale(len, ic._16c, bdytail, ic._32), ic._32, ic._32b, ic._64), ic._64);
			}
		}
		if (cdxtail != 0 || cdytail != 0) {
			if (adxtail != 0 || adytail != 0 || bdxtail != 0 || bdytail != 0) {
				twoProductSum(adxtail, bdy, adx, bdytail, ic.u);
				twoProductSum(bdxtail, -ady, bdx, -adytail, ic.v);
				abtlen = sum(4, ic.u, 4, ic.v, ic.abt);
				crossProduct(adxtail, adytail, bdxtail, bdytail, ic.abtt);
				abttlen = 4;
			} else {
				ic.abt[0] = 0;
				abtlen = 1;
				ic.abtt[0] = 0;
				abttlen = 1;
			}
			if (cdxtail != 0) {
				int len = scale(abtlen, ic.abt, cdxtail, ic._16c);
				ic.acc.add(
					sum(scale(cxtablen, ic.cxtab, cdxtail, ic._16), ic._16, scale(len, ic._16c, 2 * cdx, ic._32), ic._32, ic._48),
					ic._48);

				int len2 = scale(abttlen, ic.abtt, cdxtail, ic._8);
				ic.acc.add(sum_three(scale(len2, ic._8, 2 * cdx, ic._16), ic._16, scale(len2, ic._8, cdxtail, ic._16b), ic._16b,
					scale(len, ic._16c, cdxtail, ic._32), ic._32, ic._32b, ic._64), ic._64);

				if (adytail != 0) {
					ic.acc.add(scale(scale(4, ic.bb, cdxtail, ic._8), ic._8, adytail, ic._16), ic._16);
				}
				if (bdytail != 0) {
					ic.acc.add(scale(scale(4, ic.aa, -cdxtail, ic._8), ic._8, bdytail, ic._16), ic._16);
				}
			}
			if (cdytail != 0) {
				int len = scale(abtlen, ic.abt, cdytail, ic._16c);
				ic.acc.add(
					sum(scale(cytablen, ic.cytab, cdytail, ic._16), ic._16, scale(len, ic._16c, 2 * cdy, ic._32), ic._32, ic._48),
					ic._48);

				int len2 = scale(abttlen, ic.abtt, cdytail, ic._8);
				ic.acc.add(sum_three(scale(len2, ic._8, 2 * cdy, ic._16), ic._16, scale(len2, ic._8, cdytail, ic._16b), ic._16b,
					scale(len, ic._16c, cdytail, ic._32), ic._32, ic._32b, ic._64), ic._64);
			}
		}

		return ic.acc.fin[ic.acc.len - 1];
	}

	/** 2D in-circle test. With {@code a}, {@code b}, {@code c} given in counter-clockwise order (assuming y-up), returns a
	 * positive value when {@code d} lies strictly inside the circle through the three points, negative when outside, zero when
	 * exactly on it. Only the sign is guaranteed exact. */
	public double incircle (double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
		double adx = ax - dx;
		double bdx = bx - dx;
		double cdx = cx - dx;
		double ady = ay - dy;
		double bdy = by - dy;
		double cdy = cy - dy;

		double bdxcdy = bdx * cdy;
		double cdxbdy = cdx * bdy;
		double alift = adx * adx + ady * ady;

		double cdxady = cdx * ady;
		double adxcdy = adx * cdy;
		double blift = bdx * bdx + bdy * bdy;

		double adxbdy = adx * bdy;
		double bdxady = bdx * ady;
		double clift = cdx * cdx + cdy * cdy;

		double det = alift * (bdxcdy - cdxbdy) + blift * (cdxady - adxcdy) + clift * (adxbdy - bdxady);

		double permanent = (Math.abs(bdxcdy) + Math.abs(cdxbdy)) * alift + (Math.abs(cdxady) + Math.abs(adxcdy)) * blift
			+ (Math.abs(adxbdy) + Math.abs(bdxady)) * clift;

		double errbound = iccerrboundA * permanent;

		if (det > errbound || -det > errbound) {
			return det;
		}
		return incircleadapt(ax, ay, bx, by, cx, cy, dx, dy, permanent);
	}

	/** Non-robust counterpart of {@link #incircle}; the sign may be wrong on near-degenerate inputs. */
	public double incirclefast (double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
		double adx = ax - dx;
		double ady = ay - dy;
		double bdx = bx - dx;
		double bdy = by - dy;
		double cdx = cx - dx;
		double cdy = cy - dy;

		double abdet = adx * bdy - bdx * ady;
		double bcdet = bdx * cdy - cdx * bdy;
		double cadet = cdx * ady - adx * cdy;
		double alift = adx * adx + ady * ady;
		double blift = bdx * bdx + bdy * bdy;
		double clift = cdx * cdx + cdy * cdy;

		return alift * bcdet + blift * cadet + clift * abdet;
	}

	// ===== insphere ========================================================

	private int sum_three_scale (double[] a, double[] b, double[] cc, double az, double bz, double cz, double[] out) {
		if (s3 == null) s3 = new SumThreeScratch();
		return sum_three(scale(4, a, az, s3._8), s3._8, scale(4, b, bz, s3._8b), s3._8b, scale(4, cc, cz, s3._8c), s3._8c, s3._16,
			out);
	}

	private int liftexact (int alen, double[] a, int blen, double[] b, int clen, double[] cc, int dlen, double[] d, double x,
		double y, double z, double[] out) {
		if (lex == null) lex = new LiftExactScratch();

		int len = sum(sum(alen, a, blen, b, lex._48), lex._48, negate(sum(clen, cc, dlen, d, lex._48b), lex._48b), lex._48b,
			lex._96);

		return sum_three(scale(scale(len, lex._96, x, lex._192), lex._192, x, lex._384x), lex._384x,
			scale(scale(len, lex._96, y, lex._192), lex._192, y, lex._384y), lex._384y,
			scale(scale(len, lex._96, z, lex._192), lex._192, z, lex._384z), lex._384z, lex._768, out);
	}

	private double insphereexact (double ax, double ay, double az, double bx, double by, double bz, double cx, double cy,
		double cz, double dx, double dy, double dz, double ex, double ey, double ez) {
		if (iex == null) iex = new InsphereExactScratch();

		crossProduct(ax, ay, bx, by, iex.ab);
		crossProduct(bx, by, cx, cy, iex.bc);
		crossProduct(cx, cy, dx, dy, iex.cd);
		crossProduct(dx, dy, ex, ey, iex.de);
		crossProduct(ex, ey, ax, ay, iex.ea);
		crossProduct(ax, ay, cx, cy, iex.ac);
		crossProduct(bx, by, dx, dy, iex.bd);
		crossProduct(cx, cy, ex, ey, iex.ce);
		crossProduct(dx, dy, ax, ay, iex.da);
		crossProduct(ex, ey, bx, by, iex.eb);

		int abclen = sum_three_scale(iex.ab, iex.bc, iex.ac, cz, az, -bz, iex.abc);
		int bcdlen = sum_three_scale(iex.bc, iex.cd, iex.bd, dz, bz, -cz, iex.bcd);
		int cdelen = sum_three_scale(iex.cd, iex.de, iex.ce, ez, cz, -dz, iex.cde);
		int dealen = sum_three_scale(iex.de, iex.ea, iex.da, az, dz, -ez, iex.dea);
		int eablen = sum_three_scale(iex.ea, iex.ab, iex.eb, bz, ez, -az, iex.eab);
		int abdlen = sum_three_scale(iex.ab, iex.bd, iex.da, dz, az, bz, iex.abd);
		int bcelen = sum_three_scale(iex.bc, iex.ce, iex.eb, ez, bz, cz, iex.bce);
		int cdalen = sum_three_scale(iex.cd, iex.da, iex.ac, az, cz, dz, iex.cda);
		int deblen = sum_three_scale(iex.de, iex.eb, iex.bd, bz, dz, ez, iex.deb);
		int eaclen = sum_three_scale(iex.ea, iex.ac, iex.ce, cz, ez, az, iex.eac);

		int deterlen = sum_three(
			liftexact(cdelen, iex.cde, bcelen, iex.bce, deblen, iex.deb, bcdlen, iex.bcd, ax, ay, az, iex.adet), iex.adet,
			liftexact(dealen, iex.dea, cdalen, iex.cda, eaclen, iex.eac, cdelen, iex.cde, bx, by, bz, iex.bdet), iex.bdet,
			sum_three(liftexact(eablen, iex.eab, deblen, iex.deb, abdlen, iex.abd, dealen, iex.dea, cx, cy, cz, iex.cdet), iex.cdet,
				liftexact(abclen, iex.abc, eaclen, iex.eac, bcelen, iex.bce, eablen, iex.eab, dx, dy, dz, iex.ddet), iex.ddet,
				liftexact(bcdlen, iex.bcd, abdlen, iex.abd, cdalen, iex.cda, abclen, iex.abc, ex, ey, ez, iex.edet), iex.edet,
				iex.cddet, iex.cdedet),
			iex.cdedet, iex.abdet, iex.deter);

		return iex.deter[deterlen - 1];
	}

	private int liftadapt (double[] a, double[] b, double[] cc, double az, double bz, double cz, double x, double y, double z,
		double[] out) {
		if (lad == null) lad = new LiftAdaptScratch();
		int len = sum_three_scale(a, b, cc, az, bz, cz, lad._24);
		return sum_three(scale(scale(len, lad._24, x, lad._48), lad._48, x, lad.xdet), lad.xdet,
			scale(scale(len, lad._24, y, lad._48), lad._48, y, lad.ydet), lad.ydet,
			scale(scale(len, lad._24, z, lad._48), lad._48, z, lad.zdet), lad.zdet, lad._192, out);
	}

	private double insphereadapt (double ax, double ay, double az, double bx, double by, double bz, double cx, double cy,
		double cz, double dx, double dy, double dz, double ex, double ey, double ez, double permanent) {
		double ab3, bc3, cd3, da3, ac3, bd3;

		double aextail, bextail, cextail, dextail;
		double aeytail, beytail, ceytail, deytail;
		double aeztail, beztail, ceztail, deztail;

		if (iad == null) iad = new InsphereAdaptScratch();

		double aex = ax - ex;
		double bex = bx - ex;
		double cex = cx - ex;
		double dex = dx - ex;
		double aey = ay - ey;
		double bey = by - ey;
		double cey = cy - ey;
		double dey = dy - ey;
		double aez = az - ez;
		double bez = bz - ez;
		double cez = cz - ez;
		double dez = dz - ez;

		ab3 = crossProduct(aex, aey, bex, bey, iad.ab);
		bc3 = crossProduct(bex, bey, cex, cey, iad.bc);
		cd3 = crossProduct(cex, cey, dex, dey, iad.cd);
		da3 = crossProduct(dex, dey, aex, aey, iad.da);
		ac3 = crossProduct(aex, aey, cex, cey, iad.ac);
		bd3 = crossProduct(bex, bey, dex, dey, iad.bd);

		int finlen = sum(
			sum(negate(liftadapt(iad.bc, iad.cd, iad.bd, dez, bez, -cez, aex, aey, aez, iad.adet), iad.adet), iad.adet,
				liftadapt(iad.cd, iad.da, iad.ac, aez, cez, dez, bex, bey, bez, iad.bdet), iad.bdet, iad.abdet),
			iad.abdet, sum(negate(liftadapt(iad.da, iad.ab, iad.bd, bez, dez, aez, cex, cey, cez, iad.cdet), iad.cdet), iad.cdet,
				liftadapt(iad.ab, iad.bc, iad.ac, cez, aez, -bez, dex, dey, dez, iad.ddet), iad.ddet, iad.cddet),
			iad.cddet, iad.fin);

		double det = estimate(finlen, iad.fin);
		double errbound = isperrboundB * permanent;
		if (Math.abs(det) >= errbound) {
			return det;
		}

		aextail = twoDiffTail(ax, ex, aex);
		aeytail = twoDiffTail(ay, ey, aey);
		aeztail = twoDiffTail(az, ez, aez);
		bextail = twoDiffTail(bx, ex, bex);
		beytail = twoDiffTail(by, ey, bey);
		beztail = twoDiffTail(bz, ez, bez);
		cextail = twoDiffTail(cx, ex, cex);
		ceytail = twoDiffTail(cy, ey, cey);
		ceztail = twoDiffTail(cz, ez, cez);
		dextail = twoDiffTail(dx, ex, dex);
		deytail = twoDiffTail(dy, ey, dey);
		deztail = twoDiffTail(dz, ez, dez);
		if (aextail == 0 && aeytail == 0 && aeztail == 0 && bextail == 0 && beytail == 0 && beztail == 0 && cextail == 0
			&& ceytail == 0 && ceztail == 0 && dextail == 0 && deytail == 0 && deztail == 0) {
			return det;
		}

		errbound = isperrboundC * permanent + resulterrbound * Math.abs(det);

		double abeps = (aex * beytail + bey * aextail) - (aey * bextail + bex * aeytail);
		double bceps = (bex * ceytail + cey * bextail) - (bey * cextail + cex * beytail);
		double cdeps = (cex * deytail + dey * cextail) - (cey * dextail + dex * ceytail);
		double daeps = (dex * aeytail + aey * dextail) - (dey * aextail + aex * deytail);
		double aceps = (aex * ceytail + cey * aextail) - (aey * cextail + cex * aeytail);
		double bdeps = (bex * deytail + dey * bextail) - (bey * dextail + dex * beytail);
		det += (((bex * bex + bey * bey + bez * bez)
			* ((cez * daeps + dez * aceps + aez * cdeps) + (ceztail * da3 + deztail * ac3 + aeztail * cd3))
			+ (dex * dex + dey * dey + dez * dez)
				* ((aez * bceps - bez * aceps + cez * abeps) + (aeztail * bc3 - beztail * ac3 + ceztail * ab3)))
			- ((aex * aex + aey * aey + aez * aez)
				* ((bez * cdeps - cez * bdeps + dez * bceps) + (beztail * cd3 - ceztail * bd3 + deztail * bc3))
				+ (cex * cex + cey * cey + cez * cez)
					* ((dez * abeps + aez * bdeps + bez * daeps) + (deztail * ab3 + aeztail * bd3 + beztail * da3))))
			+ 2 * (((bex * bextail + bey * beytail + bez * beztail) * (cez * da3 + dez * ac3 + aez * cd3)
				+ (dex * dextail + dey * deytail + dez * deztail) * (aez * bc3 - bez * ac3 + cez * ab3))
				- ((aex * aextail + aey * aeytail + aez * aeztail) * (bez * cd3 - cez * bd3 + dez * bc3)
					+ (cex * cextail + cey * ceytail + cez * ceztail) * (dez * ab3 + aez * bd3 + bez * da3)));

		if (Math.abs(det) >= errbound) {
			return det;
		}

		return insphereexact(ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz, ex, ey, ez);
	}

	/** 3D in-sphere test. With {@code a}, {@code b}, {@code c}, {@code d} ordered so that {@link #orient3d orient3d(a, b, c, d)}
	 * is positive (i.e., {@code a}, {@code b}, {@code c} appear counter-clockwise viewed from {@code d}, in a right-handed
	 * coordinate system), returns a positive value when {@code e} lies strictly inside the sphere through the four points,
	 * negative when outside, zero when exactly on it. Only the sign is guaranteed exact. */
	public double insphere (double ax, double ay, double az, double bx, double by, double bz, double cx, double cy, double cz,
		double dx, double dy, double dz, double ex, double ey, double ez) {
		double aex = ax - ex;
		double bex = bx - ex;
		double cex = cx - ex;
		double dex = dx - ex;
		double aey = ay - ey;
		double bey = by - ey;
		double cey = cy - ey;
		double dey = dy - ey;
		double aez = az - ez;
		double bez = bz - ez;
		double cez = cz - ez;
		double dez = dz - ez;

		double aexbey = aex * bey;
		double bexaey = bex * aey;
		double ab = aexbey - bexaey;
		double bexcey = bex * cey;
		double cexbey = cex * bey;
		double bc = bexcey - cexbey;
		double cexdey = cex * dey;
		double dexcey = dex * cey;
		double cd = cexdey - dexcey;
		double dexaey = dex * aey;
		double aexdey = aex * dey;
		double da = dexaey - aexdey;
		double aexcey = aex * cey;
		double cexaey = cex * aey;
		double ac = aexcey - cexaey;
		double bexdey = bex * dey;
		double dexbey = dex * bey;
		double bd = bexdey - dexbey;

		double alift = aex * aex + aey * aey + aez * aez;
		double blift = bex * bex + bey * bey + bez * bez;
		double clift = cex * cex + cey * cey + cez * cez;
		double dlift = dex * dex + dey * dey + dez * dez;

		double det = (clift * (dez * ab + aez * bd + bez * da) - dlift * (aez * bc - bez * ac + cez * ab))
			+ (alift * (bez * cd - cez * bd + dez * bc) - blift * (cez * da + dez * ac + aez * cd));

		double aezplus = Math.abs(aez);
		double bezplus = Math.abs(bez);
		double cezplus = Math.abs(cez);
		double dezplus = Math.abs(dez);
		double aexbeyplus = Math.abs(aexbey) + Math.abs(bexaey);
		double bexceyplus = Math.abs(bexcey) + Math.abs(cexbey);
		double cexdeyplus = Math.abs(cexdey) + Math.abs(dexcey);
		double dexaeyplus = Math.abs(dexaey) + Math.abs(aexdey);
		double aexceyplus = Math.abs(aexcey) + Math.abs(cexaey);
		double bexdeyplus = Math.abs(bexdey) + Math.abs(dexbey);
		double permanent = (cexdeyplus * bezplus + bexdeyplus * cezplus + bexceyplus * dezplus) * alift
			+ (dexaeyplus * cezplus + aexceyplus * dezplus + cexdeyplus * aezplus) * blift
			+ (aexbeyplus * dezplus + bexdeyplus * aezplus + dexaeyplus * bezplus) * clift
			+ (bexceyplus * aezplus + aexceyplus * bezplus + aexbeyplus * cezplus) * dlift;

		double errbound = isperrboundA * permanent;
		if (det > errbound || -det > errbound) {
			return det;
		}
		return -insphereadapt(ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz, ex, ey, ez, permanent);
	}

	/** Non-robust counterpart of {@link #insphere}; the sign may be wrong on near-degenerate inputs. */
	public double inspherefast (double ax, double ay, double az, double bx, double by, double bz, double cx, double cy, double cz,
		double dx, double dy, double dz, double ex, double ey, double ez) {
		double aex = ax - ex;
		double bex = bx - ex;
		double cex = cx - ex;
		double dex = dx - ex;
		double aey = ay - ey;
		double bey = by - ey;
		double cey = cy - ey;
		double dey = dy - ey;
		double aez = az - ez;
		double bez = bz - ez;
		double cez = cz - ez;
		double dez = dz - ez;

		double ab = aex * bey - bex * aey;
		double bc = bex * cey - cex * bey;
		double cd = cex * dey - dex * cey;
		double da = dex * aey - aex * dey;
		double ac = aex * cey - cex * aey;
		double bd = bex * dey - dex * bey;

		double abc = aez * bc - bez * ac + cez * ab;
		double bcd = bez * cd - cez * bd + dez * bc;
		double cda = cez * da + dez * ac + aez * cd;
		double dab = dez * ab + aez * bd + bez * da;

		double alift = aex * aex + aey * aey + aez * aez;
		double blift = bex * bex + bey * bey + bez * bez;
		double clift = cex * cex + cey * cey + cez * cez;
		double dlift = dex * dex + dey * dey + dez * dez;

		return (clift * dab - dlift * abc) + (alift * bcd - blift * cda);
	}
}
