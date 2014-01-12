
package com.badlogic.gdx.physics.box2d.liquidfun;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.liquidfun.ParticleDef.ParticleType;
import com.badlogic.gdx.utils.Array;

/** Holds the values for a ParticleGroup creation
 * @author FinnStr */
public class ParticleGroupDef {

	/** SolidParticleGroup: resists penetration, RigidParticleGroup: keeps its shape */
	public enum ParticleGroupType {
		SolidParticleGroup(1 << 0), RigidParticleGroup(1 << 1);

		private int value;

		private ParticleGroupType (int pValue) {

		}

		public int getValue () {
			return value;
		}
	}

	/** The particle-behavior flags. **/
	public Array<ParticleType> flags = new Array<ParticleType>();

	/** The group-construction flags. **/
	public Array<ParticleGroupType> groupFlags = new Array<ParticleGroupType>();

	/** The world position of the group. Moves the group's shape a distance equal to the value of position. **/
	public final Vector2 position = new Vector2();

	/** The world angle of the group in radians. Rotates the shape by an angle equal to the value of angle. **/
	public float angle = 0;

	/** The linear velocity of the group's origin in world co-ordinates. **/
	public final Vector2 linearVelocity = new Vector2();

	/** The angular velocity of the group. **/
	public float angularVelocity = 0;

	/** The color of all particles in the group. **/
	public final Color color = new Color(0, 0, 0, 0);

	/** The strength of cohesion among the particles in a group with flag b2_elasticParticle or b2_springParticle. **/
	public float strength = 1;

	/** Shape containing the particle group. **/
	public Shape shape = null;

	/** If true, destroy the group automatically after its last particle has been destroyed. **/
	public boolean destroyAutomatically = true;
}
