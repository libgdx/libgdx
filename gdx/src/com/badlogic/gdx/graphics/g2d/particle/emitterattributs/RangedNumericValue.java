/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.badlogic.gdx.graphics.g2d.particle.emitterattributs;

import com.badlogic.gdx.math.MathUtils;

/**
 *
 * @author Daniel Heinrich <dannynullzwo@gmail.com>
 */
public class RangedNumericValue extends ParticleValue {

    private float min, max;

    public float newValue() {
        return min + (max - min) * MathUtils.random();
    }

    public void set(float value) {
        min = value;
        max = value;
    }

    public void set(float min, float max) {
        setMin(min);
        setMax(max);
    }

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }
//        public void save(Writer output) throws IOException {
//            super.save(output);
//            if (!active) {
//                return;
//            }
//            output.write("lowMin: " + lowMin + "\n");
//            output.write("lowMax: " + lowMax + "\n");
//        }
//
//        public void load(BufferedReader reader) throws IOException {
//            super.load(reader);
//            if (!active) {
//                return;
//            }
//            lowMin = readFloat(reader, "lowMin");
//            lowMax = readFloat(reader, "lowMax");
//        }

    @Override
    public RangedNumericValue clone() {
        return (RangedNumericValue) super.clone();
    }
}
