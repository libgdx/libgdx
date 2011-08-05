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

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.DesktopArea;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.renderer.LineRenderer;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Matthias Mann
 */
public class NodeArea extends DesktopArea {

    private static final int NUM_BEZIER_POINTS = 24;

    private final ArrayList<Node> nodes;
    private final ArrayList<Connection> connections;
    private final float[] bezierPoints;
    private final Label infoText;

    private Pad newConnectionPad;
    private int newConnectionX;
    private int newConnectionY;
    
    private boolean doubleClick = true;

    public NodeArea(boolean doubleClick) {
    	this.doubleClick = doubleClick;
        this.nodes = new ArrayList<Node>();
        this.connections = new ArrayList<Connection>();
        this.bezierPoints = new float[NUM_BEZIER_POINTS*2];
        this.infoText = new Label((doubleClick ? "Double click" : "Click") + " to create new nodes, then drag pads onto other pads to make connections");

        add(infoText);
    }

    public Node addNode(String name) {
        Node node = new Node(this);
        node.setTitle(name);
        nodes.add(node);
        add(node);
        return node;
    }

    public Pad padFromMouse(int x, int y) {
        for(int i=nodes.size() ; i-->0 ;) {
            Node node = nodes.get(i);

            if(node.isInside(x, y)) {
                return node.padFromMouse(x, y);
            }
        }
        return null;
    }

    public void addConnection(Pad src, Pad dest) {
        if(!dest.isInput() || src.isInput() || src.getNode() == dest.getNode()) {
            return;
        }
        Connection c = dest.getInConnection();
        if(c != null) {
            connections.remove(c);
        }

        c = new Connection(src, dest);
        connections.add(c);
        dest.setInConnection(c);
    }

    public void removeConnection(Connection connection) {
        connections.remove(connection);
    }

    public void dragNewConnection(Pad source, int x, int y) {
        this.newConnectionPad = source;
        this.newConnectionX = x;
        this.newConnectionY = y;
    }

    @Override
    protected boolean handleEvent(Event evt) {
        if(super.handleEvent(evt)) {
            return true;
        }

        if(evt.getType() == Event.Type.MOUSE_CLICKED) {
            if(!doubleClick || evt.getMouseClickCount() == 2) {
                Random r = new Random();
                Node node = addNode("Node " + (1+r.nextInt(100)));
                for(int i=0,n=r.nextInt(3) ; i<=n ; i++) {
                    node.addPad("Input " + (i+1), true);
                }
                for(int i=0,n=r.nextInt(2) ; i<=n ; i++) {
                    node.addPad("Output " + (i+1), false);
                }
                node.adjustSize();
                //System.out.println("clicked at " + evt.getMouseX() + "," + evt.getMouseY());
                node.setPosition(
                        evt.getMouseX() - node.getWidth()/2,
                        evt.getMouseY());
            }
        }
        
        return evt.isMouseEventNoWheel();
    }
    
    @Override
    protected void layout() {
        infoText.adjustSize();
        infoText.setPosition(
                getInnerX()+(getInnerWidth()-infoText.getWidth())/2,
                getInnerY());
    }

    @Override
    protected void paintWidget(GUI gui) {
        LineRenderer lineRenderer = gui.getRenderer().getLineRenderer();
        if(lineRenderer != null) {
            for(int i=0,n=connections.size() ; i<n ; i++) {
                Connection c = connections.get(i);
                drawCurve(
                        lineRenderer,
                        c.getSource().getCenterX(),
                        c.getSource().getCenterY(),
                        c.getDestination().getCenterX(),
                        c.getDestination().getCenterY(),
                        +1, -1,
                        Color.GRAY);
            }

            if(newConnectionPad != null) {
                drawCurve(lineRenderer,
                        newConnectionPad.getCenterX(),
                        newConnectionPad.getCenterY(),
                        newConnectionX,
                        newConnectionY,
                        newConnectionPad.isInput() ? -1 : +1,
                        newConnectionPad.isInput() ? +1 : -1,
                        Color.RED);
            }
        }
    }

    private void drawCurve(LineRenderer lineRenderer, int x0, int y0, int x1, int y1, int dir0, int dir1, Color color) {
        float xM = Math.abs(x1 - x0) * 0.5f;

        computeBezierCurve(x0, x0+xM*dir0, x1+xM*dir1, x1, bezierPoints, 0, NUM_BEZIER_POINTS);
        computeBezierCurve(y0, y0, y1, y1, bezierPoints, 1, NUM_BEZIER_POINTS);

        lineRenderer.drawLine(bezierPoints, NUM_BEZIER_POINTS, 2.0f, color, false);
    }

    private void computeBezierCurve(float q0, float q1, float q2, float q3, float[] dst, int off, int cnt) {
        float f = cnt - 1;
        float ff = f * f;
        float fff = f * ff;
        float rt1 = 3.0f*(q1-q0) / f;
        float rt2 = 3.0f*(q0-2.0f*q1+q2) / ff;
        float rt3 = (q3-q0+3.0f*(q1-q2)) / fff;

        q1 = rt1+rt2+rt3;
        q2 = 2*rt2+6*rt3;
        q3 = 6*rt3;

        for(int i=0 ; i<cnt ; i++) {
            dst[off+i*2] = q0;
            q0 += q1;
            q1 += q2;
            q2 += q3;
        }
    }
}
