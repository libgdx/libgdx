package com.badlogic.gdx.box2deditor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads shapes (fixture sets) defined with the Box2D Editor and applies them
 * to bodies. Has to be disposed to free some resources.
 *
 * @author Aurelien Ribon (aurelien.ribon@gmail.com)
 */
public class FixtureAtlas {
	private static final FixtureDef DEFAULT_FIXTURE = new FixtureDef();

	private final Map<String, BodyModel> bodyMap = new HashMap<String, BodyModel>();
	private final PolygonShape shape = new PolygonShape();

	/**
	 * Creates a new fixture atlas from the selected file. This file has to
	 * exist and to be valid.
	 * @param shapeFile A file created with the editor.
	 */
	public FixtureAtlas(FileHandle shapeFile) {
		if (shapeFile == null)
			throw new NullPointerException("shapeFile is null");

		importFromFile(shapeFile.read());
	}

	public void dispose() {
		bodyMap.clear();
		shape.dispose();
	}

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	/**
	 * Creates and applies the fixtures defined in the editor. The name
	 * parameter is used to retrieve the shape from the loaded binary file.
	 * Therefore, it _HAS_ to be the exact same name as the one that appeared.
	 * in the editor.
	 * <br/><br/>
	 *
	 * WARNING: The body reference point is supposed to be the bottom left
	 * corner. As a result, calling "getPosition()" on the body will return
	 * its bottom left corner. This is useful to draw a Sprite directly by
	 * setting its position to the body position.
	 * <br/><br/>
	 *
	 * Also, saved shapes are normalized. Thus, you need to provide the desired
	 * width and height of your body for them to scale according to your needs.
	 *
	 * @param body A box2d Body, previously created.
	 * @param name The name of the shape you want to load.
	 * @param width The desired width of the body.
	 * @param height The desired height of the body.
	 */
	public void createFixtures(Body body, String name, float width, float height) {
		createFixtures(body, name, width, height, null);
	}

	/**
	 * Creates and applies the fixtures defined in the editor. The name
	 * parameter is used to retrieve the shape from the loaded binary file.
	 * Therefore, it _HAS_ to be the exact same name as the one that appeared.
	 * in the editor.
	 * <br/><br/>
	 *
	 * WARNING: The body reference point is supposed to be the bottom left
	 * corner. As a result, calling "getPosition()" on the body will return
	 * its bottom left corner. This is useful to draw a Sprite directly by
	 * setting its position to the body position.
	 * <br/><br/>
	 *
	 * Also, saved shapes are normalized. Thus, you need to provide the desired
	 * width and height of your body for them to scale according to your needs.
	 * <br/><br/>
	 *
	 * Moreover, you can submit a custom FixtureDef object. Its parameters will
	 * be applied to every fixture applied to the body by this method.
	 *
	 * @param body A box2d Body, previously created.
	 * @param name The name of the shape you want to load.
	 * @param width The desired width of the body.
	 * @param height The desired height of the body.
	 * @param params Custom fixture parameters to apply.
	 */
	public void createFixtures(Body body, String name, float width, float height, FixtureDef params) {
		BodyModel bm = bodyMap.get(name);
		if (bm == null)
			throw new RuntimeException(name + " does not exist in the fixture list.");

		Vector2[][] polygons = bm.getPolygons(width, height);
		if (polygons == null)
			throw new RuntimeException(name + " does not declare any polygon. "
				+ "Should not happen. Is your shape file corrupted ?");

		for (Vector2[] polygon : polygons) {
			shape.set(polygon);
			FixtureDef fd = params == null ? DEFAULT_FIXTURE : params;
			fd.shape = shape;
			body.createFixture(fd);
		}
	}

	// -------------------------------------------------------------------------
	// Import
	// -------------------------------------------------------------------------

	private void importFromFile(InputStream stream) {
		DataInputStream is = null;

		try {
			is = new DataInputStream(stream);
			while (is.available() > 0) {
				String name = is.readUTF();
				Vector2[][] points = readVecss(is);
				Vector2[][] polygons = readVecss(is);

				BodyModel bm = new BodyModel(polygons);
				bodyMap.put(name, bm);
			}

		} catch (IOException ex) {
			throw new RuntimeException(ex.getMessage());

		} finally {
			if (is != null)
				try { is.close(); } catch (IOException ex) {}
		}
	}
	// -------------------------------------------------------------------------

	private Vector2 readVec(DataInputStream is) throws IOException {
		Vector2 v = new Vector2();
		v.x = is.readFloat();
		v.y = is.readFloat();
		return v;
	}

	private Vector2[] readVecs(DataInputStream is) throws IOException {
		int len = is.readInt();
		Vector2[] vs = new Vector2[len];
		for (int i=0; i<len; i++)
			vs[i] = readVec(is);
		return vs;
	}

	private Vector2[][] readVecss(DataInputStream is) throws IOException {
		int len = is.readInt();
		Vector2[][] vss = new Vector2[len][];
		for (int i=0; i<len; i++)
			vss[i] = readVecs(is);
		return vss;
	}

	// -------------------------------------------------------------------------
	// BodyModel class
	// -------------------------------------------------------------------------

	private class BodyModel {
		private final Vector2[][] normalizedPolygons;
		private final Vector2[][] polygons;

		public BodyModel(Vector2[][] polygons) {
			this.normalizedPolygons = polygons;
			this.polygons = new Vector2[polygons.length][];

			for (int i=0; i<polygons.length; i++) {
				this.polygons[i] = new Vector2[polygons[i].length];
				for (int ii=0; ii<polygons[i].length; ii++)
					this.polygons[i][ii] = new Vector2(polygons[i][ii]);
			}
		}

		public Vector2[][] getPolygons(float width, float height) {
			for (int i=0; i<normalizedPolygons.length; i++) {
				for (int ii=0; ii<normalizedPolygons[i].length; ii++) {
					this.polygons[i][ii] = new Vector2(normalizedPolygons[i][ii]);
					this.polygons[i][ii].x *= width / 100f;
					this.polygons[i][ii].y *= height / 100f;
				}
			}
			return polygons;
		}
	}
}
