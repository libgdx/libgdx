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

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;

/**
 *
 * @author Matthias Mann
 */
public class Pad extends Widget {

    public static final StateKey STATE_HOVER            = StateKey.get("hover");
    public static final StateKey STATE_DRAG_DESTINATION = StateKey.get("dragDestination");
    
    public static int RADIUS = 5;

    private final Node node;
    private final boolean input;
    private Connection inConnection;

    private boolean isDragActive;
    private Pad dragDestinationPad;

    public Pad(Node node, boolean input) {
        this.node = node;
        this.input = input;
    }

    public Node getNode() {
        return node;
    }

    public boolean isInput() {
        return input;
    }

    public Connection getInConnection() {
        return inConnection;
    }

    public void setInConnection(Connection inConnection) {
        this.inConnection = inConnection;
    }
    
    @Override
    protected boolean handleEvent(Event evt) {
        if(evt.isMouseEvent()) {
            getAnimationState().setAnimationState(STATE_HOVER, evt.getType() != Event.Type.MOUSE_EXITED);
        }
        
        if(evt.getType() == Event.Type.MOUSE_DRAGGED) {
            NodeArea nodeArea = node.getNodeArea();

            if(!isDragActive) {
                isDragActive = true;
                
                if(isInput()) {
                    nodeArea.removeConnection(getInConnection());
                }
            }

            nodeArea.dragNewConnection(this, evt.getMouseX(), evt.getMouseY());

            Pad pad = nodeArea.padFromMouse(evt.getMouseX(), evt.getMouseY());
            setDragDestPad(pad);
        }

        if(isDragActive && evt.isMouseDragEnd()) {
            NodeArea nodeArea = node.getNodeArea();
            if(dragDestinationPad != null) {
                if(isInput()) {
                    nodeArea.addConnection(dragDestinationPad, this);
                } else {
                    nodeArea.addConnection(this, dragDestinationPad);
                }
            }
            setDragDestPad(null);
            nodeArea.dragNewConnection(null, 0, 0);
            isDragActive = false;
        }

        return evt.isMouseEventNoWheel();
    }

    @Override
    public int getPreferredHeight() {
        return RADIUS*2;
    }

    @Override
    public int getPreferredWidth() {
        return RADIUS*2;
    }

    public int getCenterX() {
        return getX() + RADIUS;
    }

    public int getCenterY() {
        return getY() + RADIUS;
    }

    @Override
    public boolean isInside(int x, int y) {
        int dx = x - getCenterX();
        int dy = y - getCenterY();
        return dx*dx + dy*dy <= RADIUS*RADIUS;
    }

    private void setDragDestPad(Pad pad) {
        if(pad != dragDestinationPad) {
            if(dragDestinationPad != null) {
                dragDestinationPad.getAnimationState().setAnimationState(STATE_DRAG_DESTINATION, false);
            }
            dragDestinationPad = pad;
            if(dragDestinationPad != null) {
                dragDestinationPad.getAnimationState().setAnimationState(STATE_DRAG_DESTINATION, true);
            }
        }
    }
}
