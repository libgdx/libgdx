package com.badlogic.gdx.graphics.g3d.environment;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector3;

/**
 * SpotLight
 * Allow attenuation and cutoff
 * Created by Vincent on 24/03/14.
 */
public class SpotLight extends PointLight {

    //direction
    public Vector3 direction = new Vector3();

    //Attenuation factor = 1/(constantAttenuation + linearAttenuation*distance + quadraticAttenuation*distance²)
    public float constantAttenuation = 1;
    public float linearAttenuation = 0;
    public float quadraticAttenuation = 0;

    /**
     * cutoff angle
     */
    public float cutOff = 180;

    /**
     * cutoff factor, from center to cone outside
     */
    public float exponent = 1;


    public SpotLight set(final SpotLight copyFrom) {
        return set(copyFrom.color, copyFrom.position, copyFrom.intensity, copyFrom.direction,
                copyFrom.constantAttenuation, copyFrom.linearAttenuation, copyFrom.quadraticAttenuation,
                copyFrom.cutOff, copyFrom.exponent);
    }

    public SpotLight set(final Color color, final Vector3 position, final float intensity, final Vector3 direction,
                         final float constantAttenuation, final float linearAttenuation, final float quadraticAttenuation,
                         final float cutOff, final float exponent) {
        this.set(color, position, intensity, direction, constantAttenuation, linearAttenuation, quadraticAttenuation)
            .set(cutOff, exponent);
        return this;
    }

    public SpotLight set(final Color color, final Vector3 position, final float intensity, final Vector3 direction,
                         final float constantAttenuation, final float linearAttenuation, final float quadraticAttenuation) {
        super.set(color, position, intensity);
        if (direction != null) this.direction = direction;
        set(constantAttenuation, linearAttenuation, quadraticAttenuation);
        return this;
    }

    public SpotLight set(final float r, final float g, final float b, final Vector3 position, final float intensity,
                         final Vector3 direction, final float constantAttenuation, final float linearAttenuation,
                         final float quadraticAttenuation) {
        super.set(r, g, b, position, intensity);
        if (direction != null) this.direction = direction;
        set(constantAttenuation, linearAttenuation, quadraticAttenuation);
        return this;
    }

    public SpotLight set(final float r, final float g, final float b, final Vector3 position, final float intensity,
                         final Vector3 direction, final float constantAttenuation, final float linearAttenuation,
                         final float quadraticAttenuation, final float cutOff, final float exponent) {
        this.set(r,g,b,position, intensity, direction, constantAttenuation, linearAttenuation, quadraticAttenuation)
            .set(cutOff, exponent);
        return this;
    }


    public SpotLight set(final Color color, final float x, final float y, final float z, final float intensity,
                         final float dirX, final float dirY, final float dirZ, final float constantAttenuation,
                         final float linearAttenuation, final float quadraticAttenuation) {
        super.set(color, x, y, z, intensity);
        this.direction.set(dirX, dirY, dirZ);
        set(constantAttenuation, linearAttenuation, quadraticAttenuation);
        return this;
    }

    public SpotLight set(final Color color, final float x, final float y, final float z, final float intensity,
                         final float dirX, final float dirY, final float dirZ, final float constantAttenuation,
                         final float linearAttenuation, final float quadraticAttenuation, final float cutOff,
                         final float exponent) {

        this.set(color, x, y, z, intensity, dirX, dirY, dirZ, constantAttenuation, linearAttenuation, quadraticAttenuation)
            .set(cutOff, exponent);
        return this;
    }

    public SpotLight set(final float r, final float g, final float b, final float x, final float y, final float z,
                         final float intensity, final float dirX, final float dirY, final float dirZ,
                         final float constantAttenuation, final float linearAttenuation, final float quadraticAttenuation) {
        super.set(r, g, b, x, y, z, intensity);
        this.direction.set(dirX, dirY, dirZ);
        set(constantAttenuation, linearAttenuation, quadraticAttenuation);
        return this;
    }

    public SpotLight set(final float r, final float g, final float b, final float x, final float y, final float z,
                         final float intensity, final float dirX, final float dirY, final float dirZ,
                         final float constantAttenuation, final float linearAttenuation, final float quadraticAttenuation,
                         final float cutOff, final float exponent) {
        this.set(r, g, b, x, y, z, intensity, dirX, dirY, dirZ, constantAttenuation, linearAttenuation, quadraticAttenuation)
            .set(cutOff, exponent);
        return this;
    }


    private void set(final float constantAttenuation, final float linearAttenuation, final float quadraticAttenuation) {
        this.constantAttenuation = constantAttenuation;
        this.linearAttenuation = linearAttenuation;
        this.quadraticAttenuation = quadraticAttenuation;
    }

    private void set(final float cutOff, final float exponent){
        this.cutOff = cutOff;
        this.exponent = exponent;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SpotLight) ? equals((SpotLight) obj) : false;
    }

    public boolean equals(SpotLight other) {
        return  other != null &&
                super.equals(other) &&
                (other == this || (
                        constantAttenuation == other.constantAttenuation &&
                        linearAttenuation == other.linearAttenuation &&
                        quadraticAttenuation == other.quadraticAttenuation &&
                        direction.equals(other.direction)) &&
                        cutOff == other.cutOff &&
                        exponent == other.exponent
                );
    }

}
