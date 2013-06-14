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
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;

public class DirectionalLight extends Light {

	protected com.box2dLight.box2dLight.Box2dLight nativeLight;

	final Vector2 start = new Vector2();
	
	float lightWidth;


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
		float distance, float x, float y, float directionDegree, float lightWidth) {

		super(rayHandler, rays, color, directionDegree, distance);

		start.x = x;
		start.y = y;
		
		this.lightWidth = lightWidth;
		
		vertexNum = (vertexNum - 1) * 2;

		setDirection(direction);

		nativeLight = new Box2dLight(rayHandler.world, rays, false);

		lightMesh = new Mesh(VertexDataType.VertexArray, staticLight, vertexNum, 0, new VertexAttribute(
			Usage.Position, 2, "vertex_positions"), new VertexAttribute(
				Usage.ColorPacked, 4, "quad_colors"), new VertexAttribute(
					Usage.Generic, 1, "s"));
		softShadowMesh = new Mesh(VertexDataType.VertexArray,staticLight, vertexNum, 0,
			new VertexAttribute(Usage.Position, 2, "vertex_positions"),
			new VertexAttribute(Usage.ColorPacked, 4, "quad_colors"),
			new VertexAttribute(Usage.Generic, 1, "s"));

	}

	@Override
	public void setDirection(float direction) {
		super.direction = direction;
		if (staticLight)
			staticUpdate();
	}

	float lastX;

	@Override
	void update() {
		if (staticLight)
			return;

		nativeLight.update_cone(start.x, start.y, distance, direction, lightWidth);

		nativeLight.setLightMesh(segments, colorF, rayHandler.isGL20);
		if( rayHandler.isGL20 )
			lightMesh.setVertices(segments, 0, (rayNum*2)*4);
		else
			lightMesh.setVertices(segments, 0, (rayNum*2)*3);

		if (!soft || xray)
			return;

		nativeLight.setShadowMesh(segments, colorF, softShadowLenght, rayHandler.isGL20);
		if( rayHandler.isGL20 )
			softShadowMesh.setVertices(segments, 0, (rayNum*2)*4);
		else
			softShadowMesh.setVertices(segments, 0, (rayNum*2)*3);

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

	public float getLightWidth () {
		return lightWidth;
	}

	public void setLightWidth (float lightWidth) {
		this.lightWidth = lightWidth;
	}

	@Override
	public void attachToBody (Body body, float offsetX, float offSetY) {
	}

	@Override
	public Body getBody () {
		return null;
	}

	@Override
	public void setPosition (float x, float y) {
		start.x = x;
		start.y = y;
		if (staticLight)
			staticUpdate();
		start.set(x, y);
	}

	@Override
	public void setPosition (Vector2 position) {
		setPosition(position.x, position.y);
	}

	@Override
	public Vector2 getPosition() {
		tmpPosition.x = start.x;
		tmpPosition.y = start.y;
		return tmpPosition;
	}


	@Override
	public float getX () {
		return start.x;
	}

	@Override
	public float getY () {
		return start.y;
	}
}
