
package com.dozingcatsoftware.bouncy.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.dozingcatsoftware.bouncy.IFieldRenderer;

import static com.dozingcatsoftware.bouncy.util.MathUtils.*;

/**
 * This FieldElement subclass approximates a circular wall with a series of straight wall segments whose endpoints lie on a circle
 * or ellipse. These elements are defined in the layout JSON as follows: { "class": "WallArcElement", "center": [5.5, 10], //
 * center of circle or ellipse "xradius": 2.5, // radius in the horizontal direction "yradius": 2, // radius in the y direction
 * "minangle": 45, // starting angle in degrees, 0 is to the right of the center, 90 is up. "maxangle": 135, // ending angle in
 * degrees "segments": 10, // number of straight wall segments to use to approximate the arc. "color": [255,0,0] // optional RGB
 * values for the arc's color }
 * 
 * For circular walls, the "radius" attribute can be used instead of xradius and yradius.
 * 
 * @author brian
 */

public class WallArcElement extends FieldElement {

	public List wallBodies = new ArrayList();
	List<float[]> lineSegments = new ArrayList<float[]>();

	public void finishCreate (Map params, World world) {
		List centerPos = (List)params.get("center");
		float cx = asFloat(centerPos.get(0));
		float cy = asFloat(centerPos.get(1));

		// can specify "radius" for circle, or "xradius" and "yradius" for ellipse
		float xradius, yradius;
		if (params.containsKey("radius")) {
			xradius = yradius = asFloat(params.get("radius"));
		} else {
			xradius = asFloat(params.get("xradius"));
			yradius = asFloat(params.get("yradius"));
		}

		Number segments = (Number)params.get("segments");
		int numsegments = (segments != null) ? segments.intValue() : 5;
		float minangle = toRadians(asFloat(params.get("minangle")));
		float maxangle = toRadians(asFloat(params.get("maxangle")));
		float diff = maxangle - minangle;
		// create numsegments line segments to approximate circular arc
		for (int i = 0; i < numsegments; i++) {
			float angle1 = minangle + i * diff / numsegments;
			float angle2 = minangle + (i + 1) * diff / numsegments;
			float x1 = cx + xradius * (float)Math.cos(angle1);
			float y1 = cy + yradius * (float)Math.sin(angle1);
			float x2 = cx + xradius * (float)Math.cos(angle2);
			float y2 = cy + yradius * (float)Math.sin(angle2);

			Body wall = Box2DFactory.createThinWall(world, x1, y1, x2, y2, 0f);
			this.wallBodies.add(wall);
			lineSegments.add(new float[] {x1, y1, x2, y2});
		}
	}

	@Override public Collection getBodies () {
		return wallBodies;
	}

	@Override public void draw (IFieldRenderer renderer) {
		int len = lineSegments.size();
		for (int i = 0; i < len; i++) {
			float[] segment = lineSegments.get(i);
			renderer.drawLine(segment[0], segment[1], segment[2], segment[3], redColorComponent(DEFAULT_WALL_RED),
				greenColorComponent(DEFAULT_WALL_GREEN), blueColorComponent(DEFAULT_WALL_BLUE));
		}
	}

}
