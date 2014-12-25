package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/** Encapsulate the formulas to spawn a particle on a cylinder shape. 
 * @author Inferno */
public final class CylinderSpawnShapeValue extends PrimitiveSpawnShapeValue{

	public CylinderSpawnShapeValue (CylinderSpawnShapeValue cylinderSpawnShapeValue) {
		super(cylinderSpawnShapeValue);
		load(cylinderSpawnShapeValue);
	}

	public CylinderSpawnShapeValue () {}

	@Override
	public void spawnAux (Vector3 vector, float percent) {
		//Generate the point on the surface of the sphere
		float width = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
		float height = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
		float depth = spawnDepth + (spawnDepthDiff * spawnDepthValue.getScale(percent));
		
		float radiusX, radiusZ;
		float hf = height / 2;
		float ty = MathUtils.random(height) - hf;
		
		//Where generate the point, on edges or inside ?
		if(edges ){
			radiusX = width / 2;
			radiusZ = depth/2;
		}
		else {
			radiusX = MathUtils.random(width)/2;
			radiusZ = MathUtils.random(depth)/2;
		}

		float 	spawnTheta = 0;
		
		//Generate theta
		boolean isRadiusXZero = radiusX == 0, isRadiusZZero = radiusZ == 0;
		if(!isRadiusXZero && !isRadiusZZero)
			spawnTheta = MathUtils.random(360f);
		else {
			if(isRadiusXZero) spawnTheta = MathUtils.random(1) == 0 ? -90 : 90;
			else if(isRadiusZZero) spawnTheta = MathUtils.random(1)==0 ? 0 : 180;
		}
		
		vector.set(radiusX*MathUtils.cosDeg(spawnTheta), ty, radiusZ*MathUtils.sinDeg(spawnTheta));
	}

	@Override
	public SpawnShapeValue copy () {
		return new CylinderSpawnShapeValue(this);
	}

}
