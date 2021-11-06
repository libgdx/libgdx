
package com.badlogic.gdx.math;

import static org.junit.Assert.*;

import org.junit.Test;

public class QuaternionTest {
    private static final float epsilon = Float.MIN_NORMAL;

    @Test
    public void testEulerPrecision () {
        float yaw = -5f;
        float pitch = 42.00055f;
        float roll = 164.32f;

        Quaternion q = new Quaternion().setEulerAngles(yaw, pitch, roll);

        assertEquals(yaw, q.getYaw(), epsilon);
        assertEquals(pitch, q.getPitch(), epsilon);
        assertEquals(roll, q.getRoll(), epsilon);
    }
}
