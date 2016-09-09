package org.jbox2d.callbacks;

import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleGroup;

public interface ParticleDestructionListener {
  /**
   * Called when any particle group is about to be destroyed.
   */
  void sayGoodbye(ParticleGroup group);

  /**
   * Called when a particle is about to be destroyed. The index can be used in conjunction with
   * {@link World#getParticleUserDataBuffer} to determine which particle has been destroyed.
   * 
   * @param index
   */
  void sayGoodbye(int index);
}
