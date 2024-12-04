
package com.badlogic.gdx.math;

import org.junit.Assert;
import org.junit.Test;

public class BSplineTest {

	@Test
	public void testCubicSplineNonContinuous () {
		Vector3[] controlPoints = {new Vector3(0, 0, 0), new Vector3(1, 1, 0), new Vector3(2, 0, 0), new Vector3(3, -1, 0)};
		BSpline<Vector3> spline = new BSpline<>(controlPoints, 3, false);

		Vector3 result = new Vector3();
		spline.valueAt(result, 0.5f);

		Vector3 expected = new Vector3(1.5f, 0.5f, 0);
		Assert.assertEquals(expected.x, result.x, 0.1f); // Error tolerance is large because the curves are... curvy.
		Assert.assertEquals(expected.y, result.y, 0.1f);
		Assert.assertEquals(expected.z, result.z, 0.1f);
	}

	@Test
	public void testCubicSplineContinuous () {
		// Define a rough circle based on the 4 cardinal directions.
		Vector3[] controlPoints = {new Vector3(1, 0, 0), new Vector3(0, 1, 0), new Vector3(-1, 0, 0), new Vector3(0, -1, 0),};
		BSpline<Vector3> spline = new BSpline<>(controlPoints, 3, true);

		Vector3 result = new Vector3();
		// 0.875f turns around the circle takes us to the southeast quadrant.
		spline.valueAt(result, 0.875f);

		// The BSpline does not travel through the control points.
		Vector3 expected = new Vector3(0.45f, -0.45f, 0);
		Assert.assertEquals(expected.x, result.x, 0.1f);
		Assert.assertEquals(expected.y, result.y, 0.1f);
		Assert.assertEquals(expected.z, result.z, 0.1f);
	}

	@Test
	public void testCubicDerivative () {
		Vector3[] controlPoints = {new Vector3(0, 0, 0), new Vector3(1, 1, 0), new Vector3(2, 0, 0), new Vector3(3, -1, 0)};
		BSpline<Vector3> spline = new BSpline<>(controlPoints, 3, true);

		Vector3 derivative = new Vector3();
		spline.derivativeAt(derivative, 0.5f);

		Vector3 expectedDerivative = new Vector3(1, -1, 0);
		Assert.assertEquals(expectedDerivative.x, derivative.x, 0.001f);
		Assert.assertEquals(expectedDerivative.y, derivative.y, 0.001f);
		Assert.assertEquals(expectedDerivative.z, derivative.z, 0.001f);
	}

	@Test
	public void testContinuousApproximation () {
		Vector3[] controlPoints = {new Vector3(1, 0, 0), new Vector3(0, 1, 0), new Vector3(-1, 0, 0), new Vector3(0, -1, 0)};
		BSpline<Vector3> spline = new BSpline<>(controlPoints, 3, true);

		Vector3 point = new Vector3(0.45f, -0.45f, 0.0f);
		float t = spline.approximate(point);

		// 0.875 turns corresponds to the southeast quadrant, where point is.
		Assert.assertEquals(0.875f, t, 0.1f);
	}

	@Test
	public void testNonContinuousApproximation () {
		Vector3[] controlPoints = {new Vector3(1, 0, 0), new Vector3(0, 1, 0), new Vector3(-1, 0, 0), new Vector3(0, -1, 0)};
		BSpline<Vector3> spline = new BSpline<>(controlPoints, 3, false);

		Vector3 point;
		float t;
		point = new Vector3(0.0f, 0.666f, 0.0f);
		t = spline.approximate(point);
		Assert.assertEquals(0.0f, t, 0.1f);
		point = new Vector3(-0.666f, 0.0f, 0.0f);
		t = spline.approximate(point);
		Assert.assertEquals(1.0f, t, 0.1f);
		point = new Vector3(-0.45f, 0.45f, 0.0f);
		t = spline.approximate(point);
		Assert.assertEquals(0.5f, t, 0.1f);

	}

	@Test
	public void testSplineContinuity () {
		Vector3[] controlPoints = {new Vector3(0, 0, 0), new Vector3(1, 1, 0), new Vector3(2, 0, 0), new Vector3(3, -1, 0)};
		BSpline<Vector3> spline = new BSpline<>(controlPoints, 3, true);

		Vector3 start = new Vector3();
		Vector3 end = new Vector3();
		spline.valueAt(start, 0.0f);
		spline.valueAt(end, 1.0f);

		// For a continuous spline, the start and end points should be equal
		Assert.assertEquals(start.x, end.x, 0.001f);
		Assert.assertEquals(start.y, end.y, 0.001f);
		Assert.assertEquals(start.z, end.z, 0.001f);
	}

	/** Test to validate calculation with edge cases (t = 0 and t = 1). */
	@Test
	public void testEdgeCases () {
		// The first and last control points aren't on the path.
		Vector3[] controlPoints = {new Vector3(0, 0, 0), new Vector3(1, 1, 0), new Vector3(2, 0, 0), new Vector3(3, -1, 0)};
		BSpline<Vector3> spline = new BSpline<>(controlPoints, 3, false);

		Vector3 start = new Vector3();
		Vector3 expectedStart = new Vector3(1f, 0.666f, 0f);
		Vector3 end = new Vector3();
		Vector3 expectedEnd = new Vector3(2f, 0f, 0f);
		spline.valueAt(start, 0.0f);

		Assert.assertEquals(expectedStart.x, start.x, 0.001f);
		Assert.assertEquals(expectedStart.y, start.y, 0.001f);
		Assert.assertEquals(expectedStart.z, start.z, 0.001f);

		spline.valueAt(end, 1.0f);

		Assert.assertEquals(expectedEnd.x, end.x, 0.001f);
		Assert.assertEquals(expectedEnd.y, end.y, 0.001f);
		Assert.assertEquals(expectedEnd.z, end.z, 0.001f);
	}
}
