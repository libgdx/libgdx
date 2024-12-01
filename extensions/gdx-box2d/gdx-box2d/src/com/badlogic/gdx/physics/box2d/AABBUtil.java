package com.badlogic.gdx.physics.box2d;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class AABBUtil {
    /** Sets the lower and upper bounds of the specified fixture to the provided Vector2 values.
     * Supported Shapes: Polygon/Circle **/
    public static void getAABB(Fixture fixture, Vector2 lowerBound, Vector2 upperBound) {
        Body body = fixture.getBody();
        Transform transform = body.getTransform();

        Shape shape = fixture.getShape();
        switch (shape.getType()) {
            case Polygon:
                PolygonShape polygon = (PolygonShape) shape;
                getPolygonAABB(polygon, transform, lowerBound, upperBound);
                break;
            case Circle:
                CircleShape circle = (CircleShape) shape;
                getCircleAABB(circle, transform, lowerBound, upperBound);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported shape type: " + shape.getType());
        }
    }

    private static void getPolygonAABB(PolygonShape polygon, Transform transform, Vector2 lowerBound, Vector2 upperBound) {
        Vector2 vertex = new Vector2();
        lowerBound.set(Float.MAX_VALUE, Float.MAX_VALUE);
        upperBound.set(-Float.MAX_VALUE, -Float.MAX_VALUE);

        for (int i = 0; i < polygon.getVertexCount(); i++) {
            polygon.getVertex(i, vertex);
            transform.mul(vertex);

            lowerBound.x = Math.min(lowerBound.x, vertex.x);
            lowerBound.y = Math.min(lowerBound.y, vertex.y);
            upperBound.x = Math.max(upperBound.x, vertex.x);
            upperBound.y = Math.max(upperBound.y, vertex.y);
        }
    }

    private static void getCircleAABB(CircleShape circle, Transform transform, Vector2 lowerBound, Vector2 upperBound) {
        Vector2 center = transform.mul(circle.getPosition()); // Transform the center
        float radius = circle.getRadius();

        lowerBound.set(center.x - radius, center.y - radius);
        upperBound.set(center.x + radius, center.y + radius);
    }
}

