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
public class Particle extends Sprite {

    int life, currentLife;
    float scale, scaleDiff;
    float rotation, rotationDiff;
    float velocity, velocityDiff;
    float angle, angleDiff;
    float angleCos, angleSin;
    float transparency, transparencyDiff;
    float wind, windDiff;
    float gravity, gravityDiff;
    float[] tint;

    public Particle(Sprite sprite) {
        super(sprite);
    }
}
