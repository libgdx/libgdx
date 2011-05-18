
package com.dozingcatsoftware.bouncy.elements;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Methods to create Box2D shapes.
 * @author brian
 */

public class Box2DFactory {

	/**
	 * Creates a circle object with the given position and radius. Resitution defaults to 0.6.
	 */
	public static Body createCircle (World world, float x, float y, float radius, boolean isStatic) {
		CircleShape sd = new CircleShape();
		sd.setRadius(radius);

		FixtureDef fdef = new FixtureDef();
		fdef.shape = sd;
		fdef.density = 1.0f;
		fdef.friction = 0.3f;
		fdef.restitution = 0.6f;

		BodyDef bd = new BodyDef();
		// bd.isBullet = true;
		bd.allowSleep = true;
		bd.position.set(x, y);
		Body body = world.createBody(bd);
		body.createFixture(fdef);
		if (isStatic) {
			body.setType(BodyDef.BodyType.StaticBody);
		} else {
			body.setType(BodyDef.BodyType.DynamicBody);
		}
		return body;
	}

	/**
	 * Creates a wall by constructing a rectangle whose corners are (xmin,ymin) and (xmax,ymax), and rotating the box
	 * counterclockwise through the given angle. Restitution defaults to 0.5.
	 */
	public static Body createWall (World world, float xmin, float ymin, float xmax, float ymax, float angle) {
		return createWall(world, xmin, ymin, xmax, ymax, angle, 0f);
	}

	/**
	 * Creates a wall by constructing a rectangle whose corners are (xmin,ymin) and (xmax,ymax), and rotating the box
	 * counterclockwise through the given angle, with specified restitution.
	 */
	public static Body createWall (World world, float xmin, float ymin, float xmax, float ymax, float angle, float restitution) {
		float cx = (xmin + xmax) / 2;
		float cy = (ymin + ymax) / 2;
		float hx = (xmax - xmin) / 2;
		float hy = (ymax - ymin) / 2;
		if (hx < 0) hx = -hx;
		if (hy < 0) hy = -hy;
		PolygonShape wallshape = new PolygonShape();
		wallshape.setAsBox(hx, hy, new Vector2(0f, 0f), angle);

		FixtureDef fdef = new FixtureDef();
		fdef.shape = wallshape;
		fdef.density = 1.0f;
		if (restitution > 0) fdef.restitution = restitution;

		BodyDef bd = new BodyDef();
		bd.position.set(cx, cy);
		Body wall = world.createBody(bd);
		wall.createFixture(fdef);
		wall.setType(BodyDef.BodyType.StaticBody);
		return wall;
	}

	/** Creates a segment-like thin wall with 0.05 thickness going from (x1,y1) to (x2,y2) */
	public static Body createThinWall (World world, float x1, float y1, float x2, float y2, float restitution) {
		// determine center point and rotation angle for createWall
		float cx = (x1 + x2) / 2;
		float cy = (y1 + y2) / 2;
		float angle = (float)Math.atan2(y2 - y1, x2 - x1);
		float mag = (float)Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
		return createWall(world, cx - mag / 2, cy - 0.05f, cx + mag / 2, cy + 0.05f, angle, restitution);
	}

}
