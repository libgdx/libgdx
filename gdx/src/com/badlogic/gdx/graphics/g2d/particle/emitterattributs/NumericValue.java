/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.badlogic.gdx.graphics.g2d.particle.emitterattributs;

/**
 *
 * @author Daniel Heinrich <dannynullzwo@gmail.com>
 */
public class NumericValue extends ParticleValue {

    private float value;

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
//        public void save(Writer output) throws IOException {
//            super.save(output);
//            if (!active) {
//                return;
//            }
//            output.write("value: " + value + "\n");
//        }
//
//        public void load(BufferedReader reader) throws IOException {
//            super.load(reader);
//            if (!active) {
//                return;
//            }
//            value = readFloat(reader, "value");
//        }
//    

    @Override
    public NumericValue clone() {
        return (NumericValue) super.clone();
    }
}