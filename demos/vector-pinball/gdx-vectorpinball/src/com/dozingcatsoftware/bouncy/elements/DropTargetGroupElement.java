
package com.dozingcatsoftware.bouncy.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.dozingcatsoftware.bouncy.Field;
import com.dozingcatsoftware.bouncy.IFieldRenderer;

import static com.dozingcatsoftware.bouncy.util.MathUtils.*;

/** This FieldElement subclass represents a set of drop targets, which are segments that disappear when hit. When all targets are
 * hit, the Field delegate is notified, and if the reset parameter is set, the targets will reappear after a delay.
 * @author brian */

public class DropTargetGroupElement extends FieldElement {

	// store all bodies and positions, use Body's active flag to determine which targets have been hit
	List<Body> allBodies = new ArrayList<Body>();
	Map<Body, float[]> bodyPositions = new HashMap<Body, float[]>();

	@Override
	public void finishCreate (Map params, World world) {
		// individual targets are specified in "positions" list
		List<List> positions = (List<List>)params.get("positions");
		for (List pos : positions) {
			float[] parray = new float[] {asFloat(pos.get(0)), asFloat(pos.get(1)), asFloat(pos.get(2)), asFloat(pos.get(3))};
			float restitution = 0f;
			Body wallBody = Box2DFactory.createThinWall(world, parray[0], parray[1], parray[2], parray[3], restitution);
			allBodies.add(wallBody);
			bodyPositions.put(wallBody, parray);
		}
	}

	@Override
	public Collection<Body> getBodies () {
		return allBodies;
	}

	/** Returns true if all targets have been hit (and their corresponding bodies made inactive) */
	public boolean allTargetsHit () {
		for (Body body : allBodies) {
			if (body.isActive()) return false;
		}
		return true;
	}

	@Override
	public void handleCollision (Body ball, Body bodyHit, final Field field) {
		bodyHit.setActive(false);
		// if all hit, notify delegate and check for reset parameter
		if (allTargetsHit()) {
			field.getDelegate().allDropTargetsInGroupHit(field, this);

			float restoreTime = asFloat(this.parameters.get("reset"));
			if (restoreTime > 0) {
				field.scheduleAction((long)(restoreTime * 1000), new Runnable() {
					public void run () {
						makeAllTargetsVisible(field);
					}
				});
			}
		}
	}

	/** Makes all targets visible by calling Body.setActive(true) on each target body */
	public void makeAllTargetsVisible (Field field) {
		for (Body body : allBodies) {
			body.setActive(true);
		}
	}

	@Override
	public void draw (IFieldRenderer renderer) {
		// draw line for each target
		int r = redColorComponent(0);
		int g = greenColorComponent(255);
		int b = blueColorComponent(0);

		int len = allBodies.size();
		for (int i = 0; i < len; i++) {
			Body body = allBodies.get(i);
			if (body.isActive()) {
				float[] parray = bodyPositions.get(body);
				renderer.drawLine(parray[0], parray[1], parray[2], parray[3], r, g, b);
			}
		}

	}

}
