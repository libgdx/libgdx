package com.box2dLight.box2dLight;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class DirectionalLight extends Light {

	float sin;
	float cos;
	final Vector2 start[];
	final Vector2 end[];

	/**
	 * Directional lights simulate light source that locations is at infinite
	 * distance. Direction and intensity is same everywhere. -90 direction is
	 * straight from up.
	 * 
	 * @param rayHandler
	 * @param rays
	 * @param color
	 * @param directionDegree
	 */
	public DirectionalLight(RayHandler rayHandler, int rays, Color color,
			float directionDegree) {

		super(rayHandler, rays, color, directionDegree, Float.POSITIVE_INFINITY);

		vertexNum = (vertexNum - 1) * 2;

		start = new Vector2[rayNum];
		end = new Vector2[rayNum];
		for (int i = 0; i < rayNum; i++) {
			start[i] = new Vector2();
			end[i] = new Vector2();
		}
		setDirection(direction);

		lightMesh = new Mesh(VertexDataType.VertexArray, staticLight, vertexNum, 0, new VertexAttribute(
				Usage.Position, 2, "vertex_positions"), new VertexAttribute(
				Usage.ColorPacked, 4, "quad_colors"), new VertexAttribute(
				Usage.Generic, 1, "s"));
		softShadowMesh = new Mesh(VertexDataType.VertexArray,staticLight, vertexNum, 0,
				new VertexAttribute(Usage.Position, 2, "vertex_positions"),
				new VertexAttribute(Usage.ColorPacked, 4, "quad_colors"),
				new VertexAttribute(Usage.Generic, 1, "s"));
		update();
	}

	@Override
	public void setDirection(float direction) {
		super.direction = direction;
		sin = MathUtils.sinDeg(direction);
		cos = MathUtils.cosDeg(direction);
		if (staticLight)
			staticUpdate();
	}

	float lastX;

	@Override
	void update() {
		if (staticLight)
			return;

		final float width = (rayHandler.x2 - rayHandler.x1);
		final float height = (rayHandler.y2 - rayHandler.y1);

		final float sizeOfScreen = width > height ? width : height;

		float xAxelOffSet = sizeOfScreen * cos;
		float yAxelOffSet = sizeOfScreen * sin;

		// preventing length <0 assertion error on box2d.
		if ((xAxelOffSet * xAxelOffSet < 0.1f)
				&& (yAxelOffSet * yAxelOffSet < 0.1f)) {
			xAxelOffSet = 1;
			yAxelOffSet = 1;
		}

		final float widthOffSet = sizeOfScreen * -sin;
		final float heightOffSet = sizeOfScreen * cos;

		float x = (rayHandler.x1 + rayHandler.x2) * 0.5f - widthOffSet;
		float y = (rayHandler.y1 + rayHandler.y2) * 0.5f - heightOffSet;

		final float portionX = 2f * widthOffSet / (rayNum - 1);
		x = (MathUtils.floor(x / (portionX * 2))) * portionX * 2;
		final float portionY = 2f * heightOffSet / (rayNum - 1);
		y = (MathUtils.ceil(y / (portionY * 2))) * portionY * 2;
		for (int i = 0; i < rayNum; i++) {

			final float steppedX = i * portionX + x;
			final float steppedY = i * portionY + y;
			m_index = i;
			start[i].x = steppedX - xAxelOffSet;
			start[i].y = steppedY - yAxelOffSet;

			ptVals[i] = end[i].x = steppedX + xAxelOffSet;
			ptVals[i*3] = end[i].y = steppedY + yAxelOffSet;
/*
 * TODO
			if (rayHandler.world != null && !xray) {
				rayHandler.world.rayCast(ray, start[i], end[i]);
			}
 */
		}

		// update light mesh
		// ray starting point
		int size = 0;
		final int arraySize = rayNum;

		for (int i = 0; i < arraySize; i++) {
			segments[size++] = start[i].x;
			segments[size++] = start[i].y;
			segments[size++] = colorF;
			segments[size++] = 1f;
			segments[size++] = ptVals[i*3];
			segments[size++] = ptVals[i*3+1];
			segments[size++] = colorF;
			segments[size++] = 1f;
		}

		lightMesh.setVertices(segments, 0, size);

		if (!soft || xray)
			return;

		size = 0;
		for (int i = 0; i < arraySize; i++) {
			segments[size++] = ptVals[i*3];
			segments[size++] = ptVals[i*3+1];
			segments[size++] = colorF;
			segments[size++] = 1f;

			segments[size++] = ptVals[i*3] + softShadowLenght * cos;
			segments[size++] = ptVals[i*3+1] + softShadowLenght * sin;
			segments[size++] = zero;
			segments[size++] = 1f;
		}
		softShadowMesh.setVertices(segments, 0, size);

	}

	@Override
	void render() {
		rayHandler.lightRenderedLastFrame++;
		if (rayHandler.isGL20) {
			lightMesh.render(rayHandler.lightShader, GL20.GL_TRIANGLE_STRIP, 0,
					vertexNum);
			if (soft && !xray) {
				softShadowMesh.render(rayHandler.lightShader,
						GL20.GL_TRIANGLE_STRIP, 0, vertexNum);
			}
		} else {
			lightMesh.render(GL10.GL_TRIANGLE_STRIP, 0, vertexNum);
			if (soft && !xray) {
				softShadowMesh.render(GL10.GL_TRIANGLE_STRIP, 0, vertexNum);
			}
		}
	}

	@Override
	final public void attachToBody(Body body, float offsetX, float offSetY) {
	}

	@Override
	public void setPosition(float x, float y) {
	}

	@Override
	public Body getBody() {
		return null;
	}

	@Override
	public float getX() {
		return 0;
	}

	@Override
	public float getY() {
		return 0;
	}

	@Override
	public void setPosition(Vector2 position) {
	}

	@Override
	public boolean contains(float x, float y) {

		boolean oddNodes = false;
		float x2 = ptVals[rayNum*3] = start[0].x;
		float y2 = ptVals[rayNum*3+1] = start[0].y;
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
		for (int i = 0; i < rayNum; x2 = x1, y2 = y1, ++i) {
			x1 = start[i].x;
			y1 = start[i].y;
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
