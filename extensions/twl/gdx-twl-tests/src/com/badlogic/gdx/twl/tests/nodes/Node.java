/*
 * Copyright (c) 2008-2011, Matthias Mann
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
package com.badlogic.gdx.twl.tests.nodes;

import de.matthiasmann.twl.ResizableFrame;
import java.util.ArrayList;

/**
 *
 * @author Matthias Mann
 */
public class Node extends ResizableFrame {

    private final NodeArea nodeArea;
    private final ArrayList<Pad> pads;

    public Node(NodeArea nodeArea) {
        this.nodeArea = nodeArea;
        this.pads = new ArrayList<Pad>();
    }

    public NodeArea getNodeArea() {
        return nodeArea;
    }

    public Pad addPad(String name, boolean input) {
        Pad pad = new Pad(this, input);
        pad.setTooltipContent(name);
        pads.add(pad);
        add(pad);
        return pad;
    }

    public Pad padFromMouse(int x, int y) {
        for(int i=0,n=pads.size() ; i<n ; i++) {
            Pad pad = pads.get(i);
            if(pad.isInside(x, y)) {
                return pad;
            }
        }
        return null;
    }

    @Override
    protected void layout() {
        super.layout();

        int yIn = getInnerY();
        int yOut = getInnerY();
        int xIn = getX();
        int xOut = getRight() - 2*Pad.RADIUS;

        for(int i=0,n=pads.size() ; i<n ; i++) {
            Pad pad = pads.get(i);

            pad.adjustSize();
            if(pad.isInput()) {
                pad.setPosition(xIn, yIn);
                yIn += Pad.RADIUS * 3;
            } else {
                pad.setPosition(xOut, yOut);
                yOut += Pad.RADIUS * 3;
            }
        }
    }
    
}
