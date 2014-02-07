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
package com.badlogic.gdx.graphics.g3d.particles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;

/** A particle emitter.
 * It extends Emitter to handle particles custom properties like
 * position, rotation, color, etc...
 * It can be rendered on a ModelBatch too*/
public class ParticleEmitter extends Emitter<Particle> implements Disposable{
	private static final VertexAttributes ATTRIBUTES = new VertexAttributes(
			new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
			new VertexAttribute(Usage.Color, 4, ShaderProgram.COLOR_ATTRIBUTE),
			new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0"));
	
	private static final int VERTEX_SIZE = (short)(ATTRIBUTES.vertexSize / 4),
							 POSITION_OFFSET = (short)(ATTRIBUTES.findByUsage(Usage.Position).offset/4),
							 COLOR_OFFSET = (short)(ATTRIBUTES.findByUsage(Usage.Color).offset/4),
							 UV_OFFSET = (short)(ATTRIBUTES.findByUsage(Usage.TextureCoordinates).offset/4);
	private static final Vector3 TMP_V1 = new Vector3(), 
								 TMP_V2 = new Vector3(), 
								 TMP_V3 = new Vector3(), 
								 TMP_V4 = new Vector3(), 
								 TMP_V5 = new Vector3(),
								 TMP_V6 = new Vector3();
	private static final Quaternion TMP_Q1 = new Quaternion();
	
	
	static private final int UPDATE_SCALE = 1 << 0;
	static private final int UPDATE_ROTATION = 1 << 1;
	static private final int UPDATE_VELOCITY_0 = 1 << 2;
	static private final int UPDATE_VELOCITY_1 = 1 << 3;
	static private final int UPDATE_VELOCITY_2 = 1 << 4;
	static private final int UPDATE_TINT = 1 << 5;
	
	public ScaledNumericValue scaleValue,
	rotationValue,
	transparencyValue,
	spawnWidthValue,
	spawnHeightValue,
	spawnDepthValue;
	
	VelocityValue velocity0Value, velocity1Value, velocity2Value;
	public GradientColorValue tintValue;
	public RangedNumericValue xOffsetValue, yOffsetValue, zOffsetValue;
	public SpawnShapeValue spawnShapeValue;
	public AlignmentValue facingValue;

	private String imagePath;
	private Renderable renderable;
	private TextureRegion region;
	private Vector3 position;
	private Quaternion orientation; //The orientation and rotation of the emitter
	private float scaleX, scaleY, scaleZ;
	private float[] vertices;
	private short[] indices;
	private boolean isAdditive, isAttached;
	private int flags;
	private float spawnWidth, spawnWidthDiff;
	private float spawnHeight, spawnHeightDiff;
	private float spawnDepth, spawnDepthDiff;
	private float halfParticlesWidth, halfParticlesHeight;
	private BoundingBox boundingBox;
	
	public ParticleEmitter (BufferedReader reader) throws IOException {
		super(reader);
	}

	public ParticleEmitter (ParticleEmitter emitter) {
		super(emitter);
		scaleValue.load(emitter.scaleValue);
		rotationValue.load(emitter.rotationValue);
		velocity0Value.load(emitter.velocity0Value);
		velocity1Value.load(emitter.velocity1Value);
		velocity2Value.load(emitter.velocity2Value);
		transparencyValue.load(emitter.transparencyValue);
		tintValue.load(emitter.tintValue);
		xOffsetValue.load(emitter.xOffsetValue);
		yOffsetValue.load(emitter.yOffsetValue);
		spawnWidthValue.load(emitter.spawnWidthValue);
		spawnHeightValue.load(emitter.spawnHeightValue);
		spawnShapeValue.load(emitter.spawnShapeValue);
		facingValue.load(emitter.facingValue);
		setRegion(emitter.region);
		setAttached(emitter.isAttached);
		setAdditive(emitter.isAdditive);
		setOrientation(emitter.getOrientation());
		setPosition(emitter.getPosition());
		setScale(emitter.getScale(TMP_V1) );
		//setScale(emitter.getScale());
		//behind = emitter.behind;
	}
	
	public ParticleEmitter() {
		initialize();
		setAdditive(true);
		setAttached(false);
		setMaxParticleCount(100);
	}
	
	@Override
	protected void initialize() {
		super.initialize();

		scaleValue = new ScaledNumericValue();
		rotationValue = new ScaledNumericValue();
		velocity0Value = new VelocityValue();
		velocity1Value = new VelocityValue();
		velocity2Value = new VelocityValue();
		transparencyValue = new ScaledNumericValue();
		tintValue = new GradientColorValue();
		xOffsetValue = new ScaledNumericValue();
		yOffsetValue = new ScaledNumericValue();
		zOffsetValue = new ScaledNumericValue();
		spawnWidthValue = new ScaledNumericValue();
		spawnHeightValue = new ScaledNumericValue();
		spawnDepthValue = new ScaledNumericValue();
		spawnShapeValue = new SpawnShapeValue();
		facingValue = new AlignmentValue();
		
		scaleValue.setActive(true);
		transparencyValue.setActive(true);
		spawnShapeValue.setActive(true);
		spawnWidthValue.setActive(true);
		spawnHeightValue.setActive(true);
		spawnDepthValue.setActive(true);
		facingValue.setActive(true);
		tintValue.setActive(true);
		
		position = new Vector3(0,0,0);
		scaleX = scaleY = scaleZ = 1f;
		orientation = new Quaternion();
		halfParticlesWidth = halfParticlesHeight = 0.5f;
		
		//Create the renderable
		renderable = new Renderable();
		renderable.primitiveType = GL10.GL_TRIANGLES;
		renderable.meshPartOffset = 0;
		renderable.material = new Material(new BlendingAttribute(1f),
											new DepthTestAttribute(GL10.GL_LEQUAL, false), 
											TextureAttribute.createDiffuse(null));
		renderable.worldTransform.idt();
	}

	private Mesh allocMesh(int particleCount){
		vertices = new float[particleCount * 4 * VERTEX_SIZE];
		indices = new short[particleCount * 6];
		return new Mesh(false, particleCount * 4, particleCount * 6, ATTRIBUTES);
	}
	
	@Override
	protected void restart() {
		super.restart();
		spawnWidth = spawnWidthValue.newLowValue();
		spawnWidthDiff = spawnWidthValue.newHighValue();
		if (!spawnWidthValue.isRelative()) spawnWidthDiff -= spawnWidth;

		spawnHeight = spawnHeightValue.newLowValue();
		spawnHeightDiff = spawnHeightValue.newHighValue();
		if (!spawnHeightValue.isRelative()) spawnHeightDiff -= spawnHeight;
		
		spawnDepth = spawnDepthValue.newLowValue();
		spawnDepthDiff = spawnDepthValue.newHighValue();
		if (!spawnDepthValue.isRelative()) spawnDepthDiff -= spawnDepth;

		flags = 0;
		//if (thetaValue.active && thetaValue.timeline.length > 1) mFlags |= UPDATE_THETA_ANGLE;
		//if (phiValue.active && phiValue.timeline.length > 1) mFlags |= UPDATE_PHI_ANGLE;
		if (velocity0Value.active) flags |= UPDATE_VELOCITY_0;
		if (velocity1Value.active) flags |= UPDATE_VELOCITY_1;
		if (velocity2Value.active) flags |= UPDATE_VELOCITY_2;
		if (scaleValue.timeline.length > 1) flags |= UPDATE_SCALE;
		if (rotationValue.active && rotationValue.timeline.length > 1) flags |= UPDATE_ROTATION;
		if (tintValue.timeline.length > 1) flags |= UPDATE_TINT;
	}
	
	private void updateTextureAttribute(){
		TextureAttribute attribute = (TextureAttribute) renderable.material.get(TextureAttribute.Diffuse);
		attribute.textureDescription.texture = region.getTexture();
		//float aspect = (float)mRegion.getRegionWidth()/mRegion.getRegionHeight();
		//particlesHeight = (particlesWidth/aspect);
		float invAspect = (float)region.getRegionHeight()/region.getRegionWidth();
		halfParticlesHeight = invAspect*0.5f;
	}
	
	public void setRegion(TextureRegion region){
		this.region = region;
		if(region != null) updateTextureAttribute();
	}
	
	public void setRegionFromTexture(Texture texture) {
		region = texture != null ? new TextureRegion(texture) : null;
		if(region != null) updateTextureAttribute();
	}
	
	public TextureRegion getRegion() {
		return region;
	}
	
	public void setAdditive(boolean isAdditive){
		BlendingAttribute blendingAttribute = (BlendingAttribute) renderable.material.get(BlendingAttribute.Type);
		if(isAdditive)
		{
			blendingAttribute.sourceFunction =  GL10.GL_SRC_ALPHA; 
			blendingAttribute.destFunction = GL10.GL_ONE;
		}
		else 
		{
			blendingAttribute.sourceFunction = GL10.GL_SRC_ALPHA; 
			blendingAttribute.destFunction = GL10.GL_ONE_MINUS_SRC_ALPHA;
		}
		this.isAdditive = isAdditive;
	}
	
	public Vector3 getPosition() {
		return position;
	}

	public void setPosition(Vector3 position) {
		setPosition(position.x, position.y, position.z);
	}
	
	public void setPosition (float x, float y, float z) {
		if (isAttached) 
		{
			for (int i = 0; i < activeCount; ++i)
			{
				Particle particle = particles[i];
				particle.ox = x;
				particle.oy = y;
				particle.oz = z;
			}
		}
		position.set(x, y, z);
	}
	
	/** Set the orientation of the emitter, if attached all the particles will get the same exact orientation */
	public void setOrientation(Quaternion orientation){
		if(isAttached)
		{
			for (int i = 0; i < activeCount; ++i)
			{
				Particle particle = particles[i];
				particle.qx = orientation.x;
				particle.qy = orientation.y;
				particle.qz = orientation.z;
				particle.qw = orientation.w;
			}
		}
		this.orientation.set(orientation);
	}
	
	public void setOrientation(Vector3 axis, float angle) {
		setOrientation(TMP_Q1.set(axis, angle));
	}
	
	public void rotate(Vector3 axis, float angle) {
		setOrientation(TMP_Q1.set(axis,angle).mul(orientation));
	}

	public Quaternion getOrientation(){
		return orientation;
	}

	public void setScale(float x, float y, float z){
		scaleX = x;
		scaleY = y;
		scaleZ = z;
	}
	
	public void setScale(Vector3 scale){
		scaleX = scale.x;
		scaleY = scale.y;
		scaleZ = scale.z;
	}
	
	public void setScale(float scale) {
		scaleX = scaleY = scaleZ = scale;
	}

	public float getScale(){
		return scaleX;
	}
	
	public Vector3 getScale(Vector3 scale){
		return scale.set(scaleX, scaleY, scaleZ);
	}

	@Override
	protected Particle[] allocParticles(int particleCount) {
		boolean isMeshNull = renderable.mesh == null;
		if(isMeshNull|| (renderable.mesh.getMaxVertices() < particleCount*4))
		{
			if(!isMeshNull) renderable.mesh.dispose(); 
			renderable.mesh = allocMesh(particleCount);
		}
		Particle[] particles = new Particle[particleCount];
		for(int i =0; i < particleCount; ++i) particles[i] = new Particle();
		return particles;
	}
	
	private void setVelocityData(VelocityValue velocityValue, float[] velocityData){
		velocityData[Particle.VEL_STRENGTH_INDEX] = velocityValue.strength.newLowValue();
		velocityData[Particle.VEL_STRENGTH_INDEX+1] = velocityValue.strength.newHighValue();
		if (!velocityValue.strength.isRelative()) velocityData[Particle.VEL_STRENGTH_INDEX+1] -= velocityData[Particle.VEL_STRENGTH_INDEX];

		if(velocityValue.theta.active)
		{
			velocityData[Particle.VEL_THETA_INDEX] = velocityValue.theta.newLowValue();
			velocityData[Particle.VEL_THETA_INDEX+1] = velocityValue.theta.newHighValue();
			if (!velocityValue.theta.isRelative()) velocityData[Particle.VEL_THETA_INDEX+1] -= velocityData[Particle.VEL_THETA_INDEX];
		}
		else 
		{ 
			velocityData[Particle.VEL_THETA_INDEX] = velocityData[Particle.VEL_THETA_INDEX +1] = 0;
		}
			
		if(velocityValue.phi.active)
		{
			velocityData[Particle.VEL_PHI_INDEX] = velocityValue.phi.newLowValue();
			velocityData[Particle.VEL_PHI_INDEX+1] = velocityValue.phi.newHighValue();
			if (!velocityValue.phi.isRelative())  velocityData[Particle.VEL_PHI_INDEX+1] -= velocityData[Particle.VEL_PHI_INDEX];
		}
		else
		{
			velocityData[Particle.VEL_PHI_INDEX] = velocityData[Particle.VEL_PHI_INDEX+1] = 0;
		}
	}
	
	@Override
	protected void activateParticle(Particle particle) {	
		//Gdx.app.log("INFERNO", "Activating particle");
		float percent = durationTimer / (float)duration;
		
		if (velocity0Value.active) setVelocityData(velocity0Value, particle.velocity0Data);
		if (velocity1Value.active) setVelocityData(velocity1Value, particle.velocity1Data);
		if (velocity2Value.active) setVelocityData(velocity2Value, particle.velocity2Data);

		//float spriteWidth = particlesWidth;
		particle.scaleStart = scaleValue.newLowValue();
		particle.scaleDiff = scaleValue.newHighValue();
		if (!scaleValue.isRelative()) particle.scaleDiff -= particle.scaleStart;
		particle.scale = particle.scaleStart + particle.scaleDiff * scaleValue.getScale(0);
		
		if (rotationValue.active)
		{
			particle.rotationStart = rotationValue.newLowValue();
			particle.rotationDiff = rotationValue.newHighValue();
			if (!rotationValue.isRelative()) particle.rotationDiff -= particle.rotationStart;
			particle.rotation = particle.rotationStart + particle.rotationDiff * rotationValue.getScale(0);
		}
		else particle.rotation = 0;


		float[] temp = tintValue.getColor(0);
		particle.tintR = temp[0];
		particle.tintG = temp[1];
		particle.tintB = temp[2];

		particle.transparencyStart = transparencyValue.newLowValue();
		particle.transparencyDiff = transparencyValue.newHighValue() - particle.transparencyStart;

		// Spawn.
		float 	x = 0, y = 0, z = 0;
		if (xOffsetValue.active) x += xOffsetValue.newLowValue();
		if (yOffsetValue.active) y += yOffsetValue.newLowValue();
		if (zOffsetValue.active) z += zOffsetValue.newLowValue();
		
		switch (spawnShapeValue.shape) 
		{
		case rectangle: 
		{
			float width = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
			float height = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
			float depth = spawnDepth + (spawnDepthDiff * spawnDepthValue.getScale(percent));
			//Where generate the point, on edges or inside ?
			if(spawnShapeValue.edges)
			{
					int a = MathUtils.random(-1,1);
					float tx=0, ty=0, tz=0;
					if(a == -1)
					{
						//X
						tx = MathUtils.random(1)==0 ? -width/ 2 : width/ 2; 
						if(tx == 0)
						{
							ty = MathUtils.random(1)==0 ? -height / 2 : height/ 2;	
							tz = MathUtils.random(1)==0 ? -depth/2 : depth/2;
						}
						else 
						{
							ty = MathUtils.random(height) - height / 2;	
							tz = MathUtils.random(depth) - depth / 2;
						}
					}
					else if(a == 0)
					{
						//Z
						tz = MathUtils.random(1)==0 ? -depth/ 2 : depth/ 2; 
						if(tz == 0)
						{
							ty = MathUtils.random(1)==0 ? -height / 2 : height/ 2;	
							tx = MathUtils.random(1)==0 ? -width/2 : width/2;
						}
						else 
						{
							ty = MathUtils.random(height) - height / 2;	
							tx = MathUtils.random(width) - width / 2;
						}
					}
					else 
					{
						//Y
						ty = MathUtils.random(1)==0 ? -height/ 2 : height / 2; 
						if(ty == 0)
						{
							tx = MathUtils.random(1)==0 ? -width / 2 : width / 2;	
							tz = MathUtils.random(1)==0 ? -depth/2 : depth/2;
						}
						else 
						{
							tx = MathUtils.random(width) - width / 2;	
							tz = MathUtils.random(depth) - depth / 2;
						}
					}			
					x += tx; y+= ty; z +=tz;
			}
			else 
			{
				x += MathUtils.random(width) - width / 2;
				y += MathUtils.random(height) - height / 2;
				z += MathUtils.random(depth) - depth/2;	
			}
			
			break;
		}
		case sphere: 
		{
			//Generate the point on the surface of the sphere
			float width = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
			float height = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
			float depth = spawnDepth + (spawnDepthDiff * spawnDepthValue.getScale(percent));
			
			float radiusX, radiusY, radiusZ;
			//Where generate the point, on edges or inside ?
			if(spawnShapeValue.edges)
			{
				radiusX = width / 2;
				radiusY = height / 2;
				radiusZ = depth/2;
			}
			else 
			{
				radiusX = MathUtils.random(width)/2;
				radiusY = MathUtils.random(height)/2;
				radiusZ = MathUtils.random(depth)/2;
			}

			float 	spawnTheta = 0, spawnPhi;
			
			//Generate theta
			boolean isRadiusXZero = radiusX == 0, isRadiusZZero = radiusZ == 0;
			if(!isRadiusXZero && !isRadiusZZero)spawnTheta = MathUtils.random(360f);
			else 
			{
				if(isRadiusXZero) spawnTheta = MathUtils.random(0, 1) == 0 ? -90 : 90;
				else if(isRadiusZZero) spawnTheta = MathUtils.random(0, 1)*180;
			}
			
			//Generate phi
			if(radiusY == 0) spawnPhi = 0;
			else
			{
				switch (spawnShapeValue.side) 
				{
				case top:
				{
					spawnPhi = MathUtils.random(179f);
					break;
				}
				case bottom:
				{
					spawnPhi = -MathUtils.random(179f);
					break;
				}
				default:
				{
					spawnPhi = MathUtils.random(360f);
					break;
				}
				}
			}

			TMP_V1.set(Vector3.X).rotate(Vector3.Y, spawnTheta);
			TMP_V2.set(TMP_V1).crs(Vector3.Y);
			TMP_V1.rotate(TMP_V2, spawnPhi ).scl(radiusX, radiusY, radiusZ);

			x += TMP_V1.x;
			y += TMP_V1.y;
			z += TMP_V1.z;
			break;
		}
		case cylinder:
		{
			//Generate the point on the surface of the sphere
			float width = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
			float height = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
			float depth = spawnDepth + (spawnDepthDiff * spawnDepthValue.getScale(percent));
			
			float radiusX, radiusZ;
			float hf = height / 2;
			float ty = MathUtils.random(height) - hf;
			
			//Where generate the point, on edges or inside ?
			if(spawnShapeValue.edges && Math.abs(ty) != hf )
			{
				radiusX = width / 2;
				radiusZ = depth/2;
			}
			else 
			{
				radiusX = MathUtils.random(width)/2;
				radiusZ = MathUtils.random(depth)/2;
			}

			float 	spawnTheta = 0;
			
			//Generate theta
			boolean isRadiusXZero = radiusX == 0, isRadiusZZero = radiusZ == 0;
			if(!isRadiusXZero && !isRadiusZZero)spawnTheta = MathUtils.random(360f);
			else 
			{
				if(isRadiusXZero) spawnTheta = MathUtils.random(1) == 0 ? -90 : 90;
				else if(isRadiusZZero) spawnTheta = MathUtils.random(1)==0 ? 0 : 180;
			}

			TMP_V1.set(Vector3.X).rotate(Vector3.Y, spawnTheta).scl(radiusX, 0, radiusZ);
			TMP_V1.y = ty;

			x += TMP_V1.x;
			y += TMP_V1.y;
			z += TMP_V1.z;
			break;
		}
		
		case line: 
		{
			float width = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
			float height = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
			float depth = spawnDepth + (spawnDepthDiff * spawnDepthValue.getScale(percent));

			float a = MathUtils.random();
			x += a * width;
			y += a * height;
			z += a * depth;
			break;
		}
		}

		//Position
		particle.x = x; particle.y = y; particle.z = z;
		particle.ox = position.x; particle.oy = position.y; particle.oz = position.z;
		
		//Orientation
		particle.qx = orientation.x; 
		particle.qy = orientation.y;
		particle.qz = orientation.z;
		particle.qw = orientation.w;
	}
	
	protected void addVelocity(Particle particle, Vector3 velocity, VelocityValue velocityValue, float[] velocityData, float percent, float delta){
		float strength = (velocityData[Particle.VEL_STRENGTH_INDEX] + velocityData[Particle.VEL_STRENGTH_INDEX+1] * velocityValue.strength.getScale(percent)) * delta;
		if(velocityValue.type == VelocityType.centripetal)
		{
			//Centripetal
			TMP_V3.set(particle.x, particle.y, particle.z).nor();
		}
		else if(velocityValue.type == VelocityType.tangential)
		{
			//Tangential
			float phi = velocityData[Particle.VEL_PHI_INDEX] + velocityData[Particle.VEL_PHI_INDEX+1] * velocityValue.phi.getScale(percent),  
				  theta = velocityData[Particle.VEL_THETA_INDEX] + velocityData[Particle.VEL_THETA_INDEX+1] * velocityValue.theta.getScale(percent);

			TMP_V3.set(Vector3.X).rotate(Vector3.Y, theta);
			TMP_V2.set(TMP_V3).crs(Vector3.Y);
			TMP_V3.rotate(TMP_V2, phi);
			TMP_V3.crs(TMP_V2.set(particle.x, particle.y, particle.z).nor());
		}
		else 
		{
			//Polar
			float phi = velocityData[Particle.VEL_PHI_INDEX] + velocityData[Particle.VEL_PHI_INDEX+1] * velocityValue.phi.getScale(percent),  
				  theta = velocityData[Particle.VEL_THETA_INDEX] + velocityData[Particle.VEL_THETA_INDEX+1] * velocityValue.theta.getScale(percent);
			
			TMP_V3.set(Vector3.X).rotate(Vector3.Y, theta);
			TMP_V2.set(TMP_V3).crs(Vector3.Y);
			TMP_V3.rotate(TMP_V2, phi);
		}
		
		velocity.add(TMP_V3.scl(strength));
	}

	public void update(float delta) {
		super.update(delta);
		for(int i=0; i< activeCount; ++i)
		{
			Particle particle = particles[i];
			float percent = 1 - particle.currentLife / (float)particle.life;

			if ((flags & UPDATE_SCALE) != 0)
				particle.scale = particle.scaleStart + particle.scaleDiff * scaleValue.getScale(percent);

			Vector3 velocity = TMP_V1.set(0, 0, 0);
			if ((flags & UPDATE_VELOCITY_0) != 0)addVelocity(particle, velocity, velocity0Value, particle.velocity0Data, percent, delta);
			if ((flags & UPDATE_VELOCITY_1) != 0)addVelocity(particle, velocity, velocity1Value, particle.velocity1Data, percent, delta);
			if ((flags & UPDATE_VELOCITY_2) != 0) addVelocity(particle, velocity, velocity2Value, particle.velocity2Data, percent, delta);
				
			
			particle.x += velocity.x; particle.y += velocity.y; particle.z += velocity.z;
			
			if(facingValue.align == Align.particleDirection)
			{
				//Direction is used only in this case for now
				velocity.nor();
				particle.dirX = velocity.x;
				particle.dirY = velocity.y;
				particle.dirZ = velocity.z;
			}
			
			if ((flags & UPDATE_ROTATION) != 0) particle.rotation = particle.rotationStart + particle.rotationDiff * rotationValue.getScale(percent);

			if ((flags & UPDATE_TINT) != 0)
			{
				float[] color = tintValue.getColor(percent);
				particle.tintR = color[0];
				particle.tintG = color[1];
				particle.tintB = color[2];
			}
			
			particle.transparency = particle.transparencyStart + particle.transparencyDiff * transparencyValue.getScale(percent);
		}
	}

	private void updateMesh(Camera camera) {

		short vo = 0; // the current vertex
		int fo = 0; // the current offset in the vertex array
		int io = 0; // the current offset in the indices array
		float 	u = region.getU(), v = region.getV(), 
				u2 = region.getU2(), v2 = region.getV2();				
		
		float hwidth = halfParticlesWidth*scaleX, hheight = halfParticlesHeight *scaleY;
		
		if(facingValue.align == Align.screen)
		{
			Vector3 look = TMP_V3.set(camera.direction).scl(-1),  //normal
					right = TMP_V4.set(camera.up).crs(look).nor(), //tangent
					up = camera.up;
			
			for (int i = 0; i < activeCount; ++i) 
			{
				Particle particle = particles[i];
				TMP_V1.set(right).scl(particle.scale * hwidth);
				TMP_V2.set(up).scl(particle.scale * hheight);

				TMP_V5.set(particle.x, particle.y, particle.z).scl(scaleX, scaleY, scaleZ)
						.mul(TMP_Q1.set(particle.qx, particle.qy, particle.qz, particle.qw ))
						.add(particle.ox, particle.oy, particle.oz );
				
				//bottom left
				putVertex(vertices, fo, TMP_V6.set(0,0,0).sub(TMP_V1).sub(TMP_V2).rotate(look, particle.rotation).add(TMP_V5.x, TMP_V5.y, TMP_V5.z), particle.tintR, particle.tintG, particle.tintB, particle.transparency,  u, v2); fo+= VERTEX_SIZE;
				//bottom right
				putVertex(vertices, fo, TMP_V6.set(0,0,0).add(TMP_V1).sub(TMP_V2).rotate(look, particle.rotation).add(TMP_V5.x, TMP_V5.y, TMP_V5.z), particle.tintR, particle.tintG, particle.tintB, particle.transparency, u2, v2); fo+= VERTEX_SIZE;
				//top right
				putVertex(vertices, fo, TMP_V6.set(0,0,0).add(TMP_V1).add(TMP_V2).rotate(look, particle.rotation).add(TMP_V5.x, TMP_V5.y, TMP_V5.z), particle.tintR, particle.tintG, particle.tintB, particle.transparency, u2, v); fo+= VERTEX_SIZE;			
				//top left
				putVertex(vertices, fo, TMP_V6.set(0,0,0).sub(TMP_V1).add(TMP_V2).rotate(look, particle.rotation).add(TMP_V5.x, TMP_V5.y, TMP_V5.z), particle.tintR, particle.tintG, particle.tintB, particle.transparency, u, v); fo+= VERTEX_SIZE;

				indices[io] = vo;
				indices[io+1] = (short)(vo+1);
				indices[io+2] = (short)(vo+2);
				indices[io+3] = (short)(vo+2);
				indices[io+4] = (short)(vo+3);
				indices[io+5] = vo;
				vo += 4;
				io += 6;
			}
		}
		else if(facingValue.align == Align.viewPoint)
		{
			for (int i = 0; i < activeCount; ++i) 
			{
				Particle particle = particles[i];
				
				TMP_V1.set(particle.x, particle.y, particle.z).scl(scaleX, scaleY, scaleZ)
					.mul(TMP_Q1.set(particle.qx, particle.qy, particle.qz, particle.qw ))
					.add(particle.ox, particle.oy, particle.oz );
				
				Vector3 look = TMP_V3.set(camera.position).sub(TMP_V1).nor(), //normal
						right = TMP_V4.set(camera.up).crs(look).nor(), //tangent
						up = TMP_V5.set(look).crs(right);
				
				right.scl(particle.scale * hwidth);
				up.scl(particle.scale * hheight);

				
				//bottom left
				putVertex(vertices, fo, TMP_V6.set(0,0,0).sub(right).sub(up).rotate(look, particle.rotation).add(TMP_V1), particle.tintR, particle.tintG, particle.tintB, particle.transparency,  u, v2); fo+= VERTEX_SIZE;
				//bottom right
				putVertex(vertices, fo, TMP_V6.set(0,0,0).add(right).sub(up).rotate(look, particle.rotation).add(TMP_V1), particle.tintR, particle.tintG, particle.tintB, particle.transparency, u2, v2); fo+= VERTEX_SIZE;
				//top right
				putVertex(vertices, fo, TMP_V6.set(0,0,0).add(right).add(up).rotate(look, particle.rotation).add(TMP_V1), particle.tintR, particle.tintG, particle.tintB, particle.transparency, u2, v); fo+= VERTEX_SIZE;			
				//top left
				putVertex(vertices, fo, TMP_V6.set(0,0,0).sub(right).add(up).rotate(look, particle.rotation).add(TMP_V1), particle.tintR, particle.tintG, particle.tintB, particle.transparency, u, v); fo+= VERTEX_SIZE;

				indices[io] = vo;
				indices[io+1] = (short)(vo+1);
				indices[io+2] = (short)(vo+2);
				indices[io+3] = (short)(vo+2);
				indices[io+4] = (short)(vo+3);
				indices[io+5] = vo;
				vo += 4;
				io += 6;
			}
		}
		else if(facingValue.align == Align.particleDirection)
		{
			for (int i = 0; i < activeCount; ++i) 
			{
				Particle particle = particles[i];
				
				TMP_V1.set(particle.x, particle.y, particle.z).scl(scaleX, scaleY, scaleZ)
					.mul(TMP_Q1.set(particle.qx, particle.qy, particle.qz, particle.qw ))
					.add(particle.ox, particle.oy, particle.oz );
				
				Vector3 up = TMP_V5.set(particle.dirX, particle.dirY, particle.dirZ).nor(),		
						look = TMP_V3.set(camera.position).sub(TMP_V1).nor(), //normal
						right = TMP_V4.set(up).crs(look); //tangent
				
				look.set(right).crs(up);
				
				right.scl(particle.scale * hwidth);
				up.scl(particle.scale * hheight);

				//bottom left
				putVertex(vertices, fo, TMP_V6.set(0,0,0).sub(right).sub(up).rotate(look, particle.rotation).add(TMP_V1), particle.tintR, particle.tintG, particle.tintB, particle.transparency,  u, v2); fo+= VERTEX_SIZE;
				//bottom right
				putVertex(vertices, fo, TMP_V6.set(0,0,0).add(right).sub(up).rotate(look, particle.rotation).add(TMP_V1), particle.tintR, particle.tintG, particle.tintB, particle.transparency, u2, v2); fo+= VERTEX_SIZE;
				//top right
				putVertex(vertices, fo, TMP_V6.set(0,0,0).add(right).add(up).rotate(look, particle.rotation).add(TMP_V1), particle.tintR, particle.tintG, particle.tintB, particle.transparency, u2, v); fo+= VERTEX_SIZE;			
				//top left
				putVertex(vertices, fo, TMP_V6.set(0,0,0).sub(right).add(up).rotate(look, particle.rotation).add(TMP_V1), particle.tintR, particle.tintG, particle.tintB, particle.transparency, u, v); fo+= VERTEX_SIZE;

				indices[io] = vo;
				indices[io+1] = (short)(vo+1);
				indices[io+2] = (short)(vo+2);
				indices[io+3] = (short)(vo+2);
				indices[io+4] = (short)(vo+3);
				indices[io+5] = vo;
				vo += 4;
				io += 6;
			}
		}

		renderable.meshPartSize = io;
		renderable.mesh.setVertices(vertices, 0, fo);
		renderable.mesh.setIndices(indices, 0, io);
	}

	private void putVertex(float[] vertices, int offset, Vector3 position, float red, float green, float blue , float alpha, float u, float v) {
		vertices[offset + POSITION_OFFSET] = position.x;
		vertices[offset + POSITION_OFFSET+1] = position.y;
		vertices[offset + POSITION_OFFSET+2] = position.z;
		vertices[offset + COLOR_OFFSET] = red;
		vertices[offset + COLOR_OFFSET+1] = green;
		vertices[offset + COLOR_OFFSET+2] = blue;
		vertices[offset + COLOR_OFFSET+3] = alpha;
		vertices[offset + UV_OFFSET] = u;
		vertices[offset + UV_OFFSET+1] = v;
	}
	
	public void render (ModelBatch batch) {
		updateMesh(batch.getCamera());
		batch.render(renderable);
	}
	
	public Renderable getRenderable() {
		return renderable;
	}
	
	public String getImagePath () {
		return imagePath;
	}

	public void setImagePath (String aImagePath) {
		imagePath = aImagePath;
	}
	
	public GradientColorValue getTint () {
		return tintValue;
	}
	
	public VelocityValue getVelocityValue (int index) {
		if(index == 0)return velocity0Value;
		if(index == 1)return velocity1Value;
		if(index == 2)return velocity2Value;
		return null;
	}

	public ScaledNumericValue getScaleValue () {
		return scaleValue;
	}
	
	public ScaledNumericValue getRotation() {
		return rotationValue;
	}

	public ScaledNumericValue getTransparency () {
		return transparencyValue;
	}

	public RangedNumericValue getXOffsetValue () {
		return xOffsetValue;
	}

	public RangedNumericValue getYOffsetValue () {
		return yOffsetValue;
	}
	
	public RangedNumericValue getZOffsetValue () {
		return zOffsetValue;
	}

	public ScaledNumericValue getSpawnWidth () {
		return spawnWidthValue;
	}

	public ScaledNumericValue getSpawnHeight () {
		return spawnHeightValue;
	}
	
	public ScaledNumericValue getSpawnDepth () 
	{
		return spawnDepthValue;
	}

	public SpawnShapeValue getSpawnShape () {
		return spawnShapeValue;
	}
	
	public boolean isAttached () {
		return isAttached;
	}

	public void setAttached (boolean attached) {
		isAttached = attached;
	}

	public boolean isAdditive () {
		return isAdditive;
	}

	/** Returns the bounding box for all active particles (builds it using particle position as center, corners are excluded)*/
	public BoundingBox getBoundingBox () {
		if (boundingBox == null) boundingBox = new BoundingBox();

		Particle[] particles = this.particles;

		boundingBox.inf();
		float 	minX = 0, maxX = 0,
				minY = 0, maxY = 0,
				minZ = 0, maxZ = 0;
				
		for (int i = 0; i < activeCount; ++i)
		{
			Particle particle = particles[i];
			minX = Math.min(minX, particle.x);
			minY = Math.min(minY, particle.y);
			minZ = Math.min(minZ, particle.z);
			maxX = Math.min(maxX, particle.x);
			maxY = Math.min(maxY, particle.y);
			maxZ = Math.min(maxZ, particle.z);
		}
		boundingBox.set(TMP_V1.set(minX, minY, minZ), TMP_V2.set(maxX, maxY, maxZ));
		return boundingBox;
	}
	
	
	public void save (Writer output) throws IOException {
		super.save(output);
		output.write("- X Offset - \n");
		xOffsetValue.save(output);
		output.write("- Y Offset - \n");
		yOffsetValue.save(output);
		output.write("- Z Offset - \n");
		zOffsetValue.save(output);
		output.write("- Spawn Shape - \n");
		spawnShapeValue.save(output);
		output.write("- Spawn Width - \n");
		spawnWidthValue.save(output);
		output.write("- Spawn Height - \n");
		spawnHeightValue.save(output);
		output.write("- Spawn Depth - \n");
		spawnDepthValue.save(output);
		output.write("- Scale - \n");
		scaleValue.save(output);
		output.write("- Velocity 1 - \n");
		velocity0Value.save(output);
		output.write("- Velocity 2 - \n");
		velocity1Value.save(output);
		output.write("- Velocity 3 - \n");
		velocity2Value.save(output);
		output.write("- Rotation - \n");
		rotationValue.save(output);
		output.write("- Tint - \n");
		tintValue.save(output);
		output.write("- Transparency - \n");
		transparencyValue.save(output);
		output.write("- Facing - \n");
		facingValue.save(output);
		output.write("- Options - \n");
		output.write("attached: " + isAttached + "\n");
		output.write("additive: " + isAdditive + "\n");
	}

	public void load (BufferedReader reader) throws IOException {
		super.load(reader);
		reader.readLine();
		xOffsetValue.load(reader);
		reader.readLine();
		yOffsetValue.load(reader);
		reader.readLine();
		zOffsetValue.load(reader);
		reader.readLine();
		spawnShapeValue.load(reader);
		reader.readLine();
		spawnWidthValue.load(reader);
		reader.readLine();
		spawnHeightValue.load(reader);
		reader.readLine();
		spawnDepthValue.load(reader);
		reader.readLine();
		scaleValue.load(reader);
		reader.readLine();
		velocity0Value.load(reader);
		reader.readLine();
		velocity1Value.load(reader);
		reader.readLine();
		velocity2Value.load(reader);
		reader.readLine();
		rotationValue.load(reader);
		reader.readLine();
		tintValue.load(reader);
		reader.readLine();
		transparencyValue.load(reader);
		reader.readLine();
		facingValue.load(reader);
		reader.readLine();
		setAttached(readBoolean(reader, "attached"));
		setAdditive(readBoolean(reader, "additive"));
	}

	@Override
	public void dispose() {
		if(renderable.mesh != null) renderable.mesh.dispose();
	}

	public AlignmentValue getFacingValue() {
		return facingValue;
	}

}
