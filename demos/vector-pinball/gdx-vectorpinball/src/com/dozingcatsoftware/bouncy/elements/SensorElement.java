
package com.dozingcatsoftware.bouncy.elements;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.dozingcatsoftware.bouncy.Field;
import com.dozingcatsoftware.bouncy.IFieldRenderer;

import static com.dozingcatsoftware.bouncy.util.MathUtils.*;

/**
 * This FieldElement subclass is used to identify areas on the table that should cause custom behavior when the ball enters.
 * SensorElements have no bodies and don't draw anything. The area they monitor can be a rectangle defined by the "rect" parameter
 * as a [xmin,ymin,xmax,ymax] list, a circle defined by the "center" and "radius" parameters. During every tick() invocation, a
 * sensor determines if any of the field's balls are within its area, and if so calls the field delegate's ballInSensorRange
 * method.
 * @author brian
 * 
 */

public class SensorElement extends FieldElement {

	float xmin, ymin, xmax, ymax;
	boolean circular = false;
	float cx, cy; // center for circular areas
	float radiusSquared;

	@Override public void finishCreate (Map params, World world) {
		if (params.containsKey("center") && params.containsKey("radius")) {
			this.circular = true;
			List centerPos = (List)params.get("center");
			this.cx = asFloat(centerPos.get(0));
			this.cy = asFloat(centerPos.get(1));
			float radius = asFloat(params.get("radius"));
			this.radiusSquared = radius * radius;
			// create bounding box to allow rejecting balls without making distance calculations
			this.xmin = this.cx - radius / 2;
			this.xmax = this.cx + radius / 2;
			this.ymin = this.cy - radius / 2;
			this.ymax = this.cy + radius / 2;
		} else {
			List rectPos = (List)params.get("rect");
			this.xmin = asFloat(rectPos.get(0));
			this.ymin = asFloat(rectPos.get(1));
			this.xmax = asFloat(rectPos.get(2));
			this.ymax = asFloat(rectPos.get(3));
		}
	}

	@Override public boolean shouldCallTick () {
		return true;
	}

	boolean ballInRange (Body ball) {
		Vector2 bpos = ball.getPosition();
		// test against rect
		if (bpos.x < xmin || bpos.x > xmax || bpos.y < ymin || bpos.y > ymax) {
			return false;
		}
		// if circle, test (squared) distance to center
		if (this.circular) {
			float distSquared = (bpos.x - this.cx) * (bpos.x - this.cx) + (bpos.y - this.cy) * (bpos.y - this.cy);
			if (distSquared > this.radiusSquared) return false;
		}
		return true;
	}

	@Override public void tick (Field field) {
		int len = field.getBalls().size();
		for (int i = 0; i < len; i++) {
			Body ball = field.getBalls().get(i);
			if (ballInRange(ball)) {
				field.getDelegate().ballInSensorRange(field, this);
				return;
			}
		}
	}

	@Override public Collection<Body> getBodies () {
		return Collections.EMPTY_SET;
	}

	@Override public void draw (IFieldRenderer renderer) {
		// no UI
	}

}
