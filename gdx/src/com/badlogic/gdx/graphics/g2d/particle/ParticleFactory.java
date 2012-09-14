/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.badlogic.gdx.graphics.g2d.particle;

import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 *
 * @author Daniel Heinrich <dannynullzwo@gmail.com>
 */
public interface ParticleFactory {
    public Particle createParticle(Sprite sprite);
}
