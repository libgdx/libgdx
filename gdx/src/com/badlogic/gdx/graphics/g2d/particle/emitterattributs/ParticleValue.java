/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.badlogic.gdx.graphics.g2d.particle.emitterattributs;

/**
 *
 * @author Daniel Heinrich <dannynullzwo@gmail.com>
 */
public class ParticleValue implements Cloneable{

    private boolean active;
    private boolean alwaysActive;

    public void setAlwaysActive(boolean alwaysActive) {
        this.alwaysActive = alwaysActive;
    }

    public boolean isAlwaysActive() {
        return alwaysActive;
    }

    public boolean isActive() {
        return alwaysActive || active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

//    public void save(Writer output) throws IOException {
//        if (!alwaysActive) {
//            output.write("active: " + active + "\n");
//        } else {
//            active = true;
//        }
//    }
//
//    public void load(BufferedReader reader) throws IOException {
//        if (!alwaysActive) {
//            active = readBoolean(reader, "active");
//        } else {
//            active = true;
//        }
//    }
//

    @Override
    public ParticleValue clone(){
        try {
            return (ParticleValue) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new UnsupportedOperationException();
        }
    }
    
}
