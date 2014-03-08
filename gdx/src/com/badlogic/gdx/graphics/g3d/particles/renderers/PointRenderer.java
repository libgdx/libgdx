package com.badlogic.gdx.graphics.g3d.particles.renderers;

import java.util.Comparator;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.Particle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader;
import com.badlogic.gdx.graphics.g3d.particles.PointParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader.ParticleType;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader.RegionSizeAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Sort;

public class PointRenderer extends Renderer<PointParticle> {

	protected static final int scaleAndRotationUsage = 1 << 9;
	protected static final VertexAttributes CPU_ATTRIBUTES = new VertexAttributes(
		new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
		new VertexAttribute(Usage.Color, 4, ShaderProgram.COLOR_ATTRIBUTE),
		new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0"),
		new VertexAttribute(scaleAndRotationUsage, 3, "a_scaleAndRotation"));
	protected static final int CPU_VERTEX_SIZE = (short)(CPU_ATTRIBUTES.vertexSize / 4),
										CPU_POSITION_OFFSET = (short)(CPU_ATTRIBUTES.findByUsage(Usage.Position).offset/4),
										CPU_COLOR_OFFSET = (short)(CPU_ATTRIBUTES.findByUsage(Usage.Color).offset/4),
										CPU_UV_OFFSET = (short)(CPU_ATTRIBUTES.findByUsage(Usage.TextureCoordinates).offset/4),
										CPU_SCALE_AND_ROTATION_OFFSET = (short)(CPU_ATTRIBUTES.findByUsage(scaleAndRotationUsage).offset/4);
	
	protected class SortData<T>{
		float dist;
		T particle;
	}
	
	private float[] vertices;
	Renderable renderable;
	boolean isAdditive = false;
	Texture texture;
	float regionSizeX = 1, regionSizeY = 1;
	SortData<PointParticle>[] sortedParticles;
	Camera camera;
	
	@Override
	public void init () {
		
		int 	particleCount = controller.emitter.maxParticleCount,
			verticesFCount = particleCount * CPU_VERTEX_SIZE;
		boolean allocVertices = vertices == null || vertices.length != verticesFCount;
		if(allocVertices){
			vertices = new float[verticesFCount];
			sortedParticles = new SortData[particleCount];
			for(int i=0; i < particleCount; ++i)
				sortedParticles[i] = new SortData<PointParticle>();
		}

		//Renderable allocation
		boolean initShader = false;
		if(renderable == null){
			renderable = new Renderable();
			renderable.primitiveType = GL20.GL_POINTS;
			renderable.meshPartOffset = 0;
			renderable.material = new Material(	new BlendingAttribute(1f),
															new DepthTestAttribute(GL20.GL_LEQUAL, false),
															TextureAttribute.createDiffuse(null),
															new RegionSizeAttribute());
			initShader = true;
		}

		//Mesh allocation
		if(allocVertices){
			if(renderable.mesh != null) 
				renderable.mesh.dispose();
			renderable.mesh = new Mesh(false, particleCount, 0, CPU_ATTRIBUTES);
		}

		//Set the shader
		if(initShader){
			renderable.shader = new ParticleShader(renderable, new ParticleShader.Config(ParticleType.Point));
			renderable.shader.init();
		}
		
		Gdx.gl.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
		if(Gdx.app.getType() == ApplicationType.Desktop) 
			Gdx.gl20.glEnable(0x8861); // GL_POINT_OES
		setAdditive(isAdditive);
		setTexture(texture, regionSizeX, regionSizeY);
	}

	@Override
	public void update () {
		PointParticle[] particles = controller.particles;
		//if(!isAdditive) sort(particles);
		sort(particles);
		
		short vo = 0; // the current vertex
		int fo = 0; // the current offset in the vertex array
		for (int i = 0, count = controller.emitter.activeCount; i < count; ++i) {
			//putVertex(vertices, fo, particles[i]); 
			putVertex(vertices, fo, sortedParticles[i].particle); 
			fo+= CPU_VERTEX_SIZE;
			++vo;
		}
		renderable.meshPartSize = vo;
		renderable.mesh.setVertices(vertices, 0, fo);
	}
	
	private static final Comparator<SortData<PointParticle>> COMPARATOR  = new Comparator<SortData<PointParticle>>() {
		@Override
		public int compare (SortData<PointParticle> o1, SortData<PointParticle> o2) {
			return o1.dist < o2.dist ? -1 : o1.dist == o2.dist ? 0 : 1;
		}
	};
	
	private void sort (PointParticle[] particles) {
		float[] val = camera.view.val;
		TMP_V1.set(val[Matrix4.M20], val[Matrix4.M21], val[Matrix4.M22]);
		//TMP_V1.set(camera.position);
		for(int i=0,c = controller.emitter.activeCount; i <c; ++i){
			SortData<PointParticle> data = sortedParticles[i];
			PointParticle particle = particles[i];
			data.dist = TMP_V1.dot(particle.x, particle.y, particle.z);
			//data.dist = TMP_V1.dst2(particle.x, particle.y, particle.z);
			data.particle = particle;
		}
		Sort.instance().sort(sortedParticles, COMPARATOR, 0, controller.emitter.activeCount);
	}

	public void setAdditive (boolean isAdditive) {
		if(renderable != null){
			BlendingAttribute blendingAttribute = (BlendingAttribute) renderable.material.get(BlendingAttribute.Type);
			if(isAdditive){
				blendingAttribute.sourceFunction =  GL20.GL_SRC_ALPHA; 
				blendingAttribute.destFunction = GL20.GL_ONE;
			}
			else {
				blendingAttribute.sourceFunction = GL20.GL_SRC_ALPHA; 
				blendingAttribute.destFunction = GL20.GL_ONE_MINUS_SRC_ALPHA;
			}
		}
		this.isAdditive = isAdditive;
	}
	
	public boolean isAdditive(){
		return isAdditive;
	}
	
	public void setTexture(Texture texture){
		if(renderable != null){
			TextureAttribute attribute = (TextureAttribute) renderable.material.get(TextureAttribute.Diffuse);
			attribute.textureDescription.texture = texture;
			RegionSizeAttribute regionSizeAttribute = (RegionSizeAttribute) renderable.material.get(RegionSizeAttribute.RegionSize);
			regionSizeAttribute.regionSizeX = regionSizeX;
			regionSizeAttribute.regionSizeY = regionSizeY;
		}
		this.texture = texture;
	}
	
	public void setTexture(Texture texture, float regionSizeX, float regionSizeY){
		if(renderable != null){
			TextureAttribute attribute = (TextureAttribute) renderable.material.get(TextureAttribute.Diffuse);
			attribute.textureDescription.texture = texture;
			RegionSizeAttribute regionSizeAttribute = (RegionSizeAttribute) renderable.material.get(RegionSizeAttribute.RegionSize);
			regionSizeAttribute.regionSizeX = regionSizeX;
			regionSizeAttribute.regionSizeY = regionSizeY;
		}
		this.texture = texture;
		this.regionSizeX = regionSizeX;
		this.regionSizeY = regionSizeY;
	}
	
	public void setCamera(Camera camera){
		this.camera = camera;
	}
	
	
	@Override
	public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
		renderables.add(pool.obtain().set(renderable));
	}

	private static void putVertex(float[] vertices, int offset, PointParticle particle) {
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
		vertices[offset + CPU_SCALE_AND_ROTATION_OFFSET] = particle.scale;
		vertices[offset + CPU_SCALE_AND_ROTATION_OFFSET+1] = particle.cosRotation;
		vertices[offset + CPU_SCALE_AND_ROTATION_OFFSET+2] = particle.sinRotation;
		
		//UV
		vertices[offset + CPU_UV_OFFSET] = particle.u;
		vertices[offset + CPU_UV_OFFSET+1] = particle.v;
	}

	@Override
	public PointRenderer copy () {
		return new PointRenderer();
	}

}
