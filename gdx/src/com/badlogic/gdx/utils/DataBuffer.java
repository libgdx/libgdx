
package com.badlogic.gdx.utils;

import com.badlogic.gdx.utils.StreamUtils.OptimizedByteArrayOutputStream;

/**
 * Extends {@link DataOutput} that writes bytes to a byte array.
 *
 * @author Nathan Sweet
 */
public class DataBuffer extends DataOutput {
    private final OptimizedByteArrayOutputStream outStream;

    /**
     * Constructs a DataBuffer object with an initialSize of 32.
     * It extends {@link DataOutput} that writes bytes to a byte array.
     */
    public DataBuffer() {
        this(32);
    }

    /**
     * Constructs a DataBuffer object with a variable initialSize.
     * It extends {@link DataOutput} that writes bytes to a byte array.
     *
     * @param initialSize The initial size of the buffer.
     */
    public DataBuffer(int initialSize) {
        super(new OptimizedByteArrayOutputStream(initialSize));
        outStream = (OptimizedByteArrayOutputStream) out;
    }

    /**
     * Returns the backing array, which has 0 to {@link #size()} items.
     * @return The backing array.
     */
    public byte[] getBuffer() {
        return outStream.getBuffer();
    }

    /**
     * Returns the added elements on the backing array, which has 0 to {@link #written} items.
     * @return The elements that are written on the backing array.
     */
    public byte[] toArray() {
        return outStream.toByteArray();
    }
}
