/*
 * Copyright (c) 2008-2010, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.badlogic.gdx.backends.desktop;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 *
 * @author Matthias Mann
 */
public class TextureDataSource {

    private final BufferedImage img;

    public TextureDataSource(BufferedImage img) {
        this.img = img;
    }

    public int getWidth() {
        return img.getWidth();
    }

    public int getHeight() {
        return img.getHeight();
    }

    public void decode(ByteBuffer buffer) throws IOException {
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();

        ByteBuffer bb = buffer.slice();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        IntBuffer ib = bb.asIntBuffer();

        Raster r = img.getRaster();
        if(img.getType() == BufferedImage.TYPE_INT_ARGB) {
            ib.put(((DataBufferInt)r.getDataBuffer()).getData(), 0, imgWidth*imgHeight);
        } else {
            // same as image.getRGB() without allocating a large int[]
            ColorModel cm = img.getColorModel();
            Object data = r.getDataElements(0, 0, null);
            for(int y=0 ; y<imgHeight ; y++) {
                for(int x=0 ; x<imgWidth ; x++) {
                	int color = cm.getRGB(r.getDataElements(x, y, data));
                    ib.put(color);
                }
            }
        }
        
        buffer.position(buffer.position() + ib.position()*4);
    }

}
