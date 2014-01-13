
package com.badlogic.gdx.physics.box2d.liquidfun;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/** Holds the values for particle creation
 * @author FinnStr */
public class ParticleDef {

	/** The Type of the particle. b2_waterParticle b2_zombieParticle removed after next step b2_wallParticle zero velocity
	 * b2_springParticle with restitution from stretching b2_elasticParticle1 with restitution from deformation b2_viscousParticle1
	 * with viscosity b2_powderParticle without isotropic pressure b2_tensileParticle with surface tension b2_colorMixingParticle
	 * mixing color between contacting particles b2_destructionListener call b2DestructionListener on destruction */
	public enum ParticleType {
		WaterParticle(0), ZombieParticle(1 << 1), WallParticle(1 << 2), SpringParticle(1 << 3), ElasticParticle(1 << 4), ViscousParticle(
			1 << 5), PowderParticle(1 << 6), TensileParticle(1 << 7), ColorMixingParticle(1 << 8), DestructionListener(1 << 9);

		private int value;

		private ParticleType (int pValue) {
			value = pValue;
		}

		public int getValue () {
			return value;
		}
	}

	/** Specifies the type of particle. A particle may be more than one type. Multiple types are chained by logical sums, for
	 * example: pd.flags = b2_elasticParticle | b2_viscousParticle **/
	public Array<ParticleType> flags = new Array<ParticleType>();

	/** The world position of the particle. **/
	public final Vector2 position = new Vector2();

	/** The linear velocity of the particle in world co-ordinates. **/
	public final Vector2 velocitiy = new Vector2();

	/** The color of the particle. **/
	public final Color color = new Color(0, 0, 0, 0);
}
