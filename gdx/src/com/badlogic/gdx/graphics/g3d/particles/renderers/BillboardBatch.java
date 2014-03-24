package com.badlogic.gdx.graphics.g3d.particles.renderers;

import java.util.Comparator;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader.AlignMode;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSorter;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSorter.DistanceParticleSorter;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData.SaveData;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSorter.BillboardDistanceParticleSorter;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/** @author Inferno */
public class BillboardBatch extends BufferedParticleBatch<BillboardParticle> {
	protected static final Vector3 TMP_V1 = new Vector3(), 
		 TMP_V2 = new Vector3(), 
		 TMP_V3 = new Vector3(), 
		 TMP_V4 = new Vector3(), 
		 TMP_V5 = new Vector3(),
		 TMP_V6 = new Vector3();
	protected static final Matrix3 TMP_M3 = new Matrix3();
	//Attributes	
	protected static final int sizeAndRotationUsage = 1 << 9, directionUsage = 1 << 10;
	private static final VertexAttributes 
		GPU_ATTRIBUTES = new VertexAttributes(new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE), 
															new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0"), 
															new VertexAttribute(Usage.Color, 4, ShaderProgram.COLOR_ATTRIBUTE), 
															new VertexAttribute(sizeAndRotationUsage, 4, "a_sizeAndRotation")),
		GPU_EXT_ATTRIBUTES = new VertexAttributes(new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE), 
									new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0"), 
									new VertexAttribute(Usage.Color, 4, ShaderProgram.COLOR_ATTRIBUTE), 
									new VertexAttribute(sizeAndRotationUsage, 4, "a_sizeAndRotation"),
									new VertexAttribute(directionUsage, 3, "a_direction")),
		CPU_ATTRIBUTES = new VertexAttributes(	new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE), 
															new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0"), 
															new VertexAttribute(Usage.Color, 4, ShaderProgram.COLOR_ATTRIBUTE) );
	
	//Offsets
	private static final int 	GPU_POSITION_OFFSET = (short)(GPU_ATTRIBUTES.findByUsage(Usage.Position).offset/4),
										GPU_UV_OFFSET = (short)(GPU_ATTRIBUTES.findByUsage(Usage.TextureCoordinates).offset/4),
										GPU_SIZE_ROTATION_OFFSET = (short)(GPU_ATTRIBUTES.findByUsage(sizeAndRotationUsage).offset/4),
										GPU_COLOR_OFFSET = (short)(GPU_ATTRIBUTES.findByUsage(Usage.Color).offset/4),
										GPU_VERTEX_SIZE = GPU_ATTRIBUTES.vertexSize/4,

										//Ext
										GPU_EXT_POSITION_OFFSET = (short)(GPU_EXT_ATTRIBUTES.findByUsage(Usage.Position).offset/4),
										GPU_EXT_UV_OFFSET = (short)(GPU_EXT_ATTRIBUTES.findByUsage(Usage.TextureCoordinates).offset/4),
										GPU_EXT_SIZE_ROTATION_OFFSET = (short)(GPU_EXT_ATTRIBUTES.findByUsage(sizeAndRotationUsage).offset/4),
										GPU_EXT_COLOR_OFFSET = (short)(GPU_EXT_ATTRIBUTES.findByUsage(Usage.Color).offset/4),
										GPU_EXT_DIRECTION_OFFSET = (short)(GPU_EXT_ATTRIBUTES.findByUsage(directionUsage).offset/4),
										GPU_EXT_VERTEX_SIZE = GPU_EXT_ATTRIBUTES.vertexSize/4,
										
										//Cpu
										CPU_POSITION_OFFSET = (short)(CPU_ATTRIBUTES.findByUsage(Usage.Position).offset/4),
										CPU_UV_OFFSET = (short)(CPU_ATTRIBUTES.findByUsage(Usage.TextureCoordinates).offset/4),
										CPU_COLOR_OFFSET = (short)(CPU_ATTRIBUTES.findByUsage(Usage.Color).offset/4),
										CPU_VERTEX_SIZE= CPU_ATTRIBUTES.vertexSize/4;
	private final static int 	MAX_PARTICLES_PER_MESH = Short.MAX_VALUE/4,
										MAX_VERTICES_PER_MESH = MAX_PARTICLES_PER_MESH*4;
	
	private class RenderablePool extends Pool<Renderable>{
		public RenderablePool () {}

		@Override
		public Renderable newObject () {
			return allocRenderable();
		}	
	}
	
	private static class Config{
		public Config(){}
		public Config (boolean useGPU, AlignMode mode) {
			this.useGPU = useGPU;
			this.mode = mode;
		}
		boolean useGPU;
		AlignMode mode;
	}
	
	private RenderablePool renderablePool;
	private Array<Renderable> renderables;
	private float[] vertices;
	private short[] indices;
	private int currentVertexSize = 0;
	private VertexAttributes currentAttributes;
	protected boolean useGPU;
	protected AlignMode mode;
	protected Texture texture;
	Shader shader;
	
	
	public BillboardBatch(AlignMode mode, boolean useGPU, int capacity){
		super(BillboardParticle.class, new BillboardDistanceParticleSorter());
		this.mode = mode;
		this.useGPU = useGPU;
		renderables = new Array<Renderable>();
		renderablePool = new RenderablePool();
		setVertexData();
		ensureCapacity(capacity);
	}

	public BillboardBatch () {
		this(AlignMode.Screen, false, 100);
	}
	
	public BillboardBatch (int capacity) {
		this(AlignMode.Screen, false, capacity);
	}

	@Override
	public void allocParticlesData(int capacity){
		super.allocParticlesData(capacity);
		allocRenderables(capacity);
	}

	protected Renderable allocRenderable(){
		Renderable renderable = new Renderable();
		renderable.primitiveType = GL20.GL_TRIANGLES;
		renderable.meshPartOffset = 0;
		renderable.material = new Material(	new BlendingAttribute(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
			new DepthTestAttribute(GL20.GL_LEQUAL, false),
			TextureAttribute.createDiffuse(texture));
		renderable.mesh = new Mesh(false, MAX_VERTICES_PER_MESH, MAX_PARTICLES_PER_MESH*6, currentAttributes);
		renderable.shader = shader;
		return renderable;
	}
	
	private void allocRenderables(int capacity){			
		int 	verticesCount = Math.min(capacity * 4, MAX_VERTICES_PER_MESH),
				verticesFCount = verticesCount * currentVertexSize,
				indicesCount = (verticesCount/4)*6;
		vertices = new float[verticesFCount];
		indices = new short[indicesCount];
		
		//Free old meshes
		int 	meshCount = Math.max( capacity/MAX_PARTICLES_PER_MESH, 1),
				free = renderablePool.getFree();
		if(free < meshCount){
			for(int i=0, left = meshCount - free; i < left;++i)
				renderablePool.free(renderablePool.newObject());
		}
	}
	
	private Shader getShader (Renderable renderable) {
		Shader shader = useGPU 	? 	new ParticleShader(renderable, new ParticleShader.Config(mode)) :
								new DefaultShader(renderable);
		shader.init();
		return shader;
	}
	
	private void allocShader () {
		Renderable newRenderable = allocRenderable();
		shader = newRenderable.shader = getShader(newRenderable);
		renderablePool.free(newRenderable);	
	}
	
	private void clearRenderablesPool(){
		renderablePool.freeAll(renderables);
		for(int i=0, free = renderablePool.getFree(); i < free; ++i){
			Renderable renderable = renderablePool.obtain();
			renderable.mesh.dispose();
		}
		renderables.clear();
	}
	
	/** Sets vertex attributes and size */
	public void setVertexData(){
		if(useGPU){
			if(mode == AlignMode.ParticleDirection){
				currentAttributes = GPU_EXT_ATTRIBUTES;
				currentVertexSize = GPU_EXT_VERTEX_SIZE;
			}
			else{
				currentAttributes = GPU_ATTRIBUTES;
				currentVertexSize = GPU_VERTEX_SIZE;
			}
		}
		else {
			currentAttributes = CPU_ATTRIBUTES;
			currentVertexSize = CPU_VERTEX_SIZE;
		}
	}
	
	/** Allocates all the require rendering resources like Renderables,Shaders,Meshes
	 *  according to the current batch configuration.*/
	private void initRenderData () {
		setVertexData();
		clearRenderablesPool();
		allocShader();
		allocRenderables(bufferedParticles.length);
	}
	
	/** Sets the current align mode.
	 *  It will reallocate internal data, use only when necessary. */
	public void setAlignMode(AlignMode mode){
		if(mode != this.mode){
			this.mode = mode;
			if(useGPU){
				initRenderData();
			}
		}
	}
	
	public AlignMode getAlignMode(){
		return mode;
	}
	
	/** Sets the current align mode.
	*  It will reallocate internal data, use only when necessary. */
	public void setUseGpu(boolean useGPU){
		if(this.useGPU != useGPU){
			this.useGPU = useGPU;
			initRenderData();
		}
	}

	public boolean isUseGPU(){
		return useGPU;
	}
	
	public void setTexture(Texture texture){
		renderablePool.freeAll(renderables);
		renderables.clear();
		for(int i=0, free = renderablePool.getFree(); i < free; ++i){
			Renderable renderable = renderablePool.obtain();
			TextureAttribute attribute = (TextureAttribute) renderable.material.get(TextureAttribute.Diffuse);
			attribute.textureDescription.texture = texture;
		}
		this.texture = texture;
	}
	
	public Texture getTexture () {
		return texture;
	}
	

	//GPU
	//Required + Color + Rotation
	private static void putVertex(float[] vertices, int offset, BillboardParticle particle, float scaleX, float scaleY, float u, float v) {
		//Position
		vertices[offset + GPU_POSITION_OFFSET] = particle.x;
		vertices[offset + GPU_POSITION_OFFSET+1] = particle.y;
		vertices[offset + GPU_POSITION_OFFSET+2] = particle.z;
		//UV
		vertices[offset + GPU_UV_OFFSET] = u;
		vertices[offset + GPU_UV_OFFSET+1] = v;
		//Scale
		vertices[offset + GPU_SIZE_ROTATION_OFFSET] = scaleX;
		vertices[offset + GPU_SIZE_ROTATION_OFFSET+1] = scaleY;
		vertices[offset + GPU_SIZE_ROTATION_OFFSET+2] = particle.cosRotation;
		vertices[offset + GPU_SIZE_ROTATION_OFFSET+3] = particle.sinRotation;
		//Color
		vertices[offset + GPU_COLOR_OFFSET] = particle.r;
		vertices[offset + GPU_COLOR_OFFSET+1] = particle.g;
		vertices[offset + GPU_COLOR_OFFSET+2] = particle.b;
		vertices[offset + GPU_COLOR_OFFSET+3] = particle.a;
	}

	//Required + Color + Rotation + Direction
	private static void putVertex(float[] vertices, int offset, BillboardParticle particle, float scaleX, float scaleY, float u, float v, Vector3 direction) {
		//Position
		vertices[offset + GPU_EXT_POSITION_OFFSET] = particle.x;
		vertices[offset + GPU_EXT_POSITION_OFFSET+1] = particle.y;
		vertices[offset + GPU_EXT_POSITION_OFFSET+2] = particle.z;
		//UV
		vertices[offset + GPU_EXT_UV_OFFSET] = u;
		vertices[offset + GPU_EXT_UV_OFFSET+1] = v;
		//Scale
		vertices[offset + GPU_EXT_SIZE_ROTATION_OFFSET] = scaleX;
		vertices[offset + GPU_EXT_SIZE_ROTATION_OFFSET+1] = scaleY;
		vertices[offset + GPU_EXT_SIZE_ROTATION_OFFSET+2] = particle.cosRotation;
		vertices[offset + GPU_EXT_SIZE_ROTATION_OFFSET+3] = particle.sinRotation;
		//Color
		vertices[offset + GPU_EXT_COLOR_OFFSET] = particle.r;
		vertices[offset + GPU_EXT_COLOR_OFFSET+1] = particle.g;
		vertices[offset + GPU_EXT_COLOR_OFFSET+2] = particle.b;
		vertices[offset + GPU_EXT_COLOR_OFFSET+3] = particle.a;
		//Direction
		vertices[offset + GPU_EXT_DIRECTION_OFFSET] = direction.x;
		vertices[offset + GPU_EXT_DIRECTION_OFFSET +1] = direction.y;
		vertices[offset + GPU_EXT_DIRECTION_OFFSET +2] = direction.z;
	}
	
	//CPU
	//Required
	private static void putVertex(float[] vertices, int offset, Vector3 point, BillboardParticle particle, float u, float v) {
		//Position
		vertices[offset + CPU_POSITION_OFFSET] = point.x + particle.x;
		vertices[offset + CPU_POSITION_OFFSET+1] = point.y + particle.y;
		vertices[offset + CPU_POSITION_OFFSET+2] = point.z + particle.z;
		//UV
		vertices[offset + CPU_UV_OFFSET] = u;
		vertices[offset + CPU_UV_OFFSET+1] = v;
		//Color
		vertices[offset + CPU_COLOR_OFFSET] = particle.r;
		vertices[offset + CPU_COLOR_OFFSET+1] = particle.g;
		vertices[offset + CPU_COLOR_OFFSET+2] = particle.b;
		vertices[offset + CPU_COLOR_OFFSET+3] = particle.a;
	}

	@Override
	public void begin () {
		super.begin();
		renderablePool.freeAll(renderables);
		renderables.clear();
	}
	
	protected void flush(){
		int p=0;
		int leftVertexCount = bufferedParticlesCount*4;

		if(useGPU){
			if(mode != AlignMode.ParticleDirection){
				while(p< bufferedParticlesCount){
					Renderable renderable = renderablePool.obtain();
					int toAddVertexCount = Math.min(leftVertexCount, MAX_VERTICES_PER_MESH);
					int vertexFcount = 0, indicesCount = 0;

					for(int v =0; v < toAddVertexCount; v+=4, indicesCount+=6, ++p){
					BillboardParticle particle = bufferedParticles[p];
					float sx = particle.halfWidth * particle.scale, 
							sy = particle.halfHeight * particle.scale;

					//bottom left, bottom right, top right, top left
					putVertex(vertices, vertexFcount, particle, -sx, -sy, particle.u, particle.v2); vertexFcount+= currentVertexSize;
					putVertex(vertices, vertexFcount, particle, sx, -sy, particle.u2, particle.v2); vertexFcount+= currentVertexSize;
					putVertex(vertices, vertexFcount, particle, sx, sy, particle.u2, particle.v); vertexFcount+= currentVertexSize;			
					putVertex(vertices, vertexFcount, particle, -sx, sy, particle.u, particle.v); vertexFcount+= currentVertexSize;

					indices[indicesCount] = (short)v;
					indices[indicesCount+1] = (short)(v+1);
					indices[indicesCount+2] = (short)(v+2);
					indices[indicesCount+3] = (short)(v+2);
					indices[indicesCount+4] = (short)(v+3);
					indices[indicesCount+5] = (short)v;
				}

				renderable.meshPartSize = indicesCount;
				renderable.mesh.setVertices(vertices, 0, vertexFcount);
				renderable.mesh.setIndices(indices, 0, indicesCount);	
				leftVertexCount -= toAddVertexCount;
				renderables.add(renderable);
				}
			}
			else {
				while(p< bufferedParticlesCount){
					Renderable renderable = renderablePool.obtain();
					int toAddVertexCount = Math.min(leftVertexCount, MAX_VERTICES_PER_MESH);
					int vertexFcount = 0, indicesCount = 0;

					for(int v =0; v < toAddVertexCount; v+=4, indicesCount+=6, ++p){
						BillboardParticle particle = bufferedParticles[p];
						float sx = particle.scale * particle.halfWidth , 
							sy = particle.scale * particle.halfHeight;
						//bottom left, bottom right, top right, top left
						TMP_V1.set(particle.velocity).nor();
						putVertex(vertices, vertexFcount, particle, -sx, -sy, particle.u, particle.v2, TMP_V1); vertexFcount+= currentVertexSize;
						putVertex(vertices, vertexFcount, particle, sx, -sy,  particle.u2, particle.v2, TMP_V1); vertexFcount+= currentVertexSize;
						putVertex(vertices, vertexFcount, particle, sx, sy, particle.u2, particle.v, TMP_V1); vertexFcount+= currentVertexSize;			
						putVertex(vertices, vertexFcount, particle, -sx, sy, particle.u, particle.v, TMP_V1); vertexFcount+= currentVertexSize;

						indices[indicesCount] = (short)v;
						indices[indicesCount+1] = (short)(v+1);
						indices[indicesCount+2] = (short)(v+2);
						indices[indicesCount+3] = (short)(v+2);
						indices[indicesCount+4] = (short)(v+3);
						indices[indicesCount+5] = (short)v;
					}

					renderable.meshPartSize = indicesCount;
					renderable.mesh.setVertices(vertices, 0, vertexFcount);
					renderable.mesh.setIndices(indices, 0, indicesCount);	
					leftVertexCount -= toAddVertexCount;
					renderables.add(renderable);
				}
			}
		}
		else {
			if(mode == AlignMode.Screen){
				Vector3 look = TMP_V3.set(camera.direction).scl(-1),  //normal
						right = TMP_V4.set(camera.up).crs(look).nor(), //tangent
						up = camera.up;
				while(p< bufferedParticlesCount){
					Renderable renderable = renderablePool.obtain();
					int toAddVertexCount = Math.min(leftVertexCount, MAX_VERTICES_PER_MESH);
					int vertexFcount = 0, indicesCount = 0;
					
					for(int v =0; v < toAddVertexCount; v+=4, indicesCount+=6, ++p){
						BillboardParticle particle = bufferedParticles[p];
						float sx = particle.halfWidth * particle.scale, 
							sy = particle.halfHeight * particle.scale;
						TMP_V1.set(right).scl(sx);
						TMP_V2.set(up).scl(sy);

						if(particle.cosRotation != 1){
							TMP_M3.setToRotation(look, particle.cosRotation, particle.sinRotation);
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).sub(TMP_V1).sub(TMP_V2).mul(TMP_M3), particle,  particle.u, particle.v2); vertexFcount+= currentVertexSize;
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).add(TMP_V1).sub(TMP_V2).mul(TMP_M3), particle,  particle.u2, particle.v2); vertexFcount+= currentVertexSize;
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).add(TMP_V1).add(TMP_V2).mul(TMP_M3), particle,  particle.u2, particle.v); vertexFcount+= currentVertexSize;			
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).sub(TMP_V1).add(TMP_V2).mul(TMP_M3), particle,  particle.u, particle.v); vertexFcount+= currentVertexSize;
						}
						else {
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).sub(TMP_V1).sub(TMP_V2), particle,  particle.u, particle.v2); vertexFcount+= currentVertexSize;
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).add(TMP_V1).sub(TMP_V2), particle,  particle.u2, particle.v2); vertexFcount+= currentVertexSize;
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).add(TMP_V1).add(TMP_V2), particle,  particle.u2, particle.v); vertexFcount+= currentVertexSize;			
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).sub(TMP_V1).add(TMP_V2), particle,  particle.u, particle.v); vertexFcount+= currentVertexSize;
						}

						indices[indicesCount] = (short)v;
						indices[indicesCount+1] = (short)(v+1);
						indices[indicesCount+2] = (short)(v+2);
						indices[indicesCount+3] = (short)(v+2);
						indices[indicesCount+4] = (short)(v+3);
						indices[indicesCount+5] = (short)v;
					}

					renderable.meshPartSize = indicesCount;
					renderable.mesh.setVertices(vertices, 0, vertexFcount);
					renderable.mesh.setIndices(indices, 0, indicesCount);	
					leftVertexCount -= toAddVertexCount;
					renderables.add(renderable);
				}
			}
			else if(mode == AlignMode.ViewPoint){
				while(p< bufferedParticlesCount){
					Renderable renderable = renderablePool.obtain();
					int toAddVertexCount = Math.min(leftVertexCount, MAX_VERTICES_PER_MESH);
					int vertexFcount = 0, indicesCount = 0;

					for(int v =0; v < toAddVertexCount; v+=4, indicesCount+=6, ++p){
						BillboardParticle particle = bufferedParticles[p];
						Vector3 look = TMP_V3.set(camera.position).sub(particle.x, particle.y, particle.z).nor(), //normal
							right = TMP_V4.set(camera.up).crs(look).nor(), //tangent
							up = TMP_V5.set(look).crs(right);
						TMP_M3.setToRotation(look, particle.cosRotation, particle.sinRotation);
						right.scl(particle.scale * particle.halfWidth );
						up.scl(particle.scale * particle.halfHeight );

						if(particle.cosRotation != 1){
							TMP_M3.setToRotation(look, particle.cosRotation, particle.sinRotation);
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).sub(right).sub(up).mul(TMP_M3), particle,  particle.u, particle.v2); vertexFcount+= currentVertexSize;
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).add(right).sub(up).mul(TMP_M3), particle,  particle.u2, particle.v2); vertexFcount+= currentVertexSize;
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).add(right).add(up).mul(TMP_M3), particle,  particle.u2, particle.v); vertexFcount+= currentVertexSize;			
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).sub(right).add(up).mul(TMP_M3), particle,  particle.u, particle.v); vertexFcount+= currentVertexSize;
						}
						else {
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).sub(right).sub(up), particle,  particle.u, particle.v2); vertexFcount+= currentVertexSize;
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).add(right).sub(up), particle,  particle.u2, particle.v2); vertexFcount+= currentVertexSize;
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).add(right).add(up), particle,  particle.u2, particle.v); vertexFcount+= currentVertexSize;			
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).sub(right).add(up), particle,  particle.u, particle.v); vertexFcount+= currentVertexSize;
						}

						indices[indicesCount] = (short)v;
						indices[indicesCount+1] = (short)(v+1);
						indices[indicesCount+2] = (short)(v+2);
						indices[indicesCount+3] = (short)(v+2);
						indices[indicesCount+4] = (short)(v+3);
						indices[indicesCount+5] = (short)v;
					}
					renderable.meshPartSize = indicesCount;
					renderable.mesh.setVertices(vertices, 0, vertexFcount);
					renderable.mesh.setIndices(indices, 0, indicesCount);	
					leftVertexCount -= toAddVertexCount;
					renderables.add(renderable);
				}
			}
			else if(mode == AlignMode.ParticleDirection){
				while(p< bufferedParticlesCount){
					Renderable renderable = renderablePool.obtain();
					int toAddVertexCount = Math.min(leftVertexCount, MAX_VERTICES_PER_MESH);
					int vertexFcount = 0, indicesCount = 0;

					for(int v =0; v < toAddVertexCount; v+=4, indicesCount+=6, ++p){
						BillboardParticle particle = bufferedParticles[p];
						Vector3 up = TMP_V1.set(particle.velocity).nor(),		
							look = TMP_V3.set(camera.position).sub(particle.x, particle.y, particle.z).nor(), //normal
							right = TMP_V4.set(up).crs(look).nor(); //tangent

						look.set(right).crs(up).nor();
						TMP_M3.setToRotation(look, particle.cosRotation, particle.sinRotation);

						right.scl(particle.scale * particle.halfWidth);
						up.scl(particle.scale * particle.halfHeight);

						if(particle.cosRotation != 1){
							TMP_M3.setToRotation(look, particle.cosRotation, particle.sinRotation);
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).sub(right).sub(up).mul(TMP_M3), particle,  particle.u, particle.v2); vertexFcount+= CPU_VERTEX_SIZE;
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).add(right).sub(up).mul(TMP_M3), particle,  particle.u2, particle.v2); vertexFcount+= CPU_VERTEX_SIZE;
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).add(right).add(up).mul(TMP_M3), particle,  particle.u2, particle.v); vertexFcount+= CPU_VERTEX_SIZE;			
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).sub(right).add(up).mul(TMP_M3), particle,  particle.u, particle.v); vertexFcount+= CPU_VERTEX_SIZE;
						}
						else {
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).sub(right).sub(up), particle,  particle.u, particle.v2); vertexFcount+= CPU_VERTEX_SIZE;
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).add(right).sub(up), particle,  particle.u2, particle.v2); vertexFcount+= CPU_VERTEX_SIZE;
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).add(right).add(up), particle,  particle.u2, particle.v); vertexFcount+= CPU_VERTEX_SIZE;			
							putVertex(vertices, vertexFcount, TMP_V6.set(0,0,0).sub(right).add(up), particle,  particle.u, particle.v); vertexFcount+= CPU_VERTEX_SIZE;
						}

						indices[indicesCount] = (short)v;
						indices[indicesCount+1] = (short)(v+1);
						indices[indicesCount+2] = (short)(v+2);
						indices[indicesCount+3] = (short)(v+2);
						indices[indicesCount+4] = (short)(v+3);
						indices[indicesCount+5] = (short)v;
					}
					renderable.meshPartSize = indicesCount;
					renderable.mesh.setVertices(vertices, 0, vertexFcount);
					renderable.mesh.setIndices(indices, 0, indicesCount);	
					leftVertexCount -= toAddVertexCount;
					renderables.add(renderable);
				}
			}
		}
	}
	
	@Override
	public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
		if(bufferedParticlesCount > 0){
			for(Renderable renderable : this.renderables){
				renderables.add(pool.obtain().set(renderable));
			}
		}
	}

	@Override
	public void save (AssetManager manager, ResourceData resources) {
		SaveData data = resources.createSaveData("billboardBatch");
		data.save("cfg", new Config(useGPU, mode));
		data.saveAsset(manager.getAssetFileName(texture), Texture.class);
	}

	@Override
	public void load (AssetManager manager, ResourceData resources) {
		SaveData data = resources.getSaveData("billboardBatch");
		setTexture((Texture)manager.get(data.loadAsset()));
		Config cfg = (Config)data.load("cfg");
		setUseGpu(cfg.useGPU);
		setAlignMode(cfg.mode);
	}

}
