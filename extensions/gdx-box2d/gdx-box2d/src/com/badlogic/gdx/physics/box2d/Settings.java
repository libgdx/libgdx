
package com.badlogic.gdx.physics.box2d;

public class Settings {
	// @off
	/*JNI
#include <box2d/box2d.h>
	 */ // @on

// public static native void setLengthUnitsPerMeter(float value); /*
	// @off
//        b2_lengthUnitsPerMeter = value;
//    */

  public static native float getLengthUnitsPerMeter(); /*
		// @off
        return b2_lengthUnitsPerMeter;
    */

}
