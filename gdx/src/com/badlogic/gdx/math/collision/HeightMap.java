package com.badlogic.gdx.math.collision;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Heightmap (heightfield) implementation with adjustable height/width scaling and iterative smoothing
 * Typical usess: Detecting collision on the Y axis, constructing a 3d model
 * @author jrenner */
public class HeightMap {
	/** 2d array of heights for each data point */
	float[][] heights;
	/** vertical scaling factor for height */
	private float heightScale;
	/** scaling for width on the x-z plane (distance between each data point) */
	private float widthScale;
	/** computed minimum height in the heightmap */
	private float min;
	/** computed maximum height in the heightmap */
	private float max;
	/** width (x) of the height map in data points, not world units */
	private int width;
	/** depth (z) of the height map in data points, not world units */
	private int depth;
	/** center of the heightmap in world units */
	private Vector3 center = new Vector3();

	/**
	 * Creates a heightmap with default values
	 * @param file - 8-bit PNG file heightmap data
	 */
	public HeightMap(FileHandle file) {
		this(file, 1f, 1f, true, 0);
	}

	/**
	 *
	 * @param file 8-bit PNG file heightmap data
	 * @param heightScale vertical scaling (y axis)
	 * @param widthScale width on the x-z plane (distance between points)
	 * @param whiteHigh height is represented by white (instead of black)
	 */
	public HeightMap(FileHandle file, float heightScale, float widthScale, boolean whiteHigh, int smoothingPasses) {
		// TODO whiteHigh seems to breaking the normals
		this.heightScale = heightScale;
		this.widthScale = widthScale;

		Pixmap pix = new Pixmap(file);
		if (pix.getFormat() != Pixmap.Format.Alpha) {
			throw new GdxRuntimeException("Pixmap must be format Pixmap.Alpha (8-bit Grayscale), not: " + pix.getFormat());
		}
		int pixWidth = pix.getWidth();
		int pixHeight = pix.getHeight();
		int w = pixWidth;
		int h = pixHeight;
		System.out.println("w,h: " + w + ", " + h);
		heights = new float[h][w];

		boolean countWidth = true;
		for (int z = 0; z < pixHeight; z ++) {
			depth++;
			for (int x = 0; x < pixWidth; x ++) {
				if (countWidth) width++;
				int height;
				if (whiteHigh) {
					height = 256 - (-1 * pix.getPixel(x, z));
				} else {
					height = -1 * pix.getPixel(x, z);
				}
				heights[z][x] = height;
			}
			countWidth = false;
		}
		smoothVertexPositions(smoothingPasses);
		updateDimensions();
	}

	private void setMinMaxHeights() {
		min = Float.MAX_VALUE;
		max = Float.MIN_VALUE;
		for (int z = 0; z < heights.length; z++) {
			for (int x = 0; x < heights[0].length; x++) {
				float y = heights[z][x];
				if (y < min) min = y;
				if (y > max) max = y;
			}
		}
	}

	private void updateDimensions() {
		setMinMaxHeights();
		setCenter();
	}

	/** Create smoother terrain using averaging of height values
	 * @param passes number of smoothing passes (higher = smoother) */
	private void smoothVertexPositions(int passes) {
		for (int i = 0; i < passes; i++) {
			// smooth along x
			for (int z = 0; z < heights.length; z++) {
				for (int x = 1; x < heights[z].length - 1; x += 1) {
					float prev = heights[z][x - 1];
					float y = heights[z][x];
					float next = heights[z][x + 1];
					float yAvg = (next + prev) / 2f;
					heights[z][x] = (y + yAvg) / 2f;
				}
			}
			// smooth along z
			for (int x = 0; x < heights[0].length; x++) {
				for (int z = 1; z < heights.length - 1; z += 1) {
					float prev = heights[z - 1][x];
					float y = heights[z][x];
					float next = heights[z + 1][x];
					float yAvg = (next + prev) / 2f;
					heights[z][x] = (y + yAvg) / 2f;
				}
			}
		}
		updateDimensions();
	}

	private void setCenter() {
		center.set(getWidthWorld() / 2, (min + max) / 2, getDepthWorld() / 2);
	}

	/** Get the center of the heightmap in world units */
	public Vector3 getCenter() {
		setCenter();
		return center;
	}

	/** get the vertical height scaling */
	public float getHeightScale() {
		return heightScale;
	}

	/** get the width between data points in world units */
	public float getWidthScale() {
		return widthScale;
	}

	/** parameters assume heightmap's origin is at world coordinates x:0, z: 0
	 * @return true if point (x,z) is within the bounds of this heightmap */
	public boolean containsPoint(float xf, float zf) {
		xf /= widthScale;
		zf /= widthScale;
		int x = (int) Math.floor(xf);
		int z = (int) Math.floor(zf);
		if (x < 0 || z < 0) return false;
		if (z > heights.length - 1) return false;
		if (x > heights[z].length - 1) return false;
		return true;
	}

	private Vector3 tmp = new Vector3();
	private Vector3 tmp2 = new Vector3();
	private Vector3 tmp3 = new Vector3();
	private Vector3 tmp4 = new Vector3();

	/** get height for single point at x,z coords, accounting for scale, does not interpolate using neighbors
	 *  parameters assume heightmap starts at origin 0,0 */
	public float getHeight(float xf, float zf) {
		int x = worldCoordToIndex(xf);
		int z = worldCoordToIndex(zf);
		if (x < 0) x = 0;
		if (z < 0) z = 0;
		if (z >= heights.length) {
			z = heights.length - 1;
		}
		if (x >= heights[z].length) {
			x = heights[z].length - 1;
		}
		return heights[z][x] * heightScale;
	}

	/**
	 * Get the interpolated height for x,z coords, accounting for scale, interpolated using neighbors.
	 * This will give the interpolated height when the parameters lie somewhere between actual heightmap data points.
	 * parameters assume heightmap's origin is at world coordinates x:0, z: 0
	 * @return the scale-adjusted interpolated height at specified world coordinates */
	public float getInterpolatedHeight(float xf, float zf) {
		Vector3 a = tmp;
		Vector3 b = tmp2;
		Vector3 c = tmp3;
		Vector3 d = tmp4;

		float baseX = (float) Math.floor(xf / widthScale);
		float baseZ = (float) Math.floor(zf / widthScale);
		float x = baseX * widthScale;
		float z = baseZ * widthScale;
		float x2 = x + widthScale;
		float z2 = z + widthScale;

		a.set(x,   getHeight(x  , z2), z2);
		b.set(x,   getHeight(x  ,   z),   z);
		c.set(x2, getHeight(x2,   z),   z);
		d.set(x2, getHeight(x2, z2), z2);

		float zFrac = 1f - (zf - z) / widthScale;
		float xFrac = (xf - x) / widthScale;

		float y = (1f - zFrac) * ((1-xFrac) * a.y + xFrac * d.y)
				+ zFrac * ((1-xFrac) * b.y + xFrac * c.y);

		return y;
	}

	private int worldCoordToIndex(float f) {
		return (int) Math.floor(f / widthScale);
	}

	/** @return lowest elevation in the height map */
	public float getMin() {
		return min;
	}

	/** @return height elevation in the height map */
	public float getMax() {
		return max;
	}

	/** @return length of the map on the x axis in number of points */
	public int getWidth() {
		return width;
	}

	/** @return length of the map on the z axis in number of points */
	public int getDepth() {
		return depth;
	}

	/** @return length of the map on the x axis in world units */
	public float getWidthWorld() {
		return width * widthScale;
	}

	/** @return length of the map on the z axis in world units */
	public float getDepthWorld() {
		return depth * widthScale;
	}

	/** @return the number of data points in the height map */
	public int getNumPoints() {
		return width * depth;
	}

	@Override
	public String toString() {
		getCenter();
		return String.format("[HeightMap] min/max: %.1f, %.1f - World w/h: %.1f, %.1f - points: %d, center: %.1f, %.1f, %.1f",
				min, max, getWidthWorld(), getDepthWorld(), getNumPoints(), center.x, center.y, center.z);
	}
}

