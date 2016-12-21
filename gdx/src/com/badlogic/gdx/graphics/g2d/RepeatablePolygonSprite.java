/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;

/**
 * Renders polygon filled with a repeating TextureRegion with specified density
 * Without causing an additional flush or render call
 *
 * @author Avetis Zakharyan
 */
public class RepeatablePolygonSprite {

    private TextureRegion region;
    private float density;

    private boolean dirty = true;

    private Array<float[]> parts = new Array<float[]>();

    private Array<float[]> vertices = new Array<float[]>();
    private Array<short[]> indices = new Array<short[]>();

    private int cols, rows;
    private float gridWidth, gridHeight;

    public float x = 0;
    public float y = 0;
    private Color color = Color.WHITE;
    private Vector2 offset = new Vector2();


    /**
     * Sets polygon with repeating texture region, the size of repeating grid is equal to region size
     * @param region - region to repeat
     * @param vertices - cw vertices of polygon
     */
    public void setPolygon(TextureRegion region, float[] vertices) {
        setPolygon(region, vertices, -1);
    }

    /**
     * Sets polygon with repeating texture region, the size of repeating grid is equal to region size
     * @param region - region to repeat
     * @param vertices - cw vertices of polygon
     * @param density - number of regions per polygon width bound
     */
    public void setPolygon(TextureRegion region, float[] vertices, float density) {

        this.region = region;

        vertices = offset(vertices);

        Polygon polygon = new Polygon(vertices);
        Polygon tmpPoly = new Polygon();
        Polygon intersectionPoly = new Polygon();
        EarClippingTriangulator triangulator = new EarClippingTriangulator();

        int idx;

        Rectangle boundRect = polygon.getBoundingRectangle();

        if(density == -1) density = boundRect.getWidth()/region.getRegionWidth();

        float regionAspectRatio = (float) region.getRegionHeight() / (float) region.getRegionWidth();
        cols = (int) (Math.ceil(density));
        gridWidth = boundRect.getWidth() / density;
        gridHeight = regionAspectRatio * gridWidth;
        rows = (int) Math.ceil(boundRect.getHeight() / gridHeight);

        for(int col = 0; col < cols; col++) {
            for(int row = 0; row < rows; row++) {
                float[] verts = new float[8];
                idx = 0;
                verts[idx++] = col * gridWidth;
                verts[idx++] = row * gridHeight;
                verts[idx++] = (col) * gridWidth;
                verts[idx++] = (row+1) * gridHeight;
                verts[idx++] = (col+1) * gridWidth;
                verts[idx++] = (row+1) * gridHeight;
                verts[idx++] = (col+1) * gridWidth;
                verts[idx] = (row) * gridHeight;
                tmpPoly.setVertices(verts);

                Intersector.intersectPolygons(polygon, tmpPoly, intersectionPoly);
                verts = intersectionPoly.getVertices();
                if(verts.length > 0) {
                    parts.add(snapToGrid(verts));
                    ShortArray arr = triangulator.computeTriangles(verts);
                    indices.add(arr.toArray());
                } else {
                    // adding null for key consistancy, needed to get col/row from key
                    // the other alternative is to make parts - IntMap<FloatArray>
                    parts.add(null);
                }
            }
        }

        buildVertices();
    }


    /**
     * This is a garbage, due to Intersector returning values slightly different then the grid values
     * Snapping exactly to grid is important, so that during bulidVertices method, it can be figured out
     * if points is on the wall of it's own grid box or not, to set u/v properly.
     * Any other implementations are welcome
     */
    private float[] snapToGrid(float[] vertices) {
        for(int i = 0; i < vertices.length; i+=2) {
            float numX = (vertices[i] / gridWidth) % 1;
            float numY = (vertices[i+1] / gridHeight) % 1;
            if(numX > 0.99f || numX < 0.01f) {
                vertices[i] = gridWidth * Math.round(vertices[i] / gridWidth);
            }
            if(numY > 0.99f || numY < 0.01f) {
                vertices[i+1] = gridHeight * Math.round(vertices[i+1] / gridHeight);
            }
        }

        return vertices;
    }


    /**
     * Offsets polygon to 0 coordinate for ease of calculations, later offset is put back on final render
     * @param vertices
     * @return offsetted vertices
     */
    private float[] offset(float[] vertices) {
        offset.set(vertices[0], vertices[1]);
        for(int i = 0; i < vertices.length-1; i+=2) {
            if(offset.x > vertices[i]) {
                offset.x = vertices[i];
            }
            if(offset.y > vertices[i+1]) {
                offset.y = vertices[i+1];
            }
        }
        for(int i = 0; i < vertices.length; i+=2) {
            vertices[i] -= offset.x;
            vertices[i+1] -= offset.y;
        }

        return vertices;
    }

    /**
     * Builds final vertices with vertex attributes like coordinates, color and region u/v
     */
    private void buildVertices() {
        vertices.clear();
        for(int i = 0; i < parts.size; i++) {
            float verts[] = parts.get(i);
            if(verts == null) continue;

            float[] fullVerts = new float[5 * verts.length/2];
            int idx = 0;

            int col = i / rows;
            int row = i % rows;

            for(int j = 0; j < verts.length; j+=2) {
                fullVerts[idx++] = verts[j] + offset.x + x;
                fullVerts[idx++] = verts[j+1] + offset.y + y;

                fullVerts[idx++] = color.toFloatBits();

                float u = (verts[j] % gridWidth) / gridWidth;
                float v = (verts[j+1] % gridHeight) / gridHeight;
                if(verts[j] == col*gridWidth) u = 0f;
                if(verts[j] == (col+1)*gridWidth) u = 1f;
                if(verts[j+1] == row*gridHeight) v = 0f;
                if(verts[j+1] == (row+1)*gridHeight)v = 1f;
                u = region.getU() + (region.getU2() - region.getU()) * u;
                v = region.getV() + (region.getV2() - region.getV()) * v;
                fullVerts[idx++] = u;
                fullVerts[idx++] = v;
            }
            vertices.add(fullVerts);
        }
        dirty = false;
    }

    public void draw(PolygonSpriteBatch batch) {
        if(dirty) {
            buildVertices();
        }
        for(int i = 0; i < vertices.size; i++) {
            batch.draw(region.getTexture(), vertices.get(i), 0, vertices.get(i).length, indices.get(i), 0, indices.get(i).length);
        }
    }

    /**
     * @param color - Tint color to be applied to entire polygon
     */
    public void setColor(Color color) {
        this.color = color;
        dirty = true;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        dirty = true;
    }

}
