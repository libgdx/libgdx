package com.badlogic.gdx.graphics.g2d.newspritebatch;

import com.badlogic.gdx.math.Matrix4;

public class NewSpriteBatch implements NewBatch {
	final Matrix4 projection = new Matrix4();
	final Matrix4 transformation = new Matrix4();
	
	final BatchQuad currentQuad = new BatchQuad();
	final BatchPolygon currentPoly = new BatchPolygon();
	
	@Override
	public void begin () {
	}

	@Override
	public void flush () {
	}

	@Override
	public BatchQuad quad () {
		return currentQuad;
	}

	@Override
	public BatchPolygon polygon (int numPoints) {
		return currentPoly;
	}

	@Override
	public void end () {
	}

	@Override
	public Matrix4 getProjectionMatrix () {
		return projection;
	}

	@Override
	public Matrix4 getTransformMatrix () {
		return transformation;
	}
}
