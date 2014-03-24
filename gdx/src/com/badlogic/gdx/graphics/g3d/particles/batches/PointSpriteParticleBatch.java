package com.badlogic.gdx.graphics.g3d.particles.batches;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSorter.PointSpriteDistanceParticleSorter;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader.ParticleType;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSorter.DistanceParticleSorter;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData.SaveData;
import com.badlogic.gdx.graphics.g3d.particles.PointSpriteParticle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;

/** @author Inferno */
public class PointSpriteParticleBatch extends BufferedParticleBatch<PointSpriteParticle> {
	protected static final Vector3 TMP_V1 = new Vector3();
	protected static final int sizeAndRotationUsage = 1 << 9;
	protected static final VertexAttributes CPU_ATTRIBUTES = new VertexAttributes(
		new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
		new VertexAttribute(Usage.Color, 4, ShaderProgram.COLOR_ATTRIBUTE),
		new VertexAttribute(Usage.TextureCoordinates, 4, "a_region"),
		new VertexAttribute(sizeAndRotationUsage, 3, "a_sizeAndRotation"));
	protected static final int CPU_VERTEX_SIZE = (short)(CPU_ATTRIBUTES.vertexSize / 4),
										CPU_POSITION_OFFSET = (short)(CPU_ATTRIBUTES.findByUsage(Usage.Position).offset/4),
										CPU_COLOR_OFFSET = (short)(CPU_ATTRIBUTES.findByUsage(Usage.Color).offset/4),
										CPU_REGION_OFFSET = (short)(CPU_ATTRIBUTES.findByUsage(Usage.TextureCoordinates).offset/4),
										CPU_SIZE_AND_ROTATION_OFFSET = (short)(CPU_ATTRIBUTES.findByUsage(sizeAndRotationUsage).offset/4);

	private float[] vertices;
	Renderable renderable;
	
	public PointSpriteParticleBatch () {
		this(1000);
	}
	
	public PointSpriteParticleBatch (int capacity) {
		super(PointSpriteParticle.class, new PointSpriteDistanceParticleSorter());
		allocRenderable();
		ensureCapacity(capacity);
		renderable.shader = new ParticleShader(renderable, new ParticleShader.Config(ParticleType.Point));
		renderable.shader.init();
	}

	@Override
	protected void allocParticlesData(int capacity){
		super.allocParticlesData(capacity);
		vertices = new float[capacity * CPU_VERTEX_SIZE];
		if(renderable.mesh != null) 
			renderable.mesh.dispose();
		renderable.mesh = new Mesh(false, capacity, 0, CPU_ATTRIBUTES);
	}
	
	protected void allocRenderable(){
		renderable = new Renderable();
		renderable.primitiveType = GL20.GL_POINTS;
		renderable.meshPartOffset = 0;
		renderable.material = new Material(	new BlendingAttribute(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
			new DepthTestAttribute(GL20.GL_LEQUAL, false),
			TextureAttribute.createDiffuse(null));
	}

	public static void init () {
		Gdx.gl.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
		if(Gdx.app.getType() == ApplicationType.Desktop) 
			Gdx.gl20.glEnable(0x8861); // GL_POINT_OES
	}

	public void setTexture(Texture texture){
		TextureAttribute attribute = (TextureAttribute) renderable.material.get(TextureAttribute.Diffuse);
		attribute.textureDescription.texture = texture;
	}

	public Texture getTexture () {
		TextureAttribute attribute = (TextureAttribute) renderable.material.get(TextureAttribute.Diffuse);
		return attribute.textureDescription.texture;
	}


	private static void putVertex(float[] vertices, int offset, PointSpriteParticle particle) {
		//Position
		vertices[offset + CPU_POSITION_OFFSET] = particle.x;
		vertices[offset + CPU_POSITION_OFFSET+1] = particle.y;
		vertices[offset + CPU_POSITION_OFFSET+2] = particle.z;

		//Color
		vertices[offset + CPU_COLOR_OFFSET] = particle.r;
		vertices[offset + CPU_COLOR_OFFSET+1] = particle.g;
		vertices[offset + CPU_COLOR_OFFSET+2] = particle.b;
		vertices[offset + CPU_COLOR_OFFSET+3] = particle.a;
		
		//Scale
		vertices[offset + CPU_SIZE_AND_ROTATION_OFFSET] = particle.scale;
		vertices[offset + CPU_SIZE_AND_ROTATION_OFFSET+1] = particle.cosRotation;
		vertices[offset + CPU_SIZE_AND_ROTATION_OFFSET+2] = particle.sinRotation;
		
		//UV
		vertices[offset + CPU_REGION_OFFSET] = particle.u;
		vertices[offset + CPU_REGION_OFFSET+1] = particle.v;
		vertices[offset + CPU_REGION_OFFSET+2] = particle.u2;
		vertices[offset + CPU_REGION_OFFSET+3] = particle.v2;
	}
	
	protected void flush(){
		short vo = 0; // the current vertex
		int fo = 0; // the current offset in the vertex array
		for (int i = 0; i < bufferedParticlesCount; ++i) {
			putVertex(vertices, fo, bufferedParticles[i]); 
			fo+= CPU_VERTEX_SIZE;
			++vo;
		}
		renderable.meshPartSize = vo;
		renderable.mesh.setVertices(vertices, 0, fo);
	}

	@Override
	public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
		if(bufferedParticlesCount > 0)
			renderables.add(pool.obtain().set(renderable));
	}

	@Override
	public void save (AssetManager manager, ResourceData resources) {
		SaveData data = resources.createSaveData("pointSpriteBatch");
		data.saveAsset(manager.getAssetFileName(getTexture()), Texture.class);
	}

	@Override
	public void load (AssetManager manager, ResourceData resources) {
		SaveData data = resources.getSaveData("pointSpriteBatch");
		setTexture((Texture)manager.get(data.loadAsset()));
	}
	

}
