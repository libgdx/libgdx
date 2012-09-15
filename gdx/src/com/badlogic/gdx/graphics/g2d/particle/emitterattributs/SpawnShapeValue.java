/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.badlogic.gdx.graphics.g2d.particle.emitterattributs;

/**
 *
 * @author Daniel Heinrich <dannynullzwo@gmail.com>
 */
public class SpawnShapeValue extends ParticleValue {

    public static enum SpawnShape {

        point, line, square, ellipse
    }

    public static enum SpawnEllipseSide {

        both, top, bottom
    }
    private SpawnShape shape = SpawnShape.point;
    private boolean edges;
    private SpawnEllipseSide side = SpawnEllipseSide.both;

    public SpawnShape getShape() {
        return shape;
    }

    public void setShape(SpawnShape shape) {
        this.shape = shape;
    }

    public boolean isEdges() {
        return edges;
    }

    public void setEdges(boolean edges) {
        this.edges = edges;
    }

    public SpawnEllipseSide getSide() {
        return side;
    }

    public void setSide(SpawnEllipseSide side) {
        this.side = side;
    }
//        public void save(Writer output) throws IOException {
//            super.save(output);
//            if (!active) {
//                return;
//            }
//            output.write("shape: " + shape + "\n");
//            if (shape == SpawnShape.ellipse) {
//                output.write("edges: " + edges + "\n");
//                output.write("side: " + side + "\n");
//            }
//        }
//
//        public void load(BufferedReader reader) throws IOException {
//            super.load(reader);
//            if (!active) {
//                return;
//            }
//            shape = SpawnShape.valueOf(readString(reader, "shape"));
//            if (shape == SpawnShape.ellipse) {
//                edges = readBoolean(reader, "edges");
//                side = SpawnEllipseSide.valueOf(readString(reader, "side"));
//            }
//        }
//

    @Override
    public SpawnShapeValue clone() {
        return (SpawnShapeValue) super.clone();
    }
}
