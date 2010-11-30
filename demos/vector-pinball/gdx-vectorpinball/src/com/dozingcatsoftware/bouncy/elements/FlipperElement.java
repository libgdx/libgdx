package com.dozingcatsoftware.bouncy.elements;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.dozingcatsoftware.bouncy.Field;
import com.dozingcatsoftware.bouncy.IFieldRenderer;

import static com.dozingcatsoftware.bouncy.util.MathUtils.asFloat;
import static com.dozingcatsoftware.bouncy.util.MathUtils.toRadians;

/** FieldElement subclass for a flipper that is controlled by the player. A flipper consists of a Box2D RevoluteJoint 
 * where a thin wall rotates around an invisible anchor. Flippers are defined in the layout JSON as follows:
 * {
 *     "class": "FlipperElement",
 *     "position": [5.5, 10], // x,y of fixed end of flipper which it rotates around
 *     "length": 2.5, // length of the flipper. Negative if the flipper rotates around its right end.
 *     "minangle": -20, // minimum angle from the horizontal. Negative angles are below horizontal.
 *     "maxangle": 20, // maximum angle from the horizontal. 
 *     "upspeed": 7, // rate at which the flipper rotates up when activated (in radians/sec?)
 *     "downspeed": 3 // rate at which the flipper rotates down when not activated (in radians/sec?)
 * }
 * 
 * @author brian
 */

public class FlipperElement extends FieldElement {
	
	Body flipperBody;
	Collection flipperBodySet;
	public Body anchorBody;
	public RevoluteJoint joint;
	RevoluteJointDef jointDef;

	float flipperLength; // negative if flipper rotates around its right end
	float upspeed, downspeed;
	float minangle, maxangle;
	float cx, cy;
	
	@Override
	public void finishCreate(Map params, World world) {
		List pos = (List)params.get("position");
		
		this.cx = asFloat(pos.get(0));
		this.cy = asFloat(pos.get(1));
		this.flipperLength = asFloat(params.get("length"));
		this.minangle = toRadians(asFloat(params.get("minangle")));
		this.maxangle = toRadians(asFloat(params.get("maxangle")));
		this.upspeed = asFloat(params.get("upspeed"));
		this.downspeed = asFloat(params.get("downspeed"));

    	this.anchorBody = Box2DFactory.createCircle(world, this.cx, this.cy, 0.05f, true);
    	// joint angle is 0 when flipper is horizontal
    	// flipper needs to be slightly extended past anchorBody to rotate correctly 
    	float ext = (this.flipperLength > 0) ? -0.05f : +0.05f;
    	// width larger than 0.12 slows rotation?
    	this.flipperBody = Box2DFactory.createWall(world, cx+ext, cy-0.12f, cx+flipperLength, cy+0.12f, 0f);
    	flipperBody.setType(BodyDef.BodyType.DynamicBody);
    	flipperBody.setBullet(true);
    	flipperBody.getFixtureList().get(0).setDensity(5.0f);
    	
    	jointDef = new RevoluteJointDef();
    	jointDef.initialize(anchorBody, flipperBody, new Vector2(this.cx, this.cy));
    	jointDef.enableLimit = true;
    	jointDef.enableMotor = true;
    	// counterclockwise rotations are positive, so flip angles for flippers extending left
    	jointDef.lowerAngle = (this.flipperLength>0) ? this.minangle : -this.maxangle;
    	jointDef.upperAngle = (this.flipperLength>0) ? this.maxangle : -this.minangle;
    	jointDef.maxMotorTorque = 1000f;
    	
    	this.joint = (RevoluteJoint)world.createJoint(jointDef);

    	flipperBodySet = Collections.singleton(flipperBody);
    	this.setEffectiveMotorSpeed(-this.downspeed); // force flipper to bottom when field is first created
	}
	
	/** Returns true if the flipper rotates around its right end, which requires negating some values. */
	boolean isReversed() {
		return (flipperLength<0);
	}
	
	/** Returns the motor speed of the Box2D joint, normalized to be positive when the flipper is moving up. */
	float getEffectiveMotorSpeed() {
		float speed = joint.getMotorSpeed();
		return (isReversed()) ? -speed : speed;
	}
	
	/** Sets the motor speed of the Box2D joint, positive values move the flipper up. */
	void setEffectiveMotorSpeed(float speed) {
		if (isReversed()) speed = -speed;
		joint.setMotorSpeed(speed);
	}
	
	@Override
	public Collection getBodies() {
		return flipperBodySet;
	}
	
	@Override
	public boolean shouldCallTick() {
		return true;
	}
	
	@Override
	public void tick(Field field) {
		super.tick(field);
		
		// if angle is at maximum, reduce speed so that the ball won't fly off when it hits
		if (getEffectiveMotorSpeed()>0.5f) {
			float topAngle = (isReversed()) ? jointDef.lowerAngle : jointDef.upperAngle;
			if (Math.abs(topAngle - joint.getJointAngle())<0.05) {
				setEffectiveMotorSpeed(0.5f);
			}
		}
	}
	
	
	public boolean isFlipperEngaged() {
		return getEffectiveMotorSpeed() > 0;
	}
	
	public void setFlipperEngaged(boolean active) {
		// only adjust speed if state is changing, so we don't accelerate flipper that's been slowed down in tick()
		if (active!=this.isFlipperEngaged()) {
			float speed = (active) ? upspeed : -downspeed;
			setEffectiveMotorSpeed(speed);
		}
	}

	public float getFlipperLength() {
		return flipperLength;
	}
	
	public RevoluteJoint getJoint() {
		return joint;
	}
	
	public Body getAnchorBody() {
		return anchorBody;
	}

	@Override
	public void draw(IFieldRenderer renderer) {
		// draw single line segment from anchor point
		Vector2 position = anchorBody.getPosition();
		float angle = joint.getJointAngle();
		// HACK: angle can briefly get out of range, always draw between min and max
		if (angle<jointDef.lowerAngle) angle = jointDef.lowerAngle;
		if (angle>jointDef.upperAngle) angle = jointDef.upperAngle;
		float x1 = position.x;
		float y1 = position.y;
		float x2 = position.x + flipperLength * (float)Math.cos(angle);
		float y2 = position.y + flipperLength * (float)Math.sin(angle);
		
		renderer.drawLine(x1, y1, x2, y2, redColorComponent(0), greenColorComponent(255), blueColorComponent(0));

	}
}
