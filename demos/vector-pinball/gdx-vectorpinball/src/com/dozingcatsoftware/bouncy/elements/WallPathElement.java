
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
 * FieldElement subclass which represents a series of wall segments. The segments are defined in the "positions" parameter as a
 * list of [x,y] values, for example: { "class": "WallPathElement", "positions": [[5,5], [5,10], [8,10], [5, 15]] }
 * 
 * @author brian
 */

public class WallPathElement extends FieldElement {

	List wallBodies = new ArrayList();
	List<float[]> lineSegments = new ArrayList<float[]>();

	public void finishCreate (Map params, World world) {
		List positions = (List)params.get("positions");

		for (int i = 0; i < positions.size() - 1; i++) {
			List startpos = (List)positions.get(i);
			List endpos = (List)positions.get(i + 1);

			float[] segment = new float[] {asFloat(startpos.get(0)), asFloat(startpos.get(1)), asFloat(endpos.get(0)),
				asFloat(endpos.get(1))};
			lineSegments.add(segment);

			Body wall = Box2DFactory.createThinWall(world, segment[0], segment[1], segment[2], segment[3], 0f);
			this.wallBodies.add(wall);
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
