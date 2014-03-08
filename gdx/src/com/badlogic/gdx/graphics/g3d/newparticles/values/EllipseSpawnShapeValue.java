package com.badlogic.gdx.graphics.g3d.newparticles.values;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import com.badlogic.gdx.graphics.g3d.newparticles.Utils;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class EllipseSpawnShapeValue extends PrimitiveSpawnShapeValue {
	SpawnSide side = SpawnSide.both;
	
	public EllipseSpawnShapeValue (EllipseSpawnShapeValue ellipseSpawnShapeValue) {
		super(ellipseSpawnShapeValue);
	}

	public EllipseSpawnShapeValue () {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void spawnAux (Vector3 vector, float percent) {
		//Generate the point on the surface of the sphere
		float width = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
		float height = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
		float depth = spawnDepth + (spawnDepthDiff * spawnDepthValue.getScale(percent));
		
		float radiusX, radiusY, radiusZ;
		//Where generate the point, on edges or inside ?
		if(edges){
			radiusX = width / 2;
			radiusY = height / 2;
			radiusZ = depth/2;
		}
		else {
			radiusX = MathUtils.random(width)/2;
			radiusY = MathUtils.random(height)/2;
			radiusZ = MathUtils.random(depth)/2;
		}

		float theta =0, phi=0;
		//Generate theta
		boolean isRadiusXZero = radiusX == 0, isRadiusZZero = radiusZ == 0;
		if(!isRadiusXZero && !isRadiusZZero)
			//phi = acos(2 * MathUtils.random() - 1);
				phi = (float)Math.acos(2 * MathUtils.random() - 1);
			else {
			if(isRadiusXZero) phi = MathUtils.random(0, 1) == 0 ? -90 : 90;
			else if(isRadiusZZero) phi = MathUtils.random(0, 1)*180;
		}
		
		//Generate phi
		if(radiusY == 0) theta = 0;
		else{
			switch (side) 
			{
			case top:
			{
				theta = MathUtils.PI*MathUtils.random();
				break;
			}
			case bottom:
			{
				theta = -MathUtils.PI*MathUtils.random();
				break;
			}
			default:
			{
				//spawnPhi = MathUtils.random(360f);
				theta = 2 * MathUtils.PI * MathUtils.random();
				break;
			}
			}
		}
		
		float z = MathUtils.random(-1f, 1f);
		float t = MathUtils.random(0, MathUtils.PI2);
		float r = (float)Math.sqrt(1f - z*z);
		
		
		vector.set(radiusX * r * MathUtils.cos(t), radiusY * r * MathUtils.sin(t), radiusZ * z);
		
		/*
		vector.set(	radiusX * MathUtils.sin(phi) * MathUtils.cos(theta),
						radiusY * MathUtils.sin(phi) * MathUtils.sin(theta),
						radiusZ * MathUtils.cos(phi));
		*/
	}
	
	static final float scale_factor = .391f;   //empirical
	static final float HALF_PI = MathUtils.PI/2;
	private float acos(float x) {
		return HALF_PI - asin(x);
	}
	
	/** polynomial fast arcsine approximation, error bounds of ~1.5% until within 1%
	 * of domain bounds */
	private float asin(float x) {
		float x5 = x * x;     //x^2
		x5 *= x5;           //x^4
		x5 *= x;            //x^5
		return x + scale_factor*x5;
	}

	public SpawnSide getSide () {
		return side;
	}

	public void setSide (SpawnSide side) {
		this.side = side;
	}
	
	public void save (Writer output) throws IOException {
		super.save(output);
		if (!active) return;
		output.write("side: " + side + "\n");
	}

	public void load (BufferedReader reader) throws IOException {
		super.load(reader);
		if (!active) return;
		side = SpawnSide.valueOf(Utils.readString(reader, "side"));
	}

	@Override
	public void load (ParticleValue value) {
		super.load(value);
		EllipseSpawnShapeValue shape = (EllipseSpawnShapeValue) value;
		side = shape.side;
	}

	@Override
	public SpawnShapeValue copy () {
		return new EllipseSpawnShapeValue(this);
	}

}
