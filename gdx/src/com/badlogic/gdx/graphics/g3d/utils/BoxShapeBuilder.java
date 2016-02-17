
package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/** BoxShapeBuilder builds box.
 * 
 * @author realitix */
public class BoxShapeBuilder {

	/** Color used to set default value */
	private static final Color tmpColor = new Color();

	/** Vector3 used during vertices generation */
	private static final Vector3 tmp = new Vector3();

	/** Add bounding box with default color
	 * @param box */
	public static void build (MeshPartBuilder builder, BoundingBox box) {
		build(builder, box, tmpColor.set(1, 0.66f, 0, 1));
	}

	/** Add bounding box with custom color
	 * @param box
	 * @param color */
	public static void build (MeshPartBuilder builder, BoundingBox box, Color color) {
		builder.setColor(color);
		builder.box(box.getCorner000(tmp).cpy(), box.getCorner010(tmp).cpy(), box.getCorner100(tmp).cpy(), box.getCorner110(tmp)
			.cpy(), box.getCorner001(tmp).cpy(), box.getCorner011(tmp).cpy(), box.getCorner101(tmp).cpy(), box.getCorner111(tmp)
			.cpy());
	}
}
