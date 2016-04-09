
package com.badlogic.gdx.tests.g3d;

import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** This is a test class, showing how one could implement a height field. See also {@link HeightMapTest}. Do not expect this to be
 * a fully supported and implemented height field class.
 * <p />
 * Represents a HeightField, which is an evenly spaced grid of values, where each value defines the height on that position of the
 * grid, so forming a 3D shape. Typically used for (relatively simple) terrains and such. See <a
 * href="http://en.wikipedia.org/wiki/Heightmap">wikipedia</a> for more information.
 * <p />
 * A height field has a width and height, specifying the width and height of the grid. Points on this grid are specified using
 * integer values, named "x" and "y". Do not confuse these with the x, y and z floating point values representing coordinates in
 * world space.
 * <p />
 * The values of the heightfield are normalized. Meaning that they typically range from 0 to 1 (but they can be negative or more
 * than one). The plane of the heightfield can be specified using the {@link #corner00}, {@link #corner01}, {@link #corner10} and
 * {@link #corner11} members. Where `corner00` is the location on the grid at x:0, y;0, `corner01` at x:0, y:height-1, `corner10`
 * at x:width-1, y:0 and `corner11` the location on the grid at x:width-1, y:height-1.
 * <p />
 * The height and direction of the field can be set using the {@link #magnitude} vector. Typically this should be the vector
 * perpendicular to the heightfield. E.g. if the field is on the XZ plane, then the magnitude is typically pointing on the Y axis.
 * The length of the `magnitude` specifies the height of the height field. In other words, the word coordinate of a point on the
 * grid is specified as:
 * <p />
 * base[y * width + x] + magnitude * value[y * width + x]
 * <p />
 * Use the {@link #getPositionAt(Vector3, int, int)} method to get the coordinate of a specific point on the grid.
 * <p />
 * You can set this heightfield using the constructor or one of the `set` methods. E.g. by specifying an array of values or a
 * {@link Pixmap}. The latter can be used to load a HeightMap, which is an image loaded from disc of which each texel is used to
 * specify the value for each point on the field. Be aware that the total number of vertices cannot exceed 32k. Using a large
 * height map will result in unpredicted results.
 * <p />
 * You can also manually modify the heightfield by directly accessing the {@link #data} member. The index within this array can be
 * calculates as: `y * width + x`. E.g. `field.data[y * field.width + x] = value;`. When you modify the data then you can update
 * the {@link #mesh} using the {@link #update()} method.
 * <p />
 * The {@link #mesh} member can be used to render the height field. The vertex attributes this mesh contains are specified in the
 * constructor. There are two ways for generating the mesh: smooth and sharp.
 * <p />
 * Smooth can be forced by specifying `true` for the `smooth` argument of the constructor. Otherwise it will be based on whether
 * the specified vertex attributes contains a normal attribute. If there is no normal attribute then the mesh will always be
 * smooth (even when you specify `false` in the constructor). In this case the number of vertices is the same as the amount of
 * grid points. Causing vertices to be shared amongst multiple faces.
 * <p />
 * Sharp will be used if the vertex attributes contains a normal attribute and you didnt specify `true` for the `smooth` argument
 * of the constructor. This will cause the number of vertices to be around four times the amount grid points and each normal is
 * estimated for each face instead of each point.
 * @author Xoppa */
public class HeightField implements Disposable {
	public final Vector2 uvOffset = new Vector2(0, 0);
	public final Vector2 uvScale = new Vector2(1, 1);
	public final Color color00 = new Color(Color.WHITE);
	public final Color color10 = new Color(Color.WHITE);
	public final Color color01 = new Color(Color.WHITE);
	public final Color color11 = new Color(Color.WHITE);
	public final Vector3 corner00 = new Vector3(0, 0, 0);
	public final Vector3 corner10 = new Vector3(1, 0, 0);
	public final Vector3 corner01 = new Vector3(0, 0, 1);
	public final Vector3 corner11 = new Vector3(1, 0, 1);
	public final Vector3 magnitude = new Vector3(0, 1, 0);

	public final float[] data;
	public final int width;
	public final int height;
	public final boolean smooth;
	public final Mesh mesh;

	private final float vertices[];
	private final int stride;

	private final int posPos;
	private final int norPos;
	private final int uvPos;
	private final int colPos;

	private final MeshPartBuilder.VertexInfo vertex00 = new MeshPartBuilder.VertexInfo();
	private final MeshPartBuilder.VertexInfo vertex10 = new MeshPartBuilder.VertexInfo();
	private final MeshPartBuilder.VertexInfo vertex01 = new MeshPartBuilder.VertexInfo();
	private final MeshPartBuilder.VertexInfo vertex11 = new MeshPartBuilder.VertexInfo();

	private final Vector3 tmpV1 = new Vector3();
	private final Vector3 tmpV2 = new Vector3();
	private final Vector3 tmpV3 = new Vector3();
	private final Vector3 tmpV4 = new Vector3();
	private final Vector3 tmpV5 = new Vector3();
	private final Vector3 tmpV6 = new Vector3();
	private final Vector3 tmpV7 = new Vector3();
	private final Vector3 tmpV8 = new Vector3();
	private final Vector3 tmpV9 = new Vector3();
	private final Color tmpC = new Color();

	public HeightField (boolean isStatic, final Pixmap map, boolean smooth, int attributes) {
		this(isStatic, map.getWidth(), map.getHeight(), smooth, attributes);
		set(map);
	}

	public HeightField (boolean isStatic, final ByteBuffer colorData, final Pixmap.Format format, int width, int height,
		boolean smooth, int attributes) {
		this(isStatic, width, height, smooth, attributes);
		set(colorData, format);
	}

	public HeightField (boolean isStatic, final float[] data, int width, int height, boolean smooth, int attributes) {
		this(isStatic, width, height, smooth, attributes);
		set(data);
	}

	public HeightField (boolean isStatic, int width, int height, boolean smooth, int attributes) {
		this(isStatic, width, height, smooth, MeshBuilder.createAttributes(attributes));
	}

	public HeightField (boolean isStatic, int width, int height, boolean smooth, VertexAttributes attributes) {
		this.posPos = attributes.getOffset(Usage.Position, -1);
		this.norPos = attributes.getOffset(Usage.Normal, -1);
		this.uvPos = attributes.getOffset(Usage.TextureCoordinates, -1);
		this.colPos = attributes.getOffset(Usage.ColorUnpacked, -1);
		smooth = smooth || (norPos < 0); // cant have sharp edges without normals

		this.width = width;
		this.height = height;
		this.smooth = smooth;
		this.data = new float[width * height];

		this.stride = attributes.vertexSize / 4;

		final int numVertices = smooth ? width * height : (width - 1) * (height - 1) * 4;
		final int numIndices = (width - 1) * (height - 1) * 6;

		this.mesh = new Mesh(isStatic, numVertices, numIndices, attributes);
		this.vertices = new float[numVertices * stride];

		setIndices();
	}

	private void setIndices () {
		final int w = width - 1;
		final int h = height - 1;
		short indices[] = new short[w * h * 6];
		int i = -1;
		for (int y = 0; y < h; ++y) {
			for (int x = 0; x < w; ++x) {
				final int c00 = smooth ? (y * width + x) : (y * 2 * w + x * 2);
				final int c10 = c00 + 1;
				final int c01 = c00 + (smooth ? width : w * 2);
				final int c11 = c10 + (smooth ? width : w * 2);
				indices[++i] = (short)c11;
				indices[++i] = (short)c10;
				indices[++i] = (short)c00;
				indices[++i] = (short)c00;
				indices[++i] = (short)c01;
				indices[++i] = (short)c11;
			}
		}
		mesh.setIndices(indices);
	}

	public void update () {
		if (smooth) {
			if (norPos < 0)
				updateSimple();
			else
				updateSmooth();
		} else
			updateSharp();
	}

	private void updateSmooth () {
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				VertexInfo v = getVertexAt(vertex00, x, y);
				getWeightedNormalAt(v.normal, x, y);
				setVertex(y * width + x, v);
			}
		}
		mesh.setVertices(vertices);
	}

	private void updateSimple () {
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				setVertex(y * width + x, getVertexAt(vertex00, x, y));
			}
		}
		mesh.setVertices(vertices);
	}

	private void updateSharp () {
		final int w = width - 1;
		final int h = height - 1;
		for (int y = 0; y < h; ++y) {
			for (int x = 0; x < w; ++x) {
				final int c00 = (y * 2 * w + x * 2);
				final int c10 = c00 + 1;
				final int c01 = c00 + w * 2;
				final int c11 = c10 + w * 2;
				VertexInfo v00 = getVertexAt(vertex00, x, y);
				VertexInfo v10 = getVertexAt(vertex10, x + 1, y);
				VertexInfo v01 = getVertexAt(vertex01, x, y + 1);
				VertexInfo v11 = getVertexAt(vertex11, x + 1, y + 1);
				v01.normal.set(v01.position).sub(v00.position).nor().crs(tmpV1.set(v11.position).sub(v01.position).nor());
				v10.normal.set(v10.position).sub(v11.position).nor().crs(tmpV1.set(v00.position).sub(v10.position).nor());
				v00.normal.set(v01.normal).lerp(v10.normal, .5f);
				v11.normal.set(v00.normal);

				setVertex(c00, v00);
				setVertex(c10, v10);
				setVertex(c01, v01);
				setVertex(c11, v11);
			}
		}
		mesh.setVertices(vertices);
	}

	/** Does not set the normal member! */
	protected VertexInfo getVertexAt (final VertexInfo out, int x, int y) {
		final float dx = (float)x / (float)(width - 1);
		final float dy = (float)y / (float)(height - 1);
		final float a = data[y * width + x];
		out.position.set(corner00).lerp(corner10, dx).lerp(tmpV1.set(corner01).lerp(corner11, dx), dy);
		out.position.add(tmpV1.set(magnitude).scl(a));
		out.color.set(color00).lerp(color10, dx).lerp(tmpC.set(color01).lerp(color11, dx), dy);
		out.uv.set(dx, dy).scl(uvScale).add(uvOffset);
		return out;
	}

	public Vector3 getPositionAt (Vector3 out, int x, int y) {
		final float dx = (float)x / (float)(width - 1);
		final float dy = (float)y / (float)(height - 1);
		final float a = data[y * width + x];
		out.set(corner00).lerp(corner10, dx).lerp(tmpV1.set(corner01).lerp(corner11, dx), dy);
		out.add(tmpV1.set(magnitude).scl(a));
		return out;
	}

	public Vector3 getWeightedNormalAt (Vector3 out, int x, int y) {
// This commented code is based on http://www.flipcode.com/archives/Calculating_Vertex_Normals_for_Height_Maps.shtml
// Note that this approach only works for a heightfield on the XZ plane with a magnitude on the y axis
// float sx = data[(x < width - 1 ? x + 1 : x) + y * width] + data[(x > 0 ? x-1 : x) + y * width];
// if (x == 0 || x == (width - 1))
// sx *= 2f;
// float sy = data[(y < height - 1 ? y + 1 : y) * width + x] + data[(y > 0 ? y-1 : y) * width + x];
// if (y == 0 || y == (height - 1))
// sy *= 2f;
// float xScale = (corner11.x - corner00.x) / (width - 1f);
// float zScale = (corner11.z - corner00.z) / (height - 1f);
// float yScale = magnitude.len();
// out.set(-sx * yScale, 2f * xScale, sy*yScale*xScale / zScale).nor();
// return out;

// The following approach weights the normal of the four triangles (half quad) surrounding the position.
// A more accurate approach would be to weight the normal of the actual triangles.
		int faces = 0;
		out.set(0, 0, 0);

		Vector3 center = getPositionAt(tmpV2, x, y);
		Vector3 left = x > 0 ? getPositionAt(tmpV3, x - 1, y) : null;
		Vector3 right = x < (width - 1) ? getPositionAt(tmpV4, x + 1, y) : null;
		Vector3 bottom = y > 0 ? getPositionAt(tmpV5, x, y - 1) : null;
		Vector3 top = y < (height - 1) ? getPositionAt(tmpV6, x, y + 1) : null;
		if (top != null && left != null) {
			out.add(tmpV7.set(top).sub(center).nor().crs(tmpV8.set(center).sub(left).nor()).nor());
			faces++;
		}
		if (left != null && bottom != null) {
			out.add(tmpV7.set(left).sub(center).nor().crs(tmpV8.set(center).sub(bottom).nor()).nor());
			faces++;
		}
		if (bottom != null && right != null) {
			out.add(tmpV7.set(bottom).sub(center).nor().crs(tmpV8.set(center).sub(right).nor()).nor());
			faces++;
		}
		if (right != null && top != null) {
			out.add(tmpV7.set(right).sub(center).nor().crs(tmpV8.set(center).sub(top).nor()).nor());
			faces++;
		}
		if (faces != 0)
			out.scl(1f / (float)faces);
		else
			out.set(magnitude).nor();
		return out;
	}

	protected void setVertex (int index, VertexInfo info) {
		index *= stride;
		if (posPos >= 0) {
			vertices[index + posPos + 0] = info.position.x;
			vertices[index + posPos + 1] = info.position.y;
			vertices[index + posPos + 2] = info.position.z;
		}
		if (norPos >= 0) {
			vertices[index + norPos + 0] = info.normal.x;
			vertices[index + norPos + 1] = info.normal.y;
			vertices[index + norPos + 2] = info.normal.z;
		}
		if (uvPos >= 0) {
			vertices[index + uvPos + 0] = info.uv.x;
			vertices[index + uvPos + 1] = info.uv.y;
		}
		if (colPos >= 0) {
			vertices[index + colPos + 0] = info.color.r;
			vertices[index + colPos + 1] = info.color.g;
			vertices[index + colPos + 2] = info.color.b;
			vertices[index + colPos + 3] = info.color.a;
		}
	}

	public void set (final Pixmap map) {
		if (map.getWidth() != width || map.getHeight() != height) throw new GdxRuntimeException("Incorrect map size");
		set(map.getPixels(), map.getFormat());
	}

	public void set (final ByteBuffer colorData, final Pixmap.Format format) {
		set(heightColorsToMap(colorData, format, width, height));
	}

	public void set (float[] data) {
		set(data, 0);
	}

	public void set (float[] data, int offset) {
		if (this.data.length > (data.length - offset)) throw new GdxRuntimeException("Incorrect data size");
		System.arraycopy(data, offset, this.data, 0, this.data.length);
		update();
	}
	
	@Override
	public void dispose () {
		mesh.dispose();
	}

	/** Simply creates an array containing only all the red components of the data. */
	public static float[] heightColorsToMap (final ByteBuffer data, final Pixmap.Format format, int width, int height) {
		final int bytesPerColor = (format == Format.RGB888 ? 3 : (format == Format.RGBA8888 ? 4 : 0));
		if (bytesPerColor == 0) throw new GdxRuntimeException("Unsupported format, should be either RGB8 or RGBA8");
		if (data.remaining() < (width * height * bytesPerColor)) throw new GdxRuntimeException("Incorrect map size");

		final int startPos = data.position();
		byte[] source = null;
		int sourceOffset = 0;
		if (data.hasArray() && !data.isReadOnly()) {
			source = data.array();
			sourceOffset = data.arrayOffset() + startPos;
		} else {
			source = new byte[width * height * bytesPerColor];
			data.get(source);
			data.position(startPos);
		}

		float[] dest = new float[width * height];
		for (int i = 0; i < dest.length; ++i) {
			int v = source[sourceOffset + i * bytesPerColor];
			v = v < 0 ? 256 + v : v;
			dest[i] = (float)v / 255f;
		}

		return dest;
	}
}
