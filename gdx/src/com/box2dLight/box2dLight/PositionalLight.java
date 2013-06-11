package com.box2dLight.box2dLight;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PointLight;
import com.badlogic.gdx.utils.NumberUtils;

public abstract class PositionalLight extends Light {

  protected com.badlogic.gdx.physics.box2d.PointLight nativeLight;

  protected Body body;
  protected float bodyOffsetX;
  protected float bodyOffsetY;

  final Vector2 start = new Vector2();
  final float sin[];
  final float cos[];

  /**
   * attach positional light to automatically follow body. Position is fixed
   * to given offset.
   */
  @Override
  public void attachToBody(Body body, float offsetX, float offSetY) {
    this.body = body;
    bodyOffsetX = offsetX;
    bodyOffsetY = offSetY;
    if (staticLight)
      staticUpdate();
  }

  @Override
  public Vector2 getPosition() {
    tmpPosition.x = start.x;
    tmpPosition.y = start.y;
    return tmpPosition;
  }

  public Body getBody() {
    return body;
  }

  /**
   * horizontal starting position of light in world coordinates.
   */
  @Override
  public float getX() {
    return start.x;
  }

  /**
   * vertical starting position of light in world coordinates.
   */
  @Override
  public float getY() {
    return start.y;
  }

  @Override
  public void setPosition(float x, float y) {
    start.x = x;
    start.y = y;
    if (staticLight)
      staticUpdate();
  }

  @Override
  public void setPosition(Vector2 position) {
    start.x = position.x;
    start.y = position.y;
    if (staticLight)
      staticUpdate();
  }

  @Override
  void update() {
    if (body != null && !staticLight) {
      final Vector2 vec = body.getPosition();
      float angle = body.getAngle();
      final float cos = MathUtils.cos(angle);
      final float sin = MathUtils.sin(angle);
      final float dX = bodyOffsetX * cos - bodyOffsetY * sin;
      final float dY = bodyOffsetX * sin + bodyOffsetY * cos;
      start.x = vec.x + dX;
      start.y = vec.y + dY;
      setDirection(angle * MathUtils.radiansToDegrees);
    }

    if (rayHandler.culling) {
      culled = ((!rayHandler.intersect(start.x, start.y, distance
          + softShadowLenght)));
      if (culled)
        return;
    }

    if (staticLight)
      return;


    nativeLight.update(ptVals, start.x, start.y, distance);

    setMesh();
  }

  @Override
  public void setDirection(float directionDegree) {
    direction = directionDegree;
  }

  @Override
  public void remove() {
    super.remove();
    nativeLight.releaseLight();
    nativeLight = null;
  }

  void setMesh() {
    if (rayHandler.isGL20) {
      // ray starting point
      int size = 0;

      segments[size++] = start.x;
      segments[size++] = start.y;
      segments[size++] = colorF;
      segments[size++] = 1;
      // rays ending points.
      for (int i = 0; i < rayNum; i++) {
        segments[size++] = ptVals[i*3];
        segments[size++] = ptVals[i*3+1];
        segments[size++] = colorF;
        segments[size++] = 1 - ptVals[i*3+2];
      }
      lightMesh.setVertices(segments, 0, size);

      if (!soft || xray)
        return;

      size = 0;
      // rays ending points.

      for (int i = 0; i < rayNum; i++) {
        segments[size++] = ptVals[i*3];
        segments[size++] = ptVals[i*3+1];
        segments[size++] = colorF;
        final float s = (1 - ptVals[i*3+2]);
        segments[size++] = s;
        segments[size++] = ptVals[i*3] + s * softShadowLenght * cos[i];
        segments[size++] = ptVals[i*3+1] + s * softShadowLenght * sin[i];
        segments[size++] = zero;
        segments[size++] = 0f;
      }
      softShadowMesh.setVertices(segments, 0, size);
    } else {
      final float r = color.r * 255;
      final float g = color.g * 255;
      final float b = color.b * 255;
      final float a = color.a * 255;
      // ray starting point

      int size = 0;
      segments[size++] = start.x;
      segments[size++] = start.y;
      segments[size++] = colorF;
      // rays ending points.
      for (int i = 0; i < rayNum; i++) {
        segments[size++] = ptVals[i*3];
        segments[size++] = ptVals[i*3+1];
        final float s = 1f - ptVals[i*3+2];
        // ugly inlining
        segments[size++] = NumberUtils.intToFloatColor(((int) (a * s) << 24)
            | ((int) (b * s) << 16) | ((int) (g * s) << 8)
            | ((int) (r * s)));
      }
      lightMesh.setVertices(segments, 0, size);

      if (!soft || xray)
        return;

      size = 0;
      for (int i = 0; i < rayNum; i++) {
        segments[size++] = ptVals[i*3];
        segments[size++] = ptVals[i*3+1];
        // color value is cached.
        final float s = 1f - ptVals[i*3+2];
        // ugly inlining
        segments[size++] = NumberUtils
            .intToFloatColor(((int) (a * s) << 24)
                | ((int) (b * s) << 16) | ((int) (g * s) << 8)
                | ((int) (r * s)));

        segments[size++] = ptVals[i*3] + s * softShadowLenght * cos[i];
        segments[size++] = ptVals[i*3+1] + s * softShadowLenght * sin[i];
        segments[size++] = zero;
      }
      softShadowMesh.setVertices(segments, 0, size);
    }

  }

  @Override
  void render() {
    if (rayHandler.culling && culled)
      return;

    rayHandler.lightRenderedLastFrame++;
    if (rayHandler.isGL20) {
      lightMesh.render(rayHandler.lightShader, GL20.GL_TRIANGLE_FAN, 0,
          vertexNum);
      if (soft && !xray) {
        softShadowMesh.render(rayHandler.lightShader,
            GL20.GL_TRIANGLE_STRIP, 0, (vertexNum - 1) * 2);
      }
    } else {
      lightMesh.render(GL10.GL_TRIANGLE_FAN, 0, vertexNum);
      if (soft && !xray) {
        softShadowMesh.render(GL10.GL_TRIANGLE_STRIP, 0,
            (vertexNum - 1) * 2);
      }
    }
  }

  public PositionalLight(RayHandler rayHandler, int rays, Color color,
      float distance, float x, float y, float directionDegree) {
    super(rayHandler, rays, color, directionDegree, distance);

    nativeLight = new PointLight(rayHandler.world, rays);

    start.x = x;
    start.y = y;
    sin = new float[rays];
    cos = new float[rays];

    if (rayHandler.isGL20) {
      lightMesh = new Mesh(VertexDataType.VertexArray, false, vertexNum, 0,
          new VertexAttribute(Usage.Position, 2, "vertex_positions"),
          new VertexAttribute(Usage.ColorPacked, 4, "quad_colors"),
          new VertexAttribute(Usage.Generic, 1, "s"));
      softShadowMesh = new Mesh(VertexDataType.VertexArray, false, vertexNum * 2, 0,
          new VertexAttribute(Usage.Position, 2, "vertex_positions"),
          new VertexAttribute(Usage.ColorPacked, 4, "quad_colors"),
          new VertexAttribute(Usage.Generic, 1, "s"));
    } else {
      lightMesh = new Mesh(VertexDataType.VertexArray, false, vertexNum, 0,
          new VertexAttribute(Usage.Position, 2, "vertex_positions"),
          new VertexAttribute(Usage.ColorPacked, 4, "quad_colors"));
      softShadowMesh = new Mesh(VertexDataType.VertexArray, false, vertexNum * 2, 0,
          new VertexAttribute(Usage.Position, 2, "vertex_positions"),
          new VertexAttribute(Usage.ColorPacked, 4, "quad_colors"));
    }
    setMesh();
  }

  @Override
  public boolean contains(float x, float y) {

    // fast fail
    final float x_d = start.x - x;
    final float y_d = start.y - y;
    final float dst2 = x_d * x_d + y_d * y_d;
    if (distance * distance <= dst2)
      return false;

    // actual check

    boolean oddNodes = false;
    float x2 = ptVals[rayNum*3] = start.x;
    float y2 = ptVals[rayNum*3+1] = start.y;
    float x1, y1;
    for (int i = 0; i <= rayNum; x2 = x1, y2 = y1, ++i) {
      x1 = ptVals[i*3];
      y1 = ptVals[i*3+1];
      if (((y1 < y) && (y2 >= y))
          || (y1 >= y) && (y2 < y)) {
        if ((y - y1) / (y2 - y1)
            * (x2 - x1) < (x - x1))
          oddNodes = !oddNodes;
      }
    }
    return oddNodes;

  }
}
