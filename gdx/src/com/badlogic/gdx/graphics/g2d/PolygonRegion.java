
package com.badlogic.gdx.graphics.g2d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Defines a polygon shape on top of a #TextureRegion for minimising pixel drawing. Can either be constructed through a .psh file
 * from an external editor or programmatically through a list of vertices defining a polygon.
 * 
 * THIS STUFF IS WIP
 * 
 * @author Stefan Bachmann */
public class PolygonRegion {
	// texture coordinates in atlas coordinates
	private float[] texCoords;
	// pixel coordinates relative to source image.
	private float[] localVertices;
	// the underlying TextureRegion
	private TextureRegion region;

	/** Creates a PolygonRegion by reading in the vertices and texture coordinates from the external file. TextureRegion can come
	 * from an atlas.
	 * @param region the region used for drawing
	 * @param file polygon shape definition file */
	public PolygonRegion (TextureRegion region, FileHandle file) {
		this.region = region;

		if (file == null) throw new IllegalArgumentException("region cannot be null.");

		loadPolygonDefinition(file);
	}

	/** Creates a PolygonRegin by triangulating the polygon coordinates in vertices and calculates uvs based on that. TextureRegion
	 * can come from an atlas.
	 * @param region the region used for drawing
	 * @param vertices contains 2D polygon coordinates in pixels relative to source region */
	public PolygonRegion (TextureRegion region, float[] vertices) {

	}

	/** Loads the vertices and texture data from an external file. The file should look something like this:
	 * 
	 * ------------ // Triangulated vertices data (x, y) in pixel coordinates with origin bottom-left, y-up v 230.0, 230.0, ... //
	 * UVs with origin top-left u 0.23, 0.123, ... -------------
	 * 
	 * Anything not prefixed with "u" or "v" will be ignored.
	 * @param file file handle to the shape definition file */
	private void loadPolygonDefinition (FileHandle file) {
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(file.read()), 64);

		try {
			while (true) {
				line = reader.readLine();

				if (line == null)
					break;
				else if (line.startsWith("v")) {
					// read in vertices
					String[] vertices = line.substring(1).trim().split(",");
					localVertices = new float[vertices.length];
					for (int i = 0; i < vertices.length; i += 2) {
						localVertices[i] = Float.parseFloat(vertices[i]);
						localVertices[i + 1] = Float.parseFloat(vertices[i + 1]);
					}
				} else if (line.startsWith("u")) {
					// read in uvs
					String[] texCoords = line.substring(1).trim().split(",");
					float localTexCoords[] = new float[texCoords.length];
					for (int i = 0; i < texCoords.length; i += 2) {
						localTexCoords[i] = Float.parseFloat(texCoords[i]);
						localTexCoords[i + 1] = Float.parseFloat(texCoords[i + 1]);
					}

					this.texCoords = calculateAtlasTexCoords(localTexCoords);
				}
			}
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error reading polygon shape file: " + file);
		} finally {
			try {
				reader.close();
			} catch (IOException ignored) {
			}
		}
	}

	/** @param localTexCoords texture coordinates relative to the image
	 * @return the texture coordinates relative to the Texture (atlas) the region is from */
	private float[] calculateAtlasTexCoords (float[] localTexCoords) {

		float uvWidth = this.region.u2 - this.region.u;
		float uvHeight = this.region.v2 - this.region.v;

		for (int i = 0; i < localTexCoords.length; i += 2) {
			localTexCoords[i] = this.region.u + (localTexCoords[i] * uvWidth);
			localTexCoords[i + 1] = this.region.v + (localTexCoords[i + 1] * uvHeight);
		}

		return localTexCoords;
	}

	// Returns the vertices in local space
	public float[] getLocalVertices () {
		return localVertices;
	}

	// Returns the texture coordinates
	public float[] getTextureCoords () {
		return texCoords;
	}

	// Returns the underlying TextureRegion
	public TextureRegion getRegion () {
		return region;
	}
}
