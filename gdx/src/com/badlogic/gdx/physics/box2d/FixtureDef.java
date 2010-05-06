package com.badlogic.gdx.physics.box2d;

/**
 * A fixture definition is used to create a fixture. This class defines an
 * abstract fixture definition. You can reuse fixture definitions safely.
 * @author mzechner
 *
 */
public class FixtureDef 
{
	/**
	 * The shape, this must be set. The shape will be cloned, so you
	 * can create the shape on the stack.
	 */
	public Shape shape;
	
	/** The friction coefficient, usually in the range [0,1]. **/
	public float friction;
	
	/** The restitution (elasticity) usually in the range [0,1]. **/
	public float restitution;
	
	/** The density, usually in kg/m^2. **/
	public float density;
	
	/**
	 * A sensor shape collects contact information but never generates a collision
	 * response.
	 */
	public boolean isSensor;
	
	/** Contact filtering data. **/
	public final Filter filter = new Filter();
}
