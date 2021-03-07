
package com.badlogic.gdx.utils;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DataBufferTest {
    @Test
    public void initialiseNoSizeTest() {
        DataBuffer dataBuffer = new DataBuffer();
        assertEquals(32, dataBuffer.getBuffer().length);
    }

    @Test
    public void initialiseWithSizeTest() {
        DataBuffer dataBuffer = new DataBuffer(10);
        assertEquals(10, dataBuffer.getBuffer().length);
    }

    @Test
    public void toArrayTest() throws IOException {
        DataBuffer dataBuffer = new DataBuffer();
        dataBuffer.writeByte(11);

        byte[] byteArray = dataBuffer.toArray();
        assertEquals(1, byteArray.length);
        assertEquals(11, byteArray[0]);
    }
}
