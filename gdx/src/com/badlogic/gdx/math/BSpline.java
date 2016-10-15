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

import com.badlogic.gdx.utils.Array;

/** @author Xoppa */
public class BSpline<T extends Vector<T>> implements Path<T> {
	private final static float d6 = 1f / 6f;

	/** Calculates the cubic b-spline value for the given position (t).
	 * @param out The Vector to set to the result.
	 * @param t The position (0<=t<=1) on the spline
	 * @param points The control points
	 * @param continuous If true the b-spline restarts at 0 when reaching 1
	 * @param tmp A temporary vector used for the calculation
	 * @return The value of out */
	public static <T extends Vector<T>> T cubic (final T out, final float t, final T[] points, final boolean continuous,
		final T tmp) {
		final int n = continuous ? points.length : points.length - 3;
		float u = t * n;
		int i = (t >= 1f) ? (n - 1) : (int)u;
		u -= i;
		return cubic(out, i, u, points, continuous, tmp);
	}

	/** Calculates the cubic b-spline derivative for the given position (t).
	 * @param out The Vector to set to the result.
	 * @param t The position (0<=t<=1) on the spline
	 * @param points The control points
	 * @param continuous If true the b-spline restarts at 0 when reaching 1
	 * @param tmp A temporary vector used for the calculation
	 * @return The value of out */
	public static <T extends Vector<T>> T cubic_derivative (final T out, final float t, final T[] points,
		final boolean continuous, final T tmp) {
		final int n = continuous ? points.length : points.length - 3;
		float u = t * n;
		int i = (t >= 1f) ? (n - 1) : (int)u;
		u -= i;
		return cubic(out, i, u, points, continuous, tmp);
	}

	/** Calculates the cubic b-spline value for the given span (i) at the given position (u).
	 * @param out The Vector to set to the result.
	 * @param i The span (0<=i<spanCount) spanCount = continuous ? points.length : points.length - 3 (cubic degree)
	 * @param u The position (0<=u<=1) on the span
	 * @param points The control points
	 * @param continuous If true the b-spline restarts at 0 when reaching 1
	 * @param tmp A temporary vector used for the calculation
	 * @return The value of out */
	public static <T extends Vector<T>> T cubic (final T out, final int i, final float u, final T[] points,
		final boolean continuous, final T tmp) {
		final int n = points.length;
		final float dt = 1f - u;
		final float t2 = u * u;
		final float t3 = t2 * u;
		out.set(points[i]).scl((3f * t3 - 6f * t2 + 4f) * d6);
		if (continuous || i > 0) out.add(tmp.set(points[(n + i - 1) % n]).scl(dt * dt * dt * d6));
		if (continuous || i < (n - 1)) out.add(tmp.set(points[(i + 1) % n]).scl((-3f * t3 + 3f * t2 + 3f * u + 1f) * d6));
		if (continuous || i < (n - 2)) out.add(tmp.set(points[(i + 2) % n]).scl(t3 * d6));
		return out;
	}

	/** Calculates the cubic b-spline derivative for the given span (i) at the given position (u).
	 * @param out The Vector to set to the result.
	 * @param i The span (0<=i<spanCount) spanCount = continuous ? points.length : points.length - 3 (cubic degree)
	 * @param u The position (0<=u<=1) on the span
	 * @param points The control points
	 * @param continuous If true the b-spline restarts at 0 when reaching 1
	 * @param tmp A temporary vector used for the calculation
	 * @return The value of out */
	public static <T extends Vector<T>> T cubic_derivative (final T out, final int i, final float u, final T[] points,
		final boolean continuous, final T tmp) {
		final int n = points.length;
		final float dt = 1f - u;
		final float t2 = u * u;
		final float t3 = t2 * u;
		out.set(points[i]).scl(1.5f * t2 - 2 * u);
		if (continuous || i > 0) out.add(tmp.set(points[(n + i - 1) % n]).scl(-0.5f * dt * dt));
		if (continuous || i < (n - 1)) out.add(tmp.set(points[(i + 1) % n]).scl(-1.5f * t2 + u + 0.5f));
		if (continuous || i < (n - 2)) out.add(tmp.set(points[(i + 2) % n]).scl(0.5f * t2));
		return out;
	}

	/** Calculates the n-degree b-spline value for the given position (t).
	 * @param out The Vector to set to the result.
	 * @param t The position (0<=t<=1) on the spline
	 * @param points The control points
	 * @param degree The degree of the b-spline
	 * @param continuous If true the b-spline restarts at 0 when reaching 1
	 * @param tmp A temporary vector used for the calculation
	 * @return The value of out */
	public static <T extends Vector<T>> T calculate (final T out, final float t, final T[] points, final int degree,
		final boolean continuous, final T tmp) {
		final int n = continuous ? points.length : points.length - degree;
		float u = t * n;
		int i = (t >= 1f) ? (n - 1) : (int)u;
		u -= i;
		return calculate(out, i, u, points, degree, continuous, tmp);
	}

	/** Calculates the n-degree b-spline derivative for the given position (t).
	 * @param out The Vector to set to the result.
	 * @param t The position (0<=t<=1) on the spline
	 * @param points The control points
	 * @param degree The degree of the b-spline
	 * @param continuous If true the b-spline restarts at 0 when reaching 1
	 * @param tmp A temporary vector used for the calculation
	 * @return The value of out */
	public static <T extends Vector<T>> T derivative (final T out, final float t, final T[] points, final int degree,
		final boolean continuous, final T tmp) {
		final int n = continuous ? points.length : points.length - degree;
		float u = t * n;
		int i = (t >= 1f) ? (n - 1) : (int)u;
		u -= i;
		return derivative(out, i, u, points, degree, continuous, tmp);
	}

	/** Calculates the n-degree b-spline value for the given span (i) at the given position (u).
	 * @param out The Vector to set to the result.
	 * @param i The span (0<=i<spanCount) spanCount = continuous ? points.length : points.length - degree
	 * @param u The position (0<=u<=1) on the span
	 * @param points The control points
	 * @param degree The degree of the b-spline
	 * @param continuous If true the b-spline restarts at 0 when reaching 1
	 * @param tmp A temporary vector used for the calculation
	 * @return The value of out */
	public static <T extends Vector<T>> T calculate (final T out, final int i, final float u, final T[] points, final int degree,
		final boolean continuous, final T tmp) {
		switch (degree) {
		case 3:
			return cubic(out, i, u, points, continuous, tmp);
		}
		return out;
	}

	/** Calculates the n-degree b-spline derivative for the given span (i) at the given position (u).
	 * @param out The Vector to set to the result.
	 * @param i The span (0<=i<spanCount) spanCount = continuous ? points.length : points.length - degree
	 * @param u The position (0<=u<=1) on the span
	 * @param points The control points
	 * @param degree The degree of the b-spline
	 * @param continuous If true the b-spline restarts at 0 when reaching 1
	 * @param tmp A temporary vector used for the calculation
	 * @return The value of out */
	public static <T extends Vector<T>> T derivative (final T out, final int i, final float u, final T[] points, final int degree,
		final boolean continuous, final T tmp) {
		switch (degree) {
		case 3:
			return cubic_derivative(out, i, u, points, continuous, tmp);
		}
		return out;
	}

	public T[] controlPoints;
	public Array<T> knots;
	public int degree;
	public boolean continuous;
	public int spanCount;
	private T tmp;
	private T tmp2;
	private T tmp3;

	public BSpline () {
	}

	public BSpline (final T[] controlPoints, final int degree, final boolean continuous) {
		set(controlPoints, degree, continuous);
	}

	public BSpline set (final T[] controlPoints, final int degree, final boolean continuous) {
		if (tmp == null) tmp = controlPoints[0].cpy();
		if (tmp2 == null) tmp2 = controlPoints[0].cpy();
		if (tmp3 == null) tmp3 = controlPoints[0].cpy();
		this.controlPoints = controlPoints;
		this.degree = degree;
		this.continuous = continuous;
		this.spanCount = continuous ? controlPoints.length : controlPoints.length - degree;
		if (knots == null)
			knots = new Array<T>(spanCount);
		else {
			knots.clear();
			knots.ensureCapacity(spanCount);
		}
		for (int i = 0; i < spanCount; i++)
			knots.add(calculate(controlPoints[0].cpy(), continuous ? i : (int)(i + 0.5f * degree), 0f, controlPoints, degree,
				continuous, tmp));
		return this;
	}

	@Override
	public T valueAt (T out, float t) {
		final int n = spanCount;
		float u = t * n;
		int i = (t >= 1f) ? (n - 1) : (int)u;
		u -= i;
		return valueAt(out, i, u);
	}

	/** @return The value of the spline at position u of the specified span */
	public T valueAt (final T out, final int span, final float u) {
		return calculate(out, continuous ? span : (span + (int)(degree * 0.5f)), u, controlPoints, degree, continuous, tmp);
	}

	@Override
	public T derivativeAt (final T out, final float t) {
		final int n = spanCount;
		float u = t * n;
		int i = (t >= 1f) ? (n - 1) : (int)u;
		u -= i;
		return derivativeAt(out, i, u);
	}

	/** @return The derivative of the spline at position u of the specified span */
	public T derivativeAt (final T out, final int span, final float u) {
		return derivative(out, continuous ? span : (span + (int)(degree * 0.5f)), u, controlPoints, degree, continuous, tmp);
	}

	/** @return The span closest to the specified value */
	public int nearest (final T in) {
		return nearest(in, 0, spanCount);
	}

	/** @return The span closest to the specified value, restricting to the specified spans. */
	public int nearest (final T in, int start, final int count) {
		while (start < 0)
			start += spanCount;
		int result = start % spanCount;
		float dst = in.dst2(knots.get(result));
		for (int i = 1; i < count; i++) {
			final int idx = (start + i) % spanCount;
			final float d = in.dst2(knots.get(idx));
			if (d < dst) {
				dst = d;
				result = idx;
			}
		}
		return result;
	}

	@Override
	public float approximate (T v) {
		return approximate(v, nearest(v));
	}

	public float approximate (final T in, int start, final int count) {
		return approximate(in, nearest(in, start, count));
	}

	public float approximate (final T in, final int near) {
		int n = near;
		final T nearest = knots.get(n);
		final T previous = knots.get(n > 0 ? n - 1 : spanCount - 1);
		final T next = knots.get((n + 1) % spanCount);
		final float dstPrev2 = in.dst2(previous);
		final float dstNext2 = in.dst2(next);
		T P1, P2, P3;
		if (dstNext2 < dstPrev2) {
			P1 = nearest;
			P2 = next;
			P3 = in;
		} else {
			P1 = previous;
			P2 = nearest;
			P3 = in;
			n = n > 0 ? n - 1 : spanCount - 1;
		}
		float L1Sqr = P1.dst2(P2);
		float L2Sqr = P3.dst2(P2);
		float L3Sqr = P3.dst2(P1);
		float L1 = (float)Math.sqrt(L1Sqr);
		float s = (L2Sqr + L1Sqr - L3Sqr) / (2 * L1);
		float u = MathUtils.clamp((L1 - s) / L1, 0f, 1f);
		return (n + u) / spanCount;
	}

	@Override
	public float locate (T v) {
		// TODO Add a precise method
		return approximate(v);
	}

	@Override
	public float approxLength (int samples) {
		float tempLength = 0;
		for (int i = 0; i < samples; ++i) {
			tmp2.set(tmp3);
			valueAt(tmp3, (i) / ((float)samples - 1));
			if (i > 0) tempLength += tmp2.dst(tmp3);
		}
		return tempLength;
	}
}
